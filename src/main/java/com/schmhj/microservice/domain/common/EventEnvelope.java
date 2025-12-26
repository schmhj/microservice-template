package com.schmhj.microservice.domain.common;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(String id, String type, Instant timestamp, Object payload) {
    public static EventEnvelope of(String type, Object payload) {
        return new EventEnvelope(UUID.randomUUID().toString(), type, Instant.now(), payload);
    }
}
