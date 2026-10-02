package io.github.dupong00.logbackasync.admin;

import java.time.OffsetDateTime;

public record WriteDelayResponse(long millis, OffsetDateTime expiresAt) {
}
