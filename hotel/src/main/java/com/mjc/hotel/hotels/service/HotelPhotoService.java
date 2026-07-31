package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelPhotoDto;
import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelPhotoService {
    private final HotelPhotoRepository hotelPhotoRepository;
    private final HotelRepository hotelRepository;
    @Value("${hotel.images}")
    private String uploadDir;

    @Transactional
    public HotelPhotoDto insert(HotelPhotoDto photo) {
        if (photo.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        Hotel hotel = hotelRepository.findById(photo.getHotelId()).orElseThrow();
        HotelPhoto clone = HotelPhoto
                .builder()
                .hotel(hotel)
                .imagePath(photo.getImagePath())
                .build();

        return toDto(hotelPhotoRepository.save(clone), true);
    }

    @Transactional
    public HotelPhotoDto update(HotelPhotoDto photo) {
        if (photo.getSid() == null || photo.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelPhoto origin =  hotelPhotoRepository.findById(photo.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        Hotel hotel = hotelRepository.findById(photo.getHotelId()).orElseThrow();
        HotelPhoto clone = HotelPhoto
                .builder()
                .sid(photo.getSid())
                .hotel(hotel)
                .imagePath(photo.getImagePath())
                .build();
        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(hotelPhotoRepository.save(clone), true);
    }

    @Transactional
    public HotelPhotoDto delete(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        hotelPhotoRepository.delete(photo);
        return toDto(photo, true);
    }

    public HotelPhotoDto findById(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(photo, true);
    }

    @Transactional
    public List<HotelPhotoDto> upload(Long hotelId, List<MultipartFile> photos) {
        if (hotelId == null || photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        }

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        List<HotelPhotoDto> result = new ArrayList<>();
        for (MultipartFile photo : photos) {
            if (photo == null || photo.isEmpty() || falseValidatePhotoFile(photo)) {
                continue;
            }
            result.add(toDto(hotelPhotoRepository.save(saveImageFile(hotel, photo)), true));
        }
        return result;
    }

    private HotelPhoto saveImageFile(Hotel hotel, MultipartFile photo) {
        try {
            String originalFileName = photo.getOriginalFilename();
            String extension = getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFileName);
            photo.transferTo(filePath.toFile());

            return HotelPhoto.builder()
                    .hotel(hotel)
                    .imagePath("/images/hotels/" + storedFileName)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private boolean falseValidatePhotoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || !contentType.startsWith("image/") || file.getSize() > 5 * 1024 * 1024;
    }

    private HotelPhotoDto toDto(HotelPhoto photo, boolean sid) {
        if (sid) {
            return HotelPhotoDto
                    .builder()
                    .sid(photo.getSid())
                    .hotelId(photo.getHotel().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        } else {
            return HotelPhotoDto
                    .builder()
                    .hotelId(photo.getHotel().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
