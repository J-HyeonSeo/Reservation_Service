package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository repository;
    private final MemberRepository memberRepository;

    //


}
