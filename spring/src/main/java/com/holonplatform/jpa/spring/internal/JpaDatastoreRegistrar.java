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

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreConfigProperties;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.datastore.jpa.JpaDatastore;
import com.holonplatform.datastore.jpa.dialect.ORMDialect;
import com.holonplatform.datastore.jpa.internal.DefaultJpaDatastore;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.jpa.spring.EnableJpa;
import com.holonplatform.jpa.spring.EnableJpaDatastore;
import com.holonplatform.jpa.spring.JpaDatastoreConfigProperties;
import com.holonplatform.jpa.spring.SpringEntityManagerLifecycleHandler;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;
import com.holonplatform.spring.PrimaryMode;
import com.holonplatform.spring.internal.AbstractConfigPropertyRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.GenericDataContextBoundBeanDefinition;

import com.holonplatform.jdbc.spring.EnableDataSource;

/**
 * Registrar for JPA {@link Datastore} bean registration using {@link EnableJpaDatastore} annotation.
 * 
 * @since 5.0.0
 */
public class JpaDatastoreRegistrar extends AbstractConfigPropertyRegistrar implements BeanClassLoaderAware {

	/*
	 * Logger
	 */
	private static final Logger logger = JpaDatastoreLogger.create();

	/**
	 * Beans class loader
	 */
	private ClassLoader beanClassLoader;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		if (!annotationMetadata.isAnnotated(EnableJpaDatastore.class.getName())) {
			// ignore call from sub classes
			return;
		}

		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableJpaDatastore.class.getName());

		// attributes
		String dataContextId = BeanRegistryUtils.getAnnotationValue(attributes, "dataContextId", null);
		String entityManagerFactoryReference = BeanRegistryUtils.getAnnotationValue(attributes,
				"entityManagerFactoryReference", null);

		String emfBeanName = entityManagerFactoryReference;
		if (emfBeanName == null) {
			emfBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
					EnableJpa.DEFAULT_ENTITYMANAGERFACTORY_BEAN_NAME);
		}

		PrimaryMode primaryMode = BeanRegistryUtils.getAnnotationValue(attributes, "primary", PrimaryMode.AUTO);

		// defaults
		JpaDatastoreConfigProperties defaultConfig = JpaDatastoreConfigProperties.builder(dataContextId)
				.withProperty(JpaDatastoreConfigProperties.PRIMARY,
						(primaryMode == PrimaryMode.TRUE) ? Boolean.TRUE : null)
				.withProperty(JpaDatastoreConfigProperties.AUTO_FLUSH,
						BeanRegistryUtils.getAnnotationValue(attributes, "autoFlush", false))
				.withProperty(JpaDatastoreConfigProperties.TRANSACTIONAL,
						BeanRegistryUtils.getAnnotationValue(attributes, "transactional", true))
				.build();

		registerDatastore(registry, getEnvironment(), dataContextId, emfBeanName, defaultConfig, beanClassLoader);

	}

	/**
	 * Register a {@link JpaDatastore} bean
	 * @param registry BeanDefinitionRegistry
	 * @param environment Spring environment
	 * @param dataContextId Data context id
	 * @param entityManagerFactoryBeanName EntityManagerFactory bean name reference
	 * @param defaultConfig Default configuration properties
	 * @param beanClassLoader Bean class loader
	 * @return Registered Datastore bean name
	 */
	public static String registerDatastore(BeanDefinitionRegistry registry, Environment environment,
			String dataContextId, String entityManagerFactoryBeanName, JpaDatastoreConfigProperties defaultConfig,
			ClassLoader beanClassLoader) {

		// Datastore configuration
		DatastoreConfigProperties datastoreConfig = DatastoreConfigProperties.builder(dataContextId)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();

		// JPA Datastore configuration
		JpaDatastoreConfigProperties jpaDatastoreConfig = JpaDatastoreConfigProperties.builder(dataContextId)
				.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build();

		// Configuration
		boolean primary = defaultConfig
				.getConfigPropertyValueOrElse(JpaDatastoreConfigProperties.PRIMARY,
						() -> jpaDatastoreConfig.getConfigPropertyValue(JpaDatastoreConfigProperties.PRIMARY))
				.orElse(false);

		boolean transactional = defaultConfig
				.getConfigPropertyValueOrElse(JpaDatastoreConfigProperties.TRANSACTIONAL,
						() -> jpaDatastoreConfig.getConfigPropertyValue(JpaDatastoreConfigProperties.TRANSACTIONAL))
				.orElse(true);

		boolean autoFlush = defaultConfig
				.getConfigPropertyValueOrElse(JpaDatastoreConfigProperties.AUTO_FLUSH,
						() -> jpaDatastoreConfig.getConfigPropertyValue(JpaDatastoreConfigProperties.AUTO_FLUSH))
				.orElse(false);

		if (!primary && registry.containsBeanDefinition(entityManagerFactoryBeanName)) {
			primary = registry.getBeanDefinition(entityManagerFactoryBeanName).isPrimary();
		}

		// Use TransactionalJpaDatastore when transactional support is enabled — AOT-compatible replacement
		// for the previous ByteBuddy runtime proxy.
		Class<? extends DefaultJpaDatastore> datastoreClass = transactional
				? TransactionalJpaDatastore.class
				: DefaultJpaDatastore.class;

		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);
		definition.setBeanClass(datastoreClass);
		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setDependsOn(entityManagerFactoryBeanName);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		String beanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableJpaDatastore.DEFAULT_DATASTORE_BEAN_NAME);

		final SpringEntityManagerLifecycleHandler entityManagerLifecycleHandler = SpringEntityManagerLifecycleHandler
				.create();

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("initializationClassLoader", beanClassLoader);
		pvs.add("entityManagerFactory", new RuntimeBeanReference(entityManagerFactoryBeanName));
		pvs.add("entityManagerInitializer", entityManagerLifecycleHandler);
		pvs.add("entityManagerFinalizer", entityManagerLifecycleHandler);
		pvs.add("autoFlush", autoFlush);

		if (dataContextId != null) {
			pvs.add("dataContextId", dataContextId);
		}

		// Inject the qualified TransactionManager so TransactionalJpaDatastore uses the correct one
		if (transactional) {
			String tmBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
					EnableDataSource.DEFAULT_TRANSACTIONMANAGER_BEAN_NAME);
			if (registry.containsBeanDefinition(tmBeanName)) {
				pvs.add("transactionManager", new RuntimeBeanReference(tmBeanName));
			}
		}

		if (datastoreConfig != null) {
			if (datastoreConfig.isTrace()) {
				pvs.add("traceEnabled", Boolean.TRUE);
			}
			String dialectClassName = datastoreConfig.getDialect();
			if (dialectClassName != null) {
				try {
					ORMDialect dialect = (ORMDialect) Class.forName(dialectClassName).getDeclaredConstructor()
							.newInstance();
					pvs.add("dialect", dialect);
				} catch (Exception e) {
					throw new BeanCreationException(beanName,
							"Failed to load ORMDialect class using name [" + dialectClassName + "]", e);
				}
			}
		}

		definition.setPropertyValues(pvs);
		definition.setInitMethodName("initialize");

		registry.registerBeanDefinition(beanName, definition);

		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ").append(dataContextId).append("> ");
		}
		log.append("Registered JPA Datastore bean with name \"").append(beanName).append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"").append(dataContextId).append("\"");
		}
		log.append(" bound to EntityManagerFactory bean: ").append(entityManagerFactoryBeanName);
		logger.info(log.toString());

		return beanName;

	}

}

