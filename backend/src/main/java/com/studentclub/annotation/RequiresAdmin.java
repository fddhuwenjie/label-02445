package com.studentclub.annotation;

import java.lang.annotation.*;

/**
 * 要求管理员权限注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresAdmin {
}
