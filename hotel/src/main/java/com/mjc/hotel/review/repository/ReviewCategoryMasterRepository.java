package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCategoryMasterRepository extends JpaRepository<ReviewCategoryMaster, Long> {
}
