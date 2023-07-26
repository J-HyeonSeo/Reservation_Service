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
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import com.jhsfully.reservation.repository.ReviewRepository;
import com.jhsfully.reservation.repository.ShopRepository;
import com.jhsfully.reservation.service.impl.ReservationServiceImpl;
import com.jhsfully.reservation.type.Days;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_NOT_FOUND;
import static com.jhsfully.reservation.type.ReservationErrorType.*;
import static com.jhsfully.reservation.type.ReservationState.*;
import static com.jhsfully.reservation.type.ReviewErrorType.REVIEW_NOT_FOUND;
import static com.jhsfully.reservation.type.ShopErrorType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private ReservationServiceImpl reservationService;

    /*
        #######################################################################
        ###########                                                 ###########
        ###########           성공 케이스(SUCCESS CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    @Test
    @DisplayName("[SERVICE]예약 추가 - 성공")
    void addReservationSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Member user = Member.builder().id(3L).build();
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Shop.builder()
                                .id(1L)
                                .member(partner)
                                .resOpenWeek(1)
                                .resOpenCount(3)
                                .resOpenDays(
                                        new ArrayList<>(
                                                Arrays.asList(
                                                        Days.MON,
                                                        Days.TUE,
                                                        Days.WED,
                                                        Days.THU,
                                                        Days.FRI,
                                                        Days.SAT,
                                                        Days.SUN
                                                )
                                        )
                                )
                                .resOpenTimes(
                                        new ArrayList<>(
                                                Arrays.asList(
                                                        LocalTime.of(9, 0),
                                                        LocalTime.of(10,0),
                                                        LocalTime.of(11, 0),
                                                        LocalTime.of(12, 0)
                                                )
                                        )
                                )
                                .build()
                ));
        given(memberRepository.findById(1L))
                .willReturn(Optional.of(
                        user
                ));
        //when
        reservationService.addReservation(1L,
                ReservationDto.AddReservationRequest.builder()
                        .shopId(1L)
                        .resDay(LocalDate.of(2023, 7, 15))
                        .resTime(LocalTime.of(12, 0))
                        .count(3)
                        .note("비고")
                        .build(),
                LocalDate.of(2023, 7, 14));
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        Reservation reservation = captor.getValue();

        assertAll(
                () -> assertEquals(1L, reservation.getShop().getId()),
                () -> assertEquals(3L, reservation.getMember().getId()),
                () -> assertEquals(LocalDate.of(2023, 7, 15), reservation.getResDay()),
                () -> assertEquals(LocalTime.of(12, 0), reservation.getResTime()),
                () -> assertEquals(3, reservation.getCount()),
                () -> assertEquals(READY, reservation.getReservationState()),
                () -> assertEquals("비고", reservation.getNote())
        );
    }

    @Test
    @DisplayName("[SERVICE]예약 조회 for 유저 - 성공")
    void getReservationForUserSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder().name("aaa").build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        given(reservationRepository.findByMemberAndResDayGreaterThanEqual(any(), any(), any()))
                .willReturn(
                        new PageImpl<>(
                                new ArrayList<>(
                                        Arrays.asList(
                                                Reservation.builder()
                                                        .id(1L)
                                                        .shop(shop)
                                                        .member(user)
                                                        .reservationState(READY)
                                                        .resDay(LocalDate.of(2023, 7, 15))
                                                        .resTime(LocalTime.of(12, 0))
                                                        .count(3)
                                                        .note("비고")
                                                        .build()
                                        )
                                ),
                                PageRequest.of(0, 10),
                                1
                        )
                );
        //when
        List<ReservationDto.ReservationResponse> reservations = reservationService
                .getReservationForUser(3L,
                        LocalDate.of(2023, 7, 15),
                        0);
        //then
        assertAll(
                () -> assertEquals(1L, reservations.get(0).getId()),
                () -> assertEquals("aaa", reservations.get(0).getShopName()),
                () -> assertEquals(LocalDate.of(2023,7,15), reservations.get(0).getResDay()),
                () -> assertEquals(LocalTime.of(12, 0), reservations.get(0).getResTime()),
                () -> assertEquals(3, reservations.get(0).getCount()),
                () -> assertEquals(READY, reservations.get(0).getReservationState()),
                () -> assertEquals("비고", reservations.get(0).getNote())
        );
    }

    @Test
    @DisplayName("[SERVICE]예약 조회 By 매장 - 성공")
    void getReservationByShopSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder().id(1L).name("aaa").member(partner).build();
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        partner
                ));
        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(shop));
        given(reservationRepository.findByShopAndResDayGreaterThanEqual(any(), any(), any()))
                .willReturn(
                        new PageImpl<>(
                                new ArrayList<>(
                                        Arrays.asList(
                                                Reservation.builder()
                                                        .id(1L)
                                                        .shop(shop)
                                                        .member(user)
                                                        .reservationState(READY)
                                                        .resDay(LocalDate.of(2023, 7, 15))
                                                        .resTime(LocalTime.of(12, 0))
                                                        .count(3)
                                                        .note("비고")
                                                        .build()
                                        )
                                ),
                                PageRequest.of(0, 10),
                                1
                        )
                );
        //when
        List<ReservationDto.ReservationResponse> reservations = reservationService
                .getReservationByShop(1L, 1L,
                        LocalDate.of(2023, 7, 15),
                        0);
        //then
        assertAll(
                () -> assertEquals(1L, reservations.get(0).getId()),
                () -> assertEquals("aaa", reservations.get(0).getShopName()),
                () -> assertEquals(LocalDate.of(2023,7,15), reservations.get(0).getResDay()),
                () -> assertEquals(LocalTime.of(12, 0), reservations.get(0).getResTime()),
                () -> assertEquals(3, reservations.get(0).getCount()),
                () -> assertEquals(READY, reservations.get(0).getReservationState()),
                () -> assertEquals("비고", reservations.get(0).getNote())
        );
    }

    @Test
    @DisplayName("[SERVICE]예약 삭제(Hard Delete) - 성공")
    void deleteReservationHardSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder().build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .shop(shop)
                                .member(user)
                                .reservationState(READY)
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(12, 0))
                                .count(3)
                                .note("비고")
                                .build()
                ));

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        //when
        reservationService.deleteReservation(3L, 1L);
        //then
        verify(reservationRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("[SERVICE]예약 삭제(Soft Delete) - 성공")
    void deleteReservationSoftSuccess(){
        //given
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder().build();

        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .shop(shop)
                                .member(user)
                                .reservationState(ASSIGN)
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(12, 0))
                                .count(3)
                                .note("비고")
                                .build()
                ));

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        user
                ));
        //when
        reservationService.deleteReservation(3L, 1L);
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        //then
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(EXPIRED, captor.getValue().getReservationState());
    }

    @Test
    @DisplayName("[SERVICE]예약 거절 - 성공")
    void rejectReservationSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Shop shop = Shop.builder().id(1L).member(partner).build();
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .resDay(LocalDate.of(2023,7,15))
                                    .resTime(LocalTime.of(12, 0))
                                    .reservationState(READY)
                                    .build()
                        )
                );
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                            partner
                        )
                );
        //when
        reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 14));
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(REJECT, captor.getValue().getReservationState());

    }

    @Test
    @DisplayName("[SERVICE]예약 승인 - 성공")
    void assignReservationSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Shop shop = Shop.builder().id(1L).member(partner).build();
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                                Reservation.builder()
                                        .id(1L)
                                        .shop(shop)
                                        .resDay(LocalDate.of(2023,7,15))
                                        .resTime(LocalTime.of(12, 0))
                                        .reservationState(READY)
                                        .build()
                        )
                );
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                                partner
                        )
                );
        //when
        reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 14));
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(ASSIGN, captor.getValue().getReservationState());
    }

    @Test
    @DisplayName("[SERVICE]예약 조회 for 방문 - 성공")
    void getReservationForVisitSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder()
                        .id(1L)
                        .name("aaa")
                        .member(partner)
                        .resOpenWeek(1)
                        .resOpenCount(3)
                        .build();

        given(shopRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        shop
                ));
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        partner
                ));
        given(memberRepository.findByPhone(anyString()))
                .willReturn(Optional.of(
                        user
                ));
        given(reservationRepository.findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(any(), any(), any(), any()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .shop(shop)
                                .member(user)
                                .reservationState(ASSIGN)
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(12, 0))
                                .count(3)
                                .note("비고")
                                .build()
                ));
        //when
        ReservationDto.ReservationResponse response = reservationService.getReservationForVisit(1L,
                1L,
                new ReservationDto.GetReservationParam("010-1111-1111"),
                LocalDate.of(2023, 7, 15),
                LocalTime.of(11, 55));
        //then
        assertAll(
                () -> assertEquals(1, response.getReservationCount()),
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals("aaa", response.getShopName()),
                () -> assertEquals(LocalDate.of(2023, 7, 15), response.getResDay()),
                () -> assertEquals(LocalTime.of(12, 0), response.getResTime()),
                () -> assertEquals(3, response.getCount()),
                () -> assertEquals(ASSIGN, response.getReservationState()),
                () -> assertEquals("비고", response.getNote())
        );
    }

    @Test
    @DisplayName("[SERVICE]예약 방문 - 성공")
    void visitReservationSuccess(){
        //given
        Member partner = Member.builder().id(1L).build();
        Member user = Member.builder().id(3L).build();
        Shop shop = Shop.builder()
                .id(1L)
                .name("aaa")
                .member(partner)
                .resOpenWeek(1)
                .resOpenCount(3)
                .build();
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .shop(shop)
                                .member(user)
                                .reservationState(ASSIGN)
                                .resDay(LocalDate.of(2023, 7, 15))
                                .resTime(LocalTime.of(12, 0))
                                .count(3)
                                .note("비고")
                                .build()
                ));
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        partner
                ));

        //when
        reservationService.visitReservation(1L,
                1L,
                LocalDate.of(2023, 7, 15),
                LocalTime.of(11, 55));
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(VISITED, captor.getValue().getReservationState());
    }

    @Test
    @DisplayName("[SERVICE]예약에 리뷰 할당 - 성공")
    void setReviewSuccess(){
        //given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .build()
                ));
        Review review = Review.builder().id(1L).build();
        given(reviewRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        review
                ));
        //when
        reservationService.setReview(1L, 1L);
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(review, captor.getValue().getReview());
    }

    @Test
    @DisplayName("[SERVICE]예약에 리뷰 해제 - 성공")
    void releaseReviewSuccess(){
        //given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Reservation.builder()
                                .id(1L)
                                .build()
                ));
        //when
        reservationService.releaseReview(1L);
        //then
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertEquals(null, captor.getValue().getReview());
    }



    /*
        #######################################################################
        ###########                                                 ###########
        ###########              실패 케이스(FAIL CASES)              ###########
        ###########                                                 ###########
        #######################################################################
     */

    @Nested
    @DisplayName("[SERVICE]예약 추가 실패 케이스들")
    class addReservationFailCases{
        @Test
        @DisplayName("[SERVICE]매장 X")
        void shopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder().shopId(1L).build(),
                            LocalDate.now()));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder().shopId(1L).build(),
                            LocalDate.now()));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]예약인원이 0명")
        void reservationCannotAllowZero(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(0)
                                    .build(),
                            LocalDate.now()));
            //then
            assertEquals(RESERVATION_CANNOT_ALLOW_ZERO, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]매장이 삭제된 상태")
        void shopIsDeleted(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder().isDeleted(true).build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .build(),
                            LocalDate.now()));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]오픈 날짜 범위 초과")
        void reservationDayOutOfRange(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder()
                                    .id(1L)
                                    .resOpenWeek(1)
                                    .resOpenDays(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            Days.MON,
                                                            Days.TUE,
                                                            Days.WED,
                                                            Days.THU,
                                                            Days.FRI
                                                    )
                                            )
                                    )
                                    .resOpenTimes(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            LocalTime.of(9, 0),
                                                            LocalTime.of(10,0),
                                                            LocalTime.of(11, 0),
                                                            LocalTime.of(12, 0)
                                                    )
                                            )
                                    )
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 24))
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_NOT_OPENED_DAY, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]오픈 요일이 아님")
        void reservationDayNotOpened(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder()
                                    .id(1L)
                                    .resOpenWeek(1)
                                    .resOpenDays(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            Days.MON,
                                                            Days.TUE,
                                                            Days.WED,
                                                            Days.THU,
                                                            Days.FRI
                                                    )
                                            )
                                    )
                                    .resOpenTimes(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            LocalTime.of(9, 0),
                                                            LocalTime.of(10,0),
                                                            LocalTime.of(11, 0),
                                                            LocalTime.of(12, 0)
                                                    )
                                            )
                                    )
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 16))
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_NOT_OPENED_DAY, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]오픈된 시간대가 아님")
        void reservationTimeNotOpened(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder()
                                    .id(1L)
                                    .resOpenWeek(1)
                                    .resOpenDays(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            Days.MON,
                                                            Days.TUE,
                                                            Days.WED,
                                                            Days.THU,
                                                            Days.FRI
                                                    )
                                            )
                                    )
                                    .resOpenTimes(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            LocalTime.of(9, 0),
                                                            LocalTime.of(10,0),
                                                            LocalTime.of(11, 0),
                                                            LocalTime.of(12, 0)
                                                    )
                                            )
                                    )
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 18))
                                    .resTime(LocalTime.of(13, 0))
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_NOT_OPENED_TIME, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]하루에 같은 매장의 같은 날에 2번이상 금지")
        void reservationCannotAllowGreedyUser(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder()
                                    .id(1L)
                                    .resOpenWeek(1)
                                    .resOpenDays(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            Days.MON,
                                                            Days.TUE,
                                                            Days.WED,
                                                            Days.THU,
                                                            Days.FRI
                                                    )
                                            )
                                    )
                                    .resOpenTimes(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            LocalTime.of(9, 0),
                                                            LocalTime.of(10,0),
                                                            LocalTime.of(11, 0),
                                                            LocalTime.of(12, 0)
                                                    )
                                            )
                                    )
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            given(reservationRepository.getReservationCountWithShopAndDayForMember(any(), any(), any()))
                    .willReturn(1);
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 18))
                                    .resTime(LocalTime.of(12, 0))
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_ALLOW_GREEDY_USER, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]초과된 예약")
        void reservationIsOverflow(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Shop.builder()
                                    .id(1L)
                                    .resOpenWeek(1)
                                    .resOpenCount(3)
                                    .resOpenDays(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            Days.MON,
                                                            Days.TUE,
                                                            Days.WED,
                                                            Days.THU,
                                                            Days.FRI
                                                    )
                                            )
                                    )
                                    .resOpenTimes(
                                            new ArrayList<>(
                                                    Arrays.asList(
                                                            LocalTime.of(9, 0),
                                                            LocalTime.of(10,0),
                                                            LocalTime.of(11, 0),
                                                            LocalTime.of(12, 0)
                                                    )
                                            )
                                    )
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Member.builder().build()
                    ));
            given(reservationRepository.getReservationCountWithShopAndDayForMember(any(), any(), any()))
                    .willReturn(0);
            given(reservationRepository.getReservationCountWithShopAndTime(any(), any(), any()))
                    .willReturn(3);
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(1L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 18))
                                    .resTime(LocalTime.of(12, 0))
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_IS_OVERFLOW, exception.getReservationErrorType());
        }
    }

    @Test
    @DisplayName("[SERVICE]예약 조회 for 유저 - 실패(유저 X)")
    void getReservationForUserFailUserNotFound(){
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> reservationService.getReservationForUser(1L,
                        LocalDate.now(),
                        0));
        //then
        assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
    }

    @Nested
    @DisplayName("[SERVICE]예약 조회 By 매장 실패 케이스들")
    class getReservationByShopFailCases{
        @Test
        @DisplayName("[SERVICE]매장 X")
        void shopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationByShop(1L, 1L, LocalDate.now(), 0));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").member(partner).build();
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(shop));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.getReservationByShop(1L, 1L, LocalDate.now(), 0));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]매장 삭제 상태")
        void shopIsDeleted(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").member(partner).isDeleted(true).build();
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(shop));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(partner));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationByShop(1L, 1L, LocalDate.now(), 0));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 매장 소유주 매칭 X")
        void shopNotMatchUser(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().id(1L).name("aaa").member(partner).build();
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(shop));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(user));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationByShop(1L, 1L, LocalDate.now(), 0));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]예약 삭제 실패 케이스들")
    class deleteReservationFailCases{
        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().build();

            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(READY)
                                    .build()
                    ));

            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]예약, 유저 매칭 X")
        void reservationNotMatchUser(){
            //given
            Member user = Member.builder().id(3L).build();
            Member otherUser = Member.builder().id(4L).build();
            Shop shop = Shop.builder().build();

            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(READY)
                                    .build()
                    ));

            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            otherUser
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(RESERVATION_NOT_MATCH_USER, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]예약 삭제 못함 = REJECT 상태")
        void reservationCannotDeleteAssign(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().build();

            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(REJECT)
                                    .build()
                    ));

            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(RESERVATION_CANNOT_DELETE, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]예약 삭제 못함 = VISTED 상태")
        void reservationCannotDeleteVisited(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().build();

            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(VISITED)
                                    .build()
                    ));

            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(RESERVATION_CANNOT_DELETE, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]예약 삭제 못함 = EXPIRED 상태")
        void reservationCannotDeleteExpired(){
            //given
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder().build();

            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(EXPIRED)
                                    .build()
                    ));

            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            user
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(1L, 1L));
            //then
            assertEquals(RESERVATION_CANNOT_DELETE, exception.getReservationErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]예약 거절 실패 케이스들")
    class rejectReservationFailCases{
        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty()
                    );

            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]매장의 주인과, 멤버가 매칭 X")
        void shopNotMatchUser(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member otherUser = Member.builder().id(2L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    otherUser
                            )
                    );
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]READY 상태가 아닌 예약")
        void reservationCannotRejectNotReady(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(ASSIGN)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    partner
                            )
                    );
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_REJECT_NOT_READY, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]오늘을 포함한 이전 날짜는 REJECT불가능.")
        void reservationCannotRejectNowEqualBefore(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    partner
                            )
                    );
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.rejectReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_REJECT_NOW_EQUAL_BEFORE, exception.getReservationErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]예약 승인 실패 케이스들")
    class assignReservationFailCases{
        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty()
                    );

            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]매장의 주인과, 멤버가 매칭 X")
        void shopNotMatchUser(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member otherUser = Member.builder().id(2L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    otherUser
                            )
                    );
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }
        @Test
        @DisplayName("[SERVICE]READY 상태가 아닌 예약")
        void reservationCannotAssignNotReady(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(REJECT)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    partner
                            )
                    );
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_ASSIGN_NOT_READY, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]오늘을 포함한 이전 날짜는 ASSIGN불가능.")
        void reservationCannotAssignNowEqualBefore(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().id(1L).member(partner).build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    Reservation.builder()
                                            .id(1L)
                                            .shop(shop)
                                            .resDay(LocalDate.of(2023,7,15))
                                            .resTime(LocalTime.of(12, 0))
                                            .reservationState(READY)
                                            .build()
                            )
                    );
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                                    partner
                            )
                    );
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.assignReservation(1L, 1L, LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_ASSIGN_NOW_EQUAL_BEFORE, exception.getReservationErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]예약 조회 for 방문 실패 케이스들")
    class getReservationForVisitFailCases{

        @Test
        @DisplayName("[SERVICE]매장 X")
        void shopNotFound(){
            //given
            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(SHOP_NOT_FOUND, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder().build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 삭제 상태")
        void shopIsDeleted(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .isDeleted(true)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(SHOP_IS_DELETED, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 주인과 멤버 매칭 X")
        void shopNotMatchUser(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .member(user)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]전화번호로 유저 찾기 FAIL")
        void userNotFoundWithPhoneNumber(){
            //given
            Member partner = Member.builder().id(1L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            given(memberRepository.findByPhone(anyString()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            given(memberRepository.findByPhone(anyString()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(any(), any(), any(), any()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]ASSIGN 되지 않은 예약")
        void reservationCannotVisitNotAssign(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .name("aaa")
                    .member(partner)
                    .resOpenWeek(1)
                    .resOpenCount(3)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            given(memberRepository.findByPhone(anyString()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(any(), any(), any(), any()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(READY)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(12, 0))
                                    .count(3)
                                    .note("비고")
                                    .build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 55)
                    ));
            //then
            assertEquals(RESERVATION_CANNOT_VISIT_NOT_ASSIGN, exception.getReservationErrorType());
        }
        @Test
        @DisplayName("[SERVICE]Time Over 된 예약")
        void reservationCannotVisitTimeOver(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .name("aaa")
                    .member(partner)
                    .resOpenWeek(1)
                    .resOpenCount(3)
                    .build();

            given(shopRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            shop
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            given(memberRepository.findByPhone(anyString()))
                    .willReturn(Optional.of(
                            user
                    ));
            given(reservationRepository.findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(any(), any(), any(), any()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(ASSIGN)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(12, 0))
                                    .count(3)
                                    .note("비고")
                                    .build()
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.getReservationForVisit(
                            1L, 1L,
                            new ReservationDto.GetReservationParam("010-1111-1111"),
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(11, 50)
                    ));
            //then
            assertEquals(RESERVATION_CANNOT_VISIT_TIME_OVER, exception.getReservationErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]에약 방문 실패 케이스들")
    class visitReservationFailCases{
        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                                LocalDate.of(2023, 7, 15),
                                LocalTime.of(12, 0)
                            ));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]유저 X")
        void userNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            AuthenticationException exception = assertThrows(AuthenticationException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(12, 0)
                    ));
            //then
            assertEquals(AUTHENTICATION_USER_NOT_FOUND, exception.getAuthenticationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]매장 주인과 멤버 매칭 X")
        void shopNotMatchUser(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member otherPartner = Member.builder().id(2L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            otherPartner
                    ));
            //when
            ShopException exception = assertThrows(ShopException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(12, 0)
                    ));
            //then
            assertEquals(SHOP_NOT_MATCH_USER, exception.getShopErrorType());
        }

        @Test
        @DisplayName("[SERVICE]ASSIGN 되지 않은 예약")
        void reservationCannotVisitNotAssign(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(READY)
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(12, 0)
                    ));
            //then
            assertEquals(RESERVATION_CANNOT_VISIT_NOT_ASSIGN, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]방문일이 일치하지 않음.")
        void reservationCannotVisitDayNotEqual(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(ASSIGN)
                                    .resDay(LocalDate.of(2023, 7, 16))
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(12, 0)
                    ));
            //then
            assertEquals(RESERVATION_CANNOT_VISIT_DAY_NOT_EQUAL, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]Time Over 된 예약")
        void reservationCannotVisitTimeOver(){
            //given
            Member partner = Member.builder().id(1L).build();
            Member user = Member.builder().id(3L).build();
            Shop shop = Shop.builder()
                    .id(1L)
                    .member(partner)
                    .build();
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .shop(shop)
                                    .member(user)
                                    .reservationState(ASSIGN)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(12, 0))
                                    .build()
                    ));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            partner
                    ));
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.visitReservation(1L, 1L,
                            LocalDate.of(2023, 7, 15),
                            LocalTime.of(12, 0, 1)
                    ));
            //then
            assertEquals(RESERVATION_CANNOT_VISIT_TIME_OVER, exception.getReservationErrorType());
        }
    }

    @Nested
    @DisplayName("[SERVICE]예약에 리뷰 할당 실패 케이스들")
    class setReviewFailCases{
        @Test
        @DisplayName("[SERVICE]예약 X")
        void reservationNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.setReview(1L, 1L));
            //then
            assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
        }

        @Test
        @DisplayName("[SERVICE]리뷰 X")
        void reviewNotFound(){
            //given
            given(reservationRepository.findById(anyLong()))
                    .willReturn(Optional.of(
                            Reservation.builder()
                                    .id(1L)
                                    .build()
                    ));
            given(reviewRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () -> reservationService.setReview(1L, 1L));
            //then
            assertEquals(REVIEW_NOT_FOUND, exception.getReviewErrorType());
        }
    }

    @Test
    @DisplayName("[SERVICE]예약에 리뷰 해제 - 실패 (예약 X)")
    void releaseReviewFailReservationNotFound(){
        //given
        given(reservationRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        ReservationException exception = assertThrows(ReservationException.class,
                () -> reservationService.releaseReview(1L));
        //then
        assertEquals(RESERVATION_NOT_FOUND, exception.getReservationErrorType());
    }
}
