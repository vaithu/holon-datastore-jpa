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

/**
 * Cache invalidator for mutation operations.
 * Tracks cache invalidations and can clear cache by entity/table prefix.
 *
 * @since 12.0.0
 */
public class CachingInterceptor {

    private final QueryResultCache cache;
    private final Set<String> invalidationTargets = Collections.synchronizedSet(new HashSet<>());

    /**
     * Create a caching interceptor for the given cache.
     *
     * @param cache the query result cache
     */
    public CachingInterceptor(QueryResultCache cache) {
        this.cache = Objects.requireNonNull(cache, "Cache must not be null");
    }

    /**
     * Clear entire cache (call on any mutation).
     */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Invalidate cache entries by prefix.
     * Used when an entity/table is modified (insert/update/delete).
     *
     * @param entityPrefix entity/table prefix to match
     */
    public void invalidateEntity(String entityPrefix) {
        cache.invalidatePrefix(entityPrefix);
        invalidationTargets.add(entityPrefix);
    }

    /**
     * Get targets that have been invalidated since last reset.
     *
     * @return set of invalidated entity prefixes
     */
    public Set<String> getInvalidatedEntities() {
        return new HashSet<>(invalidationTargets);
    }

    /**
     * Reset invalidation tracking.
     */
    public void resetTracking() {
        invalidationTargets.clear();
    }

    /**
     * Get underlying cache.
     *
     * @return the query result cache
     */
    public QueryResultCache getCache() {
        return cache;
    }
}
