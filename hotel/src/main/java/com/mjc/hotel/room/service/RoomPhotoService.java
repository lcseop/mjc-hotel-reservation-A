package com.mjc.hotel.room.service;

import com.mjc.hotel.room.dto.RoomPhotoDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomPhotoService {
    private final RoomPhotoRepository roomPhotoRepository;
    private final RoomRepository roomRepository;
    @Value("${room.images}")
    private String uploadDir;

    @Transactional
    public RoomPhotoDto insert(RoomPhotoDto roomPhotoDto) {
        if (roomPhotoDto.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        Room room = roomRepository.findById(roomPhotoDto.getRoomId()).orElseThrow();
        RoomPhoto clone = RoomPhoto
                .builder()
                .room(room)
                .imagePath(roomPhotoDto.getImagePath())
                .build();
        return toDto(roomPhotoRepository.save(clone), true);
    }

    @Transactional
    public List<RoomPhotoDto> upload(Long roomId, List<MultipartFile> photos) {
        if (roomId == null) throw new IllegalArgumentException("roomId가 필요합니다.");
        if (photos == null || photos.isEmpty()) throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        Room room = roomRepository.findById(roomId).orElseThrow();
        List<RoomPhotoDto> result = new ArrayList<>();
        for (MultipartFile photo : photos) {
            if (photo == null || photo.isEmpty() || falseValidatePhotoFile(photo)) {
                continue;
            }
            RoomPhoto savedPhoto = roomPhotoRepository.save(saveImageFile(room, photo));
            result.add(toDto(savedPhoto, true));
        }
        if (result.isEmpty()) throw new IllegalArgumentException("저장 가능한 이미지가 없습니다.");
        return result;
    }

    @Transactional
    public RoomPhotoDto update(RoomPhotoDto roomPhotoDto) {
        if (roomPhotoDto.getSid() == null || roomPhotoDto.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomPhoto origin = roomPhotoRepository.findById(roomPhotoDto.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        Room room = roomRepository.findById(roomPhotoDto.getRoomId()).orElseThrow();
        RoomPhoto clone = RoomPhoto
                .builder()
                .sid(roomPhotoDto.getSid())
                .room(room)
                .imagePath(roomPhotoDto.getImagePath())
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(roomPhotoRepository.save(clone), true);
    }

    @Transactional
    public RoomPhotoDto delete(Long id) {
        RoomPhoto photo = roomPhotoRepository.findById(id).orElseThrow();
        roomPhotoRepository.delete(photo);
        return toDto(photo, true);
    }

    @Transactional
    public RoomPhotoDto findById(Long id) {
        RoomPhoto photo = roomPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(photo, true);
    }

    private RoomPhoto saveImageFile(Room room, MultipartFile photo) {
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
            return RoomPhoto.builder()
                    .room(room)
                    .imagePath("/images/rooms/" + storedFileName)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("객실 이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private Boolean falseValidatePhotoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return true;
        }
        return file.getSize() > 5 * 1024 * 1024;
    }

    private RoomPhotoDto toDto(RoomPhoto photo, boolean sid) {
        if (sid) {
            return RoomPhotoDto
                    .builder()
                    .sid(photo.getSid())
                    .roomId(photo.getRoom().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        } else {
            return RoomPhotoDto
                    .builder()
                    .roomId(photo.getRoom().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
