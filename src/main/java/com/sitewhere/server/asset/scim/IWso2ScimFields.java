package com.sitewhere.server.asset.scim;

/**
 * Interface for fields available via SCIM on WSO2.
 * 
 * @author dadams
 */
public interface IWso2ScimFields {

	/** Field that specifies asset id */
	public static final String PROP_ASSET_ID = IScimFields.ID;

	/** Field that specifies name */
	public static final String PROP_NAME = "fullName";

	/** Field that specifies username */
	public static final String PROP_USERNAME = IScimFields.USERNAME;

	/** Field that specifies email address */
	public static final String PROP_EMAIL_ADDRESS = "emailAddress1";

	/** Location of profile URL in from SCIM */
	public static final String PROP_PROFILE_URL = IScimFields.PROFILE_URL;
}