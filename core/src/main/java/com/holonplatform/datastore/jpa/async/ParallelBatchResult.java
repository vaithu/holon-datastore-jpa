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
package com.holonplatform.datastore.jpa.async;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Result of a parallel batch operation containing statistics and error information.
 *
 * <p>
 * This immutable class provides detailed metrics about a completed batch operation:
 * <ul>
 * <li>Total items processed</li>
 * <li>Successful items</li>
 * <li>Failed items</li>
 * <li>Execution time in milliseconds</li>
 * <li>Per-partition error tracking</li>
 * </ul>
 * </p>
 *
 * @since 12.0.0
 */
public final class ParallelBatchResult {

	private final int totalItems;
	private final int successfulRows;
	private final int failedRows;
	private final long executionTimeMs;
	private final Map<Integer, Exception> partitionErrors;

	/**
	 * Create a new batch result.
	 *
	 * @param totalItems total number of items processed
	 * @param successfulRows number of successful items
	 * @param failedRows number of failed items
	 * @param executionTimeMs total execution time in milliseconds
	 * @param partitionErrors map of partition index to exceptions
	 */
	public ParallelBatchResult(int totalItems, int successfulRows, int failedRows, long executionTimeMs,
			Map<Integer, Exception> partitionErrors) {
		this.totalItems = totalItems;
		this.successfulRows = successfulRows;
		this.failedRows = failedRows;
		this.executionTimeMs = executionTimeMs;
		this.partitionErrors = Collections
				.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(partitionErrors, "partitionErrors")));
	}

	/**
	 * Get the total number of items processed.
	 *
	 * @return total items
	 */
	public int getTotalItems() {
		return totalItems;
	}

	/**
	 * Get the number of successfully processed items.
	 *
	 * @return successful items count
	 */
	public int getSuccessfulRows() {
		return successfulRows;
	}

	/**
	 * Get the number of failed items.
	 *
	 * @return failed items count
	 */
	public int getFailedRows() {
		return failedRows;
	}

	/**
	 * Get the total execution time in milliseconds.
	 *
	 * @return execution time (ms)
	 */
	public long getExecutionTimeMs() {
		return executionTimeMs;
	}

	/**
	 * Get errors per partition (index -> exception).
	 *
	 * @return immutable map of partition errors
	 */
	public Map<Integer, Exception> getPartitionErrors() {
		return partitionErrors;
	}

	/**
	 * Check if all items were processed successfully.
	 *
	 * @return true if no errors occurred
	 */
	public boolean isSuccessful() {
		return failedRows == 0 && partitionErrors.isEmpty();
	}

	/**
	 * Calculate throughput in items per second.
	 *
	 * @return items per second (0 if no time elapsed)
	 */
	public double getThroughputPerSecond() {
		if (executionTimeMs == 0) {
			return 0.0;
		}
		return (totalItems * 1000.0) / executionTimeMs;
	}

	@Override
	public String toString() {
		return String.format(
				"ParallelBatchResult{total=%d, successful=%d, failed=%d, timeMs=%d, throughput=%.2f items/sec}",
				totalItems, successfulRows, failedRows, executionTimeMs, getThroughputPerSecond());
	}
}
