/*
 * Copyright 2016-2017 Axioma srl.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jpa.spring.boot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.holonplatform.jpa.spring.boot.JpaDatastoreHealthIndicator;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;

/**
 * Verifies that {@link JpaDatastoreHealthAutoConfiguration} registers a working
 * {@link JpaDatastoreHealthIndicator} when {@code spring-boot-actuator} is on the classpath.
 */
@SpringBootTest
@ActiveProfiles("standard")
@DirtiesContext
class TestHealthIndicator {

	@Configuration
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = TestJpaDomain1.class)
	protected static class Config {}

	@Autowired
	private JpaDatastoreHealthIndicator healthIndicator;

	@Test
	void healthIndicatorIsRegistered() {
		assertNotNull(healthIndicator, "JpaDatastoreHealthIndicator bean must be auto-configured");
	}

	@Test
	void healthIsUp() {
		Health health = healthIndicator.health();
		assertEquals(Status.UP, health.getStatus(),
				"Health status must be UP when the database is reachable");
	}

	@Test
	void healthDetailsContainDataContextId() {
		Health health = healthIndicator.health();
		Object dataContextId = health.getDetails().get("dataContextId");
		assertNotNull(dataContextId, "Health details must include 'dataContextId'");
		assertEquals("default", dataContextId,
				"Single-datasource auto-config must report dataContextId='default'");
	}

}
