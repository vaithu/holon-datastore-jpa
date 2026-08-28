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

/**
 * Fluent builder for query result cache configuration.
 * Provides a convenient API to configure cache settings.
 *
 * @since 12.0.0
 */
public class QueryCacheBuilder {

    private int maxSize = 1000;
    private long ttlMillis = 5 * 60 * 1000; // 5 minutes

    /**
     * Create a new cache builder with default settings.
     *
     * @return new builder instance
     */
    public static QueryCacheBuilder builder() {
        return new QueryCacheBuilder();
    }

    /**
     * Set maximum cache size.
     *
     * @param maxSize maximum number of entries (default 1000)
     * @return this builder
     */
    public QueryCacheBuilder maxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Set time-to-live for cache entries.
     *
     * @param ttlMillis TTL in milliseconds (default 5 minutes)
     * @return this builder
     */
    public QueryCacheBuilder ttlMillis(long ttlMillis) {
        this.ttlMillis = ttlMillis;
        return this;
    }

    /**
     * Set time-to-live for cache entries in seconds.
     *
     * @param ttlSeconds TTL in seconds
     * @return this builder
     */
    public QueryCacheBuilder ttlSeconds(long ttlSeconds) {
        this.ttlMillis = ttlSeconds * 1000;
        return this;
    }

    /**
     * Set time-to-live for cache entries in minutes.
     *
     * @param ttlMinutes TTL in minutes
     * @return this builder
     */
    public QueryCacheBuilder ttlMinutes(long ttlMinutes) {
        this.ttlMillis = ttlMinutes * 60 * 1000;
        return this;
    }

    /**
     * Build the cache instance.
     *
     * @return configured QueryResultCache
     */
    public QueryResultCache build() {
        return new QueryResultCache(maxSize, ttlMillis);
    }

    /**
     * Get current max size setting.
     *
     * @return max size
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get current TTL setting in milliseconds.
     *
     * @return TTL in milliseconds
     */
    public long getTtlMillis() {
        return ttlMillis;
    }
}
