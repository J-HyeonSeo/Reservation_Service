package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShopErrorType {
    SHOP_NOT_FOUND("해당 매장이 존재하지 않습니다."),
    SHOP_NOT_MATCH_USER("해당 매장의 소유자가 아닙니다.");
    private final String message;
}
