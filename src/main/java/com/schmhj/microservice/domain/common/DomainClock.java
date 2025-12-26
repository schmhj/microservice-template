package com.schmhj.microservice.domain.common;

import java.time.Instant;

public interface DomainClock {
    Instant now();

    static DomainClock system() {
        return Instant::now;
    }

}
