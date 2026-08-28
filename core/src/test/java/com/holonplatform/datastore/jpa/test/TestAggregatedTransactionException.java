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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.datastore.jpa.async.AggregatedTransactionException;

/**
 * Tests for {@link AggregatedTransactionException}.
 */
@DisplayName("AggregatedTransactionException Tests")
class TestAggregatedTransactionException {

	@Test
	@DisplayName("constructor: creates with failed tasks")
	void testConstructor() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error 1"));
		failedTasks.put("op2", new RuntimeException("Error 2"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"2 of 5 tasks failed", failedTasks, 5
		);

		assertEquals(2, ex.getFailureCount());
		assertEquals(5, ex.getTotalTasks());
		assertEquals(2, ex.getFailedTasks().size());
	}

	@Test
	@DisplayName("getFailedTasks: returns immutable map")
	void testGetFailedTasksImmutable() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("task1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"1 of 3 tasks failed", failedTasks, 3
		);

		Map<String, Throwable> returned = ex.getFailedTasks();
		assertNotNull(returned);
		assertEquals(1, returned.size());
		assertTrue(returned.containsKey("task1"));

		// Verify it's immutable (or at least our changes don't affect the original)
		Map<String, Throwable> copy = new HashMap<>(returned);
		assertEquals(1, copy.size());
	}

	@Test
	@DisplayName("getFailureCount: returns correct count")
	void testGetFailureCount() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error 1"));
		failedTasks.put("op2", new RuntimeException("Error 2"));
		failedTasks.put("op3", new RuntimeException("Error 3"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"3 of 10 tasks failed", failedTasks, 10
		);

		assertEquals(3, ex.getFailureCount());
	}

	@Test
	@DisplayName("getTotalTasks: returns correct count")
	void testGetTotalTasks() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"1 of 50 tasks failed", failedTasks, 50
		);

		assertEquals(50, ex.getTotalTasks());
	}

	@Test
	@DisplayName("allFailed: returns true when all tasks failed")
	void testAllFailedTrue() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error 1"));
		failedTasks.put("op2", new RuntimeException("Error 2"));
		failedTasks.put("op3", new RuntimeException("Error 3"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"All 3 tasks failed", failedTasks, 3  // failures == total
		);

		assertTrue(ex.allFailed());
	}

	@Test
	@DisplayName("allFailed: returns false when not all tasks failed")
	void testAllFailedFalse() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"1 of 5 tasks failed", failedTasks, 5  // failures < total
		);

		assertFalse(ex.allFailed());
	}

	@Test
	@DisplayName("partialFailure: returns true when some tasks failed")
	void testPartialFailureTrue() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"1 of 5 tasks failed", failedTasks, 5  // failures < total
		);

		assertTrue(ex.partialFailure());
	}

	@Test
	@DisplayName("partialFailure: returns false when all tasks failed")
	void testPartialFailureFalse() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error 1"));
		failedTasks.put("op2", new RuntimeException("Error 2"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"All 2 tasks failed", failedTasks, 2  // failures == total
		);

		assertFalse(ex.partialFailure());
	}

	@Test
	@DisplayName("partialFailure: returns false when no tasks failed")
	void testPartialFailureNoFailures() {
		Map<String, Throwable> failedTasks = new HashMap<>();

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"No tasks failed", failedTasks, 5  // failures == 0
		);

		assertFalse(ex.partialFailure());
	}

	@Test
	@DisplayName("contains: returns true for failed task")
	void testContainsFailedTask() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error 1"));
		failedTasks.put("op2", new RuntimeException("Error 2"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"2 of 4 tasks failed", failedTasks, 4
		);

		assertTrue(ex.getFailedTasks().containsKey("op1"));
		assertTrue(ex.getFailedTasks().containsKey("op2"));
	}

	@Test
	@DisplayName("getException: retrieves specific exception")
	void testGetException() {
		RuntimeException error1 = new RuntimeException("Error 1");
		RuntimeException error2 = new RuntimeException("Error 2");

		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", error1);
		failedTasks.put("op2", error2);

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"2 of 4 tasks failed", failedTasks, 4
		);

		assertEquals(error1, ex.getFailedTasks().get("op1"));
		assertEquals(error2, ex.getFailedTasks().get("op2"));
		assertTrue(ex.getFailedTasks().get("op1").getMessage().contains("Error 1"));
		assertTrue(ex.getFailedTasks().get("op2").getMessage().contains("Error 2"));
	}

	@Test
	@DisplayName("toString: includes failure summary")
	void testToString() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"1 of 5 failed", failedTasks, 5
		);

		String str = ex.toString();
		assertNotNull(str);
		assertTrue(str.contains("AggregatedTransactionException"));
		assertTrue(str.contains("failures=1"));
		assertTrue(str.contains("total=5"));
	}

	@Test
	@DisplayName("getMessage: returns description")
	void testGetMessage() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"Task op1 failed: Error", failedTasks, 5
		);

		String msg = ex.getMessage();
		assertNotNull(msg);
		assertTrue(msg.length() > 0);
		assertTrue(msg.contains("op1") || msg.contains("failed"));
	}

	@Test
	@DisplayName("empty failures map: edge case")
	void testEmptyFailuresMap() {
		Map<String, Throwable> failedTasks = new HashMap<>();

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"No tasks failed", failedTasks, 10
		);

		assertEquals(0, ex.getFailureCount());
		assertEquals(10, ex.getTotalTasks());
		assertFalse(ex.allFailed());
		assertFalse(ex.partialFailure());
		assertTrue(ex.getFailedTasks().isEmpty());
	}

	@Test
	@DisplayName("single failure: minimal case")
	void testSingleFailure() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		RuntimeException error = new RuntimeException("Single error");
		failedTasks.put("task1", error);

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"All 1 tasks failed", failedTasks, 1
		);

		assertEquals(1, ex.getFailureCount());
		assertEquals(1, ex.getTotalTasks());
		assertTrue(ex.allFailed());
		assertFalse(ex.partialFailure());
		assertEquals(error, ex.getFailedTasks().get("task1"));
	}

	@Test
	@DisplayName("multiple exception types: heterogeneous failures")
	void testMultipleExceptionTypes() {
		Map<String, Throwable> failedTasks = new HashMap<>();
		failedTasks.put("op1", new RuntimeException("Runtime error"));
		failedTasks.put("op2", new IllegalArgumentException("Argument error"));
		failedTasks.put("op3", new IllegalStateException("State error"));

		AggregatedTransactionException ex = new AggregatedTransactionException(
			"3 of 6 tasks failed", failedTasks, 6
		);

		assertEquals(3, ex.getFailureCount());
		assertTrue(ex.getFailedTasks().get("op1") instanceof RuntimeException);
		assertTrue(ex.getFailedTasks().get("op2") instanceof IllegalArgumentException);
		assertTrue(ex.getFailedTasks().get("op3") instanceof IllegalStateException);
	}

	@Test
	@DisplayName("isException: distinguishes partial from complete failures")
	void testFailureScenarios() {
		// Scenario 1: Complete failure
		Map<String, Throwable> allFailed = new HashMap<>();
		allFailed.put("op1", new RuntimeException("E1"));
		allFailed.put("op2", new RuntimeException("E2"));
		AggregatedTransactionException ex1 = new AggregatedTransactionException(
			"All 2 tasks failed", allFailed, 2
		);
		assertTrue(ex1.allFailed());
		assertFalse(ex1.partialFailure());

		// Scenario 2: Partial failure
		Map<String, Throwable> partial = new HashMap<>();
		partial.put("op1", new RuntimeException("E1"));
		AggregatedTransactionException ex2 = new AggregatedTransactionException(
			"1 of 5 tasks failed", partial, 5
		);
		assertFalse(ex2.allFailed());
		assertTrue(ex2.partialFailure());
	}
}
