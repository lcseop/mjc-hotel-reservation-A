package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.dto.PromotionSearchRequestDto;
import com.mjc.hotel.promotion.dto.PromotionStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromotionRepositoryCustom {
    Page<PromotionDto> search(PromotionSearchRequestDto dto, Pageable pageable);
    List<PromotionStatsDto> getPromotionStatistics();
}
