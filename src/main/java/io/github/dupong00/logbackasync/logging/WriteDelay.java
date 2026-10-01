package io.github.dupong00.logbackasync.logging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WriteDelay {

    @Getter
    @Setter
    private static volatile long millis;
}
