package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.ShopErrorType;
import lombok.Getter;

@Getter
public class ShopException extends CustomException{
    private ShopErrorType shopErrorType;
    public ShopException(ShopErrorType shopErrorType){
        super(shopErrorType.getMessage());
        this.shopErrorType = shopErrorType;
    }
}
