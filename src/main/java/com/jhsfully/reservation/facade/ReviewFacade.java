package com.jhsfully.reservation.facade;

import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Transactional //중간에 하나라도 실패하면, 롤백되어야함.
@RequiredArgsConstructor
public class ReviewFacade {


    private final ReviewService reviewService;
    private final ShopService shopService;
    private final ReservationService reservationService;

    /*
        reviewService에서 리뷰 작성 여부를 검증.
        작성 가능하면, 객체 반환,
        이후에, reservationService에서 리뷰를 할당함.
        이후에, shopService에서 별점을 더함.
     */
    public void writeReviewAndAddShopStar(ReviewDto.WriteReviewRequest request,
                                          Long memberId, Long reservationId,
                                          LocalDate dateNow){

        ReviewDto.WriteReviewResponse reviewResponse = reviewService.writeReview(
                request, memberId, reservationId, dateNow
        );

        reservationService.setReview(reservationId, reviewResponse.getReviewId());
        shopService.addShopStar(reviewResponse.getShopId(), request.getStar());

    }


    public void updateReviewAndUpdateShopStar(ReviewDto.WriteReviewRequest request,
                                              Long memberId, Long reviewId,
                                              LocalDate dateNow){
        ReviewDto.UpdateReviewResponse reviewResponse = reviewService.updateReview(
                request, memberId, reviewId, dateNow
        );

        shopService.updateShopStar(reviewResponse.getShopId(),
                reviewResponse.getOriginStar(), request.getStar());
    }


    public void deleteReviewAndSubShopStar(Long memberId, Long reviewId){
        ReviewDto.DeleteReviewResponse reviewResponse = reviewService.getDataForDeleteReview(
                memberId, reviewId
        );

        shopService.subShopStar(reviewResponse.getShopId(), reviewResponse.getStar());
        reservationService.releaseReview(reviewResponse.getReservationId());
        reviewService.deleteReviewComplete(reviewId);
    }

}
