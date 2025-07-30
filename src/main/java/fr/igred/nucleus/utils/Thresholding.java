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
package fr.igred.nucleus.utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.process.AutoThresholder;
import ij.process.ImageConverter;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;


public final class Thresholding {
	
	/** Default constructor: private to prevent instantiation */
	private Thresholding() {
		// Prevent instantiation
	}
	
	
	/**
	 * Compute the initial threshold value from OTSU method
	 *
	 * @param imagePlusInput raw image
	 *
	 * @return OTSU threshold
	 */
	public static int computeOTSUThreshold(ImagePlus imagePlusInput) {
		AutoThresholder autoThresholder = new AutoThresholder();
		ImageStatistics imageStatistics = new StackStatistics(imagePlusInput);
		int[]           tHistogram      = imageStatistics.histogram;
		return autoThresholder.getThreshold(AutoThresholder.Method.Otsu, tHistogram);
	}
	
	
	public static int computeThreshold(ImagePlus imagePlusInput, String typeThresholding) {
		AutoThresholder autoThresholder = new AutoThresholder();
		ImageStatistics imageStatistics = new StackStatistics(imagePlusInput);
		int[]           tHistogram      = imageStatistics.histogram;
		return autoThresholder.getThreshold(AutoThresholder.Method.valueOf(typeThresholding), tHistogram);
	}
	
	
	/**
	 * Enhances the contrast of an image and converts it to 8-bit grayscale.
	 *
	 * @param imagePlusInput the input image to be processed
	 *
	 * @return the processed ImagePlus object with enhanced contrast and converted to 8-bit grayscale
	 */
	public static ImagePlus contrastAnd8bits(ImagePlus imagePlusInput) {
		ContrastEnhancer enh = new ContrastEnhancer();
		enh.setNormalize(true);
		enh.setUseStackHistogram(true);
		enh.setProcessStack(true);
		enh.stretchHistogram(imagePlusInput, 0.05);
		StackStatistics statistics = new StackStatistics(imagePlusInput);
		imagePlusInput.setDisplayRange(statistics.min, statistics.max);
		
		ImageConverter imageConverter = new ImageConverter(imagePlusInput);
		imageConverter.convertToGray8();
		return imagePlusInput;
	}
	
	
	/**
	 * Creates a segmented image (mask) from a given threshold.
	 *
	 * @param input     raw image
	 * @param threshold threshold value for the segmentation
	 *
	 * @return segmented image
	 */
	public static ImagePlus createMask(ImagePlus input, double threshold) {
		ImagePlus mask = input.duplicate();
		binarize(mask, threshold);
		return mask;
	}
	
	
	/**
	 * Converts an image to a mask using a given threshold.
	 *
	 * @param input     raw image
	 * @param threshold threshold value for the segmentation
	 */
	public static void binarize(ImagePlus input, double threshold) {
		convertToMask(input, threshold, Double.MAX_VALUE);
	}
	
	
	/**
	 * Converts an image to a mask using a given threshold.
	 *
	 * @param input          raw image
	 * @param lowerThreshold lower threshold value
	 * @param upperThreshold upper threshold value
	 */
	public static void convertToMask(ImagePlus input, double lowerThreshold, double upperThreshold) {
		ImageStack stack = input.getStack();
		for (int z = 0; z < input.getStackSize(); ++z) {
			for (int x = 0; x < input.getWidth(); ++x) {
				for (int y = 0; y < input.getHeight(); ++y) {
					double voxelValue = stack.getVoxel(x, y, z);
					if (voxelValue >= lowerThreshold && voxelValue <= upperThreshold) {
						stack.setVoxel(x, y, z, 255);
					} else {
						stack.setVoxel(x, y, z, 0);
					}
				}
			}
		}
		ImageConverter converter = new ImageConverter(input);
		converter.convertToGray8(); // Ensure mask is 8-bit
	}
	
}

