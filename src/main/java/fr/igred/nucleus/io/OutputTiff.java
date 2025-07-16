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
package fr.igred.nucleus.io;

import ij.ImagePlus;
import ij.io.FileSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;


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
				if (Files.deleteIfExists(old.toPath())) {
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
		} catch (IOException e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
}



