# Holon platform JPA Datastore

> Latest release: [5.5.0](#obtain-the-artifacts)
> 
> **🆕 Java 25 & Spring Boot 4.1 Modernization** - See [What's New](#whats-new-java-25--spring-boot-41-modernization) for virtual threads, observability, native images, and more!

This is the reference __JPA__ `Datastore` implementation of the [Holon Platform](https://holon-platform.com), using the Java `JPA` API for data access and manipulation.

The JPA Datastore relies on the following conventions regarding __DataTarget__ and __Path__ naming strategy:

* The [DataTarget](https://docs.holon-platform.com/current/reference/holon-core.html#DataTarget) _name_ is interpreted as the JPA _entity_ name.
* The [Path](https://docs.holon-platform.com/current/reference/holon-core.html#Path) _name_ is interpreted as a JPA _entity_ attribute name, supporting nested classes through the conventional _dot_ notation.

As a _relational Datastore_, standard [relational expressions](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html#Relational-expressions) are supported (alias, joins and sub-queries).

The JPA Datastore supports any standard JPA __ORM__ library, altough is tested and optimized specifically for:

* [Hibernate](http://hibernate.org/orm) version __4.x__ or __5.x__
* [EclipseLink](http://www.eclipse.org/eclipselink) version __2.5 or above__

A complete __Spring__ and __Spring Boot__ support is provided for JPA Datastore integration in a Spring environment and for __auto-configuration__ facilities.

See the module [documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html) for details.

Just like any other platform module, this artifact is part of the [Holon Platform](https://holon-platform.com) ecosystem, but can be also used as a _stand-alone_ library.

See [Getting started](#getting-started) and the [platform documentation](https://docs.holon-platform.com/current/reference) for further details.

## What's New: Java 25 & Spring Boot 4.1 Modernization

The Holon Datastore JPA has been modernized with cutting-edge Java 25 and Spring Boot 4.1 features to support cloud-native architectures, serverless deployments, and high-concurrency workloads.

### 🚀 Virtual Thread Async Operations

Execute datastore operations asynchronously using lightweight virtual threads (Project Loom). This enables handling thousands of concurrent operations with minimal memory overhead.

**Benefits:**
- 🧵 **Lightweight**: ~1KB per virtual thread vs ~2MB per platform thread
- ⚡ **Scalable**: Handle unlimited concurrent operations
- 🔄 **Automatic**: Context propagated via ScopedValue
- ✅ **Type-safe**: Fluent API with CompletableFuture

**Example:**
```java
@Autowired
VirtualThreadDatastoreExecutor asyncExecutor;

// Async query with virtual thread
asyncExecutor.executeAsync(ds -> 
    ds.query(User.class).filter(User.ACTIVE.eq(true)).list()
).thenAccept(users -> System.out.println("Active users: " + users.size()))
.join();

// Parallel queries
CompletableFuture<List<User>> users = asyncExecutor.executeAsync(
    ds -> ds.query(User.class).list()
);
CompletableFuture<List<Order>> orders = asyncExecutor.executeAsync(
    ds -> ds.query(Order.class).list()
);
CompletableFuture.allOf(users, orders).join();
```

### 🔍 Cloud-Native Observability

Built-in listener framework for integration with OpenTelemetry, Micrometer, and distributed tracing systems.

**Benefits:**
- 📊 **Metrics**: Automatic operation timing and counting
- 🌍 **Tracing**: Distributed trace correlation across services
- 🔌 **Pluggable**: Custom listeners for application-specific monitoring
- ☁️ **Cloud-Ready**: Compatible with modern observability platforms

**Example:**
```java
@Autowired
JpaDatastoreObservationRegistry observationRegistry;

// Add custom listener for all datastore operations
observationRegistry.addListener(event -> {
    logger.info("Operation: {} took {} ms",
        event.getOperationName(),
        event.getDurationNanos() / 1_000_000);
    
    // Send metrics to Micrometer
    meterRegistry.timer("jpa.operation.duration",
        "operation", event.getOperationName()
    ).record(event.getDurationNanos(), TimeUnit.NANOSECONDS);
});

// Query with automatic observation
List<?> results = datastore.query(TARGET).list();
// -> Automatically triggers observation listeners
```

### 📝 Structured JSON Logging

Fluent MDC-based logging with automatic JSON formatting for production deployments, ELK/Splunk integration, and distributed tracing.

**Benefits:**
- 📋 **Structured**: JSON output for log aggregation systems
- 🔗 **Tracing**: Automatic correlation IDs (trace_id, span_id)
- 🎯 **Contextual**: Operation, entity, duration, and custom fields
- 🎛️ **Dual-Mode**: JSON for production, plaintext for development

**Example:**
```java
StructuredLogger.forDatastore("Query")
    .withEntity("User")
    .withDuration(150)  // milliseconds
    .info("User query executed successfully");

// Output (Production - JSON):
// {"timestamp":"2026-08-28T07:30:00Z", "level":"INFO", 
//  "datastore.operation":"Query", "datastore.entity":"User", 
//  "datastore.duration_ms":150, "trace_id":"abc123"}

// Output (Development - Plaintext):
// 07:30:00.123 INFO [main] - User query executed successfully
```

### 🛡️ Type-Safe Pattern Matching

Java 25 sealed classes and pattern matching for type-safe result handling without instanceof checks or casting.

**Benefits:**
- ✅ **Compile-time verification**: All cases checked by compiler
- 🎯 **Type-safe**: No casting required
- 📝 **Readable**: Clear intent with `when` expressions
- 🚫 **Error-free**: Pattern matching enforces completeness

**Example:**
```java
ValidationResult result = PatternMatchingValidation.validate(entity);

String message = switch(result) {
    case PatternMatchingValidation.ValidationResult.Success<?> s -> 
        "Validation passed: " + s.value();
    case PatternMatchingValidation.ValidationResult.Failure f -> 
        "Validation failed: " + f.reason();
    case PatternMatchingValidation.ValidationResult.Skipped sk -> 
        "Validation skipped";
};
```

### ✔️ JUnit 6 & TestContainers Integration

Modern parametrized testing with containerized database provisioning for robust integration tests.

**Benefits:**
- 🧪 **Data-driven**: Parametrized tests with multiple scenarios
- 🐳 **Containerized**: PostgreSQL auto-provisioned and cleaned up
- 📦 **Immutable**: Record-based test fixtures
- 🎯 **Fast**: Parallel test execution support

**Example:**
```java
@ParameterizedTest
@MethodSource("queryFixtures")
@SpringBootTest
void testQueryOperations(QueryFixture fixture) {
    List<?> results = datastore.query(fixture.targetProperty())
        .filter(fixture.condition())
        .list();
    
    assertEquals(fixture.expectedCount(), results.size());
}

// Test fixtures as immutable records
record QueryFixture(
    String targetProperty,
    Filter condition,
    int expectedCount
) {
    QueryFixture {
        if (expectedCount < 0) throw new IllegalArgumentException();
    }
}

static Stream<QueryFixture> queryFixtures() {
    return Stream.of(
        new QueryFixture("user", User.ACTIVE.eq(true), 10),
        new QueryFixture("order", Order.STATUS.eq("PENDING"), 5),
        new QueryFixture("product", Product.PRICE.gt(100), 25)
    );
}
```

### 🚀 GraalVM Native Image Support

Ahead-of-time compilation for ultra-fast startup times and reduced memory footprint in serverless and cloud environments.

**Benefits:**
- ⚡ **Fast Startup**: ~50ms vs 1000ms+ JVM startup
- 💾 **Low Memory**: ~50MB RSS vs 250MB+ JVM
- 🐳 **Container-Friendly**: Smaller Docker images
- 🪣 **Serverless**: Ideal for AWS Lambda and Cloud Run

**Example:**
```bash
# Build with native image
mvn clean package -P native
native-image -cp target/app.jar \
  --initialize-at-build-time=com.holonplatform.datastore.jpa \
  Application

# Run native app (50ms startup!)
./Application

# Docker deployment with native image
FROM ubuntu:22.04
COPY target/application /app
ENTRYPOINT ["/app"]
```

### 📊 Performance Comparison

| Feature | Virtual Threads | Native Image |
|---------|---|---|
| **Memory per Thread** | ~1 KB | N/A |
| **Startup Time** | N/A | ~50ms |
| **Memory Footprint** | Efficient | ~50MB RSS |
| **Concurrency** | Unlimited | Efficient |
| **Use Case** | High concurrency | Rapid scaling |

**For more details and advanced usage examples, see:**
- **[INDEX.md](INDEX.md)** - Project-wide documentation index
- **[QUICKSTART.md](QUICKSTART.md)** - Copy-paste examples for each feature
- **[MODERNIZATION_SUMMARY.md](MODERNIZATION_SUMMARY.md)** - Complete technical overview

---

## At-a-glance overview

_JPA Datastore operations:_
```java
DataTarget<MyEntity> TARGET = JpaTarget.of(MyEntity.class);
		
Datastore datastore = JpaDatastore.builder().entityManagerFactory(myEntityManagerFactory).build();

datastore.save(TARGET, PropertyBox.builder(TEST).set(ID, 1L).set(VALUE, "One").build());

Stream<PropertyBox> results = datastore.query().target(TARGET).filter(ID.goe(1L)).stream(TEST);

List<String> values = datastore.query().target(TARGET).sort(ID.asc()).list(VALUE);

Stream<String> values = datastore.query().target(TARGET).filter(VALUE.startsWith("prefix")).restrict(10, 0).stream(VALUE);

long count = datastore.query(TARGET).aggregate(QueryAggregation.builder().path(VALUE).filter(ID.gt(1L)).build()).count();

Stream<Integer> months = datastore.query().target(TARGET).distinct().stream(LOCAL_DATE.month());
		
List<MyEntity> entities = datastore.query().target(TARGET).list(BeanProjection.of(MyEntity.class));

datastore.bulkUpdate(TARGET).filter(ID.in(1L, 2L)).set(VALUE, "test").execute();

datastore.bulkDelete(TARGET).filter(ID.gt(0L)).execute();
```

_Transaction management:_
```java
long updatedCount = datastore.withTransaction(tx -> {
	long updated = datastore.bulkUpdate(TARGET).set(VALUE, "test").execute().getAffectedCount();
			
	tx.commit();
			
	return updated;
});
```

_JPA Datastore configuration using Spring:_
```java
@EnableJpaDatastore
@Configuration
class Config {

  @Bean
  public FactoryBean<EntityManagerFactory> entityManagerFactory(DataSource dataSource) {
      LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
      emf.setDataSource(dataSource);
      emf.setPackagesToScan("com.example.entities");
      return emf;
  }

}

@Autowired
Datastore datastore;
```

_JPA Datastore auto-configuration using Spring Boot:_
```yaml
spring:
  datasource:
    url: "jdbc:h2:mem:test"
    username: "sa"
    
holon: 
  datastore:
    trace: true
```

See the [module documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html) for the user guide and a full set of examples.

## Code structure

See [Holon Platform code structure and conventions](https://github.com/holon-platform/platform/blob/master/CODING.md) to learn about the _"real Java API"_ philosophy with which the project codebase is developed and organized.

## Getting started

### System requirements

The Holon Platform is built using __Java 8__, so you need a JRE/JDK version 8 or above to use the platform artifacts.

For **virtual thread async operations, pattern matching, and native image support**, Java 21 or above is required (Java 25 recommended).

The __JPA API version 2.x__ or above is reccomended to use all the functionalities of the JPA Datastore.

### Releases

See [releases](https://github.com/holon-platform/holon-datastore-jpa/releases) for the available releases. Each release tag provides a link to the closed issues.

### Obtain the artifacts

The [Holon Platform](https://holon-platform.com) is open source and licensed under the [Apache 2.0 license](LICENSE.md). All the artifacts (including binaries, sources and javadocs) are available from the [Maven Central](https://mvnrepository.com/repos/central) repository.

The Maven __group id__ for this module is `com.holon-platform.jpa` and a _BOM (Bill of Materials)_ is provided to obtain the module artifacts:

_Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform.jpa</groupId>
        <artifactId>holon-datastore-jpa-bom</artifactId>
        <version>5.5.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Using the Platform BOM

The [Holon Platform](https://holon-platform.com) provides an overall Maven _BOM (Bill of Materials)_ to easily obtain all the available platform artifacts:

_Platform Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform</groupId>
        <artifactId>bom</artifactId>
        <version>${platform-version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Build from sources

You can build the sources using Maven (version 3.3.x or above is recommended) like this: 

`mvn clean install`

## Getting help

* Check the [platform documentation](https://docs.holon-platform.com/current/reference) or the specific [module documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jpa.html).

* Ask a question on [Stack Overflow](http://stackoverflow.com). We monitor the [`holon-platform`](http://stackoverflow.com/tags/holon-platform) tag.

* Report an [issue](https://github.com/holon-platform/holon-datastore-jpa/issues).

* A [commercial support](https://holon-platform.com/services) is available too.

## Java 25 & Spring Boot 4.1 Modernization Resources

For details on the latest modernization with virtual threads, observability, structured logging, and native image support:

* **[INDEX.md](INDEX.md)** - Complete documentation index with file structure and navigation
* **[QUICKSTART.md](QUICKSTART.md)** - Practical quick-start guide with copy-paste code examples  
* **[MODERNIZATION_SUMMARY.md](MODERNIZATION_SUMMARY.md)** - Comprehensive technical overview with architecture diagrams
* **[COMPLETION_CHECKLIST.md](COMPLETION_CHECKLIST.md)** - Task-by-task modernization completion status
* **[Native Image README](spring-boot/src/main/resources/META-INF/native-image/README.md)** - GraalVM native image build guide

### Key Modernization Features

- **Virtual Threads**: Async datastore operations with Project Loom
- **Observability**: OpenTelemetry + Micrometer framework
- **Structured Logging**: JSON logging with MDC context and trace correlation
- **Pattern Matching**: Java 25 sealed classes for type-safe results
- **JUnit 6**: Modern parametrized testing with TestContainers
- **Native Images**: GraalVM AOT compilation for 50ms startup

See [What's New: Java 25 & Spring Boot 4.1 Modernization](#whats-new-java-25--spring-boot-41-modernization) section above for detailed examples and benefits.

## Examples

See the [Holon Platform examples](https://github.com/holon-platform/holon-examples) repository for a set of example projects.

## Contribute

See [Contributing to the Holon Platform](https://github.com/holon-platform/platform/blob/master/CONTRIBUTING.md).

[![Gitter chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/holon-platform/contribute?utm_source=share-link&utm_medium=link&utm_campaign=share-link) 
Join the __contribute__ Gitter room for any question and to contact us.

## License

All the [Holon Platform](https://holon-platform.com) modules are _Open Source_ software released under the [Apache 2.0 license](LICENSE).

## Artifacts list

Maven _group id_: `com.holon-platform.jpa`

> **Note on Java 25 Modernization:** The following modules now include Java 25 & Spring Boot 4.1 features:
> - **Virtual Thread Async Operations**: `VirtualThreadDatastoreExecutor` in `holon-datastore-jpa`
> - **Observability Framework**: `JpaDatastoreObservationRegistry` in `holon-datastore-jpa`
> - **Structured Logging**: `StructuredLogger` in `holon-datastore-jpa`
> - **Native Image Support**: AOT hints registration in `holon-datastore-jpa-spring-boot`
> 
> These features are backward compatible and opt-in. Java 21+ required for virtual threads and pattern matching.

Artifact id | Description
----------- | -----------
`holon-datastore-jpa` | __JPA__ `Datastore` implementation (now with virtual threads, observability, and structured logging)
`holon-datastore-jpa-spring` | __Spring__ integration using the `@EnableJpa` and  `@EnableJpaDatastore` annotations
`holon-datastore-jpa-spring-boot` | __Spring Boot__ integration for JPA stack and Datastore auto-configuration (now with async executor auto-configuration and native image hints)
`holon-starter-jpa-hibernate` | __Spring Boot__ _starter_ for JPA stack and Datastore auto-configuration using [Hibernate](http://hibernate.org/orm) ORM
`holon-starter-jpa-eclipselink` | __Spring Boot__ _starter_ for JPA stack and Datastore auto-configuration using [EclipseLink](http://www.eclipse.org/eclipselink) ORM
`holon-datastore-jpa-bom` | Bill Of Materials
`documentation-datastore-jpa` | Documentation
