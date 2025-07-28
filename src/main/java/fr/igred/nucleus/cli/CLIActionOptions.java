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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static fr.igred.nucleus.cli.CLIUtil.COMMAND;
import static fr.igred.nucleus.cli.CLIUtil.print;


/** Generic class to handle command line option */
public class CLIActionOptions {
	// TODO Store action and option String in constant
	/** Action option */
	protected static final String ACTION_OPTION = "action";
	
	private static final String EOL = System.lineSeparator();
	
	/** Path to input folder */
	protected Option inputFolder = Option.builder("in")
	                                     .longOpt("input")
	                                     .required()
	                                     .type(String.class)
	                                     .numberOfArgs(1)
	                                     .build();
	
	/** Path to second input folder needed in specific action */
	protected Option inputFolder2 = Option.builder("in2")
	                                      .longOpt("input2")
	                                      .type(String.class)
	                                      .numberOfArgs(1)
	                                      .build();
	
	/** Path to second input folder Need in specific action */
	protected Option inputFolder3 = Option.builder("in3")
	                                      .longOpt("input3")
	                                      .type(String.class)
	                                      .numberOfArgs(1)
	                                      .build();
	/** Path to config file */
	protected Option configFile   = Option.builder("c")
	                                      .longOpt("config")
	                                      .type(String.class)
	                                      .desc("Path to config file" + EOL +
	                                            "To generate config file example in current folder:" + EOL +
	                                            COMMAND + " -h configFileExample")
	                                      .numberOfArgs(1)
	                                      .build();
	/** List of available actions */
	protected Option action       = Option.builder(ACTION_OPTION.substring(0, 1))
	                                      .longOpt(ACTION_OPTION)
	                                      .required()
	                                      .type(String.class)
	                                      .desc("Actions available:" + EOL +
	                                            "autocrop : crop wide field images" + EOL +
	                                            "segmentation : nucleus segmentation" + EOL)
	                                      .numberOfArgs(1)
	                                      .build();
	/** Number of threads */
	protected Option threads      = Option.builder("th")
	                                      .longOpt("threads")
	                                      .type(String.class)
	                                      .desc("Number of threads used to split image processing during autocrop or nucleus segmentation (do not exceed the number of available CPUs  (=" +
	                                            Runtime.getRuntime().availableProcessors() + " CPUs))" + EOL +
	                                            "Default : 4 threads for several images (otherwise 1 thread for single image processing)")
	                                      .numberOfArgs(1)
	                                      .build();
	/** OMERO activate */
	protected Option omero        = Option.builder("ome")
	                                      .longOpt("omero")
	                                      .type(boolean.class)
	                                      .desc("Use NucleusJ with OMERO" + EOL)
	                                      .build();
	/** List of available actions */
	protected Option thresholding = Option.builder("thresh")
	                                      .longOpt("thresholding")
	                                      .type(String.class)
	                                      .desc("type of thresholding method to use:" + EOL +
	                                            "Otsu " + EOL +
	                                            "RenyiEntropy " + EOL)
	                                      .numberOfArgs(1)
	                                      .build();
	/** Path to output folder */
	protected Option outputFolder = Option.builder("out")
	                                      .longOpt("output")
	                                      .type(String.class)
	                                      .desc("Path to output results" + EOL)
	                                      .numberOfArgs(1)
	                                      .build();
	
	/** List of options */
	protected Options           options = new Options();
	/** Command line */
	protected CommandLine       cmd;
	/** Command line parser */
	protected CommandLineParser parser  = new DefaultParser();
	
	
	/**
	 * Constructor with argument
	 *
	 * @param args Command line arguments
	 */
	public CLIActionOptions(String[] args) {
		options.addOption(inputFolder);
		options.addOption(inputFolder2);
		options.addOption(inputFolder3);
		options.addOption(configFile);
		options.addOption(action);
		options.addOption(threads);
		options.addOption(omero);
		options.addOption(thresholding);
		try {
			this.cmd = parser.parse(options, args, true);
		} catch (ParseException exp) {
			print(exp.getMessage() + System.lineSeparator());
			printHelpCommand();
			this.cmd = null; // Set command to null if parsing fails
		}
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
	
	
	/** @return list of options */
	public Options getOptions() {
		return options;
	}
	
	
	/**
	 * Get command line
	 *
	 * @return CommandLine object containing parsed command line options
	 */
	public CommandLine getCmd() {
		return cmd;
	}
	
	
	/**
	 * Set command line
	 *
	 * @param cmd CommandLine object to set
	 */
	protected void setCmd(CommandLine cmd) {
		this.cmd = cmd;
	}
	
}