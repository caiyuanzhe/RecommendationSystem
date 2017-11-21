package com.zhinengb.rs.recommendation;

import com.zhinengb.rs.config.RecommendConfig;

/**
 * The main class for the recommendation system.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum ZnbRecommendationSystem {
	INSTANCE;
	private RecommendConfig rc = new RecommendConfig(true);

	public void run() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					RecommendationSystem rs = new GlobalRecommendationSystem(rc);
					while (true) {
						rs.runRecommender();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		};

		Thread thread = new Thread(r);
		thread.start();
	}

	static public void main(String avgs[]) {
		ZnbRecommendationSystem.INSTANCE.run();
	}
}
