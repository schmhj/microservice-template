package com.schmhj.microservice.platform.observability;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.health.autoconfigure.contributor.HealthContributorAutoConfiguration;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AutoConfiguration(after = HealthContributorAutoConfiguration.class)
public class ObservabilityAutoConfiguration {

    @Bean
    public Filter correlationFilter() {
        return (request, response, chain) -> {
            var httpReq = (HttpServletRequest) request;
            var httpResp = (HttpServletResponse) response;
            String correlationId = Optional.ofNullable(httpReq.getHeader("X-Correlation-Id"))
                    .orElse(UUID.randomUUID().toString());
            MDC.put("X-Correlation-Id", correlationId);
            httpResp.setHeader("X-Correlation-Id", correlationId);

            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove("X-Correlation-Id");
            }
        };

    }

}
