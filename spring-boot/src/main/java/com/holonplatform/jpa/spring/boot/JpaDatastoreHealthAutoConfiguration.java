/*
 * Copyright 2016-2017 Axioma srl.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jpa.spring.boot;

import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;

import com.holonplatform.datastore.jpa.JpaDatastore;

/**
 * Spring Boot auto-configuration that registers a {@link JpaDatastoreHealthIndicator} bean when:
 * <ul>
 *   <li>{@code spring-boot-actuator} is on the classpath ({@link HealthIndicator} is present)</li>
 *   <li>Exactly one {@link JpaDatastore} bean is registered (or one is marked {@code @Primary})</li>
 *   <li>No user-defined {@link JpaDatastoreHealthIndicator} bean already exists</li>
 * </ul>
 *
 * <p>Runs after {@link JpaDatastoreAutoConfiguration} to ensure the datastore bean is available.
 *
 * <p>For multi-datasource setups, register one {@link JpaDatastoreHealthIndicator} per datastore
 * manually in your application configuration.
 *
 * <p>To disable this auto-configured indicator:
 * <pre>management.health.jpaDatastore.enabled=false</pre>
 *
 * @since 10.0.0
 * @see JpaDatastoreHealthIndicator
 */
@AutoConfiguration
@ConditionalOnClass({ HealthIndicator.class, JpaDatastore.class })
@ConditionalOnBean(JpaDatastore.class)
@AutoConfigureAfter({ JpaDatastoreAutoConfiguration.class })
public class JpaDatastoreHealthAutoConfiguration {

	@Bean
	@ConditionalOnSingleCandidate(JpaDatastore.class)
	@ConditionalOnMissingBean(JpaDatastoreHealthIndicator.class)
	public JpaDatastoreHealthIndicator jpaDatastoreHealthIndicator(JpaDatastore datastore) {
		return new JpaDatastoreHealthIndicator(datastore);
	}

}
