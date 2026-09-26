package com.mopl.auth.config;

import com.mopl.infrastructure.security.jwt.JwtProperties;
import com.mopl.infrastructure.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

	@Bean
	public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
		return new JwtTokenProvider(jwtProperties);
	}
}
