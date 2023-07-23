package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.RedisLockErrorType;
import lombok.Getter;

@Getter
public class RedisLockException extends CustomException{
    private final RedisLockErrorType redisLockErrorType;

    public RedisLockException(RedisLockErrorType redisLockErrorType){
        super(redisLockErrorType.getMessage());
        this.redisLockErrorType = redisLockErrorType;
    }
}
