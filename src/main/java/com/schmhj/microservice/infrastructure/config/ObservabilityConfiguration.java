package com.schmhj.microservice.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.schmhj.microservice.domain.common.CorrelationId;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class ObservabilityConfiguration {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Bean
    public Filter correlationFilter() {
        return (request, response, chain) -> {
            var httpReq = (HttpServletRequest) request;
            var httpResp = (HttpServletResponse) response;
            String correlationId = Optional.ofNullable(httpReq.getHeader(CORRELATION_ID))
                    .orElse(CorrelationId.generate().toString());
            MDC.put(CORRELATION_ID, correlationId);
            httpResp.setHeader(CORRELATION_ID, correlationId);

            try (MDC.MDCCloseable _ = MDC.putCloseable(CORRELATION_ID, correlationId)) {
                chain.doFilter(request, response);
            }
        };

    }

}
