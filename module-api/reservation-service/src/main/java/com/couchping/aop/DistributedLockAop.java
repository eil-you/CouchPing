package com.couchping.aop;

import com.couchping.annotation.DistributedLock;
import com.couchping.exception.CouchPingException;
import com.couchping.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @DistributedLock 어노테이션이 붙은 메서드를 가로채서
 * Redisson 분산락 기반으로 동시성을 제어하는 AOP 클래스.
 * 일반 트랜잭션(@Transactional)보다 먼저 락을 획득해야 하므로 우선순위를 높게(HIGHEST_PRECEDENCE + 1) 설정합니다.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.couchping.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // CustomSpringELParser를 사용해 메서드 인자를 기반으로 동적 키를 생성 (ex: "room:lock:1")
        Object dynamicKey = CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        String lockKey = String.valueOf(dynamicKey);
        
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                // API 호출 시 락을 획득하지 못하면 예외를 발생시켜 클라이언트에게 명확히 에러를 전달합니다.
                log.error("락 획득 실패 (요청 폭주, 동시성 오류) - key: {}", lockKey);
                throw new CouchPingException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            log.info("락 획득 성공 - key: {}", lockKey);
            return joinPoint.proceed(); // 타겟 메서드 실행
        } catch (InterruptedException e) {
            log.error("분산 락 획득 중 인터럽트 발생 - key: {}", lockKey, e);
            throw new InterruptedException("분산 락 획득 중 인터럽트 발생 - key: " + lockKey);
        } finally {
            try {
                // 현재 스레드가 락을 가지고 있다면 해제
                if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.info("락 반환 완료 - key: {}", lockKey);
                }
            } catch (IllegalMonitorStateException e) {
                log.info("이미 해제된 락입니다 - key: {}", lockKey);
            }
        }
    }
}
