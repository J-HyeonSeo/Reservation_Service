package com.jhsfully.reservation.facade;

import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.service.ShopService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
    분산환경을 고려하여, 격리 수준을 커밋된 데이터를 가져오는 것으로 설정함.
    이는 트랜잭션을 수행 중간에, 타 서버에서 DB에 커밋을 수행하였고, 해당 값을 가져올 수 있게함.

    REVIEW - RESERVATION - SHOP 데이터가 일관성을 유지해야함.
 */

@Component
@Transactional
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

    /*
        reviewService에서 리뷰 수정 여부를 검증.
        작성 가능하면, 필요한 데이터 반환
        이후에, 별점을 수정함.
     */
    public void updateReviewAndUpdateShopStar(ReviewDto.WriteReviewRequest request,
                                              Long memberId, Long reviewId,
                                              LocalDate dateNow){
        ReviewDto.UpdateReviewResponse reviewResponse = reviewService.updateReview(
                request, memberId, reviewId, dateNow
        );

        shopService.updateShopStar(reviewResponse.getShopId(),
                reviewResponse.getOriginStar(), request.getStar());
    }

    /*

     */
    public void deleteReviewAndSubShopStar(Long memberId, Long reviewId){
        ReviewDto.DeleteReviewResponse reviewResponse = reviewService.getDataForDeleteReview(
                memberId, reviewId
        );

        reservationService.releaseReview(reviewResponse.getReservationId());
        reviewService.deleteReviewComplete(reviewId);
        shopService.subShopStar(reviewResponse.getShopId(), reviewResponse.getStar());
    }

}
