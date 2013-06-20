/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;

/**
 * Initializes the SiteWhere server.
 * 
 * @author Derek
 */
public class SiteWhereServerLoader extends HttpServlet {

	/** Serial version UUID */
	private static final long serialVersionUID = -8696135593175193509L;

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(SiteWhereServerLoader.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			SiteWhereServer.getInstance().create();
			SiteWhereServer.getInstance().start();
		} catch (SiteWhereException e) {
			LOGGER.error(e);
		} catch (Throwable e) {
			LOGGER.error(e);
		}
	}
}