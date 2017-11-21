package com.zhinengb.rs.index;

/**
 * Item word relationship
 * 
 * @author Yuanzhe Cai
 *
 */
public class ItemWord {
	String word;
	String category;
	int id;
	int count;
	long visitTimeStamp;

	public ItemWord(String word, String category, int id, int count,
			long visitTimeStamp) {
		this.category = category;
		this.word = word;
		this.id = id;
		this.count = count;
		this.visitTimeStamp = visitTimeStamp;
	}

	public ItemWord(String word, String category, int id, int count) {
		this.category = category;
		this.word = word;
		this.id = id;
		this.count = count;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getVisitTimeStamp() {
		return visitTimeStamp;
	}

	public void setVisitTimeStamp(long visitTimeStamp) {
		this.visitTimeStamp = visitTimeStamp;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * This is the shallow clone.
	 * 
	 */
	@Override
	public Object clone() {
		try {
			return (ItemWord) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
