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

import fr.igred.nucleus.utils.DistanceMap;
import fr.igred.nucleus.utils.Histogram;
import fr.igred.nucleus.utils.VoxelRecord;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.Resizer;


/**
 * this class allows the determination of the radial distance of chromocenters, using the binary nucleus and the image
 * of segmented chromocenters.
 *
 * @author Tristan Dubos and Axel Poulet
 */
public final class RadialDistance {
	
	
	/** Default constructor: private to prevent instantiation */
	private RadialDistance() {
		// Prevent instantiation
	}
	
	
	/**
	 * Method which compute the distance map of binary nucleus Rescale the voxel to obtain cubic voxel
	 *
	 * @param rescaledSegmentedImg
	 *
	 * @return
	 */
	public static ImagePlus computeDistanceMap(ImagePlus rescaledSegmentedImg) {
		DistanceMap.apply(rescaledSegmentedImg);
		return rescaledSegmentedImg;
	}
	
	
	/**
	 * Compute the shortest distance between the chromocenter periphery and the nuclear envelope
	 *
	 * @param imagePlusSegmented
	 * @param imagePlusCC
	 *
	 * @return
	 */
	public static double[] computeBorderToBorderDistances(ImagePlus imagePlusSegmented, ImagePlus imagePlusCC) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusCC);
		double[]    tLabel       = histogram.getLabels();
		Calibration calibration  = imagePlusSegmented.getCalibration();
		double      xCalibration = calibration.pixelWidth;
		imagePlusCC = resizeImage(imagePlusCC);
		ImageStack imageStackCC = imagePlusCC.getStack();
		imagePlusSegmented = resizeImage(imagePlusSegmented);
		ImagePlus  imagePlusDistanceMap = computeDistanceMap(imagePlusSegmented);
		ImageStack imageStackDistMap    = imagePlusDistanceMap.getStack();
		double     voxelValueMin;
		double     voxelValue;
		double[]   tDistanceRadial      = new double[tLabel.length];
		for (int l = 0; l < tLabel.length; ++l) {
			voxelValueMin = Double.MAX_VALUE;
			for (int k = 0; k < imagePlusCC.getNSlices(); ++k) {
				for (int i = 0; i < imagePlusCC.getWidth(); ++i) {
					for (int j = 0; j < imagePlusCC.getHeight(); ++j) {
						voxelValue = imageStackDistMap.getVoxel(i, j, k);
						if (voxelValue < voxelValueMin &&
						    tLabel[l] == imageStackCC.getVoxel(i, j, k)) {
							voxelValueMin = voxelValue;
						}
					}
				}
			}
			tDistanceRadial[l] = voxelValueMin * xCalibration;
		}
		return tDistanceRadial;
	}
	
	
	/**
	 * Determines the radial distance of all chromocenter in the image of nucleus We realise the distance map on the
	 * binary nucleus. This method measure the radial distance between the barycenter of chromocenter and the nuclear
	 * envelope.
	 *
	 * @param imagePlusSegmented
	 * @param imagePlusCC
	 *
	 * @return
	 */
	public static double[] computeBarycenterToBorderDistances(ImagePlus imagePlusSegmented, ImagePlus imagePlusCC) {
		Calibration calibration        = imagePlusSegmented.getCalibration();
		double      xCalibration       = calibration.pixelWidth;
		ImagePlus   imagePlusCCRescale = resizeImage(imagePlusCC);
		imagePlusSegmented = resizeImage(imagePlusSegmented);
		ImagePlus  imagePlusDistanceMap = computeDistanceMap(imagePlusSegmented);
		ImageStack imageStackDistMap    = imagePlusDistanceMap.getStack();
		Measure3D  measure3D            = new Measure3D();
		
		VoxelRecord[] tVoxelRecord    = measure3D.computeObjectBarycenter(imagePlusCCRescale, false);
		double[]      tRadialDistance = new double[tVoxelRecord.length];
		double        distance;
		for (int i = 0; i < tVoxelRecord.length; ++i) {
			VoxelRecord voxelRecord = tVoxelRecord[i];
			distance = imageStackDistMap.getVoxel((int) voxelRecord.getI(),
			                                      (int) voxelRecord.getJ(),
			                                      (int) voxelRecord.getK());
			tRadialDistance[i] = xCalibration * distance;
		}
		return tRadialDistance;
	}
	
	
	/**
	 * Resize the input image to obtain isotropic voxel
	 *
	 * @param imagePlus
	 *
	 * @return resized image
	 */
	private static ImagePlus resizeImage(ImagePlus imagePlus) {
		Resizer     resizer      = new Resizer();
		Calibration calibration  = imagePlus.getCalibration();
		double      xCalibration = calibration.pixelWidth;
		double      zCalibration = calibration.pixelDepth;
		double      zFactor      = zCalibration / xCalibration;
		int         newDepth     = (int) (imagePlus.getNSlices() * zFactor);
		return resizer.zScale(imagePlus, newDepth, 0);
	}
	
}