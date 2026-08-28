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
package com.holonplatform.jpa.spring.boot.async;

import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor;

/**
 * Spring Boot auto-configuration for async JPA Datastore operations using virtual threads.
 * <p>
 * This configuration provides a {@link VirtualThreadDatastoreExecutor} bean that wraps the
 * {@link JpaDatastore} and provides async/non-blocking query execution capabilities using
 * Java 25 virtual threads.
 * </p>
 * <p>
 * Configuration properties (prefixed with {@code holon.datastore.async}):
 * <ul>
 * <li>{@code enabled}: Enable/disable async datastore auto-configuration (default: true)</li>
 * </ul>
 * </p>
 *
 * @since 10.0.0
 */
@AutoConfiguration
@ConditionalOnBean(JpaDatastore.class)
@ConditionalOnProperty(name = "holon.datastore.async.enabled", havingValue = "true", matchIfMissing = true)
public class JpaDatastoreAsyncAutoConfiguration {

	/**
	 * Create a {@link VirtualThreadDatastoreExecutor} bean for the primary {@link JpaDatastore}.
	 *
	 * @param datastore The {@link JpaDatastore} (not null)
	 * @return A new {@link VirtualThreadDatastoreExecutor}
	 */
	@Bean
	@ConditionalOnMissingBean
	public VirtualThreadDatastoreExecutor virtualThreadDatastoreExecutor(
			JpaDatastore datastore) {
		return new VirtualThreadDatastoreExecutor(datastore);
	}

	/**
	 * Create a {@link VirtualThreadDatastoreExecutor} bean using a custom executor.
	 * <p>
	 * This bean is only created if a custom {@link Executor} bean is available,
	 * named "asyncDatastoreExecutor", and the default executor hasn't been registered.
	 * </p>
	 *
	 * @param datastore The {@link JpaDatastore} (not null)
	 * @param executor  The custom executor
	 * @return A new {@link VirtualThreadDatastoreExecutor} with the custom executor
	 */
	@Bean
	@ConditionalOnBean(name = "asyncDatastoreExecutor")
	@ConditionalOnMissingBean(VirtualThreadDatastoreExecutor.class)
	public VirtualThreadDatastoreExecutor virtualThreadDatastoreExecutorWithCustomExecutor(
			JpaDatastore datastore,
			Executor executor) {
		return new VirtualThreadDatastoreExecutor(datastore, executor, true);
	}

}
