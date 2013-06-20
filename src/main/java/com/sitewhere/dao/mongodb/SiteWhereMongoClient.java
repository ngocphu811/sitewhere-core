package com.sitewhere.dao.mongodb;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.sitewhere.dao.device.mongodb.IMongoCollectionNames;

/**
 * Spring wrapper for initializing a Mongo client used by SiteWhere components.
 * 
 * @author dadams
 */
public class SiteWhereMongoClient implements InitializingBean {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SiteWhereMongoClient.class);

	/** Default hostname for Mongo */
	private static final String DEFAULT_HOSTNAME = "localhost";

	/** Default port for Mongo */
	private static final int DEFAULT_PORT = 27017;

	/** Default database name */
	private static final String DEFAULT_DATABASE_NAME = "sitewhere";

	/** Mongo client */
	private MongoClient client;

	/** Hostname used to access the Mongo datastore */
	private String hostname = DEFAULT_HOSTNAME;

	/** Port used to access the Mongo datastore */
	private int port = DEFAULT_PORT;

	/** Database that holds sitewhere collections */
	private String databaseName = DEFAULT_DATABASE_NAME;

	/** Injected name used for devices collection */
	private String devicesCollectionName = IMongoCollectionNames.DEFAULT_DEVICES_COLLECTION_NAME;

	/** Injected name used for device assignments collection */
	private String deviceAssignmentsCollectionName = IMongoCollectionNames.DEFAULT_DEVICE_ASSIGNMENTS_COLLECTION_NAME;

	/** Injected name used for sites collection */
	private String sitesCollectionName = IMongoCollectionNames.DEFAULT_SITES_COLLECTION_NAME;

	/** Injected name used for zones collection */
	private String zonesCollectionName = IMongoCollectionNames.DEFAULT_ZONES_COLLECTION_NAME;

	/** Injected name used for measurements collection */
	private String measurementsCollectionName = IMongoCollectionNames.DEFAULT_MEASUREMENTS_COLLECTION_NAME;

	/** Injected name used for locations collection */
	private String locationsCollectionName = IMongoCollectionNames.DEFAULT_LOCATIONS_COLLECTION_NAME;

	/** Injected name used for alerts collection */
	private String alertsCollectionName = IMongoCollectionNames.DEFAULT_ALERTS_COLLECTION_NAME;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		this.client = new MongoClient(getHostname(), getPort());
		LOGGER.info("Mongo client initialized. Version: " + client.getVersion());
		LOGGER.info("Devices collection name: " + getDevicesCollectionName());
		LOGGER.info("Device assignments collection name: " + getDeviceAssignmentsCollectionName());
		LOGGER.info("Sites collection name: " + getSitesCollectionName());
		LOGGER.info("Zones collection name: " + getZonesCollectionName());
		LOGGER.info("Measurements collection name: " + getMeasurementsCollectionName());
		LOGGER.info("Locations collection name: " + getLocationsCollectionName());
		LOGGER.info("Alerts collection name: " + getAlertsCollectionName());
	}

	/**
	 * Get the MongoClient.
	 * 
	 * @return
	 */
	public MongoClient getMongoClient() {
		return client;
	}

	public DB getSiteWhereDatabase() {
		return client.getDB(getDatabaseName());
	}

	public DBCollection getDevicesCollection() {
		return getSiteWhereDatabase().getCollection(getDevicesCollectionName());
	}

	public DBCollection getDeviceAssignmentsCollection() {
		return getSiteWhereDatabase().getCollection(getDeviceAssignmentsCollectionName());
	}

	public DBCollection getSitesCollection() {
		return getSiteWhereDatabase().getCollection(getSitesCollectionName());
	}

	public DBCollection getZonesCollection() {
		return getSiteWhereDatabase().getCollection(getZonesCollectionName());
	}

	public DBCollection getMeasurementsCollection() {
		return getSiteWhereDatabase().getCollection(getMeasurementsCollectionName());
	}

	public DBCollection getLocationsCollection() {
		return getSiteWhereDatabase().getCollection(getLocationsCollectionName());
	}

	public DBCollection getAlertsCollection() {
		return getSiteWhereDatabase().getCollection(getAlertsCollectionName());
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDevicesCollectionName() {
		return devicesCollectionName;
	}

	public void setDevicesCollectionName(String devicesCollectionName) {
		this.devicesCollectionName = devicesCollectionName;
	}

	public String getDeviceAssignmentsCollectionName() {
		return deviceAssignmentsCollectionName;
	}

	public void setDeviceAssignmentsCollectionName(String deviceAssignmentsCollectionName) {
		this.deviceAssignmentsCollectionName = deviceAssignmentsCollectionName;
	}

	public String getSitesCollectionName() {
		return sitesCollectionName;
	}

	public void setSitesCollectionName(String sitesCollectionName) {
		this.sitesCollectionName = sitesCollectionName;
	}

	public String getZonesCollectionName() {
		return zonesCollectionName;
	}

	public void setZonesCollectionName(String zonesCollectionName) {
		this.zonesCollectionName = zonesCollectionName;
	}

	public String getMeasurementsCollectionName() {
		return measurementsCollectionName;
	}

	public void setMeasurementsCollectionName(String measurementsCollectionName) {
		this.measurementsCollectionName = measurementsCollectionName;
	}

	public String getLocationsCollectionName() {
		return locationsCollectionName;
	}

	public void setLocationsCollectionName(String locationsCollectionName) {
		this.locationsCollectionName = locationsCollectionName;
	}

	public String getAlertsCollectionName() {
		return alertsCollectionName;
	}

	public void setAlertsCollectionName(String alertsCollectionName) {
		this.alertsCollectionName = alertsCollectionName;
	}
}