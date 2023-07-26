package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.lock.RedisLock;
import com.jhsfully.reservation.model.ReservationDto;
import com.jhsfully.reservation.service.ReservationService;
import com.jhsfully.reservation.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequestMapping("/reservation")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /*
        예약 수행
        - 예약을 수행하는 과정은, 예약 카운트를 검증하는 과정에서 동시성 이슈가 발생할 수 있음.
        - "09:00" 시간대에 허용예약이 최대 4였을 경우에, 동시에, 해당 함수가 실행될 경우에
            둘다 최대 가능 예약이 4가 되어, 동시간대에 8명의 예약신청이 발생할 우려가 존재함.
        - shopId를 기준으로, lock을 걸어, 같은 shop에 대하여 동시에 예약하지 못하도록 함.
     */
    @RedisLock(group = "reservation", key = "request.shopId")
    @PostMapping
    public ResponseEntity<?> addReservation(@RequestBody ReservationDto.AddReservationRequest request){
        Long memberId = MemberUtil.getMemberId();
        reservationService.addReservation(memberId, request, LocalDate.now());
        return ResponseEntity.ok().build();
    }

    //예약 취소
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.deleteReservation(memberId, reservationId);
        return ResponseEntity.ok().build();
    }

    //유저 예약 조회(예약 승인/거절 상태 표시) -> 내용이 간단하므로 상세조회는 구현하지 않음.
    @GetMapping("/user/{pageIndex}")
    public ResponseEntity<?> getReservationsForUser(@PathVariable int pageIndex, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate){
        Long memberId = MemberUtil.getMemberId();
        List<ReservationDto.ReservationResponse> responses = reservationService.getReservationForUser(memberId, startDate, pageIndex);
        return ResponseEntity.ok(responses);
    }


    //매장 예약 조회(파트너)
    @GetMapping("/partner/{shopId}/{pageIndex}")
    public ResponseEntity<?> getReservationsByShop(@PathVariable Long shopId, @PathVariable int pageIndex, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate){
        Long memberId = MemberUtil.getMemberId();
        List<ReservationDto.ReservationResponse> responses = reservationService.getReservationByShop(memberId, shopId, startDate, pageIndex);
        return ResponseEntity.ok(responses);
    }

    //매장 예약 거절(파트너가 들어온 예약을 거절함)
    @PatchMapping("/reject/{reservationId}")
    public ResponseEntity<?> rejectReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.rejectReservation(memberId, reservationId, LocalDate.now());
        return ResponseEntity.ok().build();
    }

    //매장 예약 수락(파트너가 들어온 예약을 수락함)
    @PatchMapping("/assign/{reservationId}")
    public ResponseEntity<?> assignReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.assignReservation(memberId, reservationId, LocalDate.now());
        return ResponseEntity.ok().build();
    }

    //키오스크를 위한, 예약 조회(연락처로 조회 10분전 ~ 예약시간 까지의 데이터만 조회가능)(파트너권한)
    //키오스크는 기본적으로 파트너의 계정으로 로그인되어 있다고 가정함.
    @GetMapping("/kiosk/{shopId}")
    public ResponseEntity<?> getReservationForVisit(@PathVariable Long shopId, @ModelAttribute @Valid ReservationDto.GetReservationParam param){
        Long memberId = MemberUtil.getMemberId();
        ReservationDto.ReservationResponse response = reservationService.getReservationForVisit(memberId, shopId, param, LocalDate.now(), LocalTime.now());
        return ResponseEntity.ok(response);
    }

    //키오스크 도착확인(파트너권한)
    //키오스크는 기본적으로 파트너의 계정으로 로그인되어 있다고 가정함.
    @PatchMapping("/kiosk/visit/{reservationId}")
    public ResponseEntity<?> visitShopByReservation(@PathVariable Long reservationId){
        Long memberId = MemberUtil.getMemberId();
        reservationService.visitReservation(memberId, reservationId, LocalDate.now(), LocalTime.now());
        return ResponseEntity.ok().build();
    }

}
