package com.aegis.portal.db;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.portal.util.IConstants;

public class DBConnection {

	private String dbEnv;

	public DBConnection(String DBEnv) {
		this.dbEnv = DBEnv;

	}

	private static Logger logger = LoggerFactory.getLogger(DBConnection.class);

	public Connection getConnection() throws Exception {
		Connection con = null;
		try {

			if (dbEnv == null)
				throw new Exception("Error Initializing DB Connection: DBEnv is null or empty");

			// ResourceBundle resBundle = ResourceBundle.getBundle("db");
			// ResourceBundle resConfig = ResourceBundle.getBundle("config");

			URL root = getClass().getProtectionDomain().getCodeSource().getLocation();
			URL dbProperties = new URL(root, "db.properties");
			URL configProperties = new URL(root, "config.properties");
			Properties properties = new Properties();
			properties.load(dbProperties.openStream());
			properties.load(configProperties.openStream());
			/*
			 * if(resBundle == null) throw new Exception ("Missing db.properties file");
			 * if(resConfig == null) throw new Exception ("Missing config.properties file");
			 * String dbName = resConfig.getString("databaseName"); if(dbName == null ||
			 * dbName == "null" || dbName.isEmpty()) throw new Exception
			 * ("Error Initializing DB Connection: Database Name is missing in config.properties"
			 * ); String driverName =
			 * resBundle.getString(dbEnv+IConstants.PERIOD+dbName+IConstants.PERIOD+
			 * "driverClassName"); String dbURL =
			 * resBundle.getString(dbEnv+IConstants.PERIOD+dbName+IConstants.PERIOD+"url");
			 * String userName =
			 * resBundle.getString(dbEnv+IConstants.PERIOD+dbName+IConstants.PERIOD+
			 * "username"); String passWord =
			 * resBundle.getString(dbEnv+IConstants.PERIOD+dbName+IConstants.PERIOD+
			 * "password");
			 */

			String dbName = properties.getProperty("databaseName");
			if (dbName == null || dbName == "null" || dbName.isEmpty())
				throw new Exception("Error Initializing DB Connection: Database Name is missing in config.properties");
			String driverName = properties
					.getProperty(dbEnv + IConstants.PERIOD + dbName + IConstants.PERIOD + "driverClassName");
			String dbURL = properties.getProperty(dbEnv + IConstants.PERIOD + dbName + IConstants.PERIOD + "url");
			String userName = properties
					.getProperty(dbEnv + IConstants.PERIOD + dbName + IConstants.PERIOD + "username");
			String passWord = properties
					.getProperty(dbEnv + IConstants.PERIOD + dbName + IConstants.PERIOD + "password");

			if (driverName == null || driverName == "null" || driverName.isEmpty())
				throw new Exception(
						"Error Initializing DB Connection: property 'driverName' is missing in db.properties");
			if (dbURL == null || dbURL == "null" || dbURL.isEmpty())
				throw new Exception("Error Initializing DB Connection: property 'dbURL' is missing in db.properties");
			if (userName == null || userName == "null" || userName.isEmpty())
				throw new Exception(
						"Error Initializing DB Connection: property 'username' is missing in db.properties");
			if (passWord == null || passWord == "null" || passWord.isEmpty())
				throw new Exception(
						"Error Initializing DB Connection: property 'password' is missing in db.properties");

			Class.forName(driverName);
			con = DriverManager.getConnection(dbURL, userName, passWord);
			logger.info("DB Connection = {}", con);

		} catch (SQLException e) {
			logger.error("Exception in DB getConnection::" + e.getMessage());
			throw new SQLException("Exception in DB getConnection::" + e.getMessage());
		}
		return con;

	}

	public void closeConnection(Connection con) {
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
			logger.error("Exception in Database Connection close::" + e.getMessage());

		}
	}

}
