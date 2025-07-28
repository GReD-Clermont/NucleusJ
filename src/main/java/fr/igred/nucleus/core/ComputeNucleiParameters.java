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
package fr.igred.nucleus.core;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.OutputTextFile;
import fr.igred.nucleus.plugins.PluginParameters;
import ij.ImagePlus;
import ij.measure.Calibration;
import loci.formats.FormatException;
import loci.plugins.BF;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static fr.igred.nucleus.io.ImageSaver.saveFile;


public class ComputeNucleiParameters {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final PluginParameters pluginParameters;
	
	private String segDatasetName;
	private String currentTime;
	
	
	/**
	 * Constructor with input and output files
	 *
	 * @param rawInputDir  path to raw images
	 * @param segmentedInputDir path to segmented images associated
	 */
	public ComputeNucleiParameters(String rawInputDir, String segmentedInputDir) {
		this.pluginParameters = new PluginParameters(rawInputDir, segmentedInputDir);
	}
	
	
	public ComputeNucleiParameters() {
		String rawPath       = "." + File.separator + "raw-computeNucleiParameters";
		String segmentedPath = "." + File.separator + "segmented-computeNucleiParameters";
		
		Directory rawDirectory = new Directory(rawPath);
		rawDirectory.checkAndCreateDir();
		Directory segmentedDirectory = new Directory(segmentedPath);
		segmentedDirectory.checkAndCreateDir();
		
		this.pluginParameters = new PluginParameters(rawPath, segmentedPath);
	}
	
	
	/**
	 * Constructor with input, output files and calibration from dialog.
	 *
	 * @param rawInputDir  path to raw images
	 * @param segmentedInputDir path to segmented images associated
	 * @param cal                      calibration from dialog
	 */
	public ComputeNucleiParameters(String rawInputDir, String segmentedInputDir, Calibration cal) {
		this.pluginParameters = new PluginParameters(rawInputDir, segmentedInputDir,
		                                             cal.pixelWidth, cal.pixelHeight, cal.pixelDepth);
	}
	
	
	/**
	 * Compute nuclei parameters generate from segmentation ( OTSU / Convex Hull) Useful if parallel segmentation was
	 * used to get results parameter in the same folder.
	 */
	public void run() {
		Directory rawInputDir = new Directory(pluginParameters.getInputFolder());
		rawInputDir.listImageFiles(pluginParameters.getInputFolder());
		rawInputDir.checkIfEmpty();
		Directory segmentedInputDir = new Directory(pluginParameters.getOutputFolder());
		segmentedInputDir.listImageFiles(pluginParameters.getOutputFolder());
		segmentedInputDir.checkIfEmpty();
		List<File>    segmentedImages = segmentedInputDir.getFileList();
		StringBuilder cropInfoOTSU    = new StringBuilder();
		
		String eol = System.lineSeparator();
		
		cropInfoOTSU.append(pluginParameters.getAnalysisParameters()).append(getColNameResult());
		
		for (File f : segmentedImages) {
			ImagePlus raw = new ImagePlus(pluginParameters.getInputFolder() + File.separator + f.getName());
			try {
				ImagePlus[] segmented = BF.openImagePlus(f.getAbsolutePath());
				
				Measure3D measure3D = new Measure3D(segmented,
				                                    raw,
				                                    pluginParameters.getXCalibration(raw),
				                                    pluginParameters.getYCalibration(raw),
				                                    pluginParameters.getZCalibration(raw));
				cropInfoOTSU.append(measure3D.nucleusParameter3D()).append(eol);
			} catch (IOException | FormatException e) {
				LOGGER.error("An error occurred.", e);
			}
		}
		LocalDateTime     date      = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
		currentTime = formatter.format(date);
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(pluginParameters.getOutputFolder() +
		                                                         rawInputDir.getSeparator() +
		                                                         segDatasetName + "-" +
		                                                         currentTime + "_.csv");
		
		resultFileOutputOTSU.saveTextFile(cropInfoOTSU.toString(), true);
	}
	
	
	public void runFromOMERO(String rawDatasetID, String segmentedDatasetID, Client client)
	throws AccessException, ServiceException, ExecutionException, InterruptedException, IOException {
		DatasetWrapper rawDataset       = client.getDataset(Long.parseLong(rawDatasetID));
		DatasetWrapper segmentedDataset = client.getDataset(Long.parseLong(segmentedDatasetID));
		segDatasetName = segmentedDataset.getName();
		
		for (ImageWrapper raw : rawDataset.getImages(client)) {
			saveFile(raw.toImagePlus(client), pluginParameters.getInputFolder() + File.separator + raw.getName());
		}
		
		for (ImageWrapper segmented : segmentedDataset.getImages(client)) {
			saveFile(segmented.toImagePlus(client),
			         pluginParameters.getOutputFolder() + File.separator + segmented.getName());
		}
		
		run();
		
		segmentedDataset.addFile(client,
		                         new File(pluginParameters.getOutputFolder() + File.separator +
		                                  segDatasetName + "-" +
		                                  currentTime + "_.csv"));
		
		FileUtils.deleteDirectory(new File(pluginParameters.getInputFolder()));
		FileUtils.deleteDirectory(new File(pluginParameters.getOutputFolder()));
	}
	
	
	public void addConfigParameters(String pathToConfig) {
		pluginParameters.addGeneralProperties(pathToConfig);
		
	}
	
	
	/** @return columns names for results */
	private static String getColNameResult() {
		return "NucleusFileName\t" +
		       "Volume\t" +
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
		       "ImageSize\t" +
		       "Moment 1\t" +
		       "Moment 2\t" +
		       "Moment 3\t" +
		       "Aspect Ratio\t" +
		       " Circularity" + System.lineSeparator();
	}
	
}
