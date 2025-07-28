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
		ChromocenterParameters params = new ChromocenterParameters(cmd.getOptionValue("input"),
		                                                           cmd.getOptionValue("input2"),
		                                                           cmd.getOptionValue("o"));
		if (cmd.hasOption("isG")) {
			params.setGaussianOnRaw(true);
		}
		if (cmd.hasOption("isF")) {
			params.setSizeFiltered(true);
		}
		if (cmd.hasOption("noC")) {
			params.setNoChange(true);
		}
		if (cmd.hasOption("gX")) {
			params.setXGaussianSigma(parseDouble(cmd.getOptionValue("gX")));
		}
		if (cmd.hasOption("gY")) {
			params.setYGaussianSigma(parseDouble(cmd.getOptionValue("gY")));
		}
		if (cmd.hasOption("gZ")) {
			params.setZGaussianSigma(parseDouble(cmd.getOptionValue("gZ")));
		}
		if (cmd.hasOption("min")) {
			params.setMinSize(parseDouble(cmd.getOptionValue("min")));
		}
		if (cmd.hasOption("max")) {
			params.setMaxSize(parseDouble(cmd.getOptionValue("max")));
		}
		if (cmd.hasOption("f")) {
			params.setFactor(parseDouble(cmd.getOptionValue("f")));
		}
		if (cmd.hasOption("n")) {
			params.setNeighbours(parseInt(cmd.getOptionValue("n")));
		}
		
		ChromocenterCalling ccCalling = new ChromocenterCalling(params);
		try {
			LOGGER.info("-input {} -input2 {} -output {}",
			            params.getInputFolder(),
			            params.getSegmentedInputFolder(),
			            params.getOutputFolder());
			ccCalling.runSeveralImages2();
		} catch (IOException | FormatException e) {
			LOGGER.error("An error occurred during chromocenter segmentation.", e);
		}
		LOGGER.info("End !!! Results available: {}", params.getOutputFolder());
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
		AutocropParameters params = new AutocropParameters(cmd.getOptionValue("input"),
		                                                   cmd.getOptionValue("output"));
		if (cmd.hasOption("config")) {
			params.addGeneralProperties(cmd.getOptionValue("config"));
			params.addProperties(cmd.getOptionValue("config"));
		}
		File path = new File(cmd.getOptionValue("input"));
		if (path.isFile()) {
			AutoCropCalling autoCrop = new AutoCropCalling(params);
			autoCrop.runFile(cmd.getOptionValue("input"));
			autoCrop.saveGeneralInfo();
		} else {
			AutoCropCalling autoCrop = new AutoCropCalling(params);
			if (cmd.hasOption("threads")) {
				autoCrop.setExecutorThreads(parseInt(cmd.getOptionValue("threads")));
			}
			autoCrop.runFolder();
		}
	}
	
	
	private void runSegmentation() throws FormatException {
		SegmentationParameters params = new SegmentationParameters(cmd.getOptionValue("input"),
		                                                                           cmd.getOptionValue("output"));
		if (cmd.hasOption("config")) {
			params.addGeneralProperties(cmd.getOptionValue("config"));
			params.addProperties(cmd.getOptionValue("config"));
		}
		File path = new File(cmd.getOptionValue("input"));
		if (path.isFile()) {
			SegmentationCalling otsuModified = new SegmentationCalling(params);
			try {
				String log = otsuModified.runOneImage(cmd.getOptionValue("input"));
				otsuModified.saveCropGeneralInfo();
				if (!log.isEmpty()) {
					LOGGER.error("Nuclei which didn't pass the segmentation:{}{}", System.lineSeparator(), log);
				}
			} catch (IOException e) {
				LOGGER.error("An error occurred.", e);
			}
		} else {
			SegmentationCalling otsuModified = new SegmentationCalling(params);
			try {
				if (cmd.hasOption("threads")) {
					otsuModified.setExecutorThreads(parseInt(cmd.getOptionValue("threads")));
				}
				String log = otsuModified.runSeveralImages();
				if (!log.isEmpty()) {
					LOGGER.error("Nuclei which didn't pass the segmentation:{}{}", System.lineSeparator(), log);
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
