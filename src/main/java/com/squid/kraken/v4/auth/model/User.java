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
package com.squid.kraken.v4.auth.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

	private UserPK id;
	
	private String login;

	private String email;

	private String password;

	private List<String> groups;

	private Map<String, String> attributes = null;

	public User() {
	}

	public User(UserPK id, String login, String password, String email) {
		this.id = id;
		this.login = login;
		this.password = password;
		this.email = email;
	}

	public User(UserPK id, String login, String password) {
		this(id, login, password, null);
	}

	public User(UserPK id, String login) {
		this(id, login, null, null);
	}

	public UserPK getId() {
		return id;
	}

	public void setId(UserPK id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getGroups() {
		if (groups == null) {
			groups = new ArrayList<String>();
		}
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
}
