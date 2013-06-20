package com.sitewhere.server;

/**
 * Constants for Spring beans needed by the SiteWhere server.
 * 
 * @author Derek
 */
public interface SiteWhereServerBeans {

	/** Bean id for user management in server configuration */
	public static final String BEAN_USER_MANAGEMENT = "userManagement";

	/** Bean id for device management in server configuration */
	public static final String BEAN_DEVICE_MANAGEMENT = "deviceManagement";

	/** Bean id for asset module manager in server configuration */
	public static final String BEAN_ASSET_MODULE_MANAGER = "assetModuleManager";
}