package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;

import java.time.LocalDate;
import java.util.List;

public interface ReviewService {
    List<ReservationDto.ResponseForReview> getReservationsForReview(Long memberId, LocalDate dateNow);

    void writeReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reservationId, LocalDate dateNow);

    void updateReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reviewId, LocalDate dateNow);

    void deleteReview(Long memberId, Long reviewId);

    List<ReviewDto.ReviewResponse> getReviewsForUser(Long memberId, Long pageIndex);

    List<ReviewDto.ReviewResponse> getReviewsForShop(Long shopId, Long pageIndex);
}
