package com.studentclub.interceptor;

import com.studentclub.annotation.RequiresAdmin;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色权限拦截器
 */
@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        RequiresAdmin requiresAdmin = handlerMethod.getMethodAnnotation(RequiresAdmin.class);
        if (requiresAdmin == null) {
            requiresAdmin = handlerMethod.getBeanType().getAnnotation(RequiresAdmin.class);
        }
        
        if (requiresAdmin != null) {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"无权限操作，需要管理员权限\"}");
                return false;
            }
        }
        
        return true;
    }
}
