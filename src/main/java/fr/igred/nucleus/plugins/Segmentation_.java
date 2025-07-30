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
package fr.igred.nucleus.plugins;

import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.nucleus.dialogs.IDialogListener;
import fr.igred.nucleus.dialogs.SegmentationConfigDialog;
import fr.igred.nucleus.dialogs.SegmentationDialog;
import fr.igred.nucleus.segmentation.SegmentationCalling;
import fr.igred.nucleus.segmentation.SegmentationParameters;
import ij.IJ;
import ij.plugin.PlugIn;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Segmentation_ implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private SegmentationDialog segmentationDialog;
	
	
	/**
	 * Run method for imageJ plugin for the segmentation
	 *
	 * @param arg use by imageJ
	 */
	/* This method is used by plugins.config */
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		segmentationDialog = new SegmentationDialog(this);
	}
	
	
	@Override
	public void onStart() {
		if (segmentationDialog.isOMEROUsed()) {
			runOmeroSegmentation();
		} else {
			runLocalSegmentation();
		}
	}
	
	
	public static Client checkOMEROConnection(String hostname,
	                                          String port,
	                                          String username,
	                                          char[] password,
	                                          String group) {
		Client client = new Client();
		try {
			client.connect(hostname,
			               Integer.parseInt(port),
			               username,
			               password,
			               Long.valueOf(group));
		} catch (ServiceException | NumberFormatException exp) {
			LOGGER.error("Unable to connect to OMERO server", exp);
		}
		return client;
	}
	
	
	private SegmentationParameters setParametersFromDialog(String input, String output) {
		SegmentationParameters params = null;
		// Check config
		String config = segmentationDialog.getConfig();
		switch (segmentationDialog.getConfigMode()) {
			case FILE:
				if (config == null || config.isEmpty()) {
					IJ.error("Config file is missing");
				} else {
					LOGGER.info("Config file");
					params = new SegmentationParameters(input, output, config);
				}
				break;
			case INPUT:
				SegmentationConfigDialog scd = segmentationDialog.getSegmentationConfigFileDialog();
				if (scd.isCalibrationSelected()) {
					LOGGER.info("with calibration\tx: {}\ty: {}\tz: {}",
					            scd.getXCalibration(), scd.getYCalibration(), scd.getZCalibration());
					
					params = new SegmentationParameters(input, output,
					                                  Integer.parseInt(scd.getXCalibration()),
					                                  Integer.parseInt(scd.getYCalibration()),
					                                  Integer.parseInt(scd.getZCalibration()),
					                                  Integer.parseInt(scd.getMinVolume()),
					                                  Integer.parseInt(scd.getMaxVolume()),
					                                  scd.getConvexHullDetection()
					);
				} else {
					LOGGER.info("without calibration");
					params = new SegmentationParameters(input, output,
					                                  Integer.parseInt(scd.getMinVolume()),
					                                  Integer.parseInt(scd.getMaxVolume()),
					                                  scd.getConvexHullDetection()
					);
				}
				break;
			case DEFAULT:
			default:
				LOGGER.info("without config");
				params = new SegmentationParameters(input, output);
		}
		return params;
	}
	
	
	private void runOmeroSegmentation() {
		// Check connection
		String hostname = segmentationDialog.getHostname();
		String port     = segmentationDialog.getPort();
		String username = segmentationDialog.getUsername();
		char[] password = segmentationDialog.getPassword();
		String group    = segmentationDialog.getGroup();
		Client client   = checkOMEROConnection(hostname, port, username, password, group);
		String input    = ".";
		String output   = ".";
		
		SegmentationParameters params       = setParametersFromDialog(input, output);
		SegmentationCalling    segmentation = new SegmentationCalling(params);
		segmentation.setExecutorThreads(segmentationDialog.getThreads());
		
		// Handle the source according to the type given
		String dataType = segmentationDialog.getDataType();
		Long   inputID  = Long.valueOf(segmentationDialog.getSourceID());
		Long   outputID = Long.valueOf(segmentationDialog.getOutputProject());
		try {
			String log;
			if ("Image".equals(dataType)) {
				ImageWrapper image = client.getImage(inputID);
				
				log = segmentation.runOneImageOMERO(image, outputID, client);
				segmentation.saveCropGeneralInfoOmero(client, outputID);
			} else {
				List<ImageWrapper> images = null;
				
				switch (segmentationDialog.getDataType()) {
					case "Dataset":
						DatasetWrapper dataset = client.getDataset(inputID);
						images = dataset.getImages(client);
						break;
					case "Project":
						ProjectWrapper project = client.getProject(inputID);
						images = project.getImages(client);
						break;
					case "Tag":
						TagAnnotationWrapper tag = client.getTag(inputID);
						images = tag.getImages(client);
						break;
					default:
						LOGGER.error("Unknown data type: {}", dataType);
				}
				log = segmentation.runSeveralImagesOMERO(images, outputID, client, inputID);
			}
			if (!log.isEmpty()) {
				LOGGER.error("Nuclei which didn't pass the segmentation:{}{}", System.lineSeparator(), log);
			}
			LOGGER.info("Segmentation process has ended successfully");
			IJ.showMessage("Segmentation process ended successfully on " +
			               segmentationDialog.getDataType() + "\\" + inputID);
		} catch (ServiceException se) {
			IJ.error("Unable to access to OMERO service");
		} catch (AccessException ae) {
			IJ.error("Cannot access " + dataType + "with ID = " + inputID + ".");
		} catch (OMEROServerError | IOException | ExecutionException e) {
			LOGGER.error("An error occurred.", e);
		} catch (InterruptedException e) {
			LOGGER.error("Segmentation interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	public void runLocalSegmentation() {
		String input  = segmentationDialog.getInput();
		String output = segmentationDialog.getOutput();
		if (input == null || input.isEmpty()) {
			IJ.error("Input file or directory is missing");
		} else if (output == null || output.isEmpty()) {
			IJ.error("Output directory is missing");
		} else {
			LOGGER.info("Begin Segmentation process");
			SegmentationParameters params = setParametersFromDialog(input, output);
			try {
				SegmentationCalling otsuModified = new SegmentationCalling(params);
				otsuModified.setExecutorThreads(segmentationDialog.getThreads());
				
				File   file = new File(input);
				String log  = "";
				if (file.isDirectory()) {
					log = otsuModified.runSeveralImages();
				} else if (file.isFile()) {
					log = otsuModified.runOneImage(input);
					otsuModified.saveCropGeneralInfo();
				}
				if (!log.isEmpty()) {
					LOGGER.error("Nuclei which didn't pass the segmentation:{}{}", System.lineSeparator(), log);
				}
				LOGGER.info("Segmentation process has ended successfully");
				IJ.showMessage("Segmentation process ended successfully on " + file.getName());
			} catch (IOException ioe) {
				IJ.error("File or directory does not exist");
			} catch (NumberFormatException | FormatException e) {
				LOGGER.error("An error occurred.", e);
			}
		}
	}
	
}
