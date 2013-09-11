/*
 * MongoUserManagement.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.dao.user.mongodb;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sitewhere.dao.common.mongodb.MongoPersistence;
import com.sitewhere.dao.mongodb.SiteWhereMongoClient;
import com.sitewhere.rest.model.common.MetadataProvider;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.user.AccountStatus;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.spi.user.IUserManagement;
import com.sitewhere.spi.user.request.IUserCreateRequest;

/**
 * User management implementation that uses MongoDB for persistence.
 * 
 * @author dadams
 */
public class MongoUserManagement implements IUserManagement {

	/** Injected with global SiteWhere Mongo client */
	private SiteWhereMongoClient mongoClient;

	/** Password encoder */
	private MessageDigestPasswordEncoder passwordEncoder = new ShaPasswordEncoder();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.user.IUserManagement#createUser(com.sitewhere.spi.user.request.IUserCreateRequest)
	 */
	public IUser createUser(IUserCreateRequest request) throws SiteWhereException {
		IUser existing = getUserByUsername(request.getUsername());
		if (existing != null) {
			throw new SiteWhereSystemException(ErrorCode.DuplicateUser, ErrorLevel.ERROR,
					HttpServletResponse.SC_CONFLICT);
		}
		User user = new User();
		user.setUsername(request.getUsername());
		user.setHashedPassword(passwordEncoder.encodePassword(request.getPassword(), null));
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setLastLogin(null);
		user.setStatus(AccountStatus.Active);

		MetadataProvider.copy(request, user);
		MongoPersistence.initializeEntityMetadata(user);

		DBCollection users = getMongoClient().getUsersCollection();
		DBObject created = MongoUser.toDBObject(user);
		MongoPersistence.insert(users, created);
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#authenticate(java.lang.String, java.lang.String)
	 */
	public IUser authenticate(String username, String password) throws SiteWhereException {
		if (password == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_BAD_REQUEST);
		}
		DBObject userObj = assertUser(username);
		String inPassword = passwordEncoder.encodePassword(password, null);
		IUser match = MongoUser.fromDBObject(userObj);
		if (!match.getHashedPassword().equals(inPassword)) {
			throw new SiteWhereSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR,
					HttpServletResponse.SC_UNAUTHORIZED);
		}
		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#updateUser(java.lang.String,
	 * com.sitewhere.spi.user.request.IUserCreateRequest)
	 */
	public IUser updateUser(String username, IUserCreateRequest request) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#getUserByUsername(java.lang.String)
	 */
	public IUser getUserByUsername(String username) throws SiteWhereException {
		DBObject dbUser = getUserObjectByUsername(username);
		if (dbUser != null) {
			return MongoUser.fromDBObject(dbUser);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#getGrantedAuthorities(java.lang.String)
	 */
	public List<IGrantedAuthority> getGrantedAuthorities(String username) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#addGrantedAuthorities(java.lang.String, java.util.List)
	 */
	public List<IGrantedAuthority> addGrantedAuthorities(String username, List<String> authorities)
			throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#removeGrantedAuthorities(java.lang.String, java.util.List)
	 */
	public List<IGrantedAuthority> removeGrantedAuthorities(String username, List<String> authorities)
			throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#listUsers()
	 */
	public List<IUser> listUsers() throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#deleteUser(java.lang.String)
	 */
	public void deleteUser(String username) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.user.IUserManagement#createGrantedAuthority(com.sitewhere.spi.user.IGrantedAuthority)
	 */
	public IGrantedAuthority createGrantedAuthority(IGrantedAuthority auth) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#getGrantedAuthorityByName(java.lang.String)
	 */
	public IGrantedAuthority getGrantedAuthorityByName(String name) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#updateGrantedAuthority(java.lang.String,
	 * com.sitewhere.spi.user.IGrantedAuthority)
	 */
	public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthority auth)
			throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#listGrantedAuthorities()
	 */
	public List<IGrantedAuthority> listGrantedAuthorities() throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.user.IUserManagement#deleteGrantedAuthority(java.lang.String)
	 */
	public void deleteGrantedAuthority(String authority) throws SiteWhereException {
		throw new SiteWhereException("Not implmented.");
	}

	/**
	 * Get the {@link DBObject} for a User given username. Throw an exception if not found.
	 * 
	 * @param username
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject assertUser(String username) throws SiteWhereException {
		DBObject match = getUserObjectByUsername(username);
		if (match == null) {
			throw new SiteWhereSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR,
					HttpServletResponse.SC_NOT_FOUND);
		}
		return match;
	}

	/**
	 * Get the DBObject for a User given unique username.
	 * 
	 * @param username
	 * @return
	 * @throws SiteWhereException
	 */
	protected DBObject getUserObjectByUsername(String username) throws SiteWhereException {
		DBCollection users = getMongoClient().getUsersCollection();
		BasicDBObject query = new BasicDBObject(MongoUser.PROP_USERNAME, username);
		return users.findOne(query);
	}

	public SiteWhereMongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(SiteWhereMongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}