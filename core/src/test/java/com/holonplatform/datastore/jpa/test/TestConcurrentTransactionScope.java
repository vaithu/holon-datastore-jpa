/*
 * Copyright 2016-2025 Axioma srl.
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
package com.holonplatform.datastore.jpa.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.datastore.jpa.async.AggregatedTransactionException;
import com.holonplatform.datastore.jpa.async.ConcurrentTransactionScope;

/**
 * Tests for {@link ConcurrentTransactionScope}.
 */
@DisplayName("ConcurrentTransactionScope Tests")
class TestConcurrentTransactionScope {

	@Test
	@DisplayName("execute: successful transaction with all tasks completing")
	void testSuccessfulTransaction() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.degreeOfParallelism(4)
			.timeoutMs(10000)
			.build()) {

			scope.submit("task1", () -> counter.incrementAndGet());
			scope.submit("task2", () -> counter.incrementAndGet());
			scope.submit("task3", () -> counter.incrementAndGet());

			scope.join();
		}

		assertEquals(3, counter.get());
	}

	@Test
	@DisplayName("execute: all tasks fail")
	void testAllTasksFail() {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("task1", () -> {
				throw new RuntimeException("Error 1");
			});
			scope.submit("task2", () -> {
				throw new RuntimeException("Error 2");
			});
			scope.submit("task3", () -> {
				throw new RuntimeException("Error 3");
			});

			assertThrows(AggregatedTransactionException.class, scope::join);
		}
	}

	@Test
	@DisplayName("execute: partial failure")
	void testPartialFailure() {
		AtomicInteger successCount = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("success1", () -> successCount.incrementAndGet());
			scope.submit("fail1", () -> {
				throw new RuntimeException("Failure 1");
			});
			scope.submit("success2", () -> successCount.incrementAndGet());
			scope.submit("fail2", () -> {
				throw new RuntimeException("Failure 2");
			});

			AggregatedTransactionException e = assertThrows(
				AggregatedTransactionException.class,
				scope::join
			);

			assertEquals(2, e.getFailureCount());
			assertEquals(4, e.getTotalTasks());
			assertFalse(e.allFailed());
			assertTrue(e.partialFailure());
			assertEquals(2, e.getFailedTasks().size());
		}

		assertEquals(2, successCount.get());
	}

	@Test
	@DisplayName("execute: custom parallelism (high)")
	void testHighParallelism() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.degreeOfParallelism(16)  // High parallelism
			.build()) {

			for (int i = 0; i < 32; i++) {
				scope.submit("task-" + i, () -> counter.incrementAndGet());
			}

			scope.join();
		}

		assertEquals(32, counter.get());
	}

	@Test
	@DisplayName("execute: custom parallelism (low)")
	void testLowParallelism() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.degreeOfParallelism(1)  // Sequential execution
			.build()) {

			scope.submit("task1", () -> counter.incrementAndGet());
			scope.submit("task2", () -> counter.incrementAndGet());
			scope.submit("task3", () -> counter.incrementAndGet());

			scope.join();
		}

		assertEquals(3, counter.get());
	}

	@Test
	@DisplayName("execute: empty scope")
	void testEmptyScope() throws Exception {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {
			// No tasks submitted
			scope.join();  // Should complete without error
		}
	}

	@Test
	@DisplayName("execute: single task")
	void testSingleTask() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("single-task", () -> counter.incrementAndGet());

			scope.join();
		}

		assertEquals(1, counter.get());
	}

	@Test
	@DisplayName("builder: validation of invalid parameters")
	void testBuilderValidation() {
		ConcurrentTransactionScope.Builder builder = ConcurrentTransactionScope.builder();

		try {
			builder.degreeOfParallelism(0).build();
			assertTrue(false, "Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("degreeOfParallelism"));
		}

		try {
			builder = ConcurrentTransactionScope.builder();
			builder.timeoutMs(0).build();
			assertTrue(false, "Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("timeoutMs"));
		}
	}

	@Test
	@DisplayName("builder: default values")
	void testBuilderDefaults() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("task1", () -> counter.incrementAndGet());
			scope.submit("task2", () -> counter.incrementAndGet());

			scope.join();
		}

		assertEquals(2, counter.get());
	}

	@Test
	@DisplayName("exception: error details aggregation")
	void testErrorDetailsAggregation() {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("op1", () -> {
				throw new RuntimeException("Op1 failed");
			});
			scope.submit("op2", () -> {
				throw new RuntimeException("Op2 failed");
			});

			AggregatedTransactionException e = assertThrows(
				AggregatedTransactionException.class,
				scope::join
			);

			assertEquals(2, e.getFailureCount());
			assertTrue(e.getFailedTasks().containsKey("op1"));
			assertTrue(e.getFailedTasks().containsKey("op2"));
			assertTrue(e.getFailedTasks().get("op1").getMessage().contains("Op1 failed"));
			assertTrue(e.getFailedTasks().get("op2").getMessage().contains("Op2 failed"));
		}
	}

	@Test
	@DisplayName("close: idempotent and prevents new submissions")
	void testCloseIdempotent() throws Exception {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			AtomicInteger counter = new AtomicInteger(0);
			scope.submit("task", () -> counter.incrementAndGet());
			scope.join();

			scope.close();  // Explicit close
			scope.close();  // Second close should be idempotent

			// After close, should not allow new submissions
			assertThrows(IllegalStateException.class, () -> {
				scope.submit("new-task", () -> counter.incrementAndGet());
			});
		}
	}

	@Test
	@DisplayName("builder: custom timeout")
	void testCustomTimeout() throws Exception {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.timeoutMs(5000)  // 5 second timeout
			.build()) {

			scope.submit("quick-task", () -> {
				// This completes quickly
			});

			scope.join();
		}
	}

	@Test
	@DisplayName("execute: exception toString")
	void testExceptionToString() {
		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.build()) {

			scope.submit("fail", () -> {
				throw new RuntimeException("Test error");
			});

			AggregatedTransactionException e = assertThrows(
				AggregatedTransactionException.class,
				scope::join
			);

			String str = e.toString();
			assertTrue(str.contains("AggregatedTransactionException"));
			assertTrue(str.contains("failures=1"));
			assertTrue(str.contains("total=1"));
		}
	}

	@Test
	@DisplayName("execute: large number of tasks")
	void testLargeNumberOfTasks() throws Exception {
		AtomicInteger counter = new AtomicInteger(0);

		try (ConcurrentTransactionScope scope = ConcurrentTransactionScope.builder()
			.degreeOfParallelism(8)
			.build()) {

			for (int i = 0; i < 100; i++) {
				scope.submit("task-" + i, () -> counter.incrementAndGet());
			}

			scope.join();
		}

		assertEquals(100, counter.get());
	}
}
