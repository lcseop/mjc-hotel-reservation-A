package com.mjc.hotel.hotels.config;

import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HotelTypeDataInitializer implements ApplicationRunner {

    private static final List<String> REQUIRED_HOTEL_TYPES = List.of("호텔", "펜션/풀빌라", "리조트");

    private final HotelTypeRepository hotelTypeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        REQUIRED_HOTEL_TYPES.stream()
                .filter(type -> !hotelTypeRepository.existsByTitleAndDeletedFalse(type))
                .map(type -> HotelType.builder().title(type).build())
                .forEach(hotelTypeRepository::save);
    }
}
