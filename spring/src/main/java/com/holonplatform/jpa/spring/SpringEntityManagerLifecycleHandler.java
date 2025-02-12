/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.jpa.spring;

import jakarta.persistence.EntityManager;

import com.holonplatform.datastore.jpa.JpaDatastore.EntityManagerLifecycleHandler;
import com.holonplatform.jpa.spring.internal.DefaultSpringEntityManagerLifecycleHandler;

/**
 * An {@link EntityManagerLifecycleHandler} for Spring integration, which provides a Spring managed
 * {@link EntityManager} proxy as {@link EntityManager} instance.
 * 
 * @since 5.2.0
 */
public interface SpringEntityManagerLifecycleHandler extends EntityManagerLifecycleHandler {

	/**
	 * Create a default {@link SpringEntityManagerLifecycleHandler} instance.
	 * @return A default {@link SpringEntityManagerLifecycleHandler} instance
	 */
	static SpringEntityManagerLifecycleHandler create() {
		return new DefaultSpringEntityManagerLifecycleHandler();
	}

}
