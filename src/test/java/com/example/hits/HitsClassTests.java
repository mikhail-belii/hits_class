package com.example.hits;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
class HitsClassTests {

	@Test
	void contextLoads() {
	}

}
