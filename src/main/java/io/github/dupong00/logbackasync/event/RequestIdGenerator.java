package io.github.dupong00.logbackasync.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestIdGenerator {

    private final String runId;

    private final AtomicLong requestNo = new AtomicLong();

    public RequestIdGenerator(@Value("${app.run-id}") String runId) {
        this.runId = runId;
    }

    public String next() {
        return "%s-%06d".formatted(runId, requestNo.incrementAndGet());
    }
}
