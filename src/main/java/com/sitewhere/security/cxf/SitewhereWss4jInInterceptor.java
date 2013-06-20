package com.sitewhere.security.cxf;

import java.util.Map;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSConstants;

import com.sitewhere.security.SitewhereSecurity;
import com.sitewhere.server.user.SitewhereUsers;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.ws.cxf.SitewhereFault;

/**
 * Extends the WSS4J inbound interceptor.
 * 
 * @author Derek
 */
public class SitewhereWss4jInInterceptor extends WSS4JInInterceptor {

	/** Indicates whether to use a fallback account if token is missing */
	private boolean useFallbackAccount = true;

	/** User to use if fallback account is allowed */
	private String fallbackUser = SitewhereUsers.USER_ANONYMOUS;

	public SitewhereWss4jInInterceptor(Map<String, Object> properties) {
		super(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor#handleMessage(org.apache.cxf.binding.
	 * soap.SoapMessage)
	 */
	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		try {
			super.handleMessage(message);
		} catch (Fault fault) {
			if (WSConstants.INVALID_SECURITY.equals(fault.getFaultCode())) {
				if (isUseFallbackAccount()) {
					try {
						SitewhereSecurity.setAuthenticatedUser(getFallbackUser());
						return;
					} catch (SiteWhereException e) {
						throw new SitewhereFault(e);
					}
				}
			}
			throw fault;
		}
	}

	/**
	 * Set flag for using a fallback account when token is not found.
	 * 
	 * @param useFallbackAccount
	 */
	public void setUseFallbackAccount(boolean useFallbackAccount) {
		this.useFallbackAccount = useFallbackAccount;
	}

	/**
	 * Get flag for using a fallback account when token is not found.
	 * 
	 * @return
	 */
	public boolean isUseFallbackAccount() {
		return useFallbackAccount;
	}

	/**
	 * Set user to use if fallback is enabled.
	 * 
	 * @param fallbackUser
	 */
	public void setFallbackUser(String fallbackUser) {
		this.fallbackUser = fallbackUser;
	}

	/**
	 * Get user to use if fallback is enabled.
	 * 
	 * @return
	 */
	public String getFallbackUser() {
		return fallbackUser;
	}
}