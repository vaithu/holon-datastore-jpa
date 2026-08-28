/*
 * Copyright 2016-2025 Axioma srl.
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
package com.holonplatform.datastore.jpa.internal.reactive;

import java.util.Objects;

import com.holonplatform.core.query.Query;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Fluent builder for Project Reactor Mono adapters.
 * 
 * Follows Holon Platform fluent builder pattern with static factory method
 * and chainable configuration methods.
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * JpaMono mono = JpaMono.builder(query, PROPERTIES)
 *     .scheduler(Schedulers.boundedElastic())
 *     .build();
 * 
 * mono.doOnNext(box -> logger.info("Found: {}", box.getValue(NAME)))
 *     .subscribe();
 * </pre>
 *
 * @since 12.0.0
 */
public class JpaMonoBuilder {

    private final Query query;
    private final Iterable<?> properties;
    private Scheduler scheduler = Schedulers.boundedElastic();

    /**
     * Create a new Mono builder.
     *
     * @param query the Holon Query (must not be null)
     * @param properties the properties to retrieve (must not be null)
     * @return new builder instance
     */
    public static JpaMonoBuilder builder(Query query, Iterable<?> properties) {
        return new JpaMonoBuilder(query, properties);
    }

    /**
     * Constructor (private - use {@link #builder(Query, Iterable)}).
     *
     * @param query the Holon Query
     * @param properties the properties to retrieve
     */
    private JpaMonoBuilder(Query query, Iterable<?> properties) {
        this.query = Objects.requireNonNull(query, "Query must not be null");
        this.properties = Objects.requireNonNull(properties, "Properties must not be null");
    }

    /**
     * Set the scheduler for reactive operations.
     *
     * @param scheduler the scheduler (default: boundedElastic)
     * @return this builder for chaining
     */
    public JpaMonoBuilder scheduler(Scheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "Scheduler must not be null");
        return this;
    }

    /**
     * Build the Mono instance.
     *
     * @return configured Mono for single result
     */
    public reactor.core.publisher.Mono<?> build() {
        return JpaMono.from(query, properties, scheduler);
    }
}
