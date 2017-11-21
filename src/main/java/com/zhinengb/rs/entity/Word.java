package com.zhinengb.rs.entity;

import java.util.ArrayList;
import java.util.List;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.util.Util;

/**
 * The word object.
 * 
 * @author Yuanzhe Cai
 *
 */
public class Word implements Comparable<Word> {
	public static double DEFAULT_TIME_SLOT_SCORE = 1;
	public static double DEFAULT_FOLLOW_SCORE = ParameterProperty.INSTANCE
			.getRecommendConfig().getDefaultFollowScore();
	public static double FOLLOW_SCORE_FACTOR = ParameterProperty.INSTANCE
			.getRecommendConfig().getFollowScoreFactor();

	private String word;
	private int count;
	private double tf;
	private double idf;
	// Do not consider the time for the follow score!
	// In future, maybe we will implement this feature.
	private double followScore = DEFAULT_FOLLOW_SCORE;
	List<WordNode> wordTimeList = new ArrayList<WordNode>();
	private double normalWeight;
	private String category;
	private String key;

	public Word() {
	}

	public Word(String word, String category) {
		this.word = word;
		this.category = category;
		this.key = Util.generateCategoryWordKey(category, word);
	}

	public Word(String word, String category, int count) {
		this.word = word;
		this.category = category;
		this.count = count;
		this.key = Util.generateCategoryWordKey(category, word);
	}

	public Word(String word, String category, int count, double followScore) {
		this.word = word;
		this.category = category;
		this.count = count;
		this.followScore = followScore;
		this.key = Util.generateCategoryWordKey(category, word);
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public double getWeight() {
		return tf * idf;
	}

	public double getFollowScore() {
		return followScore;
	}

	public void setFollowScore(double followScore) {
		this.followScore = followScore;
	}

	public double getQueryWeight() {
		return this.getWeight() * this.getFollowScore()
				* this.getNormalTimeWeight();
	}

	public double getTimeWeight() {
		long curTimeMillis = System.currentTimeMillis();
		double weight = 0d;

		for (WordNode wordNode : wordTimeList) {
			double days = Util.getDays(curTimeMillis
					- wordNode.getVisitTimeStamp())
					+ DEFAULT_TIME_SLOT_SCORE;
			weight += Util.wordTimeFunc(days, wordNode.getCount());
		}

		return weight;
	}

	public void addCount(int count) {
		this.count += count;
	}

	public void addTimeWeight(WordNode wordNode) {
		this.wordTimeList.add(wordNode);
	}

	public void setNormalTimeWeight(double normalWeight) {
		this.normalWeight = normalWeight;
	}

	public double getNormalTimeWeight() {
		return normalWeight;
	}

	public String toString() {
		return word + " " + count + " " + tf + " follow: " + this.followScore;
	}

	@Override
	public int compareTo(Word word) {
		Double timeWeight1 = this.getTimeWeight();
		Double timeWeight2 = word.getTimeWeight();

		return -timeWeight1.compareTo(timeWeight2);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return this.category;
	}

	public boolean isCategory() {
		return this.word == null;
	}
}
