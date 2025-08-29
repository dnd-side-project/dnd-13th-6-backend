package com.runky.global.aop;

import com.runky.global.security.auth.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

    @Pointcut("execution(public * com.runky.*.api.*Controller.*(..))")
    public void controllerPointcut() {
    }

    @Before("controllerPointcut()")
    public void logAround(JoinPoint joinPoint) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = (principal instanceof MemberPrincipal memberPrincipal) ? memberPrincipal.memberId() : -1L;

        log.info("time:{} / Request : {}, (userId={}) {}\n{}", ZonedDateTime.now(), request.getMethod(), userId, request.getRequestURI(),
                params(joinPoint));
    }

    @AfterReturning(value = "controllerPointcut()", returning = "returnObj")
    public void logAfterReturning(JoinPoint joinPoint, Object returnObj) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = (principal instanceof MemberPrincipal memberPrincipal) ? memberPrincipal.memberId() : -1L;

        log.info("Response : {}, (userId={}) {}\n{}", request.getMethod(), userId, request.getRequestURI(), returnObj);
    }

    private Map<String, Object> params(JoinPoint joinPoint) {
        String[] parameterNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], args[i]);
        }
        return params;
    }
}
