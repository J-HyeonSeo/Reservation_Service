package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Review;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.ReservationException;
import com.jhsfully.reservation.exception.ReviewException;
import com.jhsfully.reservation.exception.ShopException;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import com.jhsfully.reservation.repository.ReviewRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.type.ReservationState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_NOT_MATCH_USER;
import static com.jhsfully.reservation.type.ReviewErrorType.*;
import static com.jhsfully.reservation.type.ShopErrorType.SHOP_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ShopRepository shopRepository;

    //작성 가능한 리뷰 조회 서비스
    @Override
    public List<ReservationDto.ResponseForReview> getReservationsForReview(Long memberId, LocalDate dateNow, int pageIndex) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        //해당 회원의 예약을 가져오는데, VISITED로 처리된 항목을 가져옴.
        //아직 예약일보다 일주일 이전이면, 조회할 수 있도록 해줌.

        Page<Reservation> reservations = reservationRepository.findReservationForReview(member, dateNow.minusWeeks(1), PageRequest.of(pageIndex, 10));

        return reservations
                .getContent()
                .stream()
                .map(x -> ReservationDto.ResponseForReview.builder()
                        .reservationCount(reservations.getTotalElements())
                        .reservationId(x.getId())
                        .shopName(x.getShop().getName())
                        .visitDay(x.getResDay())
                        .visitTime(x.getResTime())
                        .build()
                ).collect(Collectors.toList());

    }

    //리뷰 작성 서비스
    @Override
    public ReviewDto.WriteReviewResponse writeReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reservationId, LocalDate dateNow) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        //작성 가능 검증.
        validateWriteReview(member, reservation, dateNow);

        //리뷰 작성 가능
        Review review = Review.builder()
                .member(member)
                .shop(reservation.getShop())
                .reservation(reservation)
                .star(request.getStar())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        //리뷰 저장
        Review savedReview = reviewRepository.save(review);

        return ReviewDto.WriteReviewResponse.builder()
                .reviewId(savedReview.getId())
                .shopId(reservation.getShop().getId())
                .build();
    }

    @Override
    @Transactional
    public ReviewDto.UpdateReviewResponse updateReview(ReviewDto.WriteReviewRequest request, Long memberId, Long reviewId, LocalDate dateNow) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(REVIEW_NOT_FOUND));

        //리뷰 수정 가능 검증
        validateUpdateReview(member, review, dateNow);

        int originStar = review.getStar();

        //리뷰 수정
        review.setContent(request.getContent());
        review.setStar(request.getStar());
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        return ReviewDto.UpdateReviewResponse.builder()
                .originStar(originStar)
                .shopId(review.getShop().getId())
                .build();
    }

    @Override
    public ReviewDto.DeleteReviewResponse getDataForDeleteReview(Long memberId, Long reviewId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(REVIEW_NOT_FOUND));

        Reservation reservation = reservationRepository.findByReview(review)
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));

        //삭제 가능 검증.
        validateDeleteReview(review, member);

        return ReviewDto.DeleteReviewResponse.builder()
                .reservationId(reservation.getId())
                .shopId(review.getShop().getId())
                .star(review.getStar())
                .build();
    }

    @Override
    public void deleteReviewComplete(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewDto.ReviewResponse> getReviewsForUser(Long memberId, int pageIndex) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByMember(member, PageRequest.of(pageIndex, 10, Sort.by("id").descending()));
        return reviews.getContent()
                .stream()
                .map(x -> Review.toDto(x, reviews.getTotalElements()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto.ReviewResponse> getReviewsForShop(Long shopId, int pageIndex) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(SHOP_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByShop(shop, PageRequest.of(pageIndex, 10, Sort.by("id").descending()));
        return reviews.getContent()
                .stream()
                .map(x -> Review.toDto(x, reviews.getTotalElements()))
                .collect(Collectors.toList());
    }


    //======================= 검증 로직 ================================

    //리뷰 작성 검증.
    private void validateWriteReview(Member member, Reservation reservation, LocalDate dateNow) {

        //해당 유저와 매칭되는 예약이 아닌가?
        if(!Objects.equals(reservation.getMember().getId(), member.getId())){
            throw new ReservationException(RESERVATION_NOT_MATCH_USER);
        }

        //리뷰가 방문된 상태가 아닌가?
        if(reservation.getReservationState() != ReservationState.VISITED){
            throw new ReviewException(REVIEW_STATE_NOT_VISITED);
        }

        //리뷰가 이미 존재하지 않은가?
        if(reservation.getReview() != null){
            throw new ReviewException(REVIEW_ALREADY_WRITTEN);
        }

        //현재 날짜 - 1주 > 방문일 인가? (리뷰 작성 기간 초과)
        if(dateNow.minusWeeks(1).isAfter(reservation.getResDay())){
            throw new ReviewException(REVIEW_TIME_OVER);
        }

    }

    private void validateUpdateReview(Member member, Review review, LocalDate dateNow) {

        //해당 유저와 매칭되는 리뷰가 아닌가?
        if(!Objects.equals(review.getMember().getId(), member.getId())){
            throw new ReviewException(REVIEW_NOT_MATCH_USER);
        }

        if(dateNow.minusWeeks(1).isAfter(review.getReservation().getResDay())){
            throw new ReviewException(REVIEW_TIME_OVER);
        }

    }

    private void validateDeleteReview(Review review, Member member){
        if(!Objects.equals(review.getMember().getId(), member.getId())){
            throw new ReviewException(REVIEW_NOT_MATCH_USER);
        }
    }
}
