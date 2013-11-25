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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.util.StringMessageUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sitewhere.rest.model.common.SearchCriteria;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.rest.model.user.UserSearchCriteria;
import com.sitewhere.rest.service.search.SearchResults;
import com.sitewhere.security.SitewhereAuthentication;
import com.sitewhere.security.SitewhereUserDetails;
import com.sitewhere.server.metrics.DeviceManagementMetricsFacade;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.IAssetModuleManager;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.ISite;
import com.sitewhere.spi.server.device.IDeviceModelInitializer;
import com.sitewhere.spi.server.user.IUserModelInitializer;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;
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

	/** Metric regsitry */
	private MetricRegistry metricRegistry = new MetricRegistry();

	/** Health check registry */
	private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

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
	 * Get the metrics registry.
	 * 
	 * @return
	 */
	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	/**
	 * Get the health check registry.
	 * 
	 * @return
	 */
	public HealthCheckRegistry getHealthCheckRegistry() {
		return healthCheckRegistry;
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
	 * Returns a fake account used for operations on the data model done by the system.
	 * 
	 * @return
	 * @throws SiteWhereException
	 */
	public static SitewhereAuthentication getSystemAuthentication() throws SiteWhereException {
		User fake = new User();
		fake.setUsername("system");
		SitewhereUserDetails details = new SitewhereUserDetails(fake, new ArrayList<IGrantedAuthority>());
		SitewhereAuthentication auth = new SitewhereAuthentication(details, null);
		return auth;
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

		// Load device management and wrap it for metrics.
		IDeviceManagement deviceManagementImpl =
				(IDeviceManagement) SERVER_SPRING_CONTEXT.getBean(SiteWhereServerBeans.BEAN_DEVICE_MANAGEMENT);
		if (deviceManagementImpl == null) {
			throw new SiteWhereException("No device management implementation configured.");
		}
		deviceManagement = new DeviceManagementMetricsFacade(deviceManagementImpl);
		deviceManagement.start();

		// Load user management.
		userManagement =
				(IUserManagement) SERVER_SPRING_CONTEXT.getBean(SiteWhereServerBeans.BEAN_USER_MANAGEMENT);
		if (userManagement == null) {
			throw new SiteWhereException("No user management implementation configured.");
		}
		userManagement.start();

		// Load the asset module manager.
		assetModuleManager =
				(IAssetModuleManager) SERVER_SPRING_CONTEXT.getBean(SiteWhereServerBeans.BEAN_ASSET_MODULE_MANAGER);
		if (assetModuleManager == null) {
			throw new SiteWhereException("No asset module manager implementation configured.");
		}

		// Print version information.
		IVersion version = VersionHelper.getVersion();
		List<String> messages = new ArrayList<String>();
		messages.add("SiteWhere Server");
		messages.add("");
		messages.add("Version: " + version.getVersionIdentifier() + "." + version.getBuildTimestamp());
		messages.add("");
		messages.add("Copyright (c) 2013 Reveal Technologies, LLC");
		String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
		LOGGER.info("\n" + message + "\n");

		verifyUserModel();
		verifyDeviceModel();
	}

	/**
	 * Check whether user model is populated and offer to bootstrap system if not.
	 */
	protected void verifyUserModel() {
		try {
			IUserModelInitializer init =
					(IUserModelInitializer) SERVER_SPRING_CONTEXT.getBean(SiteWhereServerBeans.BEAN_USER_MODEL_INITIALIZER);
			List<IUser> users = getUserManagement().listUsers(new UserSearchCriteria());
			if (users.size() == 0) {
				List<String> messages = new ArrayList<String>();
				messages.add("User model is currently empty. A default user and permissions can be "
						+ "created automatically so that the admin interface and web services can authenticate. "
						+ "Create default user and permissions now?");
				String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
				LOGGER.info("\n" + message + "\n");
				System.out.println("Initialize user model? Yes/No (Default is Yes)");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String response = br.readLine();
				if ((response.length() == 0) || (response.toLowerCase().startsWith("y"))) {
					init.initialize(getUserManagement());
				}
			}
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No user model initializer found in Spring bean configuration. Skipping.");
			return;
		} catch (SiteWhereException e) {
			LOGGER.warn("Unable to read from user model.", e);
		} catch (IOException e) {
			LOGGER.error("Unable to read response from console.", e);
		}
	}

	/**
	 * Check whether device model is populated and offer to bootstrap system if not.
	 */
	protected void verifyDeviceModel() {
		try {
			IDeviceModelInitializer init =
					(IDeviceModelInitializer) SERVER_SPRING_CONTEXT.getBean(SiteWhereServerBeans.BEAN_DEVICE_MODEL_INITIALIZER);
			SearchResults<ISite> sites = getDeviceManagement().listSites(new SearchCriteria(1, 1));
			if (sites.getNumResults() == 0) {
				List<String> messages = new ArrayList<String>();
				messages.add("There are currently no sites defined in the system. You have the option of loading "
						+ "a default dataset for previewing system functionality. Would you like to load the default "
						+ "dataset?");
				String message = StringMessageUtils.getBoilerPlate(messages, '*', 60);
				LOGGER.info("\n" + message + "\n");
				System.out.println("Load default dataset? Yes/No (Default is Yes)");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String response = br.readLine();
				if ((response.length() == 0) || (response.toLowerCase().startsWith("y"))) {
					init.initialize(getDeviceManagement());
				}
			}
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.info("No device model initializer found in Spring bean configuration. Skipping.");
			return;
		} catch (SiteWhereException e) {
			LOGGER.warn("Unable to read from device model.", e);
		} catch (IOException e) {
			LOGGER.error("Unable to read response from console.", e);
		}
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