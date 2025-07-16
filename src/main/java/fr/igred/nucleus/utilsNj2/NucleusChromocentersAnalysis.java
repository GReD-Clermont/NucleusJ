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
package fr.igred.nucleus.utilsNj2;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.nucleus.plugins.ChromocenterParameters;
import fr.igred.nucleus.utils.Histogram;
import ij.ImagePlus;
import ij.measure.Calibration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;

import static fr.igred.nucleus.core.RadialDistance.computeBarycenterToBorderDistances;
import static fr.igred.nucleus.core.RadialDistance.computeBorderToBorderDistances;


/**
 * Several method to realise and create the outfile for the nuclear Analysis this class contains the chromocenter
 * parameters
 *
 * @author Tristan Dubos and Axel Poulet
 */
public final class NucleusChromocentersAnalysis {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	//TODO INTEGRATION CLASS NEW MEASURE 3D
	
	
	/** Private constructor to avoid instantiation */
	private NucleusChromocentersAnalysis() {
		// Private constructor to avoid instantiation
	}
	
	
	/**
	 * Analysis for several nuclei, the results are stock on output file
	 *
	 * @param rhfChoice
	 * @param imagePlusInput
	 * @param imagePlusSegmented
	 * @param imagePlusChromocenter
	 *
	 * @throws IOException
	 */
	public static File[] compute3DParameters(String rhfChoice,
	                                         ImagePlus imagePlusInput, ImagePlus imagePlusSegmented,
	                                         ImagePlus imagePlusChromocenter,
	                                         ChromocenterParameters chromocenterParameters) throws IOException {
		LOGGER.info("3D PARAMETERS ");
		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		Calibration calibration = imagePlusInput.getCalibration();
		double      voxelVolume = calibration.pixelDepth * calibration.pixelHeight * calibration.pixelWidth;
		
		imagePlusSegmented.setCalibration(calibration);
		Measure3D measure3D = new Measure3D(imagePlusSegmented, imagePlusInput,
		                                    imagePlusInput.getCalibration().pixelWidth,
		                                    imagePlusInput.getCalibration().pixelHeight,
		                                    imagePlusInput.getCalibration().pixelDepth);
		File    fileResults   = new File(chromocenterParameters.outputFolder + "NucAndCcParameters3D.tab");
		File    fileResultsCC = new File(chromocenterParameters.outputFolder + "CcParameters3D.tab");
		boolean exist         = fileResults.exists();
		
		
		String text   = "";
		String textCC = "";
		if (!exist) {
			text = chromocenterParameters.getAnalysisParametersNodeJ();
			text += getResultsColumnNames();
			textCC = chromocenterParameters.getAnalysisParametersNodeJ();
			textCC += getResultsColumnNamesCC();
		}
		
		text += measure3D.nucleusParameter3D() + "," +
		        measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter) + ",";
		
		if (histogram.getNbLabels() > 0) {
			double[] tVolumesObjects = measure3D.computeVolumeOfAllObjects(imagePlusChromocenter);
			
			double volumeCcMean = computeMeanOfTable(tVolumesObjects);
			int    nbCc         = Measure3D.getNumberOfObject(imagePlusChromocenter);
			double[] tBorderToBorderDistance = computeBorderToBorderDistances(imagePlusSegmented,
			                                                                  imagePlusChromocenter);
			double[] tBarycenterToBorderDistance = computeBarycenterToBorderDistances(imagePlusSegmented,
			                                                                          imagePlusChromocenter);
			double[] tIntensity = measure3D.computeIntensityofAllObjects(imagePlusChromocenter);
			
			double[] tBarycenterToBorderDistanceTableNucleus = computeBarycenterToBorderDistances(imagePlusSegmented,
			                                                                                      imagePlusSegmented);
			text += nbCc + "," +
			        volumeCcMean + "," +
			        volumeCcMean * nbCc + "," +
			        computeMeanOfTable(tIntensity) + "," +
			        computeMeanOfTable(tBorderToBorderDistance) + "," +
			        computeMeanOfTable(tBarycenterToBorderDistance) + ",";
			
			for (int i = 0; i < tBorderToBorderDistance.length; ++i) {
				textCC += imagePlusInput.getTitle() + "_" + i + "," +
				          tVolumesObjects[i] + "," +
				          tIntensity[i] + "," +
				          tBarycenterToBorderDistance[i] + "," +
				          tBorderToBorderDistance[i] + "," +
				          tBarycenterToBorderDistanceTableNucleus[0] + ",";
			}
		} else {
			text += "0\t0\t0\tNaN\tNaN\t";
		}
		
		text += voxelVolume + "\n";
		
		try (BufferedWriter output = new BufferedWriter(new FileWriter(fileResults, true))) {
			output.write(text);
			output.flush();
		}
		
		try (BufferedWriter outputCC = new BufferedWriter(new FileWriter(fileResultsCC, true))) {
			outputCC.write(textCC);
			outputCC.flush();
		}
		return new File[]{fileResults, fileResultsCC};
		
	}
	
	
	public static File[] compute3DParametersOmero(String rhfChoice,
	                                              ImageWrapper imageInput,
	                                              ImageWrapper imageSegmented,
	                                              ImagePlus imagePlusChromocenter,
	                                              ChromocenterParameters chromocenterParameters,
	                                              String datasetName,
	                                              Client client)
	throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
		long imageId = imageInput.getId();  // Get the image ID
		
		// Image to ImagePlus conversion
		ImagePlus[] rawImage           = {imageInput.toImagePlus(client)};
		ImagePlus[] segImage           = {imageSegmented.toImagePlus(client)};
		ImagePlus   imagePlusInput     = rawImage[0];
		ImagePlus   imagePlusSegmented = segImage[0];
		
		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		Calibration calibration = imagePlusInput.getCalibration();
		double      voxelVolume = calibration.pixelDepth * calibration.pixelHeight * calibration.pixelWidth;
		
		imagePlusSegmented.setCalibration(calibration);
		Measure3D measure3D = new Measure3D(imagePlusSegmented, imagePlusInput,
		                                    imagePlusInput.getCalibration().pixelWidth,
		                                    imagePlusInput.getCalibration().pixelHeight,
		                                    imagePlusInput.getCalibration().pixelDepth);
		
		File fileResults = new File(chromocenterParameters.outputFolder + "NucAndCcParameters3D.csv");
		
		File fileResultsParade = new File(chromocenterParameters.outputFolder + "NucAndCcParameters3D_Parade.csv");
		
		File    fileResultsCC       = new File(chromocenterParameters.outputFolder + "CcParameters3D.csv");
		File    fileResultsCCParade = new File(chromocenterParameters.outputFolder + "CcParameters3D_Parade.csv");
		boolean exist               = fileResults.exists();
		
		String text         = "";
		String textCC       = "";
		String textParade   = "";
		String textCCParade = "";
		
		// Add header if file does not exist
		if (!exist) {
			text = chromocenterParameters.getAnalysisParametersNodeJ();  // Add image as the first column
			text += getResultsColumnNames();  // Existing column names
			textCC = chromocenterParameters.getAnalysisParametersNodeJ();
			textCC += getResultsColumnNamesCC();
			
			textParade = "image,Dataset," + getResultsColumnNames();  // Add image to Parade version too
			textCCParade = "image,Dataset," + getResultsColumnNamesCC();
		}
		
		// Append the imageId at the beginning of the result string
		text += measure3D.nucleusParameter3D() + "," +
		        measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter) + ",";
		textParade += imageId + "," + datasetName + "," + measure3D.nucleusParameter3D() + "," +
		              measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter) + ",";
		
		if (histogram.getNbLabels() > 0) {
			double[] tVolumesObjects = measure3D.computeVolumeOfAllObjects(imagePlusChromocenter);
			double   volumeCcMean    = computeMeanOfTable(tVolumesObjects);
			int      nbCc            = Measure3D.getNumberOfObject(imagePlusChromocenter);
			double[] tBorderToBorderDistance = computeBorderToBorderDistances(imagePlusSegmented,
			                                                                  imagePlusChromocenter);
			double[] tBarycenterToBorderDistance = computeBarycenterToBorderDistances(imagePlusSegmented,
			                                                                          imagePlusChromocenter);
			double[] tIntensity = measure3D.computeIntensityofAllObjects(imagePlusChromocenter);
			double[] tBarycenterToBorderDistanceTableNucleus = computeBarycenterToBorderDistances(imagePlusSegmented,
			                                                                                      imagePlusSegmented);
			
			text += nbCc + "," + volumeCcMean + "," + volumeCcMean * nbCc + "," +
			        computeMeanOfTable(tIntensity) + "," +
			        computeMeanOfTable(tBorderToBorderDistance) + "," +
			        computeMeanOfTable(tBarycenterToBorderDistance) + ",";
			
			textParade += nbCc + "," + volumeCcMean + "," + volumeCcMean * nbCc + "," +
			              computeMeanOfTable(tIntensity) + "," +
			              computeMeanOfTable(tBorderToBorderDistance) + "," +
			              computeMeanOfTable(tBarycenterToBorderDistance) + ",";
			
			for (int i = 0; i < tBorderToBorderDistance.length; ++i) {
				textCC += imagePlusInput.getTitle() + "_" + i + "," +
				          tVolumesObjects[i] + "," +
				          tIntensity[i] + "," +
				          tBarycenterToBorderDistance[i] + "," +
				          tBorderToBorderDistance[i] + "," +
				          tBarycenterToBorderDistanceTableNucleus[0] + "\n";
				
				textCCParade += imageId + "," + datasetName + "," + imagePlusInput.getTitle() + "_" + i + "," +
				                tVolumesObjects[i] + "," +
				                tIntensity[i] + "," +
				                tBarycenterToBorderDistance[i] + "," +
				                tBorderToBorderDistance[i] + "," +
				                tBarycenterToBorderDistanceTableNucleus[0] + "\n";
			}
		} else {
			text += "0\t0\t0\tNaN\tNaN\t\n";  // Default values if no labels
		}
		
		text += voxelVolume + "\n";  // Append voxelVolume at the end of the row
		textParade += voxelVolume + "\n";  // Same for Parade
		
		try (BufferedWriter output = new BufferedWriter(new FileWriter(fileResults, true))) {
			output.write(text);
			output.flush();
		}
		
		try (BufferedWriter outputParade = new BufferedWriter(new FileWriter(fileResultsParade, true))) {
			outputParade.write(textParade);
			outputParade.flush();
		}
		
		try (BufferedWriter outputCC = new BufferedWriter(new FileWriter(fileResultsCC, true))) {
			outputCC.write(textCC);
			outputCC.flush();
		}
		
		try (BufferedWriter outputCCParade = new BufferedWriter(new FileWriter(fileResultsCCParade, true))) {
			outputCCParade.write(textCCParade);
			outputCCParade.flush();
		}
		
		return new File[]{fileResults, fileResultsCC, fileResultsParade, fileResultsCCParade};
	}
	
	
	/**
	 * Method wich compute the mean of the value in the table
	 *
	 * @param tInput Table of value
	 *
	 * @return Mean of the table
	 */
	public static double computeMeanOfTable(double[] tInput) {
		double mean = 0;
		for (double v : tInput) {
			mean += v;
		}
		mean /= tInput.length;
		return mean;
	}
	
	
	public static String getResultsColumnNames() {
		return "ImageName," +
		       "Volume," +
		       "Flatness," +
		       "Elongation," +
		       "Esr," +
		       //"SurfaceArea\t" +
		       //"Sphericity\t" +
		       "MeanIntensityNucleus," +
		       "MeanIntensityBackground," +
		       "StandardDeviation," +
		       "MinIntensity," +
		       "MaxIntensity," +
		       "MedianIntensityImage," +
		       "MedianIntensityNucleus," +
		       "MedianIntensityBackground," +
		       "ImageSize," +
		       "VolumeRHF," +
		       "NbCc," +
		       "VCcMean," +
		       "VCcTotal," +
		       "normIntensityMean," +
		       "DistanceBorderToBorderMean," +
		       "DistanceBarycenterToBorderMean," +
		       "VoxelVolume\n";
	}
	
	
	public static String getResultsColumnNamesCC() {
		return "ImageName," +
		       "Volume," +
		       "NormIntensity," +
		       "BorderToBorderDistance," +
		       "BarycenterToBorderDistance," +
		       "BarycenterToBorderDistanceNucleus\n";
	}
	
}