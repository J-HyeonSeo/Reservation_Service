package com.jhsfully.reservation.domain;

import com.jhsfully.reservation.type.ReservationState;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Shop shop;
    @ManyToOne
    private Member member;
    private LocalDateTime res_time; //예약시간
    private ReservationState reservationState; //예약 승인 상태(READY, ASSIGN, REJECT)
    private boolean visited; //방문 여부
    private String note; //요청 사항 추가 기재

}
