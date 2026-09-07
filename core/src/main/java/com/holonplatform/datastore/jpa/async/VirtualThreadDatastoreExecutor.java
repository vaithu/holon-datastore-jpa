/*
 * Copyright 2016-2025 Holon Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
		Objects.requireNonNull(operation, OPERATION_NOT_NULL);
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
package com.holonplatform.datastore.jpa.async;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import com.holonplatform.datastore.jpa.JpaDatastore;

/**
 * Virtual Thread-based async executor for {@link JpaDatastore} operations.
 * <p>
 * Leverages Java 25 virtual threads to provide scalable asynchronous execution of datastore operations
 * without the overhead of managing platform thread pools.
 * </p>
 * <p>
 * This executor wraps synchronous datastore operations and executes them on virtual threads via
 * {@link CompletableFuture}, enabling thousands of concurrent operations with minimal resource consumption.
 * </p>
 * <p>
 * Usage example with fluent builder pattern:
 * <pre>
 * VirtualThreadDatastoreExecutor executor = VirtualThreadDatastoreExecutor.builder()
 *     .datastore(myDatastore)
 *     .build();
 *
 * executor.executeAsync(ds -> ds.query(User.class).list())
 *     .thenAccept(users -> System.out.println(users));
 * </pre>
 * </p>
 *
 * @since 10.0.0
 */
public final class VirtualThreadDatastoreExecutor implements AutoCloseable {

	private static final String EXECUTOR_NOT_NULL = "Executor cannot be null";
	private static final String DATASTORE_NOT_NULL = "Datastore cannot be null";
	private static final String OPERATION_NOT_NULL = "Operation cannot be null";

	/**
	 * Default virtual thread executor factory (creates a new one per instance)
	 */
	private static final Executor DEFAULT_EXECUTOR = Executors
			.newVirtualThreadPerTaskExecutor();

	/**
	 * The underlying executor (virtual thread based)
	 */
	private final Executor executor;

	/**
	 * Whether we own the executor (and should close it)
	 */
	private final boolean ownsExecutor;

	/**
	 * The wrapped datastore
	 */
	private final JpaDatastore datastore;

	/**
	 * Get a builder to create a {@link VirtualThreadDatastoreExecutor} instance.
	 * @return Builder instance
	 */
	public static Builder builder() {
		return new DefaultBuilder();
	}

	/**
	 * Create a new async executor using the default virtual thread executor.
	 *
	 * @param datastore The {@link JpaDatastore} to wrap (not null)
	 */
	public VirtualThreadDatastoreExecutor(JpaDatastore datastore) {
		this(datastore, DEFAULT_EXECUTOR, false);
	}

	/**
	 * Create a new async executor with a custom executor.
	 *
	 * @param datastore The {@link JpaDatastore} to wrap (not null)
	 * @param executor  The {@link Executor} to use for async operations (not null)
	 */
	public VirtualThreadDatastoreExecutor(JpaDatastore datastore, Executor executor) {
		this(datastore, executor, false);
	}

	/**
	 * Create a new async executor with a custom executor.
	 *
	 * @param datastore   The {@link JpaDatastore} to wrap (not null)
	 * @param executor    The {@link Executor} to use for async operations (not null)
	 * @param ownsExecutor Whether this executor should manage the lifecycle of the provided executor
	 */
	public VirtualThreadDatastoreExecutor(JpaDatastore datastore, Executor executor,
			boolean ownsExecutor) {
		this.datastore = Objects.requireNonNull(datastore, DATASTORE_NOT_NULL);
		this.executor = Objects.requireNonNull(executor, EXECUTOR_NOT_NULL);
		this.ownsExecutor = ownsExecutor;
	}

	/**
	 * Get the wrapped datastore.
	 *
	 * @return The {@link JpaDatastore}
	 */
	public JpaDatastore getDatastore() {
		return datastore;
	}

	/**
	 * Get the underlying executor.
	 *
	 * @return The {@link Executor}
	 */
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * Execute a query operation asynchronously on virtual threads.
	 * <p>
	 * The query operation is executed without blocking, returning a {@link CompletableFuture}
	 * that completes when the query finishes.
	 * </p>
	 *
	 * @param <R>       The result type
	 * @param operation The query operation to execute (not null)
	 * @return A {@link CompletableFuture} that will complete with the operation result
	 */
	public <R> CompletableFuture<R> executeAsync(QueryOperation<R> operation) {
		Objects.requireNonNull(operation, OPERATION_NOT_NULL);
		return CompletableFuture.supplyAsync(() -> operation.execute(datastore), executor);
	}

	/**
	 * Execute a stream operation asynchronously on virtual threads, collecting results to a list.
	 * <p>
	 * The stream is fully consumed and collected into a list before the future completes.
	 * This is suitable for bounded result sets. For large result sets, consider using
	 * {@link #executeAsyncStream(QueryStreamOperation)} instead.
	 * </p>
	 *
	 * @param <R>       The stream element type
	 * @param operation The stream operation to execute (not null)
	 * @return A {@link CompletableFuture} that will complete with a list of results
	 */
	public <R> CompletableFuture<List<R>> executeAsyncList(QueryStreamOperation<R> operation) {
		Objects.requireNonNull(operation, OPERATION_NOT_NULL);
		return CompletableFuture.supplyAsync(() -> {
			try (Stream<R> stream = operation.executeStream(datastore)) {
				return stream.toList();
			}
		}, executor);
	}

	/**
	 * Execute a stream operation asynchronously on virtual threads.
	 * <p>
	 * Note: The returned stream must be consumed within the virtual thread context. For better
	 * resource handling, consider collecting the stream to a list using
	 * {@link #executeAsyncList(QueryStreamOperation)} instead.
	 * </p>
	 *
	 * @param <R>       The stream element type
	 * @param operation The stream operation to execute (not null)
	 * @return A {@link CompletableFuture} that will complete with a stream of results
	 */
	public <R> CompletableFuture<Stream<R>> executeAsyncStream(
			QueryStreamOperation<R> operation) {
		Objects.requireNonNull(operation, OPERATION_NOT_NULL);
		return CompletableFuture.supplyAsync(() -> operation.executeStream(datastore), executor);
	}

	/**
	 * Execute a datastore operation asynchronously without returning a result.
	 *
	 * @param operation The operation to execute (not null)
	 * @return A {@link CompletableFuture} that will complete when the operation finishes
	 */
	public CompletableFuture<Void> executeAsyncVoid(DatastoreOperation operation) {
		Objects.requireNonNull(operation, OPERATION_NOT_NULL);
		return CompletableFuture.runAsync(() -> operation.execute(datastore), executor);
	}

	/**
	 * Execute multiple query operations in parallel and wait for all to complete.
	 * <p>
	 * This is useful for fan-out scenarios where you need to execute multiple independent
	 * operations concurrently and combine their results.
	 * </p>
	 *
	 * @param operations The operations to execute in parallel (not null)
	 * @return A {@link CompletableFuture} that completes when all operations finish
	 */
	@SafeVarargs
	public final <R> CompletableFuture<Void> executeAsyncAll(
			CompletableFuture<R>... operations) {
		Objects.requireNonNull(operations, "Operations cannot be null");
		return CompletableFuture.allOf(operations);
	}

	/**
	 * Execute multiple query operations in parallel and return the first completed result.
	 * <p>
	 * Useful for fan-out/fan-in scenarios where you need the fastest result.
	 * </p>
	 *
	 * @param <R>        The result type
	 * @param operations The operations to execute in parallel (not null)
	 * @return A {@link CompletableFuture} that completes with the first result
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final <R> CompletableFuture<R> executeAsyncAny(
			CompletableFuture<R>... operations) {
		Objects.requireNonNull(operations, "Operations cannot be null");
		return CompletableFuture.anyOf(operations).thenApply(obj -> (R) obj);
	}

	/**
	 * Close this executor and release its resources.
	 * <p>
	 * If this executor was created with a custom executor that it owns, it will attempt to close it.
	 * </p>
	 */
	@Override
	public void close() {
		if (ownsExecutor && executor instanceof AutoCloseable closeable) {
			try {
				closeable.close();
			} catch (Exception e) {
				Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(
						Thread.currentThread(),
						new RuntimeException("Error closing virtual thread executor", e));
			}
		}
	}

	// ============ Functional Interfaces ============

	/**
	 * Functional interface for query operations that return a result.
	 *
	 * @param <R> The result type
	 */
	@FunctionalInterface
	public interface QueryOperation<R> {
		/**
		 * Execute the operation with the given datastore.
		 *
		 * @param datastore The datastore to use
		 * @return The operation result
		 */
		R execute(JpaDatastore datastore);
	}

	/**
	 * Functional interface for stream-returning query operations.
	 *
	 * @param <R> The stream element type
	 */
	@FunctionalInterface
	public interface QueryStreamOperation<R> {
		/**
		 * Execute the operation with the given datastore, returning a stream.
		 *
		 * @param datastore The datastore to use
		 * @return A stream of results
		 */
		Stream<R> executeStream(JpaDatastore datastore);
	}

	/**
	 * Functional interface for side-effect operations.
	 */
	@FunctionalInterface
	public interface DatastoreOperation {
		/**
		 * Execute the operation with the given datastore.
		 *
		 * @param datastore The datastore to use
		 */
		void execute(JpaDatastore datastore);
	}

	/**
	 * Builder for fluent configuration and execution of async operations.
	 */
	public static final class AsyncBuilder {

		private final VirtualThreadDatastoreExecutor executor;

		private AsyncBuilder(VirtualThreadDatastoreExecutor executor) {
			this.executor = executor;
		}

		/**
		 * Get the executor.
		 *
		 * @return The executor
		 */
		public VirtualThreadDatastoreExecutor getExecutor() {
			return executor;
		}

		/**
		 * Execute an operation and return a future.
		 *
		 * @param <R>       The result type
		 * @param operation The operation to execute
		 * @return A future that completes with the result
		 */
		public <R> CompletableFuture<R> query(QueryOperation<R> operation) {
			return executor.executeAsync(operation);
		}

		/**
		 * Execute a stream operation and collect to a list.
		 *
		 * @param <R>       The result type
		 * @param operation The stream operation to execute
		 * @return A future that completes with a list of results
		 */
		public <R> CompletableFuture<List<R>> list(QueryStreamOperation<R> operation) {
			return executor.executeAsyncList(operation);
		}

		/**
		 * Execute a stream operation.
		 *
		 * @param <R>       The result type
		 * @param operation The stream operation to execute
		 * @return A future that completes with a stream
		 */
		public <R> CompletableFuture<Stream<R>> stream(QueryStreamOperation<R> operation) {
			return executor.executeAsyncStream(operation);
		}

		/**
		 * Execute a side-effect operation.
		 *
		 * @param operation The operation to execute
		 * @return A future that completes when the operation finishes
		 */
		public CompletableFuture<Void> execute(DatastoreOperation operation) {
			return executor.executeAsyncVoid(operation);
		}
	}

	/**
	 * Get a builder for fluent async operation execution.
	 *
	 * @return A new {@link AsyncBuilder}
	 */
	public AsyncBuilder async() {
		return new AsyncBuilder(this);
	}

	// ==================== Builder Interface ====================

	/**
	 * Builder for creating {@link VirtualThreadDatastoreExecutor} instances using fluent API.
	 * <p>
	 * Follows the Holon Platform fluent builder pattern for chainable configuration.
	 * </p>
	 */
	public interface Builder {

		/**
		 * Set the {@link JpaDatastore} to wrap for async execution.
		 *
		 * @param datastore The datastore (not null)
		 * @return this
		 */
		Builder datastore(JpaDatastore datastore);

		/**
		 * Set a custom {@link Executor} for async operations.
		 * <p>
		 * If not set, defaults to {@link Executors#newVirtualThreadPerTaskExecutor()}.
		 * </p>
		 *
		 * @param executor The executor (not null)
		 * @return this
		 */
		Builder executor(Executor executor);

		/**
		 * Build the {@link VirtualThreadDatastoreExecutor} instance.
		 *
		 * @return A new VirtualThreadDatastoreExecutor
		 * @throws IllegalStateException if required configuration is missing
		 */
		VirtualThreadDatastoreExecutor build();
	}

	/**
	 * Default {@link Builder} implementation.
	 */
	private static final class DefaultBuilder implements Builder {

		private JpaDatastore datastore;
		private Executor executor;

		@Override
		public Builder datastore(JpaDatastore datastore) {
			Objects.requireNonNull(datastore, DATASTORE_NOT_NULL);
			this.datastore = datastore;
			return this;
		}

		@Override
		public Builder executor(Executor executor) {
			Objects.requireNonNull(executor, EXECUTOR_NOT_NULL);
			this.executor = executor;
			return this;
		}

		@Override
		public VirtualThreadDatastoreExecutor build() {
			Objects.requireNonNull(datastore, DATASTORE_NOT_NULL);
			return new VirtualThreadDatastoreExecutor(datastore,
					executor != null ? executor : DEFAULT_EXECUTOR,
					executor != null); // Own executor if custom provided
		}
	}

}
