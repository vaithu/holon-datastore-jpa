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
package com.holonplatform.datastore.jpa.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Structured logging utility for JPA Datastore operations.
 * <p>
 * Provides a fluent API for logging datastore operations with structured context
 * that integrates with MDC (Mapped Diagnostic Context) for distributed tracing.
 * </p>
 * <p>
 * Follows the Holon Platform fluent builder pattern.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * StructuredLogger.builder()
 *     .operation("Query")
 *     .entity("User")
 *     .duration(150)
 *     .build()
 *     .info("Query executed successfully");
 *
 * // Or using static factory method:
 * StructuredLogger.forDatastore("Query")
 *     .withEntity("User")
 *     .withDuration(duration)
 *     .info("Query executed successfully");
 * </pre>
 * </p>
 *
 * @since 10.0.0
 */
public final class StructuredLogger {

	private static final String CONTEXT_PREFIX = "datastore.";
	private static final String OPERATION_KEY = CONTEXT_PREFIX + "operation";
	private static final String ENTITY_KEY = CONTEXT_PREFIX + "entity";
	private static final String DURATION_KEY = CONTEXT_PREFIX + "duration_ms";
	private static final String RECORD_COUNT_KEY = CONTEXT_PREFIX + "record_count";
	private static final String TRACE_ID_KEY = "trace_id";
	private static final String SPAN_ID_KEY = "span_id";

	private final Logger logger;
	private final Map<String, String> context;

	/**
	 * Get a builder to create a {@link StructuredLogger} instance.
	 * @return Builder instance
	 */
	public static StructuredLoggerBuilder builder() {
		return new StructuredLoggerBuilder();
	}

	/**
	 * Create a structured logger for a datastore operation.
	 *
	 * @param operation The operation name (e.g., "Query", "Insert", "Update")
	 * @return A new StructuredLogger
	 */
	public static StructuredLogger forDatastore(String operation) {
		StructuredLogger logger = new StructuredLogger(LoggerFactory.getLogger("com.holonplatform.datastore.jpa"));
		logger.context.put(OPERATION_KEY, Objects.requireNonNull(operation, "Operation cannot be null"));
		return logger;
	}

	/**
	 * Create a structured logger with a custom logger instance.
	 *
	 * @param loggerName The logger name
	 * @return A new StructuredLogger
	 */
	public static StructuredLogger forLogger(String loggerName) {
		return new StructuredLogger(LoggerFactory.getLogger(loggerName));
	}

	/**
	 * Create a structured logger with a custom logger instance.
	 *
	 * @param logger The SLF4J logger instance
	 * @return A new StructuredLogger
	 */
	public static StructuredLogger of(Logger logger) {
		return new StructuredLogger(Objects.requireNonNull(logger, "Logger cannot be null"));
	}

	private StructuredLogger(Logger logger) {
		this.logger = logger;
		this.context = new HashMap<>();
	}

	/**
	 * Add the target entity name to the context.
	 *
	 * @param entity The entity name
	 * @return This logger for chaining
	 */
	public StructuredLogger withEntity(String entity) {
		if (entity != null) {
			context.put(ENTITY_KEY, entity);
		}
		return this;
	}

	/**
	 * Add operation duration to the context.
	 *
	 * @param durationMs The duration in milliseconds
	 * @return This logger for chaining
	 */
	public StructuredLogger withDuration(long durationMs) {
		context.put(DURATION_KEY, String.valueOf(durationMs));
		return this;
	}

	/**
	 * Add record count to the context.
	 *
	 * @param count The record count
	 * @return This logger for chaining
	 */
	public StructuredLogger withRecordCount(long count) {
		context.put(RECORD_COUNT_KEY, String.valueOf(count));
		return this;
	}

	/**
	 * Add a distributed trace ID to the context.
	 *
	 * @param traceId The W3C trace ID
	 * @return This logger for chaining
	 */
	public StructuredLogger withTraceId(String traceId) {
		if (traceId != null) {
			context.put(TRACE_ID_KEY, traceId);
		}
		return this;
	}

	/**
	 * Add a custom context field.
	 *
	 * @param key   The field key
	 * @param value The field value
	 * @return This logger for chaining
	 */
	public StructuredLogger with(String key, String value) {
		if (key != null && value != null) {
			context.put(CONTEXT_PREFIX + key, value);
		}
		return this;
	}

	/**
	 * Log an info level message with structured context.
	 *
	 * @param message The message to log
	 */
	public void info(String message) {
		log(() -> logger.info(message));
	}

	/**
	 * Log an info level message with context and throwable.
	 *
	 * @param message   The message to log
	 * @param throwable The exception to log
	 */
	public void info(String message, Throwable throwable) {
		log(() -> logger.info(message, throwable));
	}

	/**
	 * Log a debug level message with structured context.
	 *
	 * @param message The message to log
	 */
	public void debug(String message) {
		log(() -> logger.debug(message));
	}

	/**
	 * Log a debug level message with context and throwable.
	 *
	 * @param message   The message to log
	 * @param throwable The exception to log
	 */
	public void debug(String message, Throwable throwable) {
		log(() -> logger.debug(message, throwable));
	}

	/**
	 * Log a warning level message with structured context.
	 *
	 * @param message The message to log
	 */
	public void warn(String message) {
		log(() -> logger.warn(message));
	}

	/**
	 * Log a warning level message with context and throwable.
	 *
	 * @param message   The message to log
	 * @param throwable The exception to log
	 */
	public void warn(String message, Throwable throwable) {
		log(() -> logger.warn(message, throwable));
	}

	/**
	 * Log an error level message with structured context.
	 *
	 * @param message The message to log
	 */
	public void error(String message) {
		log(() -> logger.error(message));
	}

	/**
	 * Log an error level message with context and throwable.
	 *
	 * @param message   The message to log
	 * @param throwable The exception to log
	 */
	public void error(String message, Throwable throwable) {
		log(() -> logger.error(message, throwable));
	}

	/**
	 * Execute a log operation with MDC context.
	 * <p>
	 * All context fields are pushed to MDC before logging and cleared after.
	 * </p>
	 *
	 * @param logOperation The logging operation
	 */
	private void log(Runnable logOperation) {
		Map<String, String> previousContext = new HashMap<>(MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : new HashMap<>());

		try {
			// Push all context to MDC
			context.forEach(MDC::put);

			// Execute the logging operation
			logOperation.run();
		} finally {
			// Restore previous context
			MDC.clear();
			previousContext.forEach(MDC::put);
		}
	}

	/**
	 * Functional interface for logging operations.
	 */
	@FunctionalInterface
	private interface LogOperation {
		void execute();
	}

	// ==================== Builder Class ====================

	/**
	 * Builder for creating {@link StructuredLogger} instances using fluent API.
	 * <p>
	 * Follows the Holon Platform fluent builder pattern for chainable configuration.
	 * </p>
	 */
	public static final class StructuredLoggerBuilder {

		private final Logger logger;
		private final Map<String, String> context = new HashMap<>();

		/**
		 * Default constructor using default logger.
		 */
		public StructuredLoggerBuilder() {
			this.logger = LoggerFactory.getLogger("com.holonplatform.datastore.jpa");
		}

		/**
		 * Constructor with custom logger.
		 *
		 * @param logger The logger instance (not null)
		 */
		public StructuredLoggerBuilder(Logger logger) {
			this.logger = Objects.requireNonNull(logger, "Logger cannot be null");
		}

		/**
		 * Set the operation name.
		 *
		 * @param operation The operation name (e.g., "Query", "Insert", "Update")
		 * @return this
		 */
		public StructuredLoggerBuilder operation(String operation) {
			Objects.requireNonNull(operation, "Operation cannot be null");
			context.put(OPERATION_KEY, operation);
			return this;
		}

		/**
		 * Set the entity name.
		 *
		 * @param entity The entity name
		 * @return this
		 */
		public StructuredLoggerBuilder entity(String entity) {
			if (entity != null) {
				context.put(ENTITY_KEY, entity);
			}
			return this;
		}

		/**
		 * Set the operation duration in milliseconds.
		 *
		 * @param durationMs The duration in milliseconds
		 * @return this
		 */
		public StructuredLoggerBuilder duration(long durationMs) {
			context.put(DURATION_KEY, String.valueOf(durationMs));
			return this;
		}

		/**
		 * Set the record count.
		 *
		 * @param count The number of records
		 * @return this
		 */
		public StructuredLoggerBuilder recordCount(int count) {
			context.put(RECORD_COUNT_KEY, String.valueOf(count));
			return this;
		}

		/**
		 * Add a custom context value.
		 *
		 * @param key   The context key
		 * @param value The context value
		 * @return this
		 */
		public StructuredLoggerBuilder with(String key, String value) {
			if (key != null && value != null) {
				context.put(key, value);
			}
			return this;
		}

		/**
		 * Build the {@link StructuredLogger} instance.
		 *
		 * @return A new StructuredLogger
		 */
		public StructuredLogger build() {
			StructuredLogger structuredLogger = new StructuredLogger(logger);
			structuredLogger.context.putAll(context);
			return structuredLogger;
		}
	}

}

