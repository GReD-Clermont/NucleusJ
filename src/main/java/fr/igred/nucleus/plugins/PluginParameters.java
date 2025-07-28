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
package fr.igred.nucleus.plugins;

import fr.igred.nucleus.io.Directory;
import ij.IJ;
import ij.ImagePlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

import static java.time.LocalDateTime.now;


public class PluginParameters {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Activation of Gaussian Filter */
	protected boolean gaussianIsOn;
	/** Activation of manual calibration parameter */
	private   boolean manualParameter;
	/** X calibration plugin parameter */
	private   double  xCal = 1;
	/** y calibration plugin parameter */
	private   double  yCal = 1;
	/** z calibration plugin parameter */
	private   double  zCal = 1;
	/** Input folder */
	private   String  inputFolder;
	/** Output folder */
	private   String  outputFolder;
	/** Autocrop parameters information */
	private   String  headerInfo;
	
	
	/**
	 * Constructor with default parameter
	 *
	 * @param inputFolder  Path folder containing Images
	 * @param outputFolder Path folder output analyse
	 */
	public PluginParameters(String inputFolder, String outputFolder) {
		setInputFolder(inputFolder);
		setOutputFolder(outputFolder);
	}
	
	
	/**
	 * Constructor with specific calibration in x y and z
	 *
	 * @param inputFolder  Path folder containing Images
	 * @param outputFolder Path folder output analyse
	 * @param xCal         x calibration voxel
	 * @param yCal         Y calibration voxel
	 * @param zCal         Z calibration voxel
	 */
	public PluginParameters(String inputFolder, String outputFolder, double xCal, double yCal, double zCal) {
		setInputFolder(inputFolder);
		setOutputFolder(outputFolder);
		this.manualParameter = true;
		this.xCal = xCal;
		this.yCal = yCal;
		this.zCal = zCal;
	}
	
	
	public PluginParameters(String inputFolder, String outputFolder, double xCal, double yCal, double zCal,
	                        boolean gaussian) {
		setInputFolder(inputFolder);
		setOutputFolder(outputFolder);
		this.manualParameter = true;
		this.gaussianIsOn = gaussian;
		this.xCal = xCal;
		this.yCal = yCal;
		this.zCal = zCal;
	}
	
	
	/**
	 * Constructor using input , output folders and config file (for command line execution)
	 *
	 * @param inputFolder      Path folder containing Images
	 * @param outputFolder     Path folder output analyse
	 * @param pathToConfigFile Path to the config file
	 */
	public PluginParameters(String inputFolder, String outputFolder, String pathToConfigFile) {
		setInputFolder(inputFolder);
		setOutputFolder(outputFolder);
		setPropertiesFromFile(pathToConfigFile);
	}
	
	
	/**
	 * get local time start analysisinformation yyyy-MM-dd:HH-mm-ss format
	 *
	 * @return time in yyyy-MM-dd:HH-mm-ss format
	 */
	public static String getLocalTime() {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm-ss", Locale.getDefault())
		                        .format(now());
	}
	
	
	private void setPropertiesFromFile(String pathToConfigFile) {
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
				case "xCal":
					setXCal(Double.parseDouble(prop.getProperty("xCal")));
					break;
				case "yCal":
					setYCal(Double.parseDouble(prop.getProperty("yCal")));
					break;
				case "zCal":
					setZCal(Double.parseDouble(prop.getProperty("zCal")));
					break;
				default:
					// Do nothing for other properties
			}
		}
	}
	
	
	public void addGeneralProperties(String pathToConfigFile) {
		setPropertiesFromFile(pathToConfigFile);
	}
	
	
	/**
	 * Getter : input path
	 *
	 * @return input path folder
	 */
	public String getInputFolder() {
		return inputFolder;
	}
	
	
	private void setInputFolder(String inputFolder) {
		File input = new File(inputFolder);
		if (input.isDirectory()) {
			this.inputFolder = inputFolder;
		} else if (input.isFile()) {
			this.inputFolder = input.getParent();
		} else {
			LOGGER.error("{}: can't find the input folder/file !", inputFolder);
			IJ.error(inputFolder + " : can't find the input folder/file !");
		}
	}
	
	
	/**
	 * Getter : output path
	 *
	 * @return output path folder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}
	
	
	private void setOutputFolder(String outputFolder) {
		if (outputFolder == null) {
			LOGGER.error("Output directory is missing");
			System.exit(-1);
		} else {
			Directory dirOutput = new Directory(outputFolder);
			dirOutput.checkAndCreateDir();
			this.outputFolder = dirOutput.getDirPath();
		}
	}
	
	
	/**
	 * Getter : HEADER parameter of the analysis containing path input output folder and x y z calibration on parameter
	 * per line
	 *
	 * @return output path folder
	 */
	public String getAnalysisParameters() {
		String eol = System.lineSeparator();
		this.headerInfo = "#Header" + eol +
		                  "#Start time analysis: " + getLocalTime() + eol +
		                  "#Input folder: " + inputFolder + eol +
		                  "#Output folder: " + outputFolder + eol +
		                  "#Calibration:" + getInfoCalibration() + eol;
		return headerInfo;
		
	}
	
	
	public String getAnalysisParametersNodeJ() {
		String eol = System.lineSeparator();
		this.headerInfo = "#Header" + eol +
		                  "#Start time analysis: " + getLocalTime() + eol +
		                  "#Input folder: " + inputFolder + eol +
		                  "#Output folder: " + outputFolder + eol +
		                  "#Gaussian Blur:" + getInfoGaussianBlur() + eol;
		return headerInfo;
		
	}
	
	
	public String getInfoGaussianBlur() {
		String parametersInfo;
		if (gaussianIsOn) {
			parametersInfo = "x:" + xCal + "-y:" + yCal + "-z:" + zCal;
		} else {
			parametersInfo = "False";
		}
		return parametersInfo;
		
	}
	
	
	/**
	 * Getter : image x y z calibration
	 *
	 * @return output path folder
	 */
	public String getInfoCalibration() {
		String parametersInfo;
		if (manualParameter) {
			parametersInfo = "x:" + xCal + "-y:" + yCal + "-z:" + zCal;
		} else {
			parametersInfo = "x:default-y:default-z:default";
		}
		return parametersInfo;
		
	}
	
	
	public double getVoxelVolume() {
		return xCal * yCal * zCal;
		
	}
	
	
	public double getXCal() {
		return xCal;
	}
	
	
	public void setXCal(double manualXCal) {
		this.xCal = manualXCal;
		this.manualParameter = true;
	}
	
	
	public double getYCal() {
		return yCal;
	}
	
	
	public void setYCal(double manualYCal) {
		this.yCal = manualYCal;
		this.manualParameter = true;
	}
	
	
	public double getZCal() {
		return zCal;
	}
	
	
	public void setZCal(double manualZCal) {
		this.zCal = manualZCal;
		this.manualParameter = true;
	}
	
	
	public boolean isManualParameter() {
		return manualParameter;
	}
	
	
	public double getXCalibration(ImagePlus raw) {
		double xCalibration;
		if (manualParameter) {
			xCalibration = xCal;
		} else {
			xCalibration = raw.getCalibration().pixelWidth;
		}
		return xCalibration;
	}
	
	
	public double getYCalibration(ImagePlus raw) {
		double yCalibration;
		if (manualParameter) {
			yCalibration = yCal;
		} else {
			yCalibration = raw.getCalibration().pixelHeight;
		}
		return yCalibration;
	}
	
	
	public double getZCalibration(ImagePlus raw) {
		double zCalibration;
		if (manualParameter) {
			zCalibration = zCal;
		} else {
			zCalibration = raw.getCalibration().pixelDepth;
		}
		return zCalibration;
	}
	
	
	/** Autocrop parameters information */
	public String getHeaderInfo() {
		return headerInfo;
	}
	
	
	public void setHeaderInfo(String headerInfo) {
		this.headerInfo = headerInfo;
	}
	
}
