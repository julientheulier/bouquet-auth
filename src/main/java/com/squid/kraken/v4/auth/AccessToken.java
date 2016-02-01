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



/**
 * An authentication token linked to a User.<br>
 * Note that it implements Persistent but does not extend PersistentBaseImpl since it should be stored in it's own
 * collection (for performance).
 */

public class AccessToken {

    private Long expirationDateMillis;

    private AccessTokenPK id;

    protected String customerId;
    
    protected String userId;

    /**
     * Default constructor (required for jaxb).
     */
    public AccessToken() {
    }

    public AccessToken(AccessTokenPK tokenId, String customerId, Long expirationDateMillis) {
        super();
        this.customerId = customerId;
        this.expirationDateMillis = expirationDateMillis;
    }

    // persistent IF members

    public String getCustomerId() {
        return customerId;
    }

    protected void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getObjectType() {
        return this.getClass().getSimpleName();
    }
    
    

    // token members

    public Long getExpirationDateMillis() {
        return expirationDateMillis;
    }

    public void setExpirationDateMillis(Long expirationDateMillis) {
        this.expirationDateMillis = expirationDateMillis;
    }

    public AccessTokenPK getId() {
        return id;
    }

    public void setId(AccessTokenPK id) {
        this.id = id;
    }

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
