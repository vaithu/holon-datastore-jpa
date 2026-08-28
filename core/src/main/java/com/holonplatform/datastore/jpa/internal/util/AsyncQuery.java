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
package com.holonplatform.datastore.jpa.internal.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.query.Query;

/**
 * Async query execution wrapper for non-blocking JPQL/HQL queries using virtual threads.
 * 
 * Wraps Holon Platform's Query interface to provide CompletableFuture-based async methods.
 * Enables reactive composition without blocking platform threads.
 * 
 * Usage:
 * <pre>
 * Query query = datastore.query(User.class);
 * AsyncQuery asyncQuery = new AsyncQuery(query);
 * 
 * asyncQuery.listAsync(Paths.PROPERTY_SET)
 *     .thenApply(results -> results.stream()
 *         .filter(box -> box.getValue(NAME).contains("John"))
 *         .collect(Collectors.toList()))
 *     .whenComplete((results, ex) -> {
 *         if (ex != null) {
 *             logger.error("Query failed", ex);
 *         } else {
 *             logger.info("Found {} results", results.size());
 *         }
 *     });
 * </pre>
 *
 * @since 12.0.0
 */
public final class AsyncQuery {

	private static final ExecutorService VIRTUAL_THREAD_EXECUTOR = 
		Executors.newVirtualThreadPerTaskExecutor();

	private final Query query;

	/**
	 * Constructs AsyncQuery wrapping a Query.
	 *
	 * @param query Query to wrap (not null)
	 */
	public AsyncQuery(Query query) {
		this.query = Objects.requireNonNull(query, "Query must not be null");
	}

	/**
	 * Execute query and return list of results asynchronously using property iterable.
	 * 
	 * Runs on a virtual thread executor, making it suitable for I/O-bound operations.
	 * Results are materialized into a List (eager evaluation).
	 *
	 * @param properties Property iterable to retrieve (PropertySet or Collection)
	 * @return CompletableFuture completing with list of results
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<List<PropertyBox>> listAsync(Iterable<?> properties) {
		Objects.requireNonNull(properties, "Properties must not be null");
		return CompletableFuture.supplyAsync(
			() -> query.list((Iterable<com.holonplatform.core.property.Property<?>>) properties),
			VIRTUAL_THREAD_EXECUTOR
		);
	}

	/**
	 * Find first result asynchronously using property iterable.
	 * 
	 * Returns Optional.empty() if no results found. Single-row optimization.
	 *
	 * @param properties Property iterable to retrieve (PropertySet or Collection)
	 * @return CompletableFuture completing with Optional<PropertyBox>
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Optional<PropertyBox>> findOneAsync(Iterable<?> properties) {
		Objects.requireNonNull(properties, "Properties must not be null");
		return CompletableFuture.supplyAsync(
			() -> query.findOne((Iterable<com.holonplatform.core.property.Property<?>>) properties),
			VIRTUAL_THREAD_EXECUTOR
		);
	}

	/**
	 * Execute query and return stream of results asynchronously using property iterable.
	 * 
	 * Runs on a virtual thread executor. The stream is evaluated lazily but still
	 * runs within the virtual thread context. Terminal operations will trigger
	 * entity reading within the async context.
	 *
	 * @param properties Property iterable to retrieve (PropertySet or Collection)
	 * @return CompletableFuture completing with stream of results
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Stream<PropertyBox>> streamAsync(Iterable<?> properties) {
		Objects.requireNonNull(properties, "Properties must not be null");
		return CompletableFuture.supplyAsync(
			() -> query.stream((Iterable<com.holonplatform.core.property.Property<?>>) properties),
			VIRTUAL_THREAD_EXECUTOR
		);
	}

	/**
	 * Count results asynchronously.
	 * 
	 * Efficient count operation on the underlying database.
	 *
	 * @return CompletableFuture completing with count
	 */
	public CompletableFuture<Long> countAsync() {
		return CompletableFuture.supplyAsync(
			query::count,
			VIRTUAL_THREAD_EXECUTOR
		);
	}

	/**
	 * Get the underlying Query object for advanced operations.
	 *
	 * @return the wrapped Query
	 */
	public Query getQuery() {
		return query;
	}
}
