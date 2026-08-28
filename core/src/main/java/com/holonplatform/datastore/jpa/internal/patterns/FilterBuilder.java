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
package com.holonplatform.datastore.jpa.internal.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Type-safe builder for creating query filter conditions.
 * Provides fluent API for building complex filters with sealed types.
 *
 * Supports dynamic WHERE clause construction for REST APIs, search endpoints,
 * and advanced query scenarios.
 *
 * Example:
 * <pre>
 * FilterBuilder.start()
 *     .eq("name", "John")
 *     .gte("salary", 50000)
 *     .in("department", List.of("IT", "HR", "Finance"))
 *     .like("email", "%@company.com")
 *     .build();
 * </pre>
 *
 * @since 12.0.0
 */
public class FilterBuilder {

    private final List<QueryCondition> conditions = new ArrayList<>();

    private FilterBuilder() {
    }

    /**
     * Start building a new filter.
     *
     * @return FilterBuilder instance
     */
    public static FilterBuilder start() {
        return new FilterBuilder();
    }

    /**
     * Add equals condition.
     *
     * @param property the property name
     * @param value the expected value
     * @return this builder for chaining
     */
    public FilterBuilder eq(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.EQUALS,
            value
        ));
        return this;
    }

    /**
     * Add not equals condition.
     *
     * @param property the property name
     * @param value the unexpected value
     * @return this builder for chaining
     */
    public FilterBuilder ne(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.NOT_EQUALS,
            value
        ));
        return this;
    }

    /**
     * Add greater than condition.
     *
     * @param property the property name
     * @param value the threshold value
     * @return this builder for chaining
     */
    public FilterBuilder gt(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.GREATER_THAN,
            value
        ));
        return this;
    }

    /**
     * Add less than condition.
     *
     * @param property the property name
     * @param value the threshold value
     * @return this builder for chaining
     */
    public FilterBuilder lt(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.LESS_THAN,
            value
        ));
        return this;
    }

    /**
     * Add greater than or equal condition.
     *
     * @param property the property name
     * @param value the threshold value
     * @return this builder for chaining
     */
    public FilterBuilder gte(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.GREATER_THAN_OR_EQUAL,
            value
        ));
        return this;
    }

    /**
     * Add less than or equal condition.
     *
     * @param property the property name
     * @param value the threshold value
     * @return this builder for chaining
     */
    public FilterBuilder lte(String property, Object value) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.LESS_THAN_OR_EQUAL,
            value
        ));
        return this;
    }

    /**
     * Add IN condition for collection matching.
     *
     * @param property the property name
     * @param values the collection of values to match
     * @return this builder for chaining
     */
    public FilterBuilder in(String property, Collection<?> values) {
        Objects.requireNonNull(property, "Property must not be null");
        Objects.requireNonNull(values, "Values must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.IN,
            values
        ));
        return this;
    }

    /**
     * Add NOT IN condition for exclusion matching.
     *
     * @param property the property name
     * @param values the collection of values to exclude
     * @return this builder for chaining
     */
    public FilterBuilder notIn(String property, Collection<?> values) {
        Objects.requireNonNull(property, "Property must not be null");
        Objects.requireNonNull(values, "Values must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.NOT_IN,
            values
        ));
        return this;
    }

    /**
     * Add LIKE condition for pattern matching (supports % and _ wildcards).
     *
     * @param property the property name
     * @param pattern the pattern with % (multiple chars) and _ (single char) wildcards
     * @return this builder for chaining
     */
    public FilterBuilder like(String property, String pattern) {
        Objects.requireNonNull(property, "Property must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.LIKE,
            pattern
        ));
        return this;
    }

    /**
     * Add IS NULL condition.
     *
     * @param property the property name
     * @return this builder for chaining
     */
    public FilterBuilder isNull(String property) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.IS_NULL,
            null
        ));
        return this;
    }

    /**
     * Add IS NOT NULL condition.
     *
     * @param property the property name
     * @return this builder for chaining
     */
    public FilterBuilder isNotNull(String property) {
        Objects.requireNonNull(property, "Property must not be null");
        conditions.add(new QueryCondition.QueryComparison(
            property,
            QueryCondition.ComparisonOp.IS_NOT_NULL,
            null
        ));
        return this;
    }

    /**
     * Add a custom condition (advanced usage).
     *
     * @param condition the condition to add
     * @return this builder for chaining
     */
    public FilterBuilder add(QueryCondition condition) {
        Objects.requireNonNull(condition, "Condition must not be null");
        conditions.add(condition);
        return this;
    }

    /**
     * Get all conditions added to this builder.
     *
     * @return unmodifiable list of conditions
     */
    public List<QueryCondition> getConditions() {
        return List.copyOf(conditions);
    }

    /**
     * Build and return all conditions.
     *
     * @return list of conditions (unmodifiable)
     */
    public List<QueryCondition> build() {
        return List.copyOf(conditions);
    }

    /**
     * Get the number of conditions in this filter.
     *
     * @return condition count
     */
    public int size() {
        return conditions.size();
    }

    /**
     * Check if this filter is empty.
     *
     * @return true if no conditions added
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }

    /**
     * Clear all conditions.
     *
     * @return this builder for chaining
     */
    public FilterBuilder clear() {
        conditions.clear();
        return this;
    }

    @Override
    public String toString() {
        return "FilterBuilder{" +
                "conditions=" + conditions +
                '}';
    }
}
