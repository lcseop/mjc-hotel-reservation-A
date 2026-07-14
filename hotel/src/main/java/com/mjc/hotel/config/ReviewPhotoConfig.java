package com.mjc.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ReviewPhotoConfig implements WebMvcConfigurer {
    @Value("${review.images}")
    public String imagePath;
    @Value("${room.images}")
    public String roomImagePath;
    @Value("${hotel.images}")
    public String hotelImagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/reviews/**").addResourceLocations(imagePath)
                .addResourceLocations("file:" + imagePath);
        registry.addResourceHandler("/images/rooms/**").addResourceLocations(roomImagePath)
                .addResourceLocations("file:" + roomImagePath);
        registry.addResourceHandler("/images/hotels/**").addResourceLocations(hotelImagePath)
                .addResourceLocations("file:" + hotelImagePath);
    }
}
