package com.holonplatform.datastore.jpa.internal;

import jakarta.persistence.Temporal;

import com.holonplatform.core.beans.BeanProperty;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.temporal.TemporalType;

public class JpaTemporalBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

	// @Temporal / jakarta.persistence.TemporalType are deprecated since JPA 3.2 but must still be read here to
	// support legacy entities mapping java.util.Date/Calendar fields
	@SuppressWarnings("deprecation")
	@Override
	public BeanProperty.Builder<?> processBeanProperty(BeanProperty.Builder<?> property, Class<?> beanOrNestedClass) {
		property.getAnnotation(Temporal.class).map(Temporal::value).map(JpaTemporalBeanPropertyPostProcessor::convert)
				.ifPresent(property::temporalType);
		return property;
	}

	@SuppressWarnings("deprecation")
	private static TemporalType convert(jakarta.persistence.TemporalType temporalType) {
		switch (temporalType) {
		case DATE:
			return TemporalType.DATE;
		case TIME:
			return TemporalType.TIME;
		case TIMESTAMP:
		default:
			return TemporalType.DATE_TIME;
		}
	}

}