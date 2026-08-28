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
package com.holonplatform.datastore.jpa.test.observation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.holonplatform.datastore.jpa.observation.DefaultJpaDatastoreObservationRegistry;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationEvent;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationListener;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationNames;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationRegistry;

/**
 * Unit tests for {@link DefaultJpaDatastoreObservationRegistry}.
 *
 * @since 10.0.0
 */
@DisplayName("JpaDatastoreObservationRegistry Tests")
public class TestJpaDatastoreObservationRegistry {

	private DefaultJpaDatastoreObservationRegistry registry;
	private TestListener listener;

	@BeforeEach
	public void setup() {
		registry = new DefaultJpaDatastoreObservationRegistry();
		listener = new TestListener();
		registry.registerListener(listener);
	}

	@Test
	@DisplayName("Should emit start event")
	public void testOnStart() {
		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.QUERY, "TestEntity");
		registry.onStart(event);

		assertEquals(1, listener.startedEvents.size());
		assertEquals("TestEntity", listener.startedEvents.get(0).getTargetEntity());
	}

	@Test
	@DisplayName("Should emit complete event with duration")
	public void testOnComplete() throws InterruptedException {
		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.SAVE, "TestEntity");
		registry.onStart(event);
		Thread.sleep(10);
		registry.onComplete(event);

		assertEquals(1, listener.completedEvents.size());
		JpaDatastoreObservationEvent completed = listener.completedEvents.get(0);
		assertTrue(completed.isCompleted());
		assertTrue(completed.getDurationMillis() >= 10);
	}

	@Test
	@DisplayName("Should emit error event")
	public void testOnError() {
		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.DELETE, "TestEntity");
		RuntimeException exception = new RuntimeException("Test error");

		registry.onStart(event);
		registry.onError(event, exception);

		assertEquals(1, listener.errorEvents.size());
		assertEquals(exception, listener.errorEvents.get(0).exception);
	}

	@Test
	@DisplayName("Should support custom attributes")
	public void testAttributes() {
		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.UPDATE, "TestEntity");
		event.setAttribute("operation_type", "bulk_update");
		event.setAttribute("record_count", 100);

		registry.onStart(event);

		JpaDatastoreObservationEvent observed = listener.startedEvents.get(0);
		assertEquals("bulk_update", observed.getAttribute("operation_type"));
		assertEquals(100, observed.getAttribute("record_count"));
	}

	@Test
	@DisplayName("Should observe operation with result")
	public void testObserveWithResult() throws Exception {
		String result = registry.observe(JpaDatastoreObservationNames.QUERY, null,
				() -> "test_result");

		assertEquals("test_result", result);
		assertEquals(1, listener.startedEvents.size());
		assertEquals(1, listener.completedEvents.size());
		assertEquals(0, listener.errorEvents.size());
	}

	@Test
	@DisplayName("Should observe operation with exception")
	public void testObserveWithException() {
		RuntimeException testException = new RuntimeException("Operation failed");

		assertThrows(RuntimeException.class, () -> {
			registry.observe(JpaDatastoreObservationNames.DELETE, null,
					() -> {
						throw testException;
					});
		});

		assertEquals(1, listener.startedEvents.size());
		assertEquals(0, listener.completedEvents.size());
		assertEquals(1, listener.errorEvents.size());
		assertEquals(testException, listener.errorEvents.get(0).exception);
	}

	@Test
	@DisplayName("Should observe void operation")
	public void testObserveVoid() throws Exception {
		List<String> trace = new ArrayList<>();
		registry.observeVoid(JpaDatastoreObservationNames.TRANSACTION, null,
				() -> trace.add("executed"));

		assertEquals(1, trace.size());
		assertEquals(1, listener.startedEvents.size());
		assertEquals(1, listener.completedEvents.size());
	}

	@Test
	@DisplayName("Should handle multiple listeners")
	public void testMultipleListeners() {
		TestListener listener2 = new TestListener();
		registry.registerListener(listener2);

		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.QUERY, "TestEntity");
		registry.onStart(event);

		assertEquals(1, listener.startedEvents.size());
		assertEquals(1, listener2.startedEvents.size());
	}

	@Test
	@DisplayName("Should remove listeners")
	public void testUnregisterListener() {
		registry.unregisterListener(listener);

		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(
				JpaDatastoreObservationNames.QUERY, "TestEntity");
		registry.onStart(event);

		assertEquals(0, listener.startedEvents.size());
	}

	/**
	 * Test listener implementation
	 */
	private static final class TestListener implements JpaDatastoreObservationListener {
		List<JpaDatastoreObservationEvent> startedEvents = new ArrayList<>();
		List<JpaDatastoreObservationEvent> completedEvents = new ArrayList<>();
		List<ErrorEvent> errorEvents = new ArrayList<>();

		@Override
		public void onStart(JpaDatastoreObservationEvent event) {
			startedEvents.add(event);
		}

		@Override
		public void onComplete(JpaDatastoreObservationEvent event) {
			completedEvents.add(event);
		}

		@Override
		public void onError(JpaDatastoreObservationEvent event, Throwable exception) {
			errorEvents.add(new ErrorEvent(event, exception));
		}

		private record ErrorEvent(JpaDatastoreObservationEvent event, Throwable exception) {
		}
	}

}
