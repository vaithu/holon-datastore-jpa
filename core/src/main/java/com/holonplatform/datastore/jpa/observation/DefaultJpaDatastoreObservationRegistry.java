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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Default implementation of {@link JpaDatastoreObservationRegistry}.
 * <p>
 * This thread-safe implementation maintains a list of registered listeners and
 * distributes observation events to all listeners in the order they were registered.
 * </p>
 *
 * @since 10.0.0
 */
public class DefaultJpaDatastoreObservationRegistry implements JpaDatastoreObservationRegistry {

	private final List<JpaDatastoreObservationListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Create a new default observation registry.
	 */
	public DefaultJpaDatastoreObservationRegistry() {
	}

	@Override
	public void registerListener(JpaDatastoreObservationListener listener) {
		Objects.requireNonNull(listener, "Listener cannot be null");
		listeners.add(listener);
	}

	@Override
	public void unregisterListener(JpaDatastoreObservationListener listener) {
		Objects.requireNonNull(listener, "Listener cannot be null");
		listeners.remove(listener);
	}

	@Override
	public void onStart(JpaDatastoreObservationEvent event) {
		Objects.requireNonNull(event, "Event cannot be null");
		for (JpaDatastoreObservationListener listener : listeners) {
			try {
				listener.onStart(event);
			} catch (Exception e) {
				handleListenerError("onStart", listener, e);
			}
		}
	}

	@Override
	public void onComplete(JpaDatastoreObservationEvent event) {
		Objects.requireNonNull(event, "Event cannot be null");
		event.endNow();
		for (JpaDatastoreObservationListener listener : listeners) {
			try {
				listener.onComplete(event);
			} catch (Exception e) {
				handleListenerError("onComplete", listener, e);
			}
		}
	}

	@Override
	public void onError(JpaDatastoreObservationEvent event, Throwable exception) {
		Objects.requireNonNull(event, "Event cannot be null");
		Objects.requireNonNull(exception, "Exception cannot be null");
		event.endNow();
		for (JpaDatastoreObservationListener listener : listeners) {
			try {
				listener.onError(event, exception);
			} catch (Exception e) {
				handleListenerError("onError", listener, e);
			}
		}
	}

	@Override
	public <T> T observe(String name, Consumer<JpaDatastoreObservationEvent> attributes,
			JpaDatastoreOperation<T> operation) throws Exception {
		Objects.requireNonNull(name, "Observation name cannot be null");
		Objects.requireNonNull(operation, "Operation cannot be null");

		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(name, null);

		if (attributes != null) {
			attributes.accept(event);
		}

		onStart(event);

		try {
			T result = operation.execute();
			onComplete(event);
			return result;
		} catch (Exception e) {
			onError(event, e);
			throw e;
		}
	}

	@Override
	public void observeVoid(String name, Consumer<JpaDatastoreObservationEvent> attributes,
			JpaDatastoreOperationVoid operation) throws Exception {
		Objects.requireNonNull(name, "Observation name cannot be null");
		Objects.requireNonNull(operation, "Operation cannot be null");

		JpaDatastoreObservationEvent event = new JpaDatastoreObservationEvent(name, null);

		if (attributes != null) {
			attributes.accept(event);
		}

		onStart(event);

		try {
			operation.execute();
			onComplete(event);
		} catch (Exception e) {
			onError(event, e);
			throw e;
		}
	}

	/**
	 * Handle exceptions thrown by listeners.
	 * <p>
	 * By default, exceptions are logged but do not prevent other listeners from being notified.
	 * </p>
	 *
	 * @param methodName The name of the method that was called
	 * @param listener   The listener that threw the exception
	 * @param exception  The exception
	 */
	protected void handleListenerError(String methodName, JpaDatastoreObservationListener listener,
			Exception exception) {
		System.err.println("Error in observation listener " + listener.getClass().getName() + "." + methodName
				+ ": " + exception.getMessage());
		exception.printStackTrace(System.err);
	}

	/**
	 * Get the number of registered listeners.
	 *
	 * @return The listener count
	 */
	public int getListenerCount() {
		return listeners.size();
	}

}
