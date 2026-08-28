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

import com.holonplatform.datastore.jpa.internal.cache.QueryResultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QueryResultCache}.
 *
 * @since 12.0.0
 */
public class TestQueryResultCache {

    private QueryResultCache cache;

    @BeforeEach
    public void setup() {
        cache = new QueryResultCache(100, 5000); // 100 max, 5 second TTL
    }

    @Test
    public void testCachePutAndGet() {
        cache.put("key1", "value1");
        Optional<String> result = cache.get("key1");
        
        assertTrue(result.isPresent());
        assertEquals("value1", result.get());
    }

    @Test
    public void testCacheHitIncrement() {
        cache.put("key1", "value1");
        cache.get("key1");
        cache.get("key1");
        
        assertEquals(2L, cache.getStatistics().hits);
    }

    @Test
    public void testCacheMissIncrement() {
        cache.get("nonexistent");
        cache.get("also-nonexistent");
        
        assertEquals(2L, cache.getStatistics().misses);
    }

    @Test
    public void testCacheExpiration() throws InterruptedException {
        cache.put("key1", "value1");
        Thread.sleep(5100); // Wait for TTL to expire
        
        Optional<String> result = cache.get("key1");
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetOrCompute() {
        String result = cache.getOrCompute("key1", key -> "computed-value");
        assertEquals("computed-value", result);
        
        // Second call should use cache
        String cached = cache.getOrCompute("key1", key -> "different-value");
        assertEquals("computed-value", cached); // Should be cached, not "different-value"
    }

    @Test
    public void testInvalidate() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        
        cache.invalidate("key1");
        
        assertFalse(cache.get("key1").isPresent());
        assertTrue(cache.get("key2").isPresent());
    }

    @Test
    public void testInvalidatePrefix() {
        cache.put("Entity:query1", "result1");
        cache.put("Entity:query2", "result2");
        cache.put("Other:query1", "result3");
        
        cache.invalidatePrefix("Entity:");
        
        assertFalse(cache.get("Entity:query1").isPresent());
        assertFalse(cache.get("Entity:query2").isPresent());
        assertTrue(cache.get("Other:query1").isPresent());
    }

    @Test
    public void testClear() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        
        cache.clear();
        
        assertEquals(0, cache.getStatistics().currentSize);
        assertFalse(cache.get("key1").isPresent());
    }

    @Test
    public void testLRUEviction() {
        QueryResultCache smallCache = new QueryResultCache(3, 30000); // Max 3 entries
        
        smallCache.put("key1", "value1");
        smallCache.put("key2", "value2");
        smallCache.put("key3", "value3");
        smallCache.put("key4", "value4"); // Should evict key1 (least recently used)
        
        assertFalse(smallCache.get("key1").isPresent());
        assertTrue(smallCache.get("key4").isPresent());
        assertEquals(1L, smallCache.getStatistics().evictions);
    }

    @Test
    public void testStatistics() {
        cache.put("key1", "value1");
        cache.get("key1"); // hit
        cache.get("key2"); // miss
        cache.get("key1"); // hit
        
        QueryResultCache.CacheStatistics stats = cache.getStatistics();
        
        assertEquals(2L, stats.hits);
        assertEquals(1L, stats.misses);
        assertEquals(2.0 / 3 * 100, stats.hitRate, 0.01);
        assertEquals(1, stats.currentSize);
    }

    @Test
    public void testResetStatistics() {
        cache.put("key1", "value1");
        cache.get("key1");
        cache.get("nonexistent");
        
        cache.resetStatistics();
        
        QueryResultCache.CacheStatistics stats = cache.getStatistics();
        assertEquals(0L, stats.hits);
        assertEquals(0L, stats.misses);
        assertEquals(0L, stats.evictions);
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.put("key" + index + "_" + j, "value" + index + "_" + j);
                }
            });
        }
        
        for (Thread t : threads) {
            t.start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        // Verify no exceptions and cache is populated
        assertTrue(cache.getStatistics().currentSize > 0);
    }
}
