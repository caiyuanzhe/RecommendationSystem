package com.zhinengb.rs.entity;

import java.sql.Timestamp;

/**
 * User entity relationship
 * 
 * @author Yuanzhe Cai
 *
 */
public class PersonEntity {
	private int pid;
	private int entityId;
	private int entityType;
	private Timestamp accessTime;

	public PersonEntity(){
		
	}
	
	public PersonEntity(int pid, int entityId, int entityType,
			Timestamp accessTime) {
		super();
		this.pid = pid;
		this.entityId = entityId;
		this.entityType = entityType;
		this.accessTime = accessTime;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public Timestamp getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(Timestamp accessTime) {
		this.accessTime = accessTime;
	}

}
