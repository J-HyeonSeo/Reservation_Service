package com.jhsfully.reservation.repository.custom;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Shop;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationCustomRepository {

  void updateReservationState(LocalDate today);
  Page<Reservation> findReservationForReview(Member member, LocalDate dateNow, Pageable pageable);
  Integer getReservationCountWithShopAndDayForMember(Shop shop, Member member, LocalDate day);
  Integer getReservationCountWithShopAndTime(Shop shop, LocalDate day, LocalTime time);

}
