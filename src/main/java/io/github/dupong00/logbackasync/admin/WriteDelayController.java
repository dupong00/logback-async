package io.github.dupong00.logbackasync.admin;

import io.github.dupong00.logbackasync.logging.WriteDelay;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/write-delay")
public class WriteDelayController {

    private static final long MAX_DELAY_MILLIS = 1_000;

    @PostMapping
    WriteDelayResponse set(@RequestParam long millis) {
        if (millis < 0 || millis > MAX_DELAY_MILLIS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "millis는 0~" + MAX_DELAY_MILLIS);
        }
        WriteDelay.setMillis(millis);
        return new WriteDelayResponse(WriteDelay.getMillis());
    }

    @GetMapping
    WriteDelayResponse get() {
        return new WriteDelayResponse(WriteDelay.getMillis());
    }
}
