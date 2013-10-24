/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core.device;

import com.sitewhere.rest.model.asset.HardwareAsset;
import com.sitewhere.rest.model.asset.PersonAsset;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.IAsset;
import com.sitewhere.spi.device.IDeviceAssignment;

/**
 * Utility methods.
 * 
 * @author Derek Adams
 */
public class Utils {

	/**
	 * Get asset name for a device assignment.
	 * 
	 * @param assignment
	 * @return
	 * @throws SiteWhereException
	 */
	public static String getAssetNameForAssignment(IDeviceAssignment assignment) throws SiteWhereException {
		IAsset asset = SiteWhereServer.getInstance().getAssetModuleManager()
				.getAssignedAsset(assignment.getAssignmentType(), assignment.getAssetId());
		if (asset instanceof PersonAsset) {
			return ((PersonAsset) asset).getName();
		} else if (asset instanceof HardwareAsset) {
			return ((HardwareAsset) asset).getName();
		}
		return null;
	}
}