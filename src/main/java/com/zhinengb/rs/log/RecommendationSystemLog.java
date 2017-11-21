package com.zhinengb.rs.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write the log system.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum RecommendationSystemLog {
	INSTANCE;

	private static Logger logger = LoggerFactory
			.getLogger(RecommendationSystemLog.class);

	public void write(String log) {
		logger.info(log);
	}
}
