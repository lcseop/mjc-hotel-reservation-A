package com.mjc.hotel.review.entity.composite_key;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode //복합키 id 클래스
public class ReviewTagId implements Serializable {
    private Long review;
    private Long reviewTagMaster;
}
