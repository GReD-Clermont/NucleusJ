package gred.nucleus.utils;

import ij.ImagePlus;
import ij.io.FileSaver;


public final class Rd2ToTif {
	
	
	/** Default constructor: private to prevent instantiation */
	private Rd2ToTif() {
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
