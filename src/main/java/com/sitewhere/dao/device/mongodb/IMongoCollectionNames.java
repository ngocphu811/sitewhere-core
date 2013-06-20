/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.dao.device.mongodb;

/**
 * Constants for default MongoDB collection names.
 * 
 * @author dadams
 */
public interface IMongoCollectionNames {

	/** Default collection name for SiteWhere sites */
	public static final String DEFAULT_SITES_COLLECTION_NAME = "sites";

	/** Default collection name for SiteWhere zones */
	public static final String DEFAULT_ZONES_COLLECTION_NAME = "zones";

	/** Default collection name for SiteWhere devices */
	public static final String DEFAULT_DEVICES_COLLECTION_NAME = "devices";

	/** Default collection name for SiteWhere device assignments */
	public static final String DEFAULT_DEVICE_ASSIGNMENTS_COLLECTION_NAME = "assignments";

	/** Default collection name for SiteWhere measurements */
	public static final String DEFAULT_MEASUREMENTS_COLLECTION_NAME = "measurements";

	/** Default collection name for SiteWhere locations */
	public static final String DEFAULT_LOCATIONS_COLLECTION_NAME = "locations";

	/** Default collection name for SiteWhere alerts */
	public static final String DEFAULT_ALERTS_COLLECTION_NAME = "alerts";
}