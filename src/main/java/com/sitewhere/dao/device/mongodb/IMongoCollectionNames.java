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