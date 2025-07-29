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
package fr.igred.nucleus.process;

import fr.igred.nucleus.plugins.ChromocenterParameters;
import fr.igred.nucleus.utils.Histogram;
import fr.igred.nucleus.io.ImageSaver;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.GaussianBlur3D;
import ij.process.FloatProcessor;
import inra.ijpb.binary.BinaryImages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static fr.igred.nucleus.utils.Thresholding.binarize;


/**
 * Parent class for chromocenter  segmentation
 */
public class ChromocenterSegmentation {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final ImagePlus[] raw;
	private final ImagePlus[] segNuc;
	
	private final ChromocenterParameters params;
	
	private final String output;
	
	private final int    neigh;
	private final double factor;
	
	private int nbPixelNuc;
	
	private double avgNucIntensity;
	private double stdDevNucIntensity;
	
	
	/**
	 * Contructor for 3D images
	 *
	 * @param raw    : raw image input
	 * @param segNuc : segmented/Binary nucleus associated image
	 */
	public ChromocenterSegmentation(ImagePlus[] raw,
	                                ImagePlus[] segNuc,
	                                String outputFileName,
	                                ChromocenterParameters params) {
		this.params = params;
		this.raw = raw;
		this.segNuc = segNuc;
		this.output = outputFileName;
		this.nbPixelNuc = 0;
		setNbPixelNuc3D();
		int initialV = params.getNeighbours();
		LOGGER.info("\t{}", initialV);
		if (!this.params.isNoChange() && nbPixelNuc * getVoxelVolume3D() > 50) {
			this.neigh = (int) (this.params.getNeighbours() * 2.5);
			this.factor = this.params.getFactor() + 1;
		} else {
			this.neigh = this.params.getNeighbours();
			this.factor = this.params.getFactor();
		}
		LOGGER.info("\t{} {}{}", neigh, factor, System.lineSeparator());
	}
	
	
	/**
	 * Method running chromocenters segmentation. Simple algorithms description : 1- Gaussian blur 2- Image gradient :
	 * for each voxels computing sum of difference with X (_neigh parameter) neighborhood. 3- Thresholding value : keep
	 * voxels having value higher mean nucleus intensity plus X (_factor parameter) standard deviation value 4-
	 * Binarization of threshold image 5- Connected component computation from binarized image
	 */
	
	public void runCC3D(String pathGradient) {
		if (params.useGaussianOnRaw()) {
			GaussianBlur3D.blur(raw[0],
			                    params.getXGaussianSigma(),
			                    params.getYGaussianSigma(),
			                    params.getZGaussianSigma());
		}
		
		ImagePlus   imageGradient = imgGradient3D();
		Calibration cal           = raw[0].getCalibration();
		GaussianBlur3D.blur(imageGradient,
		                    params.getXGaussianSigma(),
		                    params.getYGaussianSigma(),
		                    params.getZGaussianSigma());
		imageGradient.setCalibration(cal);
		ImageSaver.saveFile(imageGradient, pathGradient);
		computeAverage3D(imageGradient);
		computeStdDev3D(imageGradient);
		double threshold = avgNucIntensity + factor * stdDevNucIntensity;
		LOGGER.info("{} {} avg {} std {}", output, threshold, avgNucIntensity, stdDevNucIntensity);
		binarize(imageGradient, threshold);
		imageGradient = BinaryImages.componentsLabeling(imageGradient, 26, 16);
		imageGradient.setCalibration(cal);
		if (params.isSizeFiltered()) {
			imageGradient = componentSizeFilter3D(imageGradient);
			imageGradient.setCalibration(cal);
		}
		
		ImageSaver.saveFile(imageGradient, output);
	}
	
	
	/**
	 * Create and save the diff image. for each pixel compute the new value computing the average subtraction between
	 * the pixel of interest and all pixel inside the neighbor 3
	 *
	 * @return : gradient image
	 */
	public ImagePlus imgGradient3D() {
		ImageStack is     = raw[0].getStack();
		ImageStack isBin  = segNuc[0].getStack();
		ImageStack isDiff = new ImageStack(raw[0].getWidth(), raw[0].getHeight(), raw[0].getNSlices());
		for (int z = 0; z < raw[0].getNSlices(); ++z) {
			FloatProcessor ipDiff = new FloatProcessor(is.getWidth(), is.getHeight());
			for (int x = 0; x < raw[0].getWidth(); ++x) {
				for (int y = 0; y < raw[0].getHeight(); ++y) {
					double sum    = 0;
					int    nb     = 0;
					double valueA = is.getVoxel(x, y, z);
					if (isBin.getVoxel(x, y, z) > 0) {
						for (int zz = z - neigh; zz < z + neigh; ++zz) {
							for (int xx = x - neigh; xx < x + neigh; ++xx) {
								for (int yy = y - neigh; yy < y + neigh; ++yy) {
									if (xx < raw[0].getWidth() &&
									    yy < raw[0].getHeight() &&
									    zz < raw[0].getNSlices()) {
										if (isBin.getVoxel(xx, yy, zz) > 0) {
											double valueB = is.getVoxel(xx, yy, zz);
											if (Double.isNaN(is.getVoxel(x, y, z))) {
												valueA = 0;
											}
											if (Double.isNaN(is.getVoxel(xx, yy, zz))) {
												valueB = 0;
											}
											double plop = valueA - valueB;
											sum += plop;
										}
										nb++;
									}
								}
							}
						}
						sum = nb != 0 ? sum / nb : Double.NaN;
						ipDiff.setf(x, y, (float) sum);
					}
					
				}
			}
			isDiff.setProcessor(ipDiff, z + 1);
		}
		ImagePlus imgDiff = new ImagePlus();
		imgDiff.setStack(isDiff);
		return imgDiff;
	}
	
	
	/**
	 * Method to compute image mean intensity only on nucleus mask.
	 *
	 * @param : raw image
	 */
	private void computeAverage3D(ImagePlus imgDiff) {
		ImageStack isRaw = imgDiff.getStack();
		ImageStack isSeg = segNuc[0].getStack();
		double     sum   = 0;
		for (int k = 0; k < raw[0].getNSlices(); ++k) {
			for (int i = 0; i < raw[0].getWidth(); ++i) {
				for (int j = 0; j < raw[0].getHeight(); ++j) {
					if (isSeg.getVoxel(i, j, k) > 1) {
						sum += isRaw.getVoxel(i, j, k);
					}
				}
			}
		}
		this.avgNucIntensity = sum / nbPixelNuc;
	}
	
	
	/**
	 * Method to compute standard deviation of intensity average from nucleus mask.
	 *
	 * @param : image gradient
	 */
	private void computeStdDev3D(ImagePlus imgDiff) {
		ImageStack is    = imgDiff.getStack();
		ImageStack isSeg = segNuc[0].getStack();
		double     sum   = 0;
		for (int k = 0; k < raw[0].getNSlices(); ++k) {
			for (int i = 0; i < raw[0].getWidth(); ++i) {
				for (int j = 0; j < raw[0].getHeight(); ++j) {
					if (isSeg.getVoxel(i, j, k) > 1) {
						sum += (is.getVoxel(i, j, k) - avgNucIntensity) *
						       (is.getVoxel(i, j, k) - avgNucIntensity);
					}
				}
			}
		}
		this.stdDevNucIntensity = Math.sqrt(sum / nbPixelNuc);
	}
	
	
	/**
	 *
	 */
	private void setNbPixelNuc3D() {
		ImageStack isSeg = segNuc[0].getStack();
		for (int k = 0; k < raw[0].getNSlices(); ++k) {
			for (int i = 0; i < raw[0].getWidth(); ++i) {
				for (int j = 0; j < raw[0].getHeight(); ++j) {
					if (isSeg.getVoxel(i, j, k) > 1) {
						this.nbPixelNuc++;
					}
				}
			}
		}
	}
	
	
	/**
	 * @param imageGradient
	 *
	 * @return
	 */
	private ImagePlus componentSizeFilter3D(ImagePlus imageGradient) {
		Histogram histogram = new Histogram();
		histogram.run(imageGradient);
		histogram.getHistogram();
		Map<Double, Integer> parcour = histogram.getHistogram();
		ImagePlus            imgCc   = imageGradient.duplicate();
		ImageStack           is      = imgCc.getStack();
		for (Map.Entry<Double, Integer> entry : parcour.entrySet()) {
			Double  cle    = entry.getKey();
			Integer valeur = entry.getValue();
			if ((valeur * getVoxelVolume3D() < params.getMinSize() ||
			     valeur * getVoxelVolume3D() > params.getMaxSize()) &&
			    valeur > 1) {
				for (int k = 0; k < raw[0].getNSlices(); ++k) {
					for (int i = 0; i < raw[0].getWidth(); ++i) {
						for (int j = 0; j < raw[0].getHeight(); ++j) {
							if (is.getVoxel(i, j, k) == cle) {
								is.setVoxel(i, j, k, 0);
							}
						}
					}
				}
			}
		}
		imgCc = BinaryImages.componentsLabeling(imgCc, 26, 16);
		return imgCc;
	}
	
	
	/**
	 * Compute volume voxel of current image analysed
	 *
	 * @return voxel volume
	 */
	private double getVoxelVolume3D() {
		return raw[0].getCalibration().pixelWidth *
		       raw[0].getCalibration().pixelHeight *
		       raw[0].getCalibration().pixelDepth;
	}
	
}
