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
package fr.igred.nucleus.segmentation;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.process.StackStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public final class SegmentationChecker {
	public static final String PATH_TO_INFO   = "OTSU" + File.separator + "result_Segmentation_Analyse_OTSU.csv";
	public static final String PATH_TO_TARGET = "target" + File.separator;
	public static final String PATH_TO_RESULT = "OTSU" + File.separator;
	
	public static final int PERCENT_MASK_OVERLAPPED = 5;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private SegmentationResult target;
	
	
	public SegmentationChecker(String segmentationPath, String targetPath) {
		File targetFile = new File(segmentationPath +
		                           PATH_TO_TARGET +
		                           targetPath + File.separator +
		                           PATH_TO_INFO);
		String resultPath = segmentationPath +
		                    PATH_TO_TARGET +
		                    targetPath + File.separator +
		                    PATH_TO_RESULT + targetPath;
		
		target = extractGeneralInfo(new SegmentationResult(), targetFile);
		target = extractResult(target, resultPath);
	}
	
	
	public static File getInfoFile(File file, String outputPath) {
		return new File(outputPath + file.getName() + File.separator + PATH_TO_INFO);
	}
	
	
	public static SegmentationResult extractGeneralInfo(SegmentationResult result, File file) {
		List<String> list = new ArrayList<>(0);
		try {
			list = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		} catch (IOException ex) {
			LOGGER.error("Error reading file: {}", file.getAbsolutePath(), ex);
		}
		
		//String[] resultLine = list.get(list.size() - 1).split("\t");
		//result.setOtsuThreshold(Long.parseLong(resultLine[16]));
		
		return result;
	}
	
	
	public static String getResultPath(File file, String outputPath) {
		return outputPath + file.getName() + File.separator + PATH_TO_RESULT + file.getName();
	}
	
	
	public static SegmentationResult extractResult(SegmentationResult result, String path) {
		result.setImage(new ImagePlus(path));
		return result;
	}
	
	
	public void checkGeneralValues(SegmentationResult foundResult) {
		// No values to verify currently
	}
	
	
	public boolean checkResult(SegmentationResult result) {
		ImagePlus imgDiff = new ImageCalculator().run("difference create stack",
		                                              target.getImage(),
		                                              result.getImage());
		
		StackStatistics statsTarget      = new StackStatistics(target.getImage());
		long[]          histogramTarget  = statsTarget.getHistogram();
		long            targetMaskPixels = histogramTarget[histogramTarget.length - 1];
		
		StackStatistics statsDiff     = new StackStatistics(imgDiff);
		long[]          histogramDiff = statsDiff.getHistogram();
		long            diffPixels    = histogramDiff[histogramDiff.length - 1];
		
		LOGGER.info("Mask: Target = {} ({}% = {})",
		            targetMaskPixels,
		            PERCENT_MASK_OVERLAPPED,
		            targetMaskPixels * PERCENT_MASK_OVERLAPPED / 100);
		LOGGER.info("Mask difference found = {}", diffPixels);
		return targetMaskPixels * PERCENT_MASK_OVERLAPPED / 100 > diffPixels;
	}
	
	
	public boolean checkValues(File file, String outputPath) {
		File               infoFile           = getInfoFile(file, outputPath);
		SegmentationResult segmentationResult = extractGeneralInfo(new SegmentationResult(), infoFile);
		segmentationResult = extractResult(segmentationResult, getResultPath(file, outputPath));
		
		checkGeneralValues(segmentationResult);
		return checkResult(segmentationResult);
	}
	
}
