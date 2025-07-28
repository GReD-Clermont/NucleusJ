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
import fr.igred.nucleus.core.ChromocenterAnalysis;
import fr.igred.nucleus.core.ComputeNucleiParameters;
import fr.igred.nucleus.plugins.ChromocenterParameters;
import fr.igred.nucleus.process.ChromocenterCalling;
import fr.igred.nucleus.segmentation.SegmentationCalling;
import fr.igred.nucleus.segmentation.SegmentationParameters;
import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import loci.formats.FormatException;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.lang.System.exit;


public class CLIRunActionOMERO {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Command line */
	private final CommandLine cmd;
	
	/** OMERO client information see fr.igred.omero.Client */
	private final Client client = new Client();
	
	/** OMERO server hostname */
	private String hostname;
	/** OMERO username */
	private String username;
	/** OMERO server port */
	private int    port;
	/** OMERO groupe ID */
	private long   groupID;
	/** OMERO password connection */
	private char[] password;
	/** OMERO session ID */
	private String sessionID;
	
	
	public CLIRunActionOMERO(CommandLine cmd) {
		this.cmd = cmd;
		if (this.cmd.hasOption("omeroConfig")) {
			addLoginCredentials(this.cmd.getOptionValue("omeroConfig"));
		} else {
			this.hostname = this.cmd.getOptionValue("hostname");
			this.port = Integer.parseInt(this.cmd.getOptionValue("port"));
			if (this.cmd.hasOption("sessionID")) {
				this.sessionID = this.cmd.getOptionValue("sessionID");
			} else {
				this.username = this.cmd.getOptionValue("username");
				getOMEROPassword();
				this.groupID = Long.parseLong(this.cmd.getOptionValue("group"));
			}
		}
		checkOMEROConnection();
	}
	
	
	public static void autoCropOMERO(String inputDirectory,
	                                 String outputDirectory,
	                                 Client client,
	                                 AutoCropCalling autoCrop)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException, InterruptedException {
		String[] param = inputDirectory.split("/");
		
		ProjectWrapper project = client.getProject(Long.parseLong(outputDirectory));
		
		if (param.length >= 2) {
			Long id = Long.parseLong(param[1]);
			if ("image".equals(param[0])) {
				ImageWrapper image = client.getImage(id);
				
				int sizeC = image.getPixels().getSizeC();
				
				Long[] outputsDat = new Long[sizeC];
				
				for (int i = 0; i < sizeC; i++) {
					outputsDat[i] = project.addDataset(client, "C" + i + "_" + image.getName(), "").getId();
				}
				
				autoCrop.runImageOMERO(image, outputsDat, client);
				autoCrop.saveGeneralInfoOmero(client, outputsDat);
			} else {
				List<ImageWrapper> images;
				
				String name = "";
				
				if ("dataset".equals(param[0])) {
					DatasetWrapper dataset = client.getDataset(id);
					
					name = dataset.getName();
					
					if (param.length == 4 && "tag".equals(param[2])) {
						images = dataset.getImagesTagged(client, Long.parseLong(param[3]));
					} else {
						images = dataset.getImages(client);
					}
				} else if ("tag".equals(param[0])) {
					TagAnnotationWrapper tag = client.getTag(id);
					images = tag.getImages(client);
				} else {
					throw new IllegalArgumentException("Wrong input parameter");
				}
				
				int sizeC = images.get(0).getPixels().getSizeC();
				
				Long[] outputsDat = new Long[sizeC];
				
				for (int i = 0; i < sizeC; i++) {
					outputsDat[i] = project.addDataset(client, "raw_C" + i + "_" + name, "").getId();
					project.reload(client);
				}
				autoCrop.runSeveralImageOMERO(images, outputsDat, client);
			}
		} else {
			throw new IllegalArgumentException("Wrong input parameter : "
			                                   + inputDirectory + "\n\n\n"
			                                   + "Example format expected:\n"
			                                   + "dataset/OMERO_ID \n");
		}
	}
	
	
	public static void segmentationOMERO(String inputDirectory,
	                                     String outputDirectory,
	                                     Client client,
	                                     SegmentationCalling otsuModified)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError {
		String[] param = inputDirectory.split("/");
		
		if (param.length >= 2) {
			Long id = Long.parseLong(param[1]);
			if ("image".equals(param[0])) {
				ImageWrapper image = client.getImage(id);
				
				try {
					String log;
					if (param.length == 3 && "ROI".equals(param[2])) {
						log = otsuModified.runOneImageOMERObyROIs(image, Long.parseLong(outputDirectory), client);
					} else {
						log = otsuModified.runOneImageOMERO(image, Long.parseLong(outputDirectory), client);
					}
					otsuModified.saveCropGeneralInfoOmero(client, Long.parseLong(outputDirectory));
					if (!log.isEmpty()) {
						LOGGER.error("Nuclei which didn't pass the segmentation\n{}", log);
					}
				} catch (IOException | OMEROServerError e) {
					LOGGER.error("An error occurred.", e);
				} catch (InterruptedException e) {
					LOGGER.error("An interruption occurred.", e);
					Thread.currentThread().interrupt();
				}
			} else {
				List<ImageWrapper> images;
				
				switch (param[0]) {
					case "dataset":
						DatasetWrapper dataset = client.getDataset(id);
						
						if (param.length == 4 && "tag".equals(param[2])) {
							images = dataset.getImagesTagged(client, Long.parseLong(param[3]));
						} else {
							images = dataset.getImages(client);
						}
						break;
					case "project":
						ProjectWrapper project = client.getProject(id);
						
						if (param.length == 4 && "tag".equals(param[2])) {
							images = project.getImagesTagged(client, Long.parseLong(param[3]));
						} else {
							images = project.getImages(client);
						}
						break;
					case "tag":
						TagAnnotationWrapper tag = client.getTag(id);
						images = client.getImages(tag);
						break;
					default:
						throw new IllegalArgumentException();
				}
				try {
					String log;
					if (param.length == 3 && "ROI".equals(param[2]) ||
					    param.length == 5 && "ROI".equals(param[4])) {
						log = otsuModified.runSeveralImagesOMERObyROIs(images, Long.parseLong(outputDirectory), client);
					} else {
						log = otsuModified.runSeveralImagesOMERO(images, Long.parseLong(outputDirectory), client, id);
					}
					if (!log.isEmpty()) {
						LOGGER.error("Nuclei which didn't pass the segmentation\n{}", log);
					}
				} catch (IOException e) {
					LOGGER.error("An error occurred.", e);
				} catch (InterruptedException e) {
					LOGGER.error("An interruption occurred.", e);
					Thread.currentThread().interrupt();
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	
	private void addLoginCredentials(String pathToConfigFile) {
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(pathToConfigFile)) {
			prop.load(is);
		} catch (FileNotFoundException ex) {
			LOGGER.error("{}: can't find the OMERO config file !", pathToConfigFile);
			exit(-1);
		} catch (IOException ex) {
			LOGGER.error("{}: can't load the OMERO config file !", pathToConfigFile);
			exit(-1);
		}
		Set<String> properties = prop.stringPropertyNames();
		for (String idProp : properties) {
			try {
				switch (idProp) {
					case "hostname":
						this.hostname = prop.getProperty("hostname");
						break;
					case "port":
						this.port = Integer.parseInt(prop.getProperty("port"));
						break;
					case "username":
						this.username = prop.getProperty("username");
						break;
					case "password":
						this.password = prop.getProperty("password").toCharArray();
						break;
					case "group":
						this.groupID = Long.parseLong(prop.getProperty("group"));
						break;
					case "sessionID":
						this.sessionID = prop.getProperty("sessionID");
						break;
					default:
						LOGGER.warn("Unknown property in OMERO config file: {}", idProp);
				}
			} catch (NumberFormatException nfe) {
				LOGGER.error("OMERO config error : Port and groupID must be number");
				exit(1);
			}
		}
	}
	
	
	private void getOMEROPassword() {
		if (cmd.hasOption("password")) {
			this.password = cmd.getOptionValue("password").toCharArray();
		} else {
			System.console().writer().println("Enter password: ");
			Console con = System.console();
			this.password = con.readPassword();
		}
	}
	
	
	private void checkOMEROConnection() {
		try {
			if (sessionID == null) {
				client.connect(hostname, port, username, password, groupID);
			} else {
				client.connect(hostname, port, sessionID);
			}
		} catch (ServiceException exp) {
			LOGGER.error("OMERO connection error: {}", exp.getMessage(), exp);
			exit(1);
		}
	}
	
	
	public void run()
	throws AccessException, ServiceException, OMEROServerError, IOException,
	       ExecutionException, InterruptedException, FormatException {
		switch (cmd.getOptionValue("action")) {
			case "autocrop":
				runAutoCropOMERO();
				break;
			case "segmentation":
				runSegmentationOMERO();
				break;
			case "generateOverlay":
				runGenerateOV();
				break;
			case "cropFromCoordinate":
				runCropFromCoordinate();
				break;
			case "computeParameters":
				runComputeNucleiParameters();
				break;
			case "segCC":
				runSegCC();
				break;
			case "computeCcParameters":
				runComputeCC();
				break;
			default:
				throw new IllegalArgumentException("Invalid action");
		}
		client.disconnect();
	}
	
	
	private void runComputeCC() {
		ChromocenterAnalysis ccAnalysis = new ChromocenterAnalysis();
		if (cmd.hasOption("rhf")) {
			ccAnalysis.isRHFVolumeAndIntensity = cmd.getOptionValue("rhf");
		}
		if (cmd.hasOption("obj")) {
			ccAnalysis.isNucAndCcAnalysis = cmd.getOptionValue("obj");
		}
		
		if (cmd.hasOption("calibration")) {
			ccAnalysis.calibration = true;
		}
		if (cmd.hasOption("cX")) {
			ccAnalysis.xCalibration = Double.parseDouble(cmd.getOptionValue("cX"));
		}
		if (cmd.hasOption("cY")) {
			ccAnalysis.yCalibration = Double.parseDouble(cmd.getOptionValue("cY"));
		}
		if (cmd.hasOption("cZ")) {
			ccAnalysis.zCalibration = Double.parseDouble(cmd.getOptionValue("cZ"));
		}
		if (cmd.hasOption("unit")) {
			ccAnalysis.unit = cmd.getOptionValue("unit");
		}
		
		String inputDirectory = cmd.getOptionValue("input");
		String segDirectory   = cmd.getOptionValue("input2");
		String ccDirectory    = cmd.getOptionValue("input3");
		
		try {
			LOGGER.info("-Input Folder : {} -Segmentation Folder : {} -Chromocenters folder : {}",
			            inputDirectory, segDirectory, ccDirectory);
			ccAnalysis.runComputeParametersCC(inputDirectory, segDirectory, ccDirectory, client);
		} catch (AccessException | ServiceException | ExecutionException e) {
			LOGGER.error("An error occurred while computing chromocenter parameters.", e);
		}
		LOGGER.info("End !!! Results available:");
	}
	
	
	private void runSegCC() {
		ChromocenterParameters chromocenterParameters = new ChromocenterParameters(".", ".", ".");
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
			chromocenterParameters.gaussianBlurXsigma = Double.parseDouble(cmd.getOptionValue("gX"));
		}
		
		if (cmd.hasOption("gY")) {
			chromocenterParameters.gaussianBlurYsigma = Double.parseDouble(cmd.getOptionValue("gY"));
		}
		
		if (cmd.hasOption("gZ")) {
			chromocenterParameters.gaussianBlurZsigma = Double.parseDouble(cmd.getOptionValue("gZ"));
		}
		
		if (cmd.hasOption("min")) {
			chromocenterParameters.minSizeConnectedComponent = Double.parseDouble(cmd.getOptionValue("min"));
		}
		if (cmd.hasOption("max")) {
			chromocenterParameters.maxSizeConnectedComponent = Double.parseDouble(cmd.getOptionValue("max"));
		}
		if (cmd.hasOption("f")) {
			chromocenterParameters.factor = Double.parseDouble(cmd.getOptionValue("f"));
		}
		if (cmd.hasOption("n")) {
			chromocenterParameters.neighbours = Integer.parseInt(cmd.getOptionValue("n"));
		}
		
		ChromocenterCalling ccCalling = new ChromocenterCalling(chromocenterParameters);
		
		String inputDirectory  = cmd.getOptionValue("input");
		String segDirectory    = cmd.getOptionValue("input2");
		String outputDirectory = cmd.getOptionValue("output");
		
		try {
			LOGGER.info("-Input Folder : {} -Segmentation Folder : {} -Output : {}",
			            inputDirectory, segDirectory, outputDirectory);
			ccCalling.segmentationOMERO(inputDirectory, segDirectory, outputDirectory, client);
		} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
			LOGGER.error("An error occurred during chromocenter segmentation.", e);
		} catch (InterruptedException e) {
			LOGGER.error("An interruption occurred during chromocenter segmentation.", e);
			Thread.currentThread().interrupt();
		}
		LOGGER.info("End !!! Results available: {}", chromocenterParameters.getOutputFolder());
	}
	
	
	private void runAutoCropOMERO()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, InterruptedException {
		AutocropParameters autocropParameters = new AutocropParameters(".", ".");
		if (cmd.hasOption("config")) {
			autocropParameters.addGeneralProperties(cmd.getOptionValue("config"));
			autocropParameters.addProperties(cmd.getOptionValue("config"));
		}
		AutoCropCalling autoCrop = new AutoCropCalling(autocropParameters);
		if (cmd.hasOption("thresholding")) {
			autoCrop.setTypeThresholding(cmd.getOptionValue("thresholding"));
		}
		
		// add setter here !!!!
		if (cmd.hasOption("threads")) {
			autoCrop.setExecutorThreads(Integer.parseInt(cmd.getOptionValue("threads")));
			LOGGER.info("Threads set to: {}", cmd.getOptionValue("threads"));
		}
		try {
			autoCropOMERO(cmd.getOptionValue("input"),
			              cmd.getOptionValue("output"),
			              client,
			              autoCrop);
		} catch (IllegalArgumentException exp) {
			LOGGER.error(exp.getMessage(), exp);
			exit(1);
		}
	}
	
	
	public void runSegmentationOMERO()
	throws AccessException, ServiceException, ExecutionException, OMEROServerError {
		SegmentationParameters segmentationParameters = new SegmentationParameters(".", ".");
		if (cmd.hasOption("config")) {
			segmentationParameters.addGeneralProperties(cmd.getOptionValue("config"));
			segmentationParameters.addProperties(cmd.getOptionValue("config"));
		}
		SegmentationCalling otsuModified = new SegmentationCalling(segmentationParameters);
		if (cmd.hasOption("threads")) {
			otsuModified.setExecutorThreads(Integer.parseInt(cmd.getOptionValue("threads")));
		}
		segmentationOMERO(cmd.getOptionValue("input"),
		                  cmd.getOptionValue("output"),
		                  client,
		                  otsuModified);
	}
	
	
	private void runGenerateOV()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException {
		GenerateOverlay ov = new GenerateOverlay();
		ov.runFromOMERO(cmd.getOptionValue("input"),
		                cmd.getOptionValue("input2"),
		                cmd.getOptionValue("output"),
		                client);
	}
	
	
	private void runCropFromCoordinate()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, FormatException {
		CropFromCoordinates cropFromCoordinates = new CropFromCoordinates(cmd.getOptionValue("input"));
		cropFromCoordinates.runFromOMERO(cmd.getOptionValue("input2"),
		                                 cmd.getOptionValue("output"),
		                                 client);
	}
	
	
	private void runComputeNucleiParameters()
	throws AccessException, ServiceException, IOException, ExecutionException, InterruptedException {
		ComputeNucleiParameters generateParameters = new ComputeNucleiParameters();
		if (cmd.hasOption("config")) {
			generateParameters.addConfigParameters(cmd.getOptionValue("config"));
		}
		generateParameters.runFromOMERO(cmd.getOptionValue("input"),
		                                cmd.getOptionValue("input2"),
		                                client);
	}
	
}
