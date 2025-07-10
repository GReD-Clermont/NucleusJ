package gred.nucleus.cli;

import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.Validate.isTrue;


/** class to handle command line option */
public class CLIActionOptionCmdLine extends CLIActionOptions {
	
	/**
	 * @param args command line argument
	 */
	public CLIActionOptionCmdLine(String[] args) {
		super(args);
		action.setDescription(action.getDescription() + "\n" +
		                      "computeParameters : compute parameters \n" +
		                      "computeParametersDL : compute parameters for machine leaning\n" +
		                      "generateProjection : generate projection from coordinates\n" +
		                      "cropFromCoordinate : crop wide-field image from coordinate\n" +
		                      "generateOverlay : generate overlay from images \n");
		
		checkSpecificOptions();
		try {
			this.cmd = parser.parse(options, args);
			isTrue(availableActionCMD(cmd.getOptionValue("action")));
		} catch (ParseException exp) {
			System.console().writer().println(exp.getMessage() + "\n");
			System.console().writer().println(super.getHelperInfo());
			System.exit(1);
		} catch (Exception exp) {
			System.console().writer().println("Action option \"" +
			                                  cmd.getOptionValue("action") +
			                                  "\" not available" + "\n");
			System.console().writer().println(super.getHelperInfo());
			System.exit(1);
		}
	}
	
	
	/**
	 * Method to check action parameter
	 *
	 * @param action NucleusJ3 action to run
	 *
	 * @return boolean existing action
	 */
	private static boolean availableActionCMD(String action) {
		List<String> actionAvailableInOMERO = new ArrayList<>();
		actionAvailableInOMERO.add("autocrop");
		actionAvailableInOMERO.add("segmentation");
		actionAvailableInOMERO.add("computeParameters");
		actionAvailableInOMERO.add("computeParametersDL");
		actionAvailableInOMERO.add("generateProjection");
		actionAvailableInOMERO.add("generateProjectionFiltered");
		actionAvailableInOMERO.add("cropFromCoordinate");
		actionAvailableInOMERO.add("generateOverlay");
		return actionAvailableInOMERO.contains(action);
	}
	
	
	/** Method to check specific action parameters */
	private void checkSpecificOptions() {
		switch (cmd.getOptionValue("action")) {
			case "autocrop":
			case "segmentation":
				inputFolder.setDescription("Path to input folder containing images to analyse\n");
				options.addOption(outputFolder);
				break;
			
			case "computeParameters":
				inputFolder.setDescription("Path to input folder containing RAW images\n");
				inputFolder2.setDescription("Path to input folder containing SEGMENTED images\n");
				options.addOption(inputFolder2);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "computeParametersDL":
				inputFolder.setDescription("Path to input folder containing RAW images\n");
				inputFolder2.setDescription("Path to input folder containing machine leaning SEGMENTED images\n");
				options.addOption(inputFolder2);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "generateProjection":
				inputFolder.setDescription("Path to input folder containing coordinates files\n");
				inputFolder2.setDescription("Path to input folder containing raw data\n");
				options.addOption(inputFolder2);
				break;
			
			case "generateProjectionFiltered":
				inputFolder.setDescription("Path to input folder containing coordinates files\n");
				inputFolder2.setDescription("Path to input folder containing kept images after segmentation filter\n");
				inputFolder3.setDescription("Path to input folder containing initial Zprojection\n");
				options.addOption(inputFolder2);
				options.addOption(inputFolder3);
				omero.setDescription("NOT AVAILABLE");
				break;
			
			case "cropFromCoordinate":
				inputFolder.setDescription("Path to tabulated file containing 2 columns :\n" +
				                           "pathToCoordinateFile   pathToRawImageAssociate\n");
				options.addOption(inputFolder2);
				options.addOption(outputFolder);
				break;
			
			case "generateOverlay":
				inputFolder.setDescription("Path to input folder containing Z-Projections\n");
				inputFolder2.setDescription("Path to input folder containing DIC images\n");
				options.addOption(inputFolder2);
				break;
		}
	}
	
}