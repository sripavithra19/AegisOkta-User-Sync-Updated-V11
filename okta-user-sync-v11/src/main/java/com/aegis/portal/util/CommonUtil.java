package com.aegis.portal.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	public String getLastSyncDate() {
		String syncDtStr = "";
		try {
			InputStream ios = this.getClass().getClassLoader().getResourceAsStream("lastRunDate.properties");
			BufferedReader bufReader = null;
			if (ios == null)
				return null;
			if (ios != null) {
				bufReader = new BufferedReader(new InputStreamReader(ios));
				syncDtStr = bufReader.readLine();
				ios.close();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return syncDtStr;
	}

	public void saveLastSyncDate() {
		String syncDtStr = "";
		try {
			Date date = new Date();
			// SimpleDateFormat formatter = new SimpleDateFormat("yy-MMM-dd");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			syncDtStr = formatter.format(date);
			URL url = this.getClass().getClassLoader().getResource("lastRunDate.properties");
			File file = null;
			try {
				file = new File(url.toURI());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (file == null) {
				logger.error("File lastRunDate.properties is null so not saving lastSync date");
				return;
			}
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file));

			bufWriter.write(syncDtStr);
			logger.info("Last Sync date is updated to {}", syncDtStr);

			if (bufWriter != null) {
				bufWriter.close();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

	public void updateLastSyncDate(String newDateTime) {
		try (FileOutputStream output = new FileOutputStream("lastRunDate.properties")) {
			Properties prop = new Properties();
			prop.setProperty("lastRunDate", newDateTime);
			prop.store(output, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
