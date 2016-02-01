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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URISyntaxException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A Servlet implementing Lost password procedure.<br>
 */
@SuppressWarnings("serial")
public class LostServlet extends HttpServlet {

	private static final String RESPONSE_TYPE = "response_type";

	private static final String LOST_JSP = "/lost.jsp";

	private static final String ERROR = "error";

	private static final String KRAKEN_UNAVAILABLE = "krakenUnavailable";

	private static final String REDIRECT_URI = "redirect_uri";

	private static final String EMAIL = "email";

	private static final String CLIENT_ID = "client_id";

	private static final String CUSTOMER_ID = "customerId";

	/**
	 * Key for client information
	 */
	private static final String STRING_XFF_HEADER = "X-Forwarded-For";

	final Logger logger = LoggerFactory.getLogger(LostServlet.class);

	private String privateServerURL;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter(EMAIL) != null) {
			// perform auth
			try {
				proceed(request, response);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				show(request, response);
			}
		} else {
			// forward to login page
			show(request, response);
		}
	}

	/**
	 * Display the input screen.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void show(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute(CUSTOMER_ID, request.getParameter(CUSTOMER_ID));

		String redirectUri = request.getParameter(REDIRECT_URI);
		if (redirectUri == null) {
			redirectUri = KrakenClientConfig.getConsoleURL();
		}
		request.setAttribute(REDIRECT_URI, redirectUri);
		request.setAttribute(RESPONSE_TYPE, request.getParameter(RESPONSE_TYPE));

		String clientId = request.getParameter(CLIENT_ID);
		if (clientId == null) {
			clientId = KrakenClientConfig.get("signin.default.clientid",
					"admin_console");
		}
		request.setAttribute(CLIENT_ID, clientId);

		RequestDispatcher rd = getServletContext().getRequestDispatcher(
				LOST_JSP);
		rd.forward(request, response);
	}

	/**
	 * Perform the action via API calls.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void proceed(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			URISyntaxException {
		String customerId = request.getParameter(CUSTOMER_ID);

		// create a POST method to execute the login request

		URIBuilder builder = new URIBuilder(privateServerURL
				+ "/rs/reset-user-pwd");

		if (StringUtils.isNotBlank(customerId)) {
			builder.addParameter(CUSTOMER_ID, customerId);
		}
		if (request.getParameter(CLIENT_ID) != null) {
			builder.addParameter("clientId", request.getParameter(CLIENT_ID));
		}
		String linkUrl = KrakenClientConfig
				.get("console.url",
						"https://api.squidsolutions.com/release/admin/console/index.html")
				+ "?access_token={access_token}#!user";
		builder.addParameter("link_url", linkUrl);

		// get login and pwd either from the request or from the session
		String email = request.getParameter(EMAIL);

		if (email == null) {
			show(request, response);
		} else {
			builder.addParameter(EMAIL, email);

			// set client information to the header
			String reqXFF = request.getHeader(STRING_XFF_HEADER);
			String postXFF;
			if (reqXFF != null) {
				// X-Forwarded-For header already exists in the request
				logger.info(STRING_XFF_HEADER + " : " + reqXFF);
				if (reqXFF.length() > 0) {
					// just add the remoteHost to it
					postXFF = reqXFF + ", " + request.getRemoteHost();
				} else {
					postXFF = request.getRemoteHost();
				}
			} else {
				postXFF = request.getRemoteHost();
			}

			HttpGet get = new HttpGet(builder.build());
			// add a new X-Forwarded-For header containing the remoteHost
			get.addHeader(STRING_XFF_HEADER, postXFF);

			// execute the login request
			HttpResponse executeCode;
			try {
				HttpClient client = HttpClientBuilder.create().build();
				executeCode = client.execute(get);
			} catch (ConnectException e) { // Authentication server unavailable
				logger.error(e.getLocalizedMessage());
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				show(request, response);
				return;
			}

			// code 500 returns by Kraken (for exemple if mongo is unavailabbe)
			if (executeCode.getStatusLine().getStatusCode() == 500) {
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				show(request, response);
				return;
			}

			// process the result
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					executeCode.getEntity().getContent()));

			StringBuffer resultBuffer = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultBuffer.append(line);
			}
			String result = resultBuffer.toString();

			Gson gson = new Gson();
			if (executeCode.getStatusLine().getStatusCode() != 200) {
				logger.info("Error : " + get.getURI() + " resulted in : "
						+ result);
				try {
					WebServicesException exception = (WebServicesException) gson
							.fromJson(result, WebServicesException.class);
					request.setAttribute(ERROR, exception.getError());
				} catch (Exception e) {
					request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				}
				// forward to input page
				show(request, response);
			} else {
				// forward to input page displaying ok message
				try {
					Message fromJson = gson.fromJson(result, Message.class);
					request.setAttribute("message", fromJson.getMessage());
					show(request, response);
				} catch (Exception e) {
					logger.info("Error : " + get.getURI() + " resulted in : "
							+ result);
					request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
					show(request, response);
				}
			}
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// setup the
		privateServerURL = (String) config.getServletContext().getAttribute(
				"privateServerURL");
	}

}
