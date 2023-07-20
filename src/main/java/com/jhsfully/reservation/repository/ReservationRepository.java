package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

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

//    List<Reservation> findByMember(Member member);
    List<Reservation> findByMemberAndResDayGreaterThanEqual(Member member, LocalDate startDate);

    List<Reservation> findByShopAndResDayGreaterThanEqual(Shop shop, LocalDate startDate);

    Optional<Reservation> findByShopAndResDayAndResTimeGreaterThanEqual(Shop shop, LocalDate resDay, LocalTime now);
}
