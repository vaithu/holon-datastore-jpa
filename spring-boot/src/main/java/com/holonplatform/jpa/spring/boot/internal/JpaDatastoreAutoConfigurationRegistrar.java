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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.jpa.spring.JpaDatastoreConfigProperties;
import com.holonplatform.jpa.spring.internal.JpaDatastoreRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;

/**
 * Registrar for JPA {@link Datastore} beans registration.
 * 
 * @since 5.0.0
 */
public class JpaDatastoreAutoConfigurationRegistrar
		implements ImportBeanDefinitionRegistrar, BeanFactoryAware, BeanClassLoaderAware, EnvironmentAware {

	private BeanFactory beanFactory;

	private ClassLoader beanClassLoader;

	private Environment environment;

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
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
		if (beanFactory instanceof ListableBeanFactory) {
			for (String[] emfDefinition : BeanRegistryUtils.getBeanNamesWithDataContextId(registry,
					(ListableBeanFactory) beanFactory, EntityManagerFactory.class,
					AbstractEntityManagerFactoryBean.class)) {
				// register JPA Datastore
				final String dataContextId = emfDefinition[1];
				JpaDatastoreRegistrar.registerDatastore(registry, environment, dataContextId, emfDefinition[0],
						JpaDatastoreConfigProperties.builder(dataContextId).build(), beanClassLoader);
			}
		}
	}

}
