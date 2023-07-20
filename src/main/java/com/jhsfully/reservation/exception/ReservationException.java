package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.ReservationErrorType;
import lombok.Getter;

@Getter
public class ReservationException extends CustomException{

    private ReservationErrorType reservationErrorType;
    public ReservationException(ReservationErrorType reservationErrorType){
        super(reservationErrorType.getMessage());
        this.reservationErrorType = reservationErrorType;
    }

}
