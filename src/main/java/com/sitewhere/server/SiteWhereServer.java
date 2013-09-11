/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.sitewhere.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.util.StringMessageUtils;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.IAssetModuleManager;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.user.IUserManagement;
import com.sitewhere.version.IVersion;
import com.sitewhere.version.VersionHelper;

/**
 * Singleton SiteWhere server instance.
 * 
 * @author Derek Adams
 */
public class SiteWhereServer {

	/** Private logger instance */
	private static Logger LOGGER = Logger.getLogger(SiteWhereServer.class);

	/** Singleton server instance */
	private static SiteWhereServer SINGLETON;

	/** Spring context for server */
	public static ApplicationContext SERVER_SPRING_CONTEXT;

	/** File name for SiteWhere server config file */
	public static final String SERVER_CONFIG_FILE_NAME = "sitewhere-server.xml";

	/** Interface to user management implementation */
	private IUserManagement userManagement;

	/** Interface to device management implementation */
	private IDeviceManagement deviceManagement;

	/** Interface for the asset module manager */
	private IAssetModuleManager assetModuleManager;

	/**
	 * Get the singleton server instance.
	 * 
	 * @return
	 */
	public static synchronized SiteWhereServer getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new SiteWhereServer();
		}
		return SINGLETON;
	}

	/**
	 * Get Spring application context for Atlas server objects.
	 * 
	 * @return
	 */
	public static ApplicationContext getServerSpringContext() {
		return SERVER_SPRING_CONTEXT;
	}

	/**
	 * Get the user management implementation.
	 * 
	 * @return
	 */
	public IUserManagement getUserManagement() {
		return userManagement;
	}

	/**
	 * Get the device management implementation.
	 * 
	 * @return
	 */
	public IDeviceManagement getDeviceManagement() {
		return deviceManagement;
	}

	/**
	 * Get the asset modules manager instance.
	 * 
	 * @return
	 */
	public IAssetModuleManager getAssetModuleManager() {
		return assetModuleManager;
	}

	/**
	 * Gets the CATALINA/conf/sitewhere folder where configs are stored.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	public static File getSiteWhereConfigFolder() throws SiteWhereException {
		String catalina = System.getProperty("catalina.base");
		if (catalina == null) {
			throw new SiteWhereException("CATALINA_HOME not set.");
		}
		File catFolder = new File(catalina);
		if (!catFolder.exists()) {
			throw new SiteWhereException("CATALINA_HOME folder does not exist.");
		}
		File confDir = new File(catalina, "conf");
		if (!confDir.exists()) {
			throw new SiteWhereException("CATALINA_HOME conf folder does not exist.");
		}
		File sitewhereDir = new File(confDir, "sitewhere");
		if (!confDir.exists()) {
			throw new SiteWhereException("CATALINA_HOME conf/sitewhere folder does not exist.");
		}
		return sitewhereDir;
	}

	/**
	 * Create the server.
	 * 
	 * @throws SiteWhereException
	 */
	public void create() throws SiteWhereException {
		LOGGER.info("Initializing SiteWhere server components.");
		File sitewhereConf = getSiteWhereConfigFolder();

		// Load server configuration.
		LOGGER.info("Loading Spring configuration ...");
		File serverConfigFile = new File(sitewhereConf, SERVER_CONFIG_FILE_NAME);
		if (!serverConfigFile.exists()) {
			throw new SiteWhereException("SiteWhere server configuration not found: "
					+ serverConfigFile.getAbsolutePath());
		}
		SERVER_SPRING_CONTEXT = loadServerApplicationContext(serverConfigFile);

		// Load device management.
		deviceManagement = (IDeviceManagement) SERVER_SPRING_CONTEXT
				.getBean(SiteWhereServerBeans.BEAN_DEVICE_MANAGEMENT);
		if (deviceManagement == null) {
			throw new SiteWhereException("No location management implementation configured.");
		}

		// Load user management.
		userManagement = (IUserManagement) SERVER_SPRING_CONTEXT
				.getBean(SiteWhereServerBeans.BEAN_USER_MANAGEMENT);
		if (userManagement == null) {
			throw new SiteWhereException("No user management implementation configured.");
		}

		// Load the asset module manager.
		assetModuleManager = (IAssetModuleManager) SERVER_SPRING_CONTEXT
				.getBean(SiteWhereServerBeans.BEAN_ASSET_MODULE_MANAGER);
		if (assetModuleManager == null) {
			throw new SiteWhereException("No asset module manager implementation configured.");
		}
		IVersion version = VersionHelper.getVersion();
		List<String> messages = new ArrayList<String>();
		messages.add("SiteWhere Server");
		messages.add("");
		messages.add("Version: " + version.getVersionIdentifier() + "." + version.getBuildTimestamp());
		messages.add("");
		messages.add("Copyright (c) 2013 Reveal Technologies, LLC");
		String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
		LOGGER.info("\n" + message + "\n");
	}

	/**
	 * Start the server.
	 */
	public void start() throws SiteWhereException {
		startServerComponents();
	}

	/**
	 * Start the various server components that have a lifecycle.
	 * 
	 * @throws SiteWhereException
	 */
	protected void startServerComponents() throws SiteWhereException {
		assetModuleManager.start();
	}

	/**
	 * Load the springified server configuration.
	 * 
	 * @return
	 */
	protected ApplicationContext loadServerApplicationContext(File configFile) throws SiteWhereException {
		GenericApplicationContext context = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		reader.loadBeanDefinitions(new FileSystemResource(configFile));
		context.refresh();
		return context;
	}
}