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
package fr.igred.nucleus.utils2;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import fr.igred.nucleus.utils.Histogram;
import fr.igred.nucleus.utils.VoxelRecord;
import ij.ImagePlus;
import ij.ImageStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.TreeMap;


/**
 * Class computing 3D parameters from raw and his segmented image associated :
 * <ul>
 *     <li>Volume</li>
 *     <li>Flatness</li>
 *     <li>Elongation</li>
 *     <li>Sphericity</li>
 *     <li>Equivalent Spherical Radius</li>
 *     <li>SurfaceArea</li>
 *     <li>SurfaceAreaCorrected</li>
 *     <li>SphericityCorrected</li>
 *     <li>MeanIntensity</li>
 *     <li>StandardDeviation</li>
 *     <li>MinIntensity</li>
 *     <li>MaxIntensity</li>
 *     <li>OTSUThreshold</li>
 * </ul>
 * <p>
 * //TODO reecrire cette classe ya des choses que je fais 5 fois c'est inutile
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class Measure3D {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Map<Double, Integer> segmentedNucleusHist = new TreeMap<>();
	private final Map<Double, Integer> backgroundHist       = new TreeMap<>();
	
	private ImagePlus imageSeg;
	private ImagePlus rawImage;
	
	private double xCal;
	private double yCal;
	private double zCal;
	
	
	public Measure3D(ImagePlus imageSeg, ImagePlus rawImage, double xCal, double ycal, double zCal) {
		this.rawImage = rawImage;
		this.imageSeg = imageSeg;
		this.xCal = xCal;
		this.yCal = ycal;
		this.zCal = zCal;
		histogramSegmentedNucleus();
	}
	
	
	/**
	 * This Method compute the volume of each segmented objects in imagePlus
	 *
	 * @param imagePlusInput ImagePlus segmented image
	 *
	 * @return double table which contain the volume of each image object
	 */
	public double[] computeVolumeOfAllObjects(ImagePlus imagePlusInput) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		double[]             tlabel        = histogram.getLabels();
		double[]             tObjectVolume = new double[tlabel.length];
		Map<Double, Integer> hist          = histogram.getHistogram();
		for (int i = 0; i < tlabel.length; ++i) {
			int nbVoxel = hist.get(tlabel[i]);
			tObjectVolume[i] = nbVoxel * xCal * yCal * zCal;
		}
		return tObjectVolume;
	}
	
	
	private double computeVolumeObjectML() {
		double volumeTMP = 0.0;
		for (Map.Entry<Double, Integer> toto : segmentedNucleusHist.entrySet()) {
			if (toto.getValue() > 0) {
				volumeTMP += toto.getValue();
			}
		}
		return volumeTMP * xCal * yCal * zCal;
	}
	
	
	/**
	 * compute the equivalent spherical radius
	 *
	 * @param volume double of the volume of the object of interesr
	 *
	 * @return double the equivalent spherical radius
	 */
	public static double equivalentSphericalRadius(double volume) {
		double radius = 3 * volume / (4 * Math.PI);
		radius = StrictMath.cbrt(radius);
		return radius;
	}
	
	
	/**
	 * Method which compute the sphericity : 36Pi*Volume^2/Surface^3 = 1 if perfect sphere
	 *
	 * @param volume  double volume of the object
	 * @param surface double surface of the object
	 *
	 * @return double sphercity
	 */
	public static double computeSphericity(double volume, double surface) {
		return StrictMath.cbrt(36 * Math.PI * volume * volume) / surface;
	}
	
	
	/**
	 * Method which compute the eigen value of the matrix (differences between the coordinates of all points and the
	 * barycenter. Obtaining a symmetric matrix : xx xy xz xy yy yz xz yz zz Compute the eigen value with the pakage
	 * JAMA
	 *
	 * @param label double label of interest
	 *
	 * @return double table containing the 3 eigen values
	 */
	public double[] computeEigenValue3D(double label) {
		ImageStack  imageStackInput = imageSeg.getImageStack();
		VoxelRecord barycenter      = computeBarycenter3D(true, imageSeg, label);
		
		double xx      = 0;
		double xy      = 0;
		double xz      = 0;
		double yy      = 0;
		double yz      = 0;
		double zz      = 0;
		int    counter = 0;
		double voxelValue;
		for (int k = 0; k < imageSeg.getStackSize(); ++k) {
			double dz = zCal * k - barycenter.getK();
			for (int i = 0; i < imageSeg.getWidth(); ++i) {
				double dx = xCal * i - barycenter.getI();
				for (int j = 0; j < imageSeg.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == label) {
						double dy = yCal * j - barycenter.getJ();
						xx += dx * dx;
						yy += dy * dy;
						zz += dz * dz;
						xy += dx * dy;
						xz += dx * dz;
						yz += dy * dz;
						counter++;
					}
				}
			}
		}
		double[][] tValues = {{xx / counter, xy / counter, xz / counter},
		                      {xy / counter, yy / counter, yz / counter},
		                      {xz / counter, yz / counter, zz / counter}};
		Matrix matrix = new Matrix(tValues);
		
		EigenvalueDecomposition eigenValueDecomposition = matrix.eig();
		return eigenValueDecomposition.getRealEigenvalues();
	}
	
	
	/**
	 * Compute the flatness and the elongation of the object of interest
	 *
	 * @param label double label of interest
	 *
	 * @return double table containing in [0] flatness and in [1] elongation
	 */
	public double[] computeFlatnessAndElongation(double label) {
		double[] shapeParameters = new double[2];
		double[] tEigenValues    = computeEigenValue3D(label);
		shapeParameters[0] = Math.sqrt(tEigenValues[1] / tEigenValues[0]);
		shapeParameters[1] = Math.sqrt(tEigenValues[2] / tEigenValues[1]);
		return shapeParameters;
	}
	
	
	/**
	 * Method which determines object barycenter
	 *
	 * @param unit           if true the coordinates of barycenter are in µm.
	 * @param imagePlusInput ImagePlus of labelled image
	 * @param label          double label of interest
	 *
	 * @return VoxelRecord the barycenter of the object of interest
	 */
	public VoxelRecord computeBarycenter3D(boolean unit,
	                                       ImagePlus imagePlusInput,
	                                       double label) {
		ImageStack  imageStackInput       = imagePlusInput.getImageStack();
		VoxelRecord voxelRecordBarycenter = new VoxelRecord();
		int         count                 = 0;
		long        sx                    = 0;
		long        sy                    = 0;
		long        sz                    = 0;
		double      voxelValue;
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == label) {
						sx += i;
						sy += j;
						sz += k;
						++count;
					}
				}
			}
		}
		sx /= count;
		sy /= count;
		sz /= count;
		voxelRecordBarycenter.setLocation(sx, sy, sz);
		if (unit) {
			voxelRecordBarycenter.multiplie(xCal, yCal, zCal);
		}
		return voxelRecordBarycenter;
	}
	
	
	/**
	 * Method which compute the RHF (total chromocenters volume/nucleus volume)
	 *
	 * @param imagePlusSegmented     binary ImagePlus
	 * @param imagePlusChromocenters ImagePLus of the chromocenters
	 *
	 * @return double Relative Heterochromatin Fraction compute on the Volume ratio
	 */
	public double computeVolumeRHF(ImagePlus imagePlusSegmented, ImagePlus imagePlusChromocenters) {
		double volumeCc = 0;
		//Calibration calSeg = imagePlusSegmented.getCalibration();
		//Calibration calChrom = imagePlusChomocenters.getCalibration();
		double[] tVolumeChromocenter = computeVolumeOfAllObjects(imagePlusChromocenters);
		for (double v : tVolumeChromocenter) {
			volumeCc += v;
		}
		double[] tVolumeSegmented = computeVolumeOfAllObjects(imagePlusSegmented);
		return volumeCc / tVolumeSegmented[0];
	}
	
	
	/**
	 * Detect the number of object on segmented image.
	 *
	 * @param imagePlusInput Segmented image
	 *
	 * @return int nb of object in the image
	 */
	public static int getNumberOfObject(ImagePlus imagePlusInput) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		return histogram.getNbLabels();
	}
	
	
	/**
	 * Compute an Hashmap describing the segmented object (from raw data). Key = Voxels intensity value = Number of
	 * voxels
	 * <p>
	 * If voxels ==255 in seg image add Hashmap (Voxels intensity ,+1)
	 */
	private void histogramSegmentedNucleus() {
		ImageStack imageStackRaw = rawImage.getStack();
		ImageStack imageStackSeg = imageSeg.getStack();
		Histogram  histogram     = new Histogram();
		histogram.run(rawImage);
		for (int k = 0; k < rawImage.getStackSize(); ++k) {
			for (int i = 0; i < rawImage.getWidth(); ++i) {
				for (int j = 0; j < rawImage.getHeight(); ++j) {
					double voxelValue = imageStackSeg.getVoxel(i, j, k);
					if (voxelValue == 255) {
						if (segmentedNucleusHist.containsKey(imageStackRaw.getVoxel(i, j, k))) {
							segmentedNucleusHist.put(imageStackRaw.getVoxel(i, j, k),
							                         segmentedNucleusHist.get(imageStackRaw.getVoxel(i, j, k)) + 1);
						} else {
							segmentedNucleusHist.put(imageStackRaw.getVoxel(i, j, k), 1);
						}
					} else {
						if (backgroundHist.containsKey(imageStackRaw.getVoxel(i, j, k))) {
							backgroundHist.put(imageStackRaw.getVoxel(i, j, k),
							                   backgroundHist.get(imageStackRaw.getVoxel(i, j, k)) + 1);
						} else {
							backgroundHist.put(imageStackRaw.getVoxel(i, j, k), 1);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Compute the mean intensity of the segmented object by comparing voxels intensity in the raw image and
	 * white/segmented voxels the segmented image.
	 *
	 * @return mean intensity of segmented object
	 */
	private double meanIntensity() {
		int    numberOfVoxel = 0;
		double mean          = 0;
		for (Map.Entry<Double, Integer> hist : segmentedNucleusHist.entrySet()) {
			numberOfVoxel += hist.getValue();
			mean += hist.getKey() * hist.getValue();
		}
		return numberOfVoxel != 0 ? mean / numberOfVoxel : 0;
	}
	
	
	/**
	 * Compute mean intensity of background
	 *
	 * @return mean intensity of background
	 */
	private double meanIntensityBackground() {
		double     meanIntensity = 0;
		int        voxelCounted  = 0;
		ImageStack imageStackRaw = rawImage.getStack();
		ImageStack imageStackSeg = imageSeg.getStack();
		for (int k = 0; k < rawImage.getStackSize(); ++k) {
			for (int i = 0; i < rawImage.getWidth(); ++i) {
				for (int j = 0; j < rawImage.getHeight(); ++j) {
					if (imageStackSeg.getVoxel(i, j, k) == 0) {
						meanIntensity += imageStackRaw.getVoxel(i, j, k);
						voxelCounted++;
					}
				}
			}
		}
		return voxelCounted != 0 ? meanIntensity / voxelCounted : 0;
		
	}
	
	
	/**
	 * @param label
	 * @param segImg
	 *
	 * @return
	 */
	private double objectIntensity(double label, ImagePlus segImg) {
		double     meanIntensity = 0;
		int        voxelCounted  = 0;
		ImageStack imageStackRaw = rawImage.getStack();
		ImageStack imageStackSeg = segImg.getStack();
		for (int k = 0; k < rawImage.getStackSize(); ++k) {
			for (int i = 0; i < rawImage.getWidth(); ++i) {
				for (int j = 0; j < rawImage.getHeight(); ++j) {
					if (imageStackSeg.getVoxel(i, j, k) == label) {
						meanIntensity += imageStackRaw.getVoxel(i, j, k);
						voxelCounted++;
					}
					
				}
			}
		}
		return voxelCounted != 0 ? meanIntensity / voxelCounted : 0;
	}
	
	
	public void setRawImage(ImagePlus raw) {
		this.rawImage = raw;
	}
	
	
	/**
	 * @param input
	 *
	 * @return
	 */
	public double[] computeIntensityofAllObjects(ImagePlus input) {
		Histogram histogram = new Histogram();
		histogram.run(input);
		double nucAvgIntesnity = meanIntensity();
		
		double[] tlabel     = histogram.getLabels();
		double[] tIntensity = new double[tlabel.length];
		for (int i = 0; i < tlabel.length; ++i) {
			double meh = objectIntensity(tlabel[i], input);
			tIntensity[i] = meh / nucAvgIntesnity;
			LOGGER.debug("Object: {}\tNucleus intensity: {}\tRatio: {}", tlabel[i], nucAvgIntesnity, tIntensity[i]);
		}
		return tIntensity;
	}
	
	
	/**
	 * Compute the standard deviation of the mean intensity
	 *
	 * @return the standard deviation of the mean intensity of segmented object
	 *
	 * @see Measure3D#meanIntensity()
	 */
	private double standardDeviationIntensity(Double mean) {
		int    numberOfVoxel = 0;
		double std           = 0;
		for (Map.Entry<Double, Integer> hist : segmentedNucleusHist.entrySet()) {
			numberOfVoxel += hist.getValue();
			std = Math.abs(hist.getKey() * hist.getValue() - hist.getValue() * mean);
		}
		return std / (numberOfVoxel - 1);
		
		
	}
	
	
	/**
	 * Find the maximum intensity voxel of segmented object
	 *
	 * @return the maximum intensity voxel of segmented object
	 */
	private double maxIntensity() {
		double maxIntensity = 0;
		for (Map.Entry<Double, Integer> entry : segmentedNucleusHist.entrySet()) {
			if (maxIntensity == 0 || entry.getKey().compareTo(maxIntensity) > 0) {
				maxIntensity = entry.getKey();
			}
		}
		return maxIntensity;
		
	}
	
	
	/**
	 * Find the minimum intensity voxel of segmented object
	 *
	 * @return the minimum intensity voxel of segmented object
	 */
	private double minIntensity() {
		double minIntensity = 0;
		for (Map.Entry<Double, Integer> entry : segmentedNucleusHist.entrySet()) {
			if (minIntensity == 0 || entry.getKey().compareTo(minIntensity) < 0) {
				minIntensity = entry.getKey();
			}
		}
		return minIntensity;
		
	}
	
	
	/**
	 * Compute the median intensity value of raw image voxel
	 *
	 * @return median intensity value of raw image voxel
	 */
	public double medianComputingImage() {
		double    voxelMedianValue = 0;
		Histogram histogram        = new Histogram();
		histogram.run(rawImage);
		Map<Double, Integer> segNucHisto = histogram.getHistogram();
		
		int medianElementStop = rawImage.getHeight() * rawImage.getWidth() * rawImage.getNSlices() / 2;
		int increment         = 0;
		for (Map.Entry<Double, Integer> entry : segNucHisto.entrySet()) {
			increment += entry.getValue();
			if (increment > medianElementStop) {
				voxelMedianValue = entry.getKey();
				break;
			}
		}
		return voxelMedianValue;
	}
	
	
	private double medianIntensityNucleus() {
		double voxelMedianValue     = 0;
		int    numberOfVoxelNucleus = 0;
		for (int f : segmentedNucleusHist.values()) {
			numberOfVoxelNucleus += f;
		}
		int medianElementStop = numberOfVoxelNucleus / 2;
		int increment         = 0;
		for (Map.Entry<Double, Integer> entry : segmentedNucleusHist.entrySet()) {
			increment += entry.getValue();
			if (increment > medianElementStop) {
				voxelMedianValue = entry.getKey();
				break;
			}
		}
		return voxelMedianValue;
	}
	
	
	private double medianIntensityBackground() {
		double voxelMedianValue = 0;
		int    numberOfVoxelBG  = 0;
		for (int f : segmentedNucleusHist.values()) {
			numberOfVoxelBG += f;
		}
		int medianElementStop = numberOfVoxelBG / 2;
		int increment         = 0;
		for (Map.Entry<Double, Integer> entry : backgroundHist.entrySet()) {
			increment += entry.getValue();
			if (increment > medianElementStop) {
				voxelMedianValue = entry.getKey();
				break;
			}
		}
		return voxelMedianValue;
	}
	
	
	/**
	 * list of parameters compute in this method returned in tabulated format
	 *
	 * @return list of parameters compute in this method returned in tabulated format
	 */
	public String nucleusParameter3D() {
		String results;
		// double volume = computeVolumeObject2(255);
		
		double volume = computeVolumeObjectML();
		//double surfaceAreaNew = computeComplexSurface();
		results = rawImage.getTitle() + ","
		       //  + computeVolumeObject2(255) + "\t"
		       + computeVolumeObjectML() + ","
		       + computeFlatnessAndElongation(255)[0] + ","
		       + computeFlatnessAndElongation(255)[1] + ","
		       + equivalentSphericalRadius(volume) + ","
		       //+ surfaceAreaNew + "\t"
		       //+ computeSphericity(volume, surfaceAreaNew)+ "\t"
		       + meanIntensity() + ","
		       + meanIntensityBackground() + ","
		       + standardDeviationIntensity(meanIntensity()) + ","
		       + minIntensity() + ","
		       + maxIntensity() + ","
		       + medianComputingImage() + ","
		       + medianIntensityNucleus() + ","
		       + medianIntensityBackground() + ","
		       + rawImage.getHeight() * rawImage.getWidth() * rawImage.getNSlices();
		
		return results;
	}
	
}