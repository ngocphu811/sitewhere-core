/*
 * IVersion.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.version;

/**
 * Interface for getting version information.
 * 
 * @author Derek
 */
public interface IVersion {

	/**
	 * Gets the Maven version identifier.
	 * 
	 * @return
	 */
	public String getVersionIdentifier();

	/**
	 * Gets the build timestamp.
	 * 
	 * @return
	 */
	public String getBuildTimestamp();
}