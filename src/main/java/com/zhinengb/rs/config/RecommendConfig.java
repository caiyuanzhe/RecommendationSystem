package com.zhinengb.rs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Load the parameters from the file(recommendation.properties).
 * 
 * @author Changsheng Shen
 *
 */
@Component
@ConfigurationProperties
public class RecommendConfig {

	// database info
	@Value("${recommendation.datasource.driver-class-name:com.mysql.jdbc.Driver}")
	private String datasourceDriverClassName;
	 @Value("${recommendation.datasource.url:jdbc:mysql://192.168.1.200:3306/znb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&zeroDateTimeBehavior=convertToNull}")
	//@Value("${recommendation.datasource.url:jdbc:mysql://rdscz04qd82u6gdt8w99w.mysql.rds.aliyuncs.com/znb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=false&failOverReadOnly=false&zeroDateTimeBehavior=convertToNull}")
	private String datasourceUrl;
	//@Value("${recommendation.datasource.username:znb}")
	 @Value("${recommendation.datasource.username:root}")
	private String datasourceUsername;
	//@Value("${recommendation.datasource.password:Znb_db140930}")
	 @Value("${recommendation.datasource.password:root}")
	private String datasourcePassword;

	// Redis info
	//@Value("${recommendation.datasource.redis.ip:10.26.221.55}")
	@Value("${recommendation.datasource.redis.ip:192.168.1.200}")
	private String datasourceRedisIp;
	@Value("${recommendation.datasource.redis.port:6379}")
	private int datasourceRedisPort;

	// Article Weight
	@Value("${recommendation.article.sim.weight :0.3}")
	private double articleSimWeight;
	@Value("${recommendation.article.slot.weight:0.3}")
	private double articleSlotWeight;
	@Value("${recommendation.article.evaluation.score.weight:0.5}")
	private double articleEvaluationScoreWeight;
	@Value("${recommendation.article.click.weight:0.2}")
	private double articleClickWeight;
	@Value("${recommendation.article.business.priority.weight:0}")
	private double articleBusinessPriorityWeight;
	@Value("${recommendation.article.slot.click.weight:0.2}")
	private double articleSlotClickWeight;

	// The "homepage score" and "non homepage score" is used as the weight for
	// the homgpage entity.
	@Value("${recommendation.article.homepage.score:0.5}")
	private double articleHomepageScore;
	@Value("${recommendation.article.non.homepage.score:0.1}")
	private double articleNonHomepageScore;

	// The excellent article will be 1.0; the non-excellent article will be 0.5;
	@Value("${recommendation.article.excellent.score:1.0}")
	private double articleExcellentScore;
	@Value("${recommend.article.non.excellent.score:0.5}")
	private double articleNonExcellentScore;

	// Discount Weight
	@Value("${recommendation.discount.sim.weight :0.3}")
	private double discountSimWeight;
	@Value("${recommendation.discount.slot.weight:0.8}")
	private double discountSlotWeight;
	@Value("${recommendation.discount.evaluation.score.weight:0.1}")
	private double discountEvaluationScoreWeight;
	@Value("${recommendation.discount.click.weight:0.1}")
	private double discountClickWeight;
	@Value("${recommendation.discount.business.priority.weight:0}")
	private double discountBusinessPriorityWeight;
	@Value("${recommendation.discount.slot.click.weight:0.3}")
	private double discountSlotClickWeight;

	// System only consider three days' discount
	@Value("${recommendation.discount.default.start.time:259200}")
	private long discountDefaultStartTime;

	// The "writing score" and "non writing score" is used as the weight for
	// the writing discount.
	@Value("${recommendation.discount.writing.score:0.5}")
	private double discountWritingScore;
	@Value("${recommendation.discount.non.writing.score:0.1}")
	private double discountNonWritingScore;

	// The content weight for the quality
	@Value("${recommendation.discount.content.weight:0.7}")
	private double discountContentWeight;

	// Product Weight
	@Value("${recommendation.product.sim.weight :0.3}")
	private double productSimWeight;
	@Value("${recommendation.product.slot.weight:0.3}")
	private double productSlotWeight;
	@Value("${recommendation.product.evaluation.score.weight:0.5}")
	private double productEvaluationScoreWeight;
	@Value("${recommendation.product.click.weight:0.2}")
	private double productClickWeight;
	@Value("${recommendation.product.business.priority.weight:0}")
	private double productBusinessPriorityWeight;
	@Value("${recommendation.product.slot.click.weight:0.2}")
	private double productSlotClickWeight;

	// System will recommend the product whose product score is larger than this
	// value.
	@Value("${recommendation.product.baseline.recommend.product.score:7.0}")
	private double productBaselineRecommendProductScore;

	// Question Weight
	@Value("${recommendation.question.sim.weight :0.3}")
	private double questionSimWeight;
	@Value("${recommendation.question.slot.weight:0.3}")
	private double questionSlotWeight;
	@Value("${recommendation.question.evaluation.score.weight:0.5}")
	private double questionEvaluationScoreWeight;
	@Value("${recommendation.question.click.weight:0.2}")
	private double questionClickWeight;
	@Value("${recommendation.question.business.priority.weight:0}")
	private double questionBusinessPriorityWeight;
	@Value("${recommendation.question.slot.click.weight:0.2}")
	private double questionSlotClickWeight;

	// The "homepage score" and "non homepage score" is used as the weight for
	// the homgpage entity.
	@Value("${recommendation.question.homepage.score:0.5}")
	private double questionHomepageScore;
	@Value("${recommendation.question.non.homepage.score:0.1}")
	private double questionNonHomepageScore;

	// The default start time for the person record.
	// Three months persons' click records are used to build the person index.
	@Value("${recommendation.default.person.start.record.time:7776000}")
	private long defaultPersonStartRecordTime;

	// The max number of words for a person.
	// The default value is 30. System only records 30 number of "important"
	// words for a person.
	@Value("${recommendation.max.number.of.words:30}")
	private int maxNumberOfWords;

	// Other Parameters

	// This paramter is used for recent days' click.
	// The default value is 2, which means two days person's click is statistic.
	@Value("${recommendation.person.click.default.short.slot.days:2}")
	private int personClickDefaultShortSlotDays;

	// The length of recommend list.
	@Value("${recommendation.max.length.per.person:10}")
	private int maxLengthPerPerson;

	// The default combination count.
	@Value("${recommendation.default.combination.recommend.count:3}")
	private int defaultCombinationRecommendCount;

	// The time to update the global recommender list.
	@Value("${recommendation.global.recommender.update.time:1800000}")
	private int globalRecommenderUpdateTime;

	// The default follow score is used to set the weight for the follow score.
	// If this value is small, system will set the high weight for the follow
	// value.
	@Value("${recommendation.default.follow.score:0.1}")
	private double defaultFollowScore;

	// If a user does not follow the entity, the follow score will multiple this
	// factor.
	@Value("${recommendation.follow.score.factor:0.1}")
	private double followScoreFactor;

	public RecommendConfig() {

	}

	public RecommendConfig(boolean test) {
		datasourceDriverClassName = "com.mysql.jdbc.Driver";
		// datasourceUrl =
		// "jdbc:mysql://rdscz04qd82u6gdt8w99w.mysql.rds.aliyuncs.com/znb";
		// datasourceUsername = "znb";
		// datasourcePassword = "Znb_db140930";
		datasourceUrl = "jdbc:mysql://192.168.1.200:3306/znb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&zeroDateTimeBehavior=convertToNull";
		datasourceUsername = "root";
		datasourcePassword = "root";

		// Redis info
		datasourceRedisIp = "192.168.1.200";
		datasourceRedisPort = 6379;

		// Article Weight
		articleSimWeight = 0.3;
		articleSlotWeight = 0.3;
		articleEvaluationScoreWeight = 0.5;
		articleClickWeight = 0.2;
		articleBusinessPriorityWeight = 0d;
		articleSlotClickWeight = 0.2;

		articleHomepageScore = 0.5;
		articleNonHomepageScore = 0.1;

		// The excellent article will be 1.0; the non-excellent article will be
		// 0.5;
		articleExcellentScore = 1.0;
		articleNonExcellentScore = 0.5;

		// Discount Weight
		discountSimWeight = 0.3;
		discountSlotWeight = 0.8;
		discountEvaluationScoreWeight = 0.1;
		discountClickWeight = 0.1;
		discountBusinessPriorityWeight = 0;
		discountSlotClickWeight = 0.3;

		discountDefaultStartTime = 259200L;

		discountWritingScore = 0.5;
		discountNonWritingScore = 0.1;

		discountContentWeight = 0.7;

		// Product Weight
		productSimWeight = 0.3;
		productSlotWeight = 0.3;
		productEvaluationScoreWeight = 0.5;
		productClickWeight = 0.2;
		productBusinessPriorityWeight = 0;
		productSlotClickWeight = 0.2;

		// System will recommend the product whose product score is larger than
		// this
		// value.
		productBaselineRecommendProductScore = 7.0;

		// Question Weight
		questionSimWeight = 0.3;
		questionSlotWeight = 0.3;
		questionEvaluationScoreWeight = 0.5;
		questionClickWeight = 0.2;
		questionBusinessPriorityWeight = 0;
		questionSlotClickWeight = 0.2;

		// Update person index

		// The default start time for the person record.
		// Three months persons' click records are used to build the person
		// index.
		defaultPersonStartRecordTime = 7776000L;

		// The max number of words for a person.
		// The default value is 30. System only records 30 number of "important"
		// words for a person.
		maxNumberOfWords = 30;

		// Other

		// This paramter is used for recent days' click.
		// The default value is 2, which means two days person's click is
		// statistic.
		personClickDefaultShortSlotDays = 2;

		// The length of recommend list.
		maxLengthPerPerson = 10;

		// The default combination count.
		defaultCombinationRecommendCount = 3;

		// The time to update the global recommender list.
		globalRecommenderUpdateTime = 1800000;

		// The default follow score.
		defaultFollowScore = 0.1;

		// The follow score factor.
		followScoreFactor = 0.1;
	}

	public String getDatasourceDriverClassName() {
		return datasourceDriverClassName;
	}

	public void setDatasourceDriverClassName(String datasourceDriverClassName) {
		this.datasourceDriverClassName = datasourceDriverClassName;
	}

	public String getDatasourceUrl() {
		return datasourceUrl;
	}

	public void setDatasourceUrl(String datasourceUrl) {
		this.datasourceUrl = datasourceUrl;
	}

	public String getDatasourceUsername() {
		return datasourceUsername;
	}

	public void setDatasourceUsername(String datasourceUsername) {
		this.datasourceUsername = datasourceUsername;
	}

	public String getDatasourcePassword() {
		return datasourcePassword;
	}

	public void setDatasourcePassword(String datasourcePassword) {
		this.datasourcePassword = datasourcePassword;
	}

	public String getDatasourceRedisIp() {
		return datasourceRedisIp;
	}

	public void setDatasourceRedisIp(String datasourceRedisIp) {
		this.datasourceRedisIp = datasourceRedisIp;
	}

	public int getDatasourceRedisPort() {
		return datasourceRedisPort;
	}

	public void setDatasourceRedisPort(int datasourceRedisPort) {
		this.datasourceRedisPort = datasourceRedisPort;
	}

	public double getArticleSimWeight() {
		return articleSimWeight;
	}

	public void setArticleSimWeight(double articleSimWeight) {
		this.articleSimWeight = articleSimWeight;
	}

	public double getArticleSlotWeight() {
		return articleSlotWeight;
	}

	public void setArticleSlotWeight(double articleSlotWeight) {
		this.articleSlotWeight = articleSlotWeight;
	}

	public double getArticleEvaluationScoreWeight() {
		return articleEvaluationScoreWeight;
	}

	public void setArticleEvaluationScoreWeight(
			double articleEvaluationScoreWeight) {
		this.articleEvaluationScoreWeight = articleEvaluationScoreWeight;
	}

	public double getArticleClickWeight() {
		return articleClickWeight;
	}

	public void setArticleClickWeight(double articleClickWeight) {
		this.articleClickWeight = articleClickWeight;
	}

	public double getArticleBusinessPriorityWeight() {
		return articleBusinessPriorityWeight;
	}

	public void setArticleBusinessPriorityWeight(
			double articleBusinessPriorityWeight) {
		this.articleBusinessPriorityWeight = articleBusinessPriorityWeight;
	}

	public double getArticleExcellentScore() {
		return articleExcellentScore;
	}

	public void setArticleExcellentScore(double articleExcellentScore) {
		this.articleExcellentScore = articleExcellentScore;
	}

	public double getArticleNonExcellentScore() {
		return articleNonExcellentScore;
	}

	public void setArticleNonExcellentScore(double articleNonExcellentScore) {
		this.articleNonExcellentScore = articleNonExcellentScore;
	}

	public double getDiscountSimWeight() {
		return discountSimWeight;
	}

	public void setDiscountSimWeight(double discountSimWeight) {
		this.discountSimWeight = discountSimWeight;
	}

	public double getDiscountSlotWeight() {
		return discountSlotWeight;
	}

	public void setDiscountSlotWeight(double discountSlotWeight) {
		this.discountSlotWeight = discountSlotWeight;
	}

	public double getDiscountEvaluationScoreWeight() {
		return discountEvaluationScoreWeight;
	}

	public void setDiscountEvaluationScoreWeight(
			double discountEvaluationScoreWeight) {
		this.discountEvaluationScoreWeight = discountEvaluationScoreWeight;
	}

	public double getDiscountClickWeight() {
		return discountClickWeight;
	}

	public void setDiscountClickWeight(double discountClickWeight) {
		this.discountClickWeight = discountClickWeight;
	}

	public double getDiscountBusinessPriorityWeight() {
		return discountBusinessPriorityWeight;
	}

	public void setDiscountBusinessPriorityWeight(
			double discountBusinessPriorityWeight) {
		this.discountBusinessPriorityWeight = discountBusinessPriorityWeight;
	}

	public double getProductSimWeight() {
		return productSimWeight;
	}

	public void setProductSimWeight(double productSimWeight) {
		this.productSimWeight = productSimWeight;
	}

	public double getProductSlotWeight() {
		return productSlotWeight;
	}

	public void setProductSlotWeight(double productSlotWeight) {
		this.productSlotWeight = productSlotWeight;
	}

	public double getProductEvaluationScoreWeight() {
		return productEvaluationScoreWeight;
	}

	public void setProductEvaluationScoreWeight(
			double productEvaluationScoreWeight) {
		this.productEvaluationScoreWeight = productEvaluationScoreWeight;
	}

	public double getProductClickWeight() {
		return productClickWeight;
	}

	public void setProductClickWeight(double productClickWeight) {
		this.productClickWeight = productClickWeight;
	}

	public double getProductBusinessPriorityWeight() {
		return productBusinessPriorityWeight;
	}

	public void setProductBusinessPriorityWeight(
			double productBusinessPriorityWeight) {
		this.productBusinessPriorityWeight = productBusinessPriorityWeight;
	}

	public double getProductBaselineRecommendProductScore() {
		return productBaselineRecommendProductScore;
	}

	public void setProductBaselineRecommendProductScore(
			double productBaselineRecommendProductScore) {
		this.productBaselineRecommendProductScore = productBaselineRecommendProductScore;
	}

	public double getQuestionSimWeight() {
		return questionSimWeight;
	}

	public void setQuestionSimWeight(double questionSimWeight) {
		this.questionSimWeight = questionSimWeight;
	}

	public double getQuestionSlotWeight() {
		return questionSlotWeight;
	}

	public void setQuestionSlotWeight(double questionSlotWeight) {
		this.questionSlotWeight = questionSlotWeight;
	}

	public double getQuestionEvaluationScoreWeight() {
		return questionEvaluationScoreWeight;
	}

	public void setQuestionEvaluationScoreWeight(
			double questionEvaluationScoreWeight) {
		this.questionEvaluationScoreWeight = questionEvaluationScoreWeight;
	}

	public double getQuestionClickWeight() {
		return questionClickWeight;
	}

	public void setQuestionClickWeight(double questionClickWeight) {
		this.questionClickWeight = questionClickWeight;
	}

	public double getQuestionBusinessPriorityWeight() {
		return questionBusinessPriorityWeight;
	}

	public void setQuestionBusinessPriorityWeight(
			double questionBusinessPriorityWeight) {
		this.questionBusinessPriorityWeight = questionBusinessPriorityWeight;
	}

	public int getPersonClickDefaultShortSlotDays() {
		return personClickDefaultShortSlotDays;
	}

	public void setPersonClickDefaultShortSlotDays(
			int personClickDefaultShortSlotDays) {
		this.personClickDefaultShortSlotDays = personClickDefaultShortSlotDays;
	}

	public int getMaxLengthPerPerson() {
		return maxLengthPerPerson;
	}

	public void setMaxLengthPerPerson(int maxLengthPerPerson) {
		this.maxLengthPerPerson = maxLengthPerPerson;
	}

	public int getDefaultCombinationRecommendCount() {
		return defaultCombinationRecommendCount;
	}

	public void setDefaultCombinationRecommendCount(
			int defaultCombinationRecommendCount) {
		this.defaultCombinationRecommendCount = defaultCombinationRecommendCount;
	}

	public int getGlobalRecommenderUpdateTime() {
		return globalRecommenderUpdateTime;
	}

	public void setGlobalRecommenderUpdateTime(int globalRecommenderUpdateTime) {
		this.globalRecommenderUpdateTime = globalRecommenderUpdateTime;
	}

	public double getArticleSlotClickWeight() {
		return articleSlotClickWeight;
	}

	public void setArticleSlotClickWeight(double articleSlotClickWeight) {
		this.articleSlotClickWeight = articleSlotClickWeight;
	}

	public double getDiscountSlotClickWeight() {
		return discountSlotClickWeight;
	}

	public void setDiscountSlotClickWeight(double discountSlotClickWeight) {
		this.discountSlotClickWeight = discountSlotClickWeight;
	}

	public double getProductSlotClickWeight() {
		return productSlotClickWeight;
	}

	public void setProductSlotClickWeight(double productSlotClickWeight) {
		this.productSlotClickWeight = productSlotClickWeight;
	}

	public double getQuestionSlotClickWeight() {
		return questionSlotClickWeight;
	}

	public void setQuestionSlotClickWeight(double questionSlotClickWeight) {
		this.questionSlotClickWeight = questionSlotClickWeight;
	}

	public double getArticleHomepageScore() {
		return articleHomepageScore;
	}

	public void setArticleHomepageScore(double articleHomepageScore) {
		this.articleHomepageScore = articleHomepageScore;
	}

	public double getArticleNonHomepageScore() {
		return articleNonHomepageScore;
	}

	public void setArticleNonHomepageScore(double articleNonHomepageScore) {
		this.articleNonHomepageScore = articleNonHomepageScore;
	}

	public double getDiscountWritingScore() {
		return discountWritingScore;
	}

	public void setDiscountWritingScore(double discountWritingScore) {
		this.discountWritingScore = discountWritingScore;
	}

	public double getDiscountNonWritingScore() {
		return discountNonWritingScore;
	}

	public void setDiscountNonWritingScore(double discountNonWritingScore) {
		this.discountNonWritingScore = discountNonWritingScore;
	}

	public double getQuestionHomepageScore() {
		return questionHomepageScore;
	}

	public void setQuestionHomepageScore(double questionHomepageScore) {
		this.questionHomepageScore = questionHomepageScore;
	}

	public double getQuestionNonHomepageScore() {
		return questionNonHomepageScore;
	}

	public void setQuestionNonHomepageScore(double questionNonHomepageScore) {
		this.questionNonHomepageScore = questionNonHomepageScore;
	}

	public long getDefaultPersonStartRecordTime() {
		return defaultPersonStartRecordTime;
	}

	public void setDefaultPersonStartRecordTime(
			long defaultPersonStartRecordTime) {
		this.defaultPersonStartRecordTime = defaultPersonStartRecordTime;
	}

	public int getMaxNumberOfWords() {
		return maxNumberOfWords;
	}

	public void setMaxNumberOfWords(int maxNumberOfWords) {
		this.maxNumberOfWords = maxNumberOfWords;
	}

	public double getDefaultFollowScore() {
		return defaultFollowScore;
	}

	public void setDefaultFollowScore(double defaultFollowScore) {
		this.defaultFollowScore = defaultFollowScore;
	}

	public double getFollowScoreFactor() {
		return followScoreFactor;
	}

	public void setFollowScoreFactor(double followScoreFactor) {
		this.followScoreFactor = followScoreFactor;
	}

	public long getDiscountDefaultStartTime() {
		return discountDefaultStartTime;
	}

	public void setDiscountDefaultStartTime(long discountDefaultStartTime) {
		this.discountDefaultStartTime = discountDefaultStartTime;
	}

	public double getDiscountContentWeight() {
		return discountContentWeight;
	}

	public void setDiscountContentWeight(double discountContentWeight) {
		this.discountContentWeight = discountContentWeight;
	}
}
