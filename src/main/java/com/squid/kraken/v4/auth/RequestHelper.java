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

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class RequestHelper {

	/**
	 * Key for client information
	 */
	private static final String STRING_XFF_HEADER = "X-Forwarded-For";

	private static final Logger logger = LoggerFactory.getLogger(RequestHelper.class);

	public static <T> T processRequest(Class<T> type, HttpServletRequest request, HttpRequestBase req)
			throws IOException, URISyntaxException, ServerUnavailableException, ServiceException, SSORedirectException {

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

		// add a new X-Forwarded-For header containing the remoteHost
		req.addHeader(STRING_XFF_HEADER, postXFF);

		// execute the login request
		HttpResponse executeCode;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			executeCode = client.execute(req);
		} catch (ConnectException e) {
			// Authentication server unavailable
			throw new ServerUnavailableException(e);
		}

		// process the result
		BufferedReader rd = new BufferedReader(new InputStreamReader(executeCode.getEntity().getContent()));

		StringBuffer resultBuffer = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			resultBuffer.append(line);
		}
		String result = resultBuffer.toString();

		T fromJson;
		Gson gson = new Gson();
		int statusCode = executeCode.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			if (executeCode.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
				String redirectURL = executeCode.getFirstHeader("Location").getValue();
				throw new SSORedirectException("SSO Redirect Exception", redirectURL);
			} else {
				logger.info("Error : " + req.getURI() + " resulted in : " + result);
				WebServicesException exception;
				try {
					exception = gson.fromJson(result, WebServicesException.class);
				} catch (Exception e) {
					if ((statusCode >= 500) && (statusCode < 600)) {
						// Authentication server unavailable
						throw new ServerUnavailableException();
					} else {
						throw new ServiceException();
					}
				}
				throw new ServiceException(exception);
			}
		} else {
			// forward to input page displaying ok message
			try {
				fromJson = gson.fromJson(result, type);
			} catch (Exception e) {
				throw new ServiceException(e);
			}
		}
		return fromJson;
	}

}
