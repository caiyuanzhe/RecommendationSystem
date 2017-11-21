package com.zhinengb.rs.entityrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.Product;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Product recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public class ProductRecommendationSystem extends RealEntityRecommendationSystem {
	public static double BASELINE_RECOMMEND_PRODUCT_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getProductBaselineRecommendProductScore();
	private static String PRODUCT_TABLE = "t_device_base";
	private static String PRODUCT_TIME_COL = "publish_date";
	private static String PRODUCT_CATEGORY_COL = "type";

	public ProductRecommendationSystem() {
		this.entityType = EntityType.PRODUCT.ordinal();
		this.entityTable = PRODUCT_TABLE;
		this.timeCol = PRODUCT_TIME_COL;
		this.categoryCol = PRODUCT_CATEGORY_COL;
	}

	@Override
	protected String getWordEntitySql(List<Integer> entityIdList) {

		StringBuffer sb = null;
		if (entityIdList != null) {
			sb = new StringBuffer();
			sb.append(Util.getEntityInListSQL("ta", "id", entityIdList));
		}

		StringBuffer sqlsb = new StringBuffer();

		sqlsb.append(" select td.id, tdt.name, tb.name, unix_timestamp(td.publish_date), count(*) ");
		sqlsb.append(" from t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where td.brand = tb.id and tdt.id = td.type ");
		sqlsb.append(" and unix_timestamp(td.publish_date) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by td.id, tb.name, tdt.name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select td.id, tdt.name, td.model_name, unix_timestamp(td.publish_date), count(*) ");
		sqlsb.append(" from t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where td.brand = tb.id and tdt.id = td.type ");
		sqlsb.append(" and unix_timestamp(td.publish_date) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by td.id, tdt.name, td.model_name ");

		return sqlsb.toString();
	}

	@Override
	protected void calQuality(Map<Integer, Entity> entityMap)
			throws SQLException {

		String sql = "select tdb.id id, (tdb.avg_score/10.0) quality "
				+ " from t_device_base tdb ";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			double quality = rs.getDouble(2);

			if (entityMap.containsKey(entityId)) {
				Entity entity = entityMap.get(entityId);
				entity.setEvaluationScore(quality);
			}
		}
		rs.close();
	}

	@Override
	protected void calTimeAnalysis(Map<Integer, Entity> entityMap,
			String tableName, String timeCol) throws SQLException {
		// basic info time
		String sql;

		sql = "select tn.id id, unix_timestamp(now()) - unix_timestamp(tn."
				+ timeCol + ") time_slot " + " from " + tableName
				+ " tn where tn.avg_score >= "
				+ ProductRecommendationSystem.BASELINE_RECOMMEND_PRODUCT_SCORE;

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
		return new Product(id);
	}
}
