package com.zhinengb.rs.index;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.PersonFollow;
import com.zhinengb.rs.entity.Word;
import com.zhinengb.rs.entity.WordNode;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * person index.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum PersonIndex {
	INSTANCE;

	public static int MAX_NUMBER_OF_WORDS = ParameterProperty.INSTANCE
			.getRecommendConfig().getMaxNumberOfWords();

	private Map<Integer, Integer> personTotalCountMap = new HashMap<Integer, Integer>();
	private Map<Integer, Map<String, Word>> personIndex = new HashMap<Integer, Map<String, Word>>();

	private Map<Integer, String> catorgyMap = new HashMap<Integer, String>();

	// keep the most recent time stamp
	private List<Long> entityTimeStampList = new ArrayList<Long>();

	private PersonIndex() {
		for (int i = 0; i < EntityType.values().length; i++) {
			entityTimeStampList.add(ParameterProperty.INSTANCE
					.getRecommendConfig().getDefaultPersonStartRecordTime());
		}
	}

	/**
	 * Normal update
	 * 
	 * @throws SQLException
	 */
	public void autoUpdate() throws SQLException {
		List<ItemWord> personWordList = getRecentPersonWordList();
		updateIndex(personWordList);
		filterPersonIndex();
	}

	private String getPersonEntityRelationshipSQL(int entityType,
			long curTimeStamp) {
		if (entityType == EntityType.ARTICLE.ordinal()) {
			return getPersonArticleRelationshipSQL(curTimeStamp);
		}

		if (entityType == EntityType.DISCOUNT.ordinal()) {
			return getPersonDiscountRelationshipSQL(curTimeStamp);
		}

		if (entityType == EntityType.PRODUCT.ordinal()) {
			return this.getPersonProductRelationshipSQL(curTimeStamp);
		}

		if (entityType == EntityType.QUESTION.ordinal()) {
			return this.getPersonQuestionRelationshipSQL(curTimeStamp);
		}

		return null;
	}

	/**
	 * Get Person discount relationship
	 * 
	 * @param curTimeStamp
	 * @return
	 */
	private String getPersonDiscountRelationshipSQL(long curTimeStamp) {
		String sql = " select tru.person_id, aw.catergory, aw.word, aw.time, aw.count, unix_timestamp(tru.access_time) time_slot "
				+ " from "
				+ " ( "
				+ "	select entity_id, catergory, word, time, sum(count) count "
				+ "	from ( "
				+ "		select ta.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(ta.create_time) time, count(*) count "
				+ "		from t_promotion_article ta, t_device_base td, t_brand tb, t_devicetype tdt "
				+ "		where ta.device_id = td.id and td.brand = tb.id and tdt.id = ta.type "
				+ "		group by ta.id, tb.name, tdt.name "
				+ "		UNION "
				+ "		select ta.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(ta.create_time) time, count(*) count "
				+ "		from t_promotion_article ta, t_device_base td, t_brand tb, t_devicetype tdt "
				+ "		where ta.device_id = td.id and td.brand = tb.id and tdt.id = ta.type "
				+ "		group by ta.id, tdt.name, td.model_name "
				+ "		) tmp_aw "
				+ "		group by entity_id, catergory, word, time "
				+ "	) aw, t_rs_person_entity tru "
				+ " where aw.entity_id = tru.entity_id and unix_timestamp(tru.access_time) > (unix_timestamp(now()) - "
				+ curTimeStamp + ")";

		return sql;
	}

	/**
	 * Get Person question relationship
	 * 
	 * @param curTimeStamp
	 * @return
	 */
	private String getPersonQuestionRelationshipSQL(long curTimeStamp) {
		String sql = " select tru.person_id, aw.catergory, aw.word, aw.time, aw.count, unix_timestamp(tru.access_time) time_slot "
				+ " from "
				+ " ( "
				+ " select entity_id, catergory, word, time, sum(count) count "
				+ " from ( "
				+ " 	select ta.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(ta.create_time) time, count(*) count "
				+ " 	from t_question ta, "
				+ " 	(select distinct device_id, question_id from  t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt "
				+ "		where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category "
				+ " 	group by ta.id, tb.name, tdt.name "
				+ " 	UNION "
				+ " 	select ta.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(ta.create_time) time, count(*) count "
				+ " 	from t_question ta, "
				+ " 	(select distinct device_id, question_id from  t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt "
				+ " 	where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category  "
				+ " 	group by ta.id, tdt.name, td.model_name "
				+ "     UNION "
				+ "		select ta.id entity_id, tdt.name catergory, tu.full_name word, unix_timestamp(ta.create_time) time, count(*) count "
				+ " 	from t_question ta, "
				+ "		(select distinct device_id, question_id from t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt, t_user tu "
				+ " 	where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and tu.id = ta.author_id "
				+ "		group by ta.id, tdt.name, tu.full_name "
				+ " 	) tmp_aw "
				+ " 	group by entity_id, catergory, word, time "
				+ " ) aw, t_rs_person_entity tru "
				+ " where aw.entity_id = tru.entity_id and unix_timestamp(tru.access_time) > (unix_timestamp(now()) - "
				+ curTimeStamp + ")";
		return sql;
	}

	/**
	 * Get Person product relationship
	 * 
	 * @param curTimeStamp
	 * @return
	 */
	private String getPersonProductRelationshipSQL(long curTimeStamp) {
		String sql = " select tru.person_id, aw.catergory, aw.word, aw.time, aw.count, unix_timestamp(tru.access_time) time_slot "
				+ " from "
				+ " ( "
				+ "	select entity_id, catergory, word, time, sum(count) count "
				+ "	from ( "
				+ "				select td.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(td.publish_date) time, count(*) count "
				+ "				from t_device_base td, t_brand tb, t_devicetype tdt "
				+ "				where td.brand = tb.id and tdt.id = td.type "
				+ "				group by td.id, tb.name, tdt.name "
				+ "				UNION "
				+ "				select td.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(td.publish_date) time, count(*) count "
				+ "				from t_device_base td, t_brand tb, t_devicetype tdt "
				+ "				where td.brand = tb.id and tdt.id = td.type "
				+ "				group by td.id, tdt.name, td.model_name "
				+ "		) tmp_aw "
				+ "		group by entity_id, catergory, word, time "
				+ "	) aw, t_rs_person_entity tru "
				+ " where aw.entity_id = tru.entity_id and unix_timestamp(tru.access_time) > (unix_timestamp(now()) - "
				+ curTimeStamp + ")";
		return sql;
	}

	/**
	 * Get Person article relationship
	 * 
	 * @param curTimeStamp
	 * @return
	 */
	private String getPersonArticleRelationshipSQL(long curTimeStamp) {
		String sql = " select tru.person_id, aw.catergory, aw.word, aw.time, aw.count, unix_timestamp(tru.access_time) time_slot "
				+ " from "
				+ " ( "
				+ " select entity_id, catergory, word, time, sum(count) count "
				+ " from ( "
				+ " 	select ta.id entity_id, tdt.name catergory, tta.tag word, unix_timestamp(ta.issue_time) time, count(*) count "
				+ " 	from t_article ta, t_tags_article tta,t_devicetype tdt "
				+ " 	where ta.id = tta.article_id and tdt.id = ta.category "
				+ "		group by ta.id, tdt.name, tta.tag, time "
				+ " 	UNION "
				+ " 	select ta.id entity_id, tdt.name catergory, tb.name word, unix_timestamp(ta.issue_time) time, count(*) count "
				+ " 	from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt "
				+ " 	where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category "
				+ " 	group by ta.id,tb.name, tdt.name, time "
				+ " 	UNION "
				+ " 	select ta.id entity_id,tdt.name catergory, td.model_name word, unix_timestamp(ta.issue_time) time, count(*) count "
				+ " 	from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt "
				+ " 	where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category "
				+ " 	group by ta.id, tdt.name, td.model_name, time "
				+ " 	UNION "
				+ " 	select ta.id entity_id,tdt.name catergory, tu.full_name word, unix_timestamp(ta.issue_time) time, count(*) count "
				+ " 	from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt, t_user tu "
				+ " 	where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and tu.id = ta.author_id "
				+ " 	group by ta.id, tdt.name, tu.full_name, time "
				+ " ) tmp_aw "
				+ " group by entity_id, catergory, word, time "
				+ " ) aw, t_rs_person_entity tru "
				+ " where aw.entity_id = tru.entity_id and unix_timestamp(tru.access_time) > (unix_timestamp(now()) - "
				+ curTimeStamp + ")";

		return sql;
	}

	/**
	 * Calculate Person follow relationship.
	 * 
	 * @param curTimeStamp
	 * @param itemWordList
	 * @return
	 * @throws SQLException
	 */
	private List<ItemWord> getPersonFollowRelationship(
			List<ItemWord> itemWordList) throws SQLException {
		String sql = " select truf.pid, tdt.name, truf.word,  1 count "
				+ " from t_rs_user_follow truf, t_devicetype tdt "
				+ " where truf.category = tdt.id ";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int pid = rs.getInt(1);
			String category = rs.getString(2);
			String word = rs.getString(3);
			int count = rs.getInt(4);
			// Follow time can also be stored into DB.
			long personVisitTimeSlot = System.currentTimeMillis();

			ItemWord itemWord = new ItemWord(word, category, pid, count,
					personVisitTimeSlot);
			itemWordList.add(itemWord);
		}

		return itemWordList;
	}

	/**
	 * Add non-follow data This is the person - article relationship In future,
	 * we also need to consider the person - comment / person - discount /
	 * person - product
	 * 
	 * @param sql
	 * @param itemWordList
	 * @param curTimeStamp
	 * @return
	 * @throws SQLException
	 */
	private void addPersonEntityRelationship(int entityType,
			List<ItemWord> itemWordList, long curTimeStamp, EntityType type)
			throws SQLException {
		String sql = this.getPersonEntityRelationshipSQL(entityType,
				curTimeStamp);

		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int pid = rs.getInt(1);
			String category = rs.getString(2);
			String word = rs.getString(3);
			long entityTimeStamp = rs.getLong(4);
			int count = rs.getInt(5);
			long personVisitTimeSlot = rs.getLong(6);

			if (entityTimeStamp > curTimeStamp) {
				curTimeStamp = entityTimeStamp;
			}

			// add category - word
			ItemWord itemWord = new ItemWord(word, category, pid, count,
					personVisitTimeSlot);
			itemWordList.add(itemWord);

			// add category
			itemWord = new ItemWord(null, category, pid, count,
					personVisitTimeSlot);
			itemWordList.add(itemWord);
		}
	}

	/**
	 * Person entity relationship
	 * 
	 * @param curTimeStamp
	 * @return
	 * @throws SQLException
	 */
	private List<ItemWord> getRecentPersonWordList() throws SQLException {
		// use curTimeStamp
		List<ItemWord> itemWordList = new ArrayList<ItemWord>();

		// add person-entity relationship
		for (EntityType entityType : EntityType.values()) {
			addPersonEntityRelationship(entityType.ordinal(), itemWordList,
					this.entityTimeStampList.get(entityType.ordinal()),
					entityType);
		}

		// add follow data
		getPersonFollowRelationship(itemWordList);

		return itemWordList;
	}

	/**
	 * Update index.
	 * 
	 * @param personWordList
	 * @throws SQLException
	 */
	public void updateIndex(List<ItemWord> personWordList) throws SQLException {
		// set map value
		Set<Integer> personIdSet = new HashSet<Integer>();

		for (ItemWord personWord : personWordList) {

			int pid = personWord.getId();
			String word = personWord.getWord();
			String category = personWord.getCategory();
			String key = Util.generateCategoryWordKey(category, word);

			int count = personWord.getCount();
			long timeStamp = personWord.getVisitTimeStamp();

			if (word == null) {
				continue;
			}

			personIdSet.add(pid);

			Integer totalCount = personTotalCountMap.get(pid);
			if (totalCount == null) {
				personTotalCountMap.put(pid, count);
			} else {
				personTotalCountMap.put(pid, totalCount + count);
			}

			Map<String, Word> wordCountMap = personIndex.get(pid);
			if (wordCountMap == null) {
				wordCountMap = new HashMap<String, Word>();

				Word wordEntity = new Word(word, category, count);
				wordEntity.addTimeWeight(new WordNode(timeStamp, count));
				wordCountMap.put(key, wordEntity);

				personIndex.put(pid, wordCountMap);
			} else {
				Word tWord = wordCountMap.get(word);

				if (tWord == null) {
					Word wordEntity = new Word(word, category, count);
					wordEntity.addTimeWeight(new WordNode(timeStamp, count));
					wordCountMap.put(key, wordEntity);
				} else {
					tWord.addCount(count);
					// accumulate the time weight
					tWord.addTimeWeight(new WordNode(timeStamp, count));

					wordCountMap.put(key, tWord);
				}
			}
		}

		normalizedTimeWeight(personIdSet);
	}

	/**
	 * For each user, system only records "MAX_NUMBER_OF_WORDS" (30) number of
	 * "important" words for a person.
	 */
	private void filterPersonIndex() {
		for (Map.Entry<Integer, Map<String, Word>> entry : this.personIndex
				.entrySet()) {
			Map<String, Word> wordMap = entry.getValue();

			if (wordMap.size() <= MAX_NUMBER_OF_WORDS) {
				continue;
			} else {
				List<Word> wordList = new ArrayList<Word>();

				for (Map.Entry<String, Word> wordEntry : wordMap.entrySet()) {
					wordList.add(wordEntry.getValue());
				}

				Collections.sort(wordList);

				for (int i = MAX_NUMBER_OF_WORDS; i < wordList.size(); i++) {
					Word word = wordList.get(i);
					wordMap.remove(word.getWord());
				}
			}
		}
	}

	/**
	 * Normalized Time Weight
	 * 
	 * @param personIdSet
	 * @throws SQLException
	 */
	private void normalizedTimeWeight(Set<Integer> personIdSet)
			throws SQLException {
		for (Integer pid : personIdSet) {
			Map<String, Word> wordMap = personIndex.get(pid);

			if (wordMap == null) {
				continue;
			}

			double maxCategoryWeight = 0d;
			double maxWordWeight = 0d;

			for (Map.Entry<String, Word> entry : wordMap.entrySet()) {
				Word word = entry.getValue();

				if (word.isCategory()) {
					if (maxCategoryWeight < word.getTimeWeight()) {
						maxCategoryWeight = word.getTimeWeight();
					}
				} else {
					if (maxWordWeight < word.getTimeWeight()) {
						maxWordWeight = word.getTimeWeight();
					}
				}
			}

			for (Map.Entry<String, Word> entry : wordMap.entrySet()) {
				Word word = entry.getValue();

				if (word.isCategory()) {
					word.setNormalTimeWeight(word.getTimeWeight()
							/ maxCategoryWeight);
				} else {
					word.setNormalTimeWeight(word.getTimeWeight()
							/ maxWordWeight);
				}
			}
		}
	}

	/**
	 * Return all the related words for this person.
	 * 
	 * @param pid
	 * @return
	 */
	public List<Word> getWords(int pid) {
		List<Word> wordList = new ArrayList<Word>();
		Map<String, Word> wordMap = personIndex.get(pid);

		if (wordMap != null) {
			for (Map.Entry<String, Word> entry : wordMap.entrySet()) {
				Word word = entry.getValue();
				int totalCount = personTotalCountMap.get(pid);
				double tf = Util.calTermfrequency(word.getCount(), totalCount);
				word.setTf(tf);
				wordList.add(word);
			}
		}

		return wordList;
	}

	/**
	 * Update the follow score in the list.
	 * 
	 * @param personFollowList
	 * @throws SQLException
	 */
	public void updateFollow(List<PersonFollow> personFollowList)
			throws SQLException {
		this.catorgyMap = Util.updateCatorgyMap();

		List<ItemWord> itemWordList = new ArrayList<ItemWord>();
		for (PersonFollow uf : personFollowList) {
			int pid = uf.getPid();
			String category = this.catorgyMap.get(uf.getCategory());
			String word = uf.getWord();
			int count = 1;
			long personVisitTimeSlot = System.currentTimeMillis();

			ItemWord itemWord = new ItemWord(word, category, pid, count,
					personVisitTimeSlot);
			itemWordList.add(itemWord);
		}

		this.updateIndex(itemWordList);
	}

	/**
	 * Mark some words for a special person.
	 * 
	 * @param pid
	 * @param wordList
	 */
	public void markWords(int pid, List<Word> wordList) {
		Map<String, Word> wordMap = personIndex.get(pid);

		Set<String> categorySet = new HashSet<String>();

		// Remove the category-word.
		for (Word word : wordList) {
			if (!word.isCategory()) {
				wordMap.remove(word.getKey());
			} else {
				categorySet.add(word.getCategory());
			}
		}

		// Remove all the words in this category.
		for (Map.Entry<String, Word> entry : wordMap.entrySet()) {
			String key = entry.getKey();
			String category = entry.getValue().getCategory();

			if (categorySet.contains(category)) {
				wordMap.remove(key);
			}
		}
	}

	/**
	 * Update click information.
	 * 
	 * @param personEntityList
	 * @throws SQLException
	 */
	public void updateClick(List<ItemWord> personWordList) throws SQLException {
		updateIndex(personWordList);
	}

	public static void main(String avgs[]) {
		String[] words = { "w1", "w2", "w3", "w4" };
		int[][] wordPersonCount = { { 1, 0, 3, 0 }, { 2, 3, 0, 4 } };

		List<ItemWord> personWordList = new ArrayList<ItemWord>();

		for (int i = 0; i < wordPersonCount.length; i++) {
			for (int j = 0; j < wordPersonCount[0].length; j++) {
				if (wordPersonCount[i][j] != 0) {
					ItemWord itemWord = new ItemWord(words[j], words[j], i,
							wordPersonCount[i][j]);
					personWordList.add(itemWord);
				}
			}
		}
	}

}
