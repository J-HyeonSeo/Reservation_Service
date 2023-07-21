package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.ReviewErrorType;
import lombok.Getter;

@Getter
public class ReviewException extends CustomException{
    private final ReviewErrorType reviewErrorType;
    public ReviewException(ReviewErrorType reviewErrorType){
        super(reviewErrorType.getMessage());
        this.reviewErrorType = reviewErrorType;
    }
}
