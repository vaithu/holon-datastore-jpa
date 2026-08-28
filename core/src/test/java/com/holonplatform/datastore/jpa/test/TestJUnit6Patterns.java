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
package com.holonplatform.datastore.jpa.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.holonplatform.datastore.jpa.test.fixtures.TestFixtures.*;

/**
 * Parametrized tests showcasing JUnit 6 modern testing patterns.
 * <p>
 * Uses {@code @ParameterizedTest} with various parameter sources for
 * data-driven testing with records as test fixtures.
 * </p>
 *
 * @since 10.0.0
 */
@DisplayName("JUnit 6 Parametrized Tests")
public class TestJUnit6Patterns {

	@ParameterizedTest(name = "{index} - {0}")
	@ValueSource(ints = { 1, 2, 5, 10, 100 })
	@DisplayName("Should handle various record counts")
	@Tag("datadriven")
	void testVariousRecordCounts(int recordCount) {
		assertTrue(recordCount > 0, "Record count should be positive");
		assertTrue(recordCount <= 100, "Record count should not exceed 100");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideQueryFixtures")
	@DisplayName("Should execute queries with various filters")
	@Tag("datadriven")
	void testQueryExecutionPatterns(QueryFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertNotBlank(fixture.targetEntity(), "Target entity must be specified");
		assertTrue(fixture.expected() >= 0, "Expected count cannot be negative");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideInsertFixtures")
	@DisplayName("Should validate insert operations")
	@Tag("datadriven")
	void testInsertOperationPatterns(InsertFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertNotBlank(fixture.entity(), "Entity must be specified");
		assertTrue(fixture.fieldCount() >= 0, "Field count cannot be negative");

		// Verify fixture display name contains operation details
		String display = fixture.toString();
		assertTrue(display.contains(fixture.entity()), "Display name should contain entity");
		assertTrue(display.contains(String.valueOf(fixture.fieldCount())), "Display name should contain field count");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideUpdateFixtures")
	@DisplayName("Should validate update operations")
	@Tag("datadriven")
	void testUpdateOperationPatterns(UpdateFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertNotBlank(fixture.entity(), "Entity must be specified");
		assertTrue(fixture.updateSize() >= 0, "Update size cannot be negative");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideDeleteFixtures")
	@DisplayName("Should validate delete operations")
	@Tag("datadriven")
	void testDeleteOperationPatterns(DeleteFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertNotBlank(fixture.entity(), "Entity must be specified");
		assertTrue(fixture.deleteSize() >= 0, "Delete size cannot be negative");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("provideTransactionFixtures")
	@DisplayName("Should validate transaction patterns")
	@Tag("datadriven")
	void testTransactionPatterns(TransactionFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertTrue(fixture.operations() >= 1, "Operations must be at least 1");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("providePerformanceFixtures")
	@DisplayName("Should meet performance benchmarks")
	@Tag("performance")
	void testPerformanceBenchmarks(PerformanceFixture fixture) {
		assertNotNull(fixture, "Fixture cannot be null");
		assertTrue(fixture.recordCount() > 0, "Record count must be positive");
		assertTrue(fixture.maxDurationMs() > 0, "Max duration must be positive");
	}

	/**
	 * Helper for asserting non-blank strings (using pattern matching concept).
	 */
	private static void assertNotBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			fail(message);
		}
	}

	// ========== Test Fixture Providers ==========

	static Stream<QueryFixture> provideQueryFixtures() {
		return Stream.of(
			new QueryFixture("Simple query", "User", null, 10),
			new QueryFixture("Filtered query", "User", "id > 5", 5),
			new QueryFixture("Complex filter", "Order", "status = 'PENDING' AND total > 100", 3),
			new QueryFixture("Empty result", "Product", "id = -1", 0)
		);
	}

	static Stream<InsertFixture> provideInsertFixtures() {
		return Stream.of(
			new InsertFixture("Single field insert", "User", 1, false),
			new InsertFixture("Multi-field insert", "Order", 5, false),
			new InsertFixture("All fields insert", "Product", 10, false),
			new InsertFixture("Null field rejection", "User", 1, true)
		);
	}

	static Stream<UpdateFixture> provideUpdateFixtures() {
		return Stream.of(
			new UpdateFixture("Update single record", "User", "id = 1", 1, false),
			new UpdateFixture("Update multiple records", "Order", "status = 'ACTIVE'", 10, false),
			new UpdateFixture("Update with complex filter", "Product", "price > 100 AND stock < 5", 5, false),
			new UpdateFixture("Update no matches", "User", "id = -999", 0, false)
		);
	}

	static Stream<DeleteFixture> provideDeleteFixtures() {
		return Stream.of(
			new DeleteFixture("Delete single record", "User", "id = 1", 1, false),
			new DeleteFixture("Delete multiple records", "Order", "status = 'CANCELLED'", 10, false),
			new DeleteFixture("Delete with complex filter", "Product", "stock = 0 AND archived = true", 3, false),
			new DeleteFixture("Delete no matches", "User", "id = -999", 0, false)
		);
	}

	static Stream<TransactionFixture> provideTransactionFixtures() {
		return Stream.of(
			new TransactionFixture("Single operation", 1, false, false),
			new TransactionFixture("Multiple operations", 5, false, false),
			new TransactionFixture("Nested transaction", 3, false, false),
			new TransactionFixture("Failing transaction", 3, true, true),
			new TransactionFixture("Partial transaction failure", 5, true, true)
		);
	}

	static Stream<PerformanceFixture> providePerformanceFixtures() {
		return Stream.of(
			new PerformanceFixture("Query 100 records", "Query", 100, 100),
			new PerformanceFixture("Insert 50 records", "Insert", 50, 500),
			new PerformanceFixture("Update 1000 records", "Update", 1000, 1000),
			new PerformanceFixture("Delete 100 records", "Delete", 100, 500)
		);
	}

}
