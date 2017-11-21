package com.zhinengb.rs.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.config.RecommendConfig;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entityrs.EntityRecommendationSystem;
import com.zhinengb.rs.index.PersonIndex;

/**
 * Update all person.
 * 
 * @author Yuanzhe Cai
 *
 */
public class AllPersonRecommendationSystem extends RecommendationSystem {
	static public String ALL_USER_RECOMMENDER_RESULT = "t_rs_all_person_recommender_result";

	public AllPersonRecommendationSystem(RecommendConfig rc) {
		super(rc);

		resultTable = ALL_USER_RECOMMENDER_RESULT;
		updateTime = ParameterProperty.INSTANCE.getRecommendConfig()
				.getGlobalRecommenderUpdateTime();
	}

	@Override
	protected void autoBuildRecommenders() throws Exception {
		// TODO Auto-generated method stub

		// update person index
		PersonIndex.INSTANCE.autoUpdate();

		// update entity recommendation
		List<Map<Integer, List<Entity>>> combinePersonRecommenderList = new ArrayList<Map<Integer, List<Entity>>>();
		for (EntityRecommendationSystem ers : rsList) {
			combinePersonRecommenderList.add(ers.calPersonRecommenders());
		}

		storePersonRecommender(combinePersonRecommenderList);
	}
}
