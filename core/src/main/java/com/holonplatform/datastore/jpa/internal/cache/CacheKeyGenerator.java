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
 * Cache key generator for datastore queries.
 * Generates deterministic cache keys from query objects using identity hash.
 *
 * @since 12.0.0
 */
public class CacheKeyGenerator {

    private CacheKeyGenerator() {
    }

    /**
     * Generate cache key from query object.
     * Uses object identity hash to create a unique key.
     *
     * @param query the query object
     * @return cache key string
     */
    public static String generateKey(Object query) {
        StringBuilder sb = new StringBuilder();
        sb.append("query:");
        sb.append(System.identityHashCode(query));
        return sb.toString();
    }

    /**
     * Generate cache key with custom suffix.
     *
     * @param query the query object
     * @param suffix custom suffix (e.g., "list", "count", "findOne")
     * @return cache key string
     */
    public static String generateKey(Object query, String suffix) {
        if (suffix == null) {
            return generateKey(query);
        }
        return generateKey(query) + ":" + suffix;
    }
}
