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

import com.holonplatform.datastore.jpa.internal.audit.QueryAuditLog;
import com.holonplatform.datastore.jpa.internal.audit.SlowQueryDetector;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SlowQueryDetector}.
 *
 * @since 12.0.0
 */
public class TestSlowQueryDetector {

    @Test
    public void testDetectSlowQuery() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        QueryAuditLog slowLog = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u WHERE u.status = :status")
            .parameters(List.of("ACTIVE"))
            .executionTimeMs(600)
            .executedAt(Instant.now())
            .rowsReturned(100)
            .successful(true)
            .operationType("SELECT")
            .build();
        
        assertEquals(0, detector.getSlowQueryCount());
        detector.onQueryExecuted(slowLog);
        assertEquals(1, detector.getSlowQueryCount());
    }

    @Test
    public void testIgnoreFastQuery() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        QueryAuditLog fastLog = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u WHERE u.id = :id")
            .parameters(List.of(1))
            .executionTimeMs(50)
            .executedAt(Instant.now())
            .rowsReturned(1)
            .successful(true)
            .operationType("SELECT")
            .build();
        
        detector.onQueryExecuted(fastLog);
        assertEquals(0, detector.getSlowQueryCount());
    }

    @Test
    public void testMultipleSlowQueries() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        for (int i = 0; i < 5; i++) {
            QueryAuditLog log = QueryAuditLog.builder()
                .jpql("SELECT u FROM User u WHERE u.age > " + i)
                .parameters(List.of(i))
                .executionTimeMs(501 + i * 100)
                .executedAt(Instant.now())
                .rowsReturned(100 - i)
                .successful(true)
                .operationType("SELECT")
                .build();
            
            detector.onQueryExecuted(log);
        }
        
        assertEquals(5, detector.getSlowQueryCount());
        assertTrue(detector.getAverageSlowQueryTime() > 500);
    }

    @Test
    public void testClearHistory() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        QueryAuditLog slowLog = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(600)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        detector.onQueryExecuted(slowLog);
        assertEquals(1, detector.getSlowQueryCount());
        
        detector.clearHistory();
        assertEquals(0, detector.getSlowQueryCount());
    }

    @Test
    public void testGetStatistics() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        QueryAuditLog log1 = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(600)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        QueryAuditLog log2 = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u WHERE u.status = :status")
            .parameters(List.of("ACTIVE"))
            .executionTimeMs(1000)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        detector.onQueryExecuted(log1);
        detector.onQueryExecuted(log2);
        
        String stats = detector.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("2"));
        assertTrue(stats.contains("800.00"));
        assertTrue(stats.contains("1000"));
    }

    @Test
    public void testMaxSlowQueryTime() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        for (long time : new long[]{600, 1000, 750, 800}) {
            QueryAuditLog log = QueryAuditLog.builder()
                .jpql("SELECT u FROM User u")
                .executionTimeMs(time)
                .executedAt(Instant.now())
                .successful(true)
                .operationType("SELECT")
                .build();
            detector.onQueryExecuted(log);
        }
        
        assertEquals(1000, detector.getMaxSlowQueryTime());
    }

    @Test
    public void testBoundedSlowQueryList() {
        SlowQueryDetector detector = new SlowQueryDetector(500, true, 3);
        
        for (int i = 0; i < 5; i++) {
            QueryAuditLog log = QueryAuditLog.builder()
                .jpql("SELECT u FROM User u WHERE id = " + i)
                .executionTimeMs(600 + i * 100)
                .executedAt(Instant.now())
                .successful(true)
                .operationType("SELECT")
                .build();
            detector.onQueryExecuted(log);
        }
        
        assertEquals(3, detector.getSlowQueryCount());
    }

    @Test
    public void testGetSlowQueries() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(600)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        detector.onQueryExecuted(log);
        
        List<QueryAuditLog> queries = detector.getSlowQueries();
        assertEquals(1, queries.size());
        assertEquals("SELECT u FROM User u", queries.get(0).jpql());
        
        // Verify immutability
        assertThrows(UnsupportedOperationException.class, () -> 
            queries.add(log)
        );
    }

    @Test
    public void testThrowsOnNullAuditLog() {
        SlowQueryDetector detector = new SlowQueryDetector(500);
        assertThrows(NullPointerException.class, () ->
            detector.onQueryExecuted(null)
        );
    }
}
