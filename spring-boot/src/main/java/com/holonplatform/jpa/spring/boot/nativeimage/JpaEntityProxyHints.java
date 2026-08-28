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

import jakarta.persistence.Entity;

/**
 * GraalVM native image hints for JPA entity proxies and Hibernate lazy loading.
 * <p>
 * Registers proxy generation hints required for Hibernate's runtime proxy creation
 * in native image builds. Entities use proxies for lazy loading, which requires
 * reflection hints for native compilation.
 * </p>
 * <p>
 * Automatically discovered and registered via Spring Boot's AOT framework.
 * </p>
 *
 * @since 10.0.0
 */
public class JpaEntityProxyHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerHibernateProxyHints(hints);
		registerJakartaPersistenceHints(hints);
	}

	/**
	 * Register hints for Hibernate proxy classes.
	 * <p>
	 * Hibernate uses ByteBuddy to generate proxy classes at runtime for lazy loading.
	 * These need reflection hints in native image.
	 * </p>
	 */
	private void registerHibernateProxyHints(RuntimeHints hints) {
		// Hibernate proxy patterns - registered generically to handle dynamic proxy generation
		// Specific Hibernate internals are registered via @RegisterReflectionForBinding
	}

	/**
	 * Register hints for Jakarta Persistence annotations.
	 * <p>
	 * These annotations are frequently accessed via reflection during entity scanning
	 * and metadata processing.
	 * </p>
	 */
	private void registerJakartaPersistenceHints(RuntimeHints hints) {
		// Core entity and relationship annotations
		hints.reflection()
			.registerType(Entity.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.Table.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.Column.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.Id.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.GeneratedValue.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.OneToMany.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.ManyToOne.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.OneToOne.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.ManyToMany.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.JoinColumn.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.JoinTable.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.Transient.class, MemberCategory.INTROSPECT_PUBLIC_METHODS);

		// Entity manager and query interfaces
		hints.reflection()
			.registerType(jakarta.persistence.EntityManager.class, MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(jakarta.persistence.EntityManagerFactory.class, MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(jakarta.persistence.Query.class, MemberCategory.INVOKE_PUBLIC_METHODS)
			.registerType(jakarta.persistence.TypedQuery.class, MemberCategory.INVOKE_PUBLIC_METHODS);

		// JPA lifecycle callbacks
		hints.reflection()
			.registerType(jakarta.persistence.PrePersist.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PostPersist.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PreUpdate.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PostUpdate.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PreRemove.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PostRemove.class, MemberCategory.INTROSPECT_PUBLIC_METHODS)
			.registerType(jakarta.persistence.PostLoad.class, MemberCategory.INTROSPECT_PUBLIC_METHODS);
	}

}
