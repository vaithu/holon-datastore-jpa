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
package com.holonplatform.datastore.jpa.internal.util;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.holonplatform.core.query.Query;

/**
 * Fluent builder for async query execution with virtual threads.
 * 
 * Follows Holon Platform fluent builder pattern with static factory method
 * and chainable configuration methods.
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * AsyncQuery asyncQuery = AsyncQuery.builder(query)
 *     .executor(Executors.newVirtualThreadPerTaskExecutor())
 *     .build();
 * 
 * asyncQuery.listAsync(PROPERTIES)
 *     .thenAccept(results -> logger.info("Found {} results", results.size()));
 * </pre>
 *
 * @since 12.0.0
 */
public class AsyncQueryBuilder {

    private final Query query;
    private Executor executor;

    /**
     * Create a new async query builder.
     *
     * @param query the Holon Query to wrap (must not be null)
     * @return new builder instance
     */
    public static AsyncQueryBuilder builder(Query query) {
        return new AsyncQueryBuilder(query);
    }

    /**
     * Constructor (private - use {@link #builder(Query)}).
     *
     * @param query the Holon Query to wrap
     */
    private AsyncQueryBuilder(Query query) {
        this.query = Objects.requireNonNull(query, "Query must not be null");
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Set the executor for async operations.
     *
     * @param executor the executor to use (must be ExecutorService)
     * @return this builder for chaining
     */
    public AsyncQueryBuilder executor(Executor executor) {
        if (!(executor instanceof java.util.concurrent.ExecutorService)) {
            throw new IllegalArgumentException("Executor must be an ExecutorService");
        }
        this.executor = Objects.requireNonNull(executor, "Executor must not be null");
        return this;
    }

    /**
     * Build the AsyncQuery instance.
     *
     * @return configured AsyncQuery instance
     */
    public AsyncQuery build() {
        return new AsyncQuery(query, (java.util.concurrent.ExecutorService) executor);
    }
}
