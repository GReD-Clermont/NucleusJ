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
package fr.igred.nucleus.imageprocessing;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.AutoThresholder;
import ij.process.ImageConverter;
import ij.process.ImageStatistics;
import ij.process.StackConverter;
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
	 * <p> TODO STRUCTURES PROBABLY NEEDED
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
	 * TODO COMMENT !!!! 2D 3D
	 *
	 * @param imagePlusInput
	 *
	 * @return
	 */
	public static ImagePlus contrastAnd8bits(ImagePlus imagePlusInput) {
		ContrastEnhancer enh = new ContrastEnhancer();
		enh.setNormalize(true);
		enh.setUseStackHistogram(true);
		enh.setProcessStack(true);
		enh.stretchHistogram(imagePlusInput, 0.05);
		StackStatistics statistics = new StackStatistics(imagePlusInput);
		imagePlusInput.setDisplayRange(statistics.min, statistics.max);
		
		if (imagePlusInput.getNSlices() > 1) { // 3D
			StackConverter stackConverter = new StackConverter(imagePlusInput);
			stackConverter.convertToGray8();
		} else { // 2D
			ImageConverter imageConverter = new ImageConverter(imagePlusInput);
			imageConverter.convertToGray8();
		}
		return imagePlusInput;
	}
	
}

