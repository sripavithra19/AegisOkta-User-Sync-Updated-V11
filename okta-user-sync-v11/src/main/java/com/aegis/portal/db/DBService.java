package com.aegis.portal.db;

import com.aegis.portal.model.EventsDTO;
import com.aegis.portal.model.PortalUser;
import com.aegis.portal.util.CommonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBService {
	private static Logger logger = LoggerFactory.getLogger(DBService.class);
	private String env;
	private String lastsyncdate;
	CommonUtil util = new CommonUtil();

	public DBService(String dbEnv) {
		this.env = dbEnv;
	}

	public List<PortalUser> fetchAndStoreEvents() {
		DBConnection dbConn = new DBConnection(env);
		Connection con = null;
		PreparedStatement stmt = null;
		List<PortalUser> usrList = new ArrayList<>();
		lastsyncdate = util.getLastSyncDate();

		try {
			con = dbConn.getConnection();
			logger.debug("db Connection = {}", con);

			String userQuery = "SELECT ID, login, First_name, Last_name, Email FROM DPS_USER";
			stmt = con.prepareStatement(userQuery);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				PortalUser usr = new PortalUser();
				usr.setUsrID(rs.getString("ID"));
				usr.setLogin(rs.getString("login"));
				usr.setFirstName(rs.getString("First_name"));
				usr.setLastName(rs.getString("Last_name"));
				usr.setEmail(rs.getString("Email"));

				usrList.add(usr);
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			logger.error("Exception in getAllUsers :: {}", e.getMessage());
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException se) {
				logger.error("Exception in DB Closing {}", se.getMessage());
			}
		}
		return usrList;
	}

	public void insertEvents(List<EventsDTO> eventList) {
		String sql = "INSERT INTO DSS_DPS_EVENT (ID, TIMESTAMP, SESSIONID, PROFILEID) "
				+ "VALUES (?, TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?)";

		try (Connection con = new DBConnection(env).getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			for (EventsDTO evt : eventList) {
				String userId = getUserIdByLogin(con, evt.getProfileId());
				if (userId != null) {
					ps.setString(1, evt.getId());
					ps.setString(2, evt.getTimestamp());
					ps.setString(3, evt.getSessionId() != null ? evt.getSessionId() : "");
					ps.setString(4, userId);
					ps.addBatch();
				}
			}

			ps.executeBatch();
			logger.info("Inserted {} events into DSS_DPS_EVENT", eventList.size());

		} catch (Exception e) {
			logger.error("Error inserting events: {}", e.getMessage(), e);
		}
	}

	private String getUserIdByLogin(Connection con, String login) throws SQLException {
		String sql = "SELECT ID FROM DPS_USER WHERE login = ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, login);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString("ID");
			}
		}
		return null;

	}
}
