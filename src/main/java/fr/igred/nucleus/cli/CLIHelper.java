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

import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.OutputTextFile;
import org.apache.commons.cli.HelpFormatter;

import static fr.igred.nucleus.cli.CLIUtil.COMMAND;
import static fr.igred.nucleus.cli.CLIUtil.print;


/** Class to generate helper */
public final class CLIHelper {
	
	/** Private constructor to avoid instantiation */
	private CLIHelper() {
		// DO NOTHING
	}
	
	
	/**
	 * Main method
	 *
	 * @param args command line arguments
	 */
	public static void run(String[] args) {
		if (args.length == 2) {
			specificAction(args[1]);
		} else {
			printHelpFull();
		}
	}
	
	
	private static void printExampleCommand(String argument) {
		String                 eol        = System.lineSeparator();
		String[]               exampleCMD = argument.split(" ");
		CLIActionOptionCmdLine cmd        = new CLIActionOptionCmdLine(exampleCMD);
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("NucleusJ segmentation cli: ", cmd.getOptions());
		print(eol + "Command line example:" + eol +
		      COMMAND + " " + argument + eol + eol);
	}
	
	
	private static void printExampleCommandOMERO(String argument) {
		String               eol        = System.lineSeparator();
		String[]             exampleCMD = argument.split(" ");
		CLIActionOptionOMERO cmd        = new CLIActionOptionOMERO(exampleCMD);
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("NucleusJ CLI: ", cmd.getOptions());
		print(eol + "Command line example:" + eol +
		      COMMAND + " " + argument + eol + eol);
	}
	
	
	/** Print help command usage */
	public static void printHelpCommand() {
		String eol = System.lineSeparator();
		String info = "More details for available actions:" + eol +
		              COMMAND + " -h" + eol +
		              COMMAND + " -help" + eol +
		              eol +
		              "More details for a specific action:" + eol +
		              COMMAND + " -h <action>" + eol +
		              COMMAND + " -help <action>";
		print(info);
	}
	
	
	/** Print full help command usage */
	private static void printHelpFull() {
		String exampleArgument = "-action autocrop " +
		                         "-input path/to/input/folder/ " +
		                         "-output path/to/output/folder/ ";
		printExampleCommand(exampleArgument);
	}
	
	
	/**
	 * Helper for specific action.
	 *
	 * @param action action
	 */
	private static void specificAction(String action) {
		String eol = System.lineSeparator();
		String exampleArgument;
		String exampleArgumentOMERO;
		switch (action) {
			case "segmentation":
				exampleArgument = "-action segmentation " +
				                  "-input path/to/input/folder/ " +
				                  "-output path/to/output/folder/ ";
				printExampleCommand(exampleArgument);
				
				exampleArgumentOMERO = "-omero " +
				                       "-action segmentation " +
				                       "-input path/to/input/folder/ " +
				                       "-output path/to/output/folder/ " +
				                       "-hostname omero-server-address " +
				                       "-port 0 " +
				                       "-user username " +
				                       "-group 000";
				printExampleCommandOMERO(exampleArgumentOMERO);
				break;
			
			case "autocrop":
				exampleArgument = "-action autocrop " +
				                  "-input path/to/input/folder/ " +
				                  "-output path/to/output/folder/ ";
				printExampleCommand(exampleArgument);
				
				exampleArgumentOMERO = "-omero " +
				                       "-action autocrop " +
				                       "-input path/to/input/folder/ " +
				                       "-output path/to/output/folder/ " +
				                       "-hostname omero-server-address " +
				                       "-port 0 " +
				                       "-user username " +
				                       "-group 000";
				printExampleCommandOMERO(exampleArgumentOMERO);
				break;
			
			case "computeParameters":
				exampleArgument = "-action computeParameters " +
				                  "-input path/to/raw/image/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ ";
				printExampleCommand(exampleArgument);
				break;
			
			case "computeParametersDL":
				exampleArgument = "-action computeParametersDL " +
				                  "-input path/to/raw/image/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ ";
				printExampleCommand(exampleArgument);
				break;
			
			case "generateProjection":
				exampleArgument = "-action generateProjection " +
				                  "-input path/to/coordinate/file/folder/ " +
				                  "-input2 path/to/raw/image/folder/ ";
				printExampleCommand(exampleArgument);
				break;
			
			case "generateProjectionFiltered":
				exampleArgument = "-action generateProjectionFiltered " +
				                  "-input path/to/coordinate/file/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ " +
				                  "-input3 path/to/ZProjection/folder/";
				printExampleCommand(exampleArgument);
				break;
			
			case "CropFromCoordinate":
				exampleArgument = "-action CropFromCoordinate " +
				                  "-input path/to/coordinate/file/folder/ ";
				printExampleCommand(exampleArgument);
				break;
			
			case "GenerateOverlay":
				exampleArgument = "-action GenerateOverlay " +
				                  "-input path/to/input/zprojection/ " +
				                  "-input2 path/to/input/dic_images/";
				printExampleCommand(exampleArgument);
				
				exampleArgumentOMERO = "-omero " +
				                       "-action GenerateOverlay " +
				                       "-input ZProjection_dataset_ID " +
				                       "-input2 DIC_dataset_ID " +
				                       "-output output_project_ID " +
				                       "-port 0 " +
				                       "-user username " +
				                       "-group 000";
				printExampleCommandOMERO(exampleArgumentOMERO);
				break;
			
			case "configFileExample":
				String autocropConfigOption = "xCropBoxSize:40" + eol +
				                              "yCropBoxSize:40" + eol +
				                              "zCropBoxSize:20" + eol +
				                              "minVolumeNucleus:1" + eol +
				                              "maxVolumeNucleus:2147483647" + eol +
				                              "thresholdOTSUComputing:20" + eol +
				                              "channelToComputeThreshold:0" + eol +
				                              "slicesOTSUComputing:0" + eol +
				                              "boxesPercentSurfaceToFilter:50" + eol +
				                              "boxesRegrouping:10" + eol +
				                              "xCal:1" + eol +
				                              "yCal:1" + eol +
				                              "zCal:1";
				
				String segConfigOption = "thresholdOTSUComputing:20" + eol +
				                         "ConvexHullDetection:true" + eol +
				                         "xCal:1" + eol +
				                         "yCal:1" + eol +
				                         "zCal:1";
				print("Two config file with default parameters generate:" + eol);
				
				saveFile(autocropConfigOption, "autocropConfigListParameters");
				saveFile(segConfigOption, "segmentationConfigListParameters");
				print("autocrop parameters details: " +
				      "https://github.com/GReD-Clermont/NucleusJ/wiki/Autocrop#list-of-available-parameters " +
				      eol +
				      "segmentation parameters details: " +
				      "https://github.com/GReD-Clermont/NucleusJ/wiki/Autocrop#list-of-available-parameters");
				break;
			
			default:
				print("Invalid action \"" + action + "\" :" + eol);
				printHelpCommand();
				break;
		}
	}
	
	
	/**
	 * Save information use to save config file parameter example.
	 *
	 * @param text     text to save
	 * @param fileName file name
	 */
	public static void saveFile(String text, String fileName) {
		Directory dirOutput = new Directory(System.getProperty("user.dir"));
		OutputTextFile otsuOutput = new OutputTextFile(dirOutput.getDirPath() +
		                                               dirOutput.getSeparator() +
		                                               fileName);
		otsuOutput.saveTextFile(text, true);
	}
	
}
