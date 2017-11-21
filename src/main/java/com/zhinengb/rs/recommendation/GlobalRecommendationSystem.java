package com.zhinengb.rs.recommendation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.config.RecommendConfig;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entityrs.EntityRecommendationSystem;

/**
 * Update the global recommender results.
 * 
 * @author Yuanzhe Cai
 *
 */
public class GlobalRecommendationSystem extends RecommendationSystem {
	static public String GLOBAL_RECOMMENDER_RESULT = "t_rs_global_recommender_result";

	public GlobalRecommendationSystem(RecommendConfig rc) {
		super(rc);

		resultTable = GLOBAL_RECOMMENDER_RESULT;
		updateTime = ParameterProperty.INSTANCE.getRecommendConfig()
				.getGlobalRecommenderUpdateTime();
	}

	@Override
	protected void autoBuildRecommenders() throws Exception {
		List<List<Entity>> combineUserRecommenderList = new ArrayList<List<Entity>>();

		for (EntityRecommendationSystem ers : rsList) {
			combineUserRecommenderList.add(ers.calGlobalRecommenders());
		}

		storeGlobalRecommender(combineUserRecommenderList);
	}

	private void storeGlobalRecommender(
			List<List<Entity>> combineRecommenderList) throws IOException,
			SQLException {
		List<Entity> entityList = CombinationRecommender.combineRecommenders(
				combineRecommenderList, Integer.MAX_VALUE);

		String filePath = this.filePath + curTableName + ".sql";

		CombinationRecommender.storeGlobalRecommenders(entityList,
				curTableName, filePath);
	}
}
