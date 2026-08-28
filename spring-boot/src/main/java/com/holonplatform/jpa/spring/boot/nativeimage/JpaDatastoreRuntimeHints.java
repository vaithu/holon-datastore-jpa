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
package com.holonplatform.jpa.spring.boot.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor;
import com.holonplatform.datastore.jpa.observation.DefaultJpaDatastoreObservationRegistry;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationEvent;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationListener;
import com.holonplatform.datastore.jpa.observation.JpaDatastoreObservationRegistry;

/**
 * GraalVM native image hints for Holon JPA Datastore.
 * <p>
 * Registers reflection hints for classes used in native image builds.
 * This enables AOT (Ahead-of-Time) compilation with GraalVM's native-image tool.
 * </p>
 * <p>
 * Automatically discovered and registered via Spring Boot's AOT framework.
 * </p>
 *
 * @since 10.0.0
 */
public class JpaDatastoreRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerJpaDatastoreHints(hints);
		registerAsyncHints(hints);
		registerObservabilityHints(hints);
		registerLoggingHints(hints);
	}

	/**
	 * Register hints for core JPA Datastore classes.
	 */
	private void registerJpaDatastoreHints(RuntimeHints hints) {
		hints.reflection()
			.registerType(JpaDatastore.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INVOKE_DECLARED_METHODS);
	}

	/**
	 * Register hints for virtual thread async execution classes.
	 */
	private void registerAsyncHints(RuntimeHints hints) {
		hints.reflection()
			.registerType(VirtualThreadDatastoreExecutor.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(VirtualThreadDatastoreExecutor.QueryOperation.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(VirtualThreadDatastoreExecutor.QueryStreamOperation.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(VirtualThreadDatastoreExecutor.DatastoreOperation.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(VirtualThreadDatastoreExecutor.AsyncBuilder.class,
				MemberCategory.INVOKE_PUBLIC_METHODS);
	}

	/**
	 * Register hints for observability and tracing classes.
	 */
	private void registerObservabilityHints(RuntimeHints hints) {
		hints.reflection()
			.registerType(JpaDatastoreObservationRegistry.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(DefaultJpaDatastoreObservationRegistry.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INVOKE_DECLARED_METHODS)
			.registerType(JpaDatastoreObservationListener.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(JpaDatastoreObservationEvent.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INTROSPECT_PUBLIC_METHODS);

		// Register observation functional interfaces
		hints.reflection()
			.registerType(JpaDatastoreObservationRegistry.JpaDatastoreOperation.class,
				MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(JpaDatastoreObservationRegistry.JpaDatastoreOperationVoid.class,
				MemberCategory.INVOKE_PUBLIC_METHODS);
	}

	/**
	 * Register hints for structured logging classes.
	 */
	private void registerLoggingHints(RuntimeHints hints) {
		hints.reflection()
			.registerType(com.holonplatform.datastore.jpa.logging.StructuredLogger.class,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.INVOKE_DECLARED_METHODS);
	}

}
