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

import com.holonplatform.datastore.jpa.internal.patterns.PatternMatcher;
import com.holonplatform.datastore.jpa.internal.patterns.QueryCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PatternMatcher}.
 *
 * @since 12.0.0
 */
public class TestPatternMatcher {

    @Test
    public void testMatchQueryValue() {
        QueryCondition.QueryValue qv = new QueryCondition.QueryValue("test");
        assertTrue(PatternMatcher.matches(qv, "test"));
        assertFalse(PatternMatcher.matches(qv, "other"));
        assertFalse(PatternMatcher.matches(qv, null));
    }

    @Test
    public void testMatchEquals() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "name", QueryCondition.ComparisonOp.EQUALS, "John"
        );
        assertTrue(PatternMatcher.matches(qc, "John"));
        assertFalse(PatternMatcher.matches(qc, "Jane"));
        assertFalse(PatternMatcher.matches(qc, null));
    }

    @Test
    public void testMatchNotEquals() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "status", QueryCondition.ComparisonOp.NOT_EQUALS, "inactive"
        );
        assertTrue(PatternMatcher.matches(qc, "active"));
        assertTrue(PatternMatcher.matches(qc, null));
        assertFalse(PatternMatcher.matches(qc, "inactive"));
    }

    @Test
    public void testMatchGreaterThan() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.GREATER_THAN, 18
        );
        assertTrue(PatternMatcher.matches(qc, 25));
        assertFalse(PatternMatcher.matches(qc, 18));
        assertFalse(PatternMatcher.matches(qc, 10));
    }

    @Test
    public void testMatchLessThan() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.LESS_THAN, 65
        );
        assertTrue(PatternMatcher.matches(qc, 50));
        assertFalse(PatternMatcher.matches(qc, 65));
        assertFalse(PatternMatcher.matches(qc, 70));
    }

    @Test
    public void testMatchGreaterThanOrEqual() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "salary", QueryCondition.ComparisonOp.GREATER_THAN_OR_EQUAL, 50000
        );
        assertTrue(PatternMatcher.matches(qc, 50000));
        assertTrue(PatternMatcher.matches(qc, 75000));
        assertFalse(PatternMatcher.matches(qc, 40000));
    }

    @Test
    public void testMatchLessThanOrEqual() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "salary", QueryCondition.ComparisonOp.LESS_THAN_OR_EQUAL, 100000
        );
        assertTrue(PatternMatcher.matches(qc, 100000));
        assertTrue(PatternMatcher.matches(qc, 75000));
        assertFalse(PatternMatcher.matches(qc, 120000));
    }

    @Test
    public void testMatchIn() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "department", QueryCondition.ComparisonOp.IN, List.of("IT", "HR", "Finance")
        );
        assertTrue(PatternMatcher.matches(qc, "IT"));
        assertTrue(PatternMatcher.matches(qc, "HR"));
        assertFalse(PatternMatcher.matches(qc, "Sales"));
        assertFalse(PatternMatcher.matches(qc, null));
    }

    @Test
    public void testMatchNotIn() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "status", QueryCondition.ComparisonOp.NOT_IN, List.of("deleted", "archived")
        );
        assertTrue(PatternMatcher.matches(qc, "active"));
        assertTrue(PatternMatcher.matches(qc, "pending"));
        assertFalse(PatternMatcher.matches(qc, "deleted"));
        assertFalse(PatternMatcher.matches(qc, null));
    }

    @Test
    public void testMatchLike() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "email", QueryCondition.ComparisonOp.LIKE, "%@company.com"
        );
        assertTrue(PatternMatcher.matches(qc, "john@company.com"));
        assertTrue(PatternMatcher.matches(qc, "jane@company.com"));
        assertFalse(PatternMatcher.matches(qc, "john@other.com"));
    }

    @Test
    public void testMatchLikeWithUnderscores() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "code", QueryCondition.ComparisonOp.LIKE, "A_B%"
        );
        assertTrue(PatternMatcher.matches(qc, "A1B2345"));
        assertTrue(PatternMatcher.matches(qc, "AXB"));
        assertFalse(PatternMatcher.matches(qc, "AB123"));
    }

    @Test
    public void testMatchIsNull() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "middleName", QueryCondition.ComparisonOp.IS_NULL, null
        );
        assertTrue(PatternMatcher.matches(qc, null));
        assertFalse(PatternMatcher.matches(qc, "James"));
    }

    @Test
    public void testMatchIsNotNull() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "email", QueryCondition.ComparisonOp.IS_NOT_NULL, null
        );
        assertTrue(PatternMatcher.matches(qc, "john@example.com"));
        assertFalse(PatternMatcher.matches(qc, null));
    }

    @Test
    public void testIsValid() {
        assertTrue(PatternMatcher.isValid(
            new QueryCondition.QueryValue("test")
        ));
        assertFalse(PatternMatcher.isValid(
            new QueryCondition.QueryValue(null)
        ));

        assertTrue(PatternMatcher.isValid(
            new QueryCondition.QueryComparison("age", QueryCondition.ComparisonOp.EQUALS, 25)
        ));
        assertFalse(PatternMatcher.isValid(
            new QueryCondition.QueryComparison("", QueryCondition.ComparisonOp.EQUALS, 25)
        ));
    }

    @Test
    public void testThrowsOnNullCondition() {
        assertThrows(NullPointerException.class, () ->
            PatternMatcher.matches(null, "value")
        );
        assertThrows(NullPointerException.class, () ->
            PatternMatcher.isValid(null)
        );
    }

    @Test
    public void testNumericComparisons() {
        QueryCondition.QueryComparison qc = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.GREATER_THAN, 21
        );
        
        // Test with different numeric types
        assertTrue(PatternMatcher.matches(qc, 25));
        assertTrue(PatternMatcher.matches(qc, 25L));
        assertTrue(PatternMatcher.matches(qc, 25.5));
    }

    @Test
    public void testComplexPatternMatching() {
        // Simulate checking multiple conditions
        QueryCondition c1 = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.GREATER_THAN_OR_EQUAL, 18
        );
        QueryCondition c2 = new QueryCondition.QueryComparison(
            "age", QueryCondition.ComparisonOp.LESS_THAN_OR_EQUAL, 65
        );
        QueryCondition c3 = new QueryCondition.QueryComparison(
            "department", QueryCondition.ComparisonOp.IN, 
            List.of("IT", "HR", "Finance")
        );
        
        // User aged 35 in IT
        assertTrue(PatternMatcher.matches(c1, 35));
        assertTrue(PatternMatcher.matches(c2, 35));
        assertTrue(PatternMatcher.matches(c3, "IT"));
    }
}
