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
package com.holonplatform.datastore.jpa.test.fixtures;

/**
 * Test fixtures for JPA Datastore testing.
 * <p>
 * Uses Java records for clean, immutable test data structures.
 * Integrates with JUnit 6 parametrized tests for data-driven testing.
 * </p>
 *
 * @since 10.0.0
 */
public final class TestFixtures {

	private TestFixtures() {
	}

	/**
	 * Query test fixture.
	 *
	 * @param name         Test name
	 * @param targetEntity Target entity name
	 * @param filter       Optional filter expression
	 * @param expected     Expected result count
	 */
	public record QueryFixture(
		String name,
		String targetEntity,
		String filter,
		int expected
	) {
		public QueryFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Test name cannot be blank");
			}
			if (targetEntity == null || targetEntity.isBlank()) {
				throw new IllegalArgumentException("Target entity cannot be blank");
			}
			if (expected < 0) {
				throw new IllegalArgumentException("Expected count cannot be negative");
			}
		}

		@Override
		public String toString() {
			return name + " (" + targetEntity + ")";
		}
	}

	/**
	 * Insert operation test fixture.
	 *
	 * @param name        Test name
	 * @param entity      Entity name
	 * @param fieldCount  Number of fields to insert
	 * @param shouldFail  Whether the operation should fail
	 */
	public record InsertFixture(
		String name,
		String entity,
		int fieldCount,
		boolean shouldFail
	) {
		public InsertFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Test name cannot be blank");
			}
			if (entity == null || entity.isBlank()) {
				throw new IllegalArgumentException("Entity cannot be blank");
			}
			if (fieldCount < 0) {
				throw new IllegalArgumentException("Field count cannot be negative");
			}
		}

		@Override
		public String toString() {
			return name + " (" + entity + ", fields=" + fieldCount + ")";
		}
	}

	/**
	 * Update operation test fixture.
	 *
	 * @param name       Test name
	 * @param entity     Entity name
	 * @param filter     Filter expression
	 * @param updateSize Number of records to update
	 * @param shouldFail Whether the operation should fail
	 */
	public record UpdateFixture(
		String name,
		String entity,
		String filter,
		long updateSize,
		boolean shouldFail
	) {
		public UpdateFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Test name cannot be blank");
			}
			if (entity == null || entity.isBlank()) {
				throw new IllegalArgumentException("Entity cannot be blank");
			}
			if (updateSize < 0) {
				throw new IllegalArgumentException("Update size cannot be negative");
			}
		}

		@Override
		public String toString() {
			return name + " (" + entity + ", updating=" + updateSize + ")";
		}
	}

	/**
	 * Delete operation test fixture.
	 *
	 * @param name       Test name
	 * @param entity     Entity name
	 * @param filter     Filter expression
	 * @param deleteSize Number of records to delete
	 * @param shouldFail Whether the operation should fail
	 */
	public record DeleteFixture(
		String name,
		String entity,
		String filter,
		long deleteSize,
		boolean shouldFail
	) {
		public DeleteFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Test name cannot be blank");
			}
			if (entity == null || entity.isBlank()) {
				throw new IllegalArgumentException("Entity cannot be blank");
			}
			if (deleteSize < 0) {
				throw new IllegalArgumentException("Delete size cannot be negative");
			}
		}

		@Override
		public String toString() {
			return name + " (" + entity + ", deleting=" + deleteSize + ")";
		}
	}

	/**
	 * Transaction test fixture.
	 *
	 * @param name       Test name
	 * @param operations Number of operations in transaction
	 * @param shouldFail Whether the transaction should fail
	 * @param rollback   Whether to rollback after failure
	 */
	public record TransactionFixture(
		String name,
		int operations,
		boolean shouldFail,
		boolean rollback
	) {
		public TransactionFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Test name cannot be blank");
			}
			if (operations < 1) {
				throw new IllegalArgumentException("Operations must be at least 1");
			}
		}

		@Override
		public String toString() {
			return name + " (" + operations + " operations, shouldFail=" + shouldFail + ")";
		}
	}

	/**
	 * Performance benchmark fixture.
	 *
	 * @param name           Benchmark name
	 * @param operationType  Type of operation (Query, Insert, Update, Delete)
	 * @param recordCount    Number of records to process
	 * @param maxDurationMs  Maximum acceptable duration in ms
	 */
	public record PerformanceFixture(
		String name,
		String operationType,
		long recordCount,
		long maxDurationMs
	) {
		public PerformanceFixture {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Name cannot be blank");
			}
			if (operationType == null || operationType.isBlank()) {
				throw new IllegalArgumentException("Operation type cannot be blank");
			}
			if (recordCount < 1) {
				throw new IllegalArgumentException("Record count must be at least 1");
			}
			if (maxDurationMs < 1) {
				throw new IllegalArgumentException("Max duration must be at least 1ms");
			}
		}

		@Override
		public String toString() {
			return name + " (" + operationType + ", records=" + recordCount + ")";
		}
	}

	/**
	 * Sealed result type for test outcomes.
	 */
	public sealed interface TestResult {
		record Success(String message) implements TestResult {
			@Override
			public String toString() {
				return "✓ " + message;
			}
		}

		record Failure(String message, Throwable cause) implements TestResult {
			@Override
			public String toString() {
				return "✗ " + message + " (" + cause.getClass().getSimpleName() + ")";
			}
		}

		record Skipped(String reason) implements TestResult {
			@Override
			public String toString() {
				return "⊘ " + reason;
			}
		}
	}

}
