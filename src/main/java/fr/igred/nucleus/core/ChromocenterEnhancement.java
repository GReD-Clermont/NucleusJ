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

import fr.igred.nucleus.gradient.MyGradient;
import fr.igred.nucleus.utils.RegionalExtremaFilter;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;
import inra.ijpb.binary.BinaryImages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static inra.ijpb.watershed.Watershed.computeWatershed;


/**
 * Several method to create the image of contrasted regions
 *
 * @author Tristan Dubos and Axel Poulet
 */
public final class ChromocenterEnhancement {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/** Private constructor to prevent instantiation */
	private ChromocenterEnhancement() {
		// This class should not be instantiated
	}
	
	
	/**
	 * compute and create the image contrast with the raw image and the segmented image
	 *
	 * @param imagePlusRaw       raw image
	 * @param imagePlusSegmented segmented image of the nucleus
	 *
	 * @return image of the contrasted region
	 */
	public static ImagePlus applyEnhanceChromocenters(ImagePlus imagePlusRaw, ImagePlus imagePlusSegmented) {
		MyGradient            myGradient        = new MyGradient(imagePlusRaw, imagePlusSegmented);
		ImagePlus             imagePlusGradient = myGradient.run();
		RegionalExtremaFilter regExtremaFilter  = new RegionalExtremaFilter();
		regExtremaFilter.setMask(imagePlusSegmented);
		ImagePlus imagePlusExtrema   = regExtremaFilter.applyWithMask(imagePlusGradient);
		ImagePlus imagePlusLabels    = BinaryImages.componentsLabeling(imagePlusExtrema, 26, 32);
		ImagePlus imagePlusWatershed = computeWatershed(imagePlusGradient,
		                                                imagePlusLabels,
		                                                imagePlusSegmented,
		                                                26, false, false);
		// Change -1 value in 0
		// TODO remove this line after updating morpholib_J versions (>=1.4.3)
		imagePlusWatershed = convertNegativeValue(imagePlusWatershed);
		double[] contrast = computeContrast(imagePlusRaw, imagePlusWatershed);
		return computeImage(imagePlusWatershed, contrast);
	}
	
	
	/**
	 * Compute the region adjacency graph. The aim is to detect the  neighboring region.
	 *
	 * @param imagePlusWatershed image results of the watershed
	 *
	 * @return a float table which contain the value of the contrast between each region
	 */
	private static double[][] getRegionAdjacencyGraph(ImagePlus imagePlusWatershed) {
		int             voxelValue;
		int             neighborVoxelValue;
		ImageStatistics imageStatistics = new StackStatistics(imagePlusWatershed);
		
		double[][] regAdjacencyGraph = new double[(int) imageStatistics.histMax + 1][(int) imageStatistics.histMax + 1];
		
		Calibration calibration         = imagePlusWatershed.getCalibration();
		double      volumeVoxel         = calibration.pixelWidth * calibration.pixelHeight * calibration.pixelDepth;
		ImageStack  imageStackWatershed = imagePlusWatershed.getStack();
		for (int k = 1; k < imagePlusWatershed.getNSlices() - 1; ++k) {
			for (int i = 1; i < imagePlusWatershed.getWidth() - 1; ++i) {
				for (int j = 1; j < imagePlusWatershed.getHeight() - 1; ++j) {
					voxelValue = (int) imageStackWatershed.getVoxel(i, j, k);
					for (int kk = k - 1; kk <= k + 1; kk += 2) {
						neighborVoxelValue = (int) imageStackWatershed.getVoxel(i, j, kk);
						
						if (neighborVoxelValue > 0 && voxelValue != neighborVoxelValue) {
							regAdjacencyGraph[voxelValue][neighborVoxelValue] += volumeVoxel;
						}
					}
					for (int jj = j - 1; jj <= j + 1; jj += 2) {
						neighborVoxelValue = (int) imageStackWatershed.getVoxel(i, jj, k);
						if (neighborVoxelValue > 0 && voxelValue != neighborVoxelValue) {
							regAdjacencyGraph[voxelValue][neighborVoxelValue] += volumeVoxel;
						}
					}
					for (int ii = i - 1; ii <= i + 1; ii += 2) {
						neighborVoxelValue = (int) imageStackWatershed.getVoxel(ii, j, k);
						if (neighborVoxelValue > 0 && voxelValue != neighborVoxelValue) {
							regAdjacencyGraph[voxelValue][neighborVoxelValue] += volumeVoxel;
						}
					}
				}
			}
		}
		return regAdjacencyGraph;
	}
	
	
	/**
	 * Compute the contrasts between neighboring region.
	 *
	 * @param imagePlusRaw     raw image
	 * @param imagePlusRegions image of the contrasted regions
	 *
	 * @return table of contrast
	 */
	private static double[] computeContrast(ImagePlus imagePlusRaw, ImagePlus imagePlusRegions) {
		double[][] tRegAdjacencyGraph = getRegionAdjacencyGraph(imagePlusRegions);
		double[]   tMean              = computeMeanIntensity(imagePlusRaw, imagePlusRegions);
		double[]   tContrast          = new double[tRegAdjacencyGraph.length + 1];
		double     neighborVolumeTotal;
		for (int i = 1; i < tRegAdjacencyGraph.length; ++i) {
			neighborVolumeTotal = 0;
			for (int j = 1; j < tRegAdjacencyGraph[i].length; ++j) {
				if (tRegAdjacencyGraph[i][j] > 0 && i != j) {
					tContrast[i] += tRegAdjacencyGraph[i][j] * (tMean[i] - tMean[j]);
					neighborVolumeTotal += tRegAdjacencyGraph[i][j];
				}
			}
			if (tContrast[i] <= 0 || neighborVolumeTotal == 0) {
				tContrast[i] = 0;
			} else {
				tContrast[i] /= neighborVolumeTotal;
			}
		}
		return tContrast;
	}
	
	
	/**
	 * Compute the mean of value voxel for each region
	 *
	 * @param imagePlusInput     ImagePlus raw image
	 * @param imagePlusWatershed ImagePlus of the results of the watershed
	 *
	 * @return table of double of average intensity for each watershed label
	 */
	private static double[] computeMeanIntensity(ImagePlus imagePlusInput, ImagePlus imagePlusWatershed) {
		ImageStatistics imageStatistics      = new StackStatistics(imagePlusWatershed);
		ImageStack      imageStackWatershed  = imagePlusWatershed.getStack();
		ImageStack      imageStackInput      = imagePlusInput.getStack();
		double[]        tIntensityTotal      = new double[(int) imageStatistics.histMax + 1];
		double[]        tIntensityMean       = new double[(int) imageStatistics.histMax + 1];
		int[]           tNbVoxelInEachRegion = new int[(int) imageStatistics.histMax + 1];
		int             voxelValue;
		for (int k = 0; k < imagePlusWatershed.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusWatershed.getWidth(); ++i) {
				for (int j = 0; j < imagePlusWatershed.getHeight(); ++j) {
					voxelValue = (int) imageStackWatershed.getVoxel(i, j, k);
					if (voxelValue > 0) {
						tIntensityTotal[voxelValue] += imageStackInput.getVoxel(i, j, k);
						++tNbVoxelInEachRegion[voxelValue];
					}
				}
			}
		}
		for (int i = 1; i < tIntensityTotal.length; ++i) {
			tIntensityMean[i] = tIntensityTotal[i] / tNbVoxelInEachRegion[i];
		}
		return tIntensityMean;
	}
	
	
	/**
	 * Creation of the image of contrasted regions
	 *
	 * @param imagePlusInput ImagePlus raw image
	 * @param tVoxelValue    table of double of the mean region value
	 *
	 * @return ImagePlus image contrast
	 */
	private static ImagePlus computeImage(ImagePlus imagePlusInput, double[] tVoxelValue) {
		double     voxelValue;
		ImagePlus  imagePlusContrast  = imagePlusInput.duplicate();
		ImageStack imageStackContrast = imagePlusContrast.getStack();
		for (int k = 0; k < imagePlusContrast.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusContrast.getWidth(); ++i) {
				for (int j = 0; j < imagePlusContrast.getHeight(); ++j) {
					voxelValue = imageStackContrast.getVoxel(i, j, k);
					if (voxelValue > 0) {
						imageStackContrast.setVoxel(i, j, k, tVoxelValue[(int) voxelValue]);
					}
				}
			}
		}
		return imagePlusContrast;
	}
	
	
	/**
	 * Converts values from -1 to 0 following the release of a new MorpholibJ version
	 *
	 * @param imagePlusInput
	 *
	 * @return
	 */
	private static ImagePlus convertNegativeValue(ImagePlus imagePlusInput) {
		ImagePlus  imagePlusContrast  = imagePlusInput.duplicate();
		ImageStack imageStackContrast = imagePlusContrast.getStack();
		for (int k = 0; k < imagePlusContrast.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusContrast.getWidth(); ++i) {
				for (int j = 0; j < imagePlusContrast.getHeight(); ++j) {
					if (imageStackContrast.getVoxel(i, j, k) == -1.0) {
						imageStackContrast.setVoxel(i, j, k, 0.0);
					}
				}
			}
		}
		return imagePlusContrast;
	}
	
}