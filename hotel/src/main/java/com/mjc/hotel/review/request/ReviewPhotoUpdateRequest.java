package com.mjc.hotel.review.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewPhotoUpdateRequest {
    Long sid;
    Long reviewId;
    MultipartFile photo;
}
