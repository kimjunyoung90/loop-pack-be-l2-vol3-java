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

import java.util.List;
import java.util.Map;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Map<String, Map<String, List<String>>> LDAP_DIRECTORY = Map.of(
            "loopers", Map.of(
                    "admin", List.of("test")
            )
    );

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

        String account = request.getHeader(AuthConstants.LDAP_HEADER);
        if (!isAdmin(account)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        return true;
    }

    private boolean isAdmin(String account) {
        if (account == null) {
            return false;
        }
        return LDAP_DIRECTORY
                .getOrDefault("loopers", Map.of())
                .getOrDefault("admin", List.of())
                .contains(account);
    }
}
