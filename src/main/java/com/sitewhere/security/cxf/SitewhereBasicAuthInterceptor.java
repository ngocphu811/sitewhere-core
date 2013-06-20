package com.sitewhere.security.cxf;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Passes information from basic authentication in a form that Spring security can use it.
 * 
 * @author Derek
 */
public class SitewhereBasicAuthInterceptor extends AbstractPhaseInterceptor<Message> {

	public SitewhereBasicAuthInterceptor() {
		super(Phase.RECEIVE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
	 */
	public void handleMessage(Message inMessage) {
		AuthorizationPolicy policy = inMessage.get(AuthorizationPolicy.class);
		if (policy != null) {
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					policy.getUserName(), policy.getPassword());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	}
}