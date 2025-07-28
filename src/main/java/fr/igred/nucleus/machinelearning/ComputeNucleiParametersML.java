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
package fr.igred.nucleus.machinelearning;

import fr.igred.nucleus.core.Measure3D;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.OutputTextFile;
import fr.igred.nucleus.plugins.PluginParameters;
import fr.igred.nucleus.utils.Histogram;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import loci.formats.FormatException;
import loci.plugins.BF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;


public class ComputeNucleiParametersML {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final String rawImagesInputDirectory;
	private final String segmentedImagesDirectory;
	
	
	/**
	 * Constructor
	 *
	 * @param rawInputDir  path to raw images
	 * @param segmentedInputDir path to list of segmented images from machine learning associated to raw
	 */
	public ComputeNucleiParametersML(String rawInputDir, String segmentedInputDir) {
		this.rawImagesInputDirectory = rawInputDir;
		this.segmentedImagesDirectory = segmentedInputDir;
	}
	
	
	/**
	 * Filter connected connected component if more then 1 nuclei
	 *
	 * @param imagePlusInput
	 * @param threshold
	 *
	 * @return
	 */
	public static ImagePlus generateSegmentedImage(ImagePlus imagePlusInput, int threshold) {
		ImageStack imageStackInput    = imagePlusInput.getStack();
		ImagePlus  imagePlusSegmented = imagePlusInput.duplicate();
		
		imagePlusSegmented.setTitle(imagePlusInput.getTitle());
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					double voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue >= threshold) {
						imageStackSegmented.setVoxel(i, j, k, 255);
					} else {
						imageStackSegmented.setVoxel(i, j, k, 0);
					}
				}
			}
		}
		return imagePlusSegmented;
	}
	
	
	/**
	 * Run parameters computation parameters see Measure3D
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public void run() throws IOException, FormatException {
		PluginParameters pluginParameters = new PluginParameters(rawImagesInputDirectory, segmentedImagesDirectory);
		
		String eol = System.lineSeparator();
		
		Directory directoryInput = new Directory(pluginParameters.getOutputFolder());
		directoryInput.listImageFiles(pluginParameters.getOutputFolder());
		directoryInput.checkIfEmpty();
		
		List<File> segImages = directoryInput.getFileList();
		
		StringBuilder cropInfoOtsu = new StringBuilder(pluginParameters.getAnalysisParameters() +
		                                               getResultsColumnNames());
		for (File currentFile : segImages) {
			LOGGER.info("Current File: {}", currentFile.getName());
			ImagePlus raw = new ImagePlus(pluginParameters.getInputFolder() +
			                              directoryInput.getSeparator() +
			                              currentFile.getName());
			ImagePlus[] segmented = BF.openImagePlus(pluginParameters.getOutputFolder() + currentFile.getName());
			// TODO TRANSFORMATION FACTORISABLE AVEC METHODE DU DESSUS !!!!!
			segmented[0] = generateSegmentedImage(segmented[0], 1);
			segmented[0] = BinaryImages.componentsLabeling(segmented[0], 26, 32);
			LabelImages.removeBorderLabels(segmented[0]);
			segmented[0] = generateSegmentedImage(segmented[0], 1);
			Histogram histogram = new Histogram();
			histogram.run(segmented[0]);
			if (histogram.getNbLabels() > 0) {
				Measure3D measure3D = new Measure3D(segmented,
				                                    raw,
				                                    pluginParameters.getXCalibration(raw),
				                                    pluginParameters.getYCalibration(raw),
				                                    pluginParameters.getZCalibration(raw));
				cropInfoOtsu.append(measure3D.nucleusParameter3D()).append(eol);
			}
		}
		
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(pluginParameters.getOutputFolder()
		                                                         + directoryInput.getSeparator()
		                                                         + "result_Segmentation_Analyse.csv");
		resultFileOutputOTSU.saveTextFile(cropInfoOtsu.toString(), true);
	}
	
	
	/**
	 * @return columns names for results
	 */
	public static String getResultsColumnNames() {
		return "NucleusFileName\t" +
		       "Volume\t" +
		       "Moment 1\t" +
		       "Moment 2\t" +
		       "Moment 3 \t" +
		       "Flatness\t" +
		       "Elongation\t" +
		       "Sphericity\t" +
		       "Esr\t" +
		       "SurfaceArea\t" +
		       "SurfaceAreaCorrected\t" +
		       "SphericityCorrected\t" +
		       "MeanIntensityNucleus\t" +
		       "MeanIntensityBackground\t" +
		       "StandardDeviation\t" +
		       "MinIntensity\t" +
		       "MaxIntensity\t" +
		       "MedianIntensityImage\t" +
		       "MedianIntensityNucleus\t" +
		       "MedianIntensityBackground\t" +
		       "ImageSize\t" +
		       "OTSUThreshold\t" +
		       System.lineSeparator();
	}
	
}
