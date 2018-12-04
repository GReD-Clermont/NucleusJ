package gred.nucleus.core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import gred.nucleus.utils.FillingHoles;
import gred.nucleus.utils.Gradient;
import gred.nucleus.utils.Histogram;
import ij.*;
import ij.plugin.Filters3D;
import ij.process.*;
import ij.measure.*;
import ij.process.AutoThresholder.Method;
import inra.ijpb.binary.ConnectedComponents;

/**
 * this class allows the realization of segmention method in the image in input. This segmentation
 * is based on the method of Otsu, and we add the maximization of the sphericity (shape parameter)
 * of detected object .
 *
 * @author Tristan Dubos and Axel Poulet
 *
 */
public class NucleusSegmentation {

	private int _bestThreshold = -1;
	/** Segmentation parameters*/
	private double _volumeMin;
	/** */
	private double _volumeMax;
	/** */
	private String _logErrorSeg = "";

	/**
	 *
	 */
	public NucleusSegmentation (){	}

	/**
	 * Method which run the process in input image. This image will be segmented, and
	 * the binary image will be saved in a directory.
	 *
	 * @param imagePlusInput
	 * @return
	 */
	public ImagePlus run (ImagePlus imagePlusInput) {
		IJ.log("Begin segmentation "+imagePlusInput.getTitle());
		ImagePlus imagePlusSegmented = applySegmentation (imagePlusInput);
		IJ.log("End segmentation "+imagePlusInput.getTitle()+" "+_bestThreshold);
		if (_bestThreshold == -1) {
			if (_logErrorSeg.length() == 0) {
				IJ.showMessage("Error Segmentation", "Bad parameter for the segmentation, any object is detected between "
						+_volumeMin+" and "+ _volumeMax+" "+ imagePlusInput.getCalibration().getUnit()+"^3");
			}
			else {
				File fileLogError = new File (_logErrorSeg);
				BufferedWriter bufferedWriterLogError;
				FileWriter fileWriterLogError;
				try {
					fileWriterLogError = new FileWriter(fileLogError, true);
					bufferedWriterLogError = new BufferedWriter(fileWriterLogError);
					bufferedWriterLogError.write(imagePlusInput.getTitle()+"\n");
					bufferedWriterLogError.flush();
					bufferedWriterLogError.close();
				}
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		return imagePlusSegmented;
	}

	/**
	 * Compute of the first threshold of input image with the method of Otsu
	 * From this initial value we will seek the better segmentaion possible:
	 * for this we will take the voxels value superior at the threshold value of method of Otsu :
	 * Then we compute the standard deviation of this values voxel > threshold value
	 * determines which allows range of value we will search the better threshodl value :
	 *   thresholdOtsu-ecartType et thresholdOtsu+ecartType.
	 * For each threshold test; we realize a opening and a closing, then we use
	 * the holesFilling. To finish we compute the sphericity.
	 * The aim of this method is to maximize the sphericity to obtain the segmented object
	 * nearest of the biological object.
	 *
	 * @param imagePlusInput
	 * @return
	 */
	public ImagePlus applySegmentation (ImagePlus imagePlusInput) {
		double sphericityMax = -1.0;
		double sphericity;
		double volume;
		Calibration calibration = imagePlusInput.getCalibration();
		final double xCalibration = calibration.pixelWidth;
		final double yCalibration = calibration.pixelHeight;
		final double zCalibration = calibration.pixelDepth;
		Measure3D measure3D = new Measure3D();
		Gradient gradient = new Gradient(imagePlusInput);
		final double imageVolume = xCalibration*imagePlusInput.getWidth()*yCalibration*imagePlusInput.getHeight()*zCalibration*imagePlusInput.getStackSize();
		ImagePlus imagePlusSegmented = new ImagePlus();
		ArrayList<Integer> arrayListThreshold = computeMinMaxThreshold(imagePlusInput);  // methode OTSU
		for (int t = arrayListThreshold.get(0) ; t <= arrayListThreshold.get(1); ++t) {
			ImagePlus imagePlusSegmentedTemp = generateSegmentedImage(imagePlusInput,t);
			imagePlusSegmentedTemp = ConnectedComponents.computeLabels(imagePlusSegmentedTemp, 26, 32);
			deleteArtefact(imagePlusSegmentedTemp);
			imagePlusSegmentedTemp.setCalibration(calibration);
			volume = measure3D.computeVolumeObject(imagePlusSegmentedTemp,255);
			imagePlusSegmentedTemp.setCalibration(calibration);
			//IJ.log(""+ getClass().getName()+" L-"+ new Exception().getStackTrace()[0].getLineNumber()+" "+" Volume : "+ volume+ "   "+imageVolume +" Valmin "+_volumeMin+" Valmax "+_volumeMax+"\n les stack"+imagePlusSegmented.getImageStack());
			boolean first = isVoxelThresholded(imagePlusSegmentedTemp,255, 0);
			boolean last = isVoxelThresholded(imagePlusSegmentedTemp,255, imagePlusInput.getStackSize()-1);
			if (testRelativeObjectVolume(volume,imageVolume) &&
					volume >= _volumeMin &&
					volume <= _volumeMax && first == false && last == false) {
				sphericity = measure3D.computeSphericity(volume,measure3D.computeComplexSurface(imagePlusSegmentedTemp,gradient));
				//IJ.log(""+ getClass().getName()+" L-"+ new Exception().getStackTrace()[0].getLineNumber()+"les stack"+imagePlusSegmentedTemp.getImageStack());
				if (sphericity > sphericityMax ) {
					_bestThreshold = t;
					sphericityMax = sphericity;
					StackConverter stackConverter = new StackConverter( imagePlusSegmentedTemp );
					stackConverter.convertToGray8();
					imagePlusSegmented= imagePlusSegmentedTemp.duplicate();
				}
			}
			//else
			//	IJ.log("Volume de l'objet segmenté : "+ volume + " seuil entre "+ _volumeMin +" et "+  _volumeMax);
		}
		ImageStack imageStackInput = imagePlusSegmented.getImageStack();
		//IJ.log(""+ getClass().getName()+" L-"+ new Exception().getStackTrace()[0].getLineNumber()+" "+imageStackInput);
		if(_bestThreshold != -1 )
			morphologicalCorrection(imagePlusSegmented);
		return imagePlusSegmented;
	}


	/**
	 * Compute the beginig threshold value
	 *
	 * @param imagePlusInput raw image
	 * @return
	 */
	private int computeThreshold (ImagePlus imagePlusInput) {
		AutoThresholder autoThresholder = new AutoThresholder();
		ImageStatistics imageStatistics = new StackStatistics(imagePlusInput);
		int [] tHisto = imageStatistics.histogram;
		return autoThresholder.getThreshold(Method.Otsu,tHisto);
	}

	/**
	 * Creation of the nucleus segmented image
	 *
	 * @param imagePlusInput raw image
	 * @param threshold threshold value for the segmentation
	 * @return segmented image of the nucleus
	 */
	private ImagePlus generateSegmentedImage (ImagePlus imagePlusInput, int threshold) {
		ImageStack imageStackInput = imagePlusInput.getStack();
		ImagePlus imagePlusSegmented = imagePlusInput.duplicate();
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		for(int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					double voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue >= threshold)
						imageStackSegmented.setVoxel(i, j, k, 255);
					else
						imageStackSegmented.setVoxel(i, j, k, 0);
				}
			}
		}
		return imagePlusSegmented;
	}

	/**
	 * Determine of the minimum and the maximum value o find the better threshold value
	 *
	 * @param imagePlusInput raw image
	 * @return array lis which contain at the index 0 the min valu and index 1 the max value
	 *
	 */
	private ArrayList<Integer> computeMinMaxThreshold(ImagePlus imagePlusInput) {
		ArrayList<Integer> arrayListMinMaxThreshold = new ArrayList<Integer>();
		int threshold = computeThreshold (imagePlusInput);
		StackStatistics stackStatistics = new StackStatistics(imagePlusInput);
		double stdDev =stackStatistics.stdDev ;
		double min = threshold - stdDev*2;
		double max = threshold + stdDev/2;
		if ( min < 0)
			arrayListMinMaxThreshold.add(6);
		else
			arrayListMinMaxThreshold.add((int)min);
		arrayListMinMaxThreshold.add((int)max);
		return arrayListMinMaxThreshold;
	}

	/**
	 * Determines there
	 *
	 * @param imagePlusSegmented
	 * @param threshold
	 * @param stackIndice
	 * @return
	 */
	private boolean isVoxelThresholded (ImagePlus imagePlusSegmented, int threshold, int stackIndice) {
		boolean voxelThresolded = false;
		int nbVoxelThresholded = 0;
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		for (int i = 0; i < imagePlusSegmented.getWidth(); ++i ) {
			for (int j = 0; j < imagePlusSegmented.getHeight(); ++j ) {
				if ( imageStackSegmented.getVoxel(i,j,stackIndice) >= threshold)
					nbVoxelThresholded++;
			}
		}
		if (nbVoxelThresholded >= 10)
			voxelThresolded = true;
		return voxelThresolded;
	}


	/**
	 * 	 method to realise sevral morphological correction ( filling holes and top hat)
	 *
	 * @param imagePlusSegmented image to be correct
	 */
	private void morphologicalCorrection (ImagePlus imagePlusSegmented) {
		FillingHoles holesFilling = new FillingHoles();
		int temps =imagePlusSegmented.getBitDepth();
		computeOpening(imagePlusSegmented);
		computeClosing(imagePlusSegmented);
		imagePlusSegmented = holesFilling.apply2D(imagePlusSegmented);
	}


	/**
	 * compute closing with the segmented image
	 *
	 * @param imagePlusInput image segmented
	 */
	private void computeClosing (ImagePlus imagePlusInput) {
		ImageStack imageStackInput = imagePlusInput.getImageStack();
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MAX,1,1,(float)0.5);
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MIN,1,1,(float)0.5);
		imagePlusInput.setStack(imageStackInput);
	}

	/**
	 * compute opening with the segmented image 
	 *
	 * @param imagePlusInput image segmented
	 */
	private void computeOpening (ImagePlus imagePlusInput) {
		ImageStack imageStackInput = imagePlusInput.getImageStack();
		int temps =imageStackInput.getBitDepth();
		//IJ.log(""+ getClass().getName()+" L-"+ new Exception().getStackTrace()[0].getLineNumber()+" "+imageStackInput +"  getBitDepth "+temps);
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MIN,1,1,(float)0.5);
		imageStackInput = Filters3D.filter(imageStackInput, Filters3D.MAX,1,1,(float)0.5);
		imagePlusInput.setStack(imageStackInput);
	}


	/**
	 * getter to retun the chosen threshold value
	 * @return the final threshold value
	 */
	public int getBestThreshold (){
		return _bestThreshold;
	}

	/**
	 * if the detected object is superior or equal at 70% of the image return false
	 *
	 * @param objectVolume
	 * @return
	 */
	private boolean testRelativeObjectVolume(double objectVolume,double imageVolume) {
		final double ratio = (objectVolume/imageVolume)*100;
		if (ratio >= 70)
			return false;
		else
			return true;
	}

	/**
	 * interval of volume to detect the object
	 *
	 * @param volumeMin
	 * @param volumeMax
	 */
	public void setVolumeRange(double volumeMin, double volumeMax) {
		_volumeMin = volumeMin;
		_volumeMax = volumeMax;
	}

	/**
	 *
	 * @param logErrorSeg
	 */
	public void setLogErrorSegmentationFile (String logErrorSeg) {
		_logErrorSeg = logErrorSeg;
	}

	/**
	 *
	 * @param imagePlusInput
	 */
	public void deleteArtefact (ImagePlus imagePlusInput) {
		double voxelValue;
		double mode = getLabelOfLargestObject(imagePlusInput);
		ImageStack imageStackInput = imagePlusInput.getStack();
		for(int k = 0; k < imagePlusInput.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == mode)
						imageStackInput.setVoxel(i, j, k, 255);
					else
						imageStackInput.setVoxel(i, j, k, 0);
				}
			}
		}
	}

	/**
	 *
	 * @param imagePlusInput
	 * @return
	 */
	public double getLabelOfLargestObject(ImagePlus imagePlusInput) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		double indiceNbVoxelMax = 0;
		double nbVoxelMax = -1;
		for(Entry<Double, Integer> entry : histogram.getHistogram().entrySet()) {
			double label = entry.getKey();
			int nbVoxel = entry.getValue();
			if (nbVoxel > nbVoxelMax) {
				nbVoxelMax = nbVoxel;
				indiceNbVoxelMax = label;
			}
		}
		return indiceNbVoxelMax;
	}
}