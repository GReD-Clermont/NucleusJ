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
		ImagePlus  imagePlusCorrected  = imagePlusInput;
		ImageStack imageStackCorrected = imagePlusCorrected.getStack();
		double     voxelValue;
		ImageStack imageStackOutput =
				new ImageStack(imageStackCorrected.getWidth(), imageStackCorrected.getHeight());
		for (int k = 1; k <= imageStackCorrected.getSize(); ++k) {
			ImageProcessor imageProcessorLabeled = imageStackCorrected.getProcessor(k);
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
					voxelValue = imageProcessorLabeled.getPixel(i, j);
					if (voxelValue > 0) {
						imageProcessorLabeled.putPixelValue(i, j, 0);
					} else {
						imageProcessorLabeled.putPixelValue(i, j, 255);
					}
				}
			}
			imageProcessorLabeled = BinaryImages.componentsLabeling(imageProcessorLabeled, 26, 32);
			int       label;
			boolean[] tEdgeFlags = new boolean[(int) imageProcessorLabeled.getMax() + 1];
			// Analysis of extreme plans along x axis
			for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
				label = (int) imageProcessorLabeled.getf(0, j);
				tEdgeFlags[label] = true;
				label = (int) imageProcessorLabeled.getf(imageStackCorrected.getWidth() - 1, j);
				tEdgeFlags[label] = true;
			}
			// Analysis of extreme plans along y axis
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				label = (int) imageProcessorLabeled.getf(i, 0);
				tEdgeFlags[label] = true;
				label = (int) imageProcessorLabeled.getf(i, imageStackCorrected.getHeight() - 1);
				tEdgeFlags[label] = true;
			}
			
			for (int i = 0; i < imageStackCorrected.getWidth(); ++i) {
				for (int j = 0; j < imageStackCorrected.getHeight(); ++j) {
					label = (int) imageProcessorLabeled.getf(i, j);
					if (label == 0 || !tEdgeFlags[label]) {
						imageProcessorLabeled.putPixelValue(i, j, 255);
					} else {
						imageProcessorLabeled.putPixelValue(i, j, 0);
					}
				}
			}
			imageStackOutput.addSlice(imageProcessorLabeled);
		}
		imagePlusCorrected.setStack(imageStackOutput);
		StackConverter stackConverter = new StackConverter(imagePlusCorrected);
		stackConverter.convertToGray8();
		return imagePlusCorrected;
	}
	
}