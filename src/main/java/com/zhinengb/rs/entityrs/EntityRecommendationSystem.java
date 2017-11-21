package com.zhinengb.rs.entityrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.Word;
import com.zhinengb.rs.index.EntityIndex;
import com.zhinengb.rs.index.ItemWord;
import com.zhinengb.rs.index.PersonIndex;
import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.storage.BrowseClickJedis;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Abstract recommendation class.
 * 
 * @author Yuanzhe Cai
 *
 */
public abstract class EntityRecommendationSystem {
	public static long DEFAULT_START_TIME = 0;
	public static long ONE_DAY_TIME_SLOT = 24 * 60 * 60;
	public static int DEFAULT_DAYS_SLOT = ParameterProperty.INSTANCE
			.getRecommendConfig().getPersonClickDefaultShortSlotDays();

	protected int entityType;
	protected String entityTable;
	protected String timeCol;
	protected String categoryCol;
	protected int daysSlot = DEFAULT_DAYS_SLOT;

	byte[] indexMutex = new byte[0];

	EntityIndex entityIndex = new EntityIndex();
	Map<Integer, Entity> globalQualityEntityMap;
	Map<Integer, Map<String, Double>> followMap;

	protected long startEntityTimeStamp = EntityRecommendationSystem.DEFAULT_START_TIME;
	protected long curEntityUpdateTimeStamp = EntityRecommendationSystem.DEFAULT_START_TIME;

	EntityRecommendationSystem() {

	}

	/**
	 * TODO: This function calculate the global recommender lists
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Entity> calGlobalRecommenders() throws SQLException {
		Map<Integer, Entity> entityMap = queryEntitiesStatistic();
		List<Entity> entityList = new LinkedList<Entity>();

		for (Map.Entry<Integer, Entity> entry : entityMap.entrySet()) {
			entityList.add(entry.getValue());
		}

		Collections.sort(entityList);

		return entityList;
	}

	/**
	 * This function calculate all the recommenders for each person.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, List<Entity>> calPersonRecommenders() throws Exception {
		// update index
		RecommendationSystemLog.INSTANCE.write("update entity index.");
		autoUpdateEntityIndex();

		// update recommender
		RecommendationSystemLog.INSTANCE.write("update entity recommender.");
		return autoUpdatePersonRecommender();
	}

	/**
	 * update person recommender
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, List<Entity>> autoUpdatePersonRecommender()
			throws Exception {
		// build statistic info
		RecommendationSystemLog.INSTANCE.write("build statistic info.");
		globalQualityEntityMap = queryEntitiesStatistic();

		// calculate follow score
		RecommendationSystemLog.INSTANCE.write("calculate follow score.");
		followMap = calFollowPerPerson();

		// calculate the recommender for each person
		RecommendationSystemLog.INSTANCE
				.write("calculate the recommender for each person.");
		Map<Integer, List<Entity>> rsMap = calRecommenderPerPersons(
				globalQualityEntityMap, followMap);

		return rsMap;
	}

	/**
	 * update entity index
	 * 
	 * @throws SQLException
	 */
	protected void autoUpdateEntityIndex() throws SQLException {
		// update reverse index
		List<ItemWord> itemWordList = getWordEntityList();
		entityIndex.update(itemWordList);
	}

	/**
	 * Follow score will set the weight for each words. This score is used in
	 * the similarity calculation.
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected Map<Integer, Map<String, Double>> calFollowPerPerson()
			throws SQLException {
		Map<Integer, Map<String, Double>> followMap = new HashMap<Integer, Map<String, Double>>();

		String sql = " select distinct truf.pid pid, truf.word, td.name, truf.follow_score fs "
				+ " from t_rs_user_follow truf, t_devicetype td "
				+ " where truf.entity_type = '"
				+ EntityType.getEntityTypeStr(this.getType())
				+ "' and truf.category = td.id ";

		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int personId = rs.getInt(1);
			String word = rs.getString(2);
			String category = rs.getString(3);
			double followScore = rs.getDouble(4);

			String key = Util.generateCategoryWordKey(category, word);

			Map<String, Double> wordScoreMap = followMap.get(personId);
			if (wordScoreMap != null) {
				wordScoreMap.put(key, followScore);
			} else {
				wordScoreMap = new HashMap<String, Double>();
				wordScoreMap.put(key, followScore);
				followMap.put(personId, wordScoreMap);
			}
		}
		rs.close();
		
		return followMap;
	}

	/**
	 * Calculate the recommender for each person.
	 * 
	 * @param entityInfoMap
	 * @param followMap
	 * @param filterMap
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, List<Entity>> calRecommenderPerPersons(
			Map<Integer, Entity> entityInfoMap,
			Map<Integer, Map<String, Double>> followMap) throws Exception {
		Map<Integer, List<Entity>> personEntitiesMap = new HashMap<Integer, List<Entity>>();

		List<Integer> personList = Util.getAllPids();

		for (Integer pid : personList) {
			List<Entity> entityList = new LinkedList<Entity>();

			// get word list.
			List<Word> wordList = PersonIndex.INSTANCE.getWords(pid);

			// set follow score.
			// default follow score is 0.5.
			for (Word word : wordList) {
				if (followMap.containsKey(pid)
						&& followMap.get(pid).containsKey(word.getWord())) {
					word.setFollowScore(followMap.get(pid).get(word.getWord()));
				} else {
					word.setFollowScore(Word.DEFAULT_FOLLOW_SCORE);
				}
			}

			// calculate the similarity score
			Map<Integer, Double> entitySimMap = entityIndex
					.getSimScore(wordList);

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
			personEntitiesMap.put(pid, entityList);
		}

		return personEntitiesMap;
	}

	public void runBackGroud() throws SQLException {
		synchronized (indexMutex) {
			autoUpdateEntityIndex();
		}
	}

	protected abstract String getWordEntitySql(List<Integer> entityIdList);

	private List<ItemWord> getWordEntityList() throws SQLException {
		return getWordEntityList(null);
	}

	protected List<ItemWord> getWordEntityList(List<Integer> entityIdList)
			throws SQLException {
		List<ItemWord> itemWordList = new LinkedList<ItemWord>();

		String sql = getWordEntitySql(entityIdList);
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			String category = rs.getString(2);
			String word = rs.getString(3);
			int count = rs.getInt(5);

			ItemWord itemWord = new ItemWord(word, category, entityId, count);
			itemWordList.add(itemWord);

			itemWord = new ItemWord(null, category, entityId, count);
			itemWordList.add(itemWord);
		}
		rs.close();
		
		return itemWordList;
	}

	/**
	 * Return type.
	 * 
	 * @return
	 */
	public int getType() {
		return entityType;
	}

	/**
	 * Get day slot.
	 * 
	 * @return
	 */
	public int getDaysSlot() {
		return this.daysSlot;
	}

	/**
	 * Calculate entity quality.
	 * 
	 * @param entityMap
	 * @throws SQLException
	 */
	abstract protected void calQuality(Map<Integer, Entity> entityMap)
			throws SQLException;

	abstract protected Entity getEntity(int id);

	private Map<Integer, Entity> queryEntitiesStatistic() throws SQLException {
		Map<Integer, Entity> entityMap = new HashMap<Integer, Entity>();

		calTimeAnalysis(entityMap, entityTable, timeCol);
		calQuality(entityMap);
		calCategory(entityMap, entityTable, categoryCol);
		calClickHistory(entityMap, entityTable, timeCol);
		calShortTermClick(entityMap, entityTable, timeCol);

		return entityMap;
	}

	/**
	 * Time analysis.
	 * 
	 * @param entityMap
	 * @param tableName
	 * @param timeCol
	 * @throws SQLException
	 */
	protected void calTimeAnalysis(Map<Integer, Entity> entityMap,
			String tableName, String timeCol) throws SQLException {
		// basic info time
		String sql = "select tn.id id, unix_timestamp(now()) - unix_timestamp(tn."
				+ timeCol
				+ ") time_slot "
				+ " from "
				+ tableName
				+ " tn where tn.deleted = 0";

		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			long slot = rs.getLong(2);

			Entity entity = getEntity(entityId);
			entity.setTimeSlot(slot);
			entityMap.put(entityId, entity);
		}
		rs.close();
	}

	/**
	 * Get all the click info
	 * 
	 * @param entityMap
	 * @param tableName
	 * @param timeCol
	 * @throws SQLException
	 */
	protected void calClickHistory(Map<Integer, Entity> entityMap,
			String tableName, String timeCol) throws SQLException {

		String sql = " select max(tt.total_click) "
				+ " from (select tn.id id, (count(*) / ((unix_timestamp(now())-unix_timestamp(tn."
				+ timeCol + " )) / ( "
				+ EntityRecommendationSystem.ONE_DAY_TIME_SLOT
				+ " ))) total_click " + " from " + tableName
				+ " tn, t_rs_person_entity tru "
				+ " where tn.id = tru.entity_id and tru.entity_type = '"
				+ this.getType() + "' " + " group by id) tt";

		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		rs.next();
		double maxClickScore = rs.getDouble(1);
		rs.close();
		
		sql = " select tn.id id, (count(*) / ((unix_timestamp(now())-unix_timestamp(tn."
				+ timeCol
				+ ")) / ( "
				+ EntityRecommendationSystem.ONE_DAY_TIME_SLOT
				+ " ))) total_click "
				+ " from "
				+ tableName
				+ " tn, t_rs_person_entity tru "
				+ " where tn.id = tru.entity_id and tru.entity_type = '"
				+ this.getType() + "' " + " group by id ";
		rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			double totalClick = rs.getDouble(2) / maxClickScore;

			if (entityMap.containsKey(entityId)) {
				Entity entity = entityMap.get(entityId);
				entity.setNumClick(totalClick);
			}
		}
		rs.close();
	}

	/**
	 * Get category
	 * 
	 * @param entityMap
	 * @param tableName
	 * @param categoryCol
	 * @throws SQLException
	 */
	protected void calCategory(Map<Integer, Entity> entityMap,
			String tableName, String categoryCol) throws SQLException {
		// basic info time
		String sql = "select tn.id id, tn." + categoryCol + " category "
				+ " from " + tableName + " tn ";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			int category = rs.getInt(2);

			if (entityMap.containsKey(entityId)) {
				Entity entity = entityMap.get(entityId);
				entity.setCategory(category);
			}
		}
		rs.close();
	}

	/**
	 * Get short term click info
	 * 
	 * @param entityMap
	 * @param tableName
	 * @param timeCol
	 * @throws SQLException
	 */
	protected void calShortTermClick(Map<Integer, Entity> entityMap,
			String tableName, String timeCol) throws SQLException {

		String sql = " select max(tt.slot_days_click) from (select tn.id id, count(*) / "
				+ this.getDaysSlot()
				+ " slot_days_click "
				+ " from "
				+ tableName
				+ " tn, t_rs_person_entity tru "
				+ " where tn.id = tru.entity_id and tru.entity_type = '"
				+ this.getType()
				+ "' and unix_timestamp(tn."
				+ timeCol
				+ ") > ( "
				+ " unix_timestamp(now()) - "
				+ this.getDaysSlot()
				* EntityRecommendationSystem.ONE_DAY_TIME_SLOT
				+ " ) group by tn.id) tt";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		rs.next();
		Double maxSlotClickScore = rs.getDouble(1);
		rs.close();
		
		sql = " select tn.id id, count(*) / " + this.getDaysSlot()
				+ " slot_days_click " + " from " + tableName
				+ " tn, t_rs_person_entity tru "
				+ " where tn.id = tru.entity_id and tru.entity_type = '"
				+ this.getType() + "' and unix_timestamp(tn." + timeCol
				+ ") > ( " + " unix_timestamp(now()) - " + this.getDaysSlot()
				* EntityRecommendationSystem.ONE_DAY_TIME_SLOT
				+ " ) group by tn.id ";
		rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			double slotDaysClick = rs.getDouble(2) / maxSlotClickScore;

			if (entityMap.containsKey(entityId)) {
				Entity entity = entityMap.get(entityId);
				entity.setNumClickInSlotDay(slotDaysClick);
			}
		}
		rs.close();
	}

}
