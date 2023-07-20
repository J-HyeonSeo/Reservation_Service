package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {
    void addReservation(Long memberId, ReservationDto.AddReservationRequest request, LocalDate dateNow);

    List<ReservationDto.ReservationResponse> getReservationForUser(Long memberId, LocalDate startDate);

    List<ReservationDto.ReservationResponse> getReservationByShop(Long memberId, Long shopId, LocalDate startDate);

    void deleteReservation(Long memberId, Long reservationId);

    void rejectReservation(Long memberId, Long reservationId, LocalDate dateNow);

    void assignReservation(Long memberId, Long reservationId, LocalDate dateNow);

    ReservationDto.ReservationResponse getReservationForVisit(Long memberId, Long shopId, LocalDate dateNow, LocalTime timeNow);

    void visitReservation(Long memberId, Long reservationId, LocalDate dateNow, LocalTime timeNow);
}
