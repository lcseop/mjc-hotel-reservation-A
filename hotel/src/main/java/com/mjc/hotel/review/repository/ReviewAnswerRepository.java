package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long> {
}
