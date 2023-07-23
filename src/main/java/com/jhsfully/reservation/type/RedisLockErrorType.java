package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisLockErrorType {
    REDIS_ALREADY_LOCKED("해당 key는 이미 lock되었습니다.");
    private final String message;
}
