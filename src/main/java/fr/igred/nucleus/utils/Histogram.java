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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;


/**
 * This class permit to obtain values who are on the Input image (8, 16 or 32 bits)
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class Histogram {
	/** HashMap which stock the different values of voxels and the number of voxels for each value present on the image */
	private final Map<Double, Integer> hHistogram = new TreeMap<>();
	
	/** All the value present on the image */
	private double[] label;
	
	/**  */
	private double labelMax = -1;
	
	/**  */
	private int nbLabel;
	
	
	/** @param imagePlusInput  */
	public void run(ImagePlus imagePlusInput) {
		Object[] tTemp = computeHistogram(imagePlusInput).keySet().toArray();
		label = new double[tTemp.length];
		for (int i = 0; i < tTemp.length; ++i) {
			label[i] = Double.parseDouble(tTemp[i].toString());
		}
		Arrays.sort(label);
		if (nbLabel > 0) {
			labelMax = label[label.length - 1];
		}
	}
	
	
	/**
	 * this method return a Histogram of the image input in hashMap form
	 *
	 * @param imagePlusInput
	 *
	 * @return
	 */
	private Map<Double, Integer> computeHistogram(ImagePlus imagePlusInput) {
		double     voxelValue;
		ImageStack imageStackInput = imagePlusInput.getImageStack();
		for (int k = 0; k < imagePlusInput.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue > 0) {
						if (hHistogram.containsKey(voxelValue)) {
							int nbVoxel = hHistogram.get(voxelValue);
							++nbVoxel;
							hHistogram.put(voxelValue, nbVoxel);
						} else {
							hHistogram.put(voxelValue, 1);
							++nbLabel;
						}
					}
				}
			}
		}
		return hHistogram;
	}
	
	
	/**
	 * this method return a double table which contain the all the value voxel present on the input image
	 *
	 * @return
	 */
	public double[] getLabels() {
		return label;
	}
	
	
	/** @return  */
	public Map<Double, Integer> getHistogram() {
		return new TreeMap<>(hHistogram);
	}
	
	
	/** @return  */
	public double getLabelMax() {
		return labelMax;
	}
	
	
	/** @return  */
	public int getNbLabels() {
		return nbLabel;
	}
	
}
