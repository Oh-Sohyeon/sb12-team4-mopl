package com.mopl.realtime.global.config;

import com.mopl.infrastructure.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class RealtimeSecurityConfig {
}