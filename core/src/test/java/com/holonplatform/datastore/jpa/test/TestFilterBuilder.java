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

import com.holonplatform.datastore.jpa.internal.patterns.FilterBuilder;
import com.holonplatform.datastore.jpa.internal.patterns.QueryCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FilterBuilder}.
 *
 * @since 12.0.0
 */
public class TestFilterBuilder {

    @Test
    public void testStart() {
        FilterBuilder fb = FilterBuilder.start();
        assertNotNull(fb);
        assertTrue(fb.isEmpty());
    }

    @Test
    public void testEq() {
        FilterBuilder fb = FilterBuilder.start().eq("name", "John");
        assertEquals(1, fb.size());
        QueryCondition qc = fb.getConditions().get(0);
        assertInstanceOf(QueryCondition.QueryComparison.class, qc);
        assertEquals(QueryCondition.ComparisonOp.EQUALS, 
            ((QueryCondition.QueryComparison) qc).op());
    }

    @Test
    public void testNe() {
        FilterBuilder fb = FilterBuilder.start().ne("status", "inactive");
        assertEquals(1, fb.size());
        QueryCondition qc = fb.getConditions().get(0);
        assertInstanceOf(QueryCondition.QueryComparison.class, qc);
        assertEquals(QueryCondition.ComparisonOp.NOT_EQUALS, 
            ((QueryCondition.QueryComparison) qc).op());
    }

    @Test
    public void testGt() {
        FilterBuilder fb = FilterBuilder.start().gt("age", 18);
        assertEquals(1, fb.size());
        QueryCondition qc = fb.getConditions().get(0);
        assertEquals(QueryCondition.ComparisonOp.GREATER_THAN, 
            ((QueryCondition.QueryComparison) qc).op());
    }

    @Test
    public void testLt() {
        FilterBuilder fb = FilterBuilder.start().lt("age", 65);
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.LESS_THAN, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testGte() {
        FilterBuilder fb = FilterBuilder.start().gte("salary", 50000);
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.GREATER_THAN_OR_EQUAL, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testLte() {
        FilterBuilder fb = FilterBuilder.start().lte("salary", 100000);
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.LESS_THAN_OR_EQUAL, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testIn() {
        FilterBuilder fb = FilterBuilder.start().in("department", List.of("IT", "HR"));
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.IN, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testNotIn() {
        FilterBuilder fb = FilterBuilder.start().notIn("status", List.of("deleted", "archived"));
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.NOT_IN, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testLike() {
        FilterBuilder fb = FilterBuilder.start().like("name", "John%");
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.LIKE, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testIsNull() {
        FilterBuilder fb = FilterBuilder.start().isNull("middleName");
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.IS_NULL, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testIsNotNull() {
        FilterBuilder fb = FilterBuilder.start().isNotNull("email");
        assertEquals(1, fb.size());
        assertEquals(QueryCondition.ComparisonOp.IS_NOT_NULL, 
            ((QueryCondition.QueryComparison) fb.getConditions().get(0)).op());
    }

    @Test
    public void testChaining() {
        FilterBuilder fb = FilterBuilder.start()
            .eq("name", "John")
            .gte("age", 18)
            .in("department", List.of("IT", "HR"));
        
        assertEquals(3, fb.size());
    }

    @Test
    public void testBuild() {
        FilterBuilder fb = FilterBuilder.start()
            .eq("name", "John")
            .gte("salary", 50000);
        
        List<QueryCondition> conditions = fb.build();
        assertEquals(2, conditions.size());
    }

    @Test
    public void testGetConditions() {
        FilterBuilder fb = FilterBuilder.start()
            .eq("name", "John")
            .ne("status", "inactive");
        
        List<QueryCondition> conditions = fb.getConditions();
        assertEquals(2, conditions.size());
        
        // Verify immutability
        assertThrows(UnsupportedOperationException.class, () ->
            conditions.add(new QueryCondition.QueryValue("test"))
        );
    }

    @Test
    public void testClear() {
        FilterBuilder fb = FilterBuilder.start()
            .eq("name", "John")
            .gte("age", 18);
        
        assertEquals(2, fb.size());
        
        fb.clear();
        assertEquals(0, fb.size());
        assertTrue(fb.isEmpty());
    }

    @Test
    public void testIsEmpty() {
        FilterBuilder fb = FilterBuilder.start();
        assertTrue(fb.isEmpty());
        
        fb.eq("name", "John");
        assertFalse(fb.isEmpty());
    }

    @Test
    public void testThrowsOnNullProperty() {
        assertThrows(NullPointerException.class, () ->
            FilterBuilder.start().eq(null, "value")
        );
    }

    @Test
    public void testComplexScenario() {
        // Complex filter for user search
        FilterBuilder fb = FilterBuilder.start()
            .like("firstName", "John%")
            .gte("age", 18)
            .lte("age", 65)
            .in("department", List.of("IT", "HR", "Finance"))
            .ne("status", "inactive")
            .isNotNull("email");
        
        assertEquals(6, fb.size());
        List<QueryCondition> conditions = fb.build();
        assertTrue(conditions.stream()
            .allMatch(c -> c.getType().equals("COMPARISON"))
        );
    }
}
