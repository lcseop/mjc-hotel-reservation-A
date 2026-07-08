package com.mjc.hotel.hotels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;

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

        String safeKeyword = keyword == null || keyword.isBlank() ? "호텔" : keyword.trim();
        String areaCode = resolveAreaCode(safeKeyword);
        JsonNode items = areaCode != null
                ? requestItems("/areaBasedList2",
                        param("numOfRows", String.valueOf(size)),
                        param("pageNo", String.valueOf(page)),
                        param("contentTypeId", "32"),
                        param("areaCode", areaCode),
                        param("listYN", "Y"),
                        param("arrange", "Q")
                )
                : requestItems("/searchKeyword2",
                        param("numOfRows", String.valueOf(size)),
                        param("pageNo", String.valueOf(page)),
                        param("contentTypeId", "32"),
                        param("keyword", safeKeyword)
                );

        int requested = items.size();
        int imported = 0;
        int skipped = 0;

        for (JsonNode item : items) {
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
                    param("numOfRows", "10"),
                    param("pageNo", "1")
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
                param("contentTypeId", contentTypeId == null || contentTypeId.isBlank() ? "32" : contentTypeId),
                param("defaultYN", "Y"),
                param("firstImageYN", "Y"),
                param("areacodeYN", "Y"),
                param("addrinfoYN", "Y"),
                param("mapinfoYN", "Y"),
                param("overviewYN", "Y"),
                param("numOfRows", "1"),
                param("pageNo", "1")
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

        if (value.contains("서울")) return "1";
        if (value.contains("인천")) return "2";
        if (value.contains("대전")) return "3";
        if (value.contains("대구")) return "4";
        if (value.contains("광주")) return "5";
        if (value.contains("부산")) return "6";
        if (value.contains("울산")) return "7";
        if (value.contains("세종")) return "8";
        if (value.contains("경기")) return "31";
        if (value.contains("강원")) return "32";
        if (value.contains("충북") || value.contains("충청북")) return "33";
        if (value.contains("충남") || value.contains("충청남")) return "34";
        if (value.contains("경북") || value.contains("경상북")) return "35";
        if (value.contains("경남") || value.contains("경상남")) return "36";
        if (value.contains("전북") || value.contains("전라북")) return "37";
        if (value.contains("전남") || value.contains("전라남")) return "38";
        if (value.contains("제주")) return "39";

        return null;
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
        int hash = Math.abs(title.hashCode());
        return BigDecimal.valueOf(90000 + (hash % 9) * 20000L)
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(1000))
                .intValue();
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
}
