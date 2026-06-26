package com.fluxo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"app.storage.s3.bucket=test-bucket",
		"app.storage.s3.region=us-east-2"
})
@ActiveProfiles("test") // <-- Adicione esta linha
class FluxoBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
