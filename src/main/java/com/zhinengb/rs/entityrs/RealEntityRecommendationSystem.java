package com.zhinengb.rs.entityrs;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.recommendation.CombinationRecommender;
import com.zhinengb.rs.recommendation.GlobalRecommendationSystem;
import com.zhinengb.rs.storage.BrowseClickJedis;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.PersonFollow;
import com.zhinengb.rs.entity.Word;
import com.zhinengb.rs.index.ItemWord;
import com.zhinengb.rs.index.PersonIndex;

/**
 * The real time recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public abstract class RealEntityRecommendationSystem extends
		EntityRecommendationSystem {

	/**
	 * Update the entity index.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void updateEntityIndex() throws SQLException, IOException {
		// update entity　index
		RecommendationSystemLog.INSTANCE.write("update "
				+ EntityType.getEntityTypeStr(this.getType())
				+ " entity index...");
		RecommendationSystemLog.INSTANCE.write("build entity index...");
		autoUpdateEntityIndex();

		// build follow score
		RecommendationSystemLog.INSTANCE.write("build follow score...");
		followMap = calFollowPerPerson();
	}

	/**
	 * Get all the recommenders for a person.
	 * 
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, List<Entity>> getPersonRecommenders(int pid)
			throws Exception {
		RecommendationSystemLog.INSTANCE.write("get "
				+ EntityType.getEntityTypeStr(this.getType())
				+ " recommendation system...");
		// build statistic info
		globalQualityEntityMap = queryEntitiesStatistic();

		// calculate the recommender for each user
		Map<Integer, List<Entity>> rsMap = calRecommenderPerPerson(
				globalQualityEntityMap, followMap, pid);

		return rsMap;
	}

	private Map<Integer, Entity> queryEntitiesStatistic() throws SQLException {
		Map<Integer, Entity> entityMap = new HashMap<Integer, Entity>();

		String curTable = Util
				.getActiveTableName(GlobalRecommendationSystem.GLOBAL_RECOMMENDER_RESULT);

		String sql = "select entity_id, category, score " + " from " + curTable
				+ " where entity_type = '"
				+ EntityType.getEntityTypeStr(this.getType()) + "'";
		Statement statement = DB.INSTANCE.getConn().createStatement();
		ResultSet rs = statement.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			int category = rs.getInt(2);
			double globalComScore = rs.getDouble(3);

			Entity entity = this.getEntity(entityId);

			entity.setCategory(category);
			entity.setGlobalComScore(globalComScore);

			entityMap.put(entityId, entity);
		}
		rs.close();
		return entityMap;
	}

	private Map<Integer, List<Entity>> calRecommenderPerPerson(
			Map<Integer, Entity> entityInfoMap,
			Map<Integer, Map<String, Double>> followMap, int pid)
			throws Exception {
		Map<Integer, List<Entity>> userEntitiesMap = new HashMap<Integer, List<Entity>>();

		List<Entity> resultEntityList = new LinkedList<Entity>();
		List<Entity> entityList = new LinkedList<Entity>();

		// get word list.
		List<Word> wordList = PersonIndex.INSTANCE.getWords(pid);

		// set follow score.
		for (Word word : wordList) {
			if (followMap.containsKey(pid)
					&& followMap.get(pid).containsKey(word.getKey())) {
				word.setFollowScore(followMap.get(pid).get(word.getKey()));
			} else {
				word.setFollowScore(Word.DEFAULT_FOLLOW_SCORE);
			}
		}

		// calculate the similarity score
		Map<Integer, Double> entitySimMap = entityIndex.getSimScore(wordList);

		for (Map.Entry<Integer, Double> entry : entitySimMap.entrySet()) {
			int entityId = entry.getKey();
			double simScore = entry.getValue();

			if (entityInfoMap.containsKey(entityId)) {

				if (!BrowseClickJedis.INSTATNCE.getBit(pid, entityType,
						entityId)) {

					Entity entity = entityInfoMap.get(entityId);
					entity.setSimScore(simScore);

					entityList.add(entity);
				}
			}
		}

		Collections.sort(entityList);

		for (int i = 0; i < CombinationRecommender.MAX_LENGTH_PER_USER
				&& i < entityList.size(); i++) {
			resultEntityList.add(entityList.get(i));
		}

		// If related entities are not enough, system will select the entities
		// from the global quality list.
		if (entityList.size() < CombinationRecommender.MAX_LENGTH_PER_USER) {

			// build the global entity rank
			List<Entity> globalQualitEntityList = Util
					.buildGlobalQualityEntityList(globalQualityEntityMap);

			for (Entity entity : globalQualitEntityList) {
				if (!BrowseClickJedis.INSTATNCE.getBit(pid, entity.getType(),
						entity.getId())) {
					resultEntityList.add(entity);
				}

				if (resultEntityList.size() >= CombinationRecommender.MAX_LENGTH_PER_USER) {
					break;
				}
			}
		}

		userEntitiesMap.put(pid, resultEntityList);

		return userEntitiesMap;
	}

	/**
	 * Remove entities from the index.
	 * 
	 * @param entityIdList
	 */
	public void removeEntities(List<Integer> entityIdList) {
		synchronized (indexMutex) {
			entityIndex.removeEntities(entityIdList);
		}
	}

	/**
	 * Add entities into the index.
	 * 
	 * @param entityIdList
	 * @throws SQLException
	 */
	public void addEntities(List<Integer> entityIdList) throws SQLException {
		List<ItemWord> entityWordList = this.getWordEntityList(entityIdList);

		synchronized (indexMutex) {
			entityIndex.update(entityWordList);
		}
	}

	/**
	 * Update follow info and word.
	 *
	 * @param userFollowList
	 * @throws SQLException
	 */
	public void updateFollowMap(PersonFollow uf) throws SQLException {

		Map<Integer, String> categoryMap = Util.updateCatorgyMap();

		int pid = uf.getPid();
		Map<String, Double> wordScoreMap = followMap.get(pid);
		String word = uf.getWord();
		String category = categoryMap.get(uf.getCategory());
		String key = Util.generateCategoryWordKey(category, word);
		double followScore = uf.getFollowScore();

		if (wordScoreMap != null) {
			for (Map.Entry<String, Double> entry : wordScoreMap.entrySet()) {
				String categoryEntry = Util.getCategory(entry.getKey());
				if (categoryEntry.equals(category)) {
					wordScoreMap.put(entry.getKey(), followScore);
				}
			}

			wordScoreMap.put(key, followScore);
		} else {
			wordScoreMap = new HashMap<String, Double>();
			wordScoreMap.put(key, followScore);
		}

		RecommendationSystemLog.INSTANCE.write(uf.toString());
		RecommendationSystemLog.INSTANCE.write(wordScoreMap.toString());
	}

	/**
	 * Get words for a person.
	 * 
	 * @param args
	 */
	public List<Word> getRelatedWords(int entityId) {
		return entityIndex.getRelatedEntityWords(entityId);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
