package com.couchping.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 분산락을 적용하기 위한 커스텀 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락의 이름 (Key)
     * SpEL 표현식을 사용할 수 있습니다. (예: "'room:lock:' + #roomId")
     */
    String key();

    /**
     * 락을 획득하기 위해 대기할 시간 (기본값 5초)
     */
    long waitTime() default 5L;

    /**
     * 락을 획득한 후 점유할 최대 시간 (기본값 3초)
     * 이 시간이 지나면 락이 자동으로 해제됩니다.
     */
    long leaseTime() default 3L;

    /**
     * 시간 단위 (기본값 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
