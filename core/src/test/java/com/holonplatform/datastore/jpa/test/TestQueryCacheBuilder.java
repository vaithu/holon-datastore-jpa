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
package com.holonplatform.datastore.jpa.test;

import com.holonplatform.datastore.jpa.internal.cache.QueryCacheBuilder;
import com.holonplatform.datastore.jpa.internal.cache.QueryResultCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QueryCacheBuilder}.
 *
 * @since 12.0.0
 */
public class TestQueryCacheBuilder {

    @Test
    public void testBuilderDefaults() {
        QueryResultCache cache = QueryCacheBuilder.builder().build();
        
        assertNotNull(cache);
        // Default TTL is 5 minutes (300000 ms)
        QueryResultCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(0, stats.currentSize);
    }

    @Test
    public void testMaxSize() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .maxSize(5000)
            .build();
        
        assertNotNull(cache);
        // Verify by adding entries up to near max
        for (int i = 0; i < 5000; i++) {
            cache.put("key" + i, "value" + i);
        }
        
        assertTrue(cache.getStatistics().currentSize <= 5000);
    }

    @Test
    public void testTtlMillis() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .ttlMillis(10000)
            .build();
        
        assertNotNull(cache);
        // Cache will have 10 second TTL
        cache.put("key", "value");
        assertTrue(cache.get("key").isPresent());
    }

    @Test
    public void testTtlSeconds() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .ttlSeconds(30)
            .build();
        
        assertNotNull(cache);
        cache.put("key", "value");
        assertTrue(cache.get("key").isPresent());
    }

    @Test
    public void testTtlMinutes() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .ttlMinutes(15)
            .build();
        
        assertNotNull(cache);
        cache.put("key", "value");
        assertTrue(cache.get("key").isPresent());
    }

    @Test
    public void testFluentConfiguration() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .maxSize(2000)
            .ttlMinutes(10)
            .build();
        
        assertNotNull(cache);
        for (int i = 0; i < 100; i++) {
            cache.put("key" + i, "value" + i);
        }
        assertEquals(100, cache.getStatistics().currentSize);
    }

    @Test
    public void testMultipleBuilders() {
        QueryResultCache cache1 = QueryCacheBuilder.builder()
            .maxSize(1000)
            .ttlSeconds(60)
            .build();
        
        QueryResultCache cache2 = QueryCacheBuilder.builder()
            .maxSize(5000)
            .ttlMinutes(10)
            .build();
        
        assertNotNull(cache1);
        assertNotNull(cache2);
        assertNotSame(cache1, cache2);
    }

    @Test
    public void testBuilderChaining() {
        QueryResultCache cache = QueryCacheBuilder.builder()
            .ttlSeconds(30)
            .maxSize(500)
            .build();
        
        assertNotNull(cache);
        cache.put("test", "value");
        assertTrue(cache.get("test").isPresent());
    }
}
