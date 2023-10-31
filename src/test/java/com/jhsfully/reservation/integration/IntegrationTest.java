package com.jhsfully.reservation.integration;

import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_CANNOT_ALLOW_GREEDY_USER;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_CANNOT_DELETE;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_CANNOT_VISIT_TIME_OVER;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_IS_OVERFLOW;
import static com.jhsfully.reservation.type.ReservationErrorType.RESERVATION_NOT_OPENED_DAY;
import static com.jhsfully.reservation.type.ReviewErrorType.REVIEW_TIME_OVER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jhsfully.reservation.exception.ReservationException;
import com.jhsfully.reservation.exception.ReviewException;
import com.jhsfully.reservation.facade.ReviewFacade;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.model.ReviewDto;
import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponse;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.service.ReviewService;
import com.jhsfully.reservation.service.ShopService;
import com.jhsfully.reservation.type.Days;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/*
    ####################### 통합 테스트 ############################

    데이터베이스 : H2 DATABASE, REDIS

    시작하기 전:
    - application.yml에서 MySQL설정을 비활성화 하고, H2 Database의 설정을 활성화함.
    - redis는 로컬로 미리 띄워놓아야 함.

    시나리오 1 - 매장 추가
    - 월~일 까지 오픈되는 매장 (partnerId : 1L)
    - 예약 오픈 기한은 2주 뒤까지
    - 오픈 시간대는 오전 9시부터 오후 6시까지 1시간 간격
    - 시간대 오픈 인원은 최대 4명

    시나리오 2 - 예약 프로세스
    - 7/15, 7/30을 예약하여 FAIL - (예약 신청 기준일 7/15)
    - 7/16 AM9, 3명 예약 성공 (예약 신청 기준일 7/15, memberId : 2L)
    - 7/16 AM10, 1명 예약 실패 (예약 신청 기준일 7/15, memberId : 2L) - 2번 예약 X
    - 7/16 AM9, 1명 예약 성공 (예약 신청 기준일 7/15, memberId : 3L)
    - 7/16 AM9, 1명 예약 실패 (예약 신청 기준일 7/15, memberId : 4L) - 예약인원초과
    - 파트너가, memberId = 2L의 3명 예약을 거절함
    - 파트너가, memberId = 3L의 예약을 승인함.
    - 7/16 AM9, 2명 예약 성공 (예약 신청 기준일 7/15, memberId : 4L)
    - 7/16 AM9, 2명 예약 실패 (예약 신청 기준일 7/15, memberId : 2L) - 예약인원초과
    - 7/29 AM9, 4명 예약 성공 (예약 신청 기준일 7/15, memberId : 2L)
    - 파트너가 7/29일 4명 예약을 승인함(승인 기준일 7/28)

    시나리오 3 - 방문 - 리뷰 프로세스
    - 7/16일 방문
    - 7/20일 리뷰작성 (별점 3개)
    - 7/29일 오전 8시 49분에 방문 조회함 (memberId : 2L) - 시간 이전이므로 조회 결과 X
    - 7/29일 오전 8시 51분에 방문 조회함 (memberId : 2L) - 조회 결과 1
    - 7/29일날 방문 처리 완료
    - 8/6 리뷰 작성 실패 - 일주일이 넘어버림.
    - 8/5 리뷰 작성 - 성공 (별점 4개)
    - 리뷰를 작성했던, 예약을 삭제하려고 함, 실패

    시나리오 4 - 유저가 가게 정보 조회하기
    - 7/15일에 예약 데이터를 조회함, 7/16일, 7/29일 예약 가능 카운트가 다름.
    - 별점이 3.5개인지 확인.

    시나리오 5- 별점 취소 확인
    - 7/29일 방문한 사람이, 리뷰 삭제함.
    - 별점 조회시, 3개 나오는지 확인.
 */

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Transactional
public class IntegrationTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewFacade reviewFacade;

    @BeforeAll
    static void setup(@Autowired DataSource dataSource){
        try(Connection connection = dataSource.getConnection()){
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("/testsqls/member.sql"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Nested
    @Order(1)
    @DisplayName("[시나리오1] - 매장 추가")
    class ScenarioOneAddShop{

        @Test
        @Rollback(value = false)
        @DisplayName("매장 추가")
        void addShop(){
            ShopDto.AddShopRequest request = ShopDto.AddShopRequest.builder()
                    .name("헤어샵")
                    .introduce("소개")
                    .address("주소")
                    .latitude(37.77)
                    .longitude(128.55)
                    .resOpenWeek(2)
                    .resOpenCount(4)
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
                                            LocalTime.of(10, 0),
                                            LocalTime.of(11, 0),
                                            LocalTime.of(12, 0),
                                            LocalTime.of(13, 0),
                                            LocalTime.of(14, 0),
                                            LocalTime.of(15, 0),
                                            LocalTime.of(16, 0),
                                            LocalTime.of(17, 0),
                                            LocalTime.of(18, 0)
                                    )
                            )
                    )
                    .build();
            shopService.addShop(1L, request);
        }

    }


    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("[시나리오2] - 예약 프로세스")
    class ScenarioTwoReservationProcess{

        @Test
        @Order(1)
        @DisplayName("예약 실패일 확인")
        void reservationFails(){
            //when

            ReservationException exception715 = assertThrows(ReservationException.class,
                    () ->             reservationService.addReservation(
                            2L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 15))
                                    .resTime(LocalTime.of(9, 0))
                                    .note("note")
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            ReservationException exception730 = assertThrows(ReservationException.class,
                    () ->             reservationService.addReservation(
                            2L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 30))
                                    .resTime(LocalTime.of(9, 0))
                                    .note("note")
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertAll(
                    () -> assertEquals(RESERVATION_NOT_OPENED_DAY, exception715.getReservationErrorType()),
                    () -> assertEquals(RESERVATION_NOT_OPENED_DAY, exception730.getReservationErrorType())
            );
        }

        @Test
        @Order(2)
        @Rollback(value = false)
        @DisplayName("2L이 7/16 오전 9시 3명 예약 성공함.")
        void reservationSuccess_2L_0716_9(){
            //when
            reservationService.addReservation(
                    2L,
                    ReservationDto.AddReservationRequest.builder()
                            .shopId(1L)
                            .count(3)
                            .resDay(LocalDate.of(2023, 7, 16))
                            .resTime(LocalTime.of(9, 0))
                            .note("note")
                            .build(),
                    LocalDate.of(2023, 7, 15));
        }

        @Test
        @Order(3)
        @DisplayName("2L이 7/16 오전 10시 1명 예약 실패함.")
        void reservationFail_2L_0716_10(){
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(
                            2L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(3)
                                    .resDay(LocalDate.of(2023, 7, 16))
                                    .resTime(LocalTime.of(10, 0))
                                    .note("note")
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_CANNOT_ALLOW_GREEDY_USER, exception.getReservationErrorType());
        }

        @Test
        @Order(4)
        @Rollback(value = false)
        @DisplayName("3L이 7/16 오전 9시 1명 예약 성공함.")
        void reservationSuccess_3L_0716_9(){
            //when
            reservationService.addReservation(
                    3L,
                    ReservationDto.AddReservationRequest.builder()
                            .shopId(1L)
                            .count(1)
                            .resDay(LocalDate.of(2023, 7, 16))
                            .resTime(LocalTime.of(9, 0))
                            .note("note")
                            .build(),
                    LocalDate.of(2023, 7, 15));
        }

        @Test
        @Order(5)
        @DisplayName("4L이 7/16 오전 9시 1명 예약 실패함. - 예약 인원 초과")
        void reservationFail_4L_0716_9(){
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(
                            4L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(1)
                                    .resDay(LocalDate.of(2023, 7, 16))
                                    .resTime(LocalTime.of(9, 0))
                                    .note("note")
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_IS_OVERFLOW, exception.getReservationErrorType());
        }

        //    - 파트너가, memberId = 2L의 오전 9시 3명 예약을 거절함
        @Test
        @Order(6)
        @Rollback(value = false)
        @DisplayName("매장 주인이 2L의 7/16 오전 9시 예약을 거절함.")
        void rejectReservation_2L_0716_9(){
            //when
            reservationService.rejectReservation(
                    1L,
                    1L,
                    LocalDate.of(2023,7,15)
            );
        }

        @Test
        @Order(7)
        @Rollback(value = false)
        @DisplayName("매장 주인이 3L의 7/16 오전 9시 예약을 승인함.")
        void assignReservation_3L_0716_9(){
            //when
            reservationService.assignReservation(
                    1L,
                    2L,
                    LocalDate.of(2023,7,15)
            );
        }

        @Test
        @Order(8)
        @Rollback(value = false)
        @DisplayName("4L이 7/16 오전 9시 2명 예약 성공함.")
        void reservationSuccess_4L_0716_9(){
            //when
            reservationService.addReservation(
                    4L,
                    ReservationDto.AddReservationRequest.builder()
                            .shopId(1L)
                            .count(2)
                            .resDay(LocalDate.of(2023, 7, 16))
                            .resTime(LocalTime.of(9, 0))
                            .note("note")
                            .build(),
                    LocalDate.of(2023, 7, 15));
        }


        @Test
        @Order(9)
        @DisplayName("2L이 7/16 오전 9시 2명 예약 실패함. - 예약 인원 초과")
        void reservationFail_2L_0716_9(){
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.addReservation(
                            2L,
                            ReservationDto.AddReservationRequest.builder()
                                    .shopId(1L)
                                    .count(2)
                                    .resDay(LocalDate.of(2023, 7, 16))
                                    .resTime(LocalTime.of(9, 0))
                                    .note("note")
                                    .build(),
                            LocalDate.of(2023, 7, 15)));
            //then
            assertEquals(RESERVATION_IS_OVERFLOW, exception.getReservationErrorType());
        }


        @Test
        @Order(10)
        @Rollback(value = false)
        @DisplayName("2L이 7/29 오전 9시 4명 예약 성공함.")
        void reservationSuccess_2L_0729_9(){
            //when
            reservationService.addReservation(
                    2L,
                    ReservationDto.AddReservationRequest.builder()
                            .shopId(1L)
                            .count(4)
                            .resDay(LocalDate.of(2023, 7, 29))
                            .resTime(LocalTime.of(9, 0))
                            .note("note")
                            .build(),
                    LocalDate.of(2023, 7, 15));
        }

        //    - 파트너가 7/29일 4명 예약을 승인함(승인 기준일 7/28)
        @Test
        @Order(11)
        @Rollback(value = false)
        @DisplayName("매장 주인이 2L의 7/29 오전 9시 예약을 승인함.")
        void assignReservation_2L_0729_9(){

            //when
            reservationService.assignReservation(
                    1L,
                    4L,
                    LocalDate.of(2023,7,28)
            );
        }
    }


    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("[시나리오3] - 방문 -> 리뷰 프로세스")
    class ScenarioThreeVisitAndReviewProcess{

        @Test
        @Order(1)
        @Rollback(value = false)
        @DisplayName("3L이 7/16 오전 8시 59분에 1명 방문")
        void VisitSuccess_3L_0716_9(){
            //방문은 키오스크에서 하므로, 매장 주인인 1L로 로그인되어야함.
            reservationService.visitReservation(1L, 2L,
                        LocalDate.of(2023, 7, 16),
                        LocalTime.of(8, 59)
                    );
        }

        @Test
        @Order(2)
        @Rollback(value = false)
        @DisplayName("3L이 7/16에 방문한 리뷰를 7/20에 작성 성공.")
        void ReviewSuccess_3L_0716_0720(){
            reviewFacade.writeReviewAndAddShopStar(
                    ReviewDto.WriteReviewRequest.builder()
                            .star(3)
                            .content("평균")
                            .build(),
                    3L,
                    2L,
                    LocalDate.of(2023, 7, 20)
            );
        }

        @Test
        @Order(3)
        @DisplayName("2L이 7/29일 오전 8:49에 방문 조회함 - 시간 범위 밖임.")
        void getReservationForVisitFail_2L_0729(){
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService
                            .getReservationForVisit(1L, 1L,
                                    new ReservationDto.GetReservationParam("010-2222-2222"),
                                    LocalDate.of(2023, 7, 29),
                                    LocalTime.of(8, 49)
                            ));

            //then
            assertEquals(RESERVATION_CANNOT_VISIT_TIME_OVER, exception.getReservationErrorType());
        }

        @Test
        @Order(4)
        @DisplayName("2L이 7/29일 오전 8:51에 방문 조회함")
        void getReservationForVisitSuccess_2L_0729(){
            //when
            ReservationDto.ReservationResponse reservation = reservationService
                    .getReservationForVisit(1L, 1L,
                            new ReservationDto.GetReservationParam("010-2222-2222"),
                            LocalDate.of(2023, 7, 29),
                            LocalTime.of(8, 51)
                    );
            //then
            assertAll(
                    () -> assertEquals("alice", reservation.getMemberName()),
                    () -> assertEquals(LocalTime.of(9, 0), reservation.getResTime()),
                    () -> assertEquals(4, reservation.getCount())
            );
        }


        @Test
        @Order(5)
        @Rollback(value = false)
        @DisplayName("2L이 7/29 오전 8시 55분에 4명 방문")
        void VisitSuccess_2L_0729_9(){
            //방문은 키오스크에서 하므로, 매장 주인인 1L로 로그인되어야함.
            reservationService.visitReservation(1L, 4L,
                    LocalDate.of(2023, 7, 29),
                    LocalTime.of(8, 55)
            );
        }

        @Test
        @Order(6)
        @DisplayName("2L이 8/6에 리뷰 작성 실패 - 작성 기한 초과")
        void reviewFail2L_0806(){
            //when
            ReviewException exception = assertThrows(ReviewException.class,
                    () ->
            reviewFacade.writeReviewAndAddShopStar(
                    ReviewDto.WriteReviewRequest.builder()
                            .star(4)
                            .content("good")
                            .build(),
                    2L,
                    4L,
                    LocalDate.of(2023, 8, 6)
            ));
            //then
            assertEquals(REVIEW_TIME_OVER, exception.getReviewErrorType());
        }

        @Test
        @Order(7)
        @Rollback(value = false)
        @DisplayName("2L이 8/5에 리뷰 작성 성공")
        void reviewSuccess2L_0805(){
            //when
            reviewFacade.writeReviewAndAddShopStar(
                    ReviewDto.WriteReviewRequest.builder()
                            .star(4)
                            .content("good")
                            .build(),
                    2L,
                    4L,
                    LocalDate.of(2023, 8, 5)
            );
        }

        @Test
        @Order(8)
        @DisplayName("2L 8/5에 작성한 리뷰에 대한 예약을 삭제하려고함 - 실패")
        void deleteReservationForReviewed_2L_0805(){
            //when
            ReservationException exception = assertThrows(ReservationException.class,
                    () -> reservationService.deleteReservation(2L, 4L));
            //then
            assertEquals(RESERVATION_CANNOT_DELETE, exception.getReservationErrorType());
        }
    }


    @Nested
    @Order(4)
    @DisplayName("[시나리오4] - 유저가 매장 데이터 조회하기")
    class ScenarioThreeFourGetShopData{
//            - 7/15일에 예약 데이터를 조회함, 7/16일, 7/29일 예약 가능 카운트가 다름.
//    - 별점이 3.5개인지 확인.
        @Test
        @DisplayName("7/15 에 매장 예약 데이터 조회함.")
        void getShopData_0715(){
            //when
            ShopDto.ShopDetailUserResponse response = shopService
                    .getShopDetailForUser(1L,
                            LocalDate.of(2023, 7, 15));

            assertAll(
                    () -> assertEquals(1L, response.getId()),
                    () -> assertEquals("헤어샵", response.getName()),
                    () -> assertEquals("소개", response.getIntroduce()),
                    () -> assertEquals("주소", response.getAddress()),
                    () -> assertEquals(3.5, response.getStar()),
                    () -> assertEquals(LocalDate.of(2023,7, 16), response.getResOpenDateTimes().get(0).getDate()),
                    () -> assertEquals(LocalTime.of(9, 0), response.getResOpenDateTimes().get(0).getReservationTimeSets().get(0).getTime()),
                    () -> assertEquals(1, response.getResOpenDateTimes().get(0).getReservationTimeSets().get(0).getCount())
            );
        }
    }


    @Nested
    @Order(5)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("[시나리오5] - 리뷰 삭제 및 별점 차감 확인")
    class ScenarioThreeFiveReviewDelete{

        @Test
        @Order(1)
        @DisplayName("2L이 8/5일에 작성한 리뷰를 삭제함.")
        void deleteReview_2L(){
            //when
            reviewFacade.deleteReviewAndSubShopStar(2L, 2L);
        }

        @Test
        @Order(2)
        @DisplayName("파트너가 별점 조회해본다.")
        void getShopDataForStar(){
            //when
            Page<ShopTopResponse> responses = shopService.getShopsByPartner(1L, 0);
            //then
            assertAll(
                    () -> assertEquals(3, responses.getContent().get(0).getStar())
            );
        }
    }
}
