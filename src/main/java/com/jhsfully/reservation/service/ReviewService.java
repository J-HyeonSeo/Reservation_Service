package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;

import java.time.LocalDate;
import java.util.List;

public interface ReviewService {
    List<ReservationDto.ResponseForReview> getReservationsForReview(Long memberId, LocalDate dateNow, int pageIndex);

    ReviewDto.WriteReviewResponse writeReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reservationId, LocalDate dateNow);

    ReviewDto.UpdateReviewResponse updateReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reviewId, LocalDate dateNow);

    ReviewDto.DeleteReviewResponse getDataForDeleteReview(Long memberId, Long reviewId);
    void deleteReviewComplete(Long reviewId);

    List<ReviewDto.ReviewResponse> getReviewsForUser(Long memberId, int pageIndex);

    List<ReviewDto.ReviewResponse> getReviewsForShop(Long shopId, int pageIndex);
}
