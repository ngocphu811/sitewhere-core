/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.sitewhere.security;

import java.util.List;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;

/**
 * Handles common security operations.
 * 
 * @author Derek
 */
public class SitewhereSecurity {

	/**
	 * Set the thread local Spring security context based on a SiteWhere username.
	 * 
	 * @param username
	 * @throws SiteWhereException
	 */
	public static void setAuthenticatedUser(String username) throws SiteWhereException {
		IUser user = SiteWhereServer.getInstance().getUserManagement().getUserByUsername(username);
		if (user == null) {
			throw new SiteWhereException("User not found for username: " + username);
		}
		List<IGrantedAuthority> auths = SiteWhereServer.getInstance().getUserManagement()
				.getGrantedAuthorities(username);
		SitewhereSecurity.setAuthenticatedUser(user, auths);
	}

	/**
	 * Set the thread local Spring security context based on an SiteWhere user and authorities.
	 * 
	 * @param user
	 * @param auths
	 * @throws SiteWhereException
	 */
	public static void setAuthenticatedUser(IUser user, List<IGrantedAuthority> auths)
			throws SiteWhereException {
		SitewhereUserDetails details = new SitewhereUserDetails(user, auths);
		SitewhereAuthentication authentication = new SitewhereAuthentication(details);
		authentication.setAuthenticated(true);
		SecurityContext context = new SecurityContextImpl();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
}