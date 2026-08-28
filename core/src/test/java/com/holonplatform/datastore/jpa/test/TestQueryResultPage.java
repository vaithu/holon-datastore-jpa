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

import com.holonplatform.datastore.jpa.internal.model.QueryResultPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link QueryResultPage}.
 *
 * @since 12.0.0
 */
public class TestQueryResultPage {

    @Test
    public void testHasMore() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            0,
            20
        );
        assertTrue(page.hasMore());
    }

    @Test
    public void testHasMoreLastPage() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            40,
            1,
            20
        );
        assertFalse(page.hasMore());
    }

    @Test
    public void testGetTotalPages() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            0,
            20
        );
        assertEquals(5, page.getTotalPages());
    }

    @Test
    public void testGetTotalPagesWithRemainder() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            105,
            0,
            20
        );
        assertEquals(6, page.getTotalPages());
    }

    @Test
    public void testIsFirst() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            0,
            20
        );
        assertTrue(page.isFirst());
    }

    @Test
    public void testIsFirstFalse() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            1,
            20
        );
        assertFalse(page.isFirst());
    }

    @Test
    public void testIsLast() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            40,
            1,
            20
        );
        assertTrue(page.isLast());
    }

    @Test
    public void testIsLastFalse() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            0,
            20
        );
        assertFalse(page.isLast());
    }

    @Test
    public void testGetOffset() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            2,
            20
        );
        assertEquals(40L, page.getOffset());
    }

    @Test
    public void testGetOffsetFirstPage() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of("item1", "item2"),
            100,
            0,
            20
        );
        assertEquals(0L, page.getOffset());
    }

    @Test
    public void testEmptyPage() {
        QueryResultPage<String> page = new QueryResultPage<>(
            List.of(),
            0,
            0,
            20
        );
        assertEquals(0, page.getTotalPages());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
        assertFalse(page.hasMore());
    }

    @Test
    public void testSingleItemPage() {
        QueryResultPage<Integer> page = new QueryResultPage<>(
            List.of(42),
            1,
            0,
            20
        );
        assertEquals(1, page.getTotalPages());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
        assertFalse(page.hasMore());
    }
}
