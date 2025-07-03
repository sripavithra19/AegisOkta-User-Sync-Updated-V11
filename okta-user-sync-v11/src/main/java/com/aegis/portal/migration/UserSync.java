package com.aegis.portal.migration;

import com.aegis.portal.db.DBService;
import com.aegis.portal.model.EventsDTO;
import com.aegis.portal.okta.OktaService;
import com.aegis.portal.util.CommonUtil;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSync {

	private static final Logger logger = LoggerFactory.getLogger(UserSync.class);

	public static void main(String[] args) {

		logger.info("=== Aegis User and Event Sync ===");

		String portalEnv = null;
		String oktaEnv = null;
		if (args.length == 2) {
			portalEnv = args[0];
			oktaEnv = args[1];
		} else {
			logger.error("Invalid Arguments. Required Portal Environment and Okta Environment");
			System.exit(0);
		}
		logger.info("Portal Env: {}", portalEnv);
		logger.info("Okta Env: {}", oktaEnv);

		try {
			// Initialize services
			OktaService oktaService = new OktaService(oktaEnv);
			oktaService.getClient(oktaEnv);

			DBService dbService = new DBService(portalEnv);
			CommonUtil util = new CommonUtil();

			String lastSyncDate = util.getLastSyncDate();
			List<EventsDTO> events = oktaService.fetchEvents(lastSyncDate);

			if (!events.isEmpty()) {
				dbService.insertEvents(events);
				util.saveLastSyncDate();
			} else {
				logger.info("No new events fetched.");
			}

		} catch (Exception e) {
			logger.error("Error running sync job: {}", e.getMessage(), e);
		}
	}
}
