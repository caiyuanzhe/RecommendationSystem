package com.zhinengb.rs.entityrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Article;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Article recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public class ArticleRecommendationSystem extends RealEntityRecommendationSystem {
	private static String ARTICLE_TABLE = "t_article";
	private static String ARTICLE_TIME_COL = "issue_time";
	private static String ARTICLE_CATEGORY_COL = "category";
	private static int ARTICLE_CONTENT_TYPE = 1;
	private static double ARTICLE_TOP_RANK = 100.0d;
	private static double EXCELLENT_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getArticleExcellentScore();
	private static double NON_EXCELLENT_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getArticleNonExcellentScore();
	private static double HOMEPAGE_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getArticleHomepageScore();
	private static double NON_HOMEPAGE_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getArticleNonHomepageScore();

	public ArticleRecommendationSystem() {
		this.entityType = EntityType.ARTICLE.ordinal();
		this.entityTable = ARTICLE_TABLE;
		this.timeCol = ARTICLE_TIME_COL;
		this.categoryCol = ARTICLE_CATEGORY_COL;
	}

	@Override
	protected String getWordEntitySql(List<Integer> entityIdList) {

		StringBuffer sb = null;
		if (entityIdList != null) {
			sb = new StringBuffer();
			sb.append(Util.getEntityInListSQL("ta", "id", entityIdList));
		}

		StringBuffer sqlsb = new StringBuffer();

		// 1. article - tag
		// 2. article - brand
		// 3. article - model
		// 4. article - author
		sqlsb.append("select ta.id, tdt.name, tta.tag, unix_timestamp(ta.issue_time), count(*) ");
		sqlsb.append(" from t_article ta, t_tags_article tta, t_devicetype tdt ");
		sqlsb.append(" where unix_timestamp(ta.issue_time) > ");
		sqlsb.append(startEntityTimeStamp);
		sqlsb.append(" and ta.id = tta.article_id and tdt.id = ta.category and ta.deleted = 0 ");
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, tta.tag ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id, tdt.name, tb.name, unix_timestamp(ta.issue_time), count(*) ");
		sqlsb.append(" from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt  ");
		sqlsb.append(" where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.issue_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tb.name, tdt.name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id,tdt.name, td.model_name, unix_timestamp(ta.issue_time), count(*) ");
		sqlsb.append(" from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.issue_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, td.model_name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id entity_id,tdt.name catergory, tu.full_name word, unix_timestamp(ta.issue_time) time, count(*) count ");
		sqlsb.append(" from t_article ta, t_device_article tda, t_device_base td, t_brand tb, t_devicetype tdt, t_user tu ");
		sqlsb.append(" where ta.id = tda.article_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and tu.id = ta.author_id and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.issue_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, tu.full_name, time ");

		return sqlsb.toString();
	}

	@Override
	protected void calQuality(Map<Integer, Entity> entityMap)
			throws SQLException {
		// Mark the excellent article + article from the "smzdm".
		String sql = " select content_id from t_excellent_content where content_type = "
				+ ARTICLE_CONTENT_TYPE
				+ " union "
				+ " select id from t_article where original_url like '%smzdm%' ";
		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		Set<Integer> excellentArticleSet = new HashSet<Integer>();

		while (rs.next()) {
			excellentArticleSet.add(rs.getInt(1));
		}
		rs.close();

		// Home quality rank for 0 1
		sql = "select content_id from t_homepage_content where content_type = 1 and enable = 1 ";
		rs = DB.INSTANCE.executeQuery(sql);
		Set<Integer> homepageSet = new HashSet<Integer>();

		while (rs.next()) {
			homepageSet.add(rs.getInt(1));
		}
		rs.close();

		// Content quality rank for 0 1 2
		sql = "select ta.id id, ((2 - ta.quality + 1)/3.0) quality "
				+ " from t_article ta ";
		rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);
			double quality = rs.getDouble(2);

			if (excellentArticleSet.contains(entityId)) {
				quality = quality * EXCELLENT_SCORE;
			} else {
				quality = quality * NON_EXCELLENT_SCORE;
			}

			if (homepageSet.contains(entityId)) {
				quality = quality + HOMEPAGE_SCORE;
			} else {
				quality = quality + NON_HOMEPAGE_SCORE;
			}

			if (entityMap.containsKey(entityId)) {
				Entity entity = entityMap.get(entityId);
				entity.setEvaluationScore(quality);
			}
		}
		rs.close();

		// Mark （新手必读） to top
		sql = "select id from t_article where title like '%（新手必读）' and deleted = 0";
		rs = DB.INSTANCE.executeQuery(sql);

		while (rs.next()) {
			int entityId = rs.getInt(1);

			if (entityMap.containsKey(entityId)) {

				Entity entity = entityMap.get(entityId);
				entity.setEvaluationScore(ARTICLE_TOP_RANK);
			}
		}
		rs.close();
	}

	@Override
	protected Entity getEntity(int id) {
		// TODO Auto-generated method stub
		return new Article(id);
	}
}
