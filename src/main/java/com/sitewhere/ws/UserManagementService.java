/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.sitewhere.rest.model.user.User;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.server.user.SitewhereRoles;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.spi.user.IUserManagement;

@Path("/users")
@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
@Transactional
public class UserManagementService {

	/** Password encoder */
	private MessageDigestPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	/**
	 * Get the user management SPI.
	 * 
	 * @return
	 */
	private IUserManagement getUserManagement() {
		return SiteWhereServer.getInstance().getUserManagement();
	}

	/**
	 * Get the password encoder instance.
	 * 
	 * @return
	 */
	private MessageDigestPasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * Authenricate a user based on username and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws SiteWhereException
	 */
	public User authenticate(String username, String password) throws SiteWhereException {
		String encoded = getPasswordEncoder().encodePassword(password, null);
		IUser result = getUserManagement().authenticate(username, encoded);
		return User.copy(result);
	}

	/**
	 * Get a user by unique username.
	 * 
	 * @param username
	 * @return
	 * @throws SiteWhereException
	 */
	@GET
	@Path("/username/{username}")
	@Secured({ SitewhereRoles.ROLE_USER_MGMT_ADMIN })
	public User getUserByUsername(@PathParam("username") String username) throws SiteWhereException {
		IUser result = getUserManagement().getUserByUsername(username);
		if (result == null) {
			return null;
		} else {
			return User.copy(result);
		}
	}
}