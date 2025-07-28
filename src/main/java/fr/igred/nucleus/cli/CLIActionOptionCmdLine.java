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
package fr.igred.nucleus.cli;

import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.lang3.Validate.isTrue;


/** class to handle command line option */
public class CLIActionOptionCmdLine extends CLIActionOptions {
	
	/**
	 * @param args command line argument
	 */
	public CLIActionOptionCmdLine(String[] args) {
		super(args);
		String eol = System.lineSeparator();
		action.setDescription(action.getDescription() + eol +
		                      "computeParameters : compute parameters" + eol +
		                      "computeParametersDL : compute parameters for machine leaning" + eol +
		                      "generateProjection : generate projection from coordinates" + eol +
		                      "cropFromCoordinate : crop wide-field image from coordinate" + eol +
		                      "generateOverlay : generate overlay from images" + eol);
		
		checkSpecificOptions();
		try {
			this.cmd = parser.parse(options, args);
			isTrue(availableActionCMD(cmd.getOptionValue("action")));
		} catch (ParseException exp) {
			System.console().writer().println(exp.getMessage() + eol);
			System.console().writer().println(getHelperInfo());
			System.exit(1);
		} catch (Exception exp) {
			System.console().writer().println("Action option \"" +
			                                  cmd.getOptionValue("action") +
			                                  "\" not available" + eol);
			System.console().writer().println(getHelperInfo());
			System.exit(1);
		}
	}
	
	
	/**
	 * Method to check action parameter
	 *
	 * @param action NucleusJ action to run
	 *
	 * @return boolean existing action
	 */
	private static boolean availableActionCMD(String action) {
		Collection<String> omeroActions = new ArrayList<>(8);
		omeroActions.add("autocrop");
		omeroActions.add("segmentation");
		omeroActions.add("computeParameters");
		omeroActions.add("computeParametersDL");
		omeroActions.add("generateProjection");
		omeroActions.add("generateProjectionFiltered");
		omeroActions.add("cropFromCoordinate");
		omeroActions.add("generateOverlay");
		return omeroActions.contains(action);
	}
	
	
	/** Method to check specific action parameters */
	private void checkSpecificOptions() {
		String eol = System.lineSeparator();
		switch (cmd.getOptionValue("action")) {
			case "autocrop":
			case "segmentation":
				inputFolder.setDescription("Path to input folder containing images to analyse" + eol);
				options.addOption(outputFolder);
				break;
			
			case "computeParameters":
				inputFolder.setDescription("Path to input folder containing RAW images" + eol);
				inputFolder2.setDescription("Path to input folder containing SEGMENTED images" + eol);
				options.addOption(inputFolder2);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "computeParametersDL":
				inputFolder.setDescription("Path to input folder containing RAW images" + eol);
				inputFolder2.setDescription("Path to input folder containing machine leaning SEGMENTED images" + eol);
				options.addOption(inputFolder2);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "generateProjection":
				inputFolder.setDescription("Path to input folder containing coordinates files" + eol);
				inputFolder2.setDescription("Path to input folder containing raw data" + eol);
				options.addOption(inputFolder2);
				break;
			
			case "generateProjectionFiltered":
				inputFolder.setDescription("Path to input folder containing coordinates files" + eol);
				inputFolder2.setDescription("Path to input folder containing kept images after segmentation filter" + eol);
				inputFolder3.setDescription("Path to input folder containing initial Zprojection" + eol);
				options.addOption(inputFolder2);
				options.addOption(inputFolder3);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "cropFromCoordinate":
				inputFolder.setDescription("Path to tabulated file containing 2 columns :" + eol +
				                           "pathToCoordinateFile   pathToRawImageAssociate" + eol);
				options.addOption(inputFolder2);
				options.addOption(outputFolder);
				break;
			
			case "generateOverlay":
				inputFolder.setDescription("Path to input folder containing Z-Projections" + eol);
				inputFolder2.setDescription("Path to input folder containing DIC images" + eol);
				options.addOption(inputFolder2);
				break;
		}
	}
	
}