package org.example.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheLoggingAspect {

    private final CacheManager cacheManager;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(cacheable)")
    public Object logCacheHitMiss(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String cacheName = cacheable.value()[0];
        String keyExpression = cacheable.key();

        // SpEL evaluation to get the cache key
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, args, nameDiscoverer);
        Expression expression = parser.parseExpression(keyExpression);
        String key = expression.getValue(context, String.class);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                log.info(">>> [CACHE] HIT - Key: [{}:{}], Method: [{}]", cacheName, key, method.getName());
                return wrapper.get();
            }
        }

        log.info(">>> [CACHE] MISS - Key: [{}:{}], Method: [{}] - Proceeding to DB", cacheName, key, method.getName());
        return joinPoint.proceed();
    }
}
