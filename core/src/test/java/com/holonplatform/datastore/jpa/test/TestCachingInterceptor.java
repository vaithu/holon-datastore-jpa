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

import com.holonplatform.datastore.jpa.internal.cache.CachingInterceptor;
import com.holonplatform.datastore.jpa.internal.cache.QueryResultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CachingInterceptor}.
 *
 * @since 12.0.0
 */
public class TestCachingInterceptor {

    private CachingInterceptor interceptor;
    private QueryResultCache cache;

    @BeforeEach
    public void setup() {
        cache = new QueryResultCache(100, 5000);
        interceptor = new CachingInterceptor(cache);
    }

    @Test
    public void testInvalidateAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        
        interceptor.invalidateAll();
        
        assertFalse(cache.get("key1").isPresent());
        assertFalse(cache.get("key2").isPresent());
    }

    @Test
    public void testInvalidateEntity() {
        cache.put("User:query1", "result1");
        cache.put("User:query2", "result2");
        cache.put("Order:query1", "result3");
        
        interceptor.invalidateEntity("User:");
        
        assertFalse(cache.get("User:query1").isPresent());
        assertFalse(cache.get("User:query2").isPresent());
        assertTrue(cache.get("Order:query1").isPresent());
    }

    @Test
    public void testTrackInvalidatedEntities() {
        interceptor.invalidateEntity("User:");
        interceptor.invalidateEntity("Order:");
        
        assertTrue(interceptor.getInvalidatedEntities().contains("User:"));
        assertTrue(interceptor.getInvalidatedEntities().contains("Order:"));
        assertEquals(2, interceptor.getInvalidatedEntities().size());
    }

    @Test
    public void testResetTracking() {
        interceptor.invalidateEntity("User:");
        interceptor.invalidateEntity("Order:");
        
        interceptor.resetTracking();
        
        assertTrue(interceptor.getInvalidatedEntities().isEmpty());
    }

    @Test
    public void testGetUnderlyingCache() {
        assertSame(cache, interceptor.getCache());
    }
}
