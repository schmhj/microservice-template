package com.schmhj.microservice.platform.foundation;

import java.util.UUID;

public final record CorrelationId(String value) {

    public static CorrelationId of(String value) {
        return new CorrelationId(UUID.fromString(value).toString());
    }

    public static CorrelationId generate() {
        return new CorrelationId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
