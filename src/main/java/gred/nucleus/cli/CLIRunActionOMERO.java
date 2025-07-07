package gred.nucleus.cli;

import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import gred.nucleus.autocrop.AutoCropCalling;
import gred.nucleus.autocrop.AutocropParameters;
import gred.nucleus.autocrop.CropFromCoordinates;
import gred.nucleus.autocrop.GenerateOverlay;
import gred.nucleus.core.ChromocenterAnalysis;
import gred.nucleus.core.ComputeNucleiParameters;
import gred.nucleus.plugins.ChromocenterParameters;
import gred.nucleus.process.ChromocenterCalling;
import gred.nucleus.segmentation.SegmentationCalling;
import gred.nucleus.segmentation.SegmentationParameters;
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
	private CommandLine cmd;

	/** OMERO client information see fr.igred.omero.Client */
	private Client      client  = new Client();

	/** OMERO server hostname */
	private String hostname;
	/** OMERO username*/
	private String username;
	/** OMERO server port */
	private int port;
	/** OMERO groupe ID */
	private long groupID;
	/** OMERO password connection */
	private char[] password;
	/** OMERO session ID */
	private String sessionID = null;
	
	
	public CLIRunActionOMERO(CommandLine cmd) {
		this.cmd = cmd;
		if(this.cmd.hasOption("omeroConfig")){
			addLoginCredentials(this.cmd.getOptionValue("omeroConfig"));
		}
		else {
			this.hostname = this.cmd.getOptionValue("hostname");
			this.port =	Integer.parseInt(this.cmd.getOptionValue("port"));
			this.username = this.cmd.getOptionValue("username");
			getOMEROPassword();
			this.groupID = Long.parseLong(this.cmd.getOptionValue("group"));
		}
		checkOMEROConnection();
	}

	public void addLoginCredentials(String pathToConfigFile) {
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
				}
			} catch (NumberFormatException nfe){
				LOGGER.error("OMERO config error : Port and groupID must be number");
				exit(1);
			}
		}
	}


	public void getOMEROPassword() {
		if (this.cmd.hasOption("password")) {
			this.password = this.cmd.getOptionValue("password").toCharArray();
		} else {
			System.console().writer().println("Enter password: ");
			Console con = System.console();
			this.password = con.readPassword();
		}
	}


	public void checkOMEROConnection() {
		try {
			if(sessionID == null) client.connect(hostname, port, username, password, groupID);
			else client.connect(hostname, port, sessionID);
		} catch (Exception exp) {
			LOGGER.error("OMERO connection error: " + exp.getMessage(), exp);
			exit(1);
		}
	}
	
	
	public void run()
	throws AccessException, ServiceException, OMEROServerError, IOException,
	       ExecutionException, InterruptedException, FormatException {
		switch (this.cmd.getOptionValue("action")) {
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
		this.client.disconnect();
	}

	private void runComputeCC() {
		ChromocenterAnalysis ccAnalysis = new ChromocenterAnalysis();
		if (this.cmd.hasOption("rhf")) ccAnalysis.isRHFVolumeAndIntensity = cmd.getOptionValue("rhf");
		if (this.cmd.hasOption("obj")) ccAnalysis.isNucAndCcAnalysis=  cmd.getOptionValue("obj");

		if (this.cmd.hasOption("calibration")) ccAnalysis._calibration = true;
		if (this.cmd.hasOption("cX")) ccAnalysis._Xcalibration=   Double.parseDouble(cmd.getOptionValue("cX"));
		if (this.cmd.hasOption("cY")) ccAnalysis._Ycalibration=   Double.parseDouble(cmd.getOptionValue("cY"));
		if (this.cmd.hasOption("cZ")) ccAnalysis._Zcalibration=  Double.parseDouble(cmd.getOptionValue("cZ"));
		if (this.cmd.hasOption("unit")) ccAnalysis._unit=  cmd.getOptionValue("unit");

		String inputDirectory = this.cmd.getOptionValue("input");
		String segDirectory = this.cmd.getOptionValue("input2");
		String ccDirectory = this.cmd.getOptionValue("input3");
		
		try {
			LOGGER.info("-Input Folder : {} -Segmentation Folder : {} -Chromocenters folder : {}", inputDirectory, segDirectory, ccDirectory);
			ccAnalysis.runComputeParametersCC(inputDirectory, segDirectory, ccDirectory, this.client);
		} catch (Exception e) {
			LOGGER.error("An error occurred while computing chromocenter parameters.", e);
		}
		LOGGER.info("End !!! Results available:");
	}

	private void runSegCC() {
		ChromocenterParameters chromocenterParameters = new ChromocenterParameters(".", ".",".");
		if (this.cmd.hasOption("isG")) chromocenterParameters.gaussianOnRaw = true;
		if (this.cmd.hasOption("isF")) chromocenterParameters.sizeFilterConnectedComponent = true;
		if (this.cmd.hasOption("noC")) chromocenterParameters.noChange = true;
		if (this.cmd.hasOption("gX"))
			chromocenterParameters.gaussianBlurXsigma =  Double.parseDouble(cmd.getOptionValue("gX"));

		if (this.cmd.hasOption("gY"))
			chromocenterParameters.gaussianBlurYsigma =  Double.parseDouble(cmd.getOptionValue("gY"));

		if (this.cmd.hasOption("gZ"))
			chromocenterParameters.gaussianBlurZsigma =  Double.parseDouble(cmd.getOptionValue("gZ"));

		if (this.cmd.hasOption("min"))
			chromocenterParameters.minSizeConnectedComponent =  Double.parseDouble(cmd.getOptionValue("min"));
		if (this.cmd.hasOption("max"))

			chromocenterParameters.maxSizeConnectedComponent =  Double.parseDouble(cmd.getOptionValue("max"));
		if (this.cmd.hasOption("f"))
			chromocenterParameters.factor =  Double.parseDouble(cmd.getOptionValue("f"));
		if (this.cmd.hasOption("n"))
			chromocenterParameters.neighbours =  Integer.parseInt(cmd.getOptionValue("n"));

		ChromocenterCalling ccCalling= new ChromocenterCalling(chromocenterParameters);

		String inputDirectory = this.cmd.getOptionValue("input");
		String segDirectory = this.cmd.getOptionValue("input2");
		String outputDirectory = this.cmd.getOptionValue("output");
		
		try {
			LOGGER.info("-Input Folder : {} -Segmentation Folder : {} -Output : {}", inputDirectory, segDirectory, outputDirectory);
			ccCalling.SegmentationOMERO(inputDirectory,segDirectory,outputDirectory,this.client);
		} catch (AccessException | OMEROServerError |ServiceException | IOException | ExecutionException e) {
			LOGGER.error("An error occurred during chromocenter segmentation.", e);
		} catch (InterruptedException e) {
			LOGGER.error("An interruption occurred during chromocenter segmentation.", e);
			Thread.currentThread().interrupt();
		}
		LOGGER.info("End !!! Results available: {}", chromocenterParameters.outputFolder);


	}
	
	public static void autoCropOMERO(String inputDirectory,
	                                 String outputDirectory,
	                                 Client client,
	                                 AutoCropCalling autoCrop)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException, InterruptedException {
		String[] param = inputDirectory.split("/");
		
		if (param.length >= 2) {
			Long id = Long.parseLong(param[1]);
			if ("image".equals(param[0])) {
				ImageWrapper image = client.getImage(id);
				
				int sizeC = image.getPixels().getSizeC();
				
				Long[] outputsDat = new Long[sizeC];
				
				for (int i = 0; i < sizeC; i++) {
					DatasetWrapper dataset = new DatasetWrapper("C" + i + "_" + image.getName(), "");
					outputsDat[i] =
							client.getProject(Long.parseLong(outputDirectory)).addDataset(client, dataset).getId();
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
					DatasetWrapper dataset = new DatasetWrapper("raw_C" + i + "_" + name, "");
					outputsDat[i] =
							client.getProject(Long.parseLong(outputDirectory)).addDataset(client, dataset).getId();
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


	private void runAutoCropOMERO()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, InterruptedException {
		AutocropParameters autocropParameters = new AutocropParameters(".", ".");
		if (this.cmd.hasOption("config")) {
			autocropParameters.addGeneralProperties(this.cmd.getOptionValue("config"));
			autocropParameters.addProperties(this.cmd.getOptionValue("config"));
		}
		AutoCropCalling autoCrop = new AutoCropCalling(autocropParameters);
		if (this.cmd.hasOption("thresholding")) {
			autoCrop.setTypeThresholding(this.cmd.getOptionValue("thresholding"));
		}

		// add setter here !!!!
		if(this.cmd.hasOption("threads")) {
			autoCrop.setExecutorThreads(Integer.parseInt(this.cmd.getOptionValue("threads")));
			LOGGER.info("Threads set to: {}", this.cmd.getOptionValue("threads"));
		}
		try {
			autoCropOMERO(this.cmd.getOptionValue("input"),
			              this.cmd.getOptionValue("output"),
			              this.client,
			              autoCrop);
		} catch (IllegalArgumentException exp) {
			LOGGER.error(exp.getMessage(), exp);
			exit(1);
		}
	}
	
	
	public void runSegmentationOMERO()
	throws AccessException, ServiceException, ExecutionException, OMEROServerError {
		SegmentationParameters segmentationParameters = new SegmentationParameters(".", ".");
		if (this.cmd.hasOption("config")) {
			segmentationParameters.addGeneralProperties(this.cmd.getOptionValue("config"));
			segmentationParameters.addProperties(this.cmd.getOptionValue("config"));
		}
		SegmentationCalling otsuModified = new SegmentationCalling(segmentationParameters);
		if(this.cmd.hasOption("threads")) {
			otsuModified.setExecutorThreads(Integer.parseInt(this.cmd.getOptionValue("threads")));
		}
		segmentationOMERO(this.cmd.getOptionValue("input"),
		                  this.cmd.getOptionValue("output"),
		                  this.client,
		                  otsuModified);
	}
	
	
	public void segmentationOMERO(String inputDirectory,
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
						log = otsuModified.runSeveralImagesOMERO(images, Long.parseLong(outputDirectory), client,id);
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


	private void runGenerateOV()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException {
		GenerateOverlay ov = new GenerateOverlay();
		ov.runFromOMERO(this.cmd.getOptionValue("input"),
						this.cmd.getOptionValue("input2"),
						this.cmd.getOptionValue("output"),
						this.client
		);
	}

	private void runCropFromCoordinate()
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, FormatException {
		CropFromCoordinates cropFromCoordinates = new CropFromCoordinates(
				this.cmd.getOptionValue("input")
		);
		cropFromCoordinates.runFromOMERO(
				this.cmd.getOptionValue("input2"),
				this.cmd.getOptionValue("output"),
				this.client
		);
	}

	private void runComputeNucleiParameters() throws AccessException, ServiceException, IOException, ExecutionException, InterruptedException {
		ComputeNucleiParameters generateParameters = new ComputeNucleiParameters();
		if (this.cmd.hasOption("config")) generateParameters.addConfigParameters(this.cmd.getOptionValue("config"));
		generateParameters.runFromOMERO(
				this.cmd.getOptionValue("input"),
				this.cmd.getOptionValue("input2"),
				client
		);
	}
}
