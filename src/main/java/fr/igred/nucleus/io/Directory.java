package fr.igred.nucleus.io;

import ij.IJ;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;


/** Class get to list directory and sub directory. */
public class Directory {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** List of nd files */
	private final List<File> fileListND = new ArrayList<>();
	
	/** Directory path */
	private String     dirPath  = "";
	/** List of files in current folder + recursive folder */
	private List<File> fileList = new ArrayList<>();
	/** Check if directory contain nd files */
	private boolean    containNdFile;
	/** Path separator */
	private String     separator;
	
	
	/**
	 * Constructor
	 *
	 * @param path of directory
	 */
	public Directory(String path) {
		try {
			this.dirPath = path;
			this.separator = File.separator;
		} catch (Exception exp) {
			LOGGER.error("Could not create Directory object.", exp);
			System.exit(1);
		}
	}
	
	
	public List<File> getFileList() {
		return new ArrayList<>(fileList);
	}
	
	
	/** Method to check if directory and create if doesn't */
	public void checkAndCreateDir() {
		checkSeparatorEndPath();
		createDir();
	}
	
	
	/** Check if separator exist */
	private void checkSeparatorEndPath() {
		if (!dirPath.endsWith(File.separator)) {
			this.dirPath += File.separator;
		}
	}
	
	
	/** Method creating folder if doesn't exist. */
	private void createDir() {
		File directory = new File(dirPath);
		if (!directory.exists()) {
			boolean isDirCreated = directory.mkdirs();
			if (isDirCreated) {
				LOGGER.info("New directory: {}", dirPath);
			} else {
				LOGGER.error("{}: directory cannot be created", dirPath);
				System.exit(-1);
			}
		}
	}
	
	
	/** @return path current directory */
	public String getDirPath() {
		return dirPath;
	}
	
	
	/**
	 * Method to recursively list files contains in folder and sub folder. (Argument needed because of recursive way)
	 *
	 * @param path path of folder
	 */
	public void listImageFiles(String path) {
		File   root = new File(path);
		File[] list = root.listFiles();
		if (list == null) {
			IJ.error(path + " does not contain files");
			System.exit(-1);
		}
		for (File f : list) {
			if (f.isDirectory()) {
				listImageFiles(f.getAbsolutePath());
			} else {
				if (!"txt".equals(FilenameUtils.getExtension(f.getName()))) {
					fileList.add(f);
					if ("nd".equals(FilenameUtils.getExtension(f.getName()))) {
						this.containNdFile = true;
						fileListND.add(f);
					}
				}
			}
		}
	}
	
	
	public void listAllFiles(String path) {
		File   root = new File(path);
		File[] list = root.listFiles();
		
		if (list != null) {
			for (File f : list) {
				fileList.add(f);
				if (f.isDirectory()) {
					listAllFiles(f.getAbsolutePath());
				}
			}
		}
	}
	
	
	/** Replace list files if ND files have been listed. */
	public void checkAndActualiseNDFiles() {
		if (containNdFile) {
			this.fileList = fileListND;
		}
	}
	
	
	/** check if input directory is empty */
	public void checkIfEmpty() {
		if (fileList.isEmpty()) {
			LOGGER.debug("Folder {} is empty", dirPath);
		}
	}
	
	
	/** @return list of files */
	public List<File> listFiles() {
		return new ArrayList<>(fileList);
	}
	
	
	/**
	 * @param index of file in list array
	 *
	 * @return File
	 */
	public File getFile(int index) {
		return fileList.get(index);
	}
	
	
	/** @return number of file listed */
	public int getNumberFiles() {
		return fileList.size();
	}
	
	
	public String getSeparator() {
		return separator;
	}
	
	
	/**
	 * Searches a file in a list file without extension. Used to compare 2 lists of files
	 */
	public File searchFileNameWithoutExtension(String fileName) {
		File fileToReturn = null;
		
		for (File f : fileList) {
			if (f.getName().substring(0, f.getName().lastIndexOf('.')).equals(fileName)) {
				fileToReturn = f;
			}
		}
		return fileToReturn;
	}
	
	
	public boolean checkIfFileExists(String fileName) {
		boolean fileExists = false;
		
		for (File f : fileList) {
			if (f.getName().substring(0, f.getName().lastIndexOf('.')).equals(fileName)
			    || f.getName().equals(fileName)) {
				fileExists = true;
			}
		}
		return fileExists;
	}
	
}
