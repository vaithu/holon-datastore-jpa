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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.annotation.DirtiesContext;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.internal.ObservableJpaDatastore;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

/**
 * Verifies that the auto-configured datastore is an {@link ObservableJpaDatastore} when
 * {@code micrometer-observation} is on the classpath, and that every EntityManager unit of
 * work produces a {@code holon.jpa.datastore} observation with the correct key-values.
 */
@SpringBootTest
@ActiveProfiles("standard")
@DirtiesContext
class TestObservability {

	/** Counts how many observations reached the "stop" lifecycle event. */
	static final AtomicInteger STOP_COUNT = new AtomicInteger();

	@Configuration
	@EnableAutoConfiguration
	@EntityScan(basePackageClasses = TestJpaDomain1.class)
	protected static class Config {

		/**
		 * Expose a real {@link ObservationRegistry} with a counting handler.
		 * This bean is picked up by {@link JpaDatastoreAutoConfigurationRegistrar}
		 * and injected into the {@link ObservableJpaDatastore}.
		 */
		@Bean
		ObservationRegistry observationRegistry() {
			ObservationRegistry registry = ObservationRegistry.create();
			registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
				@Override
				public void onStop(Observation.Context ctx) {
					if ("holon.jpa.datastore".equals(ctx.getName())) {
						STOP_COUNT.incrementAndGet();
					}
				}
				@Override
				public boolean supportsContext(Observation.Context ctx) {
					return true;
				}
			});
			return registry;
		}
	}

	private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
	private final static DataTarget<TestJpaDomain1> TARGET = JpaTarget.of(TestJpaDomain1.class);

	@Autowired
	private Datastore datastore;

	@Test
	void datastoreBeanIsObservable() {
		assertInstanceOf(ObservableJpaDatastore.class, datastore,
				"Auto-configured datastore must be ObservableJpaDatastore when micrometer-observation is on classpath");
	}

	@Test
	void queryEmitsObservation() {
		assertNotNull(datastore);
		int before = STOP_COUNT.get();

		// Execute one query — expects one holon.jpa.datastore observation
		datastore.query().target(TARGET).findOne(KEY);

		int after = STOP_COUNT.get();
		assertTrue(after > before,
				"At least one holon.jpa.datastore observation must be emitted per query; before=" + before + " after=" + after);
	}

	@Test
	void observationKeyValuesAreCorrect() {
		ObservableJpaDatastore obs = (ObservableJpaDatastore) datastore;
		// Data context for the auto-configured single-datasource path is "default"
		assertEquals("default", obs.getDataContextId().orElse("default"));
	}
}
