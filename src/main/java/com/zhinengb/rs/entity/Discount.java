package com.zhinengb.rs.entity;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entityrs.DiscountRecommendationSystem;

/**
 * Discount info
 * 
 * @author Yuanzhe Cai
 *
 */
public class Discount extends Entity {

	public Discount(int id) {
		super(id);

		entityType = EntityType.DISCOUNT.ordinal();
		simWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getDiscountSimWeight();
		timeSlotWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getDiscountSlotWeight();
		evaluationScoreWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getDiscountEvaluationScoreWeight();
		userClickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getDiscountClickWeight();
		businessPriorityWeight = ParameterProperty.INSTANCE
				.getRecommendConfig().getDiscountBusinessPriorityWeight();
		clickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getDiscountSlotClickWeight();

		// set the default weight
		this.setEvaluationScore(DiscountRecommendationSystem.NON_WRITING_SCORE);
	}
}
