package com.zhinengb.rs.entity;

/**
 * Record the time and count for each word. This will be used to calculate the
 * weight for the word.
 * 
 * @author Yuanzhe Cai
 *
 */
public class WordNode {
	private long visitTimeStamp;
	private int count;

	public WordNode(long visitTimeStamp, int count) {
		super();
		this.visitTimeStamp = visitTimeStamp;
		this.count = count;
	}

	public long getVisitTimeStamp() {
		return visitTimeStamp;
	}

	public void setVisitTimeStamp(long visitTimeStamp) {
		this.visitTimeStamp = visitTimeStamp;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
