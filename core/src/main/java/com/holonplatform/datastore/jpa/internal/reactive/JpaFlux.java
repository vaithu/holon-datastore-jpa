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
package com.holonplatform.datastore.jpa.internal.reactive;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jpa.internal.util.AsyncQuery;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Reactor Flux adapter for JPA query execution.
 * 
 * Provides stream-oriented, back-pressure-aware query results using Project Reactor's Flux.
 * Queries execute asynchronously on Schedulers.boundedElastic() for optimal I/O handling.
 * 
 * <h2>Usage Example - Fluent Builder (Recommended)</h2>
 * <pre>
 * Flux&lt;?&gt; flux = JpaFlux.builder(query, PROPERTIES)
 *     .scheduler(Schedulers.boundedElastic())
 *     .build();
 * 
 * flux.map(box -> box.getValue(NAME))
 *     .doOnNext(name -> logger.info("Processing: {}", name))
 *     .onBackpressureBuffer()
 *     .subscribe(System.out::println);
 * </pre>
 * 
 * <h2>Usage Example - Static Factory</h2>
 * <pre>
 * JpaFlux.from(query, PROPERTIES)
 *     .map(box -> box.getValue(NAME))
 *     .doOnNext(name -> logger.info("Processing: {}", name))
 *     .subscribe(System.out::println);
 * </pre>
 *
 * @since 12.0.0
 */
public final class JpaFlux {

	private JpaFlux() {
		// utility class
	}

	/**
	 * Start building a Flux with fluent builder pattern.
	 *
	 * @param query the Holon Query (must not be null)
	 * @param properties the properties to retrieve (must not be null)
	 * @return new builder instance
	 */
	public static JpaFluxBuilder builder(Query query, Iterable<?> properties) {
		return JpaFluxBuilder.builder(query, properties);
	}

	/**
	 * Create Flux from collection of items.
	 * 
	 * Useful for converting eager List results to reactive streams.
	 *
	 * @param <T> Item type
	 * @param items Collection of items to emit
	 * @return Flux emitting all items
	 */
	public static <T> Flux<T> fromIterable(Collection<T> items) {
		Objects.requireNonNull(items, "Items must not be null");
		return Flux.fromIterable(items);
	}

	/**
	 * Create Flux from Stream of items.
	 * 
	 * Stream is consumed and converted to Flux. Useful for wrapping lazy streams
	 * as reactive publishers.
	 *
	 * @param <T> Item type
	 * @param stream Stream of items to emit
	 * @return Flux emitting all items from stream
	 */
	public static <T> Flux<T> fromStream(Stream<T> stream) {
		Objects.requireNonNull(stream, "Stream must not be null");
		return Flux.fromStream(stream);
	}

	/**
	 * Execute query asynchronously and emit results as Flux.
	 * 
	 * Query is executed on virtual thread executor. Results are streamed with
	 * back-pressure support. Execution is deferred until subscription.
	 *
	 * @param query Query to execute (not null)
	 * @param properties Property iterable to retrieve
	 * @return Flux emitting query results
	 */
	public static Flux<?> from(Query query, Iterable<?> properties) {
		return from(query, properties, Schedulers.boundedElastic());
	}

	/**
	 * Execute query asynchronously and emit results as Flux with custom scheduler.
	 * 
	 * Query is executed on virtual thread executor with specified scheduler.
	 * Results are streamed with back-pressure support. Execution is deferred until subscription.
	 *
	 * @param query Query to execute (not null)
	 * @param properties Property iterable to retrieve
	 * @param scheduler the scheduler for async execution
	 * @return Flux emitting query results
	 */
	public static Flux<?> from(Query query, Iterable<?> properties, Scheduler scheduler) {
		Objects.requireNonNull(query, "Query must not be null");
		Objects.requireNonNull(properties, "Properties must not be null");
		Objects.requireNonNull(scheduler, "Scheduler must not be null");

		return Flux.defer(() -> {
			// Wrap query with async support
			AsyncQuery asyncQuery = new AsyncQuery(query);
			
			// Execute async list operation
			try {
				var results = asyncQuery.listAsync(properties).get();
				return Flux.fromIterable(results);
			} catch (Exception e) {
				return Flux.error(e);
			}
		})
		.subscribeOn(scheduler);
	}

	/**
	 * Execute query asynchronously and emit results as Flux using stream.
	 * 
	 * Query is executed on virtual thread executor. Results are emitted as
	 * they arrive from the stream. Offers true lazy evaluation with back-pressure.
	 *
	 * @param query Query to execute (not null)
	 * @param properties Property iterable to retrieve
	 * @return Flux emitting query results in streaming fashion
	 */
	public static Flux<?> fromStream(Query query, Iterable<?> properties) {
		return fromStream(query, properties, Schedulers.boundedElastic());
	}

	/**
	 * Execute query asynchronously and emit results as Flux using stream with custom scheduler.
	 * 
	 * Query is executed on virtual thread executor with specified scheduler.
	 * Results are emitted as they arrive from the stream. Offers true lazy evaluation with back-pressure.
	 *
	 * @param query Query to execute (not null)
	 * @param properties Property iterable to retrieve
	 * @param scheduler the scheduler for async execution
	 * @return Flux emitting query results in streaming fashion
	 */
	public static Flux<?> fromStream(Query query, Iterable<?> properties, Scheduler scheduler) {
		Objects.requireNonNull(query, "Query must not be null");
		Objects.requireNonNull(properties, "Properties must not be null");
		Objects.requireNonNull(scheduler, "Scheduler must not be null");

		return Flux.defer(() -> {
			// Wrap query with async support
			AsyncQuery asyncQuery = new AsyncQuery(query);
			
			// Execute async stream operation
			try {
				var results = asyncQuery.streamAsync(properties).get();
				return Flux.fromStream(results);
			} catch (Exception e) {
				return Flux.error(e);
			}
		})
		.subscribeOn(scheduler);
	}

}
