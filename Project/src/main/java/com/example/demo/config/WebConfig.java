package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptor.SessionTimeoutInterceptor;

@Configuration
// インターセプターの設定
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private SessionTimeoutInterceptor sessionTimeoutInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(sessionTimeoutInterceptor)
				// すべてのリクエストに適用
				.addPathPatterns("/**")
				// ログインページは除外
				.excludePathPatterns(PathConfig.LOGIN_URL, PathConfig.LOGOUT_URL)
				.excludePathPatterns("/css/**");

	}
}