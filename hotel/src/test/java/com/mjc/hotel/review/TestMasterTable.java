package com.mjc.hotel.review;

import com.mjc.hotel.review.entity.ReviewCategoryMaster;
import com.mjc.hotel.review.entity.ReviewTagMaster;
import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import com.mjc.hotel.review.repository.ReviewCategoryMasterRepository;
import com.mjc.hotel.review.repository.ReviewTagMasterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMasterTable {
    @Autowired
    private ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    @Autowired
    private ReviewTagMasterRepository reviewTagMasterRepository;

    @DisplayName("마스터 테이블 저장 테스트")
    @Test
    @Commit
    public void insertReviewCategoryMaster(){
        ReviewCategoryMaster reviewCategoryMaster1 = ReviewCategoryMaster.builder()
                .reviewCategoryName("청결도")
                .build();
        reviewCategoryMasterRepository.save(reviewCategoryMaster1);

        ReviewCategoryMaster reviewCategoryMaster2 = ReviewCategoryMaster.builder()
                .reviewCategoryName("서비스")
                .build();
        reviewCategoryMasterRepository.save(reviewCategoryMaster2);

        ReviewTagMaster reviewTagMaster1 = ReviewTagMaster.builder()
                .reviewTagName("청결함")
                .reviewTagCategory(ReviewTagCategory.PROS)
                .build();
        reviewTagMasterRepository.save(reviewTagMaster1);

        ReviewTagMaster reviewTagMaster2 = ReviewTagMaster.builder()
                .reviewTagName("주차 불편")
                .reviewTagCategory(ReviewTagCategory.CONS)
                .build();
        reviewTagMasterRepository.save(reviewTagMaster2);
    }
}
