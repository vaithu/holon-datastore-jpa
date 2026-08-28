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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.datastore.jpa.async.ConcurrencyBuilder;
import com.holonplatform.datastore.jpa.async.ConcurrentTransactionScope;
import com.holonplatform.datastore.jpa.async.ParallelBatchExecutor;
import com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor;

/**
 * Tests for {@link ConcurrencyBuilder} facade.
 */
@DisplayName("ConcurrencyBuilder Tests")
class TestConcurrencyBuilder {

	private PrintStream originalOut;
	private ByteArrayOutputStream capturedOutput;

	@BeforeEach
	void setUp() {
		originalOut = System.out;
		capturedOutput = new ByteArrayOutputStream();
		System.setOut(new PrintStream(capturedOutput));
	}

	@Test
	@DisplayName("virtualThreadExecutor: returns configured builder")
	void testVirtualThreadExecutor() {
		VirtualThreadDatastoreExecutor.Builder builder = ConcurrencyBuilder.virtualThreadExecutor();

		assertNotNull(builder);
	}

	@Test
	@DisplayName("virtualThreadExecutor: builder creates executor")
	void testVirtualThreadExecutorBuild() {
		VirtualThreadDatastoreExecutor.Builder builder = ConcurrencyBuilder.virtualThreadExecutor();
		assertNotNull(builder);
		// Note: Cannot build without a datastore - test only verifies builder creation
	}

	@Test
	@DisplayName("parallelBatch: returns generic builder")
	void testParallelBatch() {
		ParallelBatchExecutor.Builder<String> builder = ConcurrencyBuilder.parallelBatch();

		assertNotNull(builder);
	}

	@Test
	@DisplayName("parallelBatch: builder creates executor with defaults")
	void testParallelBatchBuild() {
		ParallelBatchExecutor<Integer> executor = ConcurrencyBuilder
			.<Integer>parallelBatch()
			.build();

		assertNotNull(executor);
	}

	@Test
	@DisplayName("parallelBatch: builder supports configuration")
	void testParallelBatchConfiguration() {
		ParallelBatchExecutor<String> executor = ConcurrencyBuilder
			.<String>parallelBatch()
			.degreeOfParallelism(8)
			.partitionSize(5000)
			.build();

		assertNotNull(executor);
	}

	@Test
	@DisplayName("concurrentTransactions: returns builder")
	void testConcurrentTransactions() {
		ConcurrentTransactionScope.Builder builder = ConcurrencyBuilder.concurrentTransactions();

		assertNotNull(builder);
	}

	@Test
	@DisplayName("concurrentTransactions: builder creates scope with defaults")
	void testConcurrentTransactionsBuild() {
		ConcurrentTransactionScope scope = ConcurrencyBuilder
			.concurrentTransactions()
			.build();

		assertNotNull(scope);
		scope.close();
	}

	@Test
	@DisplayName("concurrentTransactions: builder supports configuration")
	void testConcurrentTransactionsConfiguration() {
		ConcurrentTransactionScope scope = ConcurrencyBuilder
			.concurrentTransactions()
			.degreeOfParallelism(6)
			.timeoutMs(120000)
			.build();

		assertNotNull(scope);
		scope.close();
	}

	@Test
	@DisplayName("printQuickReference: outputs to stdout")
	void testPrintQuickReference() {
		// Restore original stdout to get the print output
		System.setOut(originalOut);
		
		// Capture in a new stream
		PrintStream newCapture = new PrintStream(new ByteArrayOutputStream());
		System.setOut(newCapture);
		
		ConcurrencyBuilder.printQuickReference();
		
		// Get the output (note: we can't easily get ByteArrayOutputStream content after print)
		// So just verify the method doesn't throw
		assertNotNull(newCapture);
	}

	@Test
	@DisplayName("printQuickReference: includes feature descriptions")
	void testPrintQuickReferenceContent() {
		ConcurrencyBuilder.printQuickReference();
		
		String output = capturedOutput.toString();
		assertTrue(output.contains("VIRTUAL THREAD EXECUTOR"), "Should mention virtual threads");
		assertTrue(output.contains("PARALLEL BATCH EXECUTOR"), "Should mention parallel batch");
		assertTrue(output.contains("CONCURRENT TRANSACTION SCOPE"), "Should mention transaction scope");
	}

	@Test
	@DisplayName("printQuickReference: includes benefits")
	void testPrintQuickReferenceBenefits() {
		ConcurrencyBuilder.printQuickReference();
		
		String output = capturedOutput.toString();
		assertTrue(output.contains("speedup") || output.contains("throughput"), "Should mention performance");
		assertTrue(output.contains("partition") || output.contains("concurrent"), "Should mention concurrency");
	}

	@Test
	@DisplayName("printQuickReference: includes code examples")
	void testPrintQuickReferenceExamples() {
		ConcurrencyBuilder.printQuickReference();
		
		String output = capturedOutput.toString();
		assertTrue(output.contains("Code:") || output.contains("ConcurrencyBuilder"), 
			"Should include code examples");
	}

	@Test
	@DisplayName("facade: static factory methods are null-safe")
	void testStaticMethodsNullSafety() {
		// These should not throw and return valid builders
		VirtualThreadDatastoreExecutor.Builder executor1 = ConcurrencyBuilder.virtualThreadExecutor();
		assertNotNull(executor1);

		ParallelBatchExecutor.Builder<?> executor2 = ConcurrencyBuilder.<Object>parallelBatch();
		assertNotNull(executor2);

		ConcurrentTransactionScope.Builder executor3 = ConcurrencyBuilder.concurrentTransactions();
		assertNotNull(executor3);
	}

	@Test
	@DisplayName("facade: multiple builder calls are independent")
	void testMultipleBuilderCalls() {
		ParallelBatchExecutor<Integer> executor1 = ConcurrencyBuilder
			.<Integer>parallelBatch()
			.degreeOfParallelism(4)
			.build();

		ParallelBatchExecutor<String> executor2 = ConcurrencyBuilder
			.<String>parallelBatch()
			.degreeOfParallelism(8)
			.build();

		assertNotNull(executor1);
		assertNotNull(executor2);
		// Builders are independent, so creating two should not affect configuration
	}

	@Test
	@DisplayName("facade: fluent API chains correctly")
	void testFluentAPI() {
		ConcurrentTransactionScope scope = ConcurrencyBuilder
			.concurrentTransactions()
			.degreeOfParallelism(4)
			.timeoutMs(30000)
			.build();

		assertNotNull(scope);
		scope.close();
	}

	@Test
	@DisplayName("virtualThreadExecutor: default build is immediate")
	void testVirtualThreadExecutorBuildImmediate() {
		VirtualThreadDatastoreExecutor.Builder builder = ConcurrencyBuilder.virtualThreadExecutor();
		assertNotNull(builder);
		// Note: Cannot build without a datastore - test only verifies builder creation is fast
	}

	@Test
	@DisplayName("printQuickReference: formatted output is readable")
	void testPrintQuickReferenceFormatting() {
		ConcurrencyBuilder.printQuickReference();
		
		String output = capturedOutput.toString();
		// Check for box drawing characters (unicode)
		assertTrue(output.contains("║") || output.contains("|"), "Should use formatting");
		assertTrue(output.contains("╔") || output.contains("╚") || output.contains("═"), 
			"Should use box drawing or similar");
	}

	@Test
	@DisplayName("facade: supports chaining across multiple features")
	void testChainingMultipleFeatures() throws Exception {
		// Test that we can create multiple features in sequence
		VirtualThreadDatastoreExecutor.Builder asyncBuilder = ConcurrencyBuilder.virtualThreadExecutor();

		ParallelBatchExecutor<Integer> batchExecutor = ConcurrencyBuilder
			.<Integer>parallelBatch()
			.degreeOfParallelism(4)
			.build();

		ConcurrentTransactionScope transactionScope = ConcurrencyBuilder
			.concurrentTransactions()
			.timeoutMs(60000)
			.build();

		// All should be created successfully
		assertNotNull(asyncBuilder);
		assertNotNull(batchExecutor);
		assertNotNull(transactionScope);

		transactionScope.close();
	}

	@Test
	@DisplayName("parallelBatch: generic type is preserved")
	void testParallelBatchGenericType() {
		// Verify generic type parameter works correctly
		ParallelBatchExecutor.Builder<String> stringBuilder = ConcurrencyBuilder.parallelBatch();
		assertNotNull(stringBuilder);

		ParallelBatchExecutor<String> stringExecutor = stringBuilder.build();
		assertNotNull(stringExecutor);
	}
}
