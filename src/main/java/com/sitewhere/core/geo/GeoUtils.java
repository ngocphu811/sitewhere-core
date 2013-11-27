/*
 * GeoUtils.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core.geo;

import java.util.List;

import com.sitewhere.spi.common.ILocation;
import com.sitewhere.spi.device.IZone;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility functions for dealing with geographic information.
 * 
 * @author Derek
 */
public class GeoUtils {

	/**
	 * Creates a JTS polygon based on zone definition.
	 * 
	 * @param zone
	 * @return
	 */
	public static Polygon createPolygonForZone(IZone zone) {
		return createPolygonForLocations(zone.getCoordinates());
	}

	/**
	 * Create a polgon for a list of locations.
	 * 
	 * @param locations
	 * @return
	 */
	public static <T extends ILocation> Polygon createPolygonForLocations(List<T> locations) {
		Coordinate[] coords = new Coordinate[locations.size() + 1];
		for (int x = 0; x < locations.size(); x++) {
			ILocation loc = locations.get(x);
			coords[x] = new Coordinate(loc.getLongitude(), loc.getLatitude());
		}
		ILocation loc = locations.get(0);
		coords[locations.size()] = new Coordinate(loc.getLongitude(), loc.getLatitude());

		GeometryFactory fact = new GeometryFactory();
		LinearRing linear = new GeometryFactory().createLinearRing(coords);
		return new Polygon(linear, null, fact);
	}
}
