package io.github.dupong00.logbackasync.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Logback 설정보다 먼저 기동 조건을 검사한다.
 * FileAppender는 설정 단계에서 파일을 열고 기본으로 이어 쓰므로 이 검사가 그보다 앞서야 한다.
 */
public class StartupValidator implements EnvironmentPostProcessor, Ordered {

    public static final List<String> POLICY_PROFILES = List.of("sync", "baseline", "blocking", "dropping");

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> active = Arrays.stream(environment.getActiveProfiles())
                .filter(POLICY_PROFILES::contains)
                .distinct()
                .toList();
        if (active.size() != 1) {
            throw new IllegalStateException("정책 프로필 " + POLICY_PROFILES + " 중 정확히 하나를 활성화해야 한다. 현재: " + active);
        }

        String runId = environment.getProperty("app.run-id");
        if (runId == null || !runId.matches("\\d{4}")) {
            throw new IllegalStateException("app.run-id는 4자리 숫자여야 한다. 현재: " + runId);
        }

        Path runsDir = Path.of(environment.getRequiredProperty("app.runs-dir")).toAbsolutePath().normalize();
        Path runDir = runsDir.resolve(runId);
        try {
            Files.createDirectories(runsDir);
            Files.createDirectory(runDir);
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("실행 번호 " + runId + "의 결과가 이미 있다: " + runDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        environment.getPropertySources()
                .addFirst(new MapPropertySource("runDirectory", Map.of("app.run-dir", runDir.toString())));
    }
}
