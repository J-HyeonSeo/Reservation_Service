package com.jhsfully.reservation.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @OneToOne
    @NotNull
    private Reservation reservation;
    private int star;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
