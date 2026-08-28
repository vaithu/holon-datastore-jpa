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
package com.holonplatform.datastore.jpa.internal.cache;

import java.util.*;
import java.util.function.Function;

/**
 * Generic query result wrapper that adds caching capabilities.
 * Can wrap any query-like object and transparently intercept operations with caching.
 *
 * @since 12.0.0
 */
public class CacheableQuery {

    private final Object query;
    private final QueryResultCache cache;

    /**
     * Create a cacheable query wrapper.
     *
     * @param query the underlying query object
     * @param cache the query result cache
     */
    public CacheableQuery(Object query, QueryResultCache cache) {
        this.query = Objects.requireNonNull(query, "Query must not be null");
        this.cache = Objects.requireNonNull(cache, "Cache must not be null");
    }

    /**
     * Execute query with caching using the provided executor.
     *
     * @param executor function that executes the query
     * @return query execution result or cached result
     */
    public Object execute(Function<Object, Object> executor) {
        String cacheKey = CacheKeyGenerator.generateKey(query, "execute");
        return cache.getOrCompute(cacheKey, key -> executor.apply(query));
    }

    /**
     * Execute list query with caching.
     *
     * @param executor function that returns list of results
     * @return list of results or cached results
     */
    public Object list(Function<Object, Object> executor) {
        String cacheKey = CacheKeyGenerator.generateKey(query, "list");
        return cache.getOrCompute(cacheKey, key -> executor.apply(query));
    }

    /**
     * Execute count query with caching.
     *
     * @param executor function that returns count
     * @return count result or cached result
     */
    public long count(Function<Object, Long> executor) {
        String cacheKey = CacheKeyGenerator.generateKey(query, "count");
        return cache.getOrCompute(cacheKey, key -> executor.apply(query));
    }

    /**
     * Invalidate cache entry for this query.
     */
    public void invalidateCache() {
        cache.clear();
    }

    /**
     * Get the underlying query object.
     *
     * @return delegate query object
     */
    public Object getQuery() {
        return query;
    }

    /**
     * Get the cache statistics.
     *
     * @return cache statistics
     */
    public QueryResultCache.CacheStatistics getCacheStatistics() {
        return cache.getStatistics();
    }
}
