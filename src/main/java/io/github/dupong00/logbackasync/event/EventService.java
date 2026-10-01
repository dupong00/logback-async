package io.github.dupong00.logbackasync.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger eventLog = LoggerFactory.getLogger("EVENT_LOG");

    private static final int EVENTS_PER_REQUEST = 10;

    private final RequestIdGenerator requestIdGenerator;

    public EventResponse record() {
        String requestId = requestIdGenerator.next();
        for (int seq = 1; seq < EVENTS_PER_REQUEST; seq++) {
            eventLog.info("{}-{}", requestId, "%02d".formatted(seq));
        }
        eventLog.error("{}-{}", requestId, "%02d".formatted(EVENTS_PER_REQUEST));
        return new EventResponse(requestId, EVENTS_PER_REQUEST);
    }
}
