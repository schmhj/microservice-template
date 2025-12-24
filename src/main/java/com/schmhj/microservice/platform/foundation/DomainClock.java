package com.schmhj.microservice.platform.foundation;

import java.time.Instant;

public interface DomainClock {
    Instant now();

    static DomainClock system() {
        return Instant::now;
    }

}
