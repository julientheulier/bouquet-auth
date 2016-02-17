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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ConfigServlet extends HttpServlet {

	final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// setup the
		String privateServerURL = KrakenClientConfig.getKrakenScheme() + "://"
				+ KrakenClientConfig.getKrakenHost() + ":"
				+ KrakenClientConfig.getKrakenPort();
		if (KrakenClientConfig.getKrakenAPIVersion() != null) {
			privateServerURL += KrakenClientConfig.getKrakenAPIVersion();
		}
		logger.info("privateServerURL : " + privateServerURL);
		config.getServletContext().setAttribute("privateServerURL",
				privateServerURL);

		logger.info("server_url : "
				+ KrakenClientConfig.getKrakenPublicServerURL());
		config.getServletContext().setAttribute("server_url",
				KrakenClientConfig.getKrakenPublicServerURL());

		logger.info("console_url : " + KrakenClientConfig.getConsoleURL());
		config.getServletContext().setAttribute("console_url",
				KrakenClientConfig.getConsoleURL());

		logger.info("signin_url : " + KrakenClientConfig.getSignInURL());
		config.getServletContext().setAttribute("signin_url",
				KrakenClientConfig.getSignInURL());

		logger.info("signup_url : " + KrakenClientConfig.getSignUpURL());
		config.getServletContext().setAttribute("signup_url",
				KrakenClientConfig.getSignUpURL());
		
		String version = null;
		InputStream input = getServletContext().getResourceAsStream(
				"/META-INF/MANIFEST.MF");
		if (input != null) {
			try {
				Manifest manifest = new Manifest(input);
				Attributes mainAttribs = manifest.getMainAttributes();
				version += "{";
				version += " \"version \" :  \""+mainAttribs.getValue("Built-Date") + " ("
						+ mainAttribs.getValue("Revision") + ")";
				version += " \",";
				version += " \"build \" :  \""+mainAttribs.getValue("Implementation-Version");
				version += " \"}";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("version : " + version);
		config.getServletContext().setAttribute("version",
				version);
	}

}
