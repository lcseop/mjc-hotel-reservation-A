package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.HotelType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelTypeRepository extends JpaRepository<HotelType, Long> {
    Slice<HotelType> findByTitleContains(String name, Pageable pageable);
}
