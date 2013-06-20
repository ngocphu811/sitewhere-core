package com.sitewhere.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Maps runtime exceptions to HTTP response codes.
 * 
 * @author Derek
 */
public class SitewhereRuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	public Response toResponse(RuntimeException exception) {
		Response.Status status;
		if (exception instanceof AccessDeniedException) {
			status = Response.Status.FORBIDDEN;
		} else if (exception instanceof AuthenticationException) {
			status = Response.Status.UNAUTHORIZED;
		} else {
			status = Response.Status.INTERNAL_SERVER_ERROR;
		}
		return Response.status(status).build();
	}
}