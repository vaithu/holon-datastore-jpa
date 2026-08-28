/*
 * LEVEL 5: Dynamic Filtering with FilterBuilder
 * 
 * REST API endpoint that accepts optional filter parameters
 * and builds type-safe queries without lots of if statements.
 */
package com.example.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.internal.patterns.FilterBuilder;

import java.math.BigDecimal;
import java.util.*;

/**
 * Example: Product Search REST API with Dynamic Filtering
 * 
 * Demonstrates LEVEL 5 - using FilterBuilder for type-safe,
 * composable query construction without nested if statements.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductSearchController {

    @Autowired
    private Datastore datastore;

    /**
     * OLD WAY (nested if statements - hard to maintain):
     * 
     * @GetMapping("/search")
     * public List<Product> oldWaySearch(
     *         @RequestParam(required = false) String name,
     *         @RequestParam(required = false) String category,
     *         @RequestParam(required = false) BigDecimal minPrice,
     *         @RequestParam(required = false) BigDecimal maxPrice) {
     *     
     *     Query query = datastore.query(Product.class);
     *     
     *     if (name != null) {
     *         query = query.filter(PRODUCT_NAME.like("%" + name + "%"));
     *     }
     *     if (category != null) {
     *         query = query.filter(PRODUCT_CATEGORY.eq(category));
     *     }
     *     if (minPrice != null) {
     *         query = query.filter(PRODUCT_PRICE.gte(minPrice));
     *     }
     *     if (maxPrice != null) {
     *         query = query.filter(PRODUCT_PRICE.lte(maxPrice));
     *     }
     *     
     *     return query.list(PRODUCT_PROPERTIES);
     * }
     */

    /**
     * NEW WAY (FilterBuilder - clean, composable, easy to test):
     * 
     * Example curl requests:
     * 
     * 1. Search by name only:
     *    GET /api/v1/products/search?name=laptop
     * 
     * 2. Search by price range:
     *    GET /api/v1/products/search?minPrice=100&maxPrice=500
     * 
     * 3. Search by category:
     *    GET /api/v1/products/search?category=Electronics
     * 
     * 4. Combine all filters:
     *    GET /api/v1/products/search?name=laptop&category=Electronics&minPrice=300&maxPrice=800&vendor=Dell
     * 
     * 5. Only in stock:
     *    GET /api/v1/products/search?inStock=true
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false, defaultValue = "false") Boolean inStock,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize) {

        try {
            // STEP 1: Start building filters dynamically
            FilterBuilder filters = FilterBuilder.start()
                .eq("active", true);  // Always filter for active products

            // STEP 2: Add optional filters only if provided
            if (name != null && !name.trim().isEmpty()) {
                filters.like("name", "%" + name.trim() + "%");
            }

            if (category != null && !category.trim().isEmpty()) {
                filters.eq("category", category);
            }

            if (minPrice != null) {
                filters.gte("price", minPrice);
            }

            if (maxPrice != null) {
                filters.lte("price", maxPrice);
            }

            if (vendor != null && !vendor.trim().isEmpty()) {
                filters.eq("vendor", vendor);
            }

            if (inStock) {
                filters.gt("quantity", 0);  // Only products with stock
            }

            // STEP 3: Execute query with dynamic filters
            List<PropertyBox> results = datastore
                .query(Product.class)
                .filter(filters.build())  // Apply all collected filters at once
                .orderBy(PRODUCT_NAME, true)
                .limit(pageSize)
                .offset(page * pageSize)
                .list(PRODUCT_PROPERTIES);

            // STEP 4: Get total count (for pagination)
            Long totalCount = datastore
                .query(Product.class)
                .filter(filters.build())
                .count();

            // Return response with results and metadata
            SearchResponse response = new SearchResponse(
                results,
                totalCount,
                page,
                pageSize,
                (totalCount + pageSize - 1) / pageSize
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                .internalServerError()
                .body(new SearchResponse(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    0,
                    "Error: " + e.getMessage()
                ));
        }
    }

    /**
     * Bonus: Advanced search with pagination
     * Uses Spring Data Pageable for better integration
     */
    @PostMapping("/advanced-search")
    public ResponseEntity<SearchResponse> advancedSearch(
            @RequestBody ProductSearchRequest request,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize) {

        // Build filters from request object (cleaner for POST)
        FilterBuilder filters = FilterBuilder.start()
            .eq("active", true);

        if (request.getName() != null) {
            filters.like("name", "%" + request.getName() + "%");
        }
        if (request.getCategory() != null) {
            filters.eq("category", request.getCategory());
        }
        if (request.getMinPrice() != null) {
            filters.gte("price", request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            filters.lte("price", request.getMaxPrice());
        }
        if (request.getVendors() != null && !request.getVendors().isEmpty()) {
            filters.in("vendor", request.getVendors());
        }
        if (request.isInStockOnly()) {
            filters.gt("quantity", 0);
        }

        // Execute with pagination
        List<PropertyBox> results = datastore
            .query(Product.class)
            .filter(filters.build())
            .orderBy(PRODUCT_NAME, true)
            .limit(pageSize)
            .offset(page * pageSize)
            .list(PRODUCT_PROPERTIES);

        Long totalCount = datastore
            .query(Product.class)
            .filter(filters.build())
            .count();

        SearchResponse response = new SearchResponse(
            results,
            totalCount,
            page,
            pageSize,
            (totalCount + pageSize - 1) / pageSize
        );

        return ResponseEntity.ok(response);
    }

    // ============ RESPONSE CLASSES ============

    public static class SearchResponse {
        public List<PropertyBox> results;
        public Long totalCount;
        public int currentPage;
        public int pageSize;
        public int totalPages;
        public String error;

        public SearchResponse(List<PropertyBox> results, Long totalCount, int page, int pageSize, int totalPages) {
            this.results = results;
            this.totalCount = totalCount;
            this.currentPage = page;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
        }

        public SearchResponse(List<PropertyBox> results, Long totalCount, int page, int pageSize, int totalPages, String error) {
            this(results, totalCount, page, pageSize, totalPages);
            this.error = error;
        }
    }

    public static class ProductSearchRequest {
        private String name;
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private List<String> vendors;
        private boolean inStockOnly;

        // Getters/Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

        public List<String> getVendors() { return vendors; }
        public void setVendors(List<String> vendors) { this.vendors = vendors; }

        public boolean isInStockOnly() { return inStockOnly; }
        public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    }

    // ============ PROPERTY DEFINITIONS ============
    // (Usually in a separate PathProperties class)

    private static final com.holonplatform.core.property.Property<Long> PRODUCT_ID =
        com.holonplatform.core.property.Property.longProperty("id");

    private static final com.holonplatform.core.property.Property<String> PRODUCT_NAME =
        com.holonplatform.core.property.Property.stringProperty("name");

    private static final com.holonplatform.core.property.Property<String> PRODUCT_CATEGORY =
        com.holonplatform.core.property.Property.stringProperty("category");

    private static final com.holonplatform.core.property.Property<BigDecimal> PRODUCT_PRICE =
        com.holonplatform.core.property.Property.create(BigDecimal.class, "price");

    private static final com.holonplatform.core.property.Property<Integer> PRODUCT_QUANTITY =
        com.holonplatform.core.property.Property.integerProperty("quantity");

    private static final com.holonplatform.core.property.Property<Boolean> PRODUCT_ACTIVE =
        com.holonplatform.core.property.Property.booleanProperty("active");

    private static final com.holonplatform.core.property.Property<String> PRODUCT_VENDOR =
        com.holonplatform.core.property.Property.stringProperty("vendor");

    private static final com.holonplatform.core.property.PropertySet PRODUCT_PROPERTIES =
        com.holonplatform.core.property.PropertySet.of(
            PRODUCT_ID, PRODUCT_NAME, PRODUCT_CATEGORY, PRODUCT_PRICE,
            PRODUCT_QUANTITY, PRODUCT_ACTIVE, PRODUCT_VENDOR
        );
}
