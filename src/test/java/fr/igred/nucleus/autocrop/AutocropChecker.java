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
package fr.igred.nucleus.autocrop;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class AutocropChecker {
	public static final String PATH_TO_INFO   = "result_Autocrop_Analyse.csv";
	public static final String PATH_TO_TARGET = "target" + File.separator;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final Pattern TAB = Pattern.compile("\\t");
	
	private static final int    VALID_CROP_NUMBER_RANGE = 10;
	private static final double VALID_CROP_PERCENTAGE   = 60;
	
	private final String pathToCoordinates;
	
	private AutocropResult target = new AutocropResult();
	
	
	public AutocropChecker(String targetPath) {
		pathToCoordinates = "coordinates" + File.separator + FilenameUtils.removeExtension(targetPath) + ".txt";
		
		File targetInfoFile = new File(AutoCropTest.PATH_TO_AUTOCROP +
		                               PATH_TO_TARGET +
		                               targetPath + File.separator +
		                               PATH_TO_INFO
		);
		
		File targetCoordFiles = new File(AutoCropTest.PATH_TO_AUTOCROP +
		                                 PATH_TO_TARGET +
		                                 targetPath + File.separator +
		                                 pathToCoordinates
		);
		
		target = extractGeneralInfo(target, targetInfoFile);
		target = extractCoordinates(target, targetCoordFiles);
	}
	
	
	public boolean checkValues(File file) {
		AutocropResult autocropResult = new AutocropResult();
		autocropResult = extractGeneralInfo(autocropResult, getInfoFile(file));
		autocropResult = extractCoordinates(autocropResult, getCoordinatesFile(file));
		
		return checkGeneralValues(autocropResult) && checkCoordinates(autocropResult);
	}
	
	
	private static File getInfoFile(File file) {
		return new File(AutoCropTest.PATH_TO_OUTPUT +
		                file.getName() + File.separator +
		                PATH_TO_INFO);
	}
	
	
	private File getCoordinatesFile(File file) {
		return new File(AutoCropTest.PATH_TO_OUTPUT +
		                file.getName() + File.separator +
		                pathToCoordinates);
	}
	
	
	public static AutocropResult extractGeneralInfo(AutocropResult result, File file) {
		LOGGER.debug("Extracting info from file: {}", file);
		List<String> resultList = new ArrayList<>(0);
		try {
			resultList = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		} catch (IOException ex) {
			LOGGER.error("Could not read file: {}", file, ex);
		}
		String[] resultLine = TAB.split(resultList.get(resultList.size() - 1));
		result.setCropNb(Integer.parseInt(resultLine[1]));
		
		return result;
	}
	
	
	public static AutocropResult extractCoordinates(AutocropResult result, File file) {
		List<String> fileList = new ArrayList<>(0);
		try {
			fileList = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		} catch (IOException ex) {
			LOGGER.error("Could not read file: {}", file, ex);
		}
		
		fileList.removeIf(line -> !line.isEmpty() && line.charAt(0) == '#');
		fileList.removeIf(line -> line.startsWith("FileName"));
		
		List<CropResult> coordinates = new ArrayList<>(fileList.size());
		for (String line : fileList) {
			String[] resultLine = TAB.split(line);
			coordinates.add(new CropResult(Integer.parseInt(resultLine[2]),
			                               Integer.parseInt(resultLine[1]),
			                               Integer.parseInt(resultLine[3]),
			                               Integer.parseInt(resultLine[4]),
			                               Integer.parseInt(resultLine[5]),
			                               Integer.parseInt(resultLine[6]),
			                               Integer.parseInt(resultLine[7]),
			                               Integer.parseInt(resultLine[8])));
		}
		result.setCoordinates(coordinates);
		return result;
	}
	
	
	public boolean checkGeneralValues(AutocropResult foundResult) {
		LOGGER.info("Crop(s): (target) {} / {} (found)", target.getCropNb(), foundResult.getCropNb());
		return target.getCropNb() + VALID_CROP_NUMBER_RANGE >= foundResult.getCropNb()
		       && target.getCropNb() - VALID_CROP_NUMBER_RANGE <= foundResult.getCropNb();
	}
	
	
	public boolean checkCoordinates(AutocropResult foundResult) {
		int overlappingCrops = getNbOfOverlappingCrops(foundResult);
		LOGGER.info("Crops found overlapping (at least 80% overlapped) with targeted ones (={}) = {}",
		            target.getCropNb(), overlappingCrops);
		/* To change: valid if 90% of the crops found */
		return overlappingCrops >= target.getCropNb() * VALID_CROP_PERCENTAGE / 100;
	}
	
	
	/**
	 * Checks whether the crops are overlapping with the wanted crops from the target
	 *
	 * @param autocropResult the autocrop result
	 *
	 * @return counts the valid crops found (corresponding to the targeted ones)
	 */
	public int getNbOfOverlappingCrops(AutocropResult autocropResult) {
		int validCrops = 0, cropCounter;
		
		for (CropResult tCrop : target.getCoordinates()) {
			LOGGER.debug("> TARGET: {}", tCrop.getCropNumber());
			
			cropCounter = 0;
			for (CropResult rCrop : autocropResult.getCoordinates()) {
				double percent = boxesPercentOverlapping(tCrop.getBox(), rCrop.getBox());
				LOGGER.debug("\t> FOUND: {} / Overlapping: {}", rCrop.getCropNumber(), percent);
				if (percent >= 80) {
					cropCounter++; // If more than one there's probably some bad crops
				}
			}
			if (cropCounter == 1) {
				validCrops++;
			}
		}
		return validCrops;
	}
	
	
	/**
	 * Calculate the intersection area between two 3D boxes which are aligned/non-rotated and return the percentage of
	 * the area of the crop whose the most overlapped
	 *
	 * @param a 3D box coordinates
	 * @param b 3D box coordinates
	 *
	 * @return the percent of area overlapping according the sum of the volumes of each box
	 */
	private static double boxesPercentOverlapping(Box a, Box b) {
		int aLeft  = a.getXMin(), aRight = a.getXMax();
		int aTop   = a.getYMin(), aBottom = a.getYMax();
		int aFront = a.getZMin(), aBack = a.getZMax();
		
		int bLeft  = b.getXMin(), bRight = b.getXMax();
		int bTop   = b.getYMin(), bBottom = b.getYMax();
		int bFront = b.getZMin(), bBack = b.getZMax();
		
		int    xOverlap    = Math.max(0, Math.min(aRight, bRight) - Math.max(aLeft, bLeft));
		int    yOverlap    = Math.max(0, Math.min(aBottom, bBottom) - Math.max(aTop, bTop));
		int    zOverlap    = Math.max(0, Math.min(aBack, bBack) - Math.max(aFront, bFront));
		double overlapArea = xOverlap * yOverlap * zOverlap;
		
		double aVol = (a.getXMax() - a.getXMin()) * (a.getYMax() - a.getYMin()) * (a.getZMax() - a.getZMin());
		double bVol = (b.getXMax() - b.getXMin()) * (b.getYMax() - b.getYMin()) * (b.getZMax() - b.getZMin());
		
		double aOverlappedPercent = 100 * overlapArea / aVol;
		double bOverlappedPercent = 100 * overlapArea / bVol;
		
		return Math.min(aOverlappedPercent, bOverlappedPercent);
	}
	
}