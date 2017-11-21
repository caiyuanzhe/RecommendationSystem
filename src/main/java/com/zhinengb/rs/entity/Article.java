package com.zhinengb.rs.entity;

import com.zhinengb.rs.config.ParameterProperty;

/**
 * Article info.
 * 
 * @author Yuanzhe Cai
 *
 */
public class Article extends Entity {

	public Article(int id) {
		super(id);

		entityType = EntityType.ARTICLE.ordinal();
		simWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getArticleSimWeight();
		timeSlotWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getArticleSlotWeight();
		evaluationScoreWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getArticleEvaluationScoreWeight();
		userClickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getArticleClickWeight();
		businessPriorityWeight = ParameterProperty.INSTANCE
				.getRecommendConfig().getArticleBusinessPriorityWeight();
		clickWeight = ParameterProperty.INSTANCE.getRecommendConfig()
				.getArticleSlotClickWeight();
	}
}
