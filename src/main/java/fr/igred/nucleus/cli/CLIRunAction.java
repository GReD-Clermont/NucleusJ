package fr.igred.nucleus.cli;

import fr.igred.nucleus.autocrop.AutoCropCalling;
import fr.igred.nucleus.autocrop.AutocropParameters;
import fr.igred.nucleus.autocrop.CropFromCoordinates;
import fr.igred.nucleus.autocrop.GenerateOverlay;
import fr.igred.nucleus.autocrop.GenerateProjectionFromCoordinates;
import fr.igred.nucleus.core.ComputeNucleiParameters;
import fr.igred.nucleus.machinelearning.ComputeNucleiParametersML;
import fr.igred.nucleus.plugins.ChromocenterParameters;
import fr.igred.nucleus.process.ChromocenterCalling;
import fr.igred.nucleus.segmentation.SegmentationCalling;
import fr.igred.nucleus.segmentation.SegmentationParameters;
import loci.formats.FormatException;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;


public class CLIRunAction {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Command line */
	private final CommandLine cmd;
	
	
	public CLIRunAction(CommandLine cmd) {
		this.cmd = cmd;
	}
	
	
	public void run() throws FormatException, IOException {
		switch (cmd.getOptionValue("action")) {
			case "autocrop":
				runAutocrop();
				break;
			case "segmentation":
				runSegmentation();
				break;
			case "computeParameters":
				runComputeNucleiParameters();
				break;
			case "computeParametersDL":
				runComputeNucleiParametersDL();
				break;
			case "generateProjection":
				runProjectionFromCoordinates();
				break;
			case "cropFromCoordinate":
				runCropFromCoordinates();
				break;
			case "generateOverlay":
				runGenerateOV();
				break;
			case "segCC":
				runSegCC();
				break;
			default:
				throw new IllegalArgumentException("Invalid action.");
		}
	}
	
	
	private void runSegCC() {
		ChromocenterParameters chromocenterParameters = new ChromocenterParameters(cmd.getOptionValue("input"),
		                                                                           cmd.getOptionValue("input2"),
		                                                                           cmd.getOptionValue("o"));
		if (cmd.hasOption("isG")) {
			chromocenterParameters.gaussianOnRaw = true;
		}
		if (cmd.hasOption("isF")) {
			chromocenterParameters.sizeFilterConnectedComponent = true;
		}
		if (cmd.hasOption("noC")) {
			chromocenterParameters.noChange = true;
		}
		if (cmd.hasOption("gX")) {
			chromocenterParameters.gaussianBlurXsigma = parseDouble(cmd.getOptionValue("gX"));
		}
		if (cmd.hasOption("gY")) {
			chromocenterParameters.gaussianBlurYsigma = parseDouble(cmd.getOptionValue("gY"));
		}
		if (cmd.hasOption("gZ")) {
			chromocenterParameters.gaussianBlurZsigma = parseDouble(cmd.getOptionValue("gZ"));
		}
		if (cmd.hasOption("min")) {
			chromocenterParameters.minSizeConnectedComponent = parseDouble(cmd.getOptionValue("min"));
		}
		if (cmd.hasOption("max")) {
			chromocenterParameters.maxSizeConnectedComponent = parseDouble(cmd.getOptionValue("max"));
		}
		if (cmd.hasOption("f")) {
			chromocenterParameters.factor = parseDouble(cmd.getOptionValue("f"));
		}
		if (cmd.hasOption("n")) {
			chromocenterParameters.neighbours = parseInt(cmd.getOptionValue("n"));
		}
		
		ChromocenterCalling ccCalling = new ChromocenterCalling(chromocenterParameters);
		try {
			LOGGER.info("-input {} -input2 {} - {}",
			            chromocenterParameters.inputFolder,
			            chromocenterParameters.segInputFolder,
			            chromocenterParameters.outputFolder);
			ccCalling.runSeveralImages2();
		} catch (IOException | FormatException e) {
			LOGGER.error("An error occurred during chromocenter segmentation.", e);
		}
		LOGGER.info("End !!! Results available: {}", chromocenterParameters.outputFolder);
	}
	
	
	private void runGenerateOV() throws IOException {
		GenerateOverlay ov = new GenerateOverlay(cmd.getOptionValue("input"),
		                                         cmd.getOptionValue("input2"));
		ov.run();
	}
	
	
	private void runCropFromCoordinates() throws IOException, FormatException {
		CropFromCoordinates cropFromCoordinates = new CropFromCoordinates(cmd.getOptionValue("input"),
		                                                                  cmd.getOptionValue("input2"),
		                                                                  cmd.getOptionValue("output"));
		cropFromCoordinates.run();
	}
	
	
	private void runProjectionFromCoordinates()
	throws IOException, FormatException {
		if (cmd.hasOption("coordinateFiltered")) {
			GenerateProjectionFromCoordinates projection =
					new GenerateProjectionFromCoordinates(cmd.getOptionValue("input"),
					                                      cmd.getOptionValue("input2"),
					                                      cmd.getOptionValue("input3"));
			projection.generateProjectionFiltered();
		} else {
			GenerateProjectionFromCoordinates projection =
					new GenerateProjectionFromCoordinates(cmd.getOptionValue("input"),
					                                      cmd.getOptionValue("input2"));
			projection.generateProjection();
		}
	}
	
	
	private void runAutocrop() {
		AutocropParameters autocropParameters = new AutocropParameters(cmd.getOptionValue("input"),
		                                                               cmd.getOptionValue("output"));
		if (cmd.hasOption("config")) {
			autocropParameters.addGeneralProperties(cmd.getOptionValue("config"));
			autocropParameters.addProperties(cmd.getOptionValue("config"));
		}
		File path = new File(cmd.getOptionValue("input"));
		if (path.isFile()) {
			AutoCropCalling autoCrop = new AutoCropCalling(autocropParameters);
			autoCrop.runFile(cmd.getOptionValue("input"));
			autoCrop.saveGeneralInfo();
		} else {
			AutoCropCalling autoCrop = new AutoCropCalling(autocropParameters);
			if (cmd.hasOption("threads")) {
				autoCrop.setExecutorThreads(parseInt(cmd.getOptionValue("threads")));
			}
			autoCrop.runFolder();
		}
	}
	
	
	private void runSegmentation() throws FormatException {
		SegmentationParameters segmentationParameters = new SegmentationParameters(cmd.getOptionValue("input"),
		                                                                           cmd.getOptionValue("output"));
		if (cmd.hasOption("config")) {
			segmentationParameters.addGeneralProperties(cmd.getOptionValue("config"));
			segmentationParameters.addProperties(cmd.getOptionValue("config"));
		}
		File path = new File(cmd.getOptionValue("input"));
		if (path.isFile()) {
			SegmentationCalling otsuModified = new SegmentationCalling(segmentationParameters);
			try {
				String log = otsuModified.runOneImage(cmd.getOptionValue("input"));
				otsuModified.saveCropGeneralInfo();
				if (!log.isEmpty()) {
					LOGGER.error("Nuclei which didn't pass the segmentation\n{}", log);
				}
			} catch (IOException e) {
				LOGGER.error("An error occurred.", e);
			}
		} else {
			SegmentationCalling otsuModified = new SegmentationCalling(segmentationParameters);
			try {
				if (cmd.hasOption("threads")) {
					otsuModified.setExecutorThreads(parseInt(cmd.getOptionValue("threads")));
				}
				String log = otsuModified.runSeveralImages2();
				if (!log.isEmpty()) {
					LOGGER.error("Nuclei which didn't pass the segmentation\n{}", log);
				}
			} catch (IOException e) {
				LOGGER.error("An error occurred.", e);
			}
		}
	}
	
	
	private void runComputeNucleiParameters() {
		ComputeNucleiParameters generateParameters = new ComputeNucleiParameters(cmd.getOptionValue("input"),
		                                                                         cmd.getOptionValue("input2"));
		if (cmd.hasOption("config")) {
			generateParameters.addConfigParameters(cmd.getOptionValue("config"));
		}
		generateParameters.run();
	}
	
	
	private void runComputeNucleiParametersDL()
	throws IOException, FormatException {
		ComputeNucleiParametersML computeParameters = new ComputeNucleiParametersML(cmd.getOptionValue("input"),
		                                                                            cmd.getOptionValue("input2"));
		computeParameters.run();
	}
	
}
