package org.example.campaign;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class CampaignCacheAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(cacheable)")
    public Object cache(
            ProceedingJoinPoint joinPoint,
            CampaignCacheable cacheable
    ) throws Throwable {

        String key = cacheable.key();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return cached;
        }

        Object result = joinPoint.proceed();
        redisTemplate.opsForValue()
                .set(key, result, cacheable.ttl(), TimeUnit.SECONDS);

        return result;
    }
}
