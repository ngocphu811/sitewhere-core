/*
 * UserModelInitializer.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import com.sitewhere.rest.model.user.User;
import com.sitewhere.rest.model.user.request.GrantedAuthorityCreateRequest;
import com.sitewhere.rest.model.user.request.UserCreateRequest;
import com.sitewhere.security.SitewhereAuthentication;
import com.sitewhere.security.SitewhereUserDetails;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.AccountStatus;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUserManagement;

/**
 * Used to load a default user and granted authorities into an empty user model. This acts
 * as a bootstrap for systems that have just been installed.
 * 
 * @author Derek
 */
public class UserModelInitializer {

	/** Default administrator username */
	public static final String DEFAULT_USERNAME = "admin";

	/** Default administrator password */
	public static final String DEFAULT_PASSWORD = "password";

	/** User management instance */
	private IUserManagement userManagement;

	public UserModelInitializer(IUserManagement userManagement) {
		this.setUserManagement(userManagement);
	}

	/**
	 * Initialize the user model with a expected list of granted authorities and default
	 * user.
	 * 
	 * @throws SiteWhereException
	 */
	public void initialize() throws SiteWhereException {
		GrantedAuthorityCreateRequest gaReq = new GrantedAuthorityCreateRequest();

		// Create authenticated user authority.
		IGrantedAuthority authUser = getUserManagement().getGrantedAuthorityByName(
				ISiteWhereAuthorities.AUTH_AUTHENTICATED_USER);
		if (authUser == null) {
			gaReq.setAuthority(ISiteWhereAuthorities.AUTH_AUTHENTICATED_USER);
			gaReq.setDescription("Log in to the system and perform basic functions.");
			authUser = getUserManagement().createGrantedAuthority(gaReq);
		}

		// Create user administration authority.
		IGrantedAuthority userAdmin = getUserManagement().getGrantedAuthorityByName(
				ISiteWhereAuthorities.AUTH_ADMIN_USERS);
		if (userAdmin == null) {
			gaReq.setAuthority(ISiteWhereAuthorities.AUTH_ADMIN_USERS);
			gaReq.setDescription("Create, Maintain, and delete user accounts.");
			userAdmin = getUserManagement().createGrantedAuthority(gaReq);
		}

		// Create site administration authority.
		IGrantedAuthority siteAdmin = getUserManagement().getGrantedAuthorityByName(
				ISiteWhereAuthorities.AUTH_ADMIN_SITES);
		if (siteAdmin == null) {
			gaReq.setAuthority(ISiteWhereAuthorities.AUTH_ADMIN_SITES);
			gaReq.setDescription("Create, Maintain, and delete site information.");
			siteAdmin = getUserManagement().createGrantedAuthority(gaReq);
		}

		List<String> auths = new ArrayList<String>();
		auths.add(authUser.getAuthority());
		auths.add(userAdmin.getAuthority());
		auths.add(siteAdmin.getAuthority());

		UserCreateRequest ureq = new UserCreateRequest();
		ureq.setFirstName("Admin");
		ureq.setLastName("User");
		ureq.setUsername(DEFAULT_USERNAME);
		ureq.setPassword(DEFAULT_PASSWORD);
		ureq.setAuthorities(auths);
		ureq.setStatus(AccountStatus.Active);

		// Create a fake "logged in" user for the "created by" field on the user.
		User fake = new User();
		fake.setUsername("system");
		SitewhereUserDetails details = new SitewhereUserDetails(fake, new ArrayList<IGrantedAuthority>());
		SitewhereAuthentication auth = new SitewhereAuthentication(details, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		getUserManagement().createUser(ureq);
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	public IUserManagement getUserManagement() {
		return userManagement;
	}

	public void setUserManagement(IUserManagement userManagement) {
		this.userManagement = userManagement;
	}
}