package com.sitewhere.server.asset.magento;

import com.sitewhere.rest.model.asset.HardwareAsset;
import com.sitewhere.spi.asset.AssetType;

/**
 * Asset stored on a Magento server.
 * 
 * @author dadams
 */
public class MagentoAsset extends HardwareAsset {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IAsset#getType()
	 */
	public AssetType getType() {
		return AssetType.Hardware;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IHardwareAsset#getSkuProperty()
	 */
	public String getSkuProperty() {
		return IMagentoFields.PROP_SKU;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.HardwareAsset#getNameProperty()
	 */
	public String getNameProperty() {
		return IMagentoFields.PROP_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.HardwareAsset#getDescriptionProperty()
	 */
	public String getDescriptionProperty() {
		return IMagentoFields.PROP_DESCRIPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.Asset#getIdProperty()
	 */
	public String getIdProperty() {
		return IMagentoFields.PROP_ASSET_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.HardwareAsset#getImageUrlProperty()
	 */
	public String getImageUrlProperty() {
		return IMagentoFields.PROP_IMAGE_URL;
	}
}