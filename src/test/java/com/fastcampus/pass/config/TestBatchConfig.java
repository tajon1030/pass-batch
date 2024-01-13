package com.fastcampus.pass.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableAutoConfiguration
@EnableBatchProcessing
@EntityScan("com.fastcampus.pass.repository")
@EnableJpaRepositories("com.fastcampus.pass.repository")
@EnableConfigurationProperties(BatchProperties.class)
public class TestBatchConfig {
}
