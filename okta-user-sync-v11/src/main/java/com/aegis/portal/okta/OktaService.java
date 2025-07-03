package com.aegis.portal.okta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.portal.model.EventsDTO;
import com.aegis.portal.util.IConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;

public class OktaService {

	private String oktaEnv;
	private String oktaURL;
	private String tokenCredentials;
	private String OktaAppID;

	private static Logger logger = LoggerFactory.getLogger(OktaService.class);

	public OktaService(String oktaEnv) {
		this.oktaEnv = oktaEnv;
	}

	public Client getClient(String oktaEnv) throws Exception {

		ResourceBundle resBundle = ResourceBundle.getBundle("oktaConfiguration");
		if (resBundle == null)
			throw new Exception("Missing OktaConfiguration Properties");

		oktaURL = resBundle.getString(oktaEnv + IConstants.PERIOD + "OrgUrl");
		if (oktaURL == null || oktaURL.equals("null") || oktaURL.isEmpty())
			throw new Exception("Error Initializing Okta Instance: OrgUrl is missing in oktaConfiguration.properties");

		tokenCredentials = resBundle.getString(oktaEnv + IConstants.PERIOD + "TokenClientCredentials");
		if (tokenCredentials == null || tokenCredentials.equals("null") || tokenCredentials.isEmpty())
			throw new Exception(
					"Error Initializing Okta Instance: TokenClientCredentials is missing in oktaConfiguration.properties");

		OktaAppID = resBundle.getString(oktaEnv + IConstants.PERIOD + "AEGISlinkAppID");
		if (OktaAppID == null || OktaAppID.equals("null") || OktaAppID.isEmpty())
			throw new Exception(
					"Error Initializing Okta Instance: AEGISlinkAppID is missing in oktaConfiguration.properties");

		logger.info("oktaURL = {}, TokenCredentials = {}, AEGISlinkAppID = {}", oktaURL, tokenCredentials, OktaAppID);

		return Clients.builder().setOrgUrl(oktaURL).setClientCredentials(new TokenClientCredentials(tokenCredentials))
				.build();
	}

	public List<EventsDTO> fetchEvents(String sinceDateTime) {
		List<EventsDTO> eventsList = new ArrayList<>();

		try {
			String filter = "eventType eq \"user.session.start\" or eventType eq \"user.session.end\" or eventType eq \"user.account.activated\" or eventType eq \"user.account.deactivated\"";
			String encodedFilter = java.net.URLEncoder.encode(filter, "UTF-8");
			String urlStr = oktaURL + "/api/v1/logs?filter=" + encodedFilter + "&since="
					+ URLEncoder.encode(sinceDateTime, "UTF-8");

			URL url = URI.create(urlStr).toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "SSWS " + tokenCredentials);
			conn.setRequestProperty("Accept", "application/json");

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;

			while ((line = in.readLine()) != null) {
				response.append(line);
			}

			in.close();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode events = mapper.readTree(response.toString());

			for (JsonNode event : events) {
				String eventId = UUID.randomUUID().toString();
				String published = event.path("published").asText();

				Instant instant = Instant.parse(published);
				LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
				String formattedTimestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				String eventType = event.path("eventType").asText();
				String sessionId = event.path("authenticationContext").path("externalSessionId").asText();
				String email = event.path("actor").path("alternateId").asText();

				String mappedType = mapOktaEventType(eventType);
				if (mappedType == null)
					continue;

				EventsDTO eventDto = new EventsDTO();
				eventDto.setId(mappedType); // One of login/logout/activation/deactivation
				eventDto.setTimestamp(formattedTimestamp);
				eventDto.setSessionId(sessionId);
				eventDto.setProfileId(email); // TEMP: use email; DBService will map to actual ID

				eventsList.add(eventDto);
			}

		} catch (Exception e) {
			logger.error("Error fetching events from Okta: {}", e.getMessage());
		}

		return eventsList;
	}

	private String mapOktaEventType(String eventType) {
		switch (eventType) {
		case "user.session.start":
			return "login";
		case "user.session.end":
			return "logout";
		case "user.account.activated":
			return "activation";
		case "user.account.deactivated":
			return "deactivation";
		default:
			return null;
		}
	}
}
