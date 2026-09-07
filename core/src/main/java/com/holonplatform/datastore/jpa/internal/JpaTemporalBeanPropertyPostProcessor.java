package com.holonplatform.datastore.jpa.internal;

import jakarta.persistence.Temporal;

import com.holonplatform.core.beans.BeanProperty;
import com.holonplatform.core.beans.BeanPropertyPostProcessor;
import com.holonplatform.core.temporal.TemporalType;

public class JpaTemporalBeanPropertyPostProcessor implements BeanPropertyPostProcessor {

	@Override
	public BeanProperty.Builder<?> processBeanProperty(BeanProperty.Builder<?> property, Class<?> beanOrNestedClass) {
		property.getAnnotation(Temporal.class).map(Temporal::value).map(JpaTemporalBeanPropertyPostProcessor::convert)
				.ifPresent(property::temporalType);
		return property;
	}

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