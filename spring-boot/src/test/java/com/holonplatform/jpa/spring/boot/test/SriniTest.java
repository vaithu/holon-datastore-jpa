package com.holonplatform.jpa.spring.boot.test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.datastore.beans.BeanDatastore;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("standard")
public class SriniTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.holonplatform.jpa.spring.boot.test.domain1")
    protected static class Config {
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private Datastore datastore;

    private final static PathProperty<Long> KEY = PathProperty.create("key", long.class);
    private final static PathProperty<String> STR = PathProperty.create("stringValue", String.class);
    private final static PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);
    private final static NumericProperty<Integer> VERSION = NumericProperty.integerType("version");

    private final static PropertySet<?> PROPERTIES = PropertySet.builderOf(KEY, STR, DEC, VERSION)
            .identifiers(KEY, VERSION)
            .build();

    private final static DataTarget<TestJpaDomain1> TARGET = JpaTarget.of(TestJpaDomain1.class);

    @Test
    @Transactional
    public void testDatastore() {

        PropertyBox propertyBox = PropertyBox.builder(PROPERTIES).set(KEY, 7L).set(STR, "Test ds").set(DEC, 7.7)
                .build();

        // INSERT: JPA initialises @Version to 0 on persist; it must be written back.
        datastore.save(TARGET, propertyBox, DefaultWriteOption.BRING_BACK_GENERATED_IDS);
        System.out.println(propertyBox);
        assertEquals(Integer.valueOf(0), propertyBox.getValue(VERSION),
                "Version must be 0 after initial insert");

        Optional<Long> found = datastore.query().target(TARGET).filter(KEY.eq(7L)).findOne(KEY);
        assertTrue(found.isPresent());

        // FIRST UPDATE: change STR to mark the entity dirty so Hibernate issues the
        // SQL UPDATE and increments @Version to 1; the new version is written back.
        propertyBox.setValue(STR, "Test ds v1");
        datastore.update(TARGET, propertyBox);
        System.out.println(propertyBox);
        assertEquals(Integer.valueOf(1), propertyBox.getValue(VERSION),
                "Version must be 1 after first update");

        // SECOND UPDATE: change STR again; @Version increments to 2.
        // This proves the PropertyBox carries a current version, not a stale one,
        // so a subsequent update cannot throw OptimisticLockException.
        propertyBox.setValue(STR, "Test ds v2");
        datastore.update(TARGET, propertyBox);
        System.out.println(propertyBox);
        assertEquals(Integer.valueOf(2), propertyBox.getValue(VERSION),
                "Version must be 2 after second update");
    }

    @Test
    public void testBeanDatastore() {

        BeanDatastore beanDatastore = BeanDatastore.of(datastore);

        beanDatastore.query(TestJpaDomain1.class).stream().findFirst()
                .ifPresentOrElse(System.out::println,
                        () -> new RuntimeException("No records found"));

        TestJpaDomain1 entity = new TestJpaDomain1();
        entity.setKey(1L);
        entity.setStringValue("Sample String");
        entity.setDecimalValue(123.45);

        entity = beanDatastore.insert(entity).getResult().orElseThrow();

        entity.setStringValue("Test");
        entity = beanDatastore.update(entity, DefaultWriteOption.BRING_BACK_GENERATED_IDS).getResult().orElseThrow();
        System.out.println(entity);
    }

}
