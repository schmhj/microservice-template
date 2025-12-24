package com.schmhj.microservice.platform.resilence;

import java.time.Duration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;

@AutoConfiguration
public class ResilienceAutoConfiguration {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50f)
                .slidingWindowSize(100)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(5))
                .build();
    }

}
