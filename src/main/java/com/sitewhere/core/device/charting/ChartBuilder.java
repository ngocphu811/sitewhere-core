/*
 * ChartBuilder.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.core.device.charting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sitewhere.rest.model.device.charting.ChartEntry;
import com.sitewhere.rest.model.device.charting.ChartSeries;
import com.sitewhere.spi.common.IMeasurementEntry;
import com.sitewhere.spi.device.IDeviceMeasurements;
import com.sitewhere.spi.device.charting.IChartSeries;

/**
 * Builds chart series from measurements.
 * 
 * @author Derek
 */
public class ChartBuilder {

	/** Map of measurement names to series */
	private Map<String, IChartSeries<Double>> seriesByMeasurementName;

	/**
	 * Process measurements into a list of charts series.
	 * 
	 * @param matches
	 * @return
	 */
	public List<IChartSeries<Double>> process(List<IDeviceMeasurements> matches) {
		seriesByMeasurementName = new HashMap<String, IChartSeries<Double>>();

		// Add all measurements.
		for (IDeviceMeasurements measurements : matches) {
			for (IMeasurementEntry entry : measurements.getMeasurements()) {
				addMeasurementEntry(entry, measurements.getEventDate());
			}
		}
		// Sort entries by date.
		List<IChartSeries<Double>> results = new ArrayList<IChartSeries<Double>>();
		for (IChartSeries<Double> series : seriesByMeasurementName.values()) {
			Collections.sort(series.getEntries());
			results.add(series);
		}
		return results;
	}

	/**
	 * Add a new measurement entry. Create a new series if one does not already exist.
	 * 
	 * @param entry
	 */
	protected void addMeasurementEntry(IMeasurementEntry entry, Date date) {
		String measurement = entry.getName();
		IChartSeries<Double> series = seriesByMeasurementName.get(measurement);
		if (series == null) {
			ChartSeries<Double> newSeries = new ChartSeries<Double>();
			newSeries.setMeasurementId(measurement);
			seriesByMeasurementName.put(measurement, newSeries);
			series = newSeries;
		}
		ChartEntry<Double> seriesEntry = new ChartEntry<Double>();
		seriesEntry.setValue(entry.getValue());
		seriesEntry.setMeasurementDate(date);
		series.getEntries().add(seriesEntry);
	}
}