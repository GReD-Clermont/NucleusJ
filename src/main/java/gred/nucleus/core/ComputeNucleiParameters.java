package gred.nucleus.core;

import gred.nucleus.files.Directory;
import gred.nucleus.files.OutputTextFile;
import gred.nucleus.plugins.PluginParameters;
import ij.ImagePlus;
import ij.measure.Calibration;
import loci.plugins.BF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;


public class ComputeNucleiParameters {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final PluginParameters pluginParameters;
	
	
	/**
	 * Constructor with input, output and config files
	 *
	 * @param rawImagesInputDirectory  path to raw images
	 * @param segmentedImagesDirectory path to segmented images associated
	 * @param pathToConfig             path to config file
	 */
	public ComputeNucleiParameters(String rawImagesInputDirectory,
	                               String segmentedImagesDirectory,
	                               String pathToConfig) {
		this.pluginParameters = new PluginParameters(rawImagesInputDirectory, segmentedImagesDirectory, pathToConfig);
		
		
	}
	
	
	/**
	 * Constructor with input and output files
	 *
	 * @param rawImagesInputDirectory  path to raw images
	 * @param segmentedImagesDirectory path to segmented images associated
	 */
	public ComputeNucleiParameters(String rawImagesInputDirectory, String segmentedImagesDirectory) {
		this.pluginParameters = new PluginParameters(rawImagesInputDirectory, segmentedImagesDirectory);
		
		
	}
	
	
	/**
	 * Constructor with input, output files and calibration from dialog.
	 *
	 * @param rawImagesInputDirectory  path to raw images
	 * @param segmentedImagesDirectory path to segmented images associated
	 * @param cal                      calibration from dialog
	 */
	public ComputeNucleiParameters(String rawImagesInputDirectory, String segmentedImagesDirectory,
	                               Calibration cal) {
		this.pluginParameters = new PluginParameters(rawImagesInputDirectory, segmentedImagesDirectory,
		                                             cal.pixelWidth, cal.pixelHeight, cal.pixelDepth);
		
	}
	
	
	/**
	 * Compute nuclei parameters generate from segmentation ( OTSU / Convex Hull)
	 * Useful if parallel segmentation was used to get results parameter in the same folder.
	 */
	public void run() {
		Directory directoryRawInput = new Directory(this.pluginParameters.getInputFolder());
		directoryRawInput.listImageFiles(this.pluginParameters.getInputFolder());
		directoryRawInput.checkIfEmpty();
		Directory directorySegmentedInput = new Directory(this.pluginParameters.getOutputFolder());
		directorySegmentedInput.listImageFiles(this.pluginParameters.getOutputFolder());
		directorySegmentedInput.checkIfEmpty();
		List<File>    segmentedImages           = directorySegmentedInput.getFileList();
		StringBuilder outputCropGeneralInfoOTSU = new StringBuilder();
		
		outputCropGeneralInfoOTSU.append(this.pluginParameters.getAnalysisParameters()).append(getColNameResult());
		
		for (File f : segmentedImages) {
			ImagePlus raw = new ImagePlus(this.pluginParameters.getInputFolder() + File.separator + f.getName());
			try {
				ImagePlus[] segmented = BF.openImagePlus(f.getAbsolutePath());
				
				Measure3D measure3D = new Measure3D(segmented,
				                                    raw,
				                                    this.pluginParameters.getXCalibration(raw),
				                                    this.pluginParameters.getYCalibration(raw),
				                                    this.pluginParameters.getZCalibration(raw));
				outputCropGeneralInfoOTSU.append(measure3D.nucleusParameter3D()).append("\n");
			} catch (Exception e) {
				LOGGER.error("An error occurred.", e);
			}
		}
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(
				this.pluginParameters.getOutputFolder()
				+ directoryRawInput.getSeparator()
				+ "result_Segmentation_Analyse.csv");
		
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU.toString(), true);
		
		
	}
	
	
	public void addConfigParameters(String pathToConfig) {
		this.pluginParameters.addGeneralProperties(pathToConfig);
		
	}
	
	
	/** @return columns names for results */
	private String getColNameResult() {
		return "NucleusFileName\t" +
		       "Volume\t" +
				"Moment 1\t" +
				"Moment 2\t" +
				"Moment 3 \t" +
		       "Flatness\t" +
		       "Elongation\t" +
		       "Esr\t" +
		       "SurfaceArea\t" +
		       "Sphericity\t" +
		       "MeanIntensityNucleus\t" +
		       "MeanIntensityBackground\t" +
		       "StandardDeviation\t" +
		       "MinIntensity\t" +
		       "MaxIntensity\t" +
		       "MedianIntensityImage\t" +
		       "MedianIntensityNucleus\t" +
		       "MedianIntensityBackground\t" +
		       "ImageSize\n";
	}
	
}
