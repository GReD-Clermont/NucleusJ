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

import java.io.File;


public class FilesNames {
	
	/** File name */
	protected String  fileName     = "";
	/** Complete pathFile */
	protected String  fullPathFile = "";
	/** Path file input */
	private   String  pathFile     = "";
	protected boolean fileExists;
	
	
	/** Constructor to create file object */
	public FilesNames(String filePath) {
		this.fullPathFile = filePath;
		File file = new File(filePath);
		pathFile = file.getParent() + File.separator;
		fileName = file.getName();
		fileExists = checkFileExists(fullPathFile);
	}
	
	
	public String prefixNameFile() {
		return FilenameUtils.removeExtension(fileName);
	}
	
	
	/**
	 * Method to check if file exists
	 *
	 * @return Whether the given file exists.
	 */
	public static boolean checkFileExists(String fullPathFile) {
		File file = new File(fullPathFile);
		return file.exists();
	}
	
	
	/** @return boolean true for existing file */
	public boolean fileExists() {
		return fileExists;
	}
	
	
	/** @return path to file */
	public String getPathFile() {
		return pathFile;
	}
	
	
	public void setFullPathFile(String fileName) {
		this.fullPathFile = pathFile + fileName;
	}
	
}
