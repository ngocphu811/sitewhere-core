package com.sitewhere.server.asset.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.sitewhere.rest.model.asset.HardwareAsset;
import com.sitewhere.rest.model.command.CommandResponse;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.server.asset.AssetMatcher;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.asset.IAssetModule;
import com.sitewhere.spi.command.CommandResult;
import com.sitewhere.spi.command.ICommandResponse;

/**
 * Modules that loads a list of hardware assets from an XML file on the filesystem.
 * 
 * @author Derek Adams
 */
public class FileSystemHardwareAssetModule implements IAssetModule<HardwareAsset> {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(FileSystemHardwareAssetModule.class);

	/** Module id */
	public static final String MODULE_ID = "filesystem-hardware";

	/** Module name */
	public static final String MODULE_NAME = "Filesystem Hardware Asset Module";

	/** Module name */
	public static final String ASSETS_FOLDER = "assets";

	/** Filename in SiteWhere config folder that contains hardware assets */
	public static final String HARDWARE_CONFIG_FILENAME = "hardware-assets.xml";

	/** Map of assets by unique id */
	protected Map<String, HardwareAsset> assetsById;

	/** Matcher used for searches */
	protected AssetMatcher matcher = new AssetMatcher();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#start()
	 */
	public void start() throws SiteWhereException {
		reload();
	}

	/**
	 * Reloads list of hardware assets from the filesystem.
	 */
	protected void reload() throws SiteWhereException {
		File config = SiteWhereServer.getSiteWhereConfigFolder();
		File assetsFolder = new File(config, ASSETS_FOLDER);
		if (!assetsFolder.exists()) {
			throw new SiteWhereException("Assets subfolder not found. Looking for: "
					+ assetsFolder.getAbsolutePath());
		}
		File hardwareConfig = new File(assetsFolder, HARDWARE_CONFIG_FILENAME);
		if (!hardwareConfig.exists()) {
			throw new SiteWhereException("Hardware assets file missing. Looking for: "
					+ hardwareConfig.getAbsolutePath());
		}
		LOGGER.info("Loading hardware assets from: " + hardwareConfig.getAbsolutePath());

		// Unmarshal assets from XML file and store in data object.
		List<HardwareAsset> assets = new ArrayList<HardwareAsset>();
		Map<String, HardwareAsset> assetsById = new HashMap<String, HardwareAsset>();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FileSystemHardwareAssets.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			FileSystemHardwareAssets xmlAssets = (FileSystemHardwareAssets) jaxbUnmarshaller
					.unmarshal(hardwareConfig);
			for (FileSystemHardwareAsset xmlAsset : xmlAssets.getHardwareAssets()) {
				HardwareAsset asset = new HardwareAsset();
				asset.setId(xmlAsset.getId());
				asset.setName(xmlAsset.getName());
				asset.setDescription(xmlAsset.getDescription());
				asset.setSku(xmlAsset.getSku());
				asset.setImageUrl(xmlAsset.getImageUrl());
				for (FileSystemAssetProperty xmlProperty : xmlAsset.getProperties()) {
					asset.setProperty(xmlProperty.getName(), xmlProperty.getValue());
				}
				assets.add(asset);
				assetsById.put(asset.getId(), asset);
			}
			this.assetsById = assetsById;
			String message = "Loaded " + assetsById.size() + " assets.";
			LOGGER.info(message);
		} catch (Exception e) {
			throw new SiteWhereException("Unable to unmarshal hardware assets file.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#stop()
	 */
	public void stop() throws SiteWhereException {
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
	public HardwareAsset getAssetById(String id) throws SiteWhereException {
		return assetsById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModule#search(java.lang.String)
	 */
	public List<HardwareAsset> search(String criteria) throws SiteWhereException {
		criteria = criteria.toLowerCase();
		List<HardwareAsset> results = new ArrayList<HardwareAsset>();
		if (criteria.length() == 0) {
			results.addAll(assetsById.values());
			return results;
		}
		for (HardwareAsset asset : assetsById.values()) {
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
			reload();
			String message = "Loaded " + assetsById.size() + " assets.";
			return new CommandResponse(CommandResult.Successful, message);
		} catch (SiteWhereException e) {
			return new CommandResponse(CommandResult.Failed, e.getMessage());
		}
	}
}