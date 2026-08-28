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
package com.holonplatform.datastore.jpa.observation;

import java.util.function.Consumer;

/**
 * Registry for JPA Datastore observation listeners.
 * <p>
 * This interface provides a mechanism to register listeners for datastore operation observations,
 * enabling metrics collection, tracing, and other observability features.
 * </p>
 * <p>
 * Observations are emitted as operation lifecycle events: start, complete, and error.
 * Listeners can use these events to collect metrics (operation duration, success/failure rates)
 * or emit spans for distributed tracing.
 * </p>
 *
 * @since 10.0.0
 */
public interface JpaDatastoreObservationRegistry {

	/**
	 * Register a listener for datastore operation observations.
	 *
	 * @param listener The observation listener (not null)
	 */
	void registerListener(JpaDatastoreObservationListener listener);

	/**
	 * Unregister a previously registered listener.
	 *
	 * @param listener The observation listener to remove
	 */
	void unregisterListener(JpaDatastoreObservationListener listener);

	/**
	 * Emit an observation event for datastore operation start.
	 *
	 * @param event The observation event (not null)
	 */
	void onStart(JpaDatastoreObservationEvent event);

	/**
	 * Emit an observation event for successful datastore operation completion.
	 *
	 * @param event The observation event (not null)
	 */
	void onComplete(JpaDatastoreObservationEvent event);

	/**
	 * Emit an observation event for datastore operation error.
	 *
	 * @param event      The observation event (not null)
	 * @param exception  The exception that occurred
	 */
	void onError(JpaDatastoreObservationEvent event, Throwable exception);

	/**
	 * Execute a block of code within an observation scope.
	 * <p>
	 * This method handles the full lifecycle of an observation: start, execution, and completion/error.
	 * Listeners are notified at each lifecycle step.
	 * </p>
	 *
	 * @param <T>          The return type
	 * @param name         The observation name (from {@link JpaDatastoreObservationNames})
	 * @param attributes   Optional observer attributes (e.g., target entity, operation type)
	 * @param operation    The operation to execute (not null)
	 * @return The result of the operation
	 * @throws Exception If the operation fails
	 */
	<T> T observe(String name, Consumer<JpaDatastoreObservationEvent> attributes,
			JpaDatastoreOperation<T> operation) throws Exception;

	/**
	 * Execute a block of code within an observation scope that doesn't return a value.
	 *
	 * @param name         The observation name
	 * @param attributes   Optional observer attributes
	 * @param operation    The operation to execute
	 * @throws Exception If the operation fails
	 */
	void observeVoid(String name, Consumer<JpaDatastoreObservationEvent> attributes,
			JpaDatastoreOperationVoid operation) throws Exception;

	/**
	 * Functional interface for observable operations that return a value.
	 *
	 * @param <T> The return type
	 */
	@FunctionalInterface
	interface JpaDatastoreOperation<T> {
		/**
		 * Execute the operation.
		 *
		 * @return The operation result
		 * @throws Exception If the operation fails
		 */
		T execute() throws Exception;
	}

	/**
	 * Functional interface for observable operations that don't return a value.
	 */
	@FunctionalInterface
	interface JpaDatastoreOperationVoid {
		/**
		 * Execute the operation.
		 *
		 * @throws Exception If the operation fails
		 */
		void execute() throws Exception;
	}

}
