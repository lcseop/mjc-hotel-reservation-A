package com.mjc.hotel.promotion;

import com.mjc.hotel.promotion.entity.*;
import com.mjc.hotel.promotion.repository.*;
import com.mjc.hotel.room.entity.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

import static com.mjc.hotel.promotion.entity.ConditionType.ACTIVE;

@SpringBootTest
public class PromotionServiceTest {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private PromotionPackageRepository packageRepository;
    @Autowired
    private FlatRepository flatRepository;
    @Autowired
    private DiscountRateRepository discountRateRepository;
    @Autowired
    private ConditionRepository conditionRepository;
    @Autowired
    private PromotionPackageRepository promotionPackageRepository;

    @Test
    @Commit
    public void promotionTest() {
        Promotion promotion = Promotion
                .builder()
                .promotionName("promotionName")
                .starRating(0)
                .howLong(LocalDateTime.now())
                .totalAmount(3)
                .build();
        promotionRepository.save(promotion);

        RoomType roomType = RoomType
                .builder()
                .title("스탠다드")
                .build();

        PromotionPackage promotionPackage = PromotionPackage
                .builder()
                .promotion(promotion)
                .type("패키지")
                .sale(20)
                .build();
        promotionPackageRepository.save(promotionPackage);

        Flat flat = Flat
                .builder()
                .promotion(promotion)
                .type("정액할인")
                .sale(30000)
                .build();
        flatRepository.save(flat);

        DiscountRate discountRate = DiscountRate
                .builder()
                .promotion(promotion)
                .type("할인율")
                .sale(30)
                .build();
        discountRateRepository.save(discountRate);


        Condition condition = Condition
                .builder()
                .promotion(promotion)
                .conditiontype(ConditionType.ACTIVE)
                .build();
        conditionRepository.save(condition);
    }
}
