package com.zhinengb.rs.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zhinengb.rs.entity.Word;
import com.zhinengb.rs.util.Util;

/**
 * The detail information shows in http://jz.docin.com/p-380702362.html. Build
 * the entity index.
 * 
 * @author Yuanzhe Cai
 *
 */
public class EntityIndex {
	// Use to search the related words for the entity.
	private Map<Integer, List<Word>> entityWordMap = new HashMap<Integer, List<Word>>();

	// Use for tf (doc reverse index)
	private Map<String, Map<Integer, EntityIndexNode>> tfIndex = new HashMap<String, Map<Integer, EntityIndexNode>>();
	private Map<Integer, Integer> entityTotalWordMap = new HashMap<Integer, Integer>();

	// Use for idf
	private Map<String, Integer> idfIndex = new HashMap<String, Integer>();
	private int totalDocs;

	/**
	 * update index by (entity, word, count).
	 * 
	 * @param entityWordList
	 */
	public void update(List<ItemWord> entityWordList) {
		Set<Integer> entitySet = new HashSet<Integer>();

		for (ItemWord ewr : entityWordList) {
			String category = ewr.getCategory();
			String word = ewr.getWord();
			String key = Util.generateCategoryWordKey(category, word);
			int entityId = ewr.getId();
			int count = ewr.getCount();

			// store tf
			Map<Integer, EntityIndexNode> wordEntityMap = tfIndex.get(key);
			if (wordEntityMap == null) {
				wordEntityMap = new HashMap<Integer, EntityIndexNode>();

				EntityIndexNode node = new EntityIndexNode(count);
				wordEntityMap.put(entityId, node);

				tfIndex.put(key, wordEntityMap);
			} else {
				EntityIndexNode node = new EntityIndexNode(count);
				wordEntityMap.put(entityId, node);
			}

			Integer val = entityTotalWordMap.get(entityId);
			if (val == null) {
				entityTotalWordMap.put(entityId, count);
			} else {
				entityTotalWordMap.put(entityId, val + count);
			}

			// store idf
			entitySet.add(entityId);
			Integer eCount = idfIndex.get(key);

			if (eCount == null) {
				idfIndex.put(key, 1);
			} else {
				idfIndex.put(key, eCount + 1);
			}

			// store the entity word map
			List<Word> wordList = entityWordMap.get(entityId);
			if (wordList != null) {
				Word wordEntity = new Word(word, category, count);
				wordList.add(wordEntity);
			} else {
				wordList = new ArrayList<Word>();

				Word wordEntity = new Word(word, category, count);
				wordList.add(wordEntity);

				entityWordMap.put(entityId, wordList);
			}
		}

		totalDocs = entitySet.size();
	}

	/**
	 * Calculate similarity score
	 * 
	 * @param personWordList
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, Double> getSimScore(List<Word> personWordList)
			throws Exception {

		Map<Integer, Double> resultSimMap = new HashMap<Integer, Double>();
		Map<Integer, Double> entitySimMap = new HashMap<Integer, Double>();

		// normal person word
		double norPersonScore = 0;
		// normal doc word
		Map<Integer, Double> norDocMap = new HashMap<Integer, Double>();

		for (Word personWord : personWordList) {
			String key = personWord.getKey();

			/**
			 * we use document's idf for the Person's idf. When the data is
			 * large enough, these two idfs should be same.
			 */
			if (!idfIndex.containsKey(key)) {
				continue;
			}

			double idf = Util.calInverseDocumentFrequency(idfIndex.get(key),
					totalDocs);
			personWord.setIdf(idf);

			norPersonScore += personWord.getQueryWeight()
					* personWord.getQueryWeight();

			Map<Integer, EntityIndexNode> docMap = tfIndex.get(key);

			if (docMap != null) {
				for (Map.Entry<Integer, EntityIndexNode> entry : docMap
						.entrySet()) {
					int entityId = entry.getKey();
					EntityIndexNode node = entry.getValue();

					Word docWord = new Word(personWord.getWord(),
							personWord.getCategory());
					double tf = Util.calTermfrequency(node.getCount(),
							entityTotalWordMap.get(entityId));
					docWord.setTf(tf);
					docWord.setIdf(idf);

					Double sumSim = entitySimMap.get(entityId);
					if (sumSim != null) {
						sumSim += personWord.getQueryWeight()
								* docWord.getWeight();
					} else {
						sumSim = personWord.getQueryWeight()
								* docWord.getWeight();
					}
					entitySimMap.put(entityId, sumSim);

					Double sumDocWeight = norDocMap.get(entityId);
					if (sumDocWeight != null) {
						sumDocWeight += docWord.getWeight()
								* docWord.getWeight();
					} else {
						sumDocWeight = docWord.getWeight()
								* docWord.getWeight();
					}

					norDocMap.put(entityId, sumDocWeight);
				}
			} else {
				throw new Exception(
						"Person's word does not match with any docs.");
			}
		}

		// cosine similarity -- vector space mode
		for (Map.Entry<Integer, Double> entry : entitySimMap.entrySet()) {
			int entityId = entry.getKey();
			double weight = entry.getValue();

			weight = weight
					/ (Math.sqrt(norDocMap.get(entityId)) * Math
							.sqrt(norPersonScore));

			// System only record the value which is larger than or equal to 0.
			if (weight > 0) {
				resultSimMap.put(entityId, weight);
			}
		}

		return resultSimMap;
	}

	/**
	 * Remove the entities.
	 * 
	 * @param entityList
	 */
	public void removeEntities(List<Integer> entityList) {
		for (Integer entityId : entityList) {
			for (Map.Entry<String, Map<Integer, EntityIndexNode>> entry : tfIndex
					.entrySet()) {
				String word = entry.getKey();
				Map<Integer, EntityIndexNode> docMap = entry.getValue();

				if (docMap.containsKey(entityId)) {
					docMap.remove(entityId);

					Integer count = idfIndex.get(word);
					if (count != null) {
						if (count != 0) {
							idfIndex.put(word, count - 1);
						} else {
							idfIndex.remove(word);
						}
					}
				}
			}

			if (entityTotalWordMap.containsKey(entityId)) {
				entityTotalWordMap.remove(entityId);
			}
		}

		this.totalDocs = this.totalDocs - entityList.size();
	}

	/**
	 * Get the related words for a special entity
	 * 
	 * @param entityId
	 * @return
	 */
	public List<Word> getRelatedEntityWords(int entityId) {
		if (entityWordMap.containsKey(entityId)) {
			return this.entityWordMap.get(entityId);
		} else {
			return new ArrayList<Word>();
		}
	}

	public static void main(String avgs[]) throws Exception {

		// Case 1: Test update function.
		String[] words = { "w1", "w2", "w3", "w4" };
		int[][] wordDocCount = { { 3, 0, 1, 0 }, { 4, 2, 0, 1 },
				{ 0, 0, 3, 1 }, { 4, 3, 0, 0 } };

		List<ItemWord> entityWordList = new ArrayList<ItemWord>();

		for (int i = 0; i < wordDocCount.length; i++) {
			for (int j = 0; j < wordDocCount[0].length; j++) {
				if (wordDocCount[i][j] != 0) {
					ItemWord itemWord = new ItemWord(words[j], words[j], i,
							wordDocCount[i][j]);
					entityWordList.add(itemWord);
				}
			}
		}

		EntityIndex entityIndex = new EntityIndex();
		entityIndex.update(entityWordList);

		// Case 2: Test similarity function.
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

		PersonIndex.INSTANCE.updateIndex(personWordList);
		List<Word> wordList = PersonIndex.INSTANCE.getWords(0);
		Map<Integer, Double> entitySimMap = entityIndex.getSimScore(wordList);
		entitySimMap.clear();
	}

}
