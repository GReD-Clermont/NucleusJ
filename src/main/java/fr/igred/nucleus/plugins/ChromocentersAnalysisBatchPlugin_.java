package fr.igred.nucleus.plugins;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.nucleus.core.ChromocenterAnalysis;
import fr.igred.nucleus.core.NucleusChromocentersAnalysis;
import fr.igred.nucleus.dialogs.ChromocentersAnalysisPipelineBatchDialog;
import fr.igred.nucleus.dialogs.IDialogListener;
import fr.igred.nucleus.io.FileList;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


/**
 * @author Tristan Dubos and Axel Poulet
 */
public class ChromocentersAnalysisBatchPlugin_ implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private ChromocentersAnalysisPipelineBatchDialog chromocentersPipelineBatchDialog;
	
	
	public static void deleteFolder(String folderPathString) throws IOException {
		Path folderPath = Paths.get(folderPathString);  // Convert String to Path
		
		// Get a stream of files and folders in reverse order (so that files are deleted before directories)
		try (Stream<Path> walk = Files.walk(folderPath)) {
			walk.sorted(Comparator.reverseOrder())  // Sort in reverse order so files are deleted before directories
			    .forEach(path -> {
				    try {
					    Files.delete(path);  // Delete each file/folder
				    } catch (IOException e) {
					    throw new RuntimeException("Failed to delete " + path,
					                               e);  // Handle any exceptions during deletion
				    }
			    });
		}
	}
	
	
	/** Run the the analyse, call the graphical windows */
	/* This method is used by plugins.config */
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		chromocentersPipelineBatchDialog = new ChromocentersAnalysisPipelineBatchDialog(this);
	}
	
	
	@Override
	public void onStart() throws AccessException, ServiceException, ExecutionException {
		if (chromocentersPipelineBatchDialog.isOmeroEnabled()) {
			runOMERO();
		} else {
			String file = chromocentersPipelineBatchDialog.getRawDataDirectory();
			if (file == null || file.isEmpty()) {
				IJ.error("Input file or directory is missing");
			} else {
				try {
					LOGGER.info("Begin Chromocenter Analysis  ");
					
					runCCAnalysis(chromocentersPipelineBatchDialog.getRawDataDirectory(),
					              chromocentersPipelineBatchDialog.getWorkDirectory());
					
					LOGGER.info("Chromocenter Analysis has ended successfully");
				} catch (Exception e) {
					LOGGER.info("Chromocenter Analysis has failed");
					LOGGER.error("An error occurred.", e);
				}
			}
		}
	}
	
	
	public static Client checkOMEROConnection(String hostname,
	                                          String port,
	                                          String username,
	                                          char[] password,
	                                          String group) {
		Client client = new Client();
		try {
			client.connect(hostname,
			               Integer.parseInt(port),
			               username,
			               password,
			               Long.valueOf(group));
		} catch (ServiceException | NumberFormatException exp) {
			IJ.error("Invalid connection values");
			return null;
		}
		return client;
	}
	
	
	private void runOMERO() throws AccessException, ServiceException, ExecutionException {
		// Check connection
		String hostname = chromocentersPipelineBatchDialog.getHostname();
		String username = chromocentersPipelineBatchDialog.getUsername();
		String password = chromocentersPipelineBatchDialog.getPassword();
		String port     = chromocentersPipelineBatchDialog.getPort();
		String group    = chromocentersPipelineBatchDialog.getGroup();
		
		String sourceDatatype = chromocentersPipelineBatchDialog.getDataType();
		String segDatatype    = chromocentersPipelineBatchDialog.getDataTypeSeg();
		String ccDatatype     = chromocentersPipelineBatchDialog.getDataTypeCC();
		
		Long inputID = Long.valueOf(chromocentersPipelineBatchDialog.getSourceID());
		Long segID   = Long.valueOf(chromocentersPipelineBatchDialog.getSegID());
		Long ccID    = Long.valueOf(chromocentersPipelineBatchDialog.getCcID());
		
		Client client = checkOMEROConnection(hostname, port, username, password.toCharArray(), group);
		
		String mainFolder = null;
		
		DatasetWrapper outDataset;
		try {
			// Check if all data types are "Image"
			if ("Image".equals(sourceDatatype) && "Image".equals(segDatatype) && "Image".equals(ccDatatype)) {
				mainFolder = processAndAnalyze(client, inputID, segID, ccID);
			}
			// Check if all data types are "Dataset"
			else if ("Dataset".equals(sourceDatatype) && "Dataset".equals(segDatatype) &&
			         "Dataset".equals(ccDatatype)) {
				long sourceImageId;
				long segImageId;
				long ccImageId;
				
				DatasetWrapper sourceDataset = client.getDataset(inputID);
				DatasetWrapper segDataset    = client.getDataset(segID);
				DatasetWrapper ccDataset     = client.getDataset(ccID);
				
				List<ImageWrapper> sourceImages = sourceDataset.getImages(client);
				List<ImageWrapper> segImages    = segDataset.getImages(client);
				List<ImageWrapper> ccImages     = ccDataset.getImages(client);
				
				if (sourceImages.size() != segImages.size() || sourceImages.size() != ccImages.size()) {
					throw new IllegalStateException("Image lists are of unequal size.");
				}
				
				try {
					LOGGER.info("Begin Chromocenter Analysis");
					for (int i = 0; i < sourceImages.size(); i++) {
						sourceImageId = sourceImages.get(i).getId();
						segImageId = segImages.get(i).getId();
						ccImageId = ccImages.get(i).getId();
						
						mainFolder = processAndAnalyze(client, sourceImageId, segImageId, ccImageId);
					}
					LOGGER.info("Chromocenter Analysis has ended successfully");
				} catch (AccessException | ServiceException | IOException | ExecutionException e) {
					LOGGER.info("Chromocenter Analysis has failed");
					LOGGER.error("An error occurred.", e);
				}
			}
			// If none of the conditions match
			else {
				LOGGER.error("Unsupported data types: sourceDatatype = {}, segDatatype = {}, ccDatatype = {}",
				             sourceDatatype, segDatatype, ccDatatype);
				LOGGER.error("Please ensure that all data types are either 'Image' or 'Dataset'.");
				return;  // Stop further execution
			}
			Path ccFilePath  = Paths.get(mainFolder, "CcParameters.tab");
			Path nucFilePath = Paths.get(mainFolder, "NucAndCcParameters.tab");
			if (Files.exists(ccFilePath) && Files.isRegularFile(ccFilePath)) {
				outDataset = client.getDataset(ccID);
				File ccTabFile = new File(String.valueOf(ccFilePath));
				outDataset.addFile(client, ccTabFile);
			}
			if (Files.exists(nucFilePath) && Files.isRegularFile(nucFilePath)) {
				File nucTabFile = new File(String.valueOf(nucFilePath));
				outDataset = client.getDataset(segID);
				outDataset.addFile(client, nucTabFile);
			}
			deleteFolder(mainFolder);
			LOGGER.info("Segmentation process has ended successfully");
			IJ.showMessage("Segmentation process ended successfully on " +
			               chromocentersPipelineBatchDialog.getDataType() + "\\" + inputID);
			
		} catch (ServiceException se) {
			IJ.error("Unable to access OMERO service.");
		} catch (AccessException ae) {
			IJ.error("Cannot access " + sourceDatatype + " with ID = " + inputID + ".");
		} catch (IOException | ExecutionException e) {
			LOGGER.error("An error occurred.", e);
		} catch (InterruptedException e) {
			LOGGER.error("Chromocenter Analysis interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	private String processAndAnalyze(Client client, Long sourceImageId, Long segImageId, Long ccImageId)
	throws AccessException, ServiceException, ExecutionException, IOException {
		String ccInputDir = fileDownloader(client, ccImageId, "SegmentedDataCc");
		fileDownloader(client, sourceImageId, "RawDataNucleus");
		fileDownloader(client, segImageId, "SegmentedDataNucleus");
		
		runCCAnalysis(ccInputDir, ccInputDir);
		return ccInputDir;
	}
	
	
	private static String fileDownloader(Client client, Long inputID, String subDirectoryName)
	throws AccessException, ServiceException, ExecutionException, IOException {
		ImageWrapper image = client.getImage(inputID);
		// Get the name of the Image
		String imageName = image.getName();
		// Get the image
		ImagePlus imp = image.toImagePlus(client);
		
		// Define the relative main directory path
		String mainDirectoryPath = "ccAnalysisDirectory";  // Relative to the working directory
		
		// Ensure the main directory exists (relative to current working directory)
		File mainDirectory = new File(mainDirectoryPath);
		if (!mainDirectory.exists()) {
			if (mainDirectory.mkdirs()) {
				LOGGER.info("Main directory created: {}", mainDirectory.getAbsolutePath());
			} else {
				throw new IOException("Failed to create main directory: " + mainDirectoryPath);
			}
		}
		
		// Create the subdirectory path within the main directory
		File subDirectory = new File(mainDirectoryPath + File.separator + subDirectoryName);
		if (!subDirectory.exists()) {
			if (subDirectory.mkdirs()) {
				LOGGER.info("Subdirectory created: {}", subDirectory.getAbsolutePath());
			} else {
				throw new IOException("Failed to create subdirectory: " + subDirectory.getAbsolutePath());
			}
		}
		
		// Generate the path for the output file in the subdirectory
		String outputFilePath = subDirectory.getAbsolutePath() + File.separator + imageName;
		
		// Save the file as TIF in the subdirectory
		FileSaver fileSaver = new FileSaver(imp);
		fileSaver.saveAsTiff(outputFilePath);
		
		// Verify if the file was successfully saved
		File resultFile = new File(outputFilePath);
		if (resultFile.exists()) {
			LOGGER.info("File saved at: {}", resultFile.getAbsolutePath());
		} else {
			throw new IOException("File saving failed: " + outputFilePath);
		}
		return mainDirectory.getAbsolutePath();
	}
	
	
	private void runCCAnalysis(String rawDataDir, String workDirectory) {
		FileList fileList      = new FileList();
		File[]   tFileRawImage = fileList.run(rawDataDir);
		
		if (fileList.isDirectoryOrFileExist(".+RawDataNucleus.+", tFileRawImage) &&
		    fileList.isDirectoryOrFileExist(".+SegmentedDataNucleus.+", tFileRawImage) &&
		    fileList.isDirectoryOrFileExist(".+SegmentedDataCc.+", tFileRawImage)) {
			String rhfChoice;
			if (chromocentersPipelineBatchDialog.isRHFVolumeAndIntensity()) {
				rhfChoice = "Volume and intensity";
			} else if (chromocentersPipelineBatchDialog.isRhfVolume()) {
				rhfChoice = "Volume";
			} else {
				rhfChoice = "Intensity";
			}
			
			List<String> listImageChromocenter = fileList.fileSearchList(".+SegmentedDataCc.+", tFileRawImage);
			
			String nameFileChromocenterAndNucleus = workDirectory + File.separator + "NucAndCcParameters.tab";
			String nameFileChromocenter           = workDirectory + File.separator + "CcParameters.tab";
			
			for (int i = 0; i < listImageChromocenter.size(); ++i) {
				LOGGER.info("image {}/{}", i + 1, listImageChromocenter.size());
				String pathImageChromocenter = listImageChromocenter.get(i);
				
				String pathNucleusRaw = pathImageChromocenter.replace("SegmentedDataCc", "RawDataNucleus");
				LOGGER.info(pathNucleusRaw);
				String pathNucleusSegmented = pathImageChromocenter.replace("SegmentedDataCc", "SegmentedDataNucleus");
				LOGGER.info(pathNucleusSegmented);
				if (fileList.isDirectoryOrFileExist(pathNucleusRaw, tFileRawImage) &&
				    fileList.isDirectoryOrFileExist(pathNucleusSegmented, tFileRawImage)) {
					ImagePlus imagePlusInput = IJ.openImage(pathNucleusRaw);
					if (imagePlusInput.getType() == ImagePlus.GRAY32) {
						IJ.error("image format", "No images in gray scale 8bits in 3D");
						return;
					}
					ImagePlus   imagePlusChromocenter = IJ.openImage(listImageChromocenter.get(i));
					ImagePlus   imagePlusSegmented    = IJ.openImage(pathNucleusSegmented);
					Calibration calibration           = new Calibration();
					if (chromocentersPipelineBatchDialog.getCalibrationStatus()) {
						calibration.pixelWidth = chromocentersPipelineBatchDialog.getXCalibration();
						calibration.pixelHeight = chromocentersPipelineBatchDialog.getYCalibration();
						calibration.pixelDepth = chromocentersPipelineBatchDialog.getZCalibration();
						calibration.setUnit(chromocentersPipelineBatchDialog.getUnit());
					} else {
						calibration = imagePlusInput.getCalibration();
					}
					imagePlusChromocenter.setCalibration(calibration);
					imagePlusSegmented.setCalibration(calibration);
					imagePlusInput.setCalibration(calibration);
					try {
						if (chromocentersPipelineBatchDialog.isNucAndCcAnalysis()) {
							ChromocenterAnalysis.computeParametersChromocenter(nameFileChromocenter,
							                                                   imagePlusSegmented,
							                                                   imagePlusChromocenter);
							LOGGER.info("chromocenterAnalysis is computing...");
							LOGGER.info("nucleusChromocenterAnalysis is computing...");
							NucleusChromocentersAnalysis.computeParameters(nameFileChromocenterAndNucleus,
							                                               rhfChoice,
							                                               imagePlusInput,
							                                               imagePlusSegmented,
							                                               imagePlusChromocenter);
						} else if (chromocentersPipelineBatchDialog.isCcAnalysis()) {
							ChromocenterAnalysis.computeParametersChromocenter(nameFileChromocenter,
							                                                   imagePlusSegmented,
							                                                   imagePlusChromocenter);
						} else {
							NucleusChromocentersAnalysis.computeParameters(nameFileChromocenterAndNucleus,
							                                               rhfChoice,
							                                               imagePlusInput,
							                                               imagePlusSegmented,
							                                               imagePlusChromocenter);
						}
					} catch (IOException e) {
						LOGGER.error("An error occurred.", e);
					}
				} else {
					LOGGER.info("Image name problem: the image {} is not found " +
					            "in the directory SegmentedDataNucleus or RawDataNucleus, " +
					            "see nameProblem.txt in {}",
					            pathImageChromocenter,
					            workDirectory);
					try (BufferedWriter bufferedWriterLogFile = new BufferedWriter(new FileWriter(workDirectory +
					                                                                              File.separator +
					                                                                              "logNameProblem.log",
					                                                                              true))) {
						bufferedWriterLogFile.write(pathImageChromocenter + "\n");
						bufferedWriterLogFile.flush();
					} catch (IOException e) {
						LOGGER.error("An error occurred.", e);
					}
				}
			}
			LOGGER.info("End of the chromocenter analysis , the results are in {}",
			            chromocentersPipelineBatchDialog.getWorkDirectory());
		} else {
			IJ.showMessage("There are no three subdirectories (See the directory name) or subDirectories are empty");
		}
		
	}
	
}