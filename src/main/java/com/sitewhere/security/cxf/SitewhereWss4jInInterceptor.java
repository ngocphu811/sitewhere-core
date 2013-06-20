/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.security.cxf;

import java.util.Map;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSConstants;

import com.sitewhere.security.SitewhereSecurity;
import com.sitewhere.server.user.SitewhereUsers;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.ws.cxf.SitewhereFault;

/**
 * Extends the WSS4J inbound interceptor.
 * 
 * @author Derek
 */
public class SitewhereWss4jInInterceptor extends WSS4JInInterceptor {

	/** Indicates whether to use a fallback account if token is missing */
	private boolean useFallbackAccount = true;

	/** User to use if fallback account is allowed */
	private String fallbackUser = SitewhereUsers.USER_ANONYMOUS;

	public SitewhereWss4jInInterceptor(Map<String, Object> properties) {
		super(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor#handleMessage(org.apache.cxf.binding.
	 * soap.SoapMessage)
	 */
	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		try {
			super.handleMessage(message);
		} catch (Fault fault) {
			if (WSConstants.INVALID_SECURITY.equals(fault.getFaultCode())) {
				if (isUseFallbackAccount()) {
					try {
						SitewhereSecurity.setAuthenticatedUser(getFallbackUser());
						return;
					} catch (SiteWhereException e) {
						throw new SitewhereFault(e);
					}
				}
			}
			throw fault;
		}
	}

	/**
	 * Set flag for using a fallback account when token is not found.
	 * 
	 * @param useFallbackAccount
	 */
	public void setUseFallbackAccount(boolean useFallbackAccount) {
		this.useFallbackAccount = useFallbackAccount;
	}

	/**
	 * Get flag for using a fallback account when token is not found.
	 * 
	 * @return
	 */
	public boolean isUseFallbackAccount() {
		return useFallbackAccount;
	}

	/**
	 * Set user to use if fallback is enabled.
	 * 
	 * @param fallbackUser
	 */
	public void setFallbackUser(String fallbackUser) {
		this.fallbackUser = fallbackUser;
	}

	/**
	 * Get user to use if fallback is enabled.
	 * 
	 * @return
	 */
	public String getFallbackUser() {
		return fallbackUser;
	}
}