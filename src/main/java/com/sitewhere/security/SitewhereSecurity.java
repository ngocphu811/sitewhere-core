package com.sitewhere.security;

import java.util.List;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.server.user.SitewhereUsers;
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
			throw new SiteWhereException("Unable to find fallback user.");
		}
		List<IGrantedAuthority> auths = SiteWhereServer.getInstance().getUserManagement()
				.getAllGrantedAuthorities(SitewhereUsers.USER_ANONYMOUS);
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