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

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.util.Assert;

import com.holonplatform.datastore.jpa.JpaDatastore;

/**
 * Spring Boot {@link org.springframework.boot.actuate.health.HealthIndicator} for a {@link JpaDatastore}.
 *
 * <p>Probes the underlying database by executing a {@code SELECT 1} native query through the
 * Datastore's own EntityManager lifecycle. This verifies both the EntityManagerFactory and the
 * JDBC connection are operational. Reported under the {@code jpaDatastore} health component by
 * default (customisable via {@code management.health.components.*}).
 *
 * <p>Details included in the health response:
 * <ul>
 *   <li>{@code dataContextId} — the Holon data context identifier ({@code "default"} for single-datasource setups)</li>
 * </ul>
 *
 * <p>Registered automatically by {@link JpaDatastoreHealthAutoConfiguration} when
 * {@code spring-boot-actuator} is on the classpath. Can be disabled via:
 * <pre>management.health.jpaDatastore.enabled=false</pre>
 *
 * @since 10.0.0
 * @see JpaDatastoreHealthAutoConfiguration
 */
public class JpaDatastoreHealthIndicator extends AbstractHealthIndicator {

	private final JpaDatastore datastore;

	/**
	 * Create a new {@code JpaDatastoreHealthIndicator}.
	 * @param datastore the {@link JpaDatastore} to health-check (not null)
	 */
	public JpaDatastoreHealthIndicator(JpaDatastore datastore) {
		super("JPA Datastore health check failed");
		Assert.notNull(datastore, "JpaDatastore must not be null");
		this.datastore = datastore;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		datastore.withEntityManager(em -> {
			em.createNativeQuery("SELECT 1").getSingleResult();
			return null;
		});
		builder.up()
			   .withDetail("dataContextId", datastore.getDataContextId().orElse("default"));
	}

}
