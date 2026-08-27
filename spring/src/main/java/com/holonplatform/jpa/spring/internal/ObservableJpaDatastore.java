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
package com.holonplatform.jpa.spring.internal;

import org.springframework.beans.factory.annotation.Autowired;

import com.holonplatform.datastore.jpa.context.EntityManagerOperation;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Extension of {@link TransactionalJpaDatastore} that instruments every EntityManager
 * unit of work with a Micrometer {@link Observation}.
 *
 * <p>This class is loaded <em>only</em> when {@code micrometer-observation} is on the classpath.
 * Both {@link JpaDatastoreRegistrar} and the Spring Boot auto-configuration registrar guard the
 * instantiation of this class behind a {@code ClassUtils.isPresent} check so that it is never
 * loaded when Micrometer is absent.
 *
 * <p>The {@link ObservationRegistry} is injected automatically via
 * {@link Autowired @Autowired(required=false)}: if an {@code ObservationRegistry} bean is
 * present in the Spring context (e.g., provided by Spring Boot Actuator) it is wired in
 * automatically. No registry → falls back to {@link ObservationRegistry#NOOP} silently.
 *
 * <p><b>Observation name:</b> {@code holon.jpa.datastore}<br>
 * <b>Low-cardinality key-values:</b>
 * <ul>
 *   <li>{@code db.system = jpa}</li>
 *   <li>{@code db.context = <dataContextId | "default">} — tenant/datasource qualifier</li>
 * </ul>
 *
 * @since 10.0.0
 */
public class ObservableJpaDatastore extends TransactionalJpaDatastore {

	private static final long serialVersionUID = 1L;

	/** Fall back to no-op when no registry is available. */
	private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

	/**
	 * Injects the Micrometer {@link ObservationRegistry}.
	 * Marked {@code required=false} so startup does not fail when Actuator / micrometer-core
	 * is on the classpath but no registry bean is defined.
	 *
	 * @param registry observation registry; {@code null} resets to {@link ObservationRegistry#NOOP}
	 */
	@Autowired(required = false)
	public void setObservationRegistry(ObservationRegistry registry) {
		this.observationRegistry = (registry != null) ? registry : ObservationRegistry.NOOP;
	}

	/**
	 * Wraps every EntityManager unit of work with a {@code holon.jpa.datastore} observation
	 * so that distributed tracing systems (Zipkin, Jaeger, OTLP) receive a span for each
	 * database interaction.  Nested calls (EM already bound in scope) produce trivially short
	 * child spans that carry the same context.
	 */
	@Override
	public <R> R withEntityManager(EntityManagerOperation<R> operation) {
		Observation obs = Observation
				.createNotStarted("holon.jpa.datastore", observationRegistry)
				.lowCardinalityKeyValue("db.system", "jpa")
				.lowCardinalityKeyValue("db.context", getDataContextId().orElse("default"))
				.start();
		try {
			R result = super.withEntityManager(operation);
			obs.stop();
			return result;
		} catch (Exception e) {
			obs.error(e);
			obs.stop();
			throw e;
		}
	}
}
