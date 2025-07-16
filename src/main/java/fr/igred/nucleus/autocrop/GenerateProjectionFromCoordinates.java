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

import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.FilesNames;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;


public class GenerateProjectionFromCoordinates {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final Pattern TAB = compile("\\t");
	private static final Pattern SEP = compile(Pattern.quote(File.separator));
	
	private static final Pattern COORDS_LINE = compile("^[^\\t]+(\\t\\d+){8}$");
	
	private final String pathToCoordinates;
	
	private String pathToConvexHullSeg;
	private String pathToZProjection;
	private String pathToRaw;
	
	
	/**
	 * Constructor
	 *
	 * @param pathToConvexHullSeg path to segmented image's folder
	 * @param pathToZProjection   path to Zprojection image's from autocrop
	 * @param pathToCoordinates   path to coordinates files from autocrop
	 */
	public GenerateProjectionFromCoordinates(String pathToCoordinates,
	                                         String pathToConvexHullSeg,
	                                         String pathToZProjection) {
		this.pathToConvexHullSeg = pathToConvexHullSeg;
		this.pathToZProjection = pathToZProjection;
		this.pathToCoordinates = pathToCoordinates;
	}
	
	
	/**
	 * Constructor
	 *
	 * @param pathToCoordinates path to segmented image's folder
	 * @param pathToRaw         path to raw image
	 */
	public GenerateProjectionFromCoordinates(String pathToCoordinates, String pathToRaw) {
		this.pathToCoordinates = pathToCoordinates;
		this.pathToRaw = pathToRaw;
	}
	
	
	/**
	 * Compute list of boxes from coordinates file.
	 *
	 * @param boxFile coordinates file
	 *
	 * @return list of boxes file to draw in red
	 */
	public static Map<String, String> readCoordinatesTXT(File boxFile) {
		Map<String, String> boxLists = new HashMap<>();
		try (Scanner scanner = new Scanner(boxFile)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (COORDS_LINE.matcher(line).matches()) {
					String[] splitLine = TAB.split(line);
					String[] fileName  = SEP.split(splitLine[0]);
					String   name      = fileName[fileName.length - 1];
					int      xMax      = Integer.parseInt(splitLine[3]) + Integer.parseInt(splitLine[6]);
					int      yMax      = Integer.parseInt(splitLine[4]) + Integer.parseInt(splitLine[7]);
					int      zMax      = Integer.parseInt(splitLine[5]) + Integer.parseInt(splitLine[8]);
					boxLists.put(name, splitLine[0] + "\t"
					                   + splitLine[3] + "\t"
					                   + xMax + "\t"
					                   + splitLine[4] + "\t"
					                   + yMax + "\t"
					                   + splitLine[5] + "\t"
					                   + zMax);
					LOGGER.debug("Box {} value {}\t{}\t{}\t{}\t{}\t{}\t{}",
					             name,
					             splitLine[0],
					             splitLine[3],
					             xMax,
					             splitLine[4],
					             yMax,
					             splitLine[5],
					             zMax);
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found.", e);
		}
		return boxLists;
	}
	
	
	/**
	 * Run new annotation of Zprojection, color in red nuclei which were filtered (in case of convex hull algorithm
	 * color in red nuclei which doesn't pass the segmentation most of case Z truncated )
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public void generateProjectionFiltered() throws IOException, FormatException {
		Directory convexHullSegImages = new Directory(pathToConvexHullSeg);
		convexHullSegImages.listImageFiles(pathToConvexHullSeg);
		convexHullSegImages.checkIfEmpty();
		Directory zProjection = new Directory(pathToZProjection);
		zProjection.listImageFiles(pathToZProjection);
		zProjection.checkIfEmpty();
		Directory coordinates = new Directory(pathToCoordinates);
		coordinates.listAllFiles(pathToCoordinates);
		coordinates.checkIfEmpty();
		for (short i = 0; i < coordinates.getNumberFiles(); ++i) {
			File                coordinateFile        = coordinates.getFile(i);
			Map<String, String> listOfBoxes           = readCoordinatesTXT(coordinateFile);
			List<String>        boxListsNucleiNotPass = new ArrayList<>(listOfBoxes.size());
			Map<String, String> sortedMap             = new TreeMap<>(listOfBoxes);
			for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
				if (!convexHullSegImages.checkIfFileExists(entry.getKey())) {
					boxListsNucleiNotPass.add(entry.getValue());
					LOGGER.info("add {}", entry.getValue());
				}
			}
			File currentZProjection = zProjection.searchFileNameWithoutExtension(coordinateFile.getName()
			                                                                                   .substring(0,
			                                                                                              coordinateFile
					                                                                                              .getName()
					                                                                                              .lastIndexOf(
							                                                                                              '.')) +
			                                                                     "_Zprojection");
			AutocropParameters autocropParameters = new AutocropParameters(currentZProjection.getParent(),
			                                                               currentZProjection.getParent() +
			                                                               zProjection.getSeparator());
			AnnotateAutoCrop annotateAutoCrop = new AnnotateAutoCrop(boxListsNucleiNotPass,
			                                                         currentZProjection,
			                                                         currentZProjection.getParent() +
			                                                         zProjection.getSeparator() +
			                                                         currentZProjection.getName()
			                                                                           .substring(0,
			                                                                                      currentZProjection.getName()
			                                                                                                        .lastIndexOf(
					                                                                                                        '.')),
			                                                         autocropParameters);
			annotateAutoCrop.runAddBadCrop();
		}
	}
	
	
	public void generateProjection() throws IOException, FormatException {
		Directory rawImage = new Directory(pathToRaw);
		rawImage.listImageFiles(pathToRaw);
		rawImage.checkIfEmpty();
		Directory coordinates = new Directory(pathToCoordinates);
		coordinates.listAllFiles(pathToCoordinates);
		coordinates.checkIfEmpty();
		
		for (short i = 0; i < coordinates.getNumberFiles(); ++i) {
			File                coordinateFile        = coordinates.getFile(i);
			Map<String, String> listOfBoxes           = readCoordinatesTXT(coordinateFile);
			List<String>        boxListsNucleiNotPass = new ArrayList<>(listOfBoxes.size());
			for (Map.Entry<String, String> entry : listOfBoxes.entrySet()) {
				boxListsNucleiNotPass.add(entry.getValue());
			}
			LOGGER.info(coordinateFile.getName());
			
			File currentRaw = rawImage.searchFileNameWithoutExtension(coordinateFile.getName()
			                                                                        .substring(0,
			                                                                                   coordinateFile.getName()
			                                                                                                 .lastIndexOf(
					                                                                                                 '.')));
			FilesNames outPutFilesNames = new FilesNames(currentRaw.toString());
			String     prefix           = outPutFilesNames.prefixNameFile();
			LOGGER.info("current raw: {}", currentRaw.getName());
			AutocropParameters autocropParameters = new AutocropParameters(currentRaw.getParent(),
			                                                               currentRaw.getParent() +
			                                                               rawImage.getSeparator());
			AnnotateAutoCrop annotateAutoCrop = new AnnotateAutoCrop(boxListsNucleiNotPass,
			                                                         currentRaw,
			                                                         currentRaw.getParent() + rawImage.getSeparator(),
			                                                         prefix,
			                                                         autocropParameters);
			annotateAutoCrop.run();
		}
	}
	
}
