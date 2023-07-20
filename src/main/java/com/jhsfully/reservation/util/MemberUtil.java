package com.jhsfully.reservation.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class MemberUtil {

    public static Long getMemberId(){
        if(SecurityContextHolder.getContext() == null){
            return null;
        }
        if(SecurityContextHolder.getContext().getAuthentication() == null){
            return null;
        }
        if(SecurityContextHolder.getContext().getAuthentication().getName() == null){
            return null;
        }

        String stringMemberId = SecurityContextHolder.getContext().getAuthentication().getName();

        return Long.parseLong(stringMemberId);
    }

}
