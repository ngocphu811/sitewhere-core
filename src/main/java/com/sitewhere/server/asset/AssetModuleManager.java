package com.sitewhere.server.asset;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sitewhere.rest.model.asset.Asset;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.AssetType;
import com.sitewhere.spi.asset.IAsset;
import com.sitewhere.spi.asset.IAssetModule;
import com.sitewhere.spi.asset.IAssetModuleManager;

/**
 * Manages the list of modules
 * 
 * @author dadams
 */
public class AssetModuleManager implements IAssetModuleManager {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(AssetModuleManager.class);

	/** List of asset modules */
	private List<IAssetModule<?>> modules;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModuleManager#start()
	 */
	public void start() throws SiteWhereException {
		for (IAssetModule<?> module : modules) {
			LOGGER.info("Starting asset module: " + module.getName());
			module.start();
			LOGGER.info("Started asset module: " + module.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModuleManager#stop()
	 */
	public void stop() {
		for (IAssetModule<?> module : modules) {
			try {
				LOGGER.info("Stopping asset module: " + module.getName());
				module.stop();
				LOGGER.info("Stopped asset module: " + module.getName());
			} catch (SiteWhereException e) {
				LOGGER.error("Unable to stop asset module.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.asset.IAssetModuleManager#getAssetById(com.sitewhere.spi.asset.AssetType,
	 * java.lang.String)
	 */
	public IAsset getAssetById(AssetType type, String id) throws SiteWhereException {
		for (IAssetModule<?> module : modules) {
			if (module.isAssetTypeSupported(type)) {
				IAsset result = module.getAssetById(id);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModuleManager#search(com.sitewhere.spi.asset.AssetType,
	 * java.lang.String)
	 */
	public List<? extends IAsset> search(AssetType type, String criteria) throws SiteWhereException {
		for (IAssetModule<?> module : modules) {
			if (module.isAssetTypeSupported(type)) {
				return module.search(criteria);
			}
		}
		return new ArrayList<Asset>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAssetModuleManager#getModules()
	 */
	public List<IAssetModule<?>> getModules() {
		return modules;
	}

	public void setModules(List<IAssetModule<?>> modules) {
		this.modules = modules;
	}
}