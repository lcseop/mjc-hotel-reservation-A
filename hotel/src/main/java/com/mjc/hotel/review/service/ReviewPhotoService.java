package com.mjc.hotel.review.service;

import com.mjc.hotel.review.repository.ReviewPhotoRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewPhotoService {

    private final ReviewRepository reviewRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
}
