package fr.igred.nucleus.segmentation;


import fr.igred.nucleus.core.NucleusSegmentation;
import fr.igred.nucleus.plugins.PluginParameters;
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
		addProperties(pathToConfigFile);
	}
	
	
	public void addProperties(String pathToConfigFile) {
		
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
	
	
	@Override
	public String getAnalysisParameters() {
		super.getAnalysisParameters();
		this.headerInfo += "#maxVolumeNucleus:" + maxVolumeNucleus + "\n"
		                   + "#minVolumeNucleus: " + minVolumeNucleus + "\n"
		                   + "#ConvexHullDetection (" + NucleusSegmentation.CONVEX_HULL_ALGORITHM + "): "
		                   + convexHullDetection + "\n";
		return headerInfo;
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
