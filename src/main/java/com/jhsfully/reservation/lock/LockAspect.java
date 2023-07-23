package com.jhsfully.reservation.lock;

import com.jhsfully.reservation.exception.RedisLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;

import static com.jhsfully.reservation.type.RedisLockErrorType.REDIS_ALREADY_LOCKED;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAspect {

    //starter-data-redis의존성에서 기본적으로 주입해주는 redisTemplate
    private final RedisTemplate<String, String> redisTemplate;

    //함수의 파라미터의 name을 가져오는 객체임.(locking에 사용할 key를 찾을 때 사용할거임)
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer;

    @Around("@annotation(redisLock)")
    public Object locking(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String group = redisLock.group();
        String key = getKeyString(joinPoint, redisLock.key());

        //ex) "lock:reservation-1", "lock:review-1"
        String lockKey = "lock:" + group + "-" + key;

        boolean acquired = false; //key를 얻었는지 확인하는 변수

        /*
            try문에 return이 존재하더라도, return을 수행하기 전 반드시,
            finally를 수행하여 key를 release하고, return이 수행됨.

            2초간, 기다리면서, key를 획득하고,
            키를 획득하였다면, joinPoint를 이어서 수행하고, 키 반납 및 결과 리턴
            키를 획득하지 못했다면, Exception을 Throw함.
         */
        try{
            acquired = getLock(lockKey, 2_000L);
            if(acquired){
                log.info(lockKey + "를 취득하였습니다.");
                return joinPoint.proceed();
            }else{
                throw new RedisLockException(REDIS_ALREADY_LOCKED);
            }
        }finally {
            if(acquired){
                log.info(lockKey + "를 반환하였습니다.");
                releaseLock(lockKey);
            }
        }

    }

    //key가 되어줄 String값을 매개변수에서 가져오는 함수.
    private String getKeyString(ProceedingJoinPoint joinPoint, String path){

        Object[] args = joinPoint.getArgs();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(methodSignature.getMethod());

        log.info(Arrays.toString(parameterNames));

        String[] parameterPath = path.split("\\."); // 점(.)을 기준으로 나눔

        log.info(Arrays.toString(parameterPath));

        for (int i = 0; i < parameterNames.length; i++) {
            if(parameterNames[i].equals(parameterPath[0])){

                if(parameterPath.length == 1){ //멤버 변수가 없는 경우.
                    return args[i] + "";
                }

                try {
                    return findFieldValue(1, args[i], parameterPath);
                }catch (Exception e){
                    new RuntimeException("데이터 구조가 올바르지 않습니다.");
                }
            }
        }

        return "";
    }

    /*FieldValue를 탐색하여, 원하는 값을 찾아내어 반환함.
    * idx : split된 멤버변수에서 현재 해당되는 위치.
    * arg : 현재 붙들고 있는 멤버변수
    * fields : 전체 멤벼변수의 계층의 name이 담긴 String 배열.
    *
    * ex) findFieldValue(1, Member arg, String[] ["member", "id"])
    * */
    private String findFieldValue(int idx, Object arg, String[] fields) throws NoSuchFieldException, IllegalAccessException {
        Field field = arg.getClass().getDeclaredField(fields[idx]); //현재 idx에 해당되는 멤버변수 할당.
        field.setAccessible(true); //필드 강제 엑세스

        Object fieldValue = field.get(arg); //멤버변수를 Value로써 추출함.
        log.info(fieldValue.toString());

        if(idx == fields.length - 1){ //마지막 멤버변수까지 도달하였기에 값을 반환함.
            return fieldValue + "";
        }else{ //아직 설정한 멤버변수가 남아있음.
            return findFieldValue(idx + 1, fieldValue, fields); //재귀호출로 한 단계 더 들어감.
        }
    }

    // lock을 얻는 함수.
    private boolean getLock(String lockKey, Long timeout) throws InterruptedException {
        Long startTime = System.currentTimeMillis();
        Long endTime = startTime + timeout;
        boolean result = false;

        //timeout시간내에 lock을 취득하지 못하면 false를 리턴하게 되어있음.
        while(System.currentTimeMillis() < endTime){

            //Duration.ofMillis를 통해 최대 15초의 길이를 갖는 lock을 생성함.
            //이는 lock의 생명주기가 최소 (메소드 수행 완료 시간) ~ 최대(15초) 라는 것을 의미함.
            result = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofMillis(15_000L));
            if(result){
                break;
            }

            //서버에 부하가 가지 않도록 0.1초 정도 딜레이를 준 이후에 다시 lock취득을 요청함.
            Thread.sleep(100);
        }
        return result;
    }

    // lock을 반납하는 함수.
    private void releaseLock(String lockKey){
        redisTemplate.delete(lockKey);
    }

}
