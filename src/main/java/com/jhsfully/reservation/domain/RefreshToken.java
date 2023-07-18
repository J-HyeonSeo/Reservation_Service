package com.jhsfully.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14)// 1분 -> 1시간 -> 1일 -> 2주(초 단위)
public class RefreshToken {
    @Id
    private String refreshToken;
    private boolean isPartner;
    private boolean isAdmin;
}
