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

import java.io.IOException;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.sitewhere.security.SitewhereSecurity;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.ws.cxf.SitewhereFault;

/**
 * WSS4J password callback backed by Atlas.
 * 
 * @author Derek
 */
public class SitewherePasswordCallback implements CallbackHandler {

	/** Password encoder */
	private MessageDigestPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		WSPasswordCallback callback = (WSPasswordCallback) callbacks[0];
		String username = callback.getIdentifer();
		String password = getPasswordEncoder().encodePassword(callback.getPassword(), null);
		try {
			IUser user = SiteWhereServer.getInstance().getUserManagement().authenticate(username, password);
			List<IGrantedAuthority> auths = SiteWhereServer.getInstance().getUserManagement()
					.getGrantedAuthorities(username);
			SitewhereSecurity.setAuthenticatedUser(user, auths);
		} catch (SiteWhereException e) {
			throw new SitewhereFault(e);
		}
	}

	protected void setPasswordEncoder(MessageDigestPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	protected MessageDigestPasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}
}