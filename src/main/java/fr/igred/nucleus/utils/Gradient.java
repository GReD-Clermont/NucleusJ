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
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.List;


/** @author Tristan Dubos and Axel Poulet */
public class Gradient {
	/**  */
	private final List<Double>[][][] tableGradient;
	/**  */
	private final List<Double>[][][] tableUnitNormals;
	
	
	@SuppressWarnings("unchecked")
	public Gradient(ImagePlus imagePlusInput) {
		int w = imagePlusInput.getWidth();
		int h = imagePlusInput.getHeight();
		int d = imagePlusInput.getStackSize();
		tableGradient = new ArrayList[w][h][d];
		tableUnitNormals = new ArrayList[w][h][d];
		computeGradient(imagePlusInput);
	}
	
	
	/**
	 * @param imagePlusInput
	 *
	 * @return
	 */
	private void computeGradient(ImagePlus imagePlusInput) {
		Calibration calibration     = imagePlusInput.getCalibration();
		ImageStack  imageStackInput = imagePlusInput.getStack();
		double      xCalibration    = calibration.pixelWidth;
		double      yCalibration    = calibration.pixelHeight;
		double      zCalibration    = calibration.pixelDepth;
		for (int k = 1; k < imagePlusInput.getStackSize() - 1; ++k) {
			for (int i = 1; i < imagePlusInput.getWidth() - 1; ++i) {
				for (int j = 1; j < imagePlusInput.getHeight() - 1; ++j) {
					List<Double> list = new ArrayList<>();
					double       dx   = 0;
					double       dy   = 0;
					double       dz   = 0;
					if (k - 1 > 1 || j - 1 > 1 || i - 1 > 1 || k + 1 < imagePlusInput.getStackSize() - 1 ||
					    j + 1 < imagePlusInput.getHeight() - 1 || i + 1 < imagePlusInput.getWidth() - 1) {
						dx = 1 / xCalibration *
						     ((imageStackInput.getVoxel(i + 1, j, k) - imageStackInput.getVoxel(i - 1, j, k)) / 2);
						dy = 1 / yCalibration *
						     ((imageStackInput.getVoxel(i, j + 1, k) - imageStackInput.getVoxel(i, j - 1, k)) / 2);
						dz = 1 / zCalibration *
						     ((imageStackInput.getVoxel(i, j, k + 1) - imageStackInput.getVoxel(i, j, k - 1)) / 2);
					}
					list.add(dx);
					list.add(dy);
					list.add(dz);
					tableGradient[i][j][k] = list;
					/* temp variable */
					double norm = StrictMath.sqrt(dx * dx + dy * dy + dz * dz);
					double nx   = 0;
					double ny   = 0;
					double nz   = 0;
					if (norm > 1.0e-15) {
						nx = dx / norm;
						ny = dy / norm;
						nz = dz / norm;
					}
					List<Double> listN = new ArrayList<>(3);
					listN.add(nx);
					listN.add(ny);
					listN.add(nz);
					tableUnitNormals[i][j][k] = listN;
				}
			}
		}
		// IJ.log("fin Gradient");
	}
	
	
	/** @return  */
	public List<Double>[][][] getUnitNormals() {
		return tableUnitNormals;
	}
	
	
	/** @return  */
	public List<Double>[][][] getGradient() {
		return tableGradient;
	}
	
}