package com.sitewhere.server.asset.scim;

/**
 * Field names in the SCIM spec.
 * 
 * @author dadams
 */
public interface IScimFields {

	/** Resources section */
	public static final String RESOURCES = "Resources";

	/** Username field */
	public static final String ID = "id";

	/** Username field */
	public static final String USERNAME = "userName";

	/** Name section */
	public static final String NAME = "name";

	/** Family name field */
	public static final String FAMILY_NAME = "familyName";

	/** Given name field */
	public static final String GIVEN_NAME = "givenName";

	/** Profile URL field */
	public static final String PROFILE_URL = "profileUrl";

	/** Emails section */
	public static final String EMAILS = "emails";

	/** Roles section */
	public static final String ROLES = "roles";
}