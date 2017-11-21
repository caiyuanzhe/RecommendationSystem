package com.zhinengb.rs.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zhinengb.rs.config.ParameterProperty;

/**
 * Database connection.
 * 
 * @author Yuanzhe Cai
 *
 */
public enum DB {
	INSTANCE;
	public static String DB_DRIVER = "spring.datasource.driver-class-name";
	public static String DB_URL = "spring.datasource.url";
	public static String DB_USER_NAME = "spring.datasource.username";
	public static String DB_PASSWORD = "spring.datasource.password";

	private Connection conn = null;
	private Statement stmt = null;

	public void init() throws SQLException {
		this.conn = getConn();
		this.stmt = getStmt();
	}

	public Connection getConn() throws SQLException {
		if (conn == null) {
			try {
				Class.forName(ParameterProperty.INSTANCE.getRecommendConfig()
						.getDatasourceDriverClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			String connStr = ParameterProperty.INSTANCE.getRecommendConfig()
					.getDatasourceUrl();
			String userName = ParameterProperty.INSTANCE.getRecommendConfig()
					.getDatasourceUsername();
			String password = ParameterProperty.INSTANCE.getRecommendConfig()
					.getDatasourcePassword();
			conn = DriverManager.getConnection(connStr, userName, password);

			// String connStr =
			// "jdbc:mysql://rdscz04qd82u6gdt8w99w.mysql.rds.aliyuncs.com/znb";
			// conn = DriverManager.getConnection(connStr, "znb",
			// "Znb_db140930");
		} else {
			return conn;
		}
		return conn;
	}

	public Statement getStmt() throws SQLException {
		if (stmt == null) {
			stmt = conn.createStatement();
		}
		return stmt;
	}

	public void dropTable(String tableName) throws SQLException {
		String sql = "drop table if exists " + tableName;
		stmt.executeUpdate(sql);
	}

	public void execute(String sql) throws SQLException {
		stmt.execute(sql);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		ResultSet rs = null;
		rs = stmt.executeQuery(sql);
		return rs;
	}

	public void close() throws SQLException {
		if (conn != null) {
			conn.close();
			conn = null;
		}

		if (stmt != null) {
			stmt.close();
			stmt = null;
		}
	}

	public void commit() throws SQLException {
		this.getConn().commit();
	}

	public PreparedStatement prepareStmt(String sql) {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pstmt;
	}
}
