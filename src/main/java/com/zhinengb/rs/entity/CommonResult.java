package com.zhinengb.rs.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Common Result.
 * 
 * @author Yuanzhe Cai
 */
public class CommonResult {

	private int result;
	private String msg;
	private Map<String, Object> data;

	public CommonResult() {
		this.result = 0;
		this.msg = "";
		this.data = new HashMap<String, Object>();
	}

	public CommonResult(int result, String msg) {
		this();
		this.result = result;
		this.msg = msg;
	}

	public void put(String key, Object value) {
		this.data.put(key, value);
	}

	public void putAll(Map<String, Object> map) {
		this.data.putAll(map);
	}

	public Object get(String key) {
		return this.data.get(key);
	}

	/**
	 * @return the result
	 */
	public int getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(int result) {
		this.result = result;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg
	 *            the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the data
	 */
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CommonResult [result=" + result + ", msg=" + msg + ", data="
				+ data + "]";
	}

}
