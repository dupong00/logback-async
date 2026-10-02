package io.github.dupong00.logbackasync.admin;

import io.github.dupong00.logbackasync.logging.WriteDelay;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class WriteDelayService {

    private static final Logger adminLog = LoggerFactory.getLogger("ADMIN_LOG");

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "write-delay-reset");
                thread.setDaemon(true);
                return thread;
            });

    private OffsetDateTime expiresAt;
    private ScheduledFuture<?> pendingReset;
    private long generation;

    public synchronized WriteDelayResponse apply(WriteDelayCommand command) {
        if (command.isOff()) {
            return clear();
        }
        return set(command);
    }

    private WriteDelayResponse set(WriteDelayCommand command) {
        cancelPendingReset();
        WriteDelay.setMillis(command.millis());
        expiresAt = OffsetDateTime.now().plusSeconds(command.ttlSeconds());
        long scheduledGeneration = generation;
        pendingReset = scheduler.schedule(() -> expire(scheduledGeneration), command.ttlSeconds(), TimeUnit.SECONDS);
        adminLog.info("write delay set millis={} ttlSeconds={} expiresAt={}", command.millis(), command.ttlSeconds(), expiresAt);
        return current();
    }

    public synchronized WriteDelayResponse clear() {
        cancelPendingReset();
        WriteDelay.setMillis(0);
        expiresAt = null;
        adminLog.info("write delay cleared");
        return current();
    }

    public synchronized WriteDelayResponse current() {
        return new WriteDelayResponse(WriteDelay.getMillis(), expiresAt);
    }

    private synchronized void expire(long scheduledGeneration) {
        if (scheduledGeneration != generation) {
            return;
        }
        pendingReset = null;
        WriteDelay.setMillis(0);
        expiresAt = null;
        adminLog.info("write delay expired");
    }

    private void cancelPendingReset() {
        generation++;
        if (pendingReset != null) {
            pendingReset.cancel(false);
            pendingReset = null;
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
