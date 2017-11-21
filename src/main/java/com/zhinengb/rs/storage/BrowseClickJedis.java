package com.zhinengb.rs.storage;

import java.sql.SQLException;

import com.zhinengb.rs.config.ParameterProperty;
import com.zhinengb.rs.entity.EntityType;
import com.zhinengb.rs.log.RecommendationSystemLog;
import com.zhinengb.rs.util.Util;

import redis.clients.jedis.Jedis;

/**
 * Jedis Operation.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum BrowseClickJedis {
	INSTATNCE;

	/**
	 * These parameters are consistent with the C-server.
	 */
	static public int APPID_PID_BROWSE = 48;
	static public int APPID_PID_CLICK = 49;
	static public int APPID_PID_CLICK_COUNT = 50;

	static public String DEFAULT_VALUE = "1";
	static public int ENTITY_CLICK = 1;
	static public int ENTITY_BROWSE = 2;
	static public String SPLIT = Util.SPLIT_TOKEN;

	private Jedis jedis;

	/**
	 * Initialized redis.
	 * 
	 * @throws SQLException
	 */
	public void init() throws SQLException {
		this.jedis = getJedis();
	}

	private Jedis getJedis() {
		if (jedis == null) {
			String ip = ParameterProperty.INSTANCE.getRecommendConfig()
					.getDatasourceRedisIp();
			int port = ParameterProperty.INSTANCE.getRecommendConfig()
					.getDatasourceRedisPort();

			RecommendationSystemLog.INSTANCE.write(ip + " " + port);
			return new Jedis(ip, port);
		} else {
			return jedis;
		}
	}

	/**
	 * Get key.
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		synchronized (jedis) {
			return jedis.get(key);
		}
	}

	/**
	 * Set key and value.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {
		synchronized (jedis) {
			this.jedis.set(key, value);
		}
	}

	private String getBrowseBitKey(int pid, int entityType) {
		return APPID_PID_BROWSE + SPLIT + pid + SPLIT + ENTITY_BROWSE + SPLIT
				+ entityType;
	}

	private String getClickKey(int pid, int entityId, int entityType) {
		return APPID_PID_CLICK + SPLIT + pid + SPLIT + ENTITY_CLICK + SPLIT
				+ entityId + SPLIT + entityType;
	}

	/**
	 * Set the bit into jedis.
	 * 
	 * @param pid
	 * @param entityType
	 * @param bcType
	 * @param entityId
	 */
	public void setBit(int pid, int entityType, int bcType, int entityId) {
		synchronized (jedis) {
			if (bcType == BrowseClickJedis.ENTITY_BROWSE) {
				String key = getBrowseBitKey(pid, entityType);
				this.jedis.setbit(key, entityId, true);
			} else {
				String key = getClickKey(pid, entityId, entityType);
				this.jedis.set(key, DEFAULT_VALUE);
			}
		}
	}

	/**
	 * Check the bit for special entity.
	 * 
	 * @param pid
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public boolean getBit(int pid, int entityType, int entityId) {
		return BrowseClickJedis.INSTATNCE.getBrowseBit(pid, entityType,
				entityId)
				|| BrowseClickJedis.INSTATNCE.getClickBit(pid, entityType,
						entityId);
	}

	/**
	 * Get click info.
	 * 
	 * @param pid
	 * @param entityType
	 * @param bcType
	 * @param entityId
	 * @return
	 */
	private boolean getClickBit(int pid, int entityType, int entityId) {
		synchronized (jedis) {
			String key = this.getClickKey(pid, entityId, entityType);
			return jedis.exists(key);
		}
	}

	/**
	 * Get browse info.
	 * 
	 * @param pid
	 * @param entityType
	 * @param bcType
	 * @param entityId
	 * @return
	 */
	private boolean getBrowseBit(int pid, int entityType, int entityId) {
		synchronized (jedis) {
			String key = getBrowseBitKey(pid, entityType);
			Boolean isExist = jedis.getbit(key, entityId);
			if (isExist == null) {
				return false;
			} else {
				return isExist;
			}
		}
	}

	/**
	 * Get person click info.
	 * 
	 * @param pid
	 * @param entityType
	 * @return
	 */
	public int getPersonTotalClick(int pid, int entityType) {
		synchronized (jedis) {
			String key = APPID_PID_CLICK_COUNT + SPLIT + pid + SPLIT
					+ entityType + SPLIT + "count";
			int click = 0;

			if (jedis.exists(key)) {
				click = new Integer(this.jedis.get(key));
			} else {
				click = 0;
			}

			return click;
		}
	}

	/**
	 * Close the redis.
	 */
	public void close() {
		synchronized (jedis) {
			this.jedis.disconnect();
			this.jedis = null;
		}
	}

	public static void main(String[] avgs) {
		try {
			BrowseClickJedis.INSTATNCE.init();
			BrowseClickJedis.INSTATNCE.set("h", "w");
			System.out.println("------------------------------------------");

			System.out.println(BrowseClickJedis.INSTATNCE.get("h"));

			// ----------------------- browse test -------------------------- //
			int pid = 1;
			int entityType = EntityType.ARTICLE.ordinal();
			int bcType = BrowseClickJedis.ENTITY_BROWSE;
			int entityId = 12344;

			BrowseClickJedis.INSTATNCE
					.setBit(pid, entityType, bcType, entityId);

			System.out.println(BrowseClickJedis.INSTATNCE.getBit(pid,
					entityType, entityId));

			// ------------------------ click test -------------------------- //
			pid = 1;
			entityType = EntityType.ARTICLE.ordinal();
			bcType = BrowseClickJedis.ENTITY_CLICK;
			entityId = 12344;

			BrowseClickJedis.INSTATNCE
					.setBit(pid, entityType, bcType, entityId);

			System.out.println(BrowseClickJedis.INSTATNCE.getBit(pid,
					entityType, entityId));

			// ------------------------ test browse --------------------------
			// //

			System.out.println(BrowseClickJedis.INSTATNCE.getBit(pid,
					entityType, entityId));

			// ------------------------ test click ---------------------------
			// //

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
