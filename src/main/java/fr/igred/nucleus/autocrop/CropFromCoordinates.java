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

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.FilesNames;
import loci.formats.FormatException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static fr.igred.nucleus.io.ImageSaver.saveFile;


public class CropFromCoordinates {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final Pattern SEP = Pattern.compile(Pattern.quote(File.separator));
	private static final Pattern TAB = Pattern.compile("\\t");
	
	private static final Pattern HEADERS = Pattern.compile("^#.*");
	private static final Pattern COLNAME = Pattern.compile("^FileName.*");
	
	private final String pathToCoordinates;
	private final String pathToRaw;
	private final String pathToOutput;
	
	private int channelToCrop = -1;
	
	
	/**
	 * Constructor for class used to crop nuclei from given coordinates
	 *
	 * @param pathToCoordinates   path to directory containing coordinates for one file
	 * @param pathToRawAndChannel path to directory containing raw images
	 * @param pathToOutput        path where cropped nuclei will be saved
	 */
	public CropFromCoordinates(String pathToCoordinates, String pathToRawAndChannel, String pathToOutput) {
		String[] splitRawInfo  = SEP.split(pathToRawAndChannel);
		String   channelNumber = splitRawInfo[splitRawInfo.length - 1];
		this.channelToCrop = Integer.parseInt(channelNumber);
		this.pathToRaw = pathToRawAndChannel.replace(File.separator + channelNumber, "");
		this.pathToCoordinates = pathToCoordinates;
		this.pathToOutput = pathToOutput;
	}
	
	
	public CropFromCoordinates(String pathToCoordinates) {
		this.pathToCoordinates = pathToCoordinates;
		this.pathToRaw = "." + File.separator + "tmp-raw_cropFromCoordinate" + File.separator;
		this.pathToOutput = "." + File.separator + "tmp-cropped_nuclei_cropFromCoordinate" + File.separator;
	}
	
	
	private Map<File, File> gatherFilePairs() {
		File coordinateDir = new File(pathToCoordinates);
		File rawDir        = new File(pathToRaw);
		
		File[] coordFiles = coordinateDir.listFiles();
		File[] rawFiles   = rawDir.listFiles();
		
		List<File>      coords          = coordFiles != null ? Arrays.asList(coordFiles) : new ArrayList<>(0);
		List<File>      raws            = rawFiles != null ? Arrays.asList(rawFiles) : new ArrayList<>(0);
		Map<File, File> coordinateToRaw = new HashMap<>(raws.size());
		// Gather all pair of files
		for (File fc : coords) {
			String coordName = FilenameUtils.removeExtension(fc.getName());
			for (File fr : raws) {
				String rawName = FilenameUtils.removeExtension(fr.getName());
				if (rawName.equals(coordName)) {
					coordinateToRaw.put(fc, fr);
				}
			}
		}
		return coordinateToRaw;
	}
	
	
	public void run() throws IOException, FormatException {
		Directory output = new Directory(pathToOutput);
		output.checkAndCreateDir();
		
		Map<File, File> allCoordinateToRaw = gatherFilePairs();
		
		for (Map.Entry<File, File> e : allCoordinateToRaw.entrySet()) {
			File coordinateFile = e.getKey();
			File rawImage       = e.getValue();
			
			AutocropParameters autocropParameters = new AutocropParameters(pathToRaw, pathToOutput);
			Map<Double, Box>   boxes              = readCoordinatesTXT(coordinateFile);
			FilesNames         outPutFilesNames   = new FilesNames(e.getValue().getName());
			String             prefix             = outPutFilesNames.prefixNameFile();
			AutoCrop           autoCrop           = new AutoCrop(rawImage, prefix, autocropParameters, boxes);
			autoCrop.cropKernels();
		}
	}
	
	
	public void runFromOMERO(String datasetIDAndChannel, String outputProjectID, Client client)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException, FormatException {
		String[] splitRawInfo  = SEP.split(datasetIDAndChannel);
		String   channelNumber = splitRawInfo[splitRawInfo.length - 1];
		this.channelToCrop = Integer.parseInt(channelNumber);
		String rawDatasetID = datasetIDAndChannel.replace(File.separator + channelNumber, "");
		
		DatasetWrapper rawDataset    = client.getDataset(Long.parseLong(rawDatasetID));
		ProjectWrapper outputProject = client.getProject(Long.parseLong(outputProjectID));
		
		Directory rawDirectory = new Directory(pathToRaw);
		rawDirectory.checkAndCreateDir();
		for (ImageWrapper raw : rawDataset.getImages(client)) {
			saveFile(raw.toImagePlus(client), pathToRaw + File.separator + raw.getName());
		}
		
		run();
		
		DatasetWrapper outputDataset;
		
		List<DatasetWrapper> datasets = outputProject.getDatasets("raw_C" + channelToCrop + "_" + rawDataset.getName());
		if (datasets.isEmpty()) {
			outputDataset = outputProject.addDataset(client, "raw_C" + channelToCrop + "_" + rawDataset.getName(), "");
			outputProject.reload(client);
		} else {
			outputDataset = datasets.get(0);
		}
		
		File   crops     = new File(pathToOutput);
		File[] cropFiles = crops.listFiles();
		
		Iterable<File> cropList = cropFiles != null ? Arrays.asList(cropFiles) : new ArrayList<>(0);
		for (File crop : cropList) {
			outputDataset.importImages(client, crop.getPath());
		}
		
		FileUtils.deleteDirectory(new File(pathToRaw));
		FileUtils.deleteDirectory(new File(pathToOutput));
	}
	
	
	public static Map<Double, Box> readCoordinatesTXT(File boxesFile) {
		Map<Double, Box> boxLists = new HashMap<>();
		
		try (Scanner scanner = new Scanner(boxesFile)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (!HEADERS.matcher(line).matches() && !COLNAME.matcher(line).matches()) {
					String[] splitLine = TAB.split(line);
					
					short xMax = (short) (Integer.parseInt(splitLine[3]) + Integer.parseInt(splitLine[6]));
					short yMax = (short) (Integer.parseInt(splitLine[4]) + Integer.parseInt(splitLine[7]));
					short zMax = (short) (Integer.parseInt(splitLine[5]) + Integer.parseInt(splitLine[8]));
					
					Box box = new Box(Short.parseShort(splitLine[3]), xMax,
					                  Short.parseShort(splitLine[4]), yMax,
					                  Short.parseShort(splitLine[5]), zMax);
					
					boxLists.put(Double.valueOf(splitLine[2]), box);
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("An error occurred.", e);
		}
		return boxLists;
	}
	
}
