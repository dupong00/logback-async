package io.github.dupong00.logbackasync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 결과 경로는 build.gradle의 test 작업이 실행 전에 비운다
@SpringBootTest(properties = { "app.run-id=9999", "app.runs-dir=build/test-runs" })
@ActiveProfiles("baseline")
class LogbackAsyncApplicationTests {

	@Test
	void contextLoads() {
	}

}
