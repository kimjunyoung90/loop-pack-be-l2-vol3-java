package com.loopers.interfaces.api.auth;

import com.loopers.support.auth.AuthConstants;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String loginId = request.getHeader(AuthConstants.LOGIN_ID_HEADER);
		String loginPwd = request.getHeader(AuthConstants.LOGIN_PW_HEADER);

		if (loginId == null || loginPwd == null) {
			throw new CoreException(ErrorType.UNAUTHORIZED, "인증 헤더가 누락되었습니다.");
		}

		return true;
	}
}
