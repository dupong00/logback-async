package io.github.dupong00.logbackasync.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/write-delay")
@RequiredArgsConstructor
public class WriteDelayController {

    private final WriteDelayService writeDelayService;

    @PostMapping
    WriteDelayResponse set(@RequestParam @Min(0) @Max(1000) long millis,
                           @RequestParam(defaultValue = "120") @Min(1) @Max(600) long ttlSeconds) {
        return writeDelayService.apply(new WriteDelayCommand(millis, ttlSeconds));
    }

    @GetMapping
    WriteDelayResponse get() {
        return writeDelayService.current();
    }
}
