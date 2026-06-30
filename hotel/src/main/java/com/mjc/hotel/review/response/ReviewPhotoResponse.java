package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewPhotoResponse {
    private Long sid;

    private String originalFileName;

    private String imagePath;
}
