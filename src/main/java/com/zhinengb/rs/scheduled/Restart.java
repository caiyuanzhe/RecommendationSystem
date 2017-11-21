package com.zhinengb.rs.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zhinengb.rs.controller.RecommendController;

@Component
public class Restart {
	private static Logger logger = LoggerFactory.getLogger(Restart.class);
	@Autowired
	private RecommendController recommendController;

	 @Scheduled(cron = "${recommend.scheduled.restart:0 0 4 * * ?}")
	//@Scheduled(cron = "${recommend.scheduled.restart:0 */2 * * * ?}")
	public void upload() {

		try {
			logger.info("Restart ZnbRealTimeRecommendationSystem start");

			recommendController.restart();
			logger.info("Restart ZnbRealTimeRecommendationSystem end");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

}
