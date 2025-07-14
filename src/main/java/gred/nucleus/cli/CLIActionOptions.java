package gred.nucleus.cli;

import gred.nucleus.Version;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/** Generic class to handle command line option */
public class CLIActionOptions {
	// TODO Store action and option String in constant
	/** NucleusJ version */
	private static final String NJ_VERSION = Version.get();
	
	/** Path to input folder */
	public Option inputFolder = Option.builder("in")
	                                  .longOpt("input")
	                                  .required()
	                                  .type(String.class)
	                                  .numberOfArgs(1)
	                                  .build();
	
	/** Path to second input folder needed in specific action */
	public Option inputFolder2 = Option.builder("in2")
	                                   .longOpt("input2")
	                                   .type(String.class)
	                                   .numberOfArgs(1)
	                                   .build();
	
	/** Path to second input folder Need in specific action */
	public    Option inputFolder3 = Option.builder("in3")
	                                      .longOpt("input3")
	                                      .type(String.class)
	                                      .numberOfArgs(1)
	                                      .build();
	/** Path to config file */
	public    Option configFile   = Option.builder("c")
	                                      .longOpt("config")
	                                      .type(String.class)
	                                      .desc("Path to config file\n" +
	                                            "To generate config file example in current folder:\n" +
	                                            "java -jar nucleusj-" +
	                                            NJ_VERSION +
	                                            ".jar -h configFileExample")
	                                      .numberOfArgs(1)
	                                      .build();
	/** List of available actions */
	public    Option action       = Option.builder("a")
	                                      .longOpt("action")
	                                      .required()
	                                      .type(String.class)
	                                      .desc("Action available:\n" +
	                                            "autocrop : crop wide field images\n" +
	                                            "segmentation : nucleus segmentation\n")
	                                      .numberOfArgs(1)
	                                      .build();
	/** Number of threads */
	public    Option threads      = Option.builder("th")
	                                      .longOpt("threads")
	                                      .type(String.class)
	                                      .desc("Number of threads used to split image processing during autocrop or nucleus segmentation (do not exceed the number of available CPUs  (=" +
	                                            Runtime.getRuntime().availableProcessors() + " CPUs))\n" +
	                                            "Default : 4 threads for several images (otherwise 1 thread for single image processing)")
	                                      .numberOfArgs(1)
	                                      .build();
	/** OMERO activate */
	public    Option omero        = Option.builder("ome")
	                                      .longOpt("omero")
	                                      .type(boolean.class)
	                                      .desc("Use of NucleusJ3 in OMERO\n")
	                                      .build();
	/** List of available actions */
	public    Option thresholding = Option.builder("thresh")
	                                      .longOpt("thresholding")
	                                      .type(String.class)
	                                      .desc("type of thresholding method to use:\n" +
	                                            "Otsu \n" +
	                                            "RenyiEntropy \n")
	                                      .numberOfArgs(1)
	                                      .build();
	/** Path to output folder */
	protected Option outputFolder = Option.builder("out")
	                                      .longOpt("output")
	                                      .type(String.class)
	                                      .desc("Path to output results\n")
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
			System.console().writer().println(exp.getMessage() + "\n");
			System.console().writer().println(getHelperInfo());
			System.exit(1);
		}
	}
	
	
	/** @return : helper info */
	public static String getHelperInfo() {
		return "More details for available actions:\n" +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -h \n" +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -help \n\n" +
		       "More details for a specific action:\n" +
		       "java -jar nucleusj-" + NJ_VERSION + ".jar -h <action>\n" +
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