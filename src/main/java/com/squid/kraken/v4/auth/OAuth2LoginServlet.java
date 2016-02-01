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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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

	/**
	 * Key for client information
	 */
	private static final String STRING_XFF_HEADER = "X-Forwarded-For";

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
				LOGIN_JSP);
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
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		if (responseType.equals(RESPONSE_TYPE_TOKEN)) {
			post = new HttpPost(privateServerURL + V4_RS_AUTH_TOKEN);
		} else {
			post = new HttpPost(privateServerURL + V4_RS_AUTH_CODE);
		}
		if (StringUtils.isNotBlank(customerId)) {
			urlParameters.add(new BasicNameValuePair(CUSTOMER_ID, customerId));
		}
		if (request.getParameter(CLIENT_ID) != null) {
			urlParameters.add(new BasicNameValuePair(CLIENT_ID, request.getParameter(CLIENT_ID)));
		}
		if (request.getParameter(REDIRECT_URI) != null) {
			urlParameters.add(new BasicNameValuePair(REDIRECT_URI,
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
			urlParameters.add(new BasicNameValuePair(LOGIN, login));
			urlParameters.add(new BasicNameValuePair(PASSWORD, password));

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
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			
			// add a new X-Forwarded-For header containing the remoteHost
			post.addHeader(STRING_XFF_HEADER, postXFF);

			// execute the login request
			HttpResponse executeCode;
			try {
				HttpClient client = HttpClientBuilder.create().build();
				executeCode = client.execute(post);
			} catch (ConnectException e) { 
				// Authentication server unavailable
				logger.error(e.getLocalizedMessage());
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				showLogin(request, response);
				return;
			}

			// code 500 returns by Kraken (for exemple if mongo is unavailabbe)
			if (executeCode.getStatusLine().getStatusCode() == 500) {
				request.setAttribute(KRAKEN_UNAVAILABLE, Boolean.TRUE);
				showLogin(request, response);
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
				logger.info("Login error : " + post.getURI()
						+ " resulted in : " + result);
				WebServicesException exception = (WebServicesException) gson
						.fromJson(result, WebServicesException.class);
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
			} else {
				// perform redirection
				String redirectUrl = request.getParameter(REDIRECT_URI).trim();
				// T489 remove any trailing #
				if (redirectUrl.endsWith("#")) {
					redirectUrl = redirectUrl.substring(0, redirectUrl.length()-1);
				}
				if (responseType.equals(RESPONSE_TYPE_TOKEN)) {
					// token type
					AccessToken token = gson
							.fromJson(result, AccessToken.class);
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
					AuthCode codeObj = gson.fromJson(result, AuthCode.class);
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
