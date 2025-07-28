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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static fr.igred.nucleus.cli.CLIUtil.print;


/** Generic class to handle command line option */
public class CLIActionOptions {
	// TODO Store action and option String in constant
	/** NucleusJ version */
	private static final String NJ_VERSION = Version.get();
	
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
	                                            "java -jar nucleusj-" +
	                                            NJ_VERSION +
	                                            ".jar -h configFileExample")
	                                      .numberOfArgs(1)
	                                      .build();
	/** List of available actions */
	protected Option action       = Option.builder("a")
	                                      .longOpt("action")
	                                      .required()
	                                      .type(String.class)
	                                      .desc("Action available:" + EOL +
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
	 * @param argument List of command line argument
	 */
	public CLIActionOptions(String[] argument) {
		options.addOption(inputFolder);
		options.addOption(inputFolder2);
		options.addOption(inputFolder3);
		options.addOption(configFile);
		options.addOption(action);
		options.addOption(threads);
		options.addOption(omero);
		options.addOption(thresholding);
		try {
			this.cmd = parser.parse(options, argument, true);
		} catch (ParseException exp) {
			print(exp.getMessage() + System.lineSeparator());
			print(getHelperInfo());
			System.exit(1);
		}
	}
	
	
	/** @return : helper info */
	public static String getHelperInfo() {
		String eol = System.lineSeparator();
		return "More details for available actions:" + eol +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -h" + eol +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -help" + eol +
		       eol +
		       "More details for a specific action:" + eol +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -h <action>" + eol +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -help <action>";
	}
	
	
	/** @return list of options */
	public Options getOptions() {
		return options;
	}
	
	
	public CommandLine getCmd() {
		return cmd;
	}
	
}