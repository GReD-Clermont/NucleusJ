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

import fr.igred.nucleus.autocrop.AutoCropCalling;
import fr.igred.nucleus.autocrop.AutocropParameters;
import fr.igred.nucleus.gui.AutocropConfigDialog;
import fr.igred.nucleus.gui.OMEROPanel;
import fr.igred.nucleus.gui.IDialogListener;
import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import ij.IJ;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Integer.parseInt;


public class AutocropPlugin implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private OMEROPanel autocropDialog;
	
	
	/**
	 * Run method for imageJ plugin for the autocrop.
	 *
	 * @param arg use by imageJ
	 */
	/* This method is used by plugins.config */
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		autocropDialog = new OMEROPanel(this);
	}
	
	
	@Override
	public void onStart() {
		if (autocropDialog.isOmeroEnabled()) {
			runOmeroAutocrop();
		} else {
			runLocalAutocrop();
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
			               parseInt(port),
			               username,
			               password,
			               Long.valueOf(group));
		} catch (ServiceException | NumberFormatException exp) {
			IJ.error("Invalid connection values");
		}
		
		return client;
	}
	
	
	private void runOmeroAutocrop() {
		// Check connection
		String hostname = autocropDialog.getHostname();
		String port     = autocropDialog.getPort();
		String username = autocropDialog.getUsername();
		char[] password = autocropDialog.getPassword();
		String group    = autocropDialog.getGroup();
		Client client   = checkOMEROConnection(hostname, port, username, password, group);
		
		String typeThresholding = autocropDialog.getTypeThresholding();
		
		AutocropParameters params = null;
		// Check config
		String configFile = autocropDialog.getConfig();
		switch (autocropDialog.getConfigMode()) {
			case DEFAULT:
				params = new AutocropParameters(".", ".");
				break;
			case FILE:
				params = new AutocropParameters(".", ".", configFile);
				break;
			case INPUT:
				AutocropConfigDialog acd = autocropDialog.getAutocropConfigFileDialog();
				if (acd.isCalibrationSelected()) {
					LOGGER.info("with calibration");
					params = new AutocropParameters(".",
					                                ".",
					                                parseInt(acd.getXCalibration()),
					                                parseInt(acd.getYCalibration()),
					                                parseInt(acd.getZCalibration()),
					                                parseInt(acd.getXCropBoxSize()),
					                                parseInt(acd.getYCropBoxSize()),
					                                parseInt(acd.getZCropBoxSize()),
					                                parseInt(acd.getBoxNumberFontSize()),
					                                parseInt(acd.getSlicesOTSUComputing()),
					                                parseInt(acd.getThresholdOTSUComputing()),
					                                parseInt(acd.getChannelToComputeThreshold()),
					                                parseInt(acd.getMinVolume()),
					                                parseInt(acd.getMaxVolume()),
					                                parseInt(acd.getBoxesSurfacePercent()),
					                                acd.isRegroupBoxesSelected()
					);
				} else {
					LOGGER.info("without calibration");
					params = new AutocropParameters(".",
					                                ".",
					                                parseInt(acd.getXCropBoxSize()),
					                                parseInt(acd.getYCropBoxSize()),
					                                parseInt(acd.getZCropBoxSize()),
					                                parseInt(acd.getBoxNumberFontSize()),
					                                parseInt(acd.getSlicesOTSUComputing()),
					                                parseInt(acd.getThresholdOTSUComputing()),
					                                parseInt(acd.getChannelToComputeThreshold()),
					                                parseInt(acd.getMinVolume()),
					                                parseInt(acd.getMaxVolume()),
					                                parseInt(acd.getBoxesSurfacePercent()),
					                                acd.isRegroupBoxesSelected()
					);
				}
				break;
			default:
				LOGGER.error("Unknown config mode: {}", autocropDialog.getConfigMode());
		}
		
		AutoCropCalling autoCrop = new AutoCropCalling(params);
		autoCrop.setTypeThresholding(typeThresholding);
		autoCrop.setExecutorThreads(autocropDialog.getThreads());
		
		// Handle the source according to the type given
		
		String dataType = autocropDialog.getDataType();
		Long   inputID  = Long.valueOf(autocropDialog.getSourceID());
		Long   outputID = Long.valueOf(autocropDialog.getOutputProject());
		try {
			ProjectWrapper project = client.getProject(outputID);
			if ("Image".equals(dataType)) {
				ImageWrapper image      = client.getImage(inputID);
				int          sizeC      = image.getPixels().getSizeC();
				Long[]       outputsDat = new Long[sizeC];
				
				for (int i = 0; i < sizeC; i++) {
					outputsDat[i] = project.addDataset(client, "C" + i + "_" + image.getName(), "").getId();
					project.reload(client);
				}
				
				autoCrop.runImageOMERO(image, outputsDat, client); // Run segmentation
				autoCrop.saveGeneralInfoOmero(client, outputsDat);
			} else {
				List<ImageWrapper> images = null;
				String             name   = "";
				
				if ("Dataset".equals(dataType)) {
					DatasetWrapper dataset = client.getDataset(inputID);
					name = dataset.getName();
					images = dataset.getImages(client);
				} else if ("Tag".equals(dataType)) {
					TagAnnotationWrapper tag = client.getTag(inputID);
					images = tag.getImages(client);
				}
				int    sizeC      = images.get(0).getPixels().getSizeC();
				Long[] outputsDat = new Long[sizeC];
				for (int i = 0; i < sizeC; i++) {
					outputsDat[i] = project.addDataset(client, "raw_C" + i + "_" + name, "").getId();
					project.reload(client);
				}
				autoCrop.runSeveralImageOMERO(images, outputsDat, client); // Run segmentation
			}
			LOGGER.info("Autocrop process has ended successfully");
			IJ.showMessage("Autocrop process ended successfully on " + autocropDialog.getDataType() + "\\" + inputID);
		} catch (ServiceException se) {
			IJ.error("Unable to access to OMERO service");
		} catch (AccessException ae) {
			IJ.error("Cannot access " + dataType + "with ID = " + inputID + ".");
		} catch (OMEROServerError | IOException | ExecutionException e) {
			LOGGER.error("An error occurred.", e);
		} catch (InterruptedException e) {
			LOGGER.error("An error occurred.", e);
			Thread.currentThread().interrupt(); // Restore interrupted status
		}
	}
	
	
	private void runLocalAutocrop() {
		String input            = autocropDialog.getInput();
		String output           = autocropDialog.getOutput();
		String config           = autocropDialog.getConfig();
		String typeThresholding = autocropDialog.getTypeThresholding();
		if (input == null || input.isEmpty()) {
			IJ.error("Input file or directory is missing");
		} else if (output == null || output.isEmpty()) {
			IJ.error("Output directory is missing");
		} else {
			try {
				LOGGER.info("Begin Autocrop process ");
				
				AutocropParameters params = null;
				
				switch (autocropDialog.getConfigMode()) {
					case FILE:
						if (config == null || config.isEmpty()) {
							IJ.error("Config file is missing");
						} else {
							LOGGER.info("Config file");
							params = new AutocropParameters(input, output, config);
						}
						break;
					case INPUT:
						AutocropConfigDialog acd = autocropDialog.getAutocropConfigFileDialog();
						if (acd.isCalibrationSelected()) {
							LOGGER.info("with calibration");
							params = new AutocropParameters(input,
							                                output,
							                                parseInt(acd.getXCalibration()),
							                                parseInt(acd.getYCalibration()),
							                                parseInt(acd.getZCalibration()),
							                                parseInt(acd.getXCropBoxSize()),
							                                parseInt(acd.getYCropBoxSize()),
							                                parseInt(acd.getZCropBoxSize()),
							                                parseInt(acd.getBoxNumberFontSize()),
							                                parseInt(acd.getSlicesOTSUComputing()),
							                                parseInt(acd.getThresholdOTSUComputing()),
							                                parseInt(acd.getChannelToComputeThreshold()),
							                                parseInt(acd.getMinVolume()),
							                                parseInt(acd.getMaxVolume()),
							                                parseInt(acd.getBoxesSurfacePercent()),
							                                acd.isRegroupBoxesSelected());
						} else {
							LOGGER.info("without calibration");
							params = new AutocropParameters(input,
							                                output,
							                                parseInt(acd.getXCropBoxSize()),
							                                parseInt(acd.getYCropBoxSize()),
							                                parseInt(acd.getZCropBoxSize()),
							                                parseInt(acd.getBoxNumberFontSize()),
							                                parseInt(acd.getSlicesOTSUComputing()),
							                                parseInt(acd.getThresholdOTSUComputing()),
							                                parseInt(acd.getChannelToComputeThreshold()),
							                                parseInt(acd.getMinVolume()),
							                                parseInt(acd.getMaxVolume()),
							                                parseInt(acd.getBoxesSurfacePercent()),
							                                acd.isRegroupBoxesSelected());
						}
						break;
					case DEFAULT:
						LOGGER.info("without config");
						params = new AutocropParameters(input, output);
						break;
				}
				AutoCropCalling autoCrop = new AutoCropCalling(params);
				autoCrop.setTypeThresholding(typeThresholding);
				autoCrop.setExecutorThreads(autocropDialog.getThreads());
				File file = new File(input);
				if (file.isDirectory()) {
					autoCrop.runFolder();
				} else if (file.isFile()) {
					autoCrop.runFile(input);
					autoCrop.saveGeneralInfo();
				}
				LOGGER.info("Autocrop process has ended successfully");
				IJ.showMessage("Segmentation process ended successfully on " + file.getName());
			} catch (NumberFormatException e) {
				LOGGER.error("An error occurred during autocrop.", e);
			}
		}
	}
	
}
