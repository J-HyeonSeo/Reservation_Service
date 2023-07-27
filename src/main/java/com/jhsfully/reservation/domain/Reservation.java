package com.jhsfully.reservation.domain;

import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.type.ReservationState;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Shop shop;
    @ManyToOne
    private Member member;
    @OneToOne
    private Review review;
    private LocalDate resDay; //예약일
    private LocalTime resTime;//예약시간
    private int count; //방문 예약 인원
    @Enumerated(EnumType.STRING)
    private ReservationState reservationState; //예약 승인 상태(READY, ASSIGN, VISITED, EXPIRED, REJECT)
    private String note; //요청 사항 추가 기재


    public static ReservationDto.ReservationResponse toDto(Reservation reservation, long reservationCount){
        return ReservationDto.ReservationResponse.builder()
                .reservationCount(reservationCount)
                .id(reservation.getId())
                .memberName(reservation.getMember().getName())
                .shopName(reservation.getShop().getName())
                .resDay(reservation.getResDay())
                .resTime(reservation.getResTime())
                .count(reservation.getCount())
                .reservationState(reservation.getReservationState())
                .note(reservation.getNote())
                .build();
    }
}
