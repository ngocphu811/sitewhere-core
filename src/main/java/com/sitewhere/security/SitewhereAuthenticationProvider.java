package com.sitewhere.security;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;

/**
 * Spring authentication provider backed by Atlas.
 * 
 * @author Derek
 */
public class SitewhereAuthenticationProvider implements AuthenticationProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.springframework.security.providers.AuthenticationProvider#authenticate(org.
	 * springframework.security. Authentication)
	 */
	public Authentication authenticate(Authentication input) throws AuthenticationException {
		String username = (String) input.getPrincipal();
		String password = (String) input.getCredentials();
		try {
			IUser user = SiteWhereServer.getInstance().getUserManagement()
					.authenticate(username, password);
			List<IGrantedAuthority> auths = SiteWhereServer.getInstance().getUserManagement()
					.getAllGrantedAuthorities(username);
			SitewhereUserDetails details = new SitewhereUserDetails(user, auths);
			return new SitewhereAuthentication(details);
		} catch (SiteWhereException e) {
			throw new BadCredentialsException("Unable to authenticate.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.providers.AuthenticationProvider#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class clazz) {
		return true;
	}
}