/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the 
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.kraken.v4.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to manage webapp configuration.<br>
 * Properties will be loaded from the following xml file (by priority order) :
 * <ol>
 * <li>[config.file]</li>
 * <li>[user.home]/explorer_webapp.xml</li>
 * </ol>
 */
public class KrakenClientConfig {

	protected static final Log logger = LogFactory
			.getLog(KrakenClientConfig.class);
	
	public static String CONFIG_FILE = "config.file";
	public static String CONFIG_FILEPATH = "explorer_webapp.xml";
	public static String CONFIG_FILEPATH_DEV = "explorer_webapp-dev.xml";

	static Properties props;

	/**
	 * Get a configuration property value
	 * 
	 * @param key
	 *            the property name
	 * @return
	 */
	public static synchronized String get(String key) {
		return get(key, null);
	}

	/**
	 * Get a configuration property value
	 * 
	 * @param key
	 *            the property name
	 * @param defaultValue
	 *            a default value
	 */
	public static synchronized String get(String key, String defaultValue) {
		String value;
		String filePath = System.getProperty(CONFIG_FILE);
		if (filePath != null) {
			// try using bouquet.auth.config
			initPropertiesFromFile(filePath);
		}
		if (props == null) {
			// try using user.home
			initProperties(System.getProperty("user.home"));
		}
		value = props.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	private static void initPropertiesFromFile(String path) {
		props = new Properties();

		// load the config file from user.home
		FileInputStream is;
		try {
			is = new FileInputStream(path);
			logger.warn("Loading config from : " + path);
			load(is, props);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Configuration file not found : "
					+ path);
		}
	}

	@Deprecated
	private static void initProperties(String filePath) {
		props = new Properties();
		String path = null;

		// load the config file from user.home
		FileInputStream is;
		try {
			path = filePath + File.separatorChar
					+ CONFIG_FILEPATH_DEV;
			is = new FileInputStream(path);
			logger.warn("Loading config from : " + path);
			load(is, props);
		} catch (FileNotFoundException e) {
			try {
				path = filePath + File.separatorChar
						+ CONFIG_FILEPATH;
				is = new FileInputStream(path);
				logger.warn("Loading config from : " + path);
				load(is, props);
			} catch (FileNotFoundException e2) {
				throw new RuntimeException("Configuration file not found : "
						+ path);
			}
		}
	}

	private static void load(InputStream in, Properties target) {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(in);
			for (Object key : properties.keySet()) {
				Object value = properties.get(key);
				target.put(key, value);
				logger.debug(key + ":" + value);
			}
		} catch (Exception e) {
			logger.warn("Could not load Kraken config file for stream : " + in,
					e);
		}

	}

	public static String getSignInURL() {
		return get("signin.url", "https://api.squidsolutions.com/sign-in");
	}

	public static String getSignUpURL() {
		return get("signup.url", "https://api.squidsolutions.com/sign-up");
	}

	public static String getConsoleURL() {
		return get("console.url", "https://api.squidsolutions.com/console");
	}

	public static String getKrakenPublicServerURL() {
		return get("kraken.api.url",
				"https://api.squidsolutions.com/release/v4.2/rs");
	}

	public static String getKrakenHost() {
		return get("kraken.rest.host");
	}

	public static String getKrakenPort() {
		return get("kraken.rest.port");
	}

	public static String getKrakenScheme() {
		return get("kraken.rest.scheme", "https");
	}

	/**
	 * @return the API version with a leading slash or null if not defined or empty
	 */
	public static String getKrakenAPIVersion() {
		String v = get("kraken.rest.version");
		if ((v != null) && (v.length()>0)) {
			if (!v.startsWith("/")) {
				v = "/"+v;
			}
		} else {
			v = null;
		}
		return v;
	}

}
