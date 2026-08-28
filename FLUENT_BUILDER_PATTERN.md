# Fluent Builder Pattern Implementation Guide

## Overview

All new classes added in the Java 25 & Spring Boot 4.1 modernization now follow the **Holon Platform fluent builder pattern**. This ensures consistency with existing Holon Platform APIs and improves code readability.

## Implementation Details

### 1. VirtualThreadDatastoreExecutor

**Builder Interface & Implementation:**
```java
public interface Builder {
    Builder datastore(JpaDatastore datastore);
    Builder executor(Executor executor);
    VirtualThreadDatastoreExecutor build();
}
```

**Before (Direct Construction):**
```java
@Autowired
private JpaDatastore datastore;

// Old way
VirtualThreadDatastoreExecutor executor = new VirtualThreadDatastoreExecutor(datastore);
```

**After (Fluent Builder):**
```java
@Autowired
private JpaDatastore datastore;

// New fluent way
VirtualThreadDatastoreExecutor executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(datastore)
    .build();

// With custom executor
VirtualThreadDatastoreExecutor executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(datastore)
    .executor(Executors.newVirtualThreadPerTaskExecutor())
    .build();
```

**Usage in Spring Configuration:**
```java
@Configuration
public class DatastoreConfig {
    
    @Bean
    public VirtualThreadDatastoreExecutor asyncExecutor(JpaDatastore datastore) {
        return VirtualThreadDatastoreExecutor.builder()
            .datastore(datastore)
            .build();
    }
}
```

---

### 2. StructuredLogger

**Builder Class Implementation:**
```java
public static final class StructuredLoggerBuilder {
    public StructuredLoggerBuilder operation(String operation);
    public StructuredLoggerBuilder entity(String entity);
    public StructuredLoggerBuilder duration(long durationMs);
    public StructuredLoggerBuilder recordCount(int count);
    public StructuredLoggerBuilder with(String key, String value);
    public StructuredLogger build();
}
```

**Before (Factory Method + Chainable Methods):**
```java
// Old way using forDatastore()
StructuredLogger.forDatastore("Query")
    .withEntity("User")
    .withDuration(150)
    .info("Query executed");
```

**After (Full Fluent Builder):**
```java
// New fluent builder way
StructuredLogger.builder()
    .operation("Query")
    .entity("User")
    .duration(150)
    .recordCount(25)
    .with("trace_id", "abc-123-def")
    .with("request_id", "req-456")
    .build()
    .info("Query executed successfully");

// Still supports old way for backward compatibility
StructuredLogger.forDatastore("Query")
    .withEntity("User")
    .withDuration(150)
    .info("Query executed");
```

**Complex Logging Example:**
```java
@Service
public class UserQueryService {
    private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);
    
    public List<User> findActiveUsers() {
        long startTime = System.currentTimeMillis();
        
        try {
            List<User> users = datastore.query(User.class)
                .filter(User.ACTIVE.eq(true))
                .list();
            
            long duration = System.currentTimeMillis() - startTime;
            
            StructuredLogger.builder()
                .operation("Query")
                .entity("User")
                .duration(duration)
                .recordCount(users.size())
                .with("filter", "active=true")
                .with("request_id", getRequestId())
                .build()
                .info("Active users fetched successfully");
            
            return users;
        } catch (Exception e) {
            StructuredLogger.builder()
                .operation("Query")
                .entity("User")
                .build()
                .error("Failed to fetch active users: " + e.getMessage());
            throw e;
        }
    }
}
```

---

### 3. DefaultJpaDatastoreObservationRegistry

**Builder Interface & Implementation:**
```java
public interface Builder {
    Builder listener(JpaDatastoreObservationListener listener);
    Builder listeners(JpaDatastoreObservationListener... listeners);
    DefaultJpaDatastoreObservationRegistry build();
}
```

**Before (Direct Construction + Manual Registration):**
```java
// Old way
DefaultJpaDatastoreObservationRegistry registry = new DefaultJpaDatastoreObservationRegistry();
registry.registerListener(metricsListener);
registry.registerListener(loggingListener);
registry.registerListener(tracingListener);

datastore.registerObservationRegistry(registry);
```

**After (Fluent Builder):**
```java
// New fluent way
JpaDatastoreObservationRegistry registry = DefaultJpaDatastoreObservationRegistry.builder()
    .listener(metricsListener)
    .listener(loggingListener)
    .listeners(tracingListener, auditListener)
    .build();

datastore.registerObservationRegistry(registry);
```

**Spring Configuration Example:**
```java
@Configuration
public class ObservabilityConfig {
    
    @Bean
    public JpaDatastoreObservationRegistry observationRegistry(
            MeterRegistry meterRegistry,
            Tracer tracer) {
        
        return DefaultJpaDatastoreObservationRegistry.builder()
            .listener(new MetricsObservationListener(meterRegistry))
            .listener(new DistributedTracingListener(tracer))
            .listener(new AuditLoggingListener())
            .build();
    }
    
    @Bean
    public JpaDatastore jpaDatastore(JpaDatastore delegate,
            JpaDatastoreObservationRegistry registry) {
        delegate.registerObservationRegistry(registry);
        return delegate;
    }
}
```

---

## Key Benefits

### 1. **Consistency**
- Follows the established Holon Platform builder pattern
- Matches the style of `JpaDatastore.builder()`, `Query.builder()`, etc.
- Reduces cognitive load for developers already familiar with Holon

### 2. **Readability**
```java
// Old way - unclear what's being configured
var executor = new VirtualThreadDatastoreExecutor(
    datastore,
    Executors.newVirtualThreadPerTaskExecutor()
);

// New way - crystal clear intent
var executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(datastore)
    .executor(Executors.newVirtualThreadPerTaskExecutor())
    .build();
```

### 3. **Maintainability**
- New configuration options can be added without breaking existing code
- No method parameter order issues
- IDE autocomplete works perfectly

### 4. **Testability**
```java
@Test
public void testWithCustomExecutor() {
    Executor customExecutor = Executors.newFixedThreadPool(2);
    
    VirtualThreadDatastoreExecutor executor = VirtualThreadDatastoreExecutor.builder()
        .datastore(mockDatastore)
        .executor(customExecutor)
        .build();
    
    assertNotNull(executor);
    customExecutor.shutdown();
}
```

### 5. **Spring Integration**
- Cleaner `@Bean` configuration methods
- Better with constructor injection
- Supports immutable Spring configuration

---

## Migration Path (Optional)

**No migration required!** Old code continues to work:

```java
// These still work (backward compatible):
VirtualThreadDatastoreExecutor executor = new VirtualThreadDatastoreExecutor(ds);
StructuredLogger logger = StructuredLogger.forDatastore("Query");
registry.registerListener(listener);

// But new fluent builders are recommended:
VirtualThreadDatastoreExecutor executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(ds)
    .build();
    
StructuredLogger logger = StructuredLogger.builder()
    .operation("Query")
    .build();
    
JpaDatastoreObservationRegistry registry = DefaultJpaDatastoreObservationRegistry.builder()
    .listener(listener)
    .build();
```

---

## Common Patterns

### Pattern 1: Minimal Configuration
```java
var executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(datastore)
    .build();
```

### Pattern 2: Full Configuration
```java
var executor = VirtualThreadDatastoreExecutor.builder()
    .datastore(datastore)
    .executor(Executors.newVirtualThreadPerTaskExecutor())
    .build();
```

### Pattern 3: Multiple Options
```java
var registry = DefaultJpaDatastoreObservationRegistry.builder()
    .listener(listener1)
    .listener(listener2)
    .listeners(listener3, listener4, listener5)
    .build();
```

### Pattern 4: Complex Context
```java
var logger = StructuredLogger.builder()
    .operation("BulkInsert")
    .entity("OrderItem")
    .duration(5000)
    .recordCount(1000)
    .with("batch_id", batchId)
    .with("user_id", userId)
    .with("source_system", "LEGACY_ERP")
    .build()
    .info("Bulk insert completed");
```

---

## Design Rationale

### Why Fluent Builders?

1. **Holon Platform Alignment**: All major Holon classes use builders
2. **Best Practice**: Recognized as a best practice in Java
3. **Scalability**: Easy to add new configuration options
4. **Clarity**: Self-documenting code
5. **Thread Safety**: Builders can be implemented immutably

### Immutability Strategy

All three classes follow this pattern:
1. **Builder** is mutable and configurable
2. **build()** creates an immutable instance
3. **Once built**, the instance cannot be changed
4. **Thread-safe** by default

```java
// Builder is mutable
Builder builder1 = VirtualThreadDatastoreExecutor.builder()
    .datastore(ds1);
Builder builder2 = builder1.datastore(ds2);  // Returns modified builder

// Once built, immutable
VirtualThreadDatastoreExecutor exec1 = builder1.build();
VirtualThreadDatastoreExecutor exec2 = builder2.build();
// exec1 and exec2 are independent and thread-safe
```

---

## Complete Examples

### Example 1: Spring Boot Service
```java
@Service
public class AsyncDatastoreService {
    
    private final VirtualThreadDatastoreExecutor asyncExecutor;
    
    public AsyncDatastoreService(JpaDatastore datastore) {
        // Fluent builder initialization
        this.asyncExecutor = VirtualThreadDatastoreExecutor.builder()
            .datastore(datastore)
            .build();
    }
    
    public CompletableFuture<List<User>> findAllUsersAsync() {
        return asyncExecutor.executeAsync(ds ->
            ds.query(User.class).list()
        ).thenApply(users -> {
            StructuredLogger.builder()
                .operation("Query")
                .entity("User")
                .recordCount(users.size())
                .build()
                .info("Users fetched asynchronously");
            return users;
        });
    }
}
```

### Example 2: Spring Configuration
```java
@Configuration
public class DatastoreConfiguration {
    
    @Bean
    public VirtualThreadDatastoreExecutor asyncExecutor(JpaDatastore datastore) {
        return VirtualThreadDatastoreExecutor.builder()
            .datastore(datastore)
            .build();
    }
    
    @Bean
    public JpaDatastoreObservationRegistry observationRegistry(
            MeterRegistry meterRegistry) {
        return DefaultJpaDatastoreObservationRegistry.builder()
            .listener(event -> {
                meterRegistry.timer("jpa.operation.duration")
                    .record(event.getDurationNanos(), TimeUnit.NANOSECONDS);
            })
            .build();
    }
}
```

### Example 3: Logging with Full Context
```java
@Aspect
@Component
public class DatastoreOperationAspect {
    
    @Around("@annotation(com.holonplatform.datastore.jpa.logging.Audited)")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.nanoTime();
        String operationName = pjp.getSignature().getName();
        
        try {
            Object result = pjp.proceed();
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            
            StructuredLogger.builder()
                .operation(operationName)
                .duration(durationMs)
                .with("status", "SUCCESS")
                .with("method", pjp.getSignature().toLongString())
                .build()
                .info("Operation completed");
            
            return result;
        } catch (Exception e) {
            StructuredLogger.builder()
                .operation(operationName)
                .with("status", "FAILED")
                .with("error", e.getClass().getSimpleName())
                .build()
                .error("Operation failed: " + e.getMessage());
            throw e;
        }
    }
}
```

---

## Backward Compatibility Guarantee

✅ **All existing code continues to work without modification**

```java
// These constructors/factories remain available:
new VirtualThreadDatastoreExecutor(datastore)
new VirtualThreadDatastoreExecutor(datastore, executor)
StructuredLogger.forDatastore("Query")
StructuredLogger.forLogger("custom.logger")
new DefaultJpaDatastoreObservationRegistry()
registry.registerListener(listener)
```

---

## Summary

The fluent builder pattern implementation:
- ✅ Maintains 100% backward compatibility
- ✅ Aligns with Holon Platform standards
- ✅ Improves code readability
- ✅ Makes configuration clearer
- ✅ Enables better IDE support
- ✅ Scales well for future enhancements
- ✅ Follows Java best practices
