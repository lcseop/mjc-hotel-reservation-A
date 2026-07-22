package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.*;
import com.mjc.hotel.hotels.service.HotelService;
import com.mjc.hotel.hotels.service.TourApiHotelImportService;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.dto.RoomResponseNoHotelDto;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
@Tag(name = "호텔", description = "호텔 데이터 전반을 관리합니다.")
public class HotelRestController {

    @Autowired
    private final HotelService hotelService;
    private final TourApiHotelImportService tourApiHotelImportService;

    @Operation(
            summary = "호텔 데이터 생성",
            description = "호텔 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelResponseDto>> insert(@RequestBody HotelRequestDto dto) {
        HotelResponseDto insert = hotelService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 데이터 수정",
            description = "호텔 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelResponseDto>> update(@RequestBody HotelRequestDto dto) {
        HotelResponseDto update = hotelService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel update success", update)
        );
    }

    @Operation(
            summary = "호텔 데이터 삭제",
            description = "호텔 데이터를 삭제하고, HotelInAmenities의 매핑 정보도 같이 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponseDto>> delete(@PathVariable Long id) {
        HotelResponseDto delete = hotelService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel delete success", delete)
        );
    }

    @Operation(
            summary = "호텔 단건 조회",
            description = "호텔 ID로 호텔 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponseDto>> findById(@PathVariable Long id) {
        HotelResponseDto search = hotelService.findById(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel search success", search)
        );
    }

    @Operation(
            summary = "호텔 데이터 검색",
            description = "호텔 데이터를 필터에 맞추어 검색합니다."
    )
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<HotelResponseDto>>> search(@RequestBody HotelSearchRequestDto dto,
                                                                      @PageableDefault(size = 5) Pageable pageable) {
        Page<HotelResponseDto> search = hotelService.search(dto, pageable);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel search success", search)
        );
    }

    @Operation(
            summary = "관리자 호텔 전체 목록",
            description = "객실 등록 여부와 상관없이 삭제되지 않은 호텔 전체를 가져옵니다."
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HotelResponseDto>>> findAllForAdmin() {
        List<HotelResponseDto> hotels = hotelService.findAllForAdmin();
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel all search success", hotels)
        );
    }

    @Operation(
            summary = "호텔의 편의 시설들 검색",
            description = "호텔 내에 있는 편의 시설들을 가져옵니다."
    )
    @GetMapping("/iname/{id}")
    public ResponseEntity<ApiResponse<List<HotelAmenitiesDto>>> findHotelInAmenities(@PathVariable Long id) {
        List<HotelAmenitiesDto> search = hotelService.findHotelInAmenities(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities search success", search)
        );
    }

    @Operation(
            summary = "호텔의 객실들 검색",
            description = "호텔 내에 있는 객실들을 가져옵니다."
    )
    @GetMapping("/inroom/{id}")
    public ResponseEntity<ApiResponse<List<RoomResponseNoHotelDto>>> findHotelInRooms(@PathVariable Long id) {
        List<RoomResponseNoHotelDto> search = hotelService.findHotelInRooms(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in rooms search success", search)
        );
    }

    @Operation(
            summary = "호텔 리뷰들 검색",
            description = "호텔에 대한 리뷰들을 가져옵니다."
    )
    @GetMapping("/inreview/{id}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> findHotelInReviews(@PathVariable Long id,
                                                                                @PageableDefault(size = 5) Pageable pageable) {
        Page<ReviewResponse> search = hotelService.findHotelInReviews(id, pageable);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in reviews search success", search)
        );
    }

    @Operation(
            summary = "호텔 사진 검색",
            description = "호텔에 대한 사진 주소들을 가져옵니다."
    )
    @GetMapping("/inimage/{id}")
    public ResponseEntity<ApiResponse<List<HotelPhotoDto>>> findHotelInReviews(@PathVariable Long id) {
        List<HotelPhotoDto> search = hotelService.findHotelInPhotos(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel photos search success", search)
        );
    }

    @Operation(
            summary = "인기 호텔 4개 검색",
            description = "가장 인기있는 호텔 4개를 검색합니다."
    )
    @GetMapping("/pop4")
    public ResponseEntity<ApiResponse<List<HotelPopularResponseDto>>> findHotelInReviews() {
        List<HotelPopularResponseDto> search = hotelService.findPopularHotels();
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotels search success", search)
        );
    }

    @Operation(
            summary = "공공 데이터 활용 호텔 데이터 가져오기",
            description = "한국관광공사 TourAPI를 이용해 호텔 데이터를 가져와 호텔/사진/기본 객실로 저장합니다."
    )
    @PostMapping("/import/tourapi")
    public ResponseEntity<ApiResponse<TourApiImportResponseDto>> importTourApiHotels(
            @RequestParam(defaultValue = "서울 호텔") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        TourApiImportResponseDto result = tourApiHotelImportService.importHotels(keyword, page, size);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "tour api hotels import success", result)
        );
    }

    @Operation(
            summary = "공공 데이터 호텔 가져오기 미리보기",
            description = "한국관광공사 TourAPI에서 호텔 후보 목록만 조회합니다. DB에는 저장하지 않습니다."
    )
    @GetMapping("/import/tourapi/preview")
    public ResponseEntity<ApiResponse<List<TourApiHotelPreviewDto>>> previewTourApiHotels(
            @RequestParam(defaultValue = "서울") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<TourApiHotelPreviewDto> result = tourApiHotelImportService.previewHotels(keyword, page, size);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "tour api hotels preview success", result)
        );
    }

    @Operation(
            summary = "선택한 공공 데이터 호텔 가져오기",
            description = "미리보기 목록에서 선택한 TourAPI 호텔만 저장하고 기본 객실까지 생성합니다."
    )
    @PostMapping("/import/tourapi/selected")
    public ResponseEntity<ApiResponse<TourApiImportResponseDto>> importSelectedTourApiHotels(
            @RequestBody TourApiImportRequestDto dto
    ) {
        TourApiImportResponseDto result = tourApiHotelImportService.importSelectedHotels(
                dto.getKeyword(),
                dto.getPage() == null ? 1 : dto.getPage(),
                dto.getSize() == null ? 10 : dto.getSize(),
                dto.getContentIds()
        );
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "selected tour api hotels import success", result)
        );
    }
}
