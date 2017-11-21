package com.zhinengb.rs.entity;

import java.util.List;

import com.zhinengb.rs.entityrs.EntityRecommendationSystem;
import com.zhinengb.rs.util.Util;

/**
 * Entity object.
 * 
 * @author Yuanzhe Cai
 *
 */
public abstract class Entity implements Comparable<Entity> {
	public static double EVALUATION_NORMAL_SCORE = 3.0d;

	private int id;
	private double simScore = -1.0d;
	private long timeSlot;
	private double evaluationScore;
	private double numClick;
	private double numClickInSlotDay;
	private double browseFilterScore = 0;
	private double clickFilterScore = 0;
	private int businessPriority = 0;
	private int category = 0;
	private double globalComScore = -1;
	private List<Word> relatedWordList;

	protected double simWeight;
	protected double timeSlotWeight;
	protected double evaluationScoreWeight;
	protected double userClickWeight;
	protected double businessPriorityWeight;
	protected double clickWeight;
	protected int entityType;

	public Entity(int id) {
		this.id = id;
		this.simScore = -1.0d;
		this.timeSlot = 0;
		this.evaluationScore = 0;
		this.numClick = 0;
		this.numClickInSlotDay = 0;
	}

	public Entity(int id, int category, double globalComScore) {
		this.id = id;
		this.globalComScore = globalComScore;
		this.category = category;
	}

	public double getPersonalCombinationScore() {
		// TODO Auto-generated method stub
		double comScore = simWeight * this.getSimScore() + (1 - simWeight)
				* getGlobalCombinationScore();
		return comScore;
	}

	protected double getClickScore() {
		// TODO Auto-generated method stub
		double clickScore = clickWeight * (this.getNumClick())
				+ (1 - clickWeight) * (this.getNumClickInSlotDay());

		return clickScore;
	}

	public double getGlobalCombinationScore() {

		if (globalComScore < 0) {
			double comScore = timeSlotWeight * this.getTimeSlot()
					+ evaluationScoreWeight * this.getEvaluationScore()
					+ userClickWeight * this.getClickScore()
					+ businessPriorityWeight * this.getBusinessPriority();

			globalComScore = comScore;

			return comScore;
		} else {
			return globalComScore;
		}
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getEvaluationScore() {
		return evaluationScore;
	}

	public void setEvaluationScore(double evaluationScore) {
		this.evaluationScore = evaluationScore / Entity.EVALUATION_NORMAL_SCORE;
	}

	public int getBusinessPriority() {
		return businessPriority;
	}

	public void setBusinessPriority(int businessPriority) {
		this.businessPriority = businessPriority;
	}

	public double getNumClick() {
		return numClick;
	}

	public void setNumClick(double numClick) {
		this.numClick = numClick;
	}

	public double getNumClickInSlotDay() {
		return numClickInSlotDay;
	}

	public void setNumClickInSlotDay(double numClickInSlotDay) {
		this.numClickInSlotDay = numClickInSlotDay;
	}

	public double getSimScore() {
		return simScore;
	}

	public void setSimScore(double simScore) {
		this.simScore = simScore;
	}

	public void setTimeSlot(long timeSlot) {
		this.timeSlot = timeSlot;
	}

	public double getTimeSlot() {
		if (timeSlotWeight > 0) {
			return Util.reverseFunc(getDaysSlot());
		}

		return timeSlotWeight;
	}

	protected double getDaysSlot() {
		return (double) this.timeSlot
				/ (double) EntityRecommendationSystem.ONE_DAY_TIME_SLOT;
	}

	public double getBrowseFilterScore() {
		return browseFilterScore;
	}

	public void setBrowseFilterScore(double browseFilterScore) {
		this.browseFilterScore = browseFilterScore;
	}

	public double getClickFilterScore() {
		return clickFilterScore;
	}

	public void setClickFilterScore(double clickFilterScore) {
		this.clickFilterScore = clickFilterScore;
	}

	public int getType() {
		return entityType;
	}

	public double getGlobalComScore() {
		return globalComScore;
	}

	public void setGlobalComScore(double globalComScore) {
		this.globalComScore = globalComScore;
	}

	public List<Word> getRelatedWordList() {
		return relatedWordList;
	}

	public void setRelatedWordList(List<Word> wordList) {
		this.relatedWordList = wordList;
	}

	@Override
	public int compareTo(Entity entity) {
		// TODO Auto-generated method stub

		if (this.getSimScore() < 0) {
			Double val1 = this.getGlobalCombinationScore();
			Double val2 = entity.getGlobalCombinationScore();
			return -val1.compareTo(val2);
		} else {
			Double val1 = this.getPersonalCombinationScore();
			Double val2 = entity.getPersonalCombinationScore();
			return -val1.compareTo(val2);
		}
	}

	public String toString() {
		return this.getId() + " " + EntityType.getEntityTypeStr(this.getType());
	}
}
