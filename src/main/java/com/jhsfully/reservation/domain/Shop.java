package com.jhsfully.reservation.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private int resOpenWeek;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
