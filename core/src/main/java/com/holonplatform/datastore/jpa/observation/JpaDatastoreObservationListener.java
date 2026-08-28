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

/**
 * Listener for JPA Datastore operation observations.
 * <p>
 * Implementations of this interface are called for each datastore operation lifecycle event:
 * start, complete, and error. This enables building observability features such as metrics
 * collection, distributed tracing, and operation logging.
 * </p>
 *
 * @since 10.0.0
 */
public interface JpaDatastoreObservationListener {

	/**
	 * Called when a datastore operation starts.
	 *
	 * @param event The observation event
	 */
	void onStart(JpaDatastoreObservationEvent event);

	/**
	 * Called when a datastore operation completes successfully.
	 *
	 * @param event The observation event (with duration information populated)
	 */
	void onComplete(JpaDatastoreObservationEvent event);

	/**
	 * Called when a datastore operation fails with an exception.
	 *
	 * @param event     The observation event
	 * @param exception The exception that occurred
	 */
	void onError(JpaDatastoreObservationEvent event, Throwable exception);

}
