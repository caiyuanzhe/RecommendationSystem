package com.zhinengb.rs.recommendation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.RecommendConfig;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.PersonEntity;
import com.zhinengb.rs.entity.PersonFollow;
import com.zhinengb.rs.entity.Word;
import com.zhinengb.rs.entityrs.EntityRecommendationSystem;
import com.zhinengb.rs.entityrs.RealEntityRecommendationSystem;
import com.zhinengb.rs.index.ItemWord;
import com.zhinengb.rs.index.PersonIndex;
import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.util.Util;

/**
 * Real time recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public class ZnbRealTimeRecommendationSystem extends RecommendationSystem {

	public ZnbRealTimeRecommendationSystem(RecommendConfig rc) {
		super(rc);

		try {
			autoBuildRecommenders();
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}
	}

	/**
	 * Update all the index.
	 * 
	 */
	@Override
	protected void autoBuildRecommenders() throws Exception {
		RecommendationSystemLog.INSTANCE.write("update person index.");
		PersonIndex.INSTANCE.autoUpdate();

		// update entity recommendation
		for (EntityRecommendationSystem ers : rsList) {
			((RealEntityRecommendationSystem) ers).updateEntityIndex();
		}
	}

	/**
	 * This function only add the information into the entity index.
	 * 
	 * @param type
	 *            : entity type
	 * @param entityIdList
	 *            : entity list
	 */
	public void addEntities(EntityType type, List<Integer> entityIdList) {
		try {
			if (entityIdList == null || entityIdList.size() == 0) {
				return;
			}

			RealEntityRecommendationSystem rs = (RealEntityRecommendationSystem) rsList
					.get(type.ordinal());
			rs.addEntities(entityIdList);
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}
	}

	/**
	 * This function only removes the information from the entity index.
	 * PersonIndex does not need to remove.
	 * 
	 * @param type
	 *            : entity type
	 * @param entityIdList
	 *            : entity list
	 */
	public void removeEntities(EntityType type, List<Integer> entityIdList) {
		try {
			if (entityIdList == null || entityIdList.size() == 0) {
				return;
			}

			RealEntityRecommendationSystem rs = (RealEntityRecommendationSystem) rsList
					.get(type.ordinal());
			rs.removeEntities(entityIdList);
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}
	}

	/**
	 * Add the click records.
	 * 
	 * @param personEntityList
	 *            : person entity
	 */
	public void addClickRecords(List<PersonEntity> personEntityList) {
		try {
			List<ItemWord> personWordList = new ArrayList<ItemWord>();

			for (PersonEntity personEntity : personEntityList) {
				int personId = personEntity.getPid();
				int entityType = personEntity.getEntityType();
				int entityId = personEntity.getEntityId();

				List<Word> wordList = ((RealEntityRecommendationSystem) this.rsList
						.get(entityType)).getRelatedWords(entityId);

				for (Word word : wordList) {
					ItemWord itemWord = new ItemWord(word.getWord(),
							word.getCategory(), personId, word.getCount(),
							personEntity.getAccessTime().getTime());
					personWordList.add(itemWord);
				}
			}

			PersonIndex.INSTANCE.updateClick(personWordList);
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}

	}

	/**
	 * Update the follow.
	 * 
	 * @param personFollowList
	 *            : follow info
	 */
	public void updateFollow(List<PersonFollow> personFollowList) {
		try {
			// 1. update person follow list.
			personFollowList = Util.getPersonFollow(personFollowList);

			// 2. update follow map for each entity.
			for (PersonFollow uf : personFollowList) {
				RealEntityRecommendationSystem recommendSystem = (RealEntityRecommendationSystem) rsList
						.get(uf.getEntityType());
				recommendSystem.updateFollowMap(uf);
			}

			// 3. update person index.
			PersonIndex.INSTANCE.updateFollow(personFollowList);
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}
	}

	/**
	 * Calculate the recommenders for a special person.
	 * 
	 * @param pid
	 *            : person id
	 * @return : return entity list
	 */
	public List<Entity> getRecommenders(int pid) {
		List<Entity> entityList = null;
		try {
			List<Map<Integer, List<Entity>>> combinePersonRecommenderList = new ArrayList<Map<Integer, List<Entity>>>();
			for (int i = 0; i < rsList.size(); i++) {
				RealEntityRecommendationSystem ers = (RealEntityRecommendationSystem) rsList
						.get(i);
				combinePersonRecommenderList
						.add(ers.getPersonRecommenders(pid));
			}

			entityList = this.combinePersonRecommenders(
					combinePersonRecommenderList, pid);
			addRelatedWords(entityList);
		} catch (Throwable e) {
			e.printStackTrace();
			RecommendationSystemLog.INSTANCE.write(e.toString());
		}
		return entityList;
	}

	/**
	 * Add the related words for these entities.
	 * 
	 * @param entityList
	 */
	private void addRelatedWords(List<Entity> entityList) {
		for (Entity entity : entityList) {
			int entityId = entity.getId();
			int entityType = entity.getType();

			List<Word> relatedWordList = ((RealEntityRecommendationSystem) this.rsList
					.get(entityType)).getRelatedWords(entityId);
			entity.setRelatedWordList(relatedWordList);
		}
	}

	/**
	 * Mark the words for a special person.
	 * 
	 * Remember: This word is constructed as "category" or "category-word".
	 * 
	 * @param pid
	 * @param word
	 */
	public void markWords(int pid, List<Word> wordList) {
		PersonIndex.INSTANCE.markWords(pid, wordList);
	}

	public static void main(String avgs[]) {
		long startTime = 0;
		long endTime = 0;

		// case 0: build TR
		System.out.println("Build ZnbRealTimeRecommendationSystem...");
		startTime = System.currentTimeMillis();
		RecommendConfig rc = new RecommendConfig(true);
		ZnbRealTimeRecommendationSystem znbRS = new ZnbRealTimeRecommendationSystem(
				rc);
		endTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endTime - startTime) + " ms.");

		// case 0.1

		System.out.println("Add the follow...");
		startTime = System.currentTimeMillis();

		List<PersonFollow> personFollowList = new ArrayList<PersonFollow>();

		for (int i = 0; i < EntityType.values().length; i++) {
			PersonFollow pf = new PersonFollow(68685, 3, null, 31, i, 1.0);
			personFollowList.add(pf);
		}

		znbRS.updateFollow(personFollowList);

		endTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endTime - startTime) + " ms.");
		//
		// case 1: get person id
		System.out.println("Get recommend results...");
		int personId = 34012;

		startTime = System.currentTimeMillis();
		List<Entity> entityList = znbRS.getRecommenders(personId);
		endTime = System.currentTimeMillis();

		for (Entity entity : entityList) {
			System.out.println(entity);
		}
		System.out.println("Running time: " + (endTime - startTime) + " ms.");

		// case 2: add entities
		System.out.println("Add entity...");
		startTime = System.currentTimeMillis();
		List<Integer> entityIdList = new ArrayList<Integer>();
		entityIdList.add(10000);

		znbRS.addEntities(EntityType.ARTICLE, entityIdList);

		endTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endTime - startTime) + " ms.");
		// case 3: remove entities

		System.out.println("Remove entity...");
		startTime = System.currentTimeMillis();
		entityIdList = new ArrayList<Integer>();
		entityIdList.add(10000);

		znbRS.addEntities(EntityType.ARTICLE, entityIdList);

		endTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endTime - startTime) + " ms.");

		// case 4: add click info
		System.out.println("Add click info...");
		startTime = System.currentTimeMillis();

		List<PersonEntity> personEntityList = new ArrayList<PersonEntity>();
		PersonEntity pe = new PersonEntity(33886, 19129, 0, new Timestamp(
				System.currentTimeMillis()));
		personEntityList.add(pe);
		znbRS.addClickRecords(personEntityList);

		endTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endTime - startTime) + " ms.");

		// case 5: add the follow
		// System.out.println("Add the follow...");
		// startTime = System.currentTimeMillis();
		//
		// List<PersonFollow> personFollowList = new ArrayList<PersonFollow>();
		// PersonFollow pf = new PersonFollow(48757, 1, null, -1, 0, 0.84d);
		// personFollowList.add(pf);
		//
		// znbRS.updateFollow(personFollowList);
		//
		// endTime = System.currentTimeMillis();
		// System.out.println("Running time: " + (endTime - startTime) +
		// " ms.");
	}
}
