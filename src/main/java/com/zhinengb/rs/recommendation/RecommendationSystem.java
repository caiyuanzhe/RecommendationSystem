package com.zhinengb.rs.recommendation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.config.RecommendConfig;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entityrs.QuestionRecommendationSystem;
import com.zhinengb.rs.entityrs.ArticleRecommendationSystem;
import com.zhinengb.rs.entityrs.DiscountRecommendationSystem;
import com.zhinengb.rs.entityrs.EntityRecommendationSystem;
import com.zhinengb.rs.entityrs.ProductRecommendationSystem;
import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.storage.BrowseClickJedis;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Recommendation system.
 * 
 * 1. update all person recommender.
 * 
 * 2. update active person recommender.
 * 
 * 3. update global recommender list.
 * 
 * @author Yuanzhe Cai
 *
 */
abstract class RecommendationSystem {

	protected static int MAX_NUMBER_OF_UPDATE = 1000;
	protected long updateTime;
	protected String resultTable;
	protected String filePath = "/tmp/";
	protected String curTableName;
	// protected String filePath = "d:\\";

	protected List<EntityRecommendationSystem> rsList = new ArrayList<EntityRecommendationSystem>();

	public RecommendationSystem(RecommendConfig rc) {
		try {
			ParameterProperty.INSTANCE.setRecommendConfig(rc);
			DB.INSTANCE.init();
			BrowseClickJedis.INSTATNCE.init();

			for (EntityType entityType : EntityType.values()) {
				if (entityType.ordinal() == EntityType.QUESTION.ordinal()) {
					rsList.add(new QuestionRecommendationSystem());
				}

				if (entityType.ordinal() == EntityType.ARTICLE.ordinal()) {
					rsList.add(new ArticleRecommendationSystem());
				}

				if (entityType.ordinal() == EntityType.PRODUCT.ordinal()) {
					rsList.add(new ProductRecommendationSystem());
				}

				if (entityType.ordinal() == EntityType.DISCOUNT.ordinal()) {
					rsList.add(new DiscountRecommendationSystem());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Build the recommenders.
	 * 
	 * @throws Exception
	 */
	protected abstract void autoBuildRecommenders() throws Exception;

	/**
	 * Store the person recommenders to the DB.
	 * 
	 * @param combinePersonRecommenderList
	 * @param tableName
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void storePersonRecommender(
			List<Map<Integer, List<Entity>>> combinePersonRecommenderList)
			throws SQLException, IOException {

		Map<Integer, List<Entity>> personRecMap = CombinationRecommender
				.combinePersonRecommenders(combinePersonRecommenderList);

		String curTableName = Util.getUpdateTableName(this.resultTable);
		String filePath = this.filePath + curTableName + ".sql";

		CombinationRecommender.storePersonRecommenders(personRecMap,
				curTableName, filePath);
	}

	/**
	 * Combine person recommenders.
	 * 
	 * @param combinePersonRecommenderList
	 * @param personId
	 * @return
	 * @throws SQLException
	 */
	protected List<Entity> combinePersonRecommenders(
			List<Map<Integer, List<Entity>>> combinePersonRecommenderList,
			int personId) throws SQLException {
		return CombinationRecommender.combinePersonRecommenders(
				combinePersonRecommenderList, personId);

	}

	/**
	 * PreRecommender: prepare the recommender.
	 * 
	 * 1. remove recommendation results 2. remove activity articles.
	 * 
	 * @throws SQLException
	 */
	private void preRecommender() throws SQLException {
		curTableName = Util.getUpdateTableName(resultTable);

		String sql = "truncate table " + curTableName;
		DB.INSTANCE.execute(sql);

		// TODO: delete later.
		// sql = " delete " + "from t_rs_person_entity " + " where exists "
		// + " (select * " + " from t_activity_info "
		// + " where article_id = t_rs_person_entity.entity_id) "
		// + " and entity_type = 'article_info' ";
		// DB.INSTANCE.execute(sql);
	}

	private void postRecommender() throws SQLException {
		Util.updateParamters(curTableName);
	}

	/**
	 * Run all the recommender
	 * 
	 * @throws Exception
	 */
	public void runRecommender() throws Exception {

		RecommendationSystemLog.INSTANCE.write("build recommender");

		preRecommender();
		autoBuildRecommenders();
		postRecommender();

		RecommendationSystemLog.INSTANCE.write("sleep......");

		Thread.sleep(this.updateTime);
	}

	/**
	 * Clean the recommendation system.
	 */
	protected void clear() {
		try {
			rsList.clear();
			DB.INSTANCE.close();
			BrowseClickJedis.INSTATNCE.close();
			ParameterProperty.INSTANCE.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
