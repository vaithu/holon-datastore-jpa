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
package com.holonplatform.jpa.spring.boot.internal;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;

import io.micrometer.observation.ObservationRegistry;

import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.jpa.spring.JpaDatastoreConfigProperties;
import com.holonplatform.jpa.spring.SpringEntityManagerLifecycleHandler;
import com.holonplatform.jpa.spring.internal.ObservableJpaDatastore;
import com.holonplatform.jpa.spring.internal.TransactionalJpaDatastore;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;

/**
 * AOT-compatible {@link BeanRegistrar} for the {@link JpaDatastore} bean in the single-{@link EntityManagerFactory}
 * auto-configuration path.
 *
 * <p>Replaces the previous {@code ImportBeanDefinitionRegistrar} implementation to enable full Spring AOT processing
 * and GraalVM native-image compilation. The registered bean type ({@link TransactionalJpaDatastore} or
 * {@link DefaultJpaDatastore}) is statically visible to the AOT processor, and the {@link EntityManagerFactory}
 * dependency is resolved through the supplier context.</p>
 *
 * @since 10.0.0
 */
public class JpaDatastoreAutoConfigurationRegistrar implements BeanRegistrar {

	@Override
	public void register(BeanRegistry registry, Environment env) {

		JpaDatastoreConfigProperties config = JpaDatastoreConfigProperties.builder(null)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(env)).build();
		DatastoreConfigProperties datastoreConfig = DatastoreConfigProperties.builder(null)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(env)).build();

		boolean transactional = config.getConfigPropertyValue(JpaDatastoreConfigProperties.TRANSACTIONAL, true);
		boolean autoFlush = config.getConfigPropertyValue(JpaDatastoreConfigProperties.AUTO_FLUSH, false);
		boolean primary = config.getConfigPropertyValue(JpaDatastoreConfigProperties.PRIMARY, false);
		boolean trace = datastoreConfig.isTrace();
		String dialectClassName = datastoreConfig.getDialect();

		// Use ObservableJpaDatastore when micrometer-observation is on the classpath (e.g., Actuator present)
		// so every EntityManager unit of work is automatically traced. Falls back to TransactionalJpaDatastore
		// when Micrometer is absent — guarding the class reference so ObservableJpaDatastore is never loaded
		// unless Micrometer is actually available.
		boolean hasObservation = ClassUtils.isPresent(
				"io.micrometer.observation.ObservationRegistry",
				JpaDatastoreAutoConfigurationRegistrar.class.getClassLoader());

		DatastoreSettings settings = new DatastoreSettings(transactional, hasObservation, autoFlush, trace,
				dialectClassName);
		Class<DefaultJpaDatastore> datastoreClass = resolveDatastoreClass(settings);

		String beanName = EnableJpaDatastore.DEFAULT_DATASTORE_BEAN_NAME;

		registry.registerBean(beanName, datastoreClass, spec -> {
			if (primary) {
				spec.primary();
			}
			spec.description("Holon JPA Datastore (auto-configured)");
			spec.supplier(ctx -> createDatastore(ctx, beanName, datastoreClass, settings));
		});
	}

	/**
	 * Resolve the concrete datastore class to register based on the given settings.
	 * @param settings Datastore settings
	 * @return The datastore class to register
	 */
	@SuppressWarnings("unchecked")
	private static Class<DefaultJpaDatastore> resolveDatastoreClass(DatastoreSettings settings) {
		final Class<? extends DefaultJpaDatastore> resolved;
		if (settings.transactional() && settings.hasObservation()) {
			resolved = ObservableJpaDatastore.class;
		} else if (settings.transactional()) {
			resolved = TransactionalJpaDatastore.class;
		} else {
			resolved = DefaultJpaDatastore.class;
		}
		return (Class<DefaultJpaDatastore>) resolved;
	}

	/**
	 * Create and initialize the datastore bean instance.
	 * @param ctx            Bean supplier context
	 * @param beanName       Registered bean name
	 * @param datastoreClass Concrete datastore class
	 * @param settings       Datastore settings
	 * @return The initialized datastore instance
	 */
	private DefaultJpaDatastore createDatastore(BeanRegistry.SupplierContext ctx, String beanName,
			Class<DefaultJpaDatastore> datastoreClass, DatastoreSettings settings) {
		EntityManagerFactory emf = ctx.bean(EntityManagerFactory.class);
		DefaultJpaDatastore ds = instantiateDatastore(datastoreClass, beanName);
		SpringEntityManagerLifecycleHandler handler = SpringEntityManagerLifecycleHandler.create();
		ds.setEntityManagerFactory(emf);
		ds.setEntityManagerInitializer(handler);
		ds.setEntityManagerFinalizer(handler);
		ds.setAutoFlush(settings.autoFlush());
		if (settings.trace()) {
			ds.setTraceEnabled(true);
		}
		if (settings.transactional() && ds instanceof TransactionalJpaDatastore txDs) {
			// single-context auto-config path: inject the transaction manager
			ctx.beanProvider(PlatformTransactionManager.class).ifAvailable(txDs::setTransactionManager);
		}
		// Inject ObservationRegistry when micrometer is present — guarded by hasObservation
		// so ObservableJpaDatastore is never instantiated or loaded without micrometer
		if (settings.hasObservation() && ds instanceof ObservableJpaDatastore obsDs) {
			ctx.beanProvider(ObservationRegistry.class).ifAvailable(obsDs::setObservationRegistry);
		}
		applyDialect(ds, settings.dialectClassName(), beanName);
		ds.initialize();
		return ds;
	}

	/**
	 * Instantiate the datastore class using its default constructor.
	 * @param datastoreClass Datastore class
	 * @param beanName       Registered bean name
	 * @return A new datastore instance
	 */
	private static DefaultJpaDatastore instantiateDatastore(Class<DefaultJpaDatastore> datastoreClass,
			String beanName) {
		try {
			return datastoreClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new BeanCreationException(beanName, "Failed to instantiate " + datastoreClass.getName(), e);
		}
	}

	/**
	 * Load and apply the configured {@link ORMDialect}, if any.
	 * @param ds               Datastore instance
	 * @param dialectClassName Dialect class name (may be null)
	 * @param beanName         Registered bean name
	 */
	private static void applyDialect(DefaultJpaDatastore ds, String dialectClassName, String beanName) {
		if (dialectClassName == null) {
			return;
		}
		try {
			ORMDialect dialect = (ORMDialect) Class.forName(dialectClassName).getDeclaredConstructor().newInstance();
			ds.setDialect(dialect);
		} catch (ReflectiveOperationException e) {
			throw new BeanCreationException(beanName, "Failed to load ORMDialect class [" + dialectClassName + "]", e);
		}
	}

	/**
	 * Immutable holder for the resolved datastore configuration settings.
	 */
	private record DatastoreSettings(boolean transactional, boolean hasObservation, boolean autoFlush, boolean trace,
			String dialectClassName) {
	}

}

