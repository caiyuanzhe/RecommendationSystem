package com.zhinengb.rs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Recommendation System Application.
 * 
 * @author Yuanzhe Cai
 *
 */
@SpringBootApplication
@EnableScheduling
public class ZnbRecommendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZnbRecommendApplication.class, args);
	}
}
