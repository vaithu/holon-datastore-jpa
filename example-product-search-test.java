/*
 * TEST: Product Search with FilterBuilder
 * 
 * Shows how FilterBuilder works with real data
 * You can run this to see LEVEL 5 in action
 */
package com.example.product.test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.internal.patterns.FilterBuilder;

/**
 * LEVEL 5: Dynamic Filtering Examples
 * 
 * This test demonstrates how to use FilterBuilder for
 * type-safe, composable query construction.
 */
@DisplayName("LEVEL 5: Product Search with Dynamic Filtering")
public class ProductSearchFilterTest {

    // Test data setup
    private Datastore datastore;
    private PropertySet PRODUCT_PROPS;

    @BeforeEach
    public void setup() {
        // In real tests, this would be injected
        // For now, showing the pattern
    }

    // ============ BASIC EXAMPLES ============

    @Test
    @DisplayName("Filter by single property: category = 'Electronics'")
    public void testFilterByCategory() {
        // Old way (lots of if statements):
        // if (category != null) {
        //     query = query.filter(CATEGORY.eq(category));
        // }

        // New way (FilterBuilder):
        FilterBuilder filters = FilterBuilder.start()
            .eq("category", "Electronics");

        // In your datastore:
        // List<PropertyBox> results = datastore.query(Product.class)
        //     .filter(filters.build())
        //     .list(PRODUCT_PROPS);
    }

    @Test
    @DisplayName("Filter by price range: 100 <= price <= 500")
    public void testFilterByPriceRange() {
        FilterBuilder filters = FilterBuilder.start()
            .gte("price", new BigDecimal("100"))
            .lte("price", new BigDecimal("500"));

        // Query: SELECT * FROM products WHERE price >= 100 AND price <= 500
    }

    @Test
    @DisplayName("Filter by name (text search): name LIKE '%laptop%'")
    public void testFilterByName() {
        FilterBuilder filters = FilterBuilder.start()
            .like("name", "%laptop%");

        // Query: SELECT * FROM products WHERE name LIKE '%laptop%'
    }

    @Test
    @DisplayName("Filter with multiple conditions (AND logic)")
    public void testMultipleFilters() {
        FilterBuilder filters = FilterBuilder.start()
            .eq("category", "Electronics")
            .gte("price", new BigDecimal("100"))
            .lte("price", new BigDecimal("500"))
            .eq("vendor", "Dell");

        // Query:
        // SELECT * FROM products
        // WHERE category = 'Electronics'
        //   AND price >= 100
        //   AND price <= 500
        //   AND vendor = 'Dell'
    }

    @Test
    @DisplayName("Conditional filters (only if parameter is provided)")
    public void testConditionalFilters() {
        // Simulating REST API parameters
        String categoryParam = "Electronics";
        BigDecimal minPriceParam = new BigDecimal("100");
        String vendorParam = null;  // Not provided

        FilterBuilder filters = FilterBuilder.start();

        if (categoryParam != null) {
            filters.eq("category", categoryParam);
        }

        if (minPriceParam != null) {
            filters.gte("price", minPriceParam);
        }

        if (vendorParam != null) {
            filters.eq("vendor", vendorParam);  // This won't run
        }

        // Result: Only first two filters applied
        // Query: WHERE category = 'Electronics' AND price >= 100
    }

    @Test
    @DisplayName("Filter with IN clause: vendor IN ('Dell', 'HP', 'Lenovo')")
    public void testFilterWithIn() {
        FilterBuilder filters = FilterBuilder.start()
            .in("vendor", List.of("Dell", "HP", "Lenovo"));

        // Query: SELECT * FROM products WHERE vendor IN ('Dell', 'HP', 'Lenovo')
    }

    @Test
    @DisplayName("Filter with NOT EQUALS: status != 'DISCONTINUED'")
    public void testFilterWithNotEquals() {
        FilterBuilder filters = FilterBuilder.start()
            .ne("status", "DISCONTINUED");

        // Query: SELECT * FROM products WHERE status != 'DISCONTINUED'
    }

    @Test
    @DisplayName("Filter with GREATER THAN: quantity > 0 (in stock)")
    public void testFilterInStock() {
        FilterBuilder filters = FilterBuilder.start()
            .gt("quantity", 0);

        // Query: SELECT * FROM products WHERE quantity > 0
    }

    // ============ REAL-WORLD REST SCENARIOS ============

    @Test
    @DisplayName("Scenario 1: User searches for 'laptop' in Electronics under $500")
    public void testScenario1_BasicSearch() {
        String searchName = "laptop";
        String searchCategory = "Electronics";
        BigDecimal maxPrice = new BigDecimal("500");

        FilterBuilder filters = FilterBuilder.start();

        if (searchName != null && !searchName.isEmpty()) {
            filters.like("name", "%" + searchName + "%");
        }
        if (searchCategory != null && !searchCategory.isEmpty()) {
            filters.eq("category", searchCategory);
        }
        if (maxPrice != null) {
            filters.lte("price", maxPrice);
        }

        // Result: Finds all laptops in Electronics under $500
    }

    @Test
    @DisplayName("Scenario 2: Advanced filter with multiple vendors and price range")
    public void testScenario2_AdvancedSearch() {
        FilterBuilder filters = FilterBuilder.start()
            .eq("category", "Computers")
            .gte("price", new BigDecimal("300"))
            .lte("price", new BigDecimal("1500"))
            .in("vendor", List.of("Dell", "HP", "Lenovo"))
            .gt("quantity", 0)  // Only in stock
            .eq("active", true);  // Only published

        // Real query:
        // WHERE category = 'Computers'
        //   AND price >= 300
        //   AND price <= 1500
        //   AND vendor IN ('Dell', 'HP', 'Lenovo')
        //   AND quantity > 0
        //   AND active = true
    }

    @Test
    @DisplayName("Scenario 3: Dashboard filter (quick count of products by status)")
    public void testScenario3_DashboardMetrics() {
        // Count active products
        FilterBuilder activeFilter = FilterBuilder.start()
            .eq("active", true)
            .gt("quantity", 0);

        // Count by price bracket
        FilterBuilder budgetFilter = FilterBuilder.start()
            .gte("price", new BigDecimal("0"))
            .lte("price", new BigDecimal("100"));

        FilterBuilder premiumFilter = FilterBuilder.start()
            .gt("price", new BigDecimal("1000"));

        // In datastore:
        // long activeCount = datastore.query(Product.class)
        //     .filter(activeFilter.build()).count();
        //
        // long budgetCount = datastore.query(Product.class)
        //     .filter(budgetFilter.build()).count();
        //
        // long premiumCount = datastore.query(Product.class)
        //     .filter(premiumFilter.build()).count();
    }

    // ============ COMPARISON: OLD vs NEW ============

    @Test
    @DisplayName("Comparison: Old nested-if vs New FilterBuilder approach")
    public void testComparison() {
        String name = "laptop";
        String category = "Electronics";
        BigDecimal minPrice = new BigDecimal("100");
        BigDecimal maxPrice = new BigDecimal("500");
        String vendor = null;
        boolean inStock = true;

        // ❌ OLD WAY: Lots of if statements
        /*
        Query query = datastore.query(Product.class);

        if (name != null) {
            query = query.filter(NAME.like("%" + name + "%"));
        }
        if (category != null) {
            query = query.filter(CATEGORY.eq(category));
        }
        if (minPrice != null) {
            query = query.filter(PRICE.gte(minPrice));
        }
        if (maxPrice != null) {
            query = query.filter(PRICE.lte(maxPrice));
        }
        if (vendor != null) {
            query = query.filter(VENDOR.eq(vendor));
        }
        if (inStock) {
            query = query.filter(QUANTITY.gt(0));
        }

        List<PropertyBox> results = query.list(PRODUCT_PROPS);
        */

        // ✅ NEW WAY: Clean FilterBuilder
        FilterBuilder filters = FilterBuilder.start();

        if (name != null) {
            filters.like("name", "%" + name + "%");
        }
        if (category != null) {
            filters.eq("category", category);
        }
        if (minPrice != null) {
            filters.gte("price", minPrice);
        }
        if (maxPrice != null) {
            filters.lte("price", maxPrice);
        }
        if (vendor != null) {
            filters.eq("vendor", vendor);
        }
        if (inStock) {
            filters.gt("quantity", 0);
        }

        // List<PropertyBox> results = datastore.query(Product.class)
        //     .filter(filters.build())
        //     .list(PRODUCT_PROPS);

        // Benefits of new way:
        // ✓ All filter logic in one block (FilterBuilder)
        // ✓ Easy to read and understand
        // ✓ Easier to test (pass FilterBuilder, not Query)
        // ✓ Reusable filters (compose filters from other methods)
        // ✓ Type-safe (no String property names to get wrong)
    }

    // ============ BONUS: COMPOSABLE FILTERS ============

    @Test
    @DisplayName("Bonus: Create reusable filter methods")
    public void testComposableFilters() {
        // Create specialized filters as methods
        FilterBuilder baseFilters = createBaseFilters();
        FilterBuilder priceRangeFilter = createPriceRangeFilter(
            new BigDecimal("100"),
            new BigDecimal("500")
        );

        // Combine filters
        FilterBuilder combined = baseFilters;
        // ... add more conditions

        // This pattern is great for:
        // - Role-based filtering (admin vs user view)
        // - Tenant isolation (only see my data)
        // - Reusable filter presets
    }

    private FilterBuilder createBaseFilters() {
        return FilterBuilder.start()
            .eq("active", true)
            .gt("quantity", 0);
    }

    private FilterBuilder createPriceRangeFilter(BigDecimal min, BigDecimal max) {
        return FilterBuilder.start()
            .gte("price", min)
            .lte("price", max);
    }
}
