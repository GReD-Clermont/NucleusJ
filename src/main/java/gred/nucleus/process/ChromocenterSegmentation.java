package gred.nucleus.process;

import gred.nucleus.plugins.ChromocenterParameters;
import gred.nucleus.utils.Histogram;
import gred.nucleus.utils.ImageSaver;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.GaussianBlur3D;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;


/**
 * Parent class for chromocenter  segmentation
 */
public class ChromocenterSegmentation {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final ImagePlus[] raw;
	private final ImagePlus[] segNuc;
	
	private final ChromocenterParameters chromocenterParams;
	
	private final String output;
	
	private final int    neigh;
	private final double factor;
	
	private int nbPixelNuc;
	
	private double avgNucIntensity;
	private double stdDevNucIntensity;
	private double threshold;
	
	
	/**
	 * Contructor for 3D images
	 *
	 * @param raw    : raw image input
	 * @param segNuc : segmented/Binary nucleus associated image
	 */
	public ChromocenterSegmentation(ImagePlus[] raw,
	                                ImagePlus[] segNuc,
	                                String outputFileName,
	                                ChromocenterParameters chromocenterParameters) {
		chromocenterParams = chromocenterParameters;
		this.raw = raw;
		this.segNuc = segNuc;
		this.output = outputFileName;
		setNbPixelNuc3D();
		int initialV = chromocenterParameters.neighbours;
		LOGGER.info("\t{}", initialV);
		if (!chromocenterParams.noChange && nbPixelNuc * getVoxelVolume3D() > 50) {
			this.neigh = (int) (chromocenterParams.neighbours * 2.5);
			this.factor = chromocenterParams.factor + 1;
		} else {
			this.neigh = chromocenterParams.neighbours;
			this.factor = chromocenterParams.factor;
		}
		LOGGER.info("\t{} {}\n", neigh, factor);
	}
	
	
	/**
	 * Method running chromocenters segmentation. Simple algorithms description : 1- Gaussian blur 2- Image gradient :
	 * for each voxels computing sum of difference with X (_neigh parameter) neighborhood. 3- Thresholding value : keep
	 * voxels having value higher mean nucleus intensity plus X (_factor parameter) standard deviation value 4-
	 * Binarization of threshold image 5- Connected component computation from binarized image
	 */
	
	public void runCC3D(String pathGradient) {
		if (chromocenterParams.gaussianOnRaw) {
			GaussianBlur3D.blur(raw[0],
			                    chromocenterParams.gaussianBlurXsigma,
			                    chromocenterParams.gaussianBlurYsigma,
			                    chromocenterParams.gaussianBlurZsigma);
		}
		
		ImagePlus   imageGradient = imgGradient3D();
		Calibration cal           = raw[0].getCalibration();
		GaussianBlur3D.blur(imageGradient,
		                    chromocenterParams.gaussianBlurXsigma,
		                    chromocenterParams.gaussianBlurYsigma,
		                    chromocenterParams.gaussianBlurZsigma);
		imageGradient.setCalibration(cal);
		ImageSaver.saveFile(imageGradient, pathGradient);
		computeAverage3D(imageGradient);
		computeStdDev3D(imageGradient);
		this.threshold = avgNucIntensity + factor * stdDevNucIntensity;
		LOGGER.info("{} {} avg {} std {}", output, threshold, avgNucIntensity, stdDevNucIntensity);
		imageGradient = binarize3D(imageGradient);
		imageGradient.setCalibration(cal);
		if (chromocenterParams.sizeFilterConnectedComponent) {
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
		for (int k = 0; k < raw[0].getNSlices(); ++k) {
			FloatProcessor ipDiff = new FloatProcessor(is.getWidth(), is.getHeight());
			for (int i = 0; i < raw[0].getWidth(); ++i) {
				for (int j = 0; j < raw[0].getHeight(); ++j) {
					double sum    = 0;
					int    nb     = 0;
					double valueA = is.getVoxel(i, j, k);
					if (isBin.getVoxel(i, j, k) > 0) {
						for (int kk = k - neigh; kk < k + neigh; ++kk) {
							for (int ii = i - neigh; ii < i + neigh; ++ii) {
								for (int jj = j - neigh; jj < j + neigh; ++jj) {
									if (ii < raw[0].getWidth() &&
									    jj < raw[0].getHeight() &&
									    kk < raw[0].getNSlices()) {
										if (isBin.getVoxel(ii, jj, kk) > 0) {
											double valueB = is.getVoxel(ii, jj, kk);
											if (Double.isNaN(is.getVoxel(i, j, k))) {
												valueA = 0;
											}
											if (Double.isNaN(is.getVoxel(ii, jj, kk))) {
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
						ipDiff.setf(i, j, (float) sum);
					}
					
				}
			}
			isDiff.setProcessor(ipDiff, k + 1);
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
	private void setNbPixelNuc2D() {
		ImageProcessor ip = segNuc[0].getProcessor();
		for (int i = 0; i < raw[0].getWidth(); ++i) {
			for (int j = 0; j < raw[0].getHeight(); ++j) {
				if (ip.get(i, j) > 1) {
					this.nbPixelNuc++;
				}
			}
			
		}
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
	 * Method to binarize image with threshold
	 *
	 * @param img : image gradient
	 *
	 * @return binarized image
	 */
	private ImagePlus binarize3D(ImagePlus img) {
		ImagePlus  imgCc = img.duplicate();
		ImageStack is    = imgCc.getStack();
		for (int k = 0; k < raw[0].getNSlices(); ++k) {
			for (int i = 0; i < raw[0].getWidth(); ++i) {
				for (int j = 0; j < raw[0].getHeight(); ++j) {
					if (is.getVoxel(i, j, k) > threshold) {
						is.setVoxel(i, j, k, 255);
					} else {
						is.setVoxel(i, j, k, 0);
					}
				}
			}
		}
		imgCc = BinaryImages.componentsLabeling(imgCc, 26, 16);
		return imgCc;
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
			if ((valeur * getVoxelVolume3D() <
			     chromocenterParams.minSizeConnectedComponent ||
			     valeur * getVoxelVolume3D() >
			     chromocenterParams.maxSizeConnectedComponent) && valeur > 1) {
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
	public double getVoxelVolume3D() {
		return raw[0].getCalibration().pixelWidth *
		       raw[0].getCalibration().pixelHeight *
		       raw[0].getCalibration().pixelDepth;
	}
	
	
	/**
	 * Compute volume voxel of current image analysed
	 *
	 * @return voxel volume
	 */
	public double getPixelSurface2D() {
		return raw[0].getCalibration().pixelWidth *
		       raw[0].getCalibration().pixelHeight;
	}
	
}
