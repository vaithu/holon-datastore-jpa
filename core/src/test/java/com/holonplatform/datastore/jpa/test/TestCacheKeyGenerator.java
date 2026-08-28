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

import com.holonplatform.datastore.jpa.internal.cache.CacheKeyGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CacheKeyGenerator}.
 *
 * @since 12.0.0
 */
public class TestCacheKeyGenerator {

    @Test
    public void testGenerateKey() {
        String key = CacheKeyGenerator.generateKey(new Object());
        
        assertNotNull(key);
        assertTrue(key.startsWith("query:"));
    }

    @Test
    public void testGenerateKeyWithSuffix() {
        String key = CacheKeyGenerator.generateKey(new Object(), "list");
        
        assertNotNull(key);
        assertTrue(key.startsWith("query:"));
        assertTrue(key.endsWith(":list"));
    }

    @Test
    public void testDeterministicGeneration() {
        Object query = new Object();
        String key1 = CacheKeyGenerator.generateKey(query);
        String key2 = CacheKeyGenerator.generateKey(query);
        
        assertEquals(key1, key2);
    }

    @Test
    public void testDifferentQueriesProduceDifferentKeys() {
        Object query1 = new Object();
        Object query2 = new Object();
        
        String key1 = CacheKeyGenerator.generateKey(query1);
        String key2 = CacheKeyGenerator.generateKey(query2);
        
        assertNotEquals(key1, key2);
    }

    @Test
    public void testSuffixVariation() {
        Object query = new Object();
        
        String keyList = CacheKeyGenerator.generateKey(query, "list");
        String keyCount = CacheKeyGenerator.generateKey(query, "count");
        String keyExecute = CacheKeyGenerator.generateKey(query, "execute");
        
        assertNotEquals(keyList, keyCount);
        assertNotEquals(keyCount, keyExecute);
        assertNotEquals(keyList, keyExecute);
    }

    @Test
    public void testNullSuffix() {
        Object query = new Object();
        String key1 = CacheKeyGenerator.generateKey(query, null);
        String key2 = CacheKeyGenerator.generateKey(query);
        
        assertEquals(key1, key2);
    }

    @Test
    public void testKeyFormat() {
        String key = CacheKeyGenerator.generateKey(new Object(), "test");
        assertTrue(key.matches("query:\\d+:test"));
    }
}
