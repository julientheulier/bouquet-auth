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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Servlet implementing OAuth 2.0 authentication.<br>
 * Supports "Client-Side" and "Server-Side" flows.<br>
 */
@SuppressWarnings("serial")
public class OAuth2LoginServlet extends HttpServlet {

	private static final String V4_RS_AUTH_TOKEN = "/rs/auth-token";

	private static final String V4_RS_AUTH_CODE = "/rs/auth-code";

	private static final String RESPONSE_TYPE = "response_type";

	private static final String RESPONSE_TYPE_TOKEN = "token";

	private static final String LOGIN_JSP = "/login.jsp";
	
	private static final String ERROR_JSP = "/error.jsp";

	private static final String ACCESS_TOKEN = "access_token";

	private static final String AUTH_CODE = "code";

	private static final String ERROR = "error";

	private static final String KRAKEN_UNAVAILABLE = "krakenUnavailable";

	private static final String DUPLICATE_USER_ERROR = "duplicateUser";

	private static final String REDIRECT_URI = "redirect_uri";

	private static final String PASSWORD = "password";

	private static final String LOGIN = "login";

	private static final String STEP = "step";

	private static final String CLIENT_ID = "client_id";

	private static final String CUSTOMER_ID = "customerId";

	private static final String ACCESS_TOKEN_PARAM_PATTERN = "${access_token}";

	private static final String AUTH_CODE_PARAM_PATTERN = "${auth_code}";

	private static final String CUSTOMERS_LIST = "customers";

	final Logger logger = LoggerFactory.getLogger(OAuth2LoginServlet.class);

	private String privateServerURL;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if ((request.getParameter(LOGIN) != null)
				|| (request.getParameter(STEP) != null)) {
			// perform auth
			try {
				login(request, response);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				showLogin(request, response);
			}
		} else {
			// forward to login page
			showLogin(request, response);
		}
	}

	/**
	 * Display the login screen.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void showLogin(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd;

		String redirectUri = request.getParameter(REDIRECT_URI);
		String clientId = request.getParameter(CLIENT_ID);
		if (redirectUri == null) {
			request.setAttribute(ERROR, "Invalid request : redirect_uri must be provided");
			rd = getServletContext().getRequestDispatcher(
					ERROR_JSP);
		} else if (clientId == null) {
			request.setAttribute(ERROR, "Invalid request : client_id must be provided");
			rd = getServletContext().getRequestDispatcher(
					ERROR_JSP);
		} else {
			request.setAttribute(CUSTOMER_ID, request.getParameter(CUSTOMER_ID));
			request.setAttribute(REDIRECT_URI, redirectUri);
			request.setAttribute(RESPONSE_TYPE, request.getParameter(RESPONSE_TYPE));
			request.setAttribute(CLIENT_ID, clientId);
	
			rd = getServletContext().getRequestDispatcher(
					LOGIN_JSP);
		}
		rd.forward(request, response);
	}

	/**
	 * Perform the login action via API calls.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, URISyntaxException {
		String responseType = request.getParameter(RESPONSE_TYPE);
		if (responseType == null) {
			responseType = RESPONSE_TYPE_TOKEN;
		}
		String customerId = request.getParameter(CUSTOMER_ID);

		// create a POST method to execute the login request
		HttpPost post;
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		if (responseType.equals(RESPONSE_TYPE_TOKEN)) {
			post = new HttpPost(privateServerURL + V4_RS_AUTH_TOKEN);
		} else {
			post = new HttpPost(privateServerURL + V4_RS_AUTH_CODE);
		}
		if (StringUtils.isNotBlank(customerId)) {
			values.add(new BasicNameValuePair(CUSTOMER_ID, customerId));
		}
		if (request.getParameter(CLIENT_ID) != null) {
			values.add(new BasicNameValuePair(CLIENT_ID, request.getParameter(CLIENT_ID)));
		}
		if (request.getParameter(REDIRECT_URI) != null) {
			values.add(new BasicNameValuePair(REDIRECT_URI,
					request.getParameter(REDIRECT_URI)));
		}

		// get login and pwd either from the request or from the session
		HttpSession session = request.getSession(false);
		String login = request.getParameter(LOGIN);
		if ((session != null) && (login == null)) {
			login = (String) session.getAttribute(LOGIN);
			session.setAttribute(LOGIN, null);
		}
		String password = request.getParameter(PASSWORD);
		if ((session != null) && (password == null)) {
			password = (String) session.getAttribute(PASSWORD);
			session.setAttribute(PASSWORD, null);
		}

		if ((login == null) || (password == null)) {
			showLogin(request, response);
		} else {
			values.add(new BasicNameValuePair(LOGIN, login));
			values.add(new BasicNameValuePair(PASSWORD, password));
			post.setEntity(new UrlEncodedFormEntity(values));

			try {
				// execute the login request
				Object auth = RequestHelper.processRequest(AuthCode.class, request, post);
				
				// perform redirection
				String redirectUrl = request.getParameter(REDIRECT_URI).trim();
				// T489 remove any trailing #
				if (redirectUrl.endsWith("#")) {
					redirectUrl = redirectUrl.substring(0, redirectUrl.length()-1);
				}
				if (responseType.equals(RESPONSE_TYPE_TOKEN)) {
					// token type
					AccessToken token = (AccessToken) auth;
					String tokenId = token.getId().getTokenId();
					// redirect URL
					if (redirectUrl.contains(ACCESS_TOKEN_PARAM_PATTERN)) {
						// replace access_token parameter pattern
						redirectUrl = StringUtils.replace(redirectUrl,
								ACCESS_TOKEN_PARAM_PATTERN, tokenId);
					} else {
						// append access_token anchor
						redirectUrl += (!redirectUrl.contains("?")) ? "?" : "&";
						redirectUrl += ACCESS_TOKEN + "=" + tokenId;
					}
				} else {
					// auth code type
					AuthCode codeObj = (AuthCode) auth;
					String code = codeObj.getCode();
					if (redirectUrl.contains(AUTH_CODE_PARAM_PATTERN)) {
						// replace code parameter pattern
						redirectUrl = StringUtils.replace(redirectUrl,
								AUTH_CODE_PARAM_PATTERN, code);
					} else {
						// append code param
						redirectUrl += (!redirectUrl.contains("?")) ? "?" : "&";
						redirectUrl += AUTH_CODE + "=" + code;
					}
				}
				response.sendRedirect(redirectUrl);
			} catch (ServerUnavailableException e1) {
				// Authentication server unavailable
				logger.error(e1.getLocalizedMessage());
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				showLogin(request, response);
				return;
			} catch (ServiceException e2) {
				WebServicesException exception = e2.getWsException();
				if (exception != null) {
					if (exception.getCustomers() != null) {
						// multiple customers found
						request.setAttribute(DUPLICATE_USER_ERROR, Boolean.TRUE);
						request.setAttribute(CUSTOMERS_LIST,
								exception.getCustomers());
						// save the credentials for later use
						request.getSession().setAttribute(LOGIN, login);
						request.getSession().setAttribute(PASSWORD, password);
					} else {
						String errorMessage = exception.getError();
						if (!errorMessage.contains("Password")) {
								request.setAttribute(ERROR, exception.getError());
						} else {
							request.setAttribute(ERROR, Boolean.TRUE);
						}
					}
				} else {
					request.setAttribute(ERROR, Boolean.TRUE);
				}
				// forward to login page
				showLogin(request, response);
				return;
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
