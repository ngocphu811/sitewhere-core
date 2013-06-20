package com.sitewhere.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.ws.cxf.SitewhereFault;

@Path("/bootstrap")
@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
public class BootstrapService {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(BootstrapService.class);

	/**
	 * Initialize the user model.
	 * 
	 * @throws SitewhereFault
	 */
	@GET
	@Path("/usermodel")
	@Produces({ MediaType.TEXT_HTML })
	public String initializeUserManagement() {
		try {
			SiteWhereServer.getInstance().initializeUserModel();
			return "<html><body><h1>User model initialized.</h1></body></html>";
		} catch (SiteWhereException e) {
			LOGGER.error(e);
			return "<html><body><h1>User model initialization failed.</h1><h2>" + e.getMessage()
					+ "</h2></body></html>";
		}
	}
}