package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ReservationDto;

import java.util.List;

public interface ReservationService {
    void addReservation(Long memberId, ReservationDto.AddReservationRequest request);

    List<ReservationDto.ReservationResponse> getReservationForUser(Long memberId);

    List<ReservationDto.ReservationResponse> getReservationByShop(Long memberId, Long shopId);
}
