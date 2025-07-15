package fr.igred.nucleus.plugins;

import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ChannelWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.roi.GenericShapeWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.nucleus.autocrop.CropFromCoordinates;
import fr.igred.nucleus.dialogs.CropFromCoordinatesDialog;
import fr.igred.nucleus.dialogs.IDialogListener;
import fr.igred.nucleus.io.FilesNames;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class CropFromCoordinates_ implements PlugIn, IDialogListener {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private DatasetWrapper toCropDataset;
	
	private CropFromCoordinatesDialog cropFromCoordinatesDialog;
	
	
	public static void cropFromCoordinates(String coordinateDir) throws IOException, FormatException {
		CropFromCoordinates test = new CropFromCoordinates(coordinateDir);
		test.run();
	}
	
	
	/* This method is used by plugins.config */
	@Override
	public void run(String s) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		cropFromCoordinatesDialog = new CropFromCoordinatesDialog();
	}
	
	
	@Override
	public void onStart() throws AccessException, ServiceException, ExecutionException {
		if (cropFromCoordinatesDialog.isOmeroEnabled()) {
			runOMERO();
		} else {
			String file = cropFromCoordinatesDialog.getLink();
			if (file == null || file.isEmpty()) {
				IJ.error("Input file or directory is missing");
			} else {
				try {
					LOGGER.info("Begin Crop from coordinate process ");
					
					cropFromCoordinates(file);
					
					LOGGER.info("Crop from coordinate process has ended successfully");
				} catch (IOException | FormatException e) {
					LOGGER.info("Crop from coordinate process has failed");
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
	
	
	private static void cropImageFromOMERO2(Client client, ImageWrapper image, ImageWrapper imageToCrop,
	                                        DatasetWrapper outputDataset, int c)
	throws AccessException, ServiceException, ExecutionException, IOException, OMEROServerError {
		List<ROIWrapper>     rois   = image.getROIs(client);
		List<ChannelWrapper> canaux = imageToCrop.getChannels(client);
		if (c > canaux.size() - 1) {
			System.out.println("Channel doesn't exists, there are only " + canaux.size() +
			                   " channels, first channel index is 0 !");
		} else {
			System.out.println("Number of channels detected : " + canaux.size());
			for (ROIWrapper roi : rois) {
				// Get the roi names
				String roiName = roi.getName();
				for (GenericShapeWrapper<?> shape : roi.getShapes()) {
					shape.setC(c);
				}
				// Get the name of the Image To Crop
				String imageToCropName = imageToCrop.getName();
				// Get the image to crop
				ImagePlus imp = imageToCrop.toImagePlus(client, roi);
				// Save Crop File
				FileSaver fileSaver = new FileSaver(imp);
				String    sortie    = fileSaver.toString();
				// Save the crop as TIF
				fileSaver.saveAsTiff(sortie);
				// generate a temporary file
				String resultPath = sortie;
				File   resultFile = new File(resultPath);
				// Remove file extension
				FilesNames outPutFilesNames = new FilesNames(imageToCropName);
				String     prefix           = outPutFilesNames.prefixNameFile();
				// Rename the temporary file same as toCrop Image name
				File toCropNewName = new File(prefix + "_" + roiName + "_C" + c);
				resultFile.renameTo(toCropNewName);
				String toCropFile = toCropNewName.toString();
				// Import Cropped Image to the Dataset
				outputDataset.importImages(client, toCropFile);
				// Delete temp file
				Files.deleteIfExists(toCropNewName.toPath());
			}
		}
	}
	
	
	private static void cropImageFromOMERO(Client client, ImageWrapper image, ImageWrapper imageToCrop,
	                                       DatasetWrapper outputDataset)
	throws AccessException, ServiceException, ExecutionException, IOException, OMEROServerError {
		List<ROIWrapper> rois = image.getROIs(client);
		for (ROIWrapper roi : rois) {
			// Get the roi names
			String roiName = roi.getName();
			// Get the name of the Image To Crop
			String imageToCropName = imageToCrop.getName();
			// Get the image to crop
			ImagePlus imp = imageToCrop.toImagePlus(client, roi);
			// Save Crop File
			FileSaver fileSaver = new FileSaver(imp);
			String    sortie    = fileSaver.toString();
			// Save the crop as TIF
			fileSaver.saveAsTiff(sortie);
			// generate a temporary file
			String resultPath = sortie;
			File   resultFile = new File(resultPath);
			// Remove file extension
			FilesNames outPutFilesNames = new FilesNames(imageToCropName);
			String     prefix           = outPutFilesNames.prefixNameFile();
			// Rename the temporary file same as toCrop Image name
			File toCropNewName = new File(prefix + "_" + roiName);
			resultFile.renameTo(toCropNewName);
			String toCropFile = toCropNewName.toString();
			// Import Cropped Image to the Dataset
			outputDataset.importImages(client, toCropFile);
			// Delete temp file
			Files.deleteIfExists(toCropNewName.toPath());
		}
	}
	
	
	public void runOMERO() throws AccessException, ServiceException, ExecutionException {
		// Check connection
		String hostname = cropFromCoordinatesDialog.getHostname();
		String port     = cropFromCoordinatesDialog.getPort();
		String username = cropFromCoordinatesDialog.getUsername();
		String password = cropFromCoordinatesDialog.getPassword();
		String group    = cropFromCoordinatesDialog.getGroup();
		String output   = cropFromCoordinatesDialog.getOutputProject();
		int    channel  = Integer.parseInt(cropFromCoordinatesDialog.getChannelToCrop());
		
		Prefs.set("omero.host", hostname);
		Prefs.set("omero.port", port);
		Prefs.set("omero.user", username);
		
		Client client = checkOMEROConnection(hostname, port, username, password.toCharArray(), group);
		
		// Handle the source according to the type given
		String         sourceDataType = cropFromCoordinatesDialog.getDataType();
		String         toCropdataType = cropFromCoordinatesDialog.getDataTypeToCrop();
		Long           inputID        = Long.valueOf(cropFromCoordinatesDialog.getSourceID());
		Long           inputToCropID  = Long.valueOf(cropFromCoordinatesDialog.getToCropID());
		DatasetWrapper outputds       = client.getDataset(Long.parseLong(output));
		
		try {
			if ("Image".equals(sourceDataType) && "Image".equals(toCropdataType)) {
				ImageWrapper image       = client.getImage(inputID);
				ImageWrapper imageToCrop = client.getImage(inputToCropID);
				
				try {
					LOGGER.info("Begin Autocrop from coordinate process ");
					cropImageFromOMERO2(client, image, imageToCrop, outputds, channel); // Ru
				} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
					LOGGER.info("Autocrop from coordinate process has failed");
					LOGGER.error("An error occurred.", e);
				}
			} else {
				List<ImageWrapper> sourceImages = new ArrayList<>(0);
				
				if ("Dataset".equals(sourceDataType) && "Dataset".equals(toCropdataType)) {
					DatasetWrapper sourceDataset = client.getDataset(inputID);
					toCropDataset = client.getDataset(inputToCropID);
					sourceImages = sourceDataset.getImages(client);
				} else if ("Tag".equals(sourceDataType)) {
					TagAnnotationWrapper tag = client.getTag(inputID);
					sourceImages = client.getImages(tag);
				}
				try {
					LOGGER.info("Begin Autocrop from coordinate process ");
					for (ImageWrapper sourceImage : sourceImages) {
						String             sourceImageName = sourceImage.getName();
						List<ImageWrapper> toCropImages    = toCropDataset.getImages(client, sourceImageName);
						if (!toCropImages.isEmpty()) {
							ImageWrapper toCropImage = toCropImages.get(0);
							cropImageFromOMERO(client, sourceImage, toCropImage, outputds); // Run cropFromCoordinates
						}
					}
					LOGGER.info("Autocrop from coordinate process has ended successfully");
				} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
					LOGGER.info("Autocrop from coordinate process has failed");
					LOGGER.error("An error occurred.", e);
				}
			}
		} catch (ServiceException se) {
			IJ.error("Unable to access to OMERO service");
		} catch (AccessException ae) {
			IJ.error("Cannot access " + sourceDataType + "with ID = " + inputID + ".");
		} catch (OMEROServerError | ExecutionException e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
}
