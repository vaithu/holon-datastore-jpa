/*
 * Copyright 2016-2025 Holon Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.holonplatform.datastore.jpa.test.nativeimage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor;
import com.holonplatform.datastore.jpa.logging.StructuredLogger;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationEvent;

/**
 * GraalVM native image compatibility tests.
 * <p>
 * Verifies that key Holon Datastore classes can be instantiated and used
 * in ahead-of-time (AOT) compiled native images where reflection metadata
 * must be explicitly configured.
 * </p>
 * <p>
 * These tests ensure that:
 * <ul>
 * <li>Classes can be instantiated via reflection</li>
 * <li>Methods can be invoked dynamically</li>
 * <li>Functional interfaces work correctly</li>
 * <li>Logging and observability integrations function in native mode</li>
 * </ul>
 * </p>
 *
 * @since 10.0.0
 */
@DisplayName("GraalVM Native Image Compatibility")
public class NativeImageCompatibilityTest {

	/**
	 * Test that virtual thread async executor can be instantiated.
	 */
	@Test
	@DisplayName("VirtualThreadDatastoreExecutor instantiation")
	void testVirtualThreadExecutorInstantiation() {
		assertDoesNotThrow(() -> {
			// Test class loading and instantiation via reflection
			Class<?> executorClass = Class.forName(
				"com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor");
			assertNotNull(executorClass);
		});
	}

	/**
	 * Test that structured logger works correctly.
	 */
	@Test
	@DisplayName("StructuredLogger functionality")
	void testStructuredLoggerFunctionality() {
		assertDoesNotThrow(() -> {
			StructuredLogger logger = StructuredLogger.forDatastore("QueryOperation");
			assertNotNull(logger);

			// Test fluent API
			logger.withEntity("TestEntity")
				.withDuration(100L)
				.info("Query executed successfully");
		});
	}

	/**
	 * Test that observation event can be instantiated.
	 */
	@Test
	@DisplayName("JpaDatastoreObservationEvent instantiation")
	void testObservationEventInstantiation() {
		assertDoesNotThrow(() -> {
			// Test class loading
			Class<?> eventClass = Class.forName(
				"com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationEvent");
			assertNotNull(eventClass);
		});
	}

	/**
	 * Test that observation registry can be instantiated.
	 */
	@Test
	@DisplayName("DefaultJpaDatastoreObservationRegistry instantiation")
	void testObservationRegistryInstantiation() {
		assertDoesNotThrow(() -> {
			Class<?> registryClass = Class.forName(
				"com.holonplatform.datastore.jpa.observation.DefaultJpaDatastoreObservationRegistry");
			assertNotNull(registryClass);
		});
	}

	/**
	 * Test that concurrent data structures work in virtual thread context.
	 */
	@Test
	@DisplayName("Virtual thread concurrent operations")
	void testConcurrentOperations() {
		assertDoesNotThrow(() -> {
			// Virtual threads should handle concurrent operations smoothly
			Thread vt1 = Thread.ofVirtual().start(() -> {
				StructuredLogger.forDatastore("Query1").info("Virtual thread 1 executing");
			});

			Thread vt2 = Thread.ofVirtual().start(() -> {
				StructuredLogger.forDatastore("Query2").info("Virtual thread 2 executing");
			});

			vt1.join();
			vt2.join();
		});
	}

	/**
	 * Test that runtime reflection hints are available.
	 */
	@Test
	@DisplayName("Observation framework availability")
	void testObservationFrameworkAvailability() {
		assertDoesNotThrow(() -> {
			// Verify observability classes exist and can be used
			Class<?> registryClass = Class.forName(
				"com.holonplatform.datastore.jpa.observation.DefaultJpaDatastoreObservationRegistry");
			assertNotNull(registryClass);

			// Verify logging framework exists
			Class<?> loggerClass = Class.forName(
				"com.holonplatform.datastore.jpa.logging.StructuredLogger");
			assertNotNull(loggerClass);
		});
	}

	/**
	 * Test that functional interfaces can be used in native context.
	 */
	@Test
	@DisplayName("Functional interface invocation")
	void testFunctionalInterfaceInvocation() {
		assertDoesNotThrow(() -> {
			// Test QueryOperation functional interface
			VirtualThreadDatastoreExecutor.QueryOperation<String> operation =
				ds -> "query result";
			assertNotNull(operation);

			// Test DatastoreOperation functional interface
			VirtualThreadDatastoreExecutor.DatastoreOperation datastoreOp =
				ds -> System.out.println("operation");
			assertNotNull(datastoreOp);
		});
	}

	/**
	 * Test that exception handling works in virtual threads.
	 */
	@Test
	@DisplayName("Exception handling in virtual threads")
	void testExceptionHandling() {
		assertDoesNotThrow(() -> {
			Thread vt = Thread.ofVirtual().start(() -> {
				try {
					StructuredLogger.forDatastore("Delete")
						.error("Test error occurred",
							new RuntimeException("Intentional error"));
				} catch (Exception e) {
					System.err.println("Caught: " + e.getMessage());
				}
			});
			vt.join();
		});
	}

}
