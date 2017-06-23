/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa;

import java.io.Serializable;

import com.holonplatform.core.query.Query;
import com.holonplatform.datastore.jpa.internal.DefaultQueryHint;

/**
 * Interface to be used to provide {@link Query} execution hints using {@link Query#parameter(String, Object)} with
 * {@link #QUERY_PARAMETER_HINT} as parameter name.
 * 
 * @since 5.0.0
 */
public interface JpaQueryHint extends Serializable {

	/**
	 * {@link Query} parameter to set a query hint (use {@link Query#parameter(String, Object)} to set query
	 * parameters).
	 * <p>
	 * Parameter value must be a {@link JpaQueryHint} instance.
	 * </p>
	 */
	public static final String QUERY_PARAMETER_HINT = "jpaQueryHint";

	/**
	 * Hint name
	 * @return Name
	 */
	String getName();

	/**
	 * Hint value
	 * @return Value
	 */
	Object getValue();

	/**
	 * Build a query hint to be used in {@link Query} parameters with {@link #QUERY_PARAMETER_HINT} parameter name.
	 * @param name Hint name (not null)
	 * @param value Hint value (not null)
	 * @return QueryHint
	 */
	static JpaQueryHint create(String name, Object value) {
		return new DefaultQueryHint(name, value);
	}

}
