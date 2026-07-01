package com.mjc.hotel.review.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewPhotoUpdateRequest {
    Long sid;
    Long reviewId;
    MultipartFile photo;
}
