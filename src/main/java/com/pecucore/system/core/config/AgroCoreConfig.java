package com.pecucore.system.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AgroCoreProperties.class)
public class AgroCoreConfig {
}