package com.jhsfully.reservation.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class MemberUtil {

    public static Long getMemberId(){
        if(SecurityContextHolder.getContext() == null){
            return -1L;
        }
        if(SecurityContextHolder.getContext().getAuthentication() == null){
            return -1L;
        }
        if(SecurityContextHolder.getContext().getAuthentication().getName() == null){
            return -1L;
        }

        String stringMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long result;
        try{
            result = Long.parseLong(stringMemberId);
        }catch (Exception e){
            result = -1L;
        }
        return result;
    }

}
