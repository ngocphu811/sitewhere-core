package com.sitewhere.server.asset.scim;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sitewhere.rest.model.command.CommandResponse;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.asset.IAssetModule;
import com.sitewhere.spi.command.CommandResult;
import com.sitewhere.spi.command.ICommandResponse;

/**
 * Asset module that interacts with an external WSO2 Identity Server via SCIM.
 * 
 * @author dadams
 */
public class Wso2ScimAssetModule implements IAssetModule<Wso2ScimAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(Wso2ScimAssetModule.class);

	/** Module id */
	private static final String MODULE_ID = "wso2scim";

	/** Module name */
	private static final String MODULE_NAME = "WSO2 SCIM Asset Module";

	/** Default base url for user operations */
	private static final String DEFAULT_URL = "https://localhost:9443/wso2/scim/Users";

	/** Default username for basic auth */
	private static final String DEFAULT_AUTH_USERNAME = "admin";

	/** Default password for basic auth */
	private static final String DEFAULT_AUTH_PASSWORD = "admin";

	/** URL used to access user info */
	private String userUrl = DEFAULT_URL;

	/** Use CXF web client to send requests */
	private WebClient client;

	/** Jackson JSON factory */
	private ObjectMapper mapper = new ObjectMapper();

	/** Cached asset map */
	private Map<String, Wso2ScimAsset> assetCache = new HashMap<String, Wso2ScimAsset>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#start()
	 */
	public void start() throws SiteWhereException {
		LOGGER.info("Connecting to WSO2 Identity Server instance at: " + getUserUrl());
		this.client = WebClient.create(getUserUrl(), DEFAULT_AUTH_USERNAME, DEFAULT_AUTH_PASSWORD, null);
		cacheAssetData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#stop()
	 */
	public void stop() throws SiteWhereException {
		this.client = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#getId()
	 */
	public String getId() {
		return MODULE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#getName()
	 */
	public String getName() {
		return MODULE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.asset.IAssetModule#isAssetTypeSupported(com.sitewhere.spi.asset.AssetType)
	 */
	public boolean isAssetTypeSupported(AssetType type) {
		if (type == AssetType.Person) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#getAssetById(java.lang.String)
	 */
	public Wso2ScimAsset getAssetById(String id) throws SiteWhereException {
		return assetCache.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#search(java.lang.String)
	 */
	public List<Wso2ScimAsset> search(String criteria) throws SiteWhereException {
		criteria = criteria.toLowerCase();
		List<Wso2ScimAsset> results = new ArrayList<Wso2ScimAsset>();
		if (criteria.length() == 0) {
			results.addAll(assetCache.values());
			return results;
		}
		for (Wso2ScimAsset asset : assetCache.values()) {
			if ((contains(asset.getName(), criteria)) || (contains(asset.getEmailAddress(), criteria))
					|| (contains(asset.getUserName(), criteria)) || (contains(asset.getId(), criteria))) {
				results.add(asset);
			}
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#refresh()
	 */
	public ICommandResponse refresh() throws SiteWhereException {
		try {
			return cacheAssetData();
		} catch (SiteWhereException e) {
			return new CommandResponse(CommandResult.Failed, e.getMessage());
		}
	}

	/**
	 * Simplifies comparing possibly null non-case sensitive values.
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	protected boolean contains(String field, String value) {
		if (field == null) {
			return false;
		}
		return field.trim().toLowerCase().indexOf(value) != -1;
	}

	/**
	 * Make remote call to list all user assets, then parse and cache them.
	 * 
	 * @throws SiteWhereException
	 */
	protected ICommandResponse cacheAssetData() throws SiteWhereException {
		assetCache.clear();

		LOGGER.info("Caching search data.");
		int totalAssets = 0;
		long startTime = System.currentTimeMillis();
		WebClient caller = WebClient.fromClient(client);
		caller.accept(MediaType.APPLICATION_JSON_TYPE);
		Response response = caller.get();
		Object entity = response.getEntity();
		try {
			JsonNode json = mapper.readTree((InputStream) entity);
			JsonNode resources = json.get(IScimFields.RESOURCES);
			if (resources == null) {
				String message = "SCIM JSON response did not contain a 'resources' section.";
				LOGGER.info(message);
				return new CommandResponse(CommandResult.Failed, message);
			}
			Iterator<JsonNode> it = resources.getElements();
			while (it.hasNext()) {
				JsonNode resource = it.next();
				Wso2ScimAsset asset = parse(resource);
				assetCache.put(asset.getId(), asset);
				totalAssets++;
			}
		} catch (JsonParseException e) {
			throw new SiteWhereException("Unable to parse asset response.", e);
		} catch (IOException e) {
			throw new SiteWhereException("Unable to read asset response.", e);
		}
		long totalTime = System.currentTimeMillis() - startTime;
		String message = "Cached " + totalAssets + " assets in " + totalTime + "ms.";
		LOGGER.info(message);
		return new CommandResponse(CommandResult.Successful, message);
	}

	/**
	 * Parse the JSON branch that holds a SCIM resource.
	 * 
	 * @param resource
	 * @return
	 */
	protected Wso2ScimAsset parse(JsonNode resource) throws SiteWhereException {
		Wso2ScimAsset asset = new Wso2ScimAsset();
		JsonNode id = resource.get(IScimFields.ID);
		if (id == null) {
			throw new SiteWhereException("SCIM resource does not have an id.");
		}
		asset.setProperty(IWso2ScimFields.PROP_ASSET_ID, id.getTextValue());

		JsonNode username = resource.get(IScimFields.USERNAME);
		if (username != null) {
			asset.setProperty(IWso2ScimFields.PROP_USERNAME, username.getTextValue());
		}

		JsonNode profileUrl = resource.get(IScimFields.PROFILE_URL);
		if (profileUrl != null) {
			asset.setProperty(IWso2ScimFields.PROP_PROFILE_URL, profileUrl.getTextValue());
		}

		parseName(resource, asset);
		parseEmail(resource, asset);

		asset.loadFromProperties();
		return asset;
	}

	/**
	 * Parse name fields.
	 * 
	 * @param resource
	 * @param asset
	 */
	protected void parseName(JsonNode resource, Wso2ScimAsset asset) {
		JsonNode name = resource.get(IScimFields.NAME);
		if (name != null) {
			String full = "";
			JsonNode given = name.get(IScimFields.GIVEN_NAME);
			if (given != null) {
				String givenValue = given.getTextValue();
				full += givenValue + " ";
				asset.setProperty(IScimFields.GIVEN_NAME, givenValue);
			}
			JsonNode family = name.get(IScimFields.FAMILY_NAME);
			if (family != null) {
				String familyValue = family.getTextValue();
				full += familyValue;
				asset.setProperty(IScimFields.FAMILY_NAME, familyValue);
			}
			asset.setProperty(IWso2ScimFields.PROP_NAME, full.trim());
		}
	}

	/**
	 * Parse email fields.
	 * 
	 * @param resource
	 * @param asset
	 */
	protected void parseEmail(JsonNode resource, Wso2ScimAsset asset) {
		JsonNode emails = resource.get(IScimFields.EMAILS);
		if (emails != null) {
			int index = 1;
			Iterator<JsonNode> it = emails.getElements();
			while (it.hasNext()) {
				String email = it.next().getTextValue();
				asset.setProperty("emailAddress" + index, email);
			}
		}
	}

	public String getUserUrl() {
		return userUrl;
	}

	public void setUserUrl(String userUrl) {
		this.userUrl = userUrl;
	}
}