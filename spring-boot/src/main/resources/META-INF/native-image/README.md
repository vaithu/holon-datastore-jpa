# GraalVM Native Image Build Configuration

This directory contains native image configuration files for GraalVM AOT (Ahead-of-Time) compilation.

## Files

### reflect-config.json
Reflection configuration for classes requiring dynamic reflection at runtime. Includes:
- Core Holon Datastore classes (DefaultJpaDatastore, VirtualThreadDatastoreExecutor)
- Observability and monitoring classes (ObservationRegistry, ObservationEvent)
- Logging classes (StructuredLogger)
- Jakarta Persistence API classes (EntityManager, Query, etc.)

### jni-config.json
JNI (Java Native Interface) configuration for classes that may be accessed via JNI in native images.

### proxy-config.json
Dynamic proxy configuration for interfaces that are proxied at runtime.

### serialization-config.json
Serialization hints for classes that need to be serialized in native images.

## Building Native Images

To build a native image with these configurations:

```bash
# 1. Create an uberjar with Spring Boot
mvn clean package -P native -DskipTests

# 2. Build native image with GraalVM
native-image -cp target/application.jar \
  --initialize-at-build-time=com.holonplatform.datastore.jpa.async.VirtualThreadDatastoreExecutor \
  --initialize-at-build-time=org.hibernate.proxy \
  -H:ReflectionConfigurationFiles=META-INF/native-image/reflect-config.json \
  -H:JNIConfigurationFiles=META-INF/native-image/jni-config.json \
  --static \
  --report-unsupported-elements-at-runtime \
  Application

# 3. Run native executable
./Application
```

## Testing Native Images

Use the provided native image tests in `core/src/test/java/com/holonplatform/datastore/jpa/test/native/` to verify compatibility.

## Troubleshooting

### Class Not Found at Runtime
**Issue**: `java.lang.ClassNotFoundException` in native image
**Solution**: Add class to reflect-config.json with appropriate member categories

### Method Invocation Failed
**Issue**: `NoSuchMethodException` in native image  
**Solution**: Ensure method is listed in reflect-config.json with correct parameter types

### Proxy Creation Failed
**Issue**: Dynamic proxy creation fails in native image
**Solution**: Add interface to proxy-config.json

### Performance Issues
GraalVM native images may need:
- `-H:+JitCompileOverCountThreshold` for hotspot compilation
- `--enable-monitoring=heapdump` for diagnostic data
- `-Dgraal.EagerSnippetSubstitution=true` for aggressive optimization
