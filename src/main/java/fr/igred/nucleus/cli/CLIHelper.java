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

import fr.igred.nucleus.Version;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.OutputTextFile;
import org.apache.commons.cli.HelpFormatter;


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
			cmdHelpFull();
		}
	}
	
	
	/**
	 * Method get help for command line with example command line
	 */
	private static void cmdHelpFull() {
		String eol            = System.lineSeparator();
		String exampleCommand = "java -jar nucleusj-" + Version.get() + ".jar ";
		String exampleArgument = "-action segmentation " +
		                         "-input path/to/input/folder/ " +
		                         "-output path/to/output/folder/ ";
		String[]               exampleCMD = exampleArgument.split(" ");
		CLIActionOptionCmdLine command    = new CLIActionOptionCmdLine(exampleCMD);
		HelpFormatter          formatter  = new HelpFormatter();
		formatter.printHelp("NucleusJ3 cli : ", command.getOptions());
		System.console().writer().println(eol + "Command line example :" + eol +
		                                  exampleCommand + " " + exampleArgument + eol + eol);
		
		String exampleArgumentOMERO = "-omero " +
		                              "-action segmentation " +
		                              "-input path/to/input/folder/ " +
		                              "-output path/to/output/folder/ " +
		                              "-hostname omero-server-address " +
		                              "-port 0 " +
		                              "-group 000";
		String[]             exampleOMEROCMD = exampleArgumentOMERO.split(" ");
		CLIActionOptionOMERO commandOMERO    = new CLIActionOptionOMERO(exampleOMEROCMD);
		formatter.printHelp("NucleusJ3 OMERO MODE: ", commandOMERO.getOptions());
		System.console().writer().println(eol + "Command line example :" + eol + eol +
		                                  exampleCommand + " " + exampleArgumentOMERO);
		
		System.exit(1);
	}
	
	
	/**
	 * Helper for specific action.
	 *
	 * @param action action
	 */
	private static void specificAction(String action) {
		String                 eol            = System.lineSeparator();
		String                 exampleCommand = "java -jar nucleusj-" + Version.get() + ".jar";
		String                 exampleArgument;
		String[]               exampleCMD;
		HelpFormatter          formatter;
		CLIActionOptionCmdLine command;
		switch (action) {
			case "segmentation":
				exampleArgument = "-action segmentation " +
				                  "-input path/to/input/folder/ " +
				                  "-output path/to/output/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 segmentation cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + " " + exampleArgument + eol + eol);
				
				String exampleArgumentOMERO = "-omero " +
				                              "-action segmentation " +
				                              "-input path/to/input/folder/ " +
				                              "-output path/to/output/folder/ " +
				                              "-hostname omero-server-address " +
				                              "-port 0 " +
				                              "-group 000";
				
				String[] exampleOMEROCMD = exampleArgumentOMERO.split(" ");
				CLIActionOptionOMERO commandOMERO = new CLIActionOptionOMERO(exampleOMEROCMD);
				formatter.printHelp("NucleusJ3 segmentation OMERO MODE: ", commandOMERO.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol + eol +
				                                  exampleCommand + " " + exampleArgumentOMERO);
				break;
			
			case "autocrop":
				exampleArgument = "-action autocrop " +
				                  "-input path/to/input/folder/ " +
				                  "-output path/to/output/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 autocrop cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + " " + exampleArgument + eol + eol);
				
				
				exampleArgumentOMERO = "-omero " +
				                       "-action autocrop " +
				                       "-input path/to/input/folder/ " +
				                       "-output path/to/output/folder/ " +
				                       "-hostname omero-server-address " +
				                       "-port 0 " +
				                       "-group 000";
				exampleOMEROCMD = exampleArgumentOMERO.split(" ");
				commandOMERO = new CLIActionOptionOMERO(exampleOMEROCMD);
				formatter.printHelp("NucleusJ3 autocrop OMERO MODE: ", commandOMERO.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol + eol +
				                                  exampleCommand + " " + exampleArgumentOMERO);
				break;
			
			case "computeParameters":
				exampleArgument = "-action computeParameters " +
				                  "-input path/to/raw/image/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 computeParameters cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + " " + exampleArgument + eol + eol);
				break;
			
			case "computeParametersDL":
				exampleArgument = "-action computeParametersDL " +
				                  "-input path/to/raw/image/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 computeParametersDL cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + " " + exampleArgument + eol + eol);
				break;
			
			case "generateProjection":
				exampleArgument = "-action generateProjection " +
				                  "-input path/to/coordinate/file/folder/ " +
				                  "-input2 path/to/raw/image/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 generateProjection cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + exampleArgument + eol + eol);
				break;
			
			case "generateProjectionFiltered":
				exampleArgument = "-action generateProjectionFiltered " +
				                  "-input path/to/coordinate/file/folder/ " +
				                  "-input2 path/to/segmented/image/folder/ " +
				                  "-input3 path/to/ZProjection/folder/";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 generateProjectionFiltered cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + exampleArgument + eol + eol);
				break;
			
			case "CropFromCoordinate":
				exampleArgument = "-action CropFromCoordinate " +
				                  "-input path/to/coordinate/file/folder/ ";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 CropFromCoordinate cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + exampleArgument + eol + eol);
				break;
			
			case "GenerateOverlay":
				exampleArgument = "-action GenerateOverlay " +
				                  "-input path/to/input/zprojection/ " +
				                  "-input2 path/to/input/dic_images/";
				exampleCMD = exampleArgument.split(" ");
				command = new CLIActionOptionCmdLine(exampleCMD);
				formatter = new HelpFormatter();
				formatter.printHelp("NucleusJ3 GenerateOverlay cli : ", command.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol +
				                                  exampleCommand + exampleArgument + eol + eol);
				
				exampleArgumentOMERO = "-omero " +
				                       "-action GenerateOverlay " +
				                       "-input ZProjection_dataset_ID " +
				                       "-input2 DIC_dataset_ID " +
				                       "-output output_project_ID " +
				                       "-port 0 " +
				                       "-group 000";
				exampleOMEROCMD = exampleArgumentOMERO.split(" ");
				commandOMERO = new CLIActionOptionOMERO(exampleOMEROCMD);
				formatter.printHelp("NucleusJ3 GenerateOverlay OMERO MODE: ", commandOMERO.getOptions());
				System.console().writer().println(eol + "Command line example :" + eol + eol +
				                                  exampleCommand + exampleArgumentOMERO);
				///
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
				System.console().writer().println("Two config file with default parameters generate:" + eol);
				
				saveFile(autocropConfigOption, "autocropConfigListParameters");
				saveFile(segConfigOption, "segmentationConfigListParameters");
				System.console().writer().println("autocrop parameters details: " +
				                                  "https://github.com/GReD-Clermont/NucleusJ/wiki/Autocrop#list-of-available-parameters " + eol +
				                                  "segmentation parameters details: " +
				                                  "https://github.com/GReD-Clermont/NucleusJ/wiki/Autocrop#list-of-available-parameters");
				break;
			
			default:
				System.console().writer().println("Invalid action \"" + action + "\" :" + eol);
				System.console().writer().println(CLIActionOptions.getHelperInfo());
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
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(dirOutput.getDirPath() +
		                                                         dirOutput.getSeparator() +
		                                                         fileName);
		resultFileOutputOTSU.saveTextFile(text, true);
	}
	
}
