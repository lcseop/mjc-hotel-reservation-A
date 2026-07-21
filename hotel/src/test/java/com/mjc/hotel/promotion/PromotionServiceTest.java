package com.mjc.hotel.promotion;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.service.PromotionService;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // 각 테스트 후 롤백되므로 데이터 오염 방지
public class PromotionServiceTest {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    private Long testRoomTypeId;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 독립적인 테스트용 RoomType 생성
        RoomType roomType = RoomType.builder().title("테스트 객실 타입").build();
        testRoomTypeId = roomTypeRepository.save(roomType).getSid();
    }

    @Test
    @DisplayName("프로모션 전체 생명주기 테스트(생성, 수정, 삭제)")
    void testPromotionLifecycle() {
        // [1] CREATE
        PromotionDto dto = PromotionDto.builder()
                .roomTypeId(testRoomTypeId)
                .promotionName("봄맞이 이벤트")
                .discountContent("10%")
                .status("진행중")
                .build();

        PromotionDto created = promotionService.insert(dto);
        assertThat(created.getSid()).isNotNull();

        // [2] UPDATE
        created.setPromotionName("여름맞이 이벤트");
        PromotionDto updated = promotionService.update(created);
        assertThat(updated.getPromotionName()).isEqualTo("여름맞이 이벤트");

        // [3] DELETE (Soft Delete 검증)
        PromotionDto deleted = promotionService.delete(updated.getSid());
        assertThat(deleted).isNotNull();

        // [4] EXCEPTION (이미 삭제된 프로모션 재삭제 시 예외 발생 검증)
        assertThrows(DataNotFoundException.class, () -> {
            promotionService.delete(updated.getSid());
        });
    }
}