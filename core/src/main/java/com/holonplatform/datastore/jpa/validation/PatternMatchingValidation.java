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
package com.holonplatform.datastore.jpa.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Validation utilities using Java 25 pattern matching for cleaner code.
 * <p>
 * Provides methods to validate common data structures using modern pattern matching
 * instead of traditional instanceof checks.
 * </p>
 *
 * @since 10.0.0
 */
public final class PatternMatchingValidation {

	private PatternMatchingValidation() {
	}

	/**
	 * Validate a collection (using pattern matching).
	 * <p>
	 * Example usage:
	 * <pre>
	 * if (PatternMatchingValidation.validateCollection(items).isValid()) {
	 *     // Process non-empty collection
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param collection The collection to validate
	 * @return A validation result
	 */
	public static ValidationResult validateCollection(Object collection) {
		return switch (collection) {
			case null -> ValidationResult.invalid("Collection cannot be null");
			case Collection<?> c when c.isEmpty() -> ValidationResult.invalid("Collection cannot be empty");
			case Collection<?> c -> ValidationResult.valid("Collection with " + c.size() + " items");
			default -> ValidationResult.invalid("Object is not a collection: " + collection.getClass().getSimpleName());
		};
	}

	/**
	 * Validate a string (using pattern matching).
	 * <p>
	 * Example usage:
	 * <pre>
	 * if (PatternMatchingValidation.validateString(name).isValid()) {
	 *     // Process non-blank string
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param value The string to validate
	 * @return A validation result
	 */
	public static ValidationResult validateString(Object value) {
		return switch (value) {
			case null -> ValidationResult.invalid("String cannot be null");
			case String s when s.isBlank() -> ValidationResult.invalid("String cannot be blank");
			case String s -> ValidationResult.valid("String: " + s);
			default -> ValidationResult.invalid("Object is not a string: " + value.getClass().getSimpleName());
		};
	}

	/**
	 * Validate a map (using pattern matching).
	 * <p>
	 * Example usage:
	 * <pre>
	 * if (PatternMatchingValidation.validateMap(properties).isValid()) {
	 *     // Process non-empty map
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param map The map to validate
	 * @return A validation result
	 */
	public static ValidationResult validateMap(Object map) {
		return switch (map) {
			case null -> ValidationResult.invalid("Map cannot be null");
			case Map<?, ?> m when m.isEmpty() -> ValidationResult.invalid("Map cannot be empty");
			case Map<?, ?> m -> ValidationResult.valid("Map with " + m.size() + " entries");
			default -> ValidationResult.invalid("Object is not a map: " + map.getClass().getSimpleName());
		};
	}

	/**
	 * Validate a number within bounds (using pattern matching).
	 * <p>
	 * Example usage:
	 * <pre>
	 * if (PatternMatchingValidation.validateNumber(limit, 1, 100).isValid()) {
	 *     // Process valid number
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param value The number to validate
	 * @param min   The minimum value (inclusive)
	 * @param max   The maximum value (inclusive)
	 * @return A validation result
	 */
	public static ValidationResult validateNumber(Object value, long min, long max) {
		return switch (value) {
			case null -> ValidationResult.invalid("Number cannot be null");
			case Integer i when i < min || i > max -> 
				ValidationResult.invalid("Integer " + i + " is outside bounds [" + min + ", " + max + "]");
			case Integer i -> ValidationResult.valid("Integer: " + i);
			case Long l when l < min || l > max -> 
				ValidationResult.invalid("Long " + l + " is outside bounds [" + min + ", " + max + "]");
			case Long l -> ValidationResult.valid("Long: " + l);
			default -> ValidationResult.invalid("Object is not a number: " + value.getClass().getSimpleName());
		};
	}

	/**
	 * Validate an object by type using pattern matching.
	 * <p>
	 * Example usage:
	 * <pre>
	 * String className = PatternMatchingValidation.classifyObject(obj);
	 * </pre>
	 * </p>
	 *
	 * @param obj The object to classify
	 * @return A description of the object type
	 */
	public static String classifyObject(Object obj) {
		return switch (obj) {
			case null -> "null";
			case String s -> "String[" + s + "]";
			case Integer i -> "Integer[" + i + "]";
			case Long l -> "Long[" + l + "]";
			case Double d -> "Double[" + d + "]";
			case Boolean b -> "Boolean[" + b + "]";
			case Collection<?> c -> "Collection[size=" + c.size() + "]";
			case Map<?, ?> m -> "Map[size=" + m.size() + "]";
			case Throwable t -> "Exception[" + t.getClass().getSimpleName() + ": " + t.getMessage() + "]";
			default -> obj.getClass().getSimpleName() + "[" + obj + "]";
		};
	}

	/**
	 * Result of a validation operation.
	 */
	public static final class ValidationResult {
		private final boolean valid;
		private final String message;

		private ValidationResult(boolean valid, String message) {
			this.valid = valid;
			this.message = Objects.requireNonNull(message);
		}

		/**
		 * Create a valid result.
		 *
		 * @param message The result message
		 * @return A valid result
		 */
		public static ValidationResult valid(String message) {
			return new ValidationResult(true, message);
		}

		/**
		 * Create an invalid result.
		 *
		 * @param message The error message
		 * @return An invalid result
		 */
		public static ValidationResult invalid(String message) {
			return new ValidationResult(false, message);
		}

		/**
		 * Check if the validation passed.
		 *
		 * @return True if valid
		 */
		public boolean isValid() {
			return valid;
		}

		/**
		 * Get the validation message.
		 *
		 * @return The message
		 */
		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return (valid ? "VALID" : "INVALID") + ": " + message;
		}
	}

}
