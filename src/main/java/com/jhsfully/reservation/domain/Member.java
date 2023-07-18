package com.jhsfully.reservation.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String name;
    private String phone;
    private boolean isPartner;
    private boolean isAdmin;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;

}
