package com.loopers.config;

import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginCheckInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final LoginUserArgumentResolver loginUserArgumentResolver;
	private final LoginCheckInterceptor loginCheckInterceptor;
	private final AdminAuthInterceptor adminAuthInterceptor;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(loginUserArgumentResolver);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(loginCheckInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/v1/users");

		registry.addInterceptor(adminAuthInterceptor)
				.addPathPatterns("/api-admin/**");

	}
}
