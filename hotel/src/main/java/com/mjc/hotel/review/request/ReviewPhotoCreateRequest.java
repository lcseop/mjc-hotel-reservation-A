package com.mjc.hotel.review.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewPhotoCreateRequest {
    Long reviewId;
    List<MultipartFile> photos;
}
