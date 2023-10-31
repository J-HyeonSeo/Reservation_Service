package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Review;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.repository.custom.ReservationCustomRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
    ReservationCustomRepository {
    Optional<Reservation> findByReview(Review review);
    Page<Reservation> findByMemberAndResDayGreaterThanEqual(Member member, LocalDate startDate, Pageable pageable);
    Page<Reservation> findByShopAndResDayGreaterThanEqual(Shop shop, LocalDate startDate, Pageable pageable);
    Optional<Reservation> findByMemberAndShopAndResDayAndResTimeGreaterThanEqual(Member member, Shop shop, LocalDate resDay, LocalTime now);
}
