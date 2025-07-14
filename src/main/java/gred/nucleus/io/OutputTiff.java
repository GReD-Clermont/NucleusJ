package gred.nucleus.io;

import ij.ImagePlus;
import ij.io.FileSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;


public class OutputTiff extends FilesNames {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/** Constructor to create file to output */
	public OutputTiff(String filePath) {
		super(filePath);
	}
	
	
	/**
	 * Method to save file with verification if file already exists
	 * <p> TODO ADD ERROR IN LOG FILE
	 */
	public void saveImage(ImagePlus imageToSave) {
		LOGGER.debug("Saving image: {}", fullPathFile);
		try {
			if (fileExists()) {
				File old = new File(fullPathFile);
				if (old.delete()) {
					LOGGER.debug("Deleted old {}", fullPathFile);
				}
				if (imageToSave.getNSlices() > 1) {
					FileSaver fileSaver = new FileSaver(imageToSave);
					fileSaver.saveAsTiffStack(fullPathFile);
				} else {
					FileSaver fileSaver = new FileSaver(imageToSave);
					fileSaver.saveAsTiff(fullPathFile);
				}
			} else {
				if (imageToSave.getNSlices() > 1) {
					FileSaver fileSaver = new FileSaver(imageToSave);
					fileSaver.saveAsTiffStack(fullPathFile);
				} else {
					FileSaver fileSaver = new FileSaver(imageToSave);
					fileSaver.saveAsTiff(fullPathFile);
				}
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
}



