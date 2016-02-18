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
import java.net.URISyntaxException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Servlet implementing Lost password procedure.<br>
 */
@SuppressWarnings("serial")
public class LostServlet extends HttpServlet {

	private static final String AN_ERROR_OCCURRED = "An error occurred";

	private static final String LOST_JSP = "/lost.jsp";

	private static final String ERROR = "error";

	private static final String KRAKEN_UNAVAILABLE = "krakenUnavailable";

	private static final String EMAIL = "email";

	private static final String CLIENT_ID = "client_id";

	private static final String CUSTOMER_ID = "customerId";

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

		RequestDispatcher rd;
		request.setAttribute(CUSTOMER_ID, request.getParameter(CUSTOMER_ID));
		request.setAttribute(CLIENT_ID, KrakenClientConfig.get("signin.default.clientid",
				"admin_console"));
		rd = getServletContext().getRequestDispatcher(
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
		// get login and pwd either from the request or from the session
		String email = request.getParameter(EMAIL);

		if (email == null) {
			show(request, response);
		} else {
			
			String customerId = request.getParameter(CUSTOMER_ID);
			String clientId = request.getParameter(CLIENT_ID);

			// create a POST method to execute the login request

			URIBuilder builder = new URIBuilder(privateServerURL
					+ "/rs/reset-user-pwd");

			String linkUrl = KrakenClientConfig
					.get("public.url")
					+ "/password?access_token={access_token}";
			builder.addParameter("link_url", linkUrl);
			builder.addParameter(EMAIL, email);
			
			if (StringUtils.isNotBlank(customerId)) {
				builder.addParameter(CUSTOMER_ID, customerId);
			}
			if (clientId != null) {
				builder.addParameter("clientId", clientId);
			}

			// execute the login request
			try {
				HttpGet req = new HttpGet(builder.build());
				Message message = RequestHelper.processRequest(Message.class, request, req);
				request.setAttribute("message", message.getMessage());
				show(request, response);
			} catch (ServerUnavailableException e1) {
				logger.error(e1.getLocalizedMessage());
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				show(request, response);
			} catch (ServiceException e1) {
				WebServicesException wsException = e1.getWsException();
				String error;
				if (wsException == null) {
					error = AN_ERROR_OCCURRED;
				} else {
					error = wsException.getError();
				}
				request.setAttribute(ERROR, error);
				show(request, response);
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
