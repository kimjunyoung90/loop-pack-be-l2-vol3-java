package com.loopers.interfaces.api.auth;

import com.loopers.support.auth.AdminOnly;
import com.loopers.support.auth.AuthConstants;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class) != null
                || handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class);

        if (!adminOnly) {
            return true;
        }

        String ldap = request.getHeader(AuthConstants.LDAP_HEADER);
        if (!AuthConstants.ADMIN_LDAP.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        return true;
    }
}
