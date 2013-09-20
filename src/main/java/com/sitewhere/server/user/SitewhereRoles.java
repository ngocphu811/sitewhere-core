/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.server.user;

/**
 * Constants for core roles installed with SiteWhere.
 * 
 * @author Derek
 */
public interface SitewhereRoles {

	/** Role for site adminstration (edit/delete) */
	public static final String ROLE_ADMINISTER_SITES = "ROLE_ADMINISTER_SITES";

	/** Role for administering user accounts and authorities */
	public static final String ROLE_ADMINISTER_USERS = "ROLE_ADMINISTER_USERS";
}