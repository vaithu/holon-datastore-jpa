package com.holonplatform.jpa.spring.boot.test;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.Datastore;
import com.holonplatform.core.datastore.DefaultWriteOption;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("standard")
class TestVersionWriteBackOnUpdate {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = TestJpaDomain1.class)
    protected static class Config {
    }

    private static final PathProperty<Long> KEY = PathProperty.create("key", long.class);
    private static final PathProperty<String> STR = PathProperty.create("stringValue", String.class);
    private static final PathProperty<Double> DEC = PathProperty.create("decimalValue", Double.class);
    private static final NumericProperty<Integer> VERSION = NumericProperty.integerType("version");

    private static final PropertySet<?> PROPERTIES = PropertySet.builderOf(KEY, STR, DEC, VERSION)
            .withIdentifier(KEY)
            .build();

    private static final DataTarget<TestJpaDomain1> TARGET = JpaTarget.of(TestJpaDomain1.class);

    @Autowired
    private Datastore datastore;

    @Test
    @Transactional
    void testVersionIsWrittenBackAfterUpdate() {
        PropertyBox box = PropertyBox.builder(PROPERTIES)
                .set(KEY, 9001L)
                .set(STR, "before-update")
                .set(DEC, 10.0)
                .build();

        datastore.save(TARGET, box, DefaultWriteOption.BRING_BACK_GENERATED_IDS_AND_VERSION);
        assertEquals(Integer.valueOf(0), box.getValue(VERSION), "Insert should initialize version to 0");

        box.setValue(STR, "after-update-1");
        datastore.update(TARGET, box);
        assertEquals(Integer.valueOf(1), box.getValue(VERSION), "First update should write back version 1");

        PropertyBox fromDb = datastore.query(TARGET).filter(KEY.eq(9001L)).findOne(PROPERTIES).orElseThrow();
        assertEquals(Integer.valueOf(1), fromDb.getValue(VERSION), "DB version should be 1 after first update");

        box.setValue(DEC, 20.0);
        datastore.update(TARGET, box);
        assertEquals(Integer.valueOf(2), box.getValue(VERSION), "Second update should write back version 2");

        fromDb = datastore.query(TARGET).filter(KEY.eq(9001L)).findOne(PROPERTIES).orElseThrow();
        assertEquals(Integer.valueOf(2), fromDb.getValue(VERSION), "DB version should be 2 after second update");
    }
}

