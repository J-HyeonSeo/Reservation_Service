package com.jhsfully.reservation.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    private String name;
    private String introduce;
    private double star;
    private String address;
    private double latitude;
    private double longitude;
    private int resOpenWeek; //예약을 몇 주 이후까지 받을 것인가?
    private int resOpenCount; //동시간대에 예약 가능한 인원
    @ElementCollection
    @CollectionTable(name = "res_open_day", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "open_day")
    private List<Integer> resOpenDays;
    @ElementCollection
    @CollectionTable(name = "res_open_time", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "open_time")
    private List<LocalTime> resOpenTimes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

}
