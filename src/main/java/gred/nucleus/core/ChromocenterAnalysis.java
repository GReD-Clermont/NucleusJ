package gred.nucleus.core;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import gred.nucleus.utils.FileList;
import gred.nucleus.utils.Histogram;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
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
 * Several method to realise and create the outfile for the chromocenter Analysis
 *
 * @author Tristan Dubos and Axel Poulet
 */
public final class ChromocenterAnalysis {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Gaussian parameters  */
	public boolean _calibration = false;
	public String isRHFVolumeAndIntensity = null;
	public String isNucAndCcAnalysis = null;
	public double _Xcalibration;
	public double _Ycalibration;
	public double _Zcalibration;
	public String _unit;
	public ChromocenterAnalysis() {
	}
	
	
	/**
	 * Compute the several parameters to characterize the chromocenter of one image, and return the results on the IJ
	 * log windows
	 *
	 * @param imagePlusSegmented    image of the segmented nucleus
	 * @param imagePlusChromocenter image of the segmented chromocenter
	 */
	public static void computeParametersChromocenter(ImagePlus imagePlusSegmented, ImagePlus imagePlusChromocenter) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		Measure3D measure3D = new Measure3D();
		double[]  tVolume   = measure3D.computeVolumeOfAllObjects(imagePlusChromocenter);
		LOGGER.info("CHROMOCENTER PARAMETERS");
		LOGGER.info("Titre Volume BorderToBorderDistance BarycenterToBorderDistance BarycenterToBorderDistanceNucleus ");
		if (histogram.getNbLabels() > 0) {
			double[] tBorderToBorderDistanceTable =
					RadialDistance.computeBorderToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
			double[] tBarycenterToBorderDistanceTable =
					RadialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
			double[] tBarycenterToBorderDistanceTableNucleus =
					RadialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusSegmented);
			for (int i = 0; i < tBorderToBorderDistanceTable.length; ++i) {
				LOGGER.info("{}_{} {} {} {} {}",
				            imagePlusChromocenter.getTitle(),
				            i,
				            tVolume[i],
				            tBorderToBorderDistanceTable[i],
				            tBarycenterToBorderDistanceTable[i],
				            tBarycenterToBorderDistanceTableNucleus[0]);
			}
		}
	}
	
	
	/**
	 * Compute the several parameters to characterize the chromocenter of several images, and create one output file for
	 * the results
	 *
	 * @param pathResultsFile       path for the output file
	 * @param imagePlusSegmented    image of the segmented nucleus
	 * @param imagePlusChromocenter image of the chromocenter segmented
	 *
	 * @throws IOException if file doesn't exist catch the exception
	 */
	public static void computeParametersChromocenter(String pathResultsFile,
	                                                 ImagePlus imagePlusSegmented,
	                                                 ImagePlus imagePlusChromocenter) throws IOException {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		if (histogram.getNbLabels() > 0) {
			File    fileResults = new File(pathResultsFile);
			boolean exist       = fileResults.exists();
			try (BufferedWriter bufferedWriterOutput = new BufferedWriter(new FileWriter(fileResults, true))) {
				Measure3D measure3D = new Measure3D(imagePlusChromocenter.getCalibration().pixelWidth,
				                                    imagePlusChromocenter.getCalibration().pixelHeight,
				                                    imagePlusChromocenter.getCalibration().pixelDepth);
				double[] tVolume =
						measure3D.computeVolumeOfAllObjects(imagePlusChromocenter);
				double[] tBorderToBorderDistanceTable =
						RadialDistance.computeBorderToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
				double[] tBarycenterToBorderDistanceTableCc =
						RadialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
				double[] tBarycenterToBorderDistanceTableNucleus =
						RadialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusSegmented);
				if (!exist) {
					bufferedWriterOutput.write(
							"Titre\tVolume\tBorderToBorderDistance\tBarycenterToBorderDistance\tBarycenterToBorderDistanceNucleus\n");
				}
				for (
						int i = 0;
						i < tBorderToBorderDistanceTable.length; ++i) {
					bufferedWriterOutput.write(
							imagePlusChromocenter.getTitle() + "_" + i + "\t"
							+ tVolume[i] + "\t"
							+ tBorderToBorderDistanceTable[i] + "\t"
							+ tBarycenterToBorderDistanceTableCc[i] + "\t"
							+ tBarycenterToBorderDistanceTableNucleus[0] + "\n"
					                          );
				}
				bufferedWriterOutput.flush();
			}
		}
	}


	public void runComputeParametersCC(String rawInput, String segInput, String ccInput,Client client) throws AccessException, ServiceException, ExecutionException {
		// Check connection
		/* Get  image or Dataset ID */
		String[] rawParam = rawInput.split("/");
		String[] segParam = segInput.split("/");
		String[] ccParam = ccInput.split("/");

		Long inputID = Long.parseLong(rawParam[1]);
		Long segID = Long.parseLong(segParam[1]);
		Long ccID = Long.parseLong(ccParam[1]);

		String sourceDatatype = rawParam[0];
		String segDatatype = segParam[0];
		String ccDatatype = ccParam[0];

		String mainFolder = null;
		DatasetWrapper outDataset;
		try {
			// Check if all data types are "Image"
			if ("Image".equals(sourceDatatype) && "Image".equals(segDatatype) && "Image".equals(ccDatatype)) {
				mainFolder = processAndAnalyze(client, inputID, segID, ccID);
			}
			// Check if all data types are "Dataset"
			else if ("Dataset".equals(sourceDatatype) && "Dataset".equals(segDatatype) && "Dataset".equals(ccDatatype)) {
				Long sourceImageId, segImageId, ccImageId;
				DatasetWrapper sourceDataset = client.getDataset(inputID),
						segDataset = client.getDataset(segID),
						ccDataset = client.getDataset(ccID);

				List<ImageWrapper> sourceImages = sourceDataset.getImages(client);
				List<ImageWrapper> segImages = segDataset.getImages(client);
				List<ImageWrapper> ccImages = ccDataset.getImages(client);

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
				} catch (Exception e) {
					LOGGER.info("Chromocenter Analysis has failed");
					LOGGER.error("An error occurred.", e);
				}
			}
			// If none of the conditions match
			else {
				LOGGER.error("Unsupported data types: sourceDatatype = " + sourceDatatype
						+ ", segDatatype = " + segDatatype
						+ ", ccDatatype = " + ccDatatype);
				IJ.error("Unsupported data types: Please ensure that all data types are either 'Image' or 'Dataset'.");
				return;  // Stop further execution
			}
			Path ccFilePath = Paths.get(mainFolder, "CcParameters.tab");
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
			//IJ.showMessage("Segmentation process ended successfully on " + chromocentersPipelineBatchDialog.getDataType() + "\\" + inputID);

		} catch (ServiceException se) {
			IJ.error("Unable to access OMERO service.");
		} catch (AccessException ae) {
			IJ.error("Cannot access " + sourceDatatype + " with ID = " + inputID + ".");
		} catch (Exception e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	

	private String processAndAnalyze(Client client, Long sourceImageId, Long segImageId, Long ccImageId)
	throws AccessException, ServiceException, IOException, ExecutionException {
		String ccInputDir = fileDownloader(client, ccImageId, "SegmentedDataCc");
		fileDownloader(client, sourceImageId, "RawDataNucleus");
		fileDownloader(client, segImageId, "SegmentedDataNucleus");

		runCCAnalysis(ccInputDir, ccInputDir);
		return ccInputDir;
	}

	
	private String fileDownloader(Client client, Long inputID, String subDirectoryName) throws AccessException, ServiceException, ExecutionException, IOException {
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
				System.out.println("Main directory created: " + mainDirectory.getAbsolutePath());
			} else {
				throw new IOException("Failed to create main directory: " + mainDirectoryPath);
			}
		}

		// Create the subdirectory path within the main directory
		File subDirectory = new File(mainDirectoryPath + File.separator + subDirectoryName);
		if (!subDirectory.exists()) {
			if (subDirectory.mkdirs()) {
				System.out.println("Subdirectory created: " + subDirectory.getAbsolutePath());
			} else {
				throw new IOException("Failed to create subdirectory: " + subDirectory.getAbsolutePath());
			}
		}

		// Generate the path for the output file in the subdirectory
		String outputFilePath = subDirectory.getAbsolutePath() + File.separator + imageName ;

		// Save the file as TIF in the subdirectory
		FileSaver fileSaver = new FileSaver(imp);
		fileSaver.saveAsTiff(outputFilePath);

		// Verify if the file was successfully saved
		File resultFile = new File(outputFilePath);
		if (resultFile.exists()) {
			System.out.println("File saved at: " + resultFile.getAbsolutePath());
		} else {
			throw new IOException("File saving failed: " + outputFilePath);
		}
		return mainDirectory.getAbsolutePath();
	}
	

	public static void deleteFolder(String folderPathString) throws IOException {
		Path folderPath = Paths.get(folderPathString);  // Convert String to Path

		// Get a stream of files and folders in reverse order (so that files are deleted before directories)
		try (Stream<Path> walk = Files.walk(folderPath)) {
			walk.sorted(Comparator.reverseOrder())  // Sort in reverse order so files are deleted before directories
					.forEach(path -> {
						try {
							Files.delete(path);  // Delete each file/folder
						} catch (IOException e) {
							throw new RuntimeException("Failed to delete " + path, e);  // Handle any exceptions during deletion
						}
					});
		}
	}

	
	private void runCCAnalysis(String rawDataDir, String workDirectory){




		FileList fileList      = new FileList();
		File[]   tFileRawImage = fileList.run(rawDataDir);

		if (fileList.isDirectoryOrFileExist(".+RawDataNucleus.+", tFileRawImage) &&
				fileList.isDirectoryOrFileExist(".+SegmentedDataNucleus.+", tFileRawImage) &&
				fileList.isDirectoryOrFileExist(".+SegmentedDataCc.+", tFileRawImage)) {

			String rhfChoice;
			if ("volume_intensity".equals(isRHFVolumeAndIntensity)) {
				rhfChoice = "Volume and intensity";
			} else if ("volume".equals(isRHFVolumeAndIntensity)) {
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
				String pathNucleusRaw =
						pathImageChromocenter.replace("SegmentedDataCc", "RawDataNucleus");
				String pathNucleusSegmented =
						pathImageChromocenter.replace("SegmentedDataCc", "SegmentedDataNucleus");
				LOGGER.info(pathNucleusRaw);
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
					if (_calibration) {
						calibration.pixelWidth = _Xcalibration ;
						calibration.pixelHeight = _Ycalibration;
						calibration.pixelDepth = _Zcalibration;
						calibration.setUnit(_unit);
					} else {
						calibration = imagePlusInput.getCalibration();
					}
					imagePlusChromocenter.setCalibration(calibration);
					imagePlusSegmented.setCalibration(calibration);
					imagePlusInput.setCalibration(calibration);
					try {
						if ("nuc_cc".equals(isNucAndCcAnalysis)) {
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
						} else if ("cc".equals(isNucAndCcAnalysis)) {
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
			//LOGGER.info("End of the chromocenter analysis , the results are in {}", chromocentersPipelineBatchDialog.getWorkDirectory());
		} else {
			IJ.showMessage(
					"There are no the three subdirectories  (See the directory name) or subDirectories are empty");
		}
	}
	
}