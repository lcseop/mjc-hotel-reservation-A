package com.mjc.hotel.hotels;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.service.HotelService;
import com.mjc.hotel.hotels.service.HotelTypeService;
import com.mjc.hotel.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest
public class HotelServiceTest {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private HotelTypeService hotelTypeService;
    @Autowired
    private HotelMapper hotelMapper;

    private static HotelRequestDto TEST_HOTEL;
    private static HotelTypeDto TEST_HOTEL_TYPE;

    @Test
    @Order(1)
    public void testInsert() {
        // GIVEN
        String name = CommonUtils.getRandomString(50);
        LocalDateTime now = LocalDateTime.now();
        // GIVEN MASTER TABLE
        Slice<HotelTypeDto> findSlice = this.hotelTypeService.findByTitleContain("", Pageable.ofSize(10));
        if (findSlice.hasContent()) {
            HotelTypeDto find = findSlice.getContent().getFirst();
            TEST_HOTEL_TYPE = HotelTypeDto.builder()
                    .sid(find.getSid())
                    .title(find.getTitle())
                    .build();
        } else {
            HotelTypeDto insertDto = HotelTypeDto.builder().title(CommonUtils.getRandomString(10)).build();
            TEST_HOTEL_TYPE = HotelTypeDto.builder()
                    .sid(insertDto.getSid())
                    .title(insertDto.getTitle())
                    .build();
        }


        // WHEN ERROR
        HotelRequestDto hotelRequestDtoFail = HotelRequestDto.builder().hotelName(name).hotelPrice(200000).location("지구").build();
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            HotelResponseDto insertedFail = this.hotelService.insert(hotelRequestDtoFail);
        });
        hotelRequestDtoFail.setTypeId(TEST_HOTEL_TYPE.getSid());
        hotelRequestDtoFail.setHotelName(hotelRequestDtoFail.getHotelName() + "A");
        assertThrows(DataIntegrityViolationException.class, () -> {
            HotelResponseDto insertedFail = this.hotelService.insert(hotelRequestDtoFail);
        });

        // WHEN
        TEST_HOTEL = HotelRequestDto.builder().typeId(TEST_HOTEL_TYPE.getSid()).hotelName(name).hotelPrice(200000).location("지구").build();
        HotelResponseDto inserted = this.hotelService.insert(TEST_HOTEL);

        // THEN
        assertThat(inserted).isNotNull();
        assertThat(inserted.getSid()).isNotNull();
        assertThat(inserted.getTypeTitle()).isNotNull();
        assertThat(inserted.getHotelName()).isNotNull();
        assertThat(inserted.getHotelPrice()).isNotNull();
        assertThat(inserted.getLocation()).isNotNull();
        assertThat(inserted.getSid()).isGreaterThanOrEqualTo(1L);
        assertThat(inserted.getTypeTitle()).isEqualTo(TEST_HOTEL_TYPE.getTitle());
        log.info("inserted object = {}", HotelResponseDto.builder()
                .sid(inserted.getSid())
                .typeTitle(inserted.getTypeTitle())
                .hotelName(inserted.getHotelName())
                .hotelPrice(inserted.getHotelPrice())
                .location(inserted.getLocation())
                .build());
    }

    @Test
    @Order(2)
    public void testUpdate() {
        // GIVEN
        HotelRequestDto updateDto = HotelRequestDto
                .builder()
                .sid(TEST_HOTEL.getSid())
                .typeId(TEST_HOTEL.getTypeId())
                .hotelName(TEST_HOTEL.getHotelName())
                .hotelPrice(TEST_HOTEL.getHotelPrice())
                .location(TEST_HOTEL.getLocation())
                .build();

        // WHEN ERROR
        assertThrows(NoSuchElementException.class, () -> {
            HotelRequestDto updateDtoFail = HotelRequestDto
                    .builder()
                    .sid(updateDto.getSid())
                    .typeId(updateDto.getTypeId())
                    .hotelName(updateDto.getHotelName())
                    .hotelPrice(updateDto.getHotelPrice())
                    .location(updateDto.getLocation())
                    .build();
            updateDtoFail.setSid(0L);
            HotelResponseDto updatedDtoFail = this.hotelService.update(updateDtoFail);
        });

        // WHEN
        updateDto.setHotelName(CommonUtils.getRandomString(50));
        HotelResponseDto updatedDto = this.hotelService.update(updateDto);

        // THEN
    }

    @Test
    @Order(3)
    public void testDelete() {
        // GIVEN
        HotelRequestDto deleteDto = HotelRequestDto
                .builder()
                .sid(TEST_HOTEL.getSid())
                .typeId(TEST_HOTEL.getTypeId())
                .hotelName(TEST_HOTEL.getHotelName())
                .hotelPrice(TEST_HOTEL.getHotelPrice())
                .location(TEST_HOTEL.getLocation())
                .build();

        // WHEN ERROR
        assertThrows(NoSuchElementException.class, () -> {
            HotelRequestDto deleteFail = HotelRequestDto
                    .builder()
                    .sid(deleteDto.getSid())
                    .typeId(deleteDto.getTypeId())
                    .hotelName(deleteDto.getHotelName())
                    .hotelPrice(deleteDto.getHotelPrice())
                    .location(deleteDto.getLocation())
                    .build();
            deleteFail.setSid(0L);
            HotelResponseDto updatedDtoFail = this.hotelService.update(deleteFail);
        });

        // WHEN
        HotelResponseDto deletedDto = this.hotelService.delete(deleteDto.getSid());

        // THEN
    }
}
