package com.mjc.hotel.room.service;

import com.mjc.hotel.hotels.dto.HotelInPhotoResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.dto.*;
import com.mjc.hotel.room.entity.*;
import com.mjc.hotel.room.mapper.RoomMapper;
import com.mjc.hotel.room.repository.*;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private RoomTagRepository roomTagRepository;
    @Autowired
    private RoomPhotoRepository roomPhotoRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private RoomInTagRepository roomInTagRepository;

    @Transactional
    public RoomResponseDto insert(RoomRequestDto room) {
        Hotel hotel = hotelRepository.findById(room.getHotelId()).orElseThrow();
        RoomType type = roomTypeRepository.findById(room.getRoomTypeId()).orElseThrow();

        Room insert = RoomMapper.clone(null, room, false, hotel, type);

        Room saved = roomRepository.save(insert);

        RoomResponseDto dto = response(saved);

        return dto;
    }

    @Transactional
    public RoomResponseDto update(RoomRequestDto room) {
        Room origin = roomRepository.findById(room.getSid()).orElseThrow();

        if (origin.getDeleted() != null && origin.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, origin.getRoomName() + " is not found");
        }

        Hotel hotel = hotelRepository.findById(room.getHotelId()).orElseThrow();
        RoomType type = roomTypeRepository.findById(room.getRoomTypeId()).orElseThrow();

        Room update =  RoomMapper.clone(origin, room, true, hotel, type);

        Room saved = roomRepository.save(update);

        RoomResponseDto dto = response(saved);

        return dto;
    }

    @Transactional
    public RoomResponseDto delete(Long id) {
        Room target = roomRepository.findById(id).orElseThrow();

        if (target.getDeleted() != null && target.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, target.getRoomName() + " is not found");
        }

        target.setDeleted(true);
        target.setDeletedAt(LocalDateTime.now());

        Room saved = roomRepository.save(target);

        RoomResponseDto dto = response(saved);

        return dto;
    }

    public RoomResponseDto findById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow();
        if (room.getDeleted() != null && room.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return response(room);
    }

    public List<RoomTagDto> findRoomInTags(Long id) {
        List<RoomInTag> roomInTags = roomInTagRepository.findByRoomSid(id);
        return roomInTags.stream()
                .map(r -> {
                    RoomTag roomTag = roomTagRepository.findById(r.getTag().getSid()).orElseThrow();
                    return RoomTagDto.builder()
                            .sid(roomTag.getSid())
                            .title(roomTag.getTitle())
                            .build();
                })
                .toList();
    }

    public List<RoomPhotoDto> findRoomInImages(Long id) {
        List<RoomPhoto> photos = roomPhotoRepository.findByRoomSid(id);
        return photos.stream()
                .filter(p -> !p.getDeleted())
                .map(p -> RoomPhotoDto
                        .builder()
                        .sid(p.getSid())
                        .roomId(p.getRoom().getSid())
                        .imagePath(p.getImagePath())
                        .build())
                .toList();
    }

    public List<RoomResponseDto> findByRoomType(Long typeId) {
        List<Room> rooms = roomRepository.findByRoomTypeIdSid(typeId);
        return rooms.stream()
                .filter(r -> !r.getDeleted())
                .map(r -> {
                    HotelInPhotoResponseDto hotel = HotelMapper.photoResponse(r.getHotelId());
                    return RoomResponseDto
                            .builder()
                            .sid(r.getSid())
                            .hotel(hotel)
                            .roomTypeTitle(r.getRoomTypeId().getTitle())
                            .roomName(r.getRoomName())
                            .roomPrice(r.getRoomPrice())
                            .roomNumber(r.getRoomNumber())
                            .floor(r.getFloor())
                            .area(r.getArea())
                            .maximumPeople(r.getMaximumPeople())
                            .checkInTime(r.getCheckInTime())
                            .checkOutTime(r.getCheckOutTime())
                            .parking(r.getParking())
                            .pet(r.getPet())
                            .smoke(r.getSmoke())
                            .idCard(r.getIdCard())
                            .build();
                })
                .toList();
    }

    private RoomResponseDto response(Room saved) {
        RoomResponseDto dto = RoomResponseDto
                .builder()
                .sid(saved.getSid())
                .hotel(HotelMapper.photoResponse(saved.getHotelId()))
                .roomTypeTitle(saved.getRoomTypeId().getTitle())
                .roomName(saved.getRoomName())
                .roomPrice(saved.getRoomPrice())
                .roomNumber(saved.getRoomNumber())
                .floor(saved.getFloor())
                .area(saved.getArea())
                .maximumPeople(saved.getMaximumPeople())
                .checkInTime(saved.getCheckInTime())
                .checkOutTime(saved.getCheckOutTime())
                .parking(saved.getParking())
                .pet(saved.getPet())
                .smoke(saved.getSmoke())
                .idCard(saved.getIdCard())
                .build();
        return dto;
    }
}
