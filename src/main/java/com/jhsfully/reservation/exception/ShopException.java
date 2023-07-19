package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.ShopErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopException extends RuntimeException{
    private ShopErrorType shopErrorType;
    private String message;

    public ShopException(ShopErrorType shopErrorType){
        this.shopErrorType = shopErrorType;
        this.message = shopErrorType.getMessage();
    }
}
