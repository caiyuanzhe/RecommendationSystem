package com.zhinengb.rs.recommendation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.storage.BrowseClickJedis;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Combine the recommenders.
 * 
 * Organized the recommenders to a person or to the whole list.
 * 
 * @author Yuanzhe Cai
 *
 */
public class CombinationRecommender {
	static public int MAX_LENGTH_PER_USER = ParameterProperty.INSTANCE
			.getRecommendConfig().getMaxLengthPerPerson();
	static public int COMBINATION_RECOMMEND_COUNT = ParameterProperty.INSTANCE
			.getRecommendConfig().getDefaultCombinationRecommendCount();

	/**
	 * proportionMap map and range array are used to calculate the probability
	 * to construct the recommender list.
	 */
	private static Map<Integer, Double> proportionMap = new HashMap<Integer, Double>();

	private static double[] range;

	static {
		// proportion
		for (EntityType entityType : EntityType.values()) {
			proportionMap.put(entityType.ordinal(),
					1.0d / EntityType.values().length);
		}

		// calculate the proportion to get the value.
		updateRange();
	}

	private static void updateRange() {
		updateRange(null);
	}

	/**
	 * Update range.
	 * 
	 * @param pid
	 */
	private static void updateRange(Integer pid) {
		if (pid != null) {

			double sum = 0.0d;

			EntityType[] entityTypes = EntityType.values();

			double[] clickNumber = new double[entityTypes.length];

			for (EntityType entityType : entityTypes) {
				int index = entityType.ordinal();
				clickNumber[index] = (double) BrowseClickJedis.INSTATNCE
						.getPersonTotalClick(pid, index)
						+ COMBINATION_RECOMMEND_COUNT;
				sum += clickNumber[index];
			}

			for (EntityType entityType : entityTypes) {
				int index = entityType.ordinal();
				proportionMap.put(index, clickNumber[index] / sum);

			}
		}

		range = new double[proportionMap.size()];
		for (int i = 0; i < range.length; i++) {
			if (i == 0) {
				range[i] = proportionMap.get(i);
			} else {
				range[i] = range[i - 1] + proportionMap.get(i);
			}
		}
	}

	private static boolean isCodeStartingUser() {
		double val = proportionMap.get(EntityType.ARTICLE.ordinal());

		EntityType[] entityTypes = EntityType.values();

		for (EntityType entityType : entityTypes) {
			if (val != proportionMap.get(entityType.ordinal())) {
				return false;
			}
		}
		return true;
	}

	private static List<Entity> coldStartingRecommenders(
			List<List<Entity>> combinePersonRecommenderList, int maxLength) {
		List<Entity> entityList = new ArrayList<Entity>();
		
		int i = 0;
		while (i < maxLength) {
			for (List<Entity> list : combinePersonRecommenderList) {
				if (list != null && list.size() > 0) {
					Entity entity = list.get(0);
					entityList.add(entity);
					list.remove(0);
					i++;
				}
				if (i == maxLength) {
					break;
				}
			}
		}

		return entityList;
	}

	/**
	 * Build the recommenders for a special person pid.
	 * 
	 * @param combinePersonRecommenderList
	 * @param pid
	 * @return
	 * @throws SQLException
	 */
	public static List<Entity> combinePersonRecommenders(
			List<Map<Integer, List<Entity>>> combinePersonRecommenderList,
			int pid) throws SQLException {

		// update range
		updateRange(pid);

		// update person
		List<List<Entity>> entityLists = new ArrayList<List<Entity>>();

		for (Map<Integer, List<Entity>> personRecEntityMap : combinePersonRecommenderList) {
			List<Entity> entityList = personRecEntityMap.get(pid);

			if (entityList == null) {
				entityList = new ArrayList<Entity>();
			}

			entityLists.add(entityList);
		}

		if (isCodeStartingUser()) {
			return coldStartingRecommenders(entityLists, MAX_LENGTH_PER_USER);
		} else {
			return combineRecommenders(entityLists, MAX_LENGTH_PER_USER);
		}
	}

	/**
	 * Build the recommenders for all the persons.
	 * 
	 * @param combinePersonRecommenderList
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, List<Entity>> combinePersonRecommenders(
			List<Map<Integer, List<Entity>>> combinePersonRecommenderList)
			throws SQLException {

		Map<Integer, List<Entity>> personRecMap = new HashMap<Integer, List<Entity>>();

		List<Integer> idlist = Util.getAllPids();

		for (int personId : idlist) {
			List<List<Entity>> entityLists = new ArrayList<List<Entity>>();

			for (Map<Integer, List<Entity>> personRecEntityMap : combinePersonRecommenderList) {
				List<Entity> entityList = personRecEntityMap.get(personId);

				if (entityList == null) {
					entityList = new ArrayList<Entity>();
				}

				entityLists.add(entityList);
			}

			personRecMap.put(
					personId,
					combineRecommenders(entityLists,
							CombinationRecommender.MAX_LENGTH_PER_USER));
		}

		return personRecMap;
	}

	/**
	 * Store all the person recommenders into the file.
	 * 
	 * @param personRecMap
	 * @param tableName
	 * @param filePath
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void storePersonRecommenders(
			Map<Integer, List<Entity>> personRecMap, String tableName,
			String filePath) throws IOException, SQLException {

		// write the data into the file
		RecommendationSystemLog.INSTANCE.write("write the data into the file.");
		Writer writer = new FileWriter(filePath);

		int i = 0;
		for (Map.Entry<Integer, List<Entity>> entry : personRecMap.entrySet()) {
			int pid = entry.getKey();
			List<Entity> entityList = entry.getValue();
			for (Entity entity : entityList) {
				writer.write(i++ + ",");
				writer.write(pid + ",");
				writer.write(entity.getId() + ",");
				writer.write(entity.getCategory() + ",");
				writer.write(EntityType.getEntityTypeStr(entity.getType())
						+ ",");
				writer.write(new Double(entity.getPersonalCombinationScore())
						.toString() + ",");
				writer.write(new Double(entity.getSimScore()).toString() + ",");
				writer.write(new Double(entity.getEvaluationScore()).toString()
						+ ",");
				writer.write(new Double(entity.getNumClick()).toString() + ",");
				writer.write(new Double(entity.getNumClickInSlotDay())
						.toString() + ",");
				writer.write(new Double(entity.getTimeSlot()).toString());

				writer.write("\n");
			}
		}

		writer.close();
		loadDataToDB(filePath, tableName);
	}

	/**
	 * Store the global recommenders into the file.
	 * 
	 * @param entityList
	 * @param tableName
	 * @param filePath
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void storeGlobalRecommenders(List<Entity> entityList,
			String tableName, String filePath) throws IOException, SQLException {

		RecommendationSystemLog.INSTANCE
				.write("write the data into the global file.");
		Writer writer = new FileWriter(filePath);

		int i = 0;
		for (Entity entity : entityList) {
			writer.write(i++ + ",");
			writer.write(entity.getId() + ",");
			writer.write(entity.getCategory() + ",");
			writer.write(EntityType.getEntityTypeStr(entity.getType()) + ",");
			writer.write(new Double(entity.getGlobalCombinationScore())
					.toString() + ",");
			writer.write(new Double(entity.getEvaluationScore()).toString()
					+ ",");
			writer.write(new Double(entity.getNumClick()).toString() + ",");
			writer.write(new Double(entity.getNumClickInSlotDay()).toString()
					+ ",");
			writer.write(new Double(entity.getTimeSlot()).toString());
			writer.write("\n");
		}

		writer.close();
		loadDataToDB(filePath, tableName);
	}

	/**
	 * Load the data into DB.
	 * 
	 * @param filePath
	 * @param tableName
	 * @throws IOException
	 * @throws SQLException
	 */
	private static void loadDataToDB(String filePath, String tableName)
			throws IOException, SQLException {
		RecommendationSystemLog.INSTANCE.write("load file into DB.");

		String sql = " LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE "
				+ tableName + " FIELDS TERMINATED BY ','";

		DB.INSTANCE.execute(sql);
	}

	/**
	 * Build the recommenders.
	 * 
	 * @param entityLists
	 * @param maxLength
	 * @return
	 */
	public static List<Entity> combineRecommenders(
			List<List<Entity>> entityLists, int maxLength) {
		List<Entity> combineEntityList = new ArrayList<Entity>();

		for (int i = 0; i < maxLength; i++) {
			Entity entity = calNextEntity(entityLists);

			if (entity == null) {
				break;
			}

			if (entity != null
					&& entity.getType() != EntityType.QUESTION.ordinal()) {
				combineEntityList.add(entity);
			}
		}

		Collections.shuffle(combineEntityList);

		return combineEntityList;
	}

	private static Entity calNextEntity(List<List<Entity>> entityLists) {
		Entity entity = null;

		while (!isEmpty(entityLists)) {
			int index = calNextIndex();
			LinkedList<Entity> entitylist = (LinkedList<Entity>) (entityLists
					.get(index));
			entity = entitylist.poll();
			if (entity != null) {
				break;
			}
		}

		return entity;
	}

	private static boolean isEmpty(List<List<Entity>> entityLists) {
		for (List<Entity> entityList : entityLists) {
			if (entityList.size() > 0) {
				return false;
			}
		}

		return true;
	}

	private static int calNextIndex() {
		double val = Math.random();

		int index = 0;
		for (int i = 0; i < range.length; i++) {
			if (val <= range[i]) {
				index = i;
				break;
			}
		}

		return index;
	}
}
