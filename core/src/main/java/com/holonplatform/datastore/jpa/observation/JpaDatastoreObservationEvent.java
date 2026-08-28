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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Observation event for JPA Datastore operations.
 * <p>
 * Contains metadata about a datastore operation including operation name, target entity,
 * timing information, and custom attributes for tracing and metrics purposes.
 * </p>
 *
 * @since 10.0.0
 */
public final class JpaDatastoreObservationEvent {

	private final String name;
	private final String targetEntity;
	private final Map<String, Object> attributes;
	private final long startTimeNanos;
	private long endTimeNanos = -1L;

	/**
	 * Create a new observation event.
	 *
	 * @param name         The observation name (not null)
	 * @param targetEntity The target entity name for the operation (nullable)
	 */
	public JpaDatastoreObservationEvent(String name, String targetEntity) {
		this.name = Objects.requireNonNull(name, "Observation name cannot be null");
		this.targetEntity = targetEntity;
		this.attributes = new HashMap<>();
		this.startTimeNanos = System.nanoTime();
	}

	/**
	 * Get the observation name.
	 *
	 * @return The name (not null)
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the target entity name.
	 *
	 * @return The target entity, or null if not applicable
	 */
	public String getTargetEntity() {
		return targetEntity;
	}

	/**
	 * Set a custom attribute for this observation.
	 *
	 * @param key   The attribute key (not null)
	 * @param value The attribute value
	 * @return This event for chaining
	 */
	public JpaDatastoreObservationEvent setAttribute(String key, Object value) {
		Objects.requireNonNull(key, "Attribute key cannot be null");
		this.attributes.put(key, value);
		return this;
	}

	/**
	 * Get a custom attribute value.
	 *
	 * @param key The attribute key
	 * @return The value, or null if not set
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Get all custom attributes.
	 *
	 * @return A map of attributes (immutable view)
	 */
	public Map<String, Object> getAttributes() {
		return Map.copyOf(attributes);
	}

	/**
	 * Get the start time in nanoseconds.
	 *
	 * @return Start time (nanos)
	 */
	public long getStartTimeNanos() {
		return startTimeNanos;
	}

	/**
	 * Get the end time in nanoseconds.
	 *
	 * @return End time (nanos), or -1 if not yet completed
	 */
	public long getEndTimeNanos() {
		return endTimeNanos;
	}

	/**
	 * Set the end time to now.
	 * <p>
	 * Called internally when operation completes.
	 * </p>
	 */
	public void endNow() {
		this.endTimeNanos = System.nanoTime();
	}

	/**
	 * Get the operation duration in milliseconds.
	 *
	 * @return Duration in ms, or -1 if operation hasn't completed
	 */
	public long getDurationMillis() {
		if (endTimeNanos == -1L) {
			return -1L;
		}
		return (endTimeNanos - startTimeNanos) / 1_000_000;
	}

	/**
	 * Check if the operation has completed.
	 *
	 * @return True if endNow() has been called
	 */
	public boolean isCompleted() {
		return endTimeNanos != -1L;
	}

	@Override
	public String toString() {
		return "JpaDatastoreObservationEvent{" +
				"name='" + name + '\'' +
				", targetEntity='" + targetEntity + '\'' +
				", durationMs=" + getDurationMillis() +
				", attributes=" + attributes +
				'}';
	}

}
