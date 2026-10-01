package io.github.dupong00.logbackasync.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class DelayedFileAppender extends FileAppender<ILoggingEvent> {

    @Override
    protected void subAppend(ILoggingEvent event) {
        long delay = WriteDelay.getMillis();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.subAppend(event);
    }
}
