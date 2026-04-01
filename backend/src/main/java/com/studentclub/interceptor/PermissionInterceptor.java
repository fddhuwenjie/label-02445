package com.studentclub.interceptor;

import com.studentclub.annotation.RequiresAdmin;
import com.studentclub.exception.PermissionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();
        
        if (clazz.isAnnotationPresent(RequiresAdmin.class) || method.isAnnotationPresent(RequiresAdmin.class)) {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                throw new PermissionException("无管理员权限");
            }
        }
        
        return true;
    }
}
