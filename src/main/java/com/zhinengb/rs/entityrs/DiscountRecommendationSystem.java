package com.zhinengb.rs.entityrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Discount;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Discount recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public class DiscountRecommendationSystem extends
		RealEntityRecommendationSystem {
	private static String DISCOUNT_TABLE = "t_promotion_article";
	private static String DISCOUNT_TIME_COL = "create_time";
	private static String DISCOUNT_CATEGORY_COL = "type";

	public static double WRITING_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getDiscountWritingScore();
	public static double NON_WRITING_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getDiscountNonWritingScore();
	public static long DEFAULT_START_TIME = ParameterProperty.INSTANCE
			.getRecommendConfig().getDiscountDefaultStartTime();
	public static double COUNTENT_WEIGHT = ParameterProperty.INSTANCE
			.getRecommendConfig().getDiscountContentWeight();

	public DiscountRecommendationSystem() {
		this.entityType = EntityType.DISCOUNT.ordinal();
		this.entityTable = DISCOUNT_TABLE;
		this.timeCol = DISCOUNT_TIME_COL;
		this.categoryCol = DISCOUNT_CATEGORY_COL;
		this.startEntityTimeStamp = DEFAULT_START_TIME;
	}

	@Override
	protected String getWordEntitySql(List<Integer> entityIdList) {
		StringBuffer sb = null;
		if (entityIdList != null) {
			sb = new StringBuffer();
			sb.append(Util.getEntityInListSQL("ta", "id", entityIdList));
		}

		StringBuffer sqlsb = new StringBuffer();

		sqlsb.append(" select ta.id, tdt.name, tb.name, unix_timestamp(ta.create_time), count(*) ");
		sqlsb.append(" from t_promotion_article ta, t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where ta.device_id = td.id and td.brand = tb.id and tdt.id = ta.type and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.create_time) > ");
		sqlsb.append("(unix_timestamp(now()) - " + startEntityTimeStamp + ") ");
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tb.name, tdt.name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id,tdt.name, td.model_name, unix_timestamp(ta.create_time), count(*) ");
		sqlsb.append(" from t_promotion_article ta, t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where ta.device_id = td.id and td.brand = tb.id and tdt.id = ta.type and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.create_time) > ");
		sqlsb.append("(unix_timestamp(now()) - " + startEntityTimeStamp + ") ");
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, td.model_name ");

		return sqlsb.toString();
	}

	/**
	 * Discount Quality.
	 */
	@Override
	protected void calQuality(Map<Integer, Entity> entityMap)
			throws SQLException {
		// Handwriting is higher than clawing and auto generating.
		String sql = "select id from t_promotion_article ta where msg_type = 2 and unix_timestamp(ta.create_time) > (unix_timestamp(now()) - ("
				+ startEntityTimeStamp
				+ "+"
				+ EntityRecommendationSystem.ONE_DAY_TIME_SLOT + "))";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int discountId = rs.getInt(1);

			if (entityMap.containsKey(discountId)) {
				Entity entity = entityMap.get(discountId);
				entity.setEvaluationScore(DiscountRecommendationSystem.WRITING_SCORE);
			}
		}

		// Consider the length of content
		// The max length of content.
		sql = " select max(length(content)) from t_promotion_article ta where unix_timestamp(ta.create_time) > (unix_timestamp(now()) - ("
				+ startEntityTimeStamp
				+ "+"
				+ EntityRecommendationSystem.ONE_DAY_TIME_SLOT + ")) ";
		rs = DB.INSTANCE.executeQuery(sql);
		rs.next();

		int maxLen = rs.getInt(1);

		sql = " select id, length(content) from t_promotion_article ta where unix_timestamp(ta.create_time) > (unix_timestamp(now()) - ("
				+ startEntityTimeStamp
				+ "+"
				+ EntityRecommendationSystem.ONE_DAY_TIME_SLOT + ")) ";
		rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int discountId = rs.getInt(1);
			int length = rs.getInt(2);

			if (entityMap.containsKey(discountId)) {
				Entity entity = entityMap.get(discountId);

				double quality = entity.getEvaluationScore();
				double contentScore = length / (double) maxLen;

				quality = quality * (COUNTENT_WEIGHT) + (1.0 - COUNTENT_WEIGHT)
						* contentScore;

				entity.setEvaluationScore(quality);
			}
		}
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
				+ " tn where tn.deleted = 0 and unix_timestamp(tn.create_time) > (unix_timestamp(now()) - "
				+ startEntityTimeStamp + ")";

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

	@Override
	protected Entity getEntity(int id) {
		// TODO Auto-generated method stub
		return new Discount(id);
	}
}
