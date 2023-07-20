package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/reservation")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    //예약 수행
    @PostMapping
    public ResponseEntity<?> addReservation(@RequestBody ReservationDto.AddReservationRequest request){
        Long memberId = MemberUtil.getMemberId();
        reservationService.addReservation(memberId, request);
        return null;
    }

    //예약 취소
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.deleteReservation(memberId, reservationId);
        return ResponseEntity.ok().build();
    }

    //유저 예약 조회(예약 승인/거절 상태 표시) -> 내용이 간단하므로 상세조회는 구현하지 않음.
    @GetMapping("/user")
    public ResponseEntity<?> getReservationsForUser(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate){
        Long memberId = MemberUtil.getMemberId();
        List<ReservationDto.ReservationResponse> responses = reservationService.getReservationForUser(memberId, startDate);
        return ResponseEntity.ok(responses);
    }


    //매장 예약 조회(파트너)
    @GetMapping("/partner/{shopId}")
    public ResponseEntity<?> getReservationsByShop(@PathVariable Long shopId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate){
        Long memberId = MemberUtil.getMemberId();
        List<ReservationDto.ReservationResponse> responses = reservationService.getReservationByShop(memberId, shopId, startDate);
        return ResponseEntity.ok(responses);
    }

    //매장 예약 거절(파트너가 들어온 예약을 거절함)
    @PatchMapping("/reject/{reservationId}")
    public ResponseEntity<?> rejectReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.rejectReservation(memberId, reservationId);
        return ResponseEntity.ok().build();
    }

    //매장 예약 수락(파트너가 들어온 예약을 수락함)
    @PatchMapping("/assign/{reservationId}")
    public ResponseEntity<?> assignReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.assignReservation(memberId, reservationId);
        return ResponseEntity.ok().build();
    }

    //키오스크를 위한, 예약 조회(연락처로 조회 10분전 ~ 예약시간 까지의 데이터만 조회가능)(파트너권한)
    @GetMapping("/kiosk")
    public ResponseEntity<?> getReservationForVisit(){
        return null;
    }

    //키오스크 도착확인(파트너권한)
    @PatchMapping("/visit")
    public ResponseEntity<?> visitShopByReservation(){
        return null;
    }

}
