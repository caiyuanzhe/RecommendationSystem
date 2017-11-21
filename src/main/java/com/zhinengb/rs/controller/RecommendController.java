package com.zhinengb.rs.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zhinengb.rs.config.RecommendConfig;
import com.zhinengb.rs.entity.CommonResult;
import com.zhinengb.rs.entity.Entity;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.entity.PersonEntity;
import com.zhinengb.rs.entity.PersonFollow;
import com.zhinengb.rs.recommendation.ZnbRealTimeRecommendationSystem;

/**
 * Recommendation Controller.
 * 
 * @author Yuanzhe Cai
 *
 */
@Controller
@RequestMapping("/recommend")
public class RecommendController {
	private static Logger logger = LoggerFactory
			.getLogger(RecommendController.class);

	@Autowired
	private RecommendConfig recommendConfig;

	private ZnbRealTimeRecommendationSystem recommendationSystem;

	@PostConstruct
	private void init() {
		recommendationSystem = new ZnbRealTimeRecommendationSystem(
				recommendConfig);
		logger.info("init finished");
	}

	@RequestMapping(value = "/list/{pid}", method = RequestMethod.GET)
	public ResponseEntity<?> getRecommenders(@PathVariable("pid") Integer pid)
			throws Exception {

		logger.info("getRecommenders -- pid {}", pid);
		List<Entity> entities = recommendationSystem.getRecommenders(pid);

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (entities != null) {
			for (Entity entity : entities) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("entityId", entity.getId());
				map.put("entityType", entity.getType());
				logger.info("entity: " + entity.toString());
				result.add(map);
			}

		}

		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/follow/{pid}", method = RequestMethod.PUT)
	public ResponseEntity<?> follow(@PathVariable("pid") Integer pid,
			@RequestBody List<PersonFollow> personFollows) throws Exception {
		logger.info("follow -- pid {}", pid);
		logger.info("personFollows -- {}", personFollows);
		CommonResult result = new CommonResult(0, "ok");
		try {

			for (PersonFollow pf : personFollows) {
				List<PersonFollow> pfList = new ArrayList<PersonFollow>();

				PersonFollow tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.ARTICLE.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.DISCOUNT.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.PRODUCT.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				logger.info(pfList.toString());

				recommendationSystem.updateFollow(pfList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(-1);
			result.setMsg(e.getMessage());
		}

		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/unfollow/{pid}", method = RequestMethod.PUT)
	public ResponseEntity<?> unfollow(@PathVariable("pid") Integer pid,
			@RequestBody List<PersonFollow> personFollows) throws Exception {
		logger.info("unfollow -- pid {}", pid);
		logger.info("personFollows -- {}", personFollows);
		CommonResult result = new CommonResult(0, "ok");
		try {
			for (PersonFollow pf : personFollows) {
				List<PersonFollow> pfList = new ArrayList<PersonFollow>();

				PersonFollow tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.ARTICLE.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.DISCOUNT.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				tmpPf = new PersonFollow(pf.getPid(), pf.getFid(),
						EntityType.PRODUCT.ordinal(), pf.getFollowScore());
				pfList.add(tmpPf);

				logger.info(pfList.toString());

				recommendationSystem.updateFollow(pfList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(-1);
			result.setMsg(e.getMessage());
		}
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/click/{pid}/{type}/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addClickRecords(@PathVariable("pid") Integer pid,
			@PathVariable("type") Integer type, @PathVariable("id") Integer id)
			throws Exception {
		logger.info("addClickRecords -- pid type id {} {} {}", pid, type, id);
		List<PersonEntity> personEntitys = new ArrayList<PersonEntity>();

		PersonEntity personEntity = new PersonEntity();
		personEntity.setPid(pid);
		personEntity.setEntityType(type);
		personEntity.setEntityId(id);
		personEntity.setAccessTime(new Timestamp(new Date().getTime()));
		personEntitys.add(personEntity);

		CommonResult result = new CommonResult(0, "ok");
		try {
			recommendationSystem.addClickRecords(personEntitys);
		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(-1);
			result.setMsg(e.getMessage());
		}
		return new ResponseEntity<Object>(result, HttpStatus.OK);

	}

	@RequestMapping(value = "/entity/{type}/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addEntity(
			@RequestBody List<PersonEntity> userEntitys) throws Exception {

		return new ResponseEntity<Object>(new CommonResult(0, "ok"),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/entity/{type}/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> removeEntity(
			@RequestBody List<PersonEntity> userEntitys) throws Exception {

		return new ResponseEntity<Object>(new CommonResult(0, "ok"),
				HttpStatus.OK);
	}

	public void restart() {
		logger.info("restarting");
		recommendationSystem = new ZnbRealTimeRecommendationSystem(
				recommendConfig);
		logger.info("restart finished");
	}

}
