package com.jhsfully.reservation.domain;

import com.jhsfully.reservation.model.ReviewDto;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Shop shop;
    @OneToOne
    @NotNull
    private Reservation reservation;
    private int star;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewDto.ReviewResponse toDto(Review review, long reviewCount){
        return ReviewDto.ReviewResponse.builder()
                .id(review.getId())
                .reviewCount(reviewCount)
                .memberName(review.getMember().getName())
                .star(review.getStar())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}
