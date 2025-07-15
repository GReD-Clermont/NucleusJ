package gred.nucleus.utils;

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