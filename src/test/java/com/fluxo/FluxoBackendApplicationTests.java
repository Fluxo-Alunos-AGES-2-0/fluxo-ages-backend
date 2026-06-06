package com.fluxo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.storage.s3.bucket=test-bucket",
		"app.storage.s3.region=us-east-2"
})
class FluxoBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
