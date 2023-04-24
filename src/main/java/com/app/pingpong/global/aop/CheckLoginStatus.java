package com.app.pingpong.global.aop;

import com.app.pingpong.global.common.status.Authority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckLoginStatus {
    Authority auth() default Authority.ROLE_USER;
}
