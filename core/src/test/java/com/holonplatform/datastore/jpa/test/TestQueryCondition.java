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

import com.holonplatform.datastore.jpa.internal.patterns.QueryCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QueryCondition}.
 *
 * @since 12.0.0
 */
public class TestQueryCondition {

    @Test
    public void testQueryValue() {
        QueryCondition.QueryValue qv = new QueryCondition.QueryValue("test");
        assertEquals("test", qv.value());
        assertEquals("VALUE", qv.getType());
    }

    @Test
    public void testQueryValueNull() {
        QueryCondition.QueryValue qv = new QueryCondition.QueryValue(null);
        assertNull(qv.value());
    }

    @Test
    public void testQueryPredicate() {
        QueryCondition.QueryPredicate qp = new QueryCondition.QueryPredicate("name", "=", "John");
        assertEquals("name", qp.property());
        assertEquals("=", qp.operator());
        assertEquals("John", qp.value());
        assertEquals("PREDICATE", qp.getType());
    }

    @Test
    public void testQueryPredicateThrowsNullProperty() {
        assertThrows(NullPointerException.class, () ->
            new QueryCondition.QueryPredicate(null, "=", "value")
        );
    }

    @Test
    public void testQueryPredicateThrowsNullOperator() {
        assertThrows(NullPointerException.class, () ->
            new QueryCondition.QueryPredicate("name", null, "value")
        );
    }

    @Test
    public void testQueryComparison() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.GREATER_THAN, 18
        );
        assertEquals("age", qc.property());
        assertEquals(QueryCondition.ComparisonOp.GREATER_THAN, qc.op());
        assertEquals(18, qc.value());
        assertEquals("COMPARISON", qc.getType());
    }

    @Test
    public void testQueryComparisonThrowsNullProperty() {
        assertThrows(NullPointerException.class, () ->
            new QueryCondition.QueryComparison(null, QueryCondition.ComparisonOp.EQUALS, "value")
        );
    }

    @Test
    public void testQueryComparisonThrowsNullOp() {
        assertThrows(NullPointerException.class, () ->
            new QueryCondition.QueryComparison("age", null, 18)
        );
    }

    @Test
    public void testComparisonOpSymbols() {
        assertEquals("=", QueryCondition.ComparisonOp.EQUALS.getSymbol());
        assertEquals("!=", QueryCondition.ComparisonOp.NOT_EQUALS.getSymbol());
        assertEquals(">", QueryCondition.ComparisonOp.GREATER_THAN.getSymbol());
        assertEquals("<", QueryCondition.ComparisonOp.LESS_THAN.getSymbol());
        assertEquals(">=", QueryCondition.ComparisonOp.GREATER_THAN_OR_EQUAL.getSymbol());
        assertEquals("<=", QueryCondition.ComparisonOp.LESS_THAN_OR_EQUAL.getSymbol());
        assertEquals("IN", QueryCondition.ComparisonOp.IN.getSymbol());
        assertEquals("NOT IN", QueryCondition.ComparisonOp.NOT_IN.getSymbol());
        assertEquals("LIKE", QueryCondition.ComparisonOp.LIKE.getSymbol());
        assertEquals("IS NULL", QueryCondition.ComparisonOp.IS_NULL.getSymbol());
        assertEquals("IS NOT NULL", QueryCondition.ComparisonOp.IS_NOT_NULL.getSymbol());
    }

    @Test
    public void testToStringFormats() {
        QueryCondition.QueryValue qv = new QueryCondition.QueryValue("test");
        assertTrue(qv.toString().contains("QueryValue"));
        assertTrue(qv.toString().contains("test"));

        QueryCondition.QueryPredicate qp = new QueryCondition.QueryPredicate("name", "=", "John");
        assertTrue(qp.toString().contains("QueryPredicate"));
        assertTrue(qp.toString().contains("name"));

        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.GREATER_THAN, 18
        );
        assertTrue(qc.toString().contains("QueryComparison"));
        assertTrue(qc.toString().contains("age"));
    }
}
