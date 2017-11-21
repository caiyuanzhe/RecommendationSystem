package com.zhinengb.rs.index;

/**
 * Index node info
 * 
 * @author Yuanzhe Cai
 *
 */
public class EntityIndexNode {
	private int count;
	private double df;

	EntityIndexNode(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getDf() {
		return df;
	}

	public void setDf(double df) {
		this.df = df;
	}

	public String toString() {
		return new Integer(count).toString();
	}
}
