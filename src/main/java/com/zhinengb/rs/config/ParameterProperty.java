package com.zhinengb.rs.config;

/**
 * Get all the parameters from the system.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum ParameterProperty {
	INSTANCE;
	RecommendConfig rc;

	public void setRecommendConfig(RecommendConfig recommendConfig) {
		this.rc = recommendConfig;
	}

	public RecommendConfig getRecommendConfig() {
		return rc;
	}

	public void close() {
		rc = null;
	}

}
