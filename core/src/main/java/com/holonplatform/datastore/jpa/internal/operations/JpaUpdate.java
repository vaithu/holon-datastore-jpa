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
package com.holonplatform.datastore.jpa.internal.operations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.datastore.Datastore.OperationResult;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.DatastoreCommodityContext.CommodityConfigurationException;
import com.holonplatform.core.datastore.DatastoreCommodityFactory;
import com.holonplatform.core.datastore.operation.Update;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.datastore.operation.AbstractUpdate;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.PathPropertyBoxAdapter;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.datastore.jpa.JpaWriteOption;
import com.holonplatform.datastore.jpa.config.JpaDatastoreCommodityContext;
import com.holonplatform.datastore.jpa.context.JpaOperationContext;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreLogger;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * JPA {@link Update}.
 *
 * @since 5.1.0
 */
public class JpaUpdate extends AbstractUpdate {

	private static final long serialVersionUID = 118863316193871221L;

	private final static Logger LOGGER = JpaDatastoreLogger.create();

	// Commodity factory
	@SuppressWarnings("serial")
	public static final DatastoreCommodityFactory<JpaDatastoreCommodityContext, Update> FACTORY = new DatastoreCommodityFactory<JpaDatastoreCommodityContext, Update>() {

		@Override
		public Class<? extends Update> getCommodityType() {
			return Update.class;
		}

		@Override
		public Update createCommodity(JpaDatastoreCommodityContext context) throws CommodityConfigurationException {
			return new JpaUpdate(context);
		}
	};

	private final JpaOperationContext operationContext;

	public JpaUpdate(JpaOperationContext operationContext) {
		super();
		this.operationContext = operationContext;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.datastore.operation.ExecutableOperation#execute()
	 */
	@Override
	public OperationResult execute() {

		// validate
		getConfiguration().validate();

		// composition context
		final JPQLResolutionContext context = JPQLResolutionContext.create(operationContext);
		context.addExpressionResolvers(getConfiguration().getExpressionResolvers());

		// get entity class
		final Class<?> entity = context.resolveOrFail(getConfiguration().getTarget(), JpaEntity.class).getEntityClass();

		return operationContext.withEntityManager(entityManager -> {

			// Use BeanPropertySet so we can read back the @Version from the merge result
			final BeanPropertySet<Object> set = operationContext.getBeanIntrospector().getPropertySet(entity);

			// merge entity — capture the returned managed instance which carries
			// the DB-incremented @Version value
			Object mergedInstance = entityManager.merge(set.write(getConfiguration().getValue(), entity.newInstance()));

			operationContext.traceOperation("MERGE entity [" + entity.getName() + "]");

			// check auto-flush
			if (operationContext.isAutoFlush() || getConfiguration().hasWriteOption(JpaWriteOption.FLUSH)) {
				entityManager.flush();

				operationContext.traceOperation("FLUSH EntityManager");
			}

			// Always write back @Version from the merged (managed) instance into the
			// PropertyBox. Without this the caller's PropertyBox holds a stale version and
			// any subsequent update in a new transaction throws OptimisticLockException.
			writeBackVersion(entityManager, set, entity, mergedInstance, getConfiguration().getValue());

			return OperationResult.builder().type(OperationType.UPDATE).affectedCount(1).build();

		});
	}

	/**
	 * Reads the @Version attribute from the managed (post-merge) entity instance and
	 * writes its current value back into the caller's PropertyBox.
	 *
	 * <p>Hibernate increments {@code @Version} only at flush time, not during
	 * {@code merge()}. Therefore this method flushes the EntityManager first so that
	 * the managed {@code mergedInstance} carries the new version value before we read
	 * it back. A second flush on an already-clean context is a no-op, so callers that
	 * have already flushed pay no extra cost.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void writeBackVersion(EntityManager entityManager, BeanPropertySet<Object> set,
			Class<?> entity, Object mergedInstance, PropertyBox propertyBox) {
		try {
			EntityType et = entityManager.getMetamodel().entity(entity);
			et.getSingularAttributes().stream()
					.filter(a -> ((SingularAttribute) a).isVersion())
					.findFirst()
					.ifPresent(versionAttr -> {
						set.getProperty(((SingularAttribute) versionAttr).getName()).ifPresent(p -> {
							// Flush so Hibernate actually issues the UPDATE and increments the
							// version field in the managed entity before we read it back.
							entityManager.flush();
							Object versionValue = set.read((PathProperty<Object>) p, mergedInstance);
							PathPropertyBoxAdapter adapter = PathPropertyBoxAdapter.create(propertyBox);
							if (adapter.contains(p)) {
								adapter.setValue(p, versionValue);
							}
						});
					});
		} catch (Exception e) {
			LOGGER.warn("Failed to write back @Version attribute after merge", e);
		}
	}

}
