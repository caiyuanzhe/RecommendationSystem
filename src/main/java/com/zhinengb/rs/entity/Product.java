package com.zhinengb.rs.entity;

import com.zhinengb.rs.config.ParameterProperty;

/**
 * Product info
 * 
 * @author Yuanzhe Cai
 *
 */
public class Product extends Entity {

	public Product(int id) {
		super(id);
		entityType = EntityType.PRODUCT.ordinal();
		simWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getProductSimWeight();
		timeSlotWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getProductSlotWeight();
		evaluationScoreWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getProductEvaluationScoreWeight();
		userClickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getProductEvaluationScoreWeight();
		businessPriorityWeight = ParameterProperty.INSTANCE
				.getRecommendConfig().getProductBusinessPriorityWeight();
		clickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getProductSlotClickWeight();
	}

}
