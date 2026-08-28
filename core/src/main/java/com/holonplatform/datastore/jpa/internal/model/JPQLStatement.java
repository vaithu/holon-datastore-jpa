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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Record for JPQL/HQL statement representation.
 * Immutable, type-safe holder for query string and bind parameters.
 * Automatically generates constructor, equals, hashCode, toString.
 *
 * @since 12.0.0
 */
public record JPQLStatement(String jpql, List<Object> parameters) {
    
    /**
     * Constructor with validation.
     *
     * @param jpql the JPQL query string (non-null, non-blank)
     * @param parameters the bind parameters (nullable, defaults to empty)
     */
    public JPQLStatement {
        Objects.requireNonNull(jpql, "JPQL cannot be null");
        if (jpql.isBlank()) {
            throw new IllegalArgumentException("JPQL cannot be blank");
        }
        if (parameters == null) {
            parameters = Collections.emptyList();
        }
    }
    
    /**
     * Check if statement has bind parameters.
     *
     * @return true if parameters list is not empty
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    
    /**
     * Get parameter count.
     *
     * @return number of parameters
     */
    public int getParameterCount() {
        return parameters.size();
    }
}
