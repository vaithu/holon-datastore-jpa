/**
 * 🎯 LEVEL 5: DYNAMIC FILTERING WITH FILTERBUILDER
 * ─────────────────────────────────────────────────────
 * 
 * START HERE! This single class contains everything you need to understand
 * and implement dynamic filtering with Holon's FilterBuilder pattern.
 * 
 * 📚 Contents:
 * ├─ 1. PRODUCT ENTITY (the model you query)
 * ├─ 2. CONTROLLER ENDPOINTS (your REST API)
 * ├─ 3. TEST EXAMPLES (how to use FilterBuilder)
 * ├─ 4. QUICK REFERENCE (syntax cheat sheet)
 * └─ 5. STEP-BY-STEP GUIDE (where to start)
 * 
 * ⚡ Quick Start: Copy the PRODUCT ENTITY to your project, then CONTROLLER ENDPOINTS
 * 
 * © Holon Platform v12.0.0 - FilterBuilder Pattern Learning
 */

package com.example.products;

import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreCmds;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.query.QueryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 1: PRODUCT ENTITY
 * ════════════════════════════════════════════════════════════════════════════════
 * 
 * Copy this class to: src/main/java/com/example/products/Product.java
 */
@Entity
@Table(name = "products")
class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column
    private Integer quantity;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column
    private String vendor;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Property Definitions (use these in your controller)
    // ─────────────────────────────────────────────────────────────────────────
    
    public static final Property<Long> ID = Property.create("id", Long.class);
    public static final Property<String> NAME = Property.create("name", String.class);
    public static final Property<String> DESCRIPTION = Property.create("description", String.class);
    public static final Property<String> CATEGORY = Property.create("category", String.class);
    public static final Property<BigDecimal> PRICE = Property.create("price", BigDecimal.class);
    public static final Property<Integer> QUANTITY = Property.create("quantity", Integer.class);
    public static final Property<Boolean> ACTIVE = Property.create("active", Boolean.class);
    public static final Property<String> VENDOR = Property.create("vendor", String.class);
    
    // PropertySet for queries
    public static final PropertySet<?> PRODUCT_PROPERTIES = PropertySet.of(
            ID, NAME, DESCRIPTION, CATEGORY, PRICE, QUANTITY, ACTIVE, VENDOR
    );
    
    // Constructors
    public Product() {}
    
    public Product(String name, String category, BigDecimal price, Integer quantity, String vendor) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.vendor = vendor;
        this.active = true;
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", active=" + active +
                ", vendor='" + vendor + '\'' +
                '}';
    }
}

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 2: PRODUCT SEARCH CONTROLLER
 * ════════════════════════════════════════════════════════════════════════════════
 * 
 * Copy this class to: src/main/java/com/example/products/ProductSearchController.java
 * 
 * This controller shows the FILTERBUILDER PATTERN in action.
 * Two endpoints: GET (simple search) and POST (advanced search)
 */
@RestController
@RequestMapping("/api/v1/products")
class ProductSearchController {
    
    @Autowired
    private Datastore datastore;
    
    /**
     * ─────────────────────────────────────────────────────────────────────────
     * ENDPOINT 1: GET /api/v1/products/search
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * Basic search with optional filters via query parameters.
     * 
     * Example requests:
     *   GET /api/v1/products/search?name=laptop
     *   GET /api/v1/products/search?category=Electronics&minPrice=100&maxPrice=500
     *   GET /api/v1/products/search?name=laptop&vendor=Dell&inStockOnly=true
     * 
     * How it works:
     *   1. Create FilterBuilder.start()
     *   2. For each optional parameter, add to filter if provided
     *   3. Execute query with filters.build()
     *   4. Return results with pagination metadata
     */
    @GetMapping("/search")
    public ProductSearchResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        // Step 1: Create FilterBuilder
        QueryFilter.QueryFilterBuilder filters = QueryFilter.start();
        
        // Step 2: Add filters only if parameters are provided (optional filtering)
        if (name != null && !name.isEmpty()) {
            filters.like(Product.NAME, "%" + name + "%");
        }
        
        if (category != null && !category.isEmpty()) {
            filters.eq(Product.CATEGORY, category);
        }
        
        if (minPrice != null) {
            filters.gte(Product.PRICE, minPrice);
        }
        
        if (maxPrice != null) {
            filters.lte(Product.PRICE, maxPrice);
        }
        
        if (vendor != null && !vendor.isEmpty()) {
            filters.eq(Product.VENDOR, vendor);
        }
        
        // Always-on filter: only active products
        if (inStockOnly != null && inStockOnly) {
            filters.gt(Product.QUANTITY, 0);
        }
        
        // Step 3: Execute query with filters
        long offset = (long) page * limit;
        
        List<ProductDTO> results = datastore.query(Product.class)
                .filter(filters.build())
                .sort(Product.NAME, true)
                .limit(limit)
                .offset(offset)
                .list(Product.PRODUCT_PROPERTIES)
                .stream()
                .map(ProductDTO::from)
                .collect(Collectors.toList());
        
        // Get total count for pagination
        long totalCount = datastore.query(Product.class)
                .filter(filters.build())
                .count();
        
        return new ProductSearchResponse(
                results,
                page,
                limit,
                totalCount,
                (totalCount + limit - 1) / limit // totalPages
        );
    }
    
    /**
     * ─────────────────────────────────────────────────────────────────────────
     * ENDPOINT 2: POST /api/v1/products/advanced-search
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * Advanced search with JSON request body for complex filtering.
     * 
     * Example request body:
     * {
     *   "searchTerm": "laptop",
     *   "category": "Electronics",
     *   "minPrice": 500,
     *   "maxPrice": 2000,
     *   "vendors": ["Dell", "HP", "Lenovo"],
     *   "inStockOnly": true,
     *   "page": 0,
     *   "limit": 50
     * }
     * 
     * Features:
     *   ✓ Multi-value vendor filter (vendors array)
     *   ✓ Range filters (minPrice, maxPrice)
     *   ✓ Text search (searchTerm with LIKE)
     *   ✓ Stock status (inStockOnly)
     *   ✓ Pagination
     */
    @PostMapping("/advanced-search")
    public ProductSearchResponse advancedSearch(@RequestBody ProductSearchRequest request) {
        
        // Step 1: Create FilterBuilder
        QueryFilter.QueryFilterBuilder filters = QueryFilter.start();
        
        // Step 2: Build filters from request
        if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
            filters.like(Product.NAME, "%" + request.getSearchTerm() + "%");
        }
        
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            filters.eq(Product.CATEGORY, request.getCategory());
        }
        
        if (request.getMinPrice() != null) {
            filters.gte(Product.PRICE, request.getMinPrice());
        }
        
        if (request.getMaxPrice() != null) {
            filters.lte(Product.PRICE, request.getMaxPrice());
        }
        
        // Multi-value filter: vendors array
        if (request.getVendors() != null && !request.getVendors().isEmpty()) {
            filters.in(Product.VENDOR, request.getVendors());
        }
        
        if (request.isInStockOnly()) {
            filters.gt(Product.QUANTITY, 0);
        }
        
        // Always-on: only active products
        filters.eq(Product.ACTIVE, true);
        
        // Step 3: Execute query
        long offset = (long) request.getPage() * request.getLimit();
        
        List<ProductDTO> results = datastore.query(Product.class)
                .filter(filters.build())
                .sort(Product.NAME, true)
                .limit(request.getLimit())
                .offset(offset)
                .list(Product.PRODUCT_PROPERTIES)
                .stream()
                .map(ProductDTO::from)
                .collect(Collectors.toList());
        
        long totalCount = datastore.query(Product.class)
                .filter(filters.build())
                .count();
        
        return new ProductSearchResponse(
                results,
                request.getPage(),
                request.getLimit(),
                totalCount,
                (totalCount + request.getLimit() - 1) / request.getLimit()
        );
    }
}

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 3: DTOs & REQUEST/RESPONSE CLASSES
 * ════════════════════════════════════════════════════════════════════════════════
 */

class ProductDTO {
    public Long id;
    public String name;
    public String category;
    public BigDecimal price;
    public Integer quantity;
    public Boolean active;
    public String vendor;
    
    public ProductDTO() {}
    
    public ProductDTO(Long id, String name, String category, BigDecimal price, 
                      Integer quantity, Boolean active, String vendor) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.active = active;
        this.vendor = vendor;
    }
    
    public static ProductDTO from(PropertyBox propertyBox) {
        return new ProductDTO(
                propertyBox.getValue(Product.ID),
                propertyBox.getValue(Product.NAME),
                propertyBox.getValue(Product.CATEGORY),
                propertyBox.getValue(Product.PRICE),
                propertyBox.getValue(Product.QUANTITY),
                propertyBox.getValue(Product.ACTIVE),
                propertyBox.getValue(Product.VENDOR)
        );
    }
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public Boolean getActive() { return active; }
    public String getVendor() { return vendor; }
}

class ProductSearchRequest {
    private String searchTerm;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> vendors;
    private boolean inStockOnly = false;
    private int page = 0;
    private int limit = 20;
    
    public ProductSearchRequest() {}
    
    // Getters & Setters
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    
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
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}

class ProductSearchResponse {
    private List<ProductDTO> products;
    private int page;
    private int limit;
    private long totalCount;
    private long totalPages;
    
    public ProductSearchResponse(List<ProductDTO> products, int page, int limit, 
                                 long totalCount, long totalPages) {
        this.products = products;
        this.page = page;
        this.limit = limit;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
    }
    
    // Getters
    public List<ProductDTO> getProducts() { return products; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public long getTotalCount() { return totalCount; }
    public long getTotalPages() { return totalPages; }
}

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 4: TEST EXAMPLES (Reference implementations)
 * ════════════════════════════════════════════════════════════════════════════════
 * 
 * These are NOT actual JUnit tests. They're code snippets showing how to build
 * filters for different scenarios. Copy the logic you need into your own tests.
 */
class ProductFilterExamples {
    
    private Datastore datastore;
    
    /**
     * Example 1: Single property filter
     * Find all laptops
     */
    public void example1_singleFilter() {
        QueryFilter filter = QueryFilter.start()
                .eq(Product.NAME, "Laptop")
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Example 2: Multiple conditions (AND logic)
     * Find all Electronics that cost between $100-$500
     */
    public void example2_multipleConditions() {
        QueryFilter filter = QueryFilter.start()
                .eq(Product.CATEGORY, "Electronics")
                .gte(Product.PRICE, new BigDecimal("100"))
                .lte(Product.PRICE, new BigDecimal("500"))
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Example 3: Text search with LIKE
     * Find products with "laptop" in the name
     */
    public void example3_textSearch() {
        QueryFilter filter = QueryFilter.start()
                .like(Product.NAME, "%laptop%")
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Example 4: IN clause (multiple values)
     * Find products from specific vendors
     */
    public void example4_inClause() {
        List<String> vendors = List.of("Dell", "HP", "Lenovo");
        
        QueryFilter filter = QueryFilter.start()
                .in(Product.VENDOR, vendors)
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Example 5: Conditional filters
     * Only add filter if parameter is provided
     */
    public void example5_conditionalFilters(String vendorParam) {
        QueryFilter.QueryFilterBuilder filters = QueryFilter.start();
        
        // Only add this filter if vendor was provided
        if (vendorParam != null && !vendorParam.isEmpty()) {
            filters.eq(Product.VENDOR, vendorParam);
        }
        
        // Always add this filter
        filters.eq(Product.ACTIVE, true);
        
        datastore.query(Product.class).filter(filters.build()).list();
    }
    
    /**
     * Example 6: GREATER THAN (stock check)
     * Find products in stock
     */
    public void example6_greaterThan() {
        QueryFilter filter = QueryFilter.start()
                .gt(Product.QUANTITY, 0)
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Example 7: NOT EQUALS
     * Find products that are NOT from specific vendor
     */
    public void example7_notEquals() {
        QueryFilter filter = QueryFilter.start()
                .neq(Product.VENDOR, "Unknown")
                .build();
        
        datastore.query(Product.class).filter(filter).list();
    }
    
    /**
     * Real-world Scenario 1:
     * User search: "Find laptops under $500 in stock"
     */
    public List<ProductDTO> scenario1_userSearch(String searchTerm, BigDecimal maxPrice) {
        QueryFilter.QueryFilterBuilder filters = QueryFilter.start();
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            filters.like(Product.NAME, "%" + searchTerm + "%");
        }
        
        if (maxPrice != null) {
            filters.lte(Product.PRICE, maxPrice);
        }
        
        filters.gt(Product.QUANTITY, 0); // in stock
        
        return datastore.query(Product.class)
                .filter(filters.build())
                .list(Product.PRODUCT_PROPERTIES)
                .stream()
                .map(ProductDTO::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Real-world Scenario 2:
     * Dashboard: "Show total value of inventory by category"
     */
    public void scenario2_dashboardMetrics() {
        // Filter: only active products in stock
        QueryFilter filter = QueryFilter.start()
                .eq(Product.ACTIVE, true)
                .gt(Product.QUANTITY, 0)
                .build();
        
        // Group by category and calculate sum(price * quantity)
        datastore.query(Product.class)
                .filter(filter)
                .list(Product.PRODUCT_PROPERTIES);
        // (In real code, use aggregation/grouping)
    }
}

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 5: QUICK REFERENCE - FILTERBUILDER SYNTAX
 * ════════════════════════════════════════════════════════════════════════════════
 * 
 * All FilterBuilder operations:
 * 
 * SYNTAX:
 * ───────
 *   QueryFilter.start()
 *       .eq(property, value)          // Equals
 *       .neq(property, value)         // Not equals
 *       .gt(property, value)          // Greater than
 *       .gte(property, value)         // Greater than or equal
 *       .lt(property, value)          // Less than
 *       .lte(property, value)         // Less than or equal
 *       .like(property, "%pattern%")  // LIKE (text search)
 *       .in(property, List<value>)    // IN clause (multiple values)
 *       .build()                       // Create the filter
 * 
 * COMMON PATTERNS:
 * ────────────────
 * 
 * 1. OPTIONAL PARAMETERS
 *    if (param != null) filters.eq(prop, param);
 * 
 * 2. ALWAYS-ON FILTERS
 *    filters.eq(Product.ACTIVE, true);
 * 
 * 3. RANGE FILTERS
 *    filters.gte(Product.PRICE, minPrice);
 *    filters.lte(Product.PRICE, maxPrice);
 * 
 * 4. TEXT SEARCH
 *    filters.like(Product.NAME, "%" + search + "%");
 * 
 * 5. MULTI-VALUE FILTER
 *    filters.in(Product.VENDOR, List.of("Dell", "HP"));
 * 
 * 6. CONDITIONAL WITH DEFAULT
 *    filters.eq(Product.ACTIVE, inStockOnly ? true : null);
 * 
 * IMPORTANT NOTES:
 * ────────────────
 * • All conditions are AND'ed together
 * • Use .in() for OR logic (multiple values for same field)
 * • Filter at SQL level (before offset/limit)
 * • Text search case-sensitivity depends on database
 * • Always check parameters for null/empty before adding to filter
 */

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * PART 6: STEP-BY-STEP GUIDE - WHERE TO START
 * ════════════════════════════════════════════════════════════════════════════════
 * 
 * STEP 1: Copy the Product Entity
 * ────────────────────────────────
 * Copy the Product class from PART 1 to your project:
 *   src/main/java/com/example/products/Product.java
 * 
 * STEP 2: Copy the ProductSearchController
 * ──────────────────────────────────────────
 * Copy the ProductSearchController from PART 2:
 *   src/main/java/com/example/products/ProductSearchController.java
 * 
 * Update @Autowired Datastore to match your setup.
 * 
 * STEP 3: Copy the DTOs
 * ─────────────────────
 * Copy ProductDTO, ProductSearchRequest, ProductSearchResponse from PART 4:
 *   src/main/java/com/example/products/ProductDTO.java
 *   src/main/java/com/example/products/ProductSearchRequest.java
 *   src/main/java/com/example/products/ProductSearchResponse.java
 * 
 * STEP 4: Test the Endpoints
 * ──────────────────────────
 * Start your Spring app and test with curl:
 * 
 *   # Simple search
 *   curl "http://localhost:8080/api/v1/products/search?name=laptop"
 *   
 *   # Range filter
 *   curl "http://localhost:8080/api/v1/products/search?minPrice=100&maxPrice=500"
 *   
 *   # Advanced search
 *   curl -X POST http://localhost:8080/api/v1/products/advanced-search \
 *     -H "Content-Type: application/json" \
 *     -d '{
 *       "searchTerm": "laptop",
 *       "category": "Electronics",
 *       "minPrice": 500,
 *       "maxPrice": 2000,
 *       "vendors": ["Dell", "HP"],
 *       "inStockOnly": true
 *     }'
 * 
 * STEP 5: Adapt to Your Entity
 * ────────────────────────────
 * Replace Product with your entity:
 *   - Update class names (Product → YourEntity)
 *   - Update property names to match your fields
 *   - Update PRODUCT_PROPERTIES to YOUR_ENTITY_PROPERTIES
 *   - Update @Table name if different
 * 
 * STEP 6: Add Your Own Filters
 * ────────────────────────────
 * For each new filter you want:
 *   a) Add @RequestParam to the endpoint
 *   b) Check if provided: if (param != null)
 *   c) Add to filters: filters.eq(property, param)
 * 
 * Example: Add vendor filter
 *   @RequestParam(required = false) String vendor,
 *   
 *   if (vendor != null) {
 *       filters.eq(Product.VENDOR, vendor);
 *   }
 * 
 * YOU'RE DONE! 🎉
 * ──────────────
 * You now have a working search API with FilterBuilder pattern.
 * No more nested if statements!
 */
