package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewTagMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTagMasterRepository extends JpaRepository<ReviewTagMaster, Long> {
}
