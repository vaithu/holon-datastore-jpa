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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.Path;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.datastore.operation.common.UpdateOperationConfiguration;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLParameterizableExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLStatement;

/**
 * {@link UpdateOperationConfiguration} resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum UpdateOperationConfigurationResolver
		implements JPQLContextExpressionResolver<UpdateOperationConfiguration, JPQLStatement> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends UpdateOperationConfiguration> getExpressionType() {
		return UpdateOperationConfiguration.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLStatement> getResolvedType() {
		return JPQLStatement.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLStatement> resolve(UpdateOperationConfiguration expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(expression.getTarget(), RelationalTarget.class);

		// build a statement context
		final JPQLStatementResolutionContext operationContext = JPQLStatementResolutionContext.asChild(context, target,
				context.getDialect().updateStatementAliasSupported() ? AliasMode.AUTO : AliasMode.UNSUPPORTED);

		final StringBuilder operation = new StringBuilder();

		operation.append("UPDATE");
		operation.append(" ");

		// target
		operation.append(operationContext.resolveOrFail(target, JPQLExpression.class).getValue());

		// values
		final Map<Path<?>, TypedExpression<?>> pathValues = expression.getValues();

		final List<String> paths = new ArrayList<>(pathValues.size());
		final List<String> values = new ArrayList<>(pathValues.size());

		final JPQLStatementResolutionContext setContext;
		if (context.getDialect().updateStatementAliasSupported()
				&& !operationContext.getDialect().updateStatementSetAliasSupported()) {
			setContext = JPQLStatementResolutionContext.asChild(operationContext, target, AliasMode.UNSUPPORTED);
		} else {
			setContext = operationContext;
		}

		pathValues.forEach((path, pathExpression) -> {
			paths.add(setContext.resolveOrFail(path, JPQLExpression.class).getValue());
			values.add(
					setContext.resolveOrFail(JPQLParameterizableExpression.create(pathExpression), JPQLExpression.class)
							.getValue());
		});

		operation.append(" SET ");
		for (int i = 0; i < paths.size(); i++) {
			if (i > 0) {
				operation.append(",");
			}
			operation.append(paths.get(i));
			operation.append("=");
			operation.append(values.get(i));
		}

		// filter
		expression.getFilter().ifPresent(f -> {
			operation.append(" WHERE ");
			operation.append(operationContext.resolveOrFail(f, JPQLExpression.class).getValue());
		});

		return Optional.of(JPQLStatement.create(operation.toString(),
				operationContext.getNamedParametersHandler().getNamedParameters()));
	}

}
