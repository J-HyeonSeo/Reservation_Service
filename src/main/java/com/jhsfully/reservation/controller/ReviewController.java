package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.facade.ReviewFacade;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService; //get은 서비스에서 다이렉트로 호출함.
    private final ReviewFacade reviewFacade;


    //리뷰 작성 가능 예약 조회
    //예약을 가져오는 것이라 ReservationController에 있어야 하지만, 유지보수를 위해
    //Review를 위한 Reservation을 Get하는 것은 여기서 작성하도록 함.
    @GetMapping("/reviewable/{pageIndex}")
    ResponseEntity<List<ReservationDto.ResponseForReview>> getReservationsForReview(@PathVariable int pageIndex){
        Long memberId = MemberUtil.getMemberId();
        List<ReservationDto.ResponseForReview> forReviews = reviewService.getReservationsForReview(memberId, LocalDate.now(), pageIndex);
        return ResponseEntity.ok(forReviews);
    }

    //리뷰 작성
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{reservationId}")
    ResponseEntity<?> writeReview(@RequestBody @Valid ReviewDto.WriteReviewRequest request,
                                  @PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reviewFacade.writeReviewAndAddShopStar(request, memberId, reservationId, LocalDate.now());
        return ResponseEntity.ok().build();
    }

    //리뷰 수정
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{reviewId}")
    ResponseEntity<?> updateReview(@RequestBody ReviewDto.WriteReviewRequest request,
                                   @PathVariable Long reviewId){
        Long memberId = MemberUtil.getMemberId();
        reviewFacade.updateReviewAndUpdateShopStar(request, memberId, reviewId, LocalDate.now());
        return ResponseEntity.ok().build();
    }

    //리뷰 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{reviewId}")
    ResponseEntity<?> deleteReview(@PathVariable Long reviewId){
        Long memberId = MemberUtil.getMemberId();
        reviewFacade.deleteReviewAndSubShopStar(memberId, reviewId);
        return ResponseEntity.ok().build();
    }

    //회원별 리뷰 조회(페이징 처리가 필요함.)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{pageIndex}")
    ResponseEntity<List<ReviewDto.ReviewResponse>> getReviewsForUser(@PathVariable int pageIndex){
        Long memberId = MemberUtil.getMemberId();
        List<ReviewDto.ReviewResponse> responses = reviewService.getReviewsForUser(memberId, pageIndex);
        return ResponseEntity.ok(responses);
    }

    //매장별 리뷰 조회(페이징 처리가 필요함.)
    @GetMapping("/shop/{shopId}/{pageIndex}")
    ResponseEntity<List<ReviewDto.ReviewResponse>> getReviewsForShop(@PathVariable Long shopId,
                                                                     @PathVariable int pageIndex){
        List<ReviewDto.ReviewResponse> responses = reviewService.getReviewsForShop(shopId, pageIndex);
        return ResponseEntity.ok(responses);
    }


}
