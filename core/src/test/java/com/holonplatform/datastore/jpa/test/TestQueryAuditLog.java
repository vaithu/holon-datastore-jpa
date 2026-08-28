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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QueryAuditLog}.
 *
 * @since 12.0.0
 */
public class TestQueryAuditLog {

    @Test
    public void testBuilder() {
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u WHERE u.status = :status")
            .parameters(List.of("ACTIVE"))
            .executionTimeMs(50)
            .executedAt(Instant.now())
            .rowsAffected(0)
            .rowsReturned(100)
            .successful(true)
            .operationType("SELECT")
            .build();
        
        assertEquals("SELECT u FROM User u WHERE u.status = :status", log.jpql());
        assertEquals(1, log.parameters().size());
        assertEquals(50, log.executionTimeMs());
        assertEquals(100, log.rowsReturned());
        assertTrue(log.successful());
        assertEquals("SELECT", log.operationType());
    }

    @Test
    public void testIsSlowQuery() {
        QueryAuditLog slowLog = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(600)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        assertTrue(slowLog.isSlowQuery(500));
        assertFalse(slowLog.isSlowQuery(700));
    }

    @Test
    public void testDefaultSuccessful() {
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executedAt(Instant.now())
            .operationType("SELECT")
            .build();
        
        assertTrue(log.successful());
    }

    @Test
    public void testFailedQuery() {
        QueryAuditLog failedLog = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(100)
            .executedAt(Instant.now())
            .successful(false)
            .errorMessage("Syntax error in JPQL")
            .operationType("SELECT")
            .build();
        
        assertFalse(failedLog.successful());
        assertEquals("Syntax error in JPQL", failedLog.errorMessage());
    }

    @Test
    public void testParametersUnmodifiable() {
        List<Object> params = List.of("ACTIVE", 10);
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .parameters(params)
            .executedAt(Instant.now())
            .operationType("SELECT")
            .build();
        
        assertThrows(UnsupportedOperationException.class, () ->
            log.parameters().add("INACTIVE")
        );
    }

    @Test
    public void testThrowsOnNullJpql() {
        assertThrows(NullPointerException.class, () ->
            new QueryAuditLog(null, List.of(), 50, Instant.now(), 0, 0, true, null, "SELECT")
        );
    }

    @Test
    public void testThrowsOnNullExecutedAt() {
        assertThrows(NullPointerException.class, () ->
            new QueryAuditLog("SELECT u FROM User u", List.of(), 50, null, 0, 0, true, null, "SELECT")
        );
    }

    @Test
    public void testThrowsOnNullOperationType() {
        assertThrows(NullPointerException.class, () ->
            new QueryAuditLog("SELECT u FROM User u", List.of(), 50, Instant.now(), 0, 0, true, null, null)
        );
    }

    @Test
    public void testUpdateQuery() {
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("UPDATE User u SET u.status = :status WHERE u.id = :id")
            .parameters(List.of("INACTIVE", 1))
            .executionTimeMs(25)
            .executedAt(Instant.now())
            .rowsAffected(1)
            .rowsReturned(0)
            .successful(true)
            .operationType("UPDATE")
            .build();
        
        assertEquals("UPDATE", log.operationType());
        assertEquals(1, log.rowsAffected());
        assertEquals(0, log.rowsReturned());
    }

    @Test
    public void testToString() {
        QueryAuditLog log = QueryAuditLog.builder()
            .jpql("SELECT u FROM User u")
            .executionTimeMs(100)
            .executedAt(Instant.now())
            .successful(true)
            .operationType("SELECT")
            .build();
        
        String str = log.toString();
        assertNotNull(str);
        assertTrue(str.contains("QueryAuditLog"));
        assertTrue(str.contains("100"));
    }
}
