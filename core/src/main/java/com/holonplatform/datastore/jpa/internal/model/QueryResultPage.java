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
package com.holonplatform.datastore.jpa.internal.model;

import java.util.List;

/**
 * Record for paginated query results.
 * Type-safe, immutable representation of result pages with utility methods.
 *
 * @param <T> the result type
 * @since 12.0.0
 */
public record QueryResultPage<T>(
    List<T> items,
    long total,
    int pageNumber,
    int pageSize
) {
    
    /**
     * Check if more results exist.
     *
     * @return true if there are more pages after current
     */
    public boolean hasMore() {
        return (long)(pageNumber + 1) * pageSize < total;
    }
    
    /**
     * Get total number of pages.
     *
     * @return total page count
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) total / pageSize);
    }
    
    /**
     * Check if this is the first page.
     *
     * @return true if pageNumber is 0
     */
    public boolean isFirst() {
        return pageNumber == 0;
    }
    
    /**
     * Check if this is the last page.
     *
     * @return true if no more pages exist
     */
    public boolean isLast() {
        return !hasMore();
    }
    
    /**
     * Get current offset (for OFFSET/LIMIT).
     *
     * @return offset for this page
     */
    public long getOffset() {
        return (long) pageNumber * pageSize;
    }
}
