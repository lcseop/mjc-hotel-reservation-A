package com.mjc.hotel.hotels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjc.hotel.hotels.dto.TourApiHotelPreviewDto;
import com.mjc.hotel.hotels.dto.TourApiImportResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomIdCardEnum;
import com.mjc.hotel.room.entity.RoomPetAndSmokeEnum;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TourApiHotelImportService {

    private static final Long DEFAULT_HOTEL_TYPE_ID = 1L;
    private static final Long DEFAULT_ROOM_TYPE_ID = 1L;

    private final HotelRepository hotelRepository;
    private final HotelPhotoRepository hotelPhotoRepository;
    private final HotelTypeRepository hotelTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String PARAM_NUM_OF_ROWS = "numOfRows";
    private static final String PARAM_PAGE_NO = "pageNo";
    private static final String PARAM_CONTENT_TYPE_ID = "contentTypeId";

    @Value("${tour-api.service-key:}")
    private String serviceKey;

    @Value("${tour-api.base-url:https://apis.data.go.kr/B551011/KorService2}")
    private String baseUrl;

    @Value("${tour-api.mobile-os:ETC}")
    private String mobileOs;

    @Value("${tour-api.mobile-app:StayNow}")
    private String mobileApp;

    @Transactional
    public TourApiImportResponseDto importHotels(String keyword, int page, int size) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("TOUR_API_SERVICE_KEY 또는 tour-api.service-key 설정이 필요합니다.");
        }

        JsonNode items = searchTourApiItems(keyword, page, size);
        return saveTourApiItems(items, null);
    }

    public List<TourApiHotelPreviewDto> previewHotels(String keyword, int page, int size) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("TOUR_API_SERVICE_KEY 또는 tour-api.service-key 설정이 필요합니다.");
        }

        JsonNode items = searchTourApiItems(keyword, page, size);
        List<TourApiHotelPreviewDto> previews = new ArrayList<>();

        for (JsonNode item : items) {
            String title = cleanText(item.path("title").asText(""));
            String location = cleanText(firstNonBlank(item.path("addr1").asText(""), item.path("addr2").asText("")));

            if (title.isBlank() || location.isBlank()) {
                continue;
            }

            title = cut(title, 50);
            location = cut(location, 50);

            previews.add(TourApiHotelPreviewDto.builder()
                    .contentId(item.path("contentid").asText(""))
                    .title(title)
                    .location(location)
                    .imagePath(firstNonBlank(item.path("firstimage").asText(""), item.path("firstimage2").asText("")))
                    .latitude(toDouble(item.path("mapy").asText("")))
                    .longitude(toDouble(item.path("mapx").asText("")))
                    .alreadyImported(hotelRepository.existsByHotelNameAndLocation(title, location))
                    .build());
        }

        return previews;
    }

    @Transactional
    public TourApiImportResponseDto importSelectedHotels(String keyword, int page, int size, List<String> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return TourApiImportResponseDto.builder()
                    .requested(0)
                    .imported(0)
                    .skipped(0)
                    .build();
        }

        JsonNode items = searchTourApiItems(keyword, page, size);
        return saveTourApiItems(items, new HashSet<>(contentIds));
    }

    private TourApiImportResponseDto saveTourApiItems(JsonNode items, Set<String> selectedContentIds) {

        int requested = items.size();
        int imported = 0;
        int skipped = 0;

        for (JsonNode item : items) {
            if (selectedContentIds != null && !selectedContentIds.contains(item.path("contentid").asText(""))) {
                skipped++;
                continue;
            }

            String title = cleanText(item.path("title").asText(""));
            String location = cleanText(firstNonBlank(item.path("addr1").asText(""), item.path("addr2").asText("")));

            if (title.isBlank() || location.isBlank()) {
                skipped++;
                continue;
            }

            title = cut(title, 50);
            location = cut(location, 50);

            if (hotelRepository.existsByHotelNameAndLocation(title, location)) {
                skipped++;
                continue;
            }

            Hotel hotel = saveHotel(item, title, location);
            savePhotos(hotel, item);
            saveDefaultRooms(hotel);
            imported++;
        }

        return TourApiImportResponseDto.builder()
                .requested(requested)
                .imported(imported)
                .skipped(skipped)
                .build();
    }

    private JsonNode searchTourApiItems(String keyword, int page, int size) {
        String safeKeyword = keyword == null || keyword.isBlank() ? "호텔" : keyword.trim();
        String areaCode = resolveAreaCode(safeKeyword);
        JsonNode items = areaCode != null
                ? requestAreaStayItems(areaCode, page, size)
                : requestItems("/searchKeyword2",
                param(PARAM_NUM_OF_ROWS, String.valueOf(size)),
                param(PARAM_PAGE_NO, String.valueOf(page)),
                param(PARAM_CONTENT_TYPE_ID, "32"),
                param("keyword", hotelKeyword(safeKeyword))
        );

        if (areaCode != null && items.isEmpty()) {
            items = requestItems("/searchKeyword2",
                    param(PARAM_NUM_OF_ROWS, String.valueOf(size)),
                    param(PARAM_PAGE_NO, String.valueOf(page)),
                    param(PARAM_CONTENT_TYPE_ID, "32"),
                    param("keyword", hotelKeyword(safeKeyword))
            );
        }

        return items;
    }

    private Hotel saveHotel(JsonNode item, String title, String location) {
        HotelType type = hotelTypeRepository.findById(DEFAULT_HOTEL_TYPE_ID).orElseThrow();
        String overview = loadOverview(item.path("contentid").asText(""), item.path("contenttypeid").asText("32"));

        Hotel hotel = Hotel.builder()
                .type(type)
                .hotelName(title)
                .hotelPrice(defaultPrice(title))
                .location(location)
                .starRating(3)
                .description(overview.isBlank() ? title + " 숙박 정보입니다." : overview)
                .latitude(toDouble(item.path("mapy").asText("")))
                .longitude(toDouble(item.path("mapx").asText("")))
                .build();

        return hotelRepository.save(hotel);
    }

    private void savePhotos(Hotel hotel, JsonNode item) {
        List<String> images = new ArrayList<>();
        addIfNotBlank(images, item.path("firstimage").asText(""));
        addIfNotBlank(images, item.path("firstimage2").asText(""));

        String contentId = item.path("contentid").asText("");
        if (!contentId.isBlank()) {
            JsonNode detailImages = requestItems("/detailImage2",
                    param("contentId", contentId),
                    param("imageYN", "Y"),
                    param("subImageYN", "Y"),
                    param(PARAM_NUM_OF_ROWS, "10"),
                    param(PARAM_PAGE_NO, "1")
            );

            for (JsonNode image : detailImages) {
                addIfNotBlank(images, image.path("originimgurl").asText(""));
                addIfNotBlank(images, image.path("smallimageurl").asText(""));
            }
        }

        images.stream()
                .filter(image -> image.length() <= 255)
                .distinct()
                .limit(8)
                .forEach(image -> hotelPhotoRepository.save(
                        HotelPhoto.builder()
                                .hotel(hotel)
                                .imagePath(image)
                                .build()
                ));
    }

    private void saveDefaultRooms(Hotel hotel) {
        RoomType roomType = roomTypeRepository.findById(DEFAULT_ROOM_TYPE_ID).orElseThrow();
        int basePrice = hotel.getHotelPrice();

        List<Room> rooms = List.of(
                buildRoom(hotel, roomType, "스탠다드 룸", basePrice, 301, 3, 28, 2),
                buildRoom(hotel, roomType, "디럭스 더블룸", basePrice + 40000, 501, 5, 36, 3),
                buildRoom(hotel, roomType, "패밀리 룸", basePrice + 80000, 701, 7, 48, 4)
        );

        roomRepository.saveAll(rooms);
    }

    private Room buildRoom(Hotel hotel, RoomType roomType, String name, int price, int number, int floor, int area, int people) {
        return Room.builder()
                .hotelId(hotel)
                .roomTypeId(roomType)
                .roomName(name)
                .roomPrice(price)
                .roomNumber(number)
                .floor(floor)
                .area(area)
                .maximumPeople(people)
                .checkInTime(15)
                .checkOutTime(11)
                .parking("가능")
                .pet(RoomPetAndSmokeEnum.BAN)
                .smoke(RoomPetAndSmokeEnum.BAN)
                .idCard(RoomIdCardEnum.ESSENTIAL)
                .build();
    }

    private String loadOverview(String contentId, String contentTypeId) {
        if (contentId == null || contentId.isBlank()) {
            return "";
        }

        JsonNode items = requestItems("/detailCommon2",
                param("contentId", contentId),
                param(PARAM_CONTENT_TYPE_ID, contentTypeId == null || contentTypeId.isBlank() ? "32" : contentTypeId),
                param("defaultYN", "Y"),
                param("firstImageYN", "Y"),
                param("areacodeYN", "Y"),
                param("addrinfoYN", "Y"),
                param("mapinfoYN", "Y"),
                param("overviewYN", "Y"),
                param(PARAM_NUM_OF_ROWS, "1"),
                param(PARAM_PAGE_NO, "1")
        );

        if (items.isEmpty()) {
            return "";
        }

        return cleanText(items.get(0).path("overview").asText(""));
    }

    private JsonNode requestItems(String path, String... params) {
        try {
            URI uri = URI.create(baseUrl + path + "?" + buildQuery(params));
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("TourAPI 호출 실패: HTTP " + response.statusCode() + " / " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode item = root.path("response").path("body").path("items").path("item");

            if (item.isArray()) {
                return item;
            }

            if (item.isObject()) {
                return objectMapper.createArrayNode().add(item);
            }

            return objectMapper.createArrayNode();
        } catch (IOException e) {
            throw new IllegalStateException("TourAPI 응답을 읽지 못했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("TourAPI 호출이 중단되었습니다.", e);
        }
    }

    private JsonNode requestAreaStayItems(String areaCode, int page, int size) {
        JsonNode items = requestItems("/searchStay2",
                param(PARAM_NUM_OF_ROWS, String.valueOf(size)),
                param(PARAM_PAGE_NO, String.valueOf(page)),
                param("areaCode", areaCode),
                param("listYN", "Y"),
                param("arrange", "A")
        );

        if (!items.isEmpty()) {
            return items;
        }

        return requestItems("/areaBasedList2",
                param(PARAM_NUM_OF_ROWS, String.valueOf(size)),
                param(PARAM_PAGE_NO, String.valueOf(page)),
                param(PARAM_CONTENT_TYPE_ID, "32"),
                param("areaCode", areaCode),
                param("listYN", "Y"),
                param("arrange", "A")
        );
    }

    private String buildQuery(String... params) {
        List<String> query = new ArrayList<>();
        query.add("serviceKey=" + encodeServiceKey(serviceKey));
        query.add("_type=json");
        query.add("MobileOS=" + encode(mobileOs));
        query.add("MobileApp=" + encode(mobileApp));

        for (String param : params) {
            query.add(param);
        }

        return String.join("&", query);
    }

    private String param(String key, String value) {
        return encode(key) + "=" + encode(value);
    }

    private String resolveAreaCode(String keyword) {
        if (keyword == null) {
            return null;
        }

        String value = keyword.replace(" ", "");

        return areaCodes().entrySet()
                .stream()
                .filter(e -> value.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String hotelKeyword(String keyword) {
        String value = keyword == null || keyword.isBlank() ? "호텔" : keyword.trim();
        String compact = value.replace(" ", "");

        if (compact.contains("호텔")
                || compact.contains("숙박")
                || compact.contains("리조트")
                || compact.contains("펜션")
                || compact.contains("모텔")
                || compact.contains("게스트하우스")) {
            return value;
        }

        return value + " 호텔";
    }

    private String encodeServiceKey(String value) {
        return value.contains("%") ? value : encode(value);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private void addIfNotBlank(List<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value.trim());
        }
    }

    private Double toDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer defaultPrice(String title) {
        int priceStep = Math.floorMod(title.hashCode(), 9);
        return 90000 + priceStep * 20000;
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .trim();
    }

    private String cut(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private Map<String, String> areaCodes() {
        return Map.ofEntries(
                Map.entry("서울", "1"),
                Map.entry("인천", "2"),
                Map.entry("대전", "3"),
                Map.entry("대구", "4"),
                Map.entry("광주", "5"),
                Map.entry("부산", "6"),
                Map.entry("울산", "7"),
                Map.entry("세종", "8"),
                Map.entry("경기", "31"),
                Map.entry("강원", "32"),
                Map.entry("충북", "33"),
                Map.entry("충청북", "33"),
                Map.entry("충남", "34"),
                Map.entry("충청남", "34"),
                Map.entry("경북", "35"),
                Map.entry("경상북", "35"),
                Map.entry("경남", "36"),
                Map.entry("경상남", "36"),
                Map.entry("전북", "37"),
                Map.entry("전라북", "37"),
                Map.entry("전남", "38"),
                Map.entry("전라남", "38"),
                Map.entry("제주", "39")
        );
    }
}
