package io.github.dupong00.logbackasync.admin;

public record WriteDelayCommand(long millis, long ttlSeconds) {

    public boolean isOff() {
        return millis == 0;
    }
}
