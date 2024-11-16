package com.holonplatform.jpa.spring.boot.test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DatastoreOperations;
import com.holonplatform.core.datastore.DefaultWriteOption;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.jpa.spring.boot.test.domain1.TestJpaDomain1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("standard")
public class SriniTest {


    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.holonplatform.jpa.spring.boot.test.domain1")
    protected static class Config {

    }

    @Autowired(required = true)
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
        datastore.save(TARGET, propertyBox, DefaultWriteOption.BRING_BACK_GENERATED_IDS);
        System.out.println(propertyBox);

        Optional<Long> found = datastore.query().target(TARGET).filter(KEY.eq(7L)).findOne(KEY);
        assertTrue(found.isPresent());

        datastore.update(TARGET, propertyBox);

        propertyBox = datastore.query(TARGET).findOne(PROPERTIES).get();
        System.out.println(propertyBox);
        propertyBox.setValue(KEY,8L);
        datastore.update(TARGET, propertyBox);
        System.out.println(propertyBox);
    }

}
