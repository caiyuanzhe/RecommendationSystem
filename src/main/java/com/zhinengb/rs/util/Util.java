package com.zhinengb.rs.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.PersonFollow;
import com.zhinengb.rs.entityrs.EntityRecommendationSystem;
import com.zhinengb.rs.storage.DB;

/**
 * Util class
 * 
 * @author Yuanzhe Cai
 *
 */
public class Util {
	public static double alf = 1.05;
	public static String SPLIT_TOKEN = "-";

	/**
	 * TF equation follows "http://jz.docin.com/p-380702362.html".
	 * 
	 * @param count
	 * @param totalCount
	 * @return
	 */
	public static double calTermfrequency(int count, int totalCount) {
		return (double) (count) / (double) (totalCount);
	}

	/**
	 * IDF equation follows
	 * "https://stackoverflow.com/questions/4826497/inverse-document-frequency".
	 * 
	 * @param numDocs
	 * @param totalNumDocs
	 * @return
	 */
	public static double calInverseDocumentFrequency(int numDocs,
			int totalNumDocs) {
		return Math.log((double) (totalNumDocs) / ((double) (numDocs) + 1));
	}

	/**
	 * Get category word key.
	 * 
	 * @param category
	 * @param word
	 * @return
	 */
	public static String generateCategoryWordKey(String category, String word) {
		if (word == null || word.length() == 0) {
			return category;
		} else {
			return category + Util.SPLIT_TOKEN + word;
		}
	}

	/**
	 * Get category from the category word key.
	 * 
	 * @param key
	 * @return
	 */
	public static String getCategory(String key) {
		return key.split(SPLIT_TOKEN)[0];
	}

	/**
	 * Time weight.
	 * 
	 * @param x
	 * @return
	 */
	public static double reverseFunc(double x) {
		return 1.0 / (1 + Math.pow(Util.alf, x));
	}

	/**
	 * Analyze the time factor for each word.
	 * 
	 * @param days
	 *            : Days is the time slot.
	 * @param count
	 *            : Count is the number of times to visit this word.
	 * @return
	 */
	public static double wordTimeFunc(double days, int count) {
		return (double) count / (double) (1 + Math.pow(Util.alf, days));
	}

	/**
	 * Calculate the days from now().
	 * 
	 * @param timeSlot
	 * @return
	 */
	public static double getDays(long timeSlot) {
		return (double) timeSlot
				/ (double) EntityRecommendationSystem.ONE_DAY_TIME_SLOT;
	}

	/**
	 * Get all person ids.
	 * 
	 * @return
	 * @throws SQLException
	 */

	public static List<Integer> getAllPids() throws SQLException {
		List<Integer> idsList = new ArrayList<Integer>();

		String sql = "select distinct id from t_follow_uid_guid";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			idsList.add(rs.getInt(1));
		}

		return idsList;
	}

	/**
	 * Get old table name.
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static String getUpdateTableName(String name) throws SQLException {
		String sql = " select name from t_parameter " + " where name like '"
				+ name + "%'" + " order by time limit 1";

		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		rs.next();

		return rs.getString(1);
	}

	/**
	 * Get recent table name.
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static String getActiveTableName(String name) throws SQLException {
		String sql = " select name from t_parameter " + " where name like '"
				+ name + "%'" + " order by time desc limit 1";

		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		rs.next();

		return rs.getString(1);
	}

	/**
	 * Update parameter table.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public static void updateParamters(String name) throws SQLException {
		String sql = "update t_parameter set time = now() where name = '"
				+ name + "'";
		DB.INSTANCE.execute(sql);
	}

	/**
	 * Create entityIdList for the SQL.
	 * 
	 * @param tableName
	 * @param id
	 * @param entityIdList
	 * @return
	 */
	public static String getEntityInListSQL(String tableName, String id,
			List<Integer> entityIdList) {
		StringBuffer sb = new StringBuffer();

		sb.append(" and ");
		sb.append(" ta.id ");
		sb.append(" in (");
		for (Integer entityId : entityIdList) {
			sb.append(entityId + ",");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append(") ");

		return sb.toString();
	}

	/**
	 * Build the category map.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, String> updateCatorgyMap() throws SQLException {
		Map<Integer, String> catorgyMap = new HashMap<Integer, String>();

		String sql = " select id, name from t_devicetype ";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			catorgyMap.put(rs.getInt(1), rs.getString(2));
		}

		return catorgyMap;
	}

	/**
	 * Get entity type key.
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public static String getEntityTypeKey(int entityType, int entityId) {
		StringBuffer sb = new StringBuffer();
		sb.append(entityType);
		sb.append(SPLIT_TOKEN);
		sb.append(entityId);

		return sb.toString();
	}

	/**
	 * Get person follow key.
	 * 
	 * @param pid
	 * @param fid
	 * @return
	 */
	public static String getPersonFollowKey(int pid, int fid) {
		StringBuffer sb = new StringBuffer();
		sb.append(pid);
		sb.append(SPLIT_TOKEN);
		sb.append(fid);

		return sb.toString();
	}

	/**
	 * Update person follow score by the follow id and person id
	 * 
	 * @param personFollowList
	 * @return
	 * @throws SQLException
	 */
	public static List<PersonFollow> getPersonFollow(
			List<PersonFollow> personFollowList) throws SQLException {

		Map<String, PersonFollow> personFollowMap = new HashMap<String, PersonFollow>();

		for (PersonFollow personFollow : personFollowList) {
			String key = getPersonFollowKey(personFollow.getPid(),
					personFollow.getFid());
			personFollowMap.put(key, personFollow);
		}

		StringBuffer sb = new StringBuffer();
		sb.append(" select * from t_rs_user_follow where ");
		for (PersonFollow personFollow : personFollowList) {
			sb.append(" (pid = " + personFollow.getPid() + " and fid = "
					+ personFollow.getFid() + ") or");
		}
		sb.delete(sb.length() - 2, sb.length());
		ResultSet rs = DB.INSTANCE.executeQuery(sb.toString());

		List<PersonFollow> resultPersonFollow = new ArrayList<PersonFollow>();
		while (rs.next()) {
			String key = getPersonFollowKey(rs.getInt(2), rs.getInt(3));
			PersonFollow pf = personFollowMap.get(key);

			pf.setCategory(rs.getInt(5));
			pf.setWord(rs.getString(4));

			resultPersonFollow.add(pf);
		}

		return resultPersonFollow;
	}

	/**
	 * Build global quality list. The quality list is sorted by the quality.
	 * 
	 * @param map
	 * @return
	 */
	public static List<Entity> buildGlobalQualityEntityList(
			Map<Integer, Entity> map) {

		List<Entity> entityList = new ArrayList<Entity>();

		for (Map.Entry<Integer, Entity> entry : map.entrySet()) {
			entityList.add(entry.getValue());
		}

		return entityList;
	}
}
