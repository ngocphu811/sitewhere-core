package com.sitewhere.server.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.sitewhere.rest.model.user.GrantedAuthority;
import com.sitewhere.rest.model.user.Group;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IGroup;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.spi.user.IUserManagement;

/**
 * Initializes the user model with the base users / groups / authoritites.
 * 
 * @author Derek
 */
public class UserModelInitializer {

	/** User management implementation */
	private IUserManagement userManagement;

	/** Password encoder implementation */
	private ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	public UserModelInitializer(IUserManagement userManagement) {
		this.userManagement = userManagement;
	}

	/**
	 * Initialize the user model.
	 * 
	 * @throws SiteWhereException
	 */
	public void initialize() throws SiteWhereException {
		createSitewhereUser();
		createSitewhereAdminsGroup();
		createUserAdminAuthority();
		getUserManagement().addUserToGroups(SitewhereUsers.USER_SITEWHERE,
				singleStringList(SitewhereGroups.GROUP_SITEWHERE_ADMINS));
		getUserManagement().addGroupGrantedAuthorities(SitewhereGroups.GROUP_SITEWHERE_ADMINS,
				singleStringList(SitewhereRoles.ROLE_USER_MGMT_ADMIN));
	}

	/**
	 * Create the SiteWhere user.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	protected IUser createSitewhereUser() throws SiteWhereException {
		IUser existing = getUserManagement().getUserByUsername(SitewhereUsers.USER_SITEWHERE);
		if (existing != null) {
			throw new SiteWhereException("SiteWhere user already exists.");
		}
		User newSitewhere = new User();
		newSitewhere.setUsername(SitewhereUsers.USER_SITEWHERE);
		newSitewhere.setHashedPassword(getPasswordEncoder().encodePassword("sitewhere", null));
		newSitewhere.setFirstName("SiteWhere");
		newSitewhere.setLastName("Admin");
		return getUserManagement().createUser(newSitewhere);
	}

	/**
	 * Create the SiteWhere administrators group.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	protected IGroup createSitewhereAdminsGroup() throws SiteWhereException {
		IGroup atlasAdmins = getUserManagement().getGroupByName(SitewhereGroups.GROUP_SITEWHERE_ADMINS);
		if (atlasAdmins != null) {
			throw new SiteWhereException("SiteWhere admins group already exists.");
		}
		Group newGroup = new Group();
		newGroup.setName(SitewhereGroups.GROUP_SITEWHERE_ADMINS);
		newGroup.setDescription("SiteWhere administrators");
		return getUserManagement().createGroup(newGroup);
	}

	/**
	 * Create the SiteWhere user management administrator authority.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	protected IGrantedAuthority createUserAdminAuthority() throws SiteWhereException {
		IGrantedAuthority existing = getUserManagement().getGrantedAuthorityByName(
				SitewhereRoles.ROLE_USER_MGMT_ADMIN);
		if (existing != null) {
			throw new SiteWhereException("User management admin authority already exists.");
		}
		GrantedAuthority auth = new GrantedAuthority();
		auth.setAuthority(SitewhereRoles.ROLE_USER_MGMT_ADMIN);
		auth.setDescription("User management administrator");
		return getUserManagement().createGrantedAuthority(auth);
	}

	/**
	 * Get a string value as a single-item list.
	 * 
	 * @param value
	 * @return
	 */
	protected List<String> singleStringList(String value) {
		List<String> result = new ArrayList<String>();
		result.add(value);
		return result;
	}

	protected void setUserManagement(IUserManagement userManagement) {
		this.userManagement = userManagement;
	}

	protected IUserManagement getUserManagement() {
		return userManagement;
	}

	protected void setPasswordEncoder(ShaPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	protected ShaPasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}
}