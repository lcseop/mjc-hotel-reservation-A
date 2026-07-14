package com.mjc.hotel.promotion;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.service.PromotionService;
import com.mjc.hotel.room.dto.RoomTypeDto;
import com.mjc.hotel.room.service.RoomTypeService;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest
@Transactional
public class PromotionServiceTest {

    @Autowired
    private PromotionService promotionService;
    @Autowired
    private RoomTypeService roomTypeService;

    private static Long TEST_ROOM_TYPE_ID;
    private static PromotionDto TEST_PROMOTION;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터가 없으면 강제 생성
        if (TEST_ROOM_TYPE_ID == null) {
            // RoomType이 하나도 없으면 테스트 자체가 불가능하므로 하나 생성
            try {
                TEST_ROOM_TYPE_ID = roomTypeService.findById(1L).getSid();
            } catch (Exception e) {
                RoomTypeDto dto = RoomTypeDto.builder().title("테스트 타입").build();
                TEST_ROOM_TYPE_ID = roomTypeService.insert(dto).getSid();
            }
        }
    }

    private static Long CREATED_PROMOTION_ID;

    @Test
    @Order(1)
    public void testInsert() {
        // GIVEN
        TEST_PROMOTION = PromotionDto.builder()
                .roomTypeId(TEST_ROOM_TYPE_ID)
                .promotionName("테스트 프로모션")
                .build();

        // WHEN
        PromotionDto inserted = promotionService.insert(TEST_PROMOTION);

        // THEN
        assertThat(inserted).isNotNull();
        assertThat(inserted.getSid()).isNotNull();

        // 생성된 ID를 클래스 변수에 저장 (이 ID를 삭제 테스트에서 사용)
        CREATED_PROMOTION_ID = inserted.getSid();
    }

    @Test
    @Order(2)
    public void testUpdate() {
        // 이미 생성된 ID를 사용하여 업데이트
        TEST_PROMOTION.setSid(CREATED_PROMOTION_ID);
        TEST_PROMOTION.setPromotionName("수정된 프로모션");

        PromotionDto updated = promotionService.update(TEST_PROMOTION);
        assertThat(updated.getPromotionName()).isEqualTo("수정된 프로모션");
    }

    @Test
    @Order(3)
    public void testDelete() {
        // 1. 삭제할 대상이 실제로 존재하는지 확인 (null 방어)
        assertThat(CREATED_PROMOTION_ID).isNotNull();

        // 2. 삭제 실행
        PromotionDto deleted = promotionService.delete(CREATED_PROMOTION_ID);

        // 3. 삭제 후 확인
        assertThat(deleted).isNotNull();
        assertThat(deleted.getSid()).isEqualTo(CREATED_PROMOTION_ID);

        // 4. 다시 삭제 시도 시 에러가 발생하는지 확인 (선택 사항)
        assertThrows(DataNotFoundException.class, () -> {
            promotionService.delete(CREATED_PROMOTION_ID);
        });
    }
}