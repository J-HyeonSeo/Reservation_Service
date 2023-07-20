package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ReservationDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    void addReservation(Long memberId, ReservationDto.AddReservationRequest request);

    List<ReservationDto.ReservationResponse> getReservationForUser(Long memberId, LocalDate startDate);

    List<ReservationDto.ReservationResponse> getReservationByShop(Long memberId, Long shopId, LocalDate startDate);

    void deleteReservation(Long memberId, Long reservationId);
}
