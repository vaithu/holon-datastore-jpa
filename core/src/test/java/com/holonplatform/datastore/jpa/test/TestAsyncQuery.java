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

import com.holonplatform.datastore.jpa.internal.util.AsyncQuery;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AsyncQuery}.
 *
 * @since 12.0.0
 */
public class TestAsyncQuery {

    @Test
    public void testAsyncQueryNotNull() {
        assertThrows(NullPointerException.class, () -> new AsyncQuery(null));
    }

    @Test
    public void testAsyncQueryWithValidQuery() {
        com.holonplatform.core.query.Query mockQuery = createMockQuery();
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        
        assertNotNull(asyncQuery);
        assertNotNull(asyncQuery.getQuery());
    }

    @Test
    public void testCountAsync() throws Exception {
        com.holonplatform.core.query.Query mockQuery = createMockQuery(() -> 10L);
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        
        CompletableFuture<Long> future = asyncQuery.countAsync();
        Long count = future.get();
        
        assertEquals(10L, count);
    }

    @Test
    public void testAsyncNonBlocking() throws Exception {
        com.holonplatform.core.query.Query mockQuery = createMockQuery(() -> 10L);
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        
        long startTime = System.currentTimeMillis();
        CompletableFuture<Long> future = asyncQuery.countAsync();
        long submitTime = System.currentTimeMillis();
        
        // Submit should be instant (< 100ms) - not blocking
        assertTrue(submitTime - startTime < 100, "Async submission should not block");
        
        // Async execution happens in background
        long result = future.get();
        assertEquals(10L, result);
    }

    @Test
    public void testAsyncExceptionHandling() throws Exception {
        com.holonplatform.core.query.Query mockQuery = createMockQueryWithException(
            () -> { throw new RuntimeException("Test error"); }
        );
        
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        CompletableFuture<Long> future = asyncQuery.countAsync();
        
        assertThrows(Exception.class, () -> future.get());
    }

    @Test
    public void testAsyncChaining() throws Exception {
        com.holonplatform.core.query.Query mockQuery = createMockQuery(() -> 10L);
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        
        CompletableFuture<Long> future = asyncQuery.countAsync()
            .thenApply(count -> count * 2);
        
        Long result = future.get();
        assertEquals(20L, result);
    }

    @Test
    public void testAsyncFallback() throws Exception {
        com.holonplatform.core.query.Query mockQuery = createMockQueryWithException(
            () -> { throw new RuntimeException("DB error"); }
        );
        
        AsyncQuery asyncQuery = new AsyncQuery(mockQuery);
        CompletableFuture<Long> future = asyncQuery.countAsync()
            .exceptionally(ex -> 0L);
        
        Long result = future.get();
        assertEquals(0L, result);
    }

    @Test
    public void testAsyncComposition() throws Exception {
        com.holonplatform.core.query.Query mockQuery1 = createMockQuery(() -> 10L);
        com.holonplatform.core.query.Query mockQuery2 = createMockQuery(() -> 5L);
        
        AsyncQuery asyncQuery1 = new AsyncQuery(mockQuery1);
        AsyncQuery asyncQuery2 = new AsyncQuery(mockQuery2);
        
        CompletableFuture<Long> combined = asyncQuery1.countAsync()
            .thenCombine(asyncQuery2.countAsync(), Long::sum);
        
        Long result = combined.get();
        assertEquals(15L, result);
    }

    /**
     * Create a mock Query using dynamic proxy.
     */
    private com.holonplatform.core.query.Query createMockQuery() {
        return createMockQuery(() -> 0L);
    }

    /**
     * Create a mock Query that returns a specific count.
     */
    private com.holonplatform.core.query.Query createMockQuery(CountProvider countProvider) {
        return (com.holonplatform.core.query.Query) Proxy.newProxyInstance(
            com.holonplatform.core.query.Query.class.getClassLoader(),
            new Class[]{com.holonplatform.core.query.Query.class},
            (proxy, method, args) -> {
                if (method.getName().equals("count")) {
                    return countProvider.getCount();
                }
                return null;
            }
        );
    }

    /**
     * Create a mock Query that throws an exception on count().
     */
    private com.holonplatform.core.query.Query createMockQueryWithException(ExceptionProvider exProvider) {
        return (com.holonplatform.core.query.Query) Proxy.newProxyInstance(
            com.holonplatform.core.query.Query.class.getClassLoader(),
            new Class[]{com.holonplatform.core.query.Query.class},
            (proxy, method, args) -> {
                if (method.getName().equals("count")) {
                    exProvider.throwException();
                    return 0L;
                }
                return null;
            }
        );
    }

    @FunctionalInterface
    interface CountProvider {
        long getCount();
    }

    @FunctionalInterface
    interface ExceptionProvider {
        void throwException() throws Exception;
    }
}

