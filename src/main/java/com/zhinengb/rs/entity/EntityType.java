package com.zhinengb.rs.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity Type
 * 
 * @author Yuanzhe Cai
 *
 */
public enum EntityType {
	ARTICLE, // article : 0
	DISCOUNT, // discount : 1
	QUESTION, // question : 2
	PRODUCT; // product : 3

	static private Map<Integer, String> entityTypeMap = new HashMap<Integer, String>();

	static {
		entityTypeMap.put(EntityType.ARTICLE.ordinal(), "articleinfo");
		entityTypeMap.put(EntityType.DISCOUNT.ordinal(), "discountinfo");
		entityTypeMap.put(EntityType.PRODUCT.ordinal(), "productinfo");
		entityTypeMap.put(EntityType.QUESTION.ordinal(), "questioninfo");
	}

	/**
	 * Get name for the entity type。
	 * 
	 * @param type
	 * @return
	 */
	static public String getEntityTypeStr(int type) {
		return entityTypeMap.get(type);
	}
}
