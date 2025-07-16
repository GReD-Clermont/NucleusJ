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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;


public class OutputTextFile extends FilesNames {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public OutputTextFile(String filePath) {
		super(filePath);
	}
	
	
	/**
	 * Method to save file with verification if file already exists
	 * <p> TODO(@DesTristus) ADD ERROR IN LOG FILE
	 */
	public void saveTextFile(String text, boolean keepExistingFile) {
		if (keepExistingFile) {
			int i = 0;
			while (fileExists()) {
				setFullPathFile(prefixNameFile() + "-" + i + "." + FilenameUtils.getExtension(fileName));
				checkFileExists();
				i++;
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFile))) {
			writer.write(text);
		} catch (IOException e) {
			LOGGER.error("{} creation failed", fullPathFile, e);
		}
		LOGGER.info("{} created", fullPathFile);
	}
	
}
