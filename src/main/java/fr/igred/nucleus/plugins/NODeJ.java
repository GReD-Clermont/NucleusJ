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
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.nucleus.dialogs.IDialogListener;
import fr.igred.nucleus.gui.GuiAnalysis;
import fr.igred.nucleus.process.ChromocenterCalling;
import ij.IJ;
import ij.plugin.PlugIn;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;


/**
 * Method to detect the chromocenters on bam
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class NODeJ implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private GuiAnalysis gui = null;
	
	
	public static Client checkOMEROConnection(String hostname,
	                                          String port,
	                                          String username,
	                                          char[] password,
	                                          String group) {
		Client client = new Client();
		try {
			client.connect(hostname, Integer.parseInt(port), username, password, Long.valueOf(group));
		} catch (ServiceException | NumberFormatException exp) {
			IJ.error("Invalid connection values");
			return null;
		}
		return client;
	}
	
	
	/**
	 * @param arg
	 */
	/* This method is used by plugins.config */
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		gui = new GuiAnalysis(this);
	}
	
	
	@Override
	public void onStart() {
		if (gui.isOmeroEnabled()) {
			runOmero();
		} else {
			runLocal();
		}
	}
	
	
	void runOmero() {
		// Check connection
		String hostname = gui.getHostname();
		String port     = gui.getPort();
		String username = gui.getUsername();
		char[] password = gui.getPassword();
		String group    = gui.getGroup();
		Client client   = checkOMEROConnection(hostname, port, username, password, group);
		// get IDs
		String sourceID;
		String segmentedID;
		String outputID = gui.getOutputProject();
		// check datatype
		String dataType          = gui.getDataType();
		String dataTypeSegmented = gui.getDataTypeSegmented();
		
		ChromocenterParameters ccAnalyseParams = new ChromocenterParameters(".", ".", ".",
		                                                                    client,
		                                                                    gui.getGaussianX(), gui.getGaussianY(),
		                                                                    gui.getGaussianZ(),
		                                                                    gui.getFactor(), gui.getNeigh(),
		                                                                    gui.isGaussian(), gui.isFilter(),
		                                                                    gui.getMax(), gui.getMin());
		
		ChromocenterCalling ccAnalyse = new ChromocenterCalling(ccAnalyseParams, true);
		
		try {
			if ("Image".equals(dataType) && "Image".equals(dataTypeSegmented)) {
				sourceID = "Image/" + gui.getSourceID();
				segmentedID = "Image/" + gui.getSegmentedNucleiID();
				ccAnalyse.segmentationOMERO(sourceID, segmentedID, outputID, client);
			} else if ("Dataset".equals(dataType) && "Dataset".equals(dataTypeSegmented)) {
				sourceID = "Dataset/" + gui.getSourceID();
				segmentedID = "Dataset/" + gui.getSegmentedNucleiID();
				ccAnalyse.segmentationOMERO(sourceID, segmentedID, outputID, client);
			}
		} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
			LOGGER.error("Error during chromocenter segmentation", e);
		} catch (InterruptedException e) {
			LOGGER.error("Chromocenter segmentation interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	void runLocal() {
		ChromocenterParameters ccAnalyseParams = new ChromocenterParameters(gui.getInputRaw(),
		                                                                    gui.getInputSeg(),
		                                                                    gui.getOutputDir(),
		                                                                    gui.getGaussianX(),
		                                                                    gui.getGaussianY(),
		                                                                    gui.getGaussianZ(),
		                                                                    gui.isGaussian(),
		                                                                    gui.isFilter(),
		                                                                    gui.getMax(),
		                                                                    gui.getMin());
		
		ChromocenterCalling ccAnalyse = new ChromocenterCalling(ccAnalyseParams);
		try {
			ccAnalyse.runSeveralImages2();
		} catch (IOException | FormatException e) {
			LOGGER.error("Error during chromocenter segmentation", e);
		}
		LOGGER.info("End of the chromocenter segmentation , the results are in: {}",
		            ccAnalyseParams.getOutputFolder());
	}
	
}
