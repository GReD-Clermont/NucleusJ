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
package fr.igred.nucleus.autocrop;

import fr.igred.nucleus.core.PluginParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

import static java.lang.Integer.parseInt;


/** This class extend plugin parameters and contain the list of specific parameters available for Autocrop function. */
public class AutocropParameters extends PluginParameters {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Minimal object volume to crop */
	private int minVolumeNucleus = 1;
	/** Maximal object volume to crop */
	private int maxVolumeNucleus = 2147483647;
	
	/** Number of pixels take plus object size in x */
	private int xCropBoxSize = 40;
	/** Number of pixels take plus object size in y */
	private int yCropBoxSize = 40;
	/** Number of slice take plus object in y */
	private int zCropBoxSize = 20;
	
	/** Font size of the box number */
	private int     numberFontSize              = 30;
	/** Minimal default OTSU threshold */
	private int     thresholdOTSUComputing      = 20;
	/** Channel to compute OTSU threshold */
	private int     channelToComputeThreshold;
	/** Slice start to compute OTSU threshold */
	private int     slicesOTSUComputing;
	/** Surface percent of boxes to groups them */
	private int     boxesSurfacePercent = 50;
	/** Activation of boxes regrouping */
	private boolean boxesRegrouping     = true;
	
	
	/**
	 * Constructor with default parameter
	 *
	 * @param inputFolder  Path folder containing Images
	 * @param outputFolder Path folder output analyse
	 */
	public AutocropParameters(String inputFolder, String outputFolder) {
		super(inputFolder, outputFolder);
	}
	
	
	/**
	 * Constructor with all manual parameters 2
	 *
	 * @param inputFolder         Path folder containing Images
	 * @param outputFolder        Path folder output analyse
	 * @param xCropBoxSize        Number of voxels add in x axis around object
	 * @param yCropBoxSize        Number of voxels add in z axis around object
	 * @param zCropBoxSize        Number of stack add in z axis around object
	 * @param otsuStartSlice      Slice start to compute OTSU
	 * @param otsuMinThreshold    Minimum OTSU threshold used
	 * @param thresholdChannel    Channel number to compute OTSU
	 * @param maxVolumeNucleus    Volume maximum of objects detected
	 * @param minVolumeNucleus    Volume minimum of objects detected
	 * @param boxesSurfacePercent Surface percent of boxes to groups them
	 * @param boxesRegrouping     Activation of boxes regrouping
	 */
	public AutocropParameters(String inputFolder, String outputFolder,
	                          int xCropBoxSize,
	                          int yCropBoxSize,
	                          int zCropBoxSize,
	                          int numberFontSize,
	                          int otsuStartSlice,
	                          int otsuMinThreshold,
	                          int thresholdChannel,
	                          int minVolumeNucleus,
	                          int maxVolumeNucleus,
	                          int boxesSurfacePercent,
	                          boolean boxesRegrouping) {
		
		super(inputFolder, outputFolder);
		this.xCropBoxSize = xCropBoxSize;
		this.yCropBoxSize = yCropBoxSize;
		this.zCropBoxSize = zCropBoxSize;
		this.numberFontSize = numberFontSize;
		this.thresholdOTSUComputing = otsuMinThreshold;
		this.slicesOTSUComputing = otsuStartSlice;
		this.channelToComputeThreshold = thresholdChannel;
		this.maxVolumeNucleus = maxVolumeNucleus;
		this.minVolumeNucleus = minVolumeNucleus;
		this.boxesRegrouping = boxesRegrouping;
		this.boxesSurfacePercent = boxesSurfacePercent;
	}
	
	
	/**
	 * Constructor with box size modification and slice number used to start OTSU threshold calculation to last slice
	 *
	 * @param inputFolder         Path folder containing Images
	 * @param outputFolder        Path folder output analyse
	 * @param xCal                Image calibration X
	 * @param yCal                Image calibration Y
	 * @param zCal                Image calibration Z
	 * @param xCropBoxSize        Number of voxels add in x axis around object
	 * @param yCropBoxSize        Number of voxels add in z axis around object
	 * @param zCropBoxSize        Number of stack add in z axis around object
	 * @param thresholdChannel    Channel number to compute OTSU
	 * @param otsuStartSlice      Slice start to compute OTSU
	 * @param otsuMinThreshold    Minimum OTSU threshold used
	 * @param maxVolumeNucleus    Volume maximum of objects detected
	 * @param minVolumeNucleus    Volume minimum of objects detected
	 * @param boxesSurfacePercent Surface percent of boxes to groups them
	 * @param regroupBoxes        Activation of boxes regrouping
	 */
	public AutocropParameters(String inputFolder, String outputFolder,
	                          double xCal,
	                          double yCal,
	                          double zCal,
	                          int xCropBoxSize,
	                          int yCropBoxSize,
	                          int zCropBoxSize,
	                          int numberFontSize,
	                          int otsuStartSlice,
	                          int otsuMinThreshold,
	                          int thresholdChannel,
	                          int minVolumeNucleus,
	                          int maxVolumeNucleus,
	                          int boxesSurfacePercent,
	                          boolean regroupBoxes) {
		super(inputFolder, outputFolder, xCal, yCal, zCal);
		this.xCropBoxSize = xCropBoxSize;
		this.yCropBoxSize = yCropBoxSize;
		this.zCropBoxSize = zCropBoxSize;
		this.numberFontSize = numberFontSize;
		this.thresholdOTSUComputing = otsuMinThreshold;
		this.slicesOTSUComputing = otsuStartSlice;
		this.channelToComputeThreshold = thresholdChannel;
		this.maxVolumeNucleus = maxVolumeNucleus;
		this.minVolumeNucleus = minVolumeNucleus;
		this.boxesSurfacePercent = boxesSurfacePercent;
		this.boxesRegrouping = regroupBoxes;
	}
	
	
	/**
	 * Constructor using input , output folders and config file (for command line execution)
	 *
	 * @param inputFolder      Path folder containing Images
	 * @param outputFolder     Path folder output analyse
	 * @param pathToConfigFile Path to the config file
	 */
	public AutocropParameters(String inputFolder, String outputFolder,
	                          String pathToConfigFile) {
		super(inputFolder, outputFolder, pathToConfigFile);
		setAutocropPropertiesFromFile(pathToConfigFile);
	}
	
	
	private void setAutocropPropertiesFromFile(String pathToConfigFile) {
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(pathToConfigFile)) {
			prop.load(is);
		} catch (FileNotFoundException ex) {
			LOGGER.error("{}: can't find the config file !", pathToConfigFile);
			System.exit(-1);
		} catch (IOException ex) {
			LOGGER.error("{}: can't load the config file !", pathToConfigFile);
			System.exit(-1);
		}
		for (String idProp : prop.stringPropertyNames()) {
			switch (idProp) {
				case "xCropBoxSize":
					this.xCropBoxSize = parseInt(prop.getProperty("xCropBoxSize"));
					break;
				case "yCropBoxSize":
					this.yCropBoxSize = parseInt(prop.getProperty("yCropBoxSize"));
					break;
				case "zCropBoxSize":
					this.zCropBoxSize = parseInt(prop.getProperty("zCropBoxSize"));
					break;
				case "boxNumberFontSize":
					this.numberFontSize = parseInt(prop.getProperty("boxNumberFontSize"));
					break;
				case "thresholdOTSUComputing":
					this.thresholdOTSUComputing = parseInt(prop.getProperty("thresholdOTSUComputing"));
					break;
				case "slicesOTSUComputing":
					this.slicesOTSUComputing = parseInt(prop.getProperty("slicesOTSUComputing"));
					break;
				case "channelToComputeThreshold":
					this.channelToComputeThreshold = parseInt(prop.getProperty("channelToComputeThreshold"));
					break;
				case "maxVolumeNucleus":
					this.maxVolumeNucleus = parseInt(prop.getProperty("maxVolumeNucleus"));
					break;
				case "minVolumeNucleus":
					this.minVolumeNucleus = parseInt(prop.getProperty("minVolumeNucleus"));
					break;
				case "boxesPercentSurfaceToFilter":
					this.boxesSurfacePercent = parseInt(prop.getProperty("boxesPercentSurfaceToFilter"));
					break;
				case "boxesRegrouping":
					this.boxesRegrouping = Boolean.parseBoolean(prop.getProperty("boxesRegrouping"));
					break;
				default:
					LOGGER.warn("Unknown property in config file: {}", idProp);
			}
		}
	}
	
	
	public void addProperties(String pathToConfigFile) {
		setAutocropPropertiesFromFile(pathToConfigFile);
	}
	
	
	/**
	 * Method to get parameters of the analysis
	 *
	 * @return : list of the parameters used for the analyse
	 */
	@Override
	public String getAnalysisParameters() {
		super.getAnalysisParameters();
		String eol = System.lineSeparator();
		String newInfo = "#X box size: " + xCropBoxSize + eol +
		                 "#Y box size: " + yCropBoxSize + eol +
		                 "#Z box size: " + zCropBoxSize + eol +
		                 "#thresholdOTSUComputing: " + thresholdOTSUComputing + eol +
		                 "#slicesOTSUComputing: " + slicesOTSUComputing + eol +
		                 "#channelToComputeThreshold: " + channelToComputeThreshold + eol +
		                 "#maxVolumeNucleus:" + maxVolumeNucleus + eol +
		                 "#minVolumeNucleus: " + minVolumeNucleus + eol +
		                 "#boxesRegrouping: " + boxesRegrouping + eol +
		                 "#boxesPercentSurfaceToFilter: " + boxesSurfacePercent + eol;
		setHeaderInfo(getHeaderInfo() + newInfo);
		
		return getHeaderInfo();
	}
	
	
	/**
	 * Getter for x box size in pixel
	 *
	 * @return x box size in pixel
	 */
	public int getXCropBoxSize() {
		return xCropBoxSize;
	}
	
	
	/**
	 * Getter for y box size in pixel
	 *
	 * @return y box size in pixel
	 */
	public int getYCropBoxSize() {
		return yCropBoxSize;
	}
	
	
	/**
	 * Getter for z box size in pixel
	 *
	 * @return z box size in pixel
	 */
	public int getZCropBoxSize() {
		return zCropBoxSize;
	}
	
	
	/**
	 * Getter for the font size of the box number
	 *
	 * @return font size
	 */
	public int getNumberFontSize() {
		return numberFontSize;
	}
	
	
	/**
	 * Getter for OTSU threshold used to compute segmented image
	 *
	 * @return OTSU threshold used
	 */
	public int getThresholdOTSUComputing() {
		return thresholdOTSUComputing;
	}
	
	
	/**
	 * Getter for channel number used to segmented image (OTSU computing)
	 *
	 * @return channel number
	 */
	public int getChannelToComputeThreshold() {
		return channelToComputeThreshold;
	}
	
	
	/**
	 * Getter for minimum volume object segmented
	 *
	 * @return minimum volume
	 */
	public int getMinVolumeNucleus() {
		return minVolumeNucleus;
	}
	
	
	/**
	 * Getter for maximum volume object segmented
	 *
	 * @return maximum volume
	 */
	public int getMaxVolumeNucleus() {
		return maxVolumeNucleus;
	}
	
	
	/**
	 * Getter for start slice used to compute OTSU
	 *
	 * @return start slice
	 */
	public int getSlicesOTSUComputing() {
		return slicesOTSUComputing;
	}
	
	
	/**
	 * Getter boxes merging activation
	 *
	 * @return status
	 */
	public boolean getBoxesRegrouping() {
		return boxesRegrouping;
	}
	
	
	/**
	 * Getter percent of surface intersection to merge 2 rectangles.
	 *
	 * @return percentage surface
	 */
	public int getBoxesSurfacePercent() {
		return boxesSurfacePercent;
	}
	
}
