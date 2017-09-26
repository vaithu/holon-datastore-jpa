/*
 * Copyright 2016-2017 Axioma srl.
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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.internal.query.QuerySortVisitor;
import com.holonplatform.core.internal.query.QuerySortVisitor.VisitableQuerySort;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.query.QuerySort.CompositeQuerySort;
import com.holonplatform.core.query.QuerySort.PathQuerySort;
import com.holonplatform.core.query.QuerySort.SortDirection;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;

/**
 * JPA {@link VisitableQuerySort} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQuerySortResolver implements ExpressionResolver<VisitableQuerySort, JPQLToken>,
		QuerySortVisitor<JPQLToken, JpaResolutionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends VisitableQuerySort> getExpressionType() {
		return VisitableQuerySort.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLToken> getResolvedType() {
		return JPQLToken.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<JPQLToken> resolve(VisitableQuerySort expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional.ofNullable(expression.accept(this, JpaResolutionContext.checkContext(context)));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QuerySortVisitor#visit(com.holonplatform.core.query.QuerySort.
	 * PathQuerySort, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(PathQuerySort<?> sort, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(JpaDatastoreUtils.resolveExpression(context, sort.getPath(), JPQLToken.class, context).getValue());
		sb.append(" ");
		if (sort.getDirection() == SortDirection.ASCENDING) {
			sb.append("asc");
		} else {
			sb.append("desc");
		}
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QuerySortVisitor#visit(com.holonplatform.core.query.QuerySort.
	 * CompositeQuerySort, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(CompositeQuerySort sort, JpaResolutionContext context) {
		List<String> resolved = new LinkedList<>();
		QueryUtils.flattenQuerySort(sort).forEach(s -> {
			resolved.add(JpaDatastoreUtils.resolveExpression(context, s, JPQLToken.class, context).getValue());
		});
		return JPQLToken.create(resolved.stream().collect(Collectors.joining(",")));
	}

}
