package com.jhsfully.reservation.repository.custom.impl;

import static com.jhsfully.reservation.type.ReservationState.ASSIGN;
import static com.jhsfully.reservation.type.ReservationState.EXPIRED;
import static com.jhsfully.reservation.type.ReservationState.READY;
import static com.jhsfully.reservation.type.ReservationState.REJECT;
import static com.jhsfully.reservation.type.ReservationState.VISITED;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.QReservation;
import com.jhsfully.reservation.domain.Reservation;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.repository.custom.ReservationCustomRepository;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final EntityManager entityManager;

  public void updateReservationState(LocalDate today){

    QReservation reservation = QReservation.reservation;

    new JPAUpdateClause(entityManager, reservation)
        .set(reservation.reservationState,
            new CaseBuilder()
                .when(reservation.reservationState.eq(READY))
                .then(REJECT)
                .when(reservation.reservationState.eq(ASSIGN))
                .then(EXPIRED)
                .otherwise(reservation.reservationState))
        .where(
            reservation.reservationState.eq(READY).and(reservation.resDay.eq(today))
                .or(
                    reservation.reservationState.eq(ASSIGN).and(reservation.resDay.eq(today.minusDays(1)))
                )
        )
        .execute();

  }

  //리뷰를 위한 예약 찾기.
  public Page<Reservation> findReservationForReview(Member member, LocalDate dateNow, Pageable pageable){

    QReservation reservation = QReservation.reservation;

    List<Reservation> reservationList = jpaQueryFactory
        .selectFrom(reservation)
        .where(
            reservation.member.eq(member),
            reservation.reservationState.eq(VISITED),
            reservation.review.isNull(),
            reservation.resDay.goe(dateNow)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long count = jpaQueryFactory.select(Wildcard.count)
        .from(reservation)
        .where(
            reservation.member.eq(member),
            reservation.reservationState.eq(VISITED),
            reservation.review.isNull(),
            reservation.resDay.goe(dateNow)
        ).fetchFirst();

    return new PageImpl<>(reservationList, pageable, count);
  }

  public Integer getReservationCountWithShopAndDayForMember(Shop shop, Member member, LocalDate day) {
    QReservation reservation = QReservation.reservation;

    Integer result = jpaQueryFactory.select(reservation.count.sum().coalesce(0))
        .from(reservation)
        .where(
            reservation.shop.eq(shop),
            reservation.member.eq(member),
            reservation.reservationState.in(ASSIGN, READY),
            reservation.resDay.eq(day)
        )
        .fetchOne();

    return Objects.requireNonNullElse(result, 0);
  }

  public Integer getReservationCountWithShopAndTime(Shop shop, LocalDate day, LocalTime time) {
    QReservation reservation = QReservation.reservation;

    Integer result = jpaQueryFactory.select(reservation.count.sum().coalesce(0))
        .from(reservation)
        .where(
            reservation.shop.eq(shop),
            reservation.reservationState.in(ASSIGN, READY, VISITED),
            reservation.resDay.eq(day),
            reservation.resTime.eq(time)
        )
        .fetchOne();

    return Objects.requireNonNullElse(result, 0);
  }

}
