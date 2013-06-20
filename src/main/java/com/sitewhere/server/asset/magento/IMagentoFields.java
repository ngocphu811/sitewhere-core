package com.sitewhere.server.asset.magento;

/**
 * Interface for fields available on a Magento object.
 * 
 * @author dadams
 */
public interface IMagentoFields {

	/** Field that specifies asset id */
	public static final String PROP_ASSET_ID = "entity_id";

	/** Field that specifies SKU */
	public static final String PROP_SKU = "sku";

	/** Field that specifies name */
	public static final String PROP_NAME = "name";

	/** Field that specifies description */
	public static final String PROP_DESCRIPTION = "short_description";

	/** Field that specifies description */
	public static final String PROP_IMAGE_URL = "image_url";
}