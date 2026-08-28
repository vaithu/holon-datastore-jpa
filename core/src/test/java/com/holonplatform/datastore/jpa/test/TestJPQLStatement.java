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

import com.holonplatform.datastore.jpa.internal.model.JPQLStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JPQLStatement}.
 *
 * @since 12.0.0
 */
public class TestJPQLStatement {

    @Test
    public void testCreateWithoutParameters() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT p FROM Product p WHERE p.active = true",
            List.of()
        );
        assertEquals("SELECT p FROM Product p WHERE p.active = true", stmt.jpql());
        assertEquals(0, stmt.getParameterCount());
        assertFalse(stmt.hasParameters());
    }

    @Test
    public void testCreateWithParameters() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT p FROM Product p WHERE p.name = :name AND p.price > :price",
            List.of("Laptop", 500.0)
        );
        assertEquals(2, stmt.getParameterCount());
        assertTrue(stmt.hasParameters());
    }

    @Test
    public void testCreateWithNullParameters() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT p FROM Product p",
            null
        );
        assertFalse(stmt.hasParameters());
        assertEquals(0, stmt.getParameterCount());
    }

    @Test
    public void testThrowsOnNullJPQL() {
        assertThrows(NullPointerException.class, () ->
            new JPQLStatement(null, List.of())
        );
    }

    @Test
    public void testThrowsOnBlankJPQL() {
        assertThrows(IllegalArgumentException.class, () ->
            new JPQLStatement("   ", List.of())
        );
    }

    @Test
    public void testParameterCount() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT u FROM User u WHERE u.firstName = ? AND u.lastName = ? AND u.age > ?",
            List.of("John", "Doe", 18)
        );
        assertEquals(3, stmt.getParameterCount());
    }

    @Test
    public void testRecordEquality() {
        JPQLStatement stmt1 = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of()
        );
        JPQLStatement stmt2 = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of()
        );
        assertEquals(stmt1, stmt2);
    }

    @Test
    public void testRecordInequality() {
        JPQLStatement stmt1 = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of()
        );
        JPQLStatement stmt2 = new JPQLStatement(
            "SELECT p FROM Product p WHERE p.active = true",
            List.of()
        );
        assertNotEquals(stmt1, stmt2);
    }

    @Test
    public void testRecordHashCode() {
        JPQLStatement stmt1 = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of(1)
        );
        JPQLStatement stmt2 = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of(1)
        );
        assertEquals(stmt1.hashCode(), stmt2.hashCode());
    }

    @Test
    public void testRecordToString() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT p FROM Product p",
            List.of(1, "test")
        );
        String str = stmt.toString();
        assertNotNull(str);
        assertTrue(str.contains("JPQLStatement"));
    }

    @Test
    public void testComplexQuery() {
        JPQLStatement stmt = new JPQLStatement(
            "SELECT u FROM User u " +
            "JOIN FETCH u.profile p " +
            "WHERE u.status = :status AND u.createdDate > :date " +
            "ORDER BY u.createdDate DESC",
            List.of("ACTIVE", java.time.LocalDate.now())
        );
        assertEquals(2, stmt.getParameterCount());
        assertTrue(stmt.hasParameters());
    }
}
