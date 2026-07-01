package com.mjc.hotel.room.service;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.dto.RoomPhotoDto;
import com.mjc.hotel.room.dto.RoomRequestDto;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.mapper.RoomMapper;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
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

    @Transactional
    public RoomResponseDto insert(RoomRequestDto room) {
        Hotel hotel = hotelRepository.findById(room.getHotelId()).orElseThrow();
        RoomTag tag = roomTagRepository.findById(room.getRoomTagId()).orElseThrow();
        RoomPhoto photo = roomPhotoRepository.findById(room.getRoomTagId()).orElseThrow();
        RoomType type = roomTypeRepository.findById(room.getRoomTagId()).orElseThrow();

        Room insert = RoomMapper.clone(null, room, false, hotel, tag, type, photo);

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
        RoomTag tag = roomTagRepository.findById(room.getRoomTagId()).orElseThrow();
        RoomPhoto photo = roomPhotoRepository.findById(room.getRoomPhotoId()).orElseThrow();
        RoomType type = roomTypeRepository.findById(room.getRoomTypeId()).orElseThrow();

        Room update =  RoomMapper.clone(origin, room, true, hotel, tag, type, photo);

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
        Hotel hotel = hotelRepository.findById(target.getHotelId().getSid()).orElseThrow();
        RoomTag tag = roomTagRepository.findById(target.getRoomTagId().getSid()).orElseThrow();
        RoomPhoto photo = roomPhotoRepository.findById(target.getRoomPhotoId().getSid()).orElseThrow();
        RoomType type = roomTypeRepository.findById(target.getRoomTypeId().getSid()).orElseThrow();

        target.setDeleted(true);
        target.setDeletedAt(LocalDateTime.now());

        Room saved = roomRepository.save(target);

        RoomResponseDto dto = response(saved);

        return dto;
    }

    public List<RoomResponseDto> findByHotelId(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        if (hotel.getDeleted() != null && hotel.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, hotel.getHotelName() + " is not found");
        }
        List<Room> rooms = roomRepository.findActiveRooms(hotel);

        return rooms.stream()
                .map(RoomMapper::response)
                .toList();
    }

    public RoomResponseDto findById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow();
        if (room.getDeleted() != null && room.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return response(room);
    }

    private RoomResponseDto response(Room saved) {
        RoomResponseDto dto = RoomResponseDto
                .builder()
                .sid(saved.getSid())
                .hotel(HotelMapper.response(saved.getHotelId()))
                .roomTagTitle(saved.getRoomTagId().getTitle())
                .roomPhotoPath(saved.getRoomPhotoId().getImagePath())
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
