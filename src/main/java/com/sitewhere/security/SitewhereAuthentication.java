package com.sitewhere.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.sitewhere.spi.SiteWhereException;

/**
 * Implementation of Spring security interface.
 * 
 * @author Derek
 */
public class SitewhereAuthentication implements Authentication {

	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	/** Spring UserDetails */
	private SitewhereUserDetails userDetails;

	/** Authenticated flag */
	private boolean authenticated;

	public SitewhereAuthentication(SitewhereUserDetails details) throws SiteWhereException {
		this.userDetails = details;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getAuthorities()
	 */
	public Collection<GrantedAuthority> getAuthorities() {
		return userDetails.getAuthorities();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.Authentication#getCredentials()
	 */
	public Object getCredentials() {
		return userDetails.getPassword();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.Authentication#getDetails()
	 */
	public Object getDetails() {
		return userDetails;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.Authentication#getPrincipal()
	 */
	public Object getPrincipal() {
		return userDetails.getUsername();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.Authentication#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.Authentication#setAuthenticated(boolean)
	 */
	public void setAuthenticated(boolean value) throws IllegalArgumentException {
		this.authenticated = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Principal#getName()
	 */
	public String getName() {
		return userDetails.getUsername();
	}
}