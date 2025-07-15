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
