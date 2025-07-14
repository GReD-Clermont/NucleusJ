package gred.nucleus.io;

import ij.ImagePlus;
import ij.io.FileSaver;


public final class ImageSaver {
	
	
	/** Default constructor: private to prevent instantiation */
	private ImageSaver() {
		// Prevent instantiation
	}
	
	
	/**
	 * Save the image file
	 *
	 * @param imagePlusInput image to save
	 * @param pathFile       path to save the image
	 */
	public static void saveFile(ImagePlus imagePlusInput, String pathFile) {
		FileSaver fileSaver = new FileSaver(imagePlusInput);
		fileSaver.saveAsTiff(pathFile);
	}
	
}
