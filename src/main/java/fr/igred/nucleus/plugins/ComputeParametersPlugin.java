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
import fr.igred.omero.exception.ServiceException;
import fr.igred.nucleus.core.ComputeNucleiParameters;
import fr.igred.nucleus.gui.ComputeParametersDialog;
import fr.igred.nucleus.gui.IDialogListener;
import ij.IJ;
import ij.Prefs;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;


public class ComputeParametersPlugin implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private ComputeParametersDialog computeParametersDialog;
	
	
	/* This method is used by plugins.config */
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		computeParametersDialog = new ComputeParametersDialog(this);
	}
	
	
	@Override
	public void onStart() {
		if (computeParametersDialog.isOmeroEnabled()) {
			runOMERO();
		} else {
			runlocal();
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
			IJ.error("Invalid connection values");
			return null;
		}
		return client;
	}
	
	
	void runlocal() {
		if (computeParametersDialog.getCalibrationStatus()) {
			Calibration calibration = new Calibration();
			calibration.pixelDepth = computeParametersDialog.getZCalibration();
			calibration.pixelWidth = computeParametersDialog.getXCalibration();
			calibration.pixelHeight = computeParametersDialog.getYCalibration();
			calibration.setUnit(computeParametersDialog.getUnit());
			ComputeNucleiParameters generateParameters = new ComputeNucleiParameters(computeParametersDialog.getRawDataDirectory(),
			                                                                         computeParametersDialog.getWorkDirectory(),
			                                                                         calibration);
			generateParameters.run();
		} else {
			ComputeNucleiParameters generateParameters = new ComputeNucleiParameters(computeParametersDialog.getRawDataDirectory(),
			                                                                         computeParametersDialog.getWorkDirectory());
			generateParameters.run();
		}
	}
	
	
	public void runOMERO() {
		// Check connection
		String hostname = computeParametersDialog.getHostname();
		String port     = computeParametersDialog.getPort();
		String username = computeParametersDialog.getUsername();
		char[] password = computeParametersDialog.getPassword();
		String group    = computeParametersDialog.getGroup();
		String rawID    = computeParametersDialog.getRawDatasetID();
		String segID    = computeParametersDialog.getSegDatasetID();
		
		Prefs.set("omero.host", hostname);
		Prefs.set("omero.port", port);
		Prefs.set("omero.user", username);
		
		Client client = checkOMEROConnection(hostname, port, username, password, group);
		
		ComputeNucleiParameters generateParameters = new ComputeNucleiParameters();
		
		try {
			try {
				LOGGER.info("Begin Compute parameter process ");
				generateParameters.runFromOMERO(rawID, segID, client); // Run Compute parameters
				LOGGER.info("Compute parameter process has ended successfully");
			} catch (AccessException | ServiceException | IOException | ExecutionException e) {
				LOGGER.info("Compute parameter process has failed");
				LOGGER.error("An error occurred.", e);
			} catch (InterruptedException e) {
				LOGGER.error("Compute parameter process has been interrupted");
				Thread.currentThread().interrupt();
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
}