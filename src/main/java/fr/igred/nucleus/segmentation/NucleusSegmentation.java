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
package fr.igred.nucleus.segmentation;

import fr.igred.nucleus.core.Measure3D;
import fr.igred.nucleus.utils.ConvexHullDetection;
import fr.igred.nucleus.utils.ConvexHullSegmentation;
import fr.igred.nucleus.utils.FillingHoles;
import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.roi.GenericShapeWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.imageprocessing.Thresholding;
import fr.igred.nucleus.utils.Gradient;
import fr.igred.nucleus.utils.Histogram;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Filters3D;
import ij.plugin.GaussianBlur3D;
import ij.plugin.filter.LutApplier;
import ij.plugin.filter.PlugInFilter;
import ij.process.StackConverter;
import ij.process.StackStatistics;
import inra.ijpb.binary.BinaryImages;
import loci.formats.FormatException;
import loci.plugins.BF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import static fr.igred.nucleus.io.ImageSaver.saveFile;


/**
 * Object segmentation method for 3D images. This segmentation used as initial threshold the method of Otsu, and then
 * select the object maximizing the sphericity of the segmented object.
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class NucleusSegmentation {
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Segmentation parameters for the analysis */
	private final SegmentationParameters segmentationParameters;
	/** ImagePlus input to process */
	private final ImagePlus imgRawTransformed;
	/** Threshold detected by the Otsu modified method */
	private       int                    bestThreshold = -1;
	/** ImagePlus input to process */
	private       ImagePlus              imgRaw;
	/** Check if the segmentation is not in border */
	private       boolean                badCrop;
	/** Segmented image */
	private       ImagePlus[]            imageSeg;
	
	
	/**
	 * Constructor for the segmentation analysis for a folder containing images.
	 *
	 * @param imageFile              Current image analysed
	 * @param params list the parameters for the analyse
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public NucleusSegmentation(File imageFile, SegmentationParameters params)
	throws IOException, FormatException {
		this.segmentationParameters = params;
		// TODO ADD CHANNEL PARAMETERS (CASE OF CHANNELS UNSPLITED)
		this.imgRaw = getImageChannel(imageFile, 0);
		imgRaw.setTitle(imageFile.getName());
		this.imgRawTransformed = imgRaw.duplicate();
		imgRawTransformed.setTitle(imageFile.getName());
		Directory dirOutputOTSU = new Directory(params.getOutputFolder() + "OTSU");
		dirOutputOTSU.checkAndCreateDir();
		if (params.getConvexHullDetection()) {
			Directory dirOutputConvexHull = new Directory(params.getOutputFolder() +
			                                              ConvexHullDetection.CONVEX_HULL_ALGORITHM);
			dirOutputConvexHull.checkAndCreateDir();
		}
	}
	
	
	public NucleusSegmentation(ImageWrapper image, SegmentationParameters params, Client client)
	throws ServiceException, AccessException, ExecutionException {
		this.segmentationParameters = params;
		
		int[] cBound = {0, 0};
		this.imgRaw = image.toImagePlus(client, null, null, cBound, null, null);
		// TODO ADD CHANNEL PARAMETERS (CASE OF CHANNELS UNSPLITED)
		imgRaw.setTitle(image.getName());
		this.imgRawTransformed = imgRaw.duplicate();
		imgRawTransformed.setTitle(image.getName());
	}
	
	
	// Changed HERE TO RETRIEVE ONLY ID, ALLOWING MULTI THREADING DOWNLOAD
	public NucleusSegmentation(ImageWrapper image, ImagePlus imp, SegmentationParameters params) {
		this.segmentationParameters = params;
		
		this.imgRaw = imp;
		// TODO ADD CHANNEL PARAMETERS (CASE OF CHANNELS UNSPLITED)
		imgRaw.setTitle(image.getName());
		this.imgRawTransformed = imgRaw.duplicate();
		imgRawTransformed.setTitle(image.getName());
	}
	
	
	public NucleusSegmentation(ImageWrapper image,
	                           ROIWrapper roi,
	                           int i,
	                           SegmentationParameters params,
	                           Client client)
	throws ServiceException, AccessException, ExecutionException {
		this.segmentationParameters = params;
		
		List<RectangleWrapper> rectangles = roi.getShapes().getElementsOf(RectangleWrapper.class);
		
		RectangleWrapper rectangle = rectangles.get(0);
		
		int roiThickness = rectangles.size();
		int channel      = rectangle.getC();
		int slice        = rectangle.getZ();
		
		double[] coordinates = rectangle.getCoordinates();
		int      x           = (int) coordinates[0];
		int      y           = (int) coordinates[1];
		int      width       = (int) coordinates[2];
		int      height      = (int) coordinates[3];
		
		int[] cBound = {channel, channel};
		int[] zBound = {slice, slice + roiThickness - 1};
		int[] xBound = {x, x + width - 1};
		int[] yBound = {y, y + height - 1};
		
		this.imgRaw = image.toImagePlus(client, xBound, yBound, cBound, zBound, null);
		
		imgRaw.setTitle(image.getName() + "_" + i + "_C" + rectangle.getC());
		this.imgRawTransformed = imgRaw.duplicate();
		imgRawTransformed.setTitle(imgRaw.getTitle());
	}
	
	
	/**
	 * Method to set a specific channel image
	 *
	 * @param imageFile
	 * @param channel   channel number of the current image to analyse
	 *
	 * @return channel image
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public static ImagePlus getImageChannel(File imageFile, int channel) throws IOException, FormatException {
		ImagePlus[] currentImage = BF.openImagePlus(imageFile.getAbsolutePath());
		currentImage = ChannelSplitter.split(currentImage[channel]);
		return currentImage[0];
	}
	
	
	/**
	 * Method to save 3D parameters computed
	 *
	 * @param segmentedImage segmented image
	 *
	 * @return
	 */
	public String saveImageResult(ImagePlus[] segmentedImage) {
		Measure3D measure3D = new Measure3D(segmentedImage, imgRaw,
		                                    getXCalibration(), getYCalibration(), getZCalibration());
		return measure3D.nucleusParameter3D();
	}
	
	
	/**
	 * Compute of the first threshold of input image with the method of Otsu. From this initial value we will seek the
	 * better segmentation possible: for this we will take the voxels value superior at the threshold value of method of
	 * Otsu : Then we compute the standard deviation of this values voxel > threshold value determines which allows
	 * range of value we will search the better threshold value : thresholdOtsu - stdDev and thresholdOtsu + stdDev. For
	 * each threshold test; we do an opening and a closing, then run the holesFilling methods. To finish we compute the
	 * sphericity.
	 * <p>
	 * The aim of this method is to maximize the sphericity to obtain the segmented object nearest of the biological
	 * object.
	 * <p>
	 * //TODO methode a reecrire y a moyen de faire plus propre mais pas urgent
	 *
	 * @return ImagePlus Segmented image
	 */
	public void findOTSUMaximisingSphericity() {
		LOGGER.info("Finding OTSU to maximize sphericity.");
		double imageVolume = getVoxelVolume() * imgRaw.getWidth() *
		                     imgRaw.getHeight() * imgRaw.getStackSize();
		Gradient gradient       = new Gradient(imgRaw);
		double   bestSphericity = -1;
		
		List<Integer> arrayListThreshold = computeMinMaxThreshold(imgRawTransformed);  // methode OTSU
		for (int t = arrayListThreshold.get(0); t <= arrayListThreshold.get(1); ++t) {
			ImagePlus tempSeg;
			tempSeg = generateSegmentedImage(imgRawTransformed, t);
			
			tempSeg = BinaryImages.componentsLabeling(tempSeg, 26, 32);
			Calibration cal = imgRaw.getCalibration();
			if (segmentationParameters.isManualParameter()) {
				//TODO ADD UNITS
				cal.setXUnit("µm");
				cal.pixelWidth = segmentationParameters.getXCal();
				cal.setYUnit("µm");
				cal.pixelHeight = segmentationParameters.getYCal();
				cal.setZUnit("µm");
				cal.pixelDepth = segmentationParameters.getZCal();
			} else {
				cal.setXUnit("µm");
				cal.pixelWidth = imgRaw.getCalibration().pixelWidth;
				cal.setYUnit("µm");
				cal.pixelHeight = imgRaw.getCalibration().pixelHeight;
				cal.setZUnit("µm");
				cal.pixelDepth = imgRaw.getCalibration().pixelDepth;
			}
			tempSeg.setCalibration(cal);
			ImagePlus[] tempSegPlus = new ImagePlus[1];
			tempSegPlus[0] = tempSeg;
			Measure3D measure = new Measure3D(tempSegPlus,
			                                  imgRawTransformed,
			                                  getXCalibration(),
			                                  getYCalibration(),
			                                  getZCalibration());
			deleteArtefact(tempSeg);
			double  volume     = measure.computeVolumeObject2(255);
			boolean firstStack = isVoxelThresholded(tempSeg, 255, 0);
			boolean lastStack  = isVoxelThresholded(tempSeg, 255, tempSeg.getStackSize() - 1);
			if (testRelativeObjectVolume(volume, imageVolume) &&
			    volume >= segmentationParameters.getMinVolumeNucleus() &&
			    volume <= segmentationParameters.getMaxVolumeNucleus() &&
			    !firstStack && !lastStack) {
				double sphericity = Measure3D.computeSphericity(volume, Measure3D.computeComplexSurface(tempSeg, gradient));
				if (sphericity > bestSphericity) {
					this.bestThreshold = t;
					bestSphericity = sphericity;
					this.imageSeg = tempSegPlus;
					imageSeg[0].setTitle(imgRawTransformed.getTitle());
				}
			}
		}
		
		if (bestThreshold != -1) {
			imageSeg[0] = morphologicalCorrection(imageSeg[0]);
			checkBorder(imageSeg[0]);
		}
	}
	
	
	/**
	 * Pre process ot the raw image : - Gaussian blur - LUT application
	 * <p> TODO object function image transformation
	 */
	public void preProcessImage() {
		LOGGER.info("Preprocessing image.");
		GaussianBlur3D.blur(imgRawTransformed, 0.1, 0.1, 1);
		ImageStack imageStack = imgRawTransformed.getStack();
		
		int max = 0;
		for (int k = 0; k < imgRawTransformed.getStackSize(); ++k) {
			for (int b = 0; b < imgRawTransformed.getWidth(); ++b) {
				for (int j = 0; j < imgRawTransformed.getHeight(); ++j) {
					if (max < imageStack.getVoxel(b, j, k)) {
						max = (int) imageStack.getVoxel(b, j, k);
					}
				}
			}
		}
		imgRawTransformed.setDisplayRange(0, max);
		/* Prepare LutApplier */
		PlugInFilter lutApplier = new LutApplier();
		lutApplier.setup("", imgRawTransformed);
		/* Set stack mode with Macro class, but thread has to be renamed */
		String threadName = Thread.currentThread().getName();
		Thread.currentThread().setName("Run$_" + threadName);
		Macro.setOptions("stack");
		/* Apply LUT */
		lutApplier.run(imgRawTransformed.getProcessor());
		/* Restore thread name */
		Thread.currentThread().setName(threadName);
		if (imgRaw.getType() == ImagePlus.GRAY16) {
			StackConverter stackConverter = new StackConverter(imgRawTransformed);
			stackConverter.convertToGray8();
		}
	}
	
	
	/**
	 * Creation of the nucleus segmented image from a OTSU threshold.
	 *
	 * @param imagePlusInput raw image
	 * @param threshold      threshold value for the segmentation
	 *
	 * @return segmented image of the nucleus
	 */
	public static ImagePlus generateSegmentedImage(ImagePlus imagePlusInput, int threshold) {
		ImageStack imageStackInput    = imagePlusInput.getStack();
		ImagePlus  imagePlusSegmented = imagePlusInput.duplicate();
		imagePlusSegmented.setTitle(imagePlusInput.getTitle());
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					double voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue >= threshold) {
						imageStackSegmented.setVoxel(i, j, k, 255);
					} else {
						imageStackSegmented.setVoxel(i, j, k, 0);
					}
				}
			}
		}
		
		return imagePlusSegmented;
	}
	
	
	/**
	 * Method to check if the final segmented image got pixel on border of the image (filter of partial nucleus).
	 *
	 * @param imagePlusInput Segmented image with the OTSU modified threshold
	 */
	private void checkBorder(ImagePlus imagePlusInput) {
		ImageStack imageStackInput = imagePlusInput.getStack();
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			if (k == 0 || k == imagePlusInput.getStackSize() - 1) {
				for (int i = 0; i < imagePlusInput.getWidth(); i++) {
					for (int j = 0; j < imagePlusInput.getHeight(); j++) {
						if (imageStackInput.getVoxel(i, j, k) == 255.0) {
							this.badCrop = true;
						}
					}
				}
			}
			
			for (int i = 0; i < imagePlusInput.getWidth();
			     i += imagePlusInput.getWidth() - 1) {
				for (int j = 0; j < imagePlusInput.getHeight(); j++) {
					if (imageStackInput.getVoxel(i, j, k) == 255.0) {
						this.badCrop = true;
					}
				}
			}
			for (int j = 0; j < imagePlusInput.getHeight();
			     j += imagePlusInput.getHeight() - 1) {
				for (int i = 0; i < imagePlusInput.getWidth(); i++) {
					if (imageStackInput.getVoxel(i, j, k) == 255.0) {
						this.badCrop = true;
					}
				}
			}
		}
	}
	
	
	/**
	 * Method to check if the nucleus is truncated (last slice or border).
	 *
	 * @return True if the nucleus is partial
	 */
	public boolean isBadCrop() {
		return badCrop;
	}
	
	
	/**
	 * Determine of the minimum and the maximum value o find the better threshold value.
	 *
	 * @param imagePlusInput raw image
	 *
	 * @return array lis which contain at the index 0 the min valu and index 1 the max value
	 */
	private static List<Integer> computeMinMaxThreshold(ImagePlus imagePlusInput) {
		List<Integer> minMaxThreshold = new ArrayList<>(2);
		
		int             threshold       = Thresholding.computeOTSUThreshold(imagePlusInput);
		StackStatistics stackStatistics = new StackStatistics(imagePlusInput);
		double          stdDev          = stackStatistics.stdDev;
		double          min             = threshold - stdDev * 2;
		double          max             = threshold + stdDev / 2;
		
		if (min < 0) {
			minMaxThreshold.add(6);
		} else {
			minMaxThreshold.add((int) min);
		}
		minMaxThreshold.add((int) max);
		return minMaxThreshold;
	}
	
	
	/**
	 * Determines the number of pixel on the stack index in input return true if the number of pixel>=10.
	 *
	 * @param imagePlusSegmented ImagePlus segmented image
	 * @param threshold          int number of pixel
	 * @param stackIndex         index of the slice of interest
	 *
	 * @return boolean true if the nb of pixel is > to threshold else false
	 */
	private static boolean isVoxelThresholded(ImagePlus imagePlusSegmented,
	                                          int threshold,
	                                          int stackIndex) {
		boolean    voxelThresolded     = false;
		int        nbVoxelThresholded  = 0;
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		for (int i = 0; i < imagePlusSegmented.getWidth(); ++i) {
			for (int j = 0; j < imagePlusSegmented.getHeight(); ++j) {
				if (imageStackSegmented.getVoxel(i, j, stackIndex) >= threshold) {
					nbVoxelThresholded++;
				}
			}
		}
		if (nbVoxelThresholded >= 10) {
			voxelThresolded = true;
		}
		return voxelThresolded;
	}
	
	
	/**
	 * method to realise morphological correction (filling holes and top hat)
	 *
	 * @param imagePlusSegmented image to be correct
	 */
	private static ImagePlus morphologicalCorrection(ImagePlus imagePlusSegmented) {
		computeOpening(imagePlusSegmented);
		computeClosing(imagePlusSegmented);
		// TODO FIX?
		return FillingHoles.apply2D(imagePlusSegmented);
	}
	
	
	/**
	 * Compute closing with the segmented image.
	 *
	 * @param imagePlusInput image segmented
	 */
	private static void computeClosing(ImagePlus imagePlusInput) {
		ImageStack imageStackInput = imagePlusInput.getImageStack();
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MAX, 1, 1, 0.5f);
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MIN, 1, 1, 0.5f);
		imagePlusInput.setStack(imageStackInput);
	}
	
	
	/**
	 * Compute opening with the segmented image
	 *
	 * @param imagePlusInput image segmented
	 */
	private static void computeOpening(ImagePlus imagePlusInput) {
		ImageStack imageStackInput = imagePlusInput.getImageStack();
		
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MIN, 1, 1, 0.5f);
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MAX, 1, 1, 0.5f);
		
		imagePlusInput.setStack(imageStackInput);
	}
	
	
	/**
	 * getter: return the threshold value computed
	 *
	 * @return the final threshold value
	 */
	public int getBestThreshold() {
		return bestThreshold;
	}
	
	
	/**
	 * Method to detected if the object is superior or equal at 70% of the image return false.
	 *
	 * @param objectVolume double volume of the object
	 *
	 * @return boolean if ratio object/image > 70% return false else return true
	 */
	private static boolean testRelativeObjectVolume(double objectVolume, double imageVolume) {
		double ratio = objectVolume / imageVolume * 100;
		return ratio < 70;
	}
	
	
	/**
	 * Keep the bigger object in the image at 255 put the other at 0.
	 *
	 * @param imgSeg ImagePlus of the segmented image
	 */
	private static void deleteArtefact(ImagePlus imgSeg) {
		double     voxelValue;
		double     mode            = getLabelOfLargestObject(imgSeg);
		ImageStack imageStackInput = imgSeg.getStack();
		for (int k = 0; k < imgSeg.getNSlices(); ++k) {
			for (int i = 0; i < imgSeg.getWidth(); ++i) {
				for (int j = 0; j < imgSeg.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == mode) {
						imageStackInput.setVoxel(i, j, k, 255);
					} else {
						imageStackInput.setVoxel(i, j, k, 0);
					}
				}
			}
		}
	}
	
	
	/**
	 * Detection of the label of the biggest object segmented in the image
	 *
	 * @param imgSeg ImagePlus segmented img
	 *
	 * @return double the label of the bigger object
	 */
	private static double getLabelOfLargestObject(ImagePlus imgSeg) {
		Histogram histogram = new Histogram();
		histogram.run(imgSeg);
		double labelMax   = 0;
		double nbVoxelMax = -1;
		for (Entry<Double, Integer> entry : histogram.getHistogram().entrySet()) {
			double label   = entry.getKey();
			int    nbVoxel = entry.getValue();
			if (nbVoxel > nbVoxelMax) {
				nbVoxelMax = nbVoxel;
				labelMax = label;
			}
		}
		return labelMax;
	}
	
	
	/**
	 * Method to get X calibration if it's present in parameters of analysis or get the metadata of the image.
	 * <p> TODO verifier cette methode si elle est au bon endroit
	 *
	 * @return X calibration
	 */
	public double getXCalibration() {
		double xCal;
		if (segmentationParameters.isManualParameter()) {
			xCal = segmentationParameters.getXCal();
		} else {
			xCal = imgRawTransformed.getCalibration().pixelWidth;
		}
		return xCal;
	}
	
	
	/**
	 * Method to get Y calibration if it's present in parameters of analysis or get the metadata of the image.
	 * <p> TODO verifier cette methode si elle est au bonne endroit
	 *
	 * @return Y calibration
	 */
	public double getYCalibration() {
		double yCal;
		if (segmentationParameters.isManualParameter()) {
			yCal = segmentationParameters.getYCal();
		} else {
			yCal = imgRawTransformed.getCalibration().pixelHeight;
		}
		return yCal;
	}
	
	
	/**
	 * Method to get Y calibration if it's present in parameters of analysis or get the metadata of the image.
	 * <p> TODO verifier cette methode si elle est à sa place
	 *
	 * @return Z calibration
	 */
	public double getZCalibration() {
		double zCal;
		if (segmentationParameters.isManualParameter()) {
			zCal = segmentationParameters.getZCal();
		} else {
			zCal = imgRawTransformed.getCalibration().pixelDepth;
		}
		return zCal;
	}
	
	
	/**
	 * Method to compute the voxel volume : if it's present in parameters of analysisor get the metadata of the image.
	 * TODO verifier cette methode si elle est à sa place
	 *
	 * @return Z calibration
	 */
	public double getVoxelVolume() {
		double calibration;
		if (segmentationParameters.isManualParameter()) {
			calibration = segmentationParameters.getVoxelVolume();
		} else {
			Calibration cal = imgRawTransformed.getCalibration();
			calibration = cal.pixelDepth * cal.pixelWidth * cal.pixelHeight;
		}
		
		return calibration;
	}
	
	
	/**
	 * Method to move bad crop (truncated nucleus) to badcrop folder.
	 * <p> TODO verifier cette methode si elle est à sa place
	 *
	 * @param inputPathDir folder of the input to create badcrop folder.
	 */
	public void checkBadCrop(String inputPathDir) {
		LOGGER.info("Checking bad crop.");
		if (badCrop || bestThreshold == -1) {
			File badCropFolder = new File(inputPathDir + File.separator + "BadCrop");
			LOGGER.debug("Saving bad crops to: {}", badCropFolder);
			
			if (badCropFolder.exists() || badCropFolder.mkdir()) {
				File    fileToMove = new File(inputPathDir + File.separator + imgRawTransformed.getTitle());
				File    newFile    = new File(badCropFolder + File.separator + imgRawTransformed.getTitle());
				boolean renamed    = fileToMove.renameTo(newFile);
				if (!renamed) {
					LOGGER.info("File not renamed: {}", fileToMove.getAbsolutePath());
				}
			} else {
				LOGGER.error("Directory does not exist and could not be created: {}", badCropFolder);
			}
		}
	}
	
	
	public void checkBadCrop(ImageWrapper image, Client client) {
		if (badCrop || bestThreshold == -1) {
			List<TagAnnotationWrapper> tags;
			TagAnnotationWrapper       tagBadCrop;
			
			try {
				tags = client.getTags("BadCrop");
			} catch (OMEROServerError | ServiceException e) {
				LOGGER.error("Could not get list of \"BadCrop\" tags", e);
				return;
			}
			
			if (tags.isEmpty()) {
				try {
					tagBadCrop = new TagAnnotationWrapper(client, "BadCrop", "");
				} catch (AccessException | ServiceException | ExecutionException e) {
					LOGGER.error("Could not create new \"BadCrop\" tag", e);
					return;
				}
			} else {
				try {
					tagBadCrop = tags.get(0);
				} catch (Exception e) {
					LOGGER.error("Could not retrieve a \"BadCrop\" tag", e);
					return;
				}
			}
			
			LOGGER.info("Adding Bad Crop tag");
			try {
				image.link(client, tagBadCrop);
			} catch (AccessException | ServiceException | ExecutionException e) {
				LOGGER.error("Tag already added", e);
			}
		}
	}
	
	
	public void checkBadCrop(ROIWrapper roi, Client client) {
		if (badCrop || bestThreshold == -1) {
			for (GenericShapeWrapper<?> shape : roi.getShapes()) {
				shape.setStroke(Color.RED);
			}
		}
		try {
			roi.saveROI(client);
		} catch (OMEROServerError | ServiceException e) {
			LOGGER.error("Could not save bad crop ROI id: {}", roi.getId());
		}
	}
	
	
	/**
	 * Method to save the OTSU segmented image.
	 * <p> TODO verifier cette methode si elle est à ca place
	 */
	public void saveOTSUSegmented() {
		LOGGER.info("Computing and saving OTSU segmentation.");
		if (!badCrop && bestThreshold != -1) {
			String pathSegOTSU = segmentationParameters.getOutputFolder() +
			                     "OTSU" +
			                     File.separator +
			                     imageSeg[0].getTitle();
			saveFile(imageSeg[0], pathSegOTSU);
			
		}
	}
	
	
	/**
	 * Method to save the OTSU segmented image.
	 * <p> TODO verifier cette methode si elle est à ca place
	 */
	public void saveOTSUSegmentedOMERO(Client client, Long output)
	throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
		LOGGER.info("Computing and saving OTSU segmentation.");
		if (!badCrop && bestThreshold != -1) {
			String path = new java.io.File(".").getCanonicalPath() +
			              // File.separator + "OTSU" +
			              File.separator + imageSeg[0].getTitle();
			saveFile(imageSeg[0], path);
			
			client.getDataset(output).importImages(client, path);
			
			File file = new File(path);
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				LOGGER.error("Could not delete file: {}", path);
			}
		}
	}
	
	
	/**
	 * Method to save the OTSU segmented image.
	 * <p> TODO verifier cette methode si elle est à sa place
	 */
	public void saveConvexHullSeg() {
		LOGGER.info("Computing and saving Convex Hull segmentation.");
		if (!badCrop && bestThreshold != -1 && segmentationParameters.getConvexHullDetection()) {
			imageSeg[0] = ConvexHullSegmentation.convexHullDetection(imageSeg[0]);
			String pathConvexHullSeg = segmentationParameters.getOutputFolder() +
			                           ConvexHullDetection.CONVEX_HULL_ALGORITHM + File.separator + imageSeg[0].getTitle();
			imageSeg[0].setTitle(pathConvexHullSeg);
			saveFile(imageSeg[0], pathConvexHullSeg);
		}
	}
	
	
	/**
	 * Method to save the OTSU segmented image.
	 * <p> TODO verifier cette methode si elle est à sa place
	 */
	public void saveConvexHullSegOMERO(Client client, Long output)
	throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
		LOGGER.info("Computing and saving Convex Hull segmentation.");
		if (!badCrop && bestThreshold != -1 && segmentationParameters.getConvexHullDetection()) {
			imageSeg[0] = ConvexHullSegmentation.convexHullDetection(imageSeg[0]);
			
			String path = new java.io.File(".").getCanonicalPath() //+ File.separator + CONVEX_HULL_ALGORITHM
			              + File.separator + imageSeg[0].getTitle();
			saveFile(imageSeg[0], path);
			
			client.getDataset(output).importImages(client, path);
			
			File file = new File(path);
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				LOGGER.error("Could not delete file: {}", path);
			}
		}
	}
	
	
	/**
	 * Method to get the parameter of the 3D parameters for OTSU segmented image if the object can't be segmented return
	 * -1 for parameters.
	 * <p> TODO verifier cette methode si elle est à sa place
	 *
	 * @return
	 */
	public String getImageCropInfoOTSU() {
		if (!badCrop && bestThreshold != -1) {
			return saveImageResult(imageSeg) + "\t" + bestThreshold + System.lineSeparator();
		} else {
			return imgRaw.getTitle() + "\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1" + System.lineSeparator();
		}
	}
	
	
	/**
	 * Method to get the parameter of the 3D parameters image segmented using convex hull algorithm if the object can't
	 * be segmented return -1 for parameters.
	 * <p> TODO verifier DUPLICATION DE getImageCropInfoOTSU à sa place ?
	 *
	 * @return
	 */
	public String getImageCropInfoConvexHull() {
		if (!badCrop && bestThreshold != -1) {
			return saveImageResult(imageSeg) + "\t" + bestThreshold + System.lineSeparator();
		} else {
			return imgRaw.getTitle() + "\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1" + System.lineSeparator();
		}
	}
	
}
