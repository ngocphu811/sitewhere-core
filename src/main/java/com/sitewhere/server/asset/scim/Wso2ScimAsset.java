package com.sitewhere.server.asset.scim;

import com.sitewhere.rest.model.asset.PersonAsset;

/**
 * Person asset loaded from the WSO2 SCIM interface.
 * 
 * @author dadams
 */
public class Wso2ScimAsset extends PersonAsset {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.PersonAsset#getUserNameProperty()
	 */
	public String getUserNameProperty() {
		return IWso2ScimFields.PROP_USERNAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.PersonAsset#getNameProperty()
	 */
	public String getNameProperty() {
		return IWso2ScimFields.PROP_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.PersonAsset#getEmailAddressProperty()
	 */
	public String getEmailAddressProperty() {
		return IWso2ScimFields.PROP_EMAIL_ADDRESS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.rest.model.asset.Asset#getIdProperty()
	 */
	public String getIdProperty() {
		return IWso2ScimFields.PROP_ASSET_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.asset.IPersonAsset#getPhotoUrlProperty()
	 */
	public String getPhotoUrlProperty() {
		return IWso2ScimFields.PROP_PROFILE_URL;
	}
}