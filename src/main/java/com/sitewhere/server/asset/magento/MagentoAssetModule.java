/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.sitewhere.server.asset.magento;

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
import com.sitewhere.server.asset.AssetMatcher;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.asset.IAssetModule;
import com.sitewhere.spi.command.CommandResult;
import com.sitewhere.spi.command.ICommandResponse;

/**
 * Asset module that interacts with an external Magento server.
 * 
 * @author dadams
 */
public class MagentoAssetModule implements IAssetModule<MagentoAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MagentoAssetModule.class);

	/** Module id */
	private static final String MODULE_ID = "magento";

	/** Module name */
	private static final String MODULE_NAME = "Magento Asset Module";

	/** Default base url for calling REST services */
	private static final String DEFAULT_URL = "http://localhost/api/rest";

	/** Base REST URL used to query for assets */
	private String baseRestUrl = DEFAULT_URL;

	/** Use CXF web client to send requests */
	private WebClient client;

	/** Jackson JSON factory */
	private ObjectMapper mapper = new ObjectMapper();

	/** Cached asset map */
	private Map<String, MagentoAsset> assetCache = new HashMap<String, MagentoAsset>();

	/** Matcher used for searches */
	protected AssetMatcher matcher = new AssetMatcher();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#start()
	 */
	public void start() throws SiteWhereException {
		LOGGER.info("Connecting to Magento instance at: " + getBaseRestUrl());
		this.client = WebClient.create(getBaseRestUrl());
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
	 * @see com.sitewhere.spi.asset.IAssetModule#isAssetTypeSupported(com.sitewhere.spi.asset.AssetType)
	 */
	public boolean isAssetTypeSupported(AssetType type) {
		if (type == AssetType.Hardware) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#getAssetById(java.lang.String)
	 */
	public MagentoAsset getAssetById(String id) throws SiteWhereException {
		return assetCache.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#search(java.lang.String)
	 */
	public List<MagentoAsset> search(String criteria) throws SiteWhereException {
		criteria = criteria.toLowerCase();
		List<MagentoAsset> results = new ArrayList<MagentoAsset>();
		if (criteria.length() == 0) {
			results.addAll(assetCache.values());
			return results;
		}
		for (MagentoAsset asset : assetCache.values()) {
			if (matcher.isHardwareMatch(asset, criteria)) {
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
	 * Parse a Magento asset from a JSON node.
	 * 
	 * @param json
	 * @return
	 * @throws SiteWhereException
	 */
	protected MagentoAsset parseFrom(JsonNode json) throws SiteWhereException {
		MagentoAsset result = new MagentoAsset();
		Iterator<String> fieldNames = json.getFieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode current = json.get(fieldName);
			result.setProperty(fieldName, current.getTextValue());
		}
		result.loadFromProperties();
		return result;
	}

	/**
	 * Make calls to the Magento server to get all products and cache them locally for fast searches.
	 * 
	 * @throws SiteWhereException
	 */
	protected ICommandResponse cacheAssetData() throws SiteWhereException {
		int pageNum = 1;
		int pageSize = 50;
		assetCache.clear();

		LOGGER.info("Caching search data.");
		int totalAssets = 0;
		long startTime = System.currentTimeMillis();
		for (;;) {
			WebClient caller = WebClient.fromClient(client);
			caller.accept(MediaType.WILDCARD_TYPE);
			caller.path("/products");
			caller.query("type", "rest");
			caller.query("page", String.valueOf(pageNum));
			caller.query("limit", String.valueOf(pageSize));
			Response response = caller.get();
			Object entity = response.getEntity();
			try {
				JsonNode json = mapper.readTree((InputStream) entity);
				Iterator<JsonNode> entries = json.getElements();
				int assetsReturned = 0;
				while (entries.hasNext()) {
					JsonNode jsonAsset = entries.next();
					MagentoAsset asset = parseFrom(jsonAsset);
					assetCache.put(asset.getId(), asset);
					assetsReturned++;
					totalAssets++;
				}
				if (assetsReturned == pageSize) {
					pageNum++;
				} else {
					break;
				}
			} catch (JsonParseException e) {
				throw new SiteWhereException("Unable to parse asset response.", e);
			} catch (IOException e) {
				throw new SiteWhereException("Unable to read asset response.", e);
			}
		}
		long totalTime = System.currentTimeMillis() - startTime;
		String message = "Cached " + totalAssets + " assets in " + totalTime + "ms.";
		LOGGER.info(message);
		return new CommandResponse(CommandResult.Successful, message);
	}

	public String getBaseRestUrl() {
		return baseRestUrl;
	}

	public void setBaseRestUrl(String baseRestUrl) {
		this.baseRestUrl = baseRestUrl;
	}
}