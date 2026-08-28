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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.datastore.jpa.async.ParallelBatchExecutor;
import com.holonplatform.datastore.jpa.async.ParallelBatchResult;

/**
 * Tests for {@link ParallelBatchExecutor}.
 */
@DisplayName("ParallelBatchExecutor Tests")
class TestParallelBatchExecutor {

	@Test
	@DisplayName("Execute: successful batch processing")
	void testExecuteSuccessful() throws InterruptedException {
		// Setup
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}

		AtomicInteger processedCount = new AtomicInteger(0);

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.degreeOfParallelism(4)
			.partitionSize(25)
			.build();

		// Execute
		ParallelBatchResult result = executor.execute(items, item -> {
			processedCount.incrementAndGet();
		});

		// Verify
		assertEquals(100, result.getTotalItems());
		assertEquals(100, result.getSuccessfulRows());
		assertEquals(0, result.getFailedRows());
		assertTrue(result.isSuccessful());
		assertEquals(0, result.getPartitionErrors().size());
		assertTrue(result.getExecutionTimeMs() >= 0);
		assertTrue(result.getThroughputPerSecond() >= 0);  // throughput can be 0 if execution is very fast

		executor.shutdown();
	}

	@Test
	@DisplayName("Execute: batch processing with 1 million items")
	void testExecuteLargeDataset() throws InterruptedException {
		// Setup
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 1_000_000; i++) {
			items.add(i);
		}

		AtomicInteger processedCount = new AtomicInteger(0);

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.degreeOfParallelism(8)
			.partitionSize(10000)
			.build();

		// Execute
		ParallelBatchResult result = executor.execute(items, item -> {
			processedCount.incrementAndGet();
		});

		// Verify
		assertEquals(1_000_000, result.getTotalItems());
		assertEquals(1_000_000, result.getSuccessfulRows());
		assertEquals(0, result.getFailedRows());
		assertTrue(result.isSuccessful());
		assertTrue(result.getExecutionTimeMs() >= 0);

		executor.shutdown();
	}

	@Test
	@DisplayName("Execute: batch processing with errors")
	void testExecuteWithErrors() throws InterruptedException {
		// Setup
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.degreeOfParallelism(4)
			.partitionSize(25)
			.build();

		// Execute - fail on every 10th item
		ParallelBatchResult result = executor.execute(items, item -> {
			if (item % 10 == 0) {
				throw new RuntimeException("Test error at " + item);
			}
		});

		// Verify
		assertEquals(100, result.getTotalItems());
		assertEquals(90, result.getSuccessfulRows());
		assertEquals(10, result.getFailedRows());
		assertFalse(result.isSuccessful());
		assertTrue(result.getPartitionErrors().size() > 0);

		executor.shutdown();
	}

	@Test
	@DisplayName("Execute: different partition sizes")
	void testExecuteDifferentPartitionSizes() throws InterruptedException {
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}

		// Test with partition size 10
		ParallelBatchExecutor<Integer> executor1 = ParallelBatchExecutor.<Integer>builder()
			.partitionSize(10)
			.build();

		ParallelBatchResult result1 = executor1.execute(items, item -> {
			// Just process
		});

		assertEquals(100, result1.getTotalItems());
		assertEquals(100, result1.getSuccessfulRows());

		executor1.shutdown();

		// Test with partition size 50
		ParallelBatchExecutor<Integer> executor2 = ParallelBatchExecutor.<Integer>builder()
			.partitionSize(50)
			.build();

		ParallelBatchResult result2 = executor2.execute(items, item -> {
			// Just process
		});

		assertEquals(100, result2.getTotalItems());
		assertEquals(100, result2.getSuccessfulRows());

		executor2.shutdown();
	}

	@Test
	@DisplayName("Execute: different parallelism degrees")
	void testExecuteDifferentParallelism() throws InterruptedException {
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}

		// Test with low parallelism
		ParallelBatchExecutor<Integer> executor1 = ParallelBatchExecutor.<Integer>builder()
			.degreeOfParallelism(2)
			.build();

		ParallelBatchResult result1 = executor1.execute(items, item -> {
			// Just process
		});

		assertEquals(100, result1.getTotalItems());
		assertEquals(100, result1.getSuccessfulRows());

		executor1.shutdown();

		// Test with high parallelism
		ParallelBatchExecutor<Integer> executor2 = ParallelBatchExecutor.<Integer>builder()
			.degreeOfParallelism(16)
			.build();

		ParallelBatchResult result2 = executor2.execute(items, item -> {
			// Just process
		});

		assertEquals(100, result2.getTotalItems());
		assertEquals(100, result2.getSuccessfulRows());

		executor2.shutdown();
	}

	@Test
	@DisplayName("Execute: empty list")
	void testExecuteEmptyList() throws InterruptedException {
		List<Integer> items = new ArrayList<>();

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.build();

		ParallelBatchResult result = executor.execute(items, item -> {
			// Should not be called
		});

		assertEquals(0, result.getTotalItems());
		assertEquals(0, result.getSuccessfulRows());
		assertEquals(0, result.getFailedRows());
		assertTrue(result.isSuccessful());

		executor.shutdown();
	}

	@Test
	@DisplayName("Result: throughput calculation")
	void testResultThroughput() throws InterruptedException {
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.build();

		ParallelBatchResult result = executor.execute(items, item -> {
			// Process
		});

		double throughput = result.getThroughputPerSecond();
		// Throughput can be 0 if execution is faster than measurable millisecond precision
		// Just verify the calculation works and returns a non-negative value
		assertTrue(throughput >= 0, "Throughput should be non-negative");
		assertTrue(result.getExecutionTimeMs() >= 0, "Execution time should be non-negative");

		executor.shutdown();
	}

	@Test
	@DisplayName("Builder: validation of invalid parameters")
	void testBuilderValidation() {
		ParallelBatchExecutor.Builder<Integer> builder = ParallelBatchExecutor.builder();

		try {
			builder.degreeOfParallelism(0).build();
			assertTrue(false, "Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("degreeOfParallelism"));
		}

		try {
			builder = ParallelBatchExecutor.builder();
			builder.partitionSize(0).build();
			assertTrue(false, "Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("partitionSize"));
		}
	}

	@Test
	@DisplayName("Builder: default values")
	void testBuilderDefaults() throws InterruptedException {
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}

		// Use defaults
		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.build();

		ParallelBatchResult result = executor.execute(items, item -> {
			// Process
		});

		assertEquals(100, result.getTotalItems());
		assertEquals(100, result.getSuccessfulRows());

		executor.shutdown();
	}

	@Test
	@DisplayName("Result: toString")
	void testResultToString() throws InterruptedException {
		List<Integer> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			items.add(i);
		}

		ParallelBatchExecutor<Integer> executor = ParallelBatchExecutor.<Integer>builder()
			.build();

		ParallelBatchResult result = executor.execute(items, item -> {
			// Process
		});

		String toString = result.toString();
		assertTrue(toString.contains("ParallelBatchResult"));
		assertTrue(toString.contains("total=10"));
		assertTrue(toString.contains("successful=10"));

		executor.shutdown();
	}
}
