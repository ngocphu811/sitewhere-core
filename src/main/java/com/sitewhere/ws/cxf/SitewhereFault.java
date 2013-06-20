/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
*
* The software in this package is published under the terms of the CPAL v1.0
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/

package com.sitewhere.ws.cxf;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.interceptor.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;

/**
 * CXF fault that wraps an Atlas exception.
 * 
 * @author Derek
 */
public class SitewhereFault extends Fault {

	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	/** Details node */
	private Element detail;

	/** Wrapped exception */
	private SiteWhereException sitewhereException;

	public SitewhereFault(SiteWhereException e) {
		super(e);
		this.sitewhereException = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.cxf.interceptor.Fault#getDetail()
	 */
	public Element getDetail() {
		if ((detail == null) && (sitewhereException instanceof SiteWhereSystemException)) {
			SiteWhereSystemException sysEx = (SiteWhereSystemException) sitewhereException;
			try {
				Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.newDocument();
				detail = dom.createElement("detail");
				Element atlas = dom.createElement("error");
				atlas.setAttribute("id", sysEx.getCode().toString());
				atlas.setAttribute("code", String.valueOf(sysEx.getCode().getCode()));
				atlas.setAttribute("severity", sysEx.getLevel().toString());
				detail.appendChild(atlas);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return detail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.cxf.interceptor.Fault#setDetail(org.w3c.dom.Element)
	 */
	public void setDetail(Element detail) {
		this.detail = detail;
	}

	public SiteWhereException getSitewhereException() {
		return sitewhereException;
	}

	public void setSitewhereException(SiteWhereException sitewhereException) {
		this.sitewhereException = sitewhereException;
	}
}