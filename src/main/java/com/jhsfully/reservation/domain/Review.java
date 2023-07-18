package com.jhsfully.reservation.domain;

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
@Entity(name = "review")
public class Review {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Reservation reservation;
    private int star;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
