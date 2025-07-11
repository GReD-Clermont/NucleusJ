package gred.nucleus.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * @author Tristan Dubos and Axel Poulet
 * <p>
 * Several method on the file
 */
public class FileList {
	private final boolean windows;
	
	private static final Pattern SEP = Pattern.compile(Pattern.quote(File.separator));
	
	
	/**
	 *
	 */
	public FileList() {
		windows = System.getProperty("os.name").startsWith("Windows");
	}
	
	
	/**
	 * run the methods to list all the files in one input directory
	 *
	 * @param directory input directory
	 *
	 * @return List of files
	 */
	public File[] run(String directory) {
		return repertoryFileList(directory);
	}
	
	
	/**
	 * method to list all the files in one input directory
	 *
	 * @param directory input directory
	 *
	 * @return list file
	 */
	public static File[] repertoryFileList(String directory) {
		File   directoryToScan = new File(directory);
		File[] tFileDirectory;
		tFileDirectory = directoryToScan.listFiles();
		for (int i = 0; i < Objects.requireNonNull(tFileDirectory).length; ++i) {
			if (tFileDirectory[i].isDirectory()) {
				File[] tTempBeforeElement = stockFileBefore(i, tFileDirectory);
				File[] tTempAfterElement  = stockFileAfter(i, tFileDirectory);
				File[] tTempFile          = repertoryFileList(tFileDirectory[i].toString());
				if (tTempFile.length != 0) {
					tFileDirectory = resize(tTempBeforeElement, tTempAfterElement, tTempFile, i);
				}
			}
		}
		return tFileDirectory;
	}
	
	
	/**
	 * methode to list on subdirectory
	 *
	 * @param tTempBeforeElement
	 * @param tTempAfterElement
	 * @param tTempFile
	 * @param indexMax
	 *
	 * @return
	 */
	public static File[] resize(File[] tTempBeforeElement, File[] tTempAfterElement, File[] tTempFile, int indexMax) {
		File[] tFile = new File[tTempBeforeElement.length + tTempFile.length + tTempAfterElement.length - 1];
		//element insertion in the file list
		for (int j = 0; j < tFile.length; ++j) {
			//list file before the directory :
			if (j < indexMax) {
				tFile[j] = tTempBeforeElement[j];
			}
			//listed file in the directory :
			else {
				if (j < indexMax + tTempFile.length) {
					tFile[j] = tTempFile[j - indexMax];
				}
				//listed files after directory :
				else {
					tFile[j] = tTempAfterElement[j - indexMax - tTempFile.length];
				}
			}
		}
		return tFile;
	}
	
	
	/**
	 * @param indexMax
	 * @param tFile
	 *
	 * @return
	 */
	public static File[] stockFileBefore(int indexMax, File[] tFile) {
		File[] tTempBeforeElement = new File[indexMax];
		if (indexMax >= 0) {
			System.arraycopy(tFile, 0, tTempBeforeElement, 0, indexMax);
		}
		return tTempBeforeElement;
	}
	
	
	/**
	 * @param indexMax
	 * @param tFile
	 *
	 * @return
	 */
	public static File[] stockFileAfter(int indexMax, File[] tFile) {
		File[] tTempAfterElement = new File[tFile.length - indexMax];
		int    j                 = 0;
		for (int k = indexMax + 1; k < tFile.length; ++k) {
			tTempAfterElement[j] = tFile[k];
			++j;
		}
		return tTempAfterElement;
	}
	
	
	/**
	 * @param regex
	 * @param tFile
	 *
	 * @return
	 */
	public boolean isDirectoryOrFileExist(String regex, File[] tFile) {
		if (windows) {
			String as  = "\\";
			String das = "\\\\";
			regex = regex.replace(as, das);
		}
		boolean testFile = false;
		for (File file : tFile) {
			if (file.toString().matches(regex)) {
				testFile = true;
				break;
			}
		}
		return testFile;
	}
	
	
	/**
	 * @param regex
	 * @param tFile
	 *
	 * @return
	 */
	public List<String> fileSearchList(String regex, File[] tFile) {
		String s = regex;
		if (windows) {
			String as  = "\\";
			String das = "\\\\";
			s = s.replace(as, das);
		}
		List<String> arrayListFile = new ArrayList<>();
		for (File file : tFile) {
			if (file.toString().matches(s)) {
				arrayListFile.add(file.toString());
			}
		}
		return arrayListFile;
	}
	
}