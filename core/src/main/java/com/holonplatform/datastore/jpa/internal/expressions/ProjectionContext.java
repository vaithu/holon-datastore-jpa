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
package com.holonplatform.datastore.jpa.internal.expressions;

import java.util.List;
import java.util.Optional;

import com.holonplatform.core.Expression;

/**
 * Query projection context.
 * 
 * @param <Q> Query result type
 * @param <R> Projection result type
 * 
 * @since 5.0.0
 */
public interface ProjectionContext<Q, R> extends Expression {

	/**
	 * Get the query selections.
	 * @return Query selection
	 */
	List<String> getSelection();

	/**
	 * Get the alias for given selection expression, if defined.
	 * @param selection Selection expression
	 * @return Optional selection alias
	 */
	Optional<String> getSelectionAlias(String selection);

	/**
	 * Get the query result type
	 * @return Query result type
	 */
	Class<? extends Q> getQueryResultType();

	/**
	 * Get the query result converter.
	 * @return Query result converter
	 */
	QueryResultConverter<Q, R> getConverter();

}
