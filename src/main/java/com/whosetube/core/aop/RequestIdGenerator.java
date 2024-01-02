package com.whosetube.core.aop;

import com.whosetube.core.model.BaseRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

//@Aspect
@Log4j2
@Component
public class RequestIdGenerator {

    @Around("execution(* com.whosetube.core.controller.*.*(..))")
    public Object createUniqueId(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        BaseRequest<?> currentRequest = null;
        int i=0;
        if(args[0] == null){
            currentRequest = new BaseRequest<>();
            args[0] = currentRequest;
        }else{
            for(i=0;i< args.length;i++){
                if(args[i] instanceof BaseRequest){
                    currentRequest = (BaseRequest<?>) args[i];
                    break;
                }
            }
        }
        if(currentRequest != null){
            currentRequest.setRequestId(UUID.randomUUID());
            currentRequest.setRequestTime(LocalDateTime.now());
        }
        args[i] = currentRequest;
        return joinPoint.proceed(args);
    }

}
