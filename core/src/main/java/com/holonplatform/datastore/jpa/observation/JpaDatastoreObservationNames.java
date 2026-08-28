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
 * Observation event names for JPA Datastore operations.
 * <p>
 * These constants define the standard observation names used for tracing and metrics
 * collection across datastore operations. They are prefixed with "jpa.datastore." to
 * identify them as part of the JPA Datastore instrumentation.
 * </p>
 *
 * @since 10.0.0
 */
public final class JpaDatastoreObservationNames {

	private static final String PREFIX = "jpa.datastore.";

	/**
	 * Observation name for datastore query operations
	 */
	public static final String QUERY = PREFIX + "query";

	/**
	 * Observation name for datastore save operations
	 */
	public static final String SAVE = PREFIX + "save";

	/**
	 * Observation name for datastore insert operations
	 */
	public static final String INSERT = PREFIX + "insert";

	/**
	 * Observation name for datastore update operations
	 */
	public static final String UPDATE = PREFIX + "update";

	/**
	 * Observation name for datastore bulk update operations
	 */
	public static final String BULK_UPDATE = PREFIX + "bulk.update";

	/**
	 * Observation name for datastore delete operations
	 */
	public static final String DELETE = PREFIX + "delete";

	/**
	 * Observation name for datastore bulk delete operations
	 */
	public static final String BULK_DELETE = PREFIX + "bulk.delete";

	/**
	 * Observation name for datastore refresh operations
	 */
	public static final String REFRESH = PREFIX + "refresh";

	/**
	 * Observation name for datastore transaction operations
	 */
	public static final String TRANSACTION = PREFIX + "transaction";

	private JpaDatastoreObservationNames() {
	}

}
