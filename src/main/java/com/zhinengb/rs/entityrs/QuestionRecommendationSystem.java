package com.zhinengb.rs.entityrs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.Question;
import com.zhinengb.rs.storage.DB;
import com.zhinengb.rs.util.Util;

/**
 * Question recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public class QuestionRecommendationSystem extends
		RealEntityRecommendationSystem {
	private static String QUESTION_TABLE = "t_question";
	private static String QUESTION_TIME_COL = "create_time";
	private static String QUESTION_CATEGORY_COL = "category";
	private static double HOMEPAGE_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getQuestionHomepageScore();
	private static double NON_HOMEPAGE_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getQuestionNonHomepageScore();

	public QuestionRecommendationSystem() {
		this.entityType = EntityType.QUESTION.ordinal();
		this.entityTable = QUESTION_TABLE;
		this.timeCol = QUESTION_TIME_COL;
		this.categoryCol = QUESTION_CATEGORY_COL;
	}

	@Override
	protected String getWordEntitySql(List<Integer> entityIdList) {
		StringBuffer sb = null;
		if (entityIdList != null) {
			sb = new StringBuffer();
			sb.append(Util.getEntityInListSQL("ta", "id", entityIdList));
		}

		StringBuffer sqlsb = new StringBuffer();

		sqlsb.append(" select ta.id entity_id, tdt.name category, tb.name word, unix_timestamp(ta.create_time) time, count(*) count ");
		sqlsb.append(" from t_question ta, ");
		sqlsb.append(" (select distinct device_id, question_id from  t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category  and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.create_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tb.name, tdt.name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id entity_id, tdt.name category, td.model_name word, unix_timestamp(ta.create_time) time, count(*) count ");
		sqlsb.append(" from t_question ta, ");
		sqlsb.append(" (select distinct device_id, question_id from  t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt ");
		sqlsb.append(" where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category  and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.create_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, td.model_name ");
		sqlsb.append(" UNION ");
		sqlsb.append(" select ta.id entity_id, tdt.name category, tu.full_name word, unix_timestamp(ta.create_time) time, count(*) count ");
		sqlsb.append(" from t_question ta, ");
		sqlsb.append(" (select distinct device_id, question_id from t_device_question) tda, t_device_base td, t_brand tb, t_devicetype tdt, t_user tu ");
		sqlsb.append(" where ta.id = tda.question_id and tda.device_id = td.id and td.brand = tb.id and tdt.id = ta.category and tu.id = ta.author_id and ta.deleted = 0 ");
		sqlsb.append(" and unix_timestamp(ta.create_time) > ");
		sqlsb.append(startEntityTimeStamp);
		if (entityIdList != null) {
			sqlsb.append(sb.toString());
		}
		sqlsb.append(" group by ta.id, tdt.name, tu.full_name ");

		return sqlsb.toString();
	}

	@Override
	protected void calQuality(Map<Integer, Entity> entityMap)
			throws SQLException {
		// set the homepage score

		String sql = " select content_id from t_homepage_content where content_type = 2 and enable = 1 ";
		Set<Integer> homepageSet = new HashSet<Integer>();
		ResultSet rs = DB.INSTANCE.executeQuery(sql);
		while (rs.next()) {
			homepageSet.add(rs.getInt(1));
		}
		rs.close();
		
		// set the follow score as the follow score
		sql = " select max(tt.follow_count) "
				+ " from (select tq.id id, count(*) follow_count "
				+ " from t_question tq, t_question_answer tqa, t_question_answer_vote tqav "
				+ " where tq.id = tqa.question_id and tqa.id = tqav.toward_id "
				+ " group by tq.id) tt ";
		rs = DB.INSTANCE.executeQuery(sql);
		rs.next();
		double maxFollowScore = (double) (rs.getInt(1));
		rs.close();
		
		sql = " select tq.id id, count(*) follow_count "
				+ " from t_question tq, t_question_answer tqa, t_question_answer_vote tqav "
				+ " where tq.id = tqa.question_id and tqa.id = tqav.toward_id "
				+ " group by tq.id ";
		rs = DB.INSTANCE.executeQuery(sql);
		while (rs.next()) {
			int entityId = rs.getInt(1);
			double quality = (double) (rs.getInt(2)) / maxFollowScore;

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
	}

	@Override
	protected Entity getEntity(int id) {
		// TODO Auto-generated method stub
		return new Question(id);
	}

}
