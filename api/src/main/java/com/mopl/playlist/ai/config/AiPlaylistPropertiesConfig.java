package com.mopl.playlist.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiPlaylistProperties.class)
public class AiPlaylistPropertiesConfig {
}