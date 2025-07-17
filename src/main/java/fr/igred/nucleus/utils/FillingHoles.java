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
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import inra.ijpb.binary.BinaryImages;


/**
 * Class HolesFilling
 *
 * @author Philippe Andrey, Tristan Dubos Axel poulet
 */
public final class FillingHoles {
	
	/** Private constructor to prevent instantiation */
	private FillingHoles() {
		// This class should not be instantiated
	}
	
	
	/** Method in two dimensions which process each plan z independent, */
	public static ImagePlus apply2D(ImagePlus imagePlusInput) {
		ImageStack imageStackCorrected = imagePlusInput.getStack();
		double     voxelValue;
		ImageStack imageStackOutput = new ImageStack(imageStackCorrected.getWidth(), imageStackCorrected.getHeight());
		for (int k = 1; k <= imageStackCorrected.getSize(); ++k) {
			ImageProcessor labelProcessor = imageStackCorrected.getProcessor(k);
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
					voxelValue = labelProcessor.getPixel(i, j);
					if (voxelValue > 0) {
						labelProcessor.putPixelValue(i, j, 0);
					} else {
						labelProcessor.putPixelValue(i, j, 255);
					}
				}
			}
			labelProcessor = BinaryImages.componentsLabeling(labelProcessor, 26, 32);
			int       label;
			boolean[] tEdgeFlags = new boolean[(int) labelProcessor.getMax() + 1];
			// Analysis of extreme plans along x axis
			for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
				label = (int) labelProcessor.getf(0, j);
				tEdgeFlags[label] = true;
				label = (int) labelProcessor.getf(imageStackCorrected.getWidth() - 1, j);
				tEdgeFlags[label] = true;
			}
			// Analysis of extreme plans along y axis
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				label = (int) labelProcessor.getf(i, 0);
				tEdgeFlags[label] = true;
				label = (int) labelProcessor.getf(i, imageStackCorrected.getHeight() - 1);
				tEdgeFlags[label] = true;
			}
			
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
					label = (int) labelProcessor.getf(i, j);
					if (label == 0 || !tEdgeFlags[label]) {
						labelProcessor.putPixelValue(i, j, 255);
					} else {
						labelProcessor.putPixelValue(i, j, 0);
					}
				}
			}
			imageStackOutput.addSlice(labelProcessor);
		}
		imagePlusInput.setStack(imageStackOutput);
		StackConverter stackConverter = new StackConverter(imagePlusInput);
		stackConverter.convertToGray8();
		return imagePlusInput;
	}
	
}