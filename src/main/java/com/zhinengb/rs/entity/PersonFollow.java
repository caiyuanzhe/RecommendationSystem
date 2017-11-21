package com.zhinengb.rs.entity;

/**
 * Person follow relationship
 * 
 * @author Yuanzhe Cai
 *
 */
public class PersonFollow {
	private int pid;
	private int fid;
	private String word;
	private int category;
	private int entityType;
	private double followScore;

	public PersonFollow() {

	}

	public PersonFollow(int pid, int fid, int entityType, double followScore) {
		super();
		this.pid = pid;
		this.fid = fid;
		this.entityType = entityType;
		this.followScore = followScore;
	}

	public PersonFollow(int pid, int fid, String word, int category,
			int entityType, double followScore) {
		super();
		this.pid = pid;
		this.fid = fid;
		this.word = word;
		this.category = category;
		this.entityType = entityType;
		this.followScore = followScore;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public double getFollowScore() {
		return followScore;
	}

	public void setFollowScore(double followScore) {
		this.followScore = followScore;
	}

	public String toString() {
		return "pid:" + pid + " fid:" + fid + " followScore:" + followScore;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return (PersonFollow) super.clone();
	}

}
