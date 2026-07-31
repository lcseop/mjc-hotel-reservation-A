package com.mjc.hotel.auth.dto;

public record EmailAvailabilityResponse(
        String email,
        boolean available
) {
}
