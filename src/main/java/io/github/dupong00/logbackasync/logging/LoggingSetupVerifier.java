package io.github.dupong00.logbackasync.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.github.dupong00.logbackasync.config.StartupValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 기동 시 실제 Logback 객체를 읽어 프로필별 기대값과 비교한다.
 * Logback 1.5.38은 속성 오타나 빈 값을 WARN으로만 남기고 기본값으로 뜨므로 여기서 기동을 멈춘다.
 * 기대값을 yml에서 읽지 않고 여기에 따로 적는다. yml 쪽 실수도 잡기 위해서다.
 */
@Component
@RequiredArgsConstructor
public class LoggingSetupVerifier implements InitializingBean {

    private static final org.slf4j.Logger adminLog = LoggerFactory.getLogger("ADMIN_LOG");

    // discardingThreshold는 start() 뒤의 값이다. -1(미설정)이면 queueSize / 5
    private static final Map<String, AsyncExpected> ASYNC_EXPECTED = Map.of(
            "baseline", new AsyncExpected(256, 51, false),
            "blocking", new AsyncExpected(256, 0, false),
            "dropping", new AsyncExpected(256, 0, true));

    private final Environment environment;

    @Override
    public void afterPropertiesSet() {
        String profile = Arrays.stream(environment.getActiveProfiles())
                .filter(StartupValidator.POLICY_PROFILES::contains)
                .findFirst()
                .orElseThrow();
        Path expectedFile = Path.of(environment.getRequiredProperty("app.run-dir"), "events.log");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger eventLog = context.getLogger("EVENT_LOG");
        List<String> problems = new ArrayList<>();

        if (eventLog.isAdditive()) {
            problems.add("EVENT_LOG의 additivity가 true다");
        }
        if (!eventLog.isInfoEnabled()) {
            problems.add("EVENT_LOG가 INFO를 기록하지 않는다");
        }

        List<Appender<ILoggingEvent>> attached = toList(eventLog.iteratorForAppenders());
        if (attached.size() != 1) {
            problems.add("EVENT_LOG에는 출력기가 하나만 있어야 한다. 현재: " + attached);
            fail(profile, problems);
        }

        String summary;
        if (profile.equals("sync")) {
            checkFile(attached.get(0), expectedFile, problems);
            summary = "EVENT_LOG -> DelayedFileAppender";
        } else {
            if (!(attached.get(0) instanceof AsyncAppender async)) {
                problems.add("EVENT_LOG에는 AsyncAppender가 붙어 있어야 한다. 현재: " + attached.get(0));
                fail(profile, problems);
                return;
            }
            AsyncExpected expected = ASYNC_EXPECTED.get(profile);
            if (!async.isStarted()) {
                problems.add("AsyncAppender가 시작되지 않았다");
            }
            if (async.getQueueSize() != expected.queueSize()) {
                problems.add("queueSize " + async.getQueueSize() + " (기대 " + expected.queueSize() + ")");
            }
            if (async.getDiscardingThreshold() != expected.discardingThreshold()) {
                problems.add("discardingThreshold " + async.getDiscardingThreshold() + " (기대 " + expected.discardingThreshold() + ")");
            }
            if (async.isNeverBlock() != expected.neverBlock()) {
                problems.add("neverBlock " + async.isNeverBlock() + " (기대 " + expected.neverBlock() + ")");
            }
            List<Appender<ILoggingEvent>> outputs = toList(async.iteratorForAppenders());
            if (outputs.size() != 1) {
                problems.add("AsyncAppender 아래에는 출력기가 하나만 있어야 한다. 현재: " + outputs);
            } else {
                checkFile(outputs.get(0), expectedFile, problems);
            }
            summary = "EVENT_LOG -> AsyncAppender(queueSize=%d, discardingThreshold=%d, neverBlock=%s) -> DelayedFileAppender"
                    .formatted(async.getQueueSize(), async.getDiscardingThreshold(), async.isNeverBlock());
        }

        fail(profile, problems);
        adminLog.info("logging verified profile={} {} file={}", profile, summary, expectedFile);
    }

    private static void checkFile(Appender<ILoggingEvent> appender, Path expectedFile, List<String> problems) {
        if (!(appender instanceof DelayedFileAppender file)) {
            problems.add("이벤트 출력기는 DelayedFileAppender여야 한다. 현재: " + appender);
            return;
        }
        if (!file.isStarted()) {
            problems.add("DelayedFileAppender가 시작되지 않았다");
        }
        if (!expectedFile.equals(Path.of(file.getFile()))) {
            problems.add("이벤트 로그 파일 " + file.getFile() + " (기대 " + expectedFile + ")");
        }
    }

    private static void fail(String profile, List<String> problems) {
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Logback 설정이 " + profile + " 프로필 기대값과 다르다: " + problems);
        }
    }

    private static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    private record AsyncExpected(int queueSize, int discardingThreshold, boolean neverBlock) {
    }
}
