package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.type.ReservationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    //새벽마다 스케줄러에 의해 상태가 업데이트될 쿼리문.
    @Modifying
    @Query(
            "UPDATE reservation r SET r.reservationState = " +
                    "CASE " +
                    "WHEN r.reservationState='READY' THEN 'REJECT' " +
                    "WHEN r.reservationState='ASSIGN' THEN 'EXPIRED' " +
                    "END " +
            "WHERE (r.reservationState = 'READY' AND r.resDay = :today) OR " +
                    "(r.reservationState = 'ASSIGN' AND r.resDay = :yesterday)"
    )
    void updateReservationState(@Param("today") LocalDate today, @Param("yesterday") LocalDate yesterday);

    //직접 서버로 가져와서 처리하기 보단, 데이터베이스가 처리하는 것이 더욱 효율성이 좋을 것 같음.(일단 보류함)
    Page<Reservation> findByReservationStateIn(List<ReservationState> states, Pageable pageable);

    //리뷰를 위한 예약 찾기.
    @Query(
            "SELECT r FROM reservation r " +
                    "WHERE " +
                    "r.member = ?1 AND " +
                    "r.reservationState = 'VISITED' AND " +
                    "r.review = null AND " +
                    "r.resDay >= ?2"
    )
    Page<Reservation> findReservationForReview(Member member, LocalDate dateNow, Pageable pageable);

    @Query(
            "SELECT COALESCE(SUM(r.count), 0) FROM reservation r " +
                    "WHERE r.shop = ?1 AND " +
                    "r.member = ?2 AND " +
                    "(r.reservationState='ASSIGN' OR r.reservationState='READY' ) AND" +
                    " r.resDay = ?3"
    )
    Integer getReservationCountWithShopAndDayForMember(Shop shop, Member member, LocalDate day);

    @Query(
            "SELECT COALESCE(SUM(r.count), 0) FROM reservation r " +
            "WHERE r.shop = ?1 AND (r.reservationState='ASSIGN' OR r.reservationState='READY' ) AND r.resDay = ?2 AND r.resTime = ?3 "
    )
    Integer getReservationCountWithShopAndTime(Shop shop, LocalDate day, LocalTime time);

    Page<Reservation> findByMemberAndResDayGreaterThanEqual(Member member, LocalDate startDate, Pageable pageable);

    Page<Reservation> findByShopAndResDayGreaterThanEqual(Shop shop, LocalDate startDate, Pageable pageable);

    Optional<Reservation> findByShopAndResDayAndResTimeGreaterThanEqual(Shop shop, LocalDate resDay, LocalTime now);
}
