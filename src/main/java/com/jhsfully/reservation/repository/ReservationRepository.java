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

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(
            "SELECT COALESCE(SUM(r.count), 0) FROM reservation r " +
            "WHERE r.shop = ?1 AND r.resDay = ?2 AND r.resTime = ?3"
    )
    Integer getReservationCountWithShopAndTime(Shop shop, LocalDate day, LocalTime time);

    List<Reservation> findByMember(Member member);

    List<Reservation> findByShop(Shop shop);

}
