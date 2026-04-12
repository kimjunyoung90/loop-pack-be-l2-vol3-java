package com.loopers.interfaces.api.auth;

import com.loopers.application.user.UserService;
import com.loopers.domain.user.User;
import com.loopers.support.auth.AuthConstants;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	private final UserService userService;

	// 인증 캐시
	private final ConcurrentHashMap<String, AuthUser> authCache = new ConcurrentHashMap<>();

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUser.class)
				&& parameter.getParameterType().equals(AuthUser.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String loginId = webRequest.getHeader(AuthConstants.LOGIN_ID_HEADER);
		String loginPwd = webRequest.getHeader(AuthConstants.LOGIN_PW_HEADER);

		String cacheKey = loginId + ":" + loginPwd;
		AuthUser cached = authCache.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		User user = userService.authenticateUser(loginId, loginPwd);
		AuthUser authUser = new AuthUser(user.getId(), user.getLoginId());
		authCache.put(cacheKey, authUser);
		return authUser;
	}
}
