/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

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