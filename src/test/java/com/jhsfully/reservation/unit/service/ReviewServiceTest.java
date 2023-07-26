package com.jhsfully.reservation.unit.service;

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
import com.jhsfully.reservation.service.impl.ReviewServiceImpl;
import com.jhsfully.reservation.type.ShopErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_NOT_MATCH_USER;
import static com.jhsfully.reservation.type.ReservationState.ASSIGN;
import static com.jhsfully.reservation.type.ReservationState.VISITED;
import static com.jhsfully.reservation.type.ReviewErrorType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*
    Mockito를 사용하여, 리뷰 서비스의 단위 테스트를 진행함.
 */
@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ShopRepository shopRepository;
    @InjectMocks
    private ReviewServiceImpl reviewService;

    /*
        #######################################################################
        ###########                                                 ###########
        ###########           성공 케이스(SUCCESS CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    @Test
    @DisplayName("[SERVICE]리뷰를 위한 예약 조회 - 성공")
    void getReservationForReviewSuccess(){
        //given
        Member member = Member.builder().build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        member
                ));
        given(reservationRepository.findReservationForReview(any(), any(), any()))
                .willReturn(new PageImpl<>(
                        new ArrayList<>(
                                Arrays.asList(
                                        Reservation.builder()
                                                .id(1L)
                                                .shop(Shop.builder().name("aaa").build())
                                                .count(3)
                                                .resDay(LocalDate.of(2023, 7, 15))
                                                .resTime(LocalTime.of(9, 0))
                                                .note("note")
                                                .build()
                                )
                        ),
                        PageRequest.of(0, 10),
                        1
                ));
        //when
        List<ReservationDto.ResponseForReview> response =
                reviewService.getReservationsForReview(1L,
                        LocalDate.of(2023, 7, 15), 0);
        //then
        assertAll(
                () -> assertEquals(1L, response.get(0).getReservationCount()),
                () -> assertEquals(1L, response.get(0).getReservationId()),
                () -> assertEquals("aaa", response.get(0).getShopName()),
                () -> assertEquals(LocalDate.of(2023, 7, 15), response.get(0).getVisitDay()),
                () -> assertEquals(LocalTime.of(9, 0), response.get(0).getVisitTime())
        );
    }

    @Test
    @DisplayName("[SERVICE]리뷰 작성 - 성공")
    void writeReviewSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder().id(1L).name("aaa").build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .member(user)
                                .shop(shop)
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(9, 0))
                                .count(1)
                                .reservationState(VISITED)
                                .build()
                ));
        given(reviewRepository.save(any()))
                .willReturn(Review.builder()
                                .id(1L)
                                .build());
        //when
        ReviewDto.WriteReviewResponse response = reviewService.writeReview(ReviewDto.WriteReviewRequest.builder()
                        .star(4)
                        .content("content!!!")
                        .build()
                , 1L, 1L, LocalDate.of(2023, 7, 15));
        //then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(1)).save(captor.capture());
        Review review = captor.getValue();
        assertAll(
                () -> assertEquals(1L, response.getReviewId()),
                () -> assertEquals(1L, response.getShopId()),
                () -> assertEquals(3L, review.getMember().getId()),
                () -> assertEquals(1L, review.getShop().getId()),
                () -> assertEquals(1L, review.getReservation().getId()),
                () -> assertEquals(4, review.getStar()),
                () -> assertEquals("content!!!", review.getContent())
        );
    }

    @Test
    @DisplayName("[SERVICE]리뷰 수정 - 성공")
    void updateReviewSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder()
                .id(1L)
                .name("aaa")
                .star(5)
                .starSum(5)
                .reviewCount(1)
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .shop(shop)
                .reservationState(VISITED)
                .resDay(LocalDate.of(2023, 7, 15))
                .resTime(LocalTime.of(9, 0))
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Review.builder()
                                .id(1L)
                                .reservation(reservation)
                                .shop(shop)
                                .member(user)
                                .star(5)
                                .content("content!!!")
                                .build()
                ));
        //when
        ReviewDto.UpdateReviewResponse response = reviewService.updateReview(
                ReviewDto.WriteReviewRequest.builder()
                        .star(3)
                        .content("updated!!")
                        .build(),
                1L,
                1L,
                LocalDate.of(2023, 7, 16));
        //then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(1)).save(captor.capture());
        Review review = captor.getValue();

        assertAll(
                () -> assertEquals(1L, response.getShopId()),
                () -> assertEquals(5, response.getOriginStar()),
                () -> assertEquals(3, review.getStar()),
                () -> assertEquals("updated!!", review.getContent())
        );
    }

    @Test
    @DisplayName("[SERVICE]리뷰 삭제에 관련된 데이터 가져오기 - 성공")
    void getDataForDeleteReviewSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Review review = Review.builder()
                .id(1L)
                .shop(Shop.builder().id(2L).build())
                .member(user)
                .star(5)
                .build();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .member(user)
                .build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        review
                ));
        given(reservationRepository.findByReview(any()))
                .willReturn(Optional.of(
                        reservation
                ));
        //when
        ReviewDto.DeleteReviewResponse response =
                reviewService.getDataForDeleteReview(1L, 1L);

        //then
        assertAll(
                () -> assertEquals(1L, response.getReservationId()),
                () -> assertEquals(2L, response.getShopId()),
                () -> assertEquals(5, response.getStar())
        );

    }

    @Test
    @DisplayName("[SERVICE]리뷰 삭제 완료 - 성공")
    void deleteReviewCompleteSuccess(){
        //given
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Review.builder().build()
                ));
        //when
        reviewService.deleteReviewComplete(1L);
        //then
        verify(reviewRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("[SERVICE]리뷰 목록 조회 for 유저 - 성공")
    void getReviewsForUserSuccess(){
        //given
        Member user = Member.builder().id(3L).name("aaa").build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        given(reviewRepository.findByMember(any(), any()))
                .willReturn(
                        new PageImpl<>(
                                new ArrayList<>(
                                        Arrays.asList(
                                                Review.builder()
                                                        .id(1L)
                                                        .member(user)
                                                        .star(5)
                                                        .content("content")
                                                        .createdAt(LocalDateTime.of(2023, 7, 15, 9, 0))
                                                        .updatedAt(LocalDateTime.of(2023, 7, 15, 9, 1))
                                                        .build()
                                        )
                                ),
                                PageRequest.of(0, 10),
                                1
                        )
                );
        //when
        List<ReviewDto.ReviewResponse> responses =
                reviewService.getReviewsForUser(1L, 0);

        //then
        assertAll(
                () -> assertEquals(1L, responses.get(0).getId()),
                () -> assertEquals(1, responses.get(0).getReviewCount()),
                () -> assertEquals("aaa", responses.get(0).getMemberName()),
                () -> assertEquals(5, responses.get(0).getStar()),
                () -> assertEquals("content", responses.get(0).getContent()),
                () -> assertEquals(LocalDateTime.of(2023, 7, 15, 9, 0), responses.get(0).getCreatedAt()),
                () -> assertEquals(LocalDateTime.of(2023, 7, 15, 9, 1), responses.get(0).getUpdatedAt())
        );
    }


    @Test
    @DisplayName("[SERVICE]리뷰 목록 조회 for 매장 - 성공")
    void getReviewsForShopSuccess(){
        //given
        Member user = Member.builder().id(3L).name("aaa").build();
        Shop shop = Shop.builder().build();
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        shop
                ));
        given(reviewRepository.findByShop(any(), any()))
                .willReturn(
                        new PageImpl<>(
                                new ArrayList<>(
                                        Arrays.asList(
                                                Review.builder()
                                                        .id(1L)
                                                        .member(user)
                                                        .star(5)
                                                        .content("content")
                                                        .createdAt(LocalDateTime.of(2023, 7, 15, 9, 0))
                                                        .updatedAt(LocalDateTime.of(2023, 7, 15, 9, 1))
                                                        .build()
                                        )
                                ),
                                PageRequest.of(0, 10),
                                1
                        )
                );
        //when
        List<ReviewDto.ReviewResponse> responses =
                reviewService.getReviewsForShop(1L, 0);

        //then
        assertAll(
                () -> assertEquals(1L, responses.get(0).getId()),
                () -> assertEquals(1, responses.get(0).getReviewCount()),
                () -> assertEquals("aaa", responses.get(0).getMemberName()),
                () -> assertEquals(5, responses.get(0).getStar()),
                () -> assertEquals("content", responses.get(0).getContent()),
                () -> assertEquals(LocalDateTime.of(2023, 7, 15, 9, 0), responses.get(0).getCreatedAt()),
                () -> assertEquals(LocalDateTime.of(2023, 7, 15, 9, 1), responses.get(0).getUpdatedAt())
        );
    }


    /*
        #######################################################################
        ###########                                                 ###########
        ###########              실패 케이스(FAIL CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    @Test
    @DisplayName("[SERVICE]리뷰를 위한 예약 가져오기 - 실패(유저 X)")
    void getReservationForReviewFailUserNotFound(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> reviewService.getReservationsForReview(1L, LocalDate.now(), 0));
        //then
        assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
    }

    @Nested
    @DisplayName("[SERVICE]리뷰 작성 실패 케이스들")
    class writeReviewFailCases{

        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            Member user = Member.builder().id(3L).build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]예약 수행자와 유저가 일치 X")
        void reservationNotMatchUser(){
            //given
            Member user = Member.builder().id(3L).build();
            Member otherUser = Member.builder().id(4L).build();

            Shop shop = Shop.builder().id(1L).name("aaa").build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .member(otherUser)
                                    .shop(shop)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(9, 0))
                                    .count(1)
                                    .reservationState(VISITED)
                                    .build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(RESERVATION_NOT_MATCH_USER, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰가 방문 상태가 아님")
        void reviewStateNotVisited(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .member(user)
                                    .shop(shop)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(9, 0))
                                    .count(1)
                                    .reservationState(ASSIGN)
                                    .build()
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(REVIEW_STATE_NOT_VISITED, exception.getReviewErrorType());
        }

        @Test
        @DisplayName("[SERVICE]이미 작성된 리뷰")
        void reviewAlreadyWritten(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").build();
            Review review = Review.builder()
                    .id(1L)
                    .build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .member(user)
                                    .shop(shop)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(9, 0))
                                    .count(1)
                                    .review(review)
                                    .reservationState(VISITED)
                                    .build()
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(REVIEW_ALREADY_WRITTEN, exception.getReviewErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 작성 시간 초과")
        void reviewTimeOver(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .member(user)
                                    .shop(shop)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(9, 0))
                                    .count(1)
                                    .reservationState(VISITED)
                                    .build()
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.writeReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 23)
                    ));
            //then
            assertEquals(REVIEW_TIME_OVER, exception.getReviewErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]리뷰 수정 실패 케이스들")
    class updateReviewFailCases{
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reviewService.updateReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 X")
        void reviewNotFound(){
            //given
            Member user = Member.builder().id(3L).build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.updateReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(REVIEW_NOT_FOUND, exception.getReviewErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 작성자와 멤버 일치 X")
        void reviewNotMatchUser(){
            //given
            Member user = Member.builder().id(3L).build();
            Member otherUser = Member.builder().id(4L).build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Review.builder()
                                    .id(1L)
                                    .member(otherUser)
                                    .star(5)
                                    .content("content!!!")
                                    .build()
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.updateReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 16)
                    ));
            //then
            assertEquals(REVIEW_NOT_MATCH_USER, exception.getReviewErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 수정 기간 초과")
        void reviewTimeOver(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .name("aaa")
                    .star(5)
                    .starSum(5)
                    .reviewCount(1)
                    .build();
            Reservation reservation = Reservation.builder()
                    .id(1L)
                    .shop(shop)
                    .reservationState(VISITED)
                    .resDay(LocalDate.of(2023, 7, 15))
                    .resTime(LocalTime.of(9, 0))
                    .build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Review.builder()
                                    .id(1L)
                                    .reservation(reservation)
                                    .shop(shop)
                                    .member(user)
                                    .star(5)
                                    .content("content!!!")
                                    .build()
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.updateReview(
                            ReviewDto.WriteReviewRequest.builder()
                                    .star(5)
                                    .content("content")
                                    .build(),
                            1L,
                            1L,
                            LocalDate.of(2023, 7, 23)
                    ));
            //then
            assertEquals(REVIEW_TIME_OVER, exception.getReviewErrorType());
        }
    }


    @Nested
    @DisplayName("[SERVICE]삭제를 위한 리뷰 가져오기 실패 케이스들")
    class getDataForDeleteReviewFailCases{
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reviewService.getDataForDeleteReview(1L, 1L));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 X")
        void reviewNotFound(){
            //given
            Member user = Member.builder().id(3L).build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.getDataForDeleteReview(1L, 1L));
            //then
            assertEquals(REVIEW_NOT_FOUND, exception.getReviewErrorType());
        }

        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            Member user = Member.builder().id(3L).build();
            Review review = Review.builder()
                    .id(1L)
                    .shop(Shop.builder().id(2L).build())
                    .member(user)
                    .star(5)
                    .build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            review
                    ));
            given(reservationRepository.findByReview(any()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reviewService.getDataForDeleteReview(1L, 1L));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 작성자와 멤버 일치 X")
        void reviewNotMatchUser(){
            //given
            Member user = Member.builder().id(3L).build();
            Member otherUser = Member.builder().id(4L).build();
            Review review = Review.builder()
                    .id(1L)
                    .shop(Shop.builder().id(2L).build())
                    .member(user)
                    .star(5)
                    .build();
            Reservation reservation = Reservation.builder()
                    .id(1L)
                    .member(user)
                    .build();
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            otherUser
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            review
                    ));
            given(reservationRepository.findByReview(any()))
                    .willReturn(Optional.of(
                            reservation
                    ));
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reviewService.getDataForDeleteReview(1L, 1L));
            //then
            assertEquals(REVIEW_NOT_MATCH_USER, exception.getReviewErrorType());
        }
    }


    @Test
    @DisplayName("[SERVICE]리뷰 삭제 - 실패(리뷰 X)")
    void deleteReviewCompleteFailReviewNotFound(){
        //given
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        ReviewException exception = assertThrows(ReviewException.class,
                () -> reviewService.deleteReviewComplete(1L));
        //then
        assertEquals(REVIEW_NOT_FOUND, exception.getReviewErrorType());
    }


    @Test
    @DisplayName("[SERVICE]리뷰 조회 for 유저 - 실패 (유저 X)")
    void getReviewsForUserFailUserNotFound(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> reviewService.getReviewsForUser(1L, 0));
        //then
        assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
    }


    @Test
    @DisplayName("[SERVICE]리뷰 조회 by 매장 - 실패 (매장 X)")
    void getReviewsForShopFailShopNotFound(){
        //given
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        ShopException exception = assertThrows(ShopException.class,
                () -> reviewService.getReviewsForShop(1L, 0));
        //then
        assertEquals(ShopErrorType.SHOP_NOT_FOUND, exception.getShopErrorType());
    }
}
