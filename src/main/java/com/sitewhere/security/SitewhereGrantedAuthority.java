package com.sitewhere.security;

import org.springframework.security.core.GrantedAuthority;

import com.sitewhere.spi.user.IGrantedAuthority;

/**
 * SiteWhere implementation of granted authority.
 * 
 * @author Derek
 */
public class SitewhereGrantedAuthority implements GrantedAuthority {

	/** Serial verison UID */
	private static final long serialVersionUID = 1L;

	/** Authority */
	private String authority;

	public SitewhereGrantedAuthority(IGrantedAuthority auth) {
		this.authority = auth.getAuthority();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.GrantedAuthority#getAuthority()
	 */
	public String getAuthority() {
		return authority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if ((o != null) && (o instanceof GrantedAuthority)) {
			return getAuthority().compareTo(((GrantedAuthority) o).getAuthority());
		}
		return -1;
	}
}