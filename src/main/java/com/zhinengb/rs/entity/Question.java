package com.zhinengb.rs.entity;

import com.zhinengb.rs.config.ParameterProperty;

/**
 * Question info.
 * 
 * @author Yuanzhe Cai
 *
 */
public class Question extends Entity {

	public Question(int id) {
		super(id);
		entityType = EntityType.QUESTION.ordinal();
		simWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getQuestionSimWeight();
		timeSlotWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getQuestionSlotWeight();
		evaluationScoreWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getQuestionEvaluationScoreWeight();
		userClickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getQuestionClickWeight();
		businessPriorityWeight = ParameterProperty.INSTANCE
				.getRecommendConfig().getQuestionBusinessPriorityWeight();
		clickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getQuestionSlotClickWeight();
	}

}
