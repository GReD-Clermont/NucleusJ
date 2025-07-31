/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.igred.nucleus.segmentation;


import fr.igred.nucleus.core.PluginParameters;
import fr.igred.nucleus.utils.ConvexHullDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;


public class SegmentationParameters extends PluginParameters {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Convex Hull algorithm option */
	private boolean convexHullDetection = true;
	/** Minimal object volume to segment */
	private int     minVolumeNucleus    = 1;
	/** Maximal object volume to segment */
	private int     maxVolumeNucleus    = 3000000;
	
	
	/**
	 * Constructor with default parameter
	 *
	 * @param inputFolder  Path folder containing Images
	 * @param outputFolder Path folder output analyse
	 */
	public SegmentationParameters(String inputFolder, String outputFolder) {
		super(inputFolder, outputFolder);
	}
	
	
	public SegmentationParameters(String inputFolder,
	                              String outputFolder,
	                              int minVolume, int maxVolume,
	                              boolean convexHull) {
		super(inputFolder, outputFolder);
		this.minVolumeNucleus = minVolume;
		this.maxVolumeNucleus = maxVolume;
		this.convexHullDetection = convexHull;
	}
	
	
	public SegmentationParameters(String inputFolder,
	                              String outputFolder,
	                              int xCal,
	                              int yCal,
	                              int zCal,
	                              int minVolume,
	                              int maxVolume,
	                              boolean convexHull) {
		super(inputFolder, outputFolder, xCal, yCal, zCal);
		this.minVolumeNucleus = minVolume;
		this.maxVolumeNucleus = maxVolume;
		this.convexHullDetection = convexHull;
	}
	
	
	public SegmentationParameters(String inputFolder, String outputFolder, String pathToConfigFile) {
		super(inputFolder, outputFolder, pathToConfigFile);
		setSegmentationPropertiesFromFile(pathToConfigFile);
	}
	
	
	private void setSegmentationPropertiesFromFile(String pathToConfigFile) {
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(pathToConfigFile)) {
			prop.load(is);
		} catch (FileNotFoundException ex) {
			LOGGER.error("{}: can't find the config file !", pathToConfigFile, ex);
			System.exit(-1);
		} catch (IOException ex) {
			LOGGER.error("{}: can't load the config file !", pathToConfigFile, ex);
			System.exit(-1);
		}
		for (String idProp : prop.stringPropertyNames()) {
			if ("ConvexHullDetection".equals(idProp)) {
				this.convexHullDetection = Boolean.parseBoolean(prop.getProperty("ConvexHullDetection"));
			}
			if ("maxVolumeNucleus".equals(idProp)) {
				this.maxVolumeNucleus = Integer.parseInt(prop.getProperty("maxVolumeNucleus"));
			}
			if ("minVolumeNucleus".equals(idProp)) {
				this.minVolumeNucleus = Integer.parseInt(prop.getProperty("minVolumeNucleus"));
			}
		}
	}
	
	
	public void addProperties(String pathToConfigFile) {
		setSegmentationPropertiesFromFile(pathToConfigFile);
	}
	
	
	@Override
	public String getAnalysisParameters() {
		super.getAnalysisParameters();
		String eol = System.lineSeparator();
		String newInfo = "#maxVolumeNucleus:" + maxVolumeNucleus + eol +
		                 "#minVolumeNucleus: " + minVolumeNucleus + eol +
		                 "#ConvexHullDetection (" + ConvexHullDetection.CONVEX_HULL_ALGORITHM + "): " +
		                 convexHullDetection + eol;
		setHeaderInfo(getHeaderInfo() + newInfo);
		return getHeaderInfo();
	}
	
	
	public int getMinVolumeNucleus() {
		return minVolumeNucleus;
	}
	
	
	public void setMinVolumeNucleus(int vMin) {
		this.minVolumeNucleus = vMin;
	}
	
	
	public int getMaxVolumeNucleus() {
		return maxVolumeNucleus;
	}
	
	
	public void setMaxVolumeNucleus(int vMax) {
		this.maxVolumeNucleus = vMax;
	}
	
	
	public boolean getConvexHullDetection() {
		return convexHullDetection;
	}
	
}
