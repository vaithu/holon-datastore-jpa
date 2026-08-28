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
 * Exception thrown when one or more operations fail within a
 * {@link ConcurrentTransactionScope}.
 *
 * <p>
 * This exception aggregates all errors that occurred during concurrent transaction
 * execution, allowing callers to inspect which tasks failed and why.
 * </p>
 *
 * @since 12.0.0
 */
public class AggregatedTransactionException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Map<String, Throwable> failedTasks;
	private final int totalTasks;
	private final int failureCount;

	/**
	 * Create a new aggregated transaction exception.
	 *
	 * @param message the exception message
	 * @param failedTasks map of task name to exception
	 * @param totalTasks total number of tasks executed
	 */
	public AggregatedTransactionException(String message, Map<String, Throwable> failedTasks, int totalTasks) {
		super(message);
		this.failedTasks = Collections
				.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(failedTasks, "failedTasks")));
		this.totalTasks = totalTasks;
		this.failureCount = failedTasks.size();
	}

	/**
	 * Get all failed tasks and their associated exceptions.
	 *
	 * @return immutable map of task name to exception
	 */
	public Map<String, Throwable> getFailedTasks() {
		return failedTasks;
	}

	/**
	 * Get the number of failed tasks.
	 *
	 * @return failure count
	 */
	public int getFailureCount() {
		return failureCount;
	}

	/**
	 * Get the total number of tasks that were executed.
	 *
	 * @return total tasks count
	 */
	public int getTotalTasks() {
		return totalTasks;
	}

	/**
	 * Check if all tasks failed.
	 *
	 * @return true if all tasks failed
	 */
	public boolean allFailed() {
		return failureCount == totalTasks;
	}

	/**
	 * Check if some (but not all) tasks failed.
	 *
	 * @return true if some tasks failed
	 */
	public boolean partialFailure() {
		return failureCount > 0 && failureCount < totalTasks;
	}

	@Override
	public String toString() {
		return String.format(
				"AggregatedTransactionException{failures=%d, total=%d, message=%s}",
				failureCount, totalTasks, getMessage());
	}
}
