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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.squid.kraken.v4.auth.model.User;

/**
 * A Servlet implementing Lost password procedure.<br>
 */
@SuppressWarnings("serial")
public class ChangePasswordServlet extends HttpServlet {

	private static final String AN_ERROR_OCCURRED = "An error occurred";

	private static final String ERROR = "error";

	private static final String KRAKEN_UNAVAILABLE = "krakenUnavailable";

	final Logger logger = LoggerFactory.getLogger(ChangePasswordServlet.class);

	private String privateServerURL;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String password = request.getParameter("password");
		if (password != null) {
			try {
				proceed(request, response);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				show(request, response);
			}
		} else {
			// forward to page
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
	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// execute the login request
		try {
			User user = getUser(request);
			request.setAttribute("user", user);
			request.setAttribute("access_token", request.getParameter("access_token"));
		} catch (ServerUnavailableException e1) {
			logger.error(e1.getLocalizedMessage());
			request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
		} catch (URISyntaxException e) {
			logger.error(e.getLocalizedMessage());
			request.setAttribute(ERROR, Boolean.TRUE);
		} catch (ServiceException e) {
			WebServicesException wsException = e.getWsException();
			String error;
			if (wsException == null) {
				error = AN_ERROR_OCCURRED;
			} else {
				error = wsException.getError();
			}
			logger.error(error);
			request.setAttribute(ERROR, error);
		} catch (SSORedirectException e) {
			response.sendRedirect(e.getRedirectURL());
		}
		RequestDispatcher rd = getServletContext().getRequestDispatcher("/password.jsp");
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
	private void proceed(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, URISyntaxException {
		try {
			User user = getUser(request);
			user.setPassword(request.getParameter("password"));

			// create a POST method to execute the change password request
			URIBuilder builder = new URIBuilder(privateServerURL + "/rs/users/");
			String token = request.getParameter("access_token");
			builder.addParameter("access_token", token);

			// execute the request
			HttpPost req = new HttpPost(builder.build());
			Gson gson = new Gson();
			String json = gson.toJson(user);
			StringEntity stringEntity = new StringEntity(json);
			stringEntity.setContentType("application/json");
			req.setEntity(stringEntity);
			user = RequestHelper.processRequest(User.class, request, req);
			request.setAttribute("message", "Password updated");
			request.setAttribute("user", user);
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/password.jsp");
			rd.forward(request, response);
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
		} catch (SSORedirectException error) {
			response.sendRedirect(error.getRedirectURL());
		}
	}

	private User getUser(HttpServletRequest request)
			throws URISyntaxException, ServiceException, ServerUnavailableException, IOException, SSORedirectException {
		URIBuilder builder = new URIBuilder(privateServerURL + "/rs/user");
		builder.addParameter("access_token", request.getParameter("access_token"));
		HttpGet req = new HttpGet(builder.build());
		User user = RequestHelper.processRequest(User.class, request, req);
		return user;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// setup the
		privateServerURL = (String) config.getServletContext().getAttribute("privateServerURL");
	}

}
