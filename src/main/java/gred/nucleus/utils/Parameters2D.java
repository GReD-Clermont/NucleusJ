package gred.nucleus.utils;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;


public class Parameters2D {
	private ImagePlus    img;
	private ImagePlus    raw;
	private ResultsTable resultsTable;
	private int          sumIntensity;
	private int          avgIntensity;
	private int          nbPixel;
	private float        label;
	
	
	/**
	 * @param bin
	 */
	public Parameters2D(ImagePlus bin) {
		this.img = bin;
	}
	
	
	/**
	 * @param bin
	 * @param raw
	 * @param label
	 */
	public Parameters2D(ImagePlus bin, ImagePlus raw, float label) {
		this.img = bin;
		sumIntensity = 0;
		nbPixel = 0;
		this.label = label;
		this.raw = raw;
		getTotalIntensity();
	}
	
	
	/**
	 * method computing the different parameters
	 *
	 * @return a resultTable containinig the results
	 */
	public void computePrameters() {
		resultsTable = new ResultsTable();
		ParticleAnalyzer particleAnalyser = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE,
		                                                         Measurements.AREA +
		                                                         Measurements.CIRCULARITY +
		                                                         Measurements.PERIMETER,
		                                                         resultsTable, 1,
		                                                         Double.MAX_VALUE, 0, 1);
		ImageProcessor ip = img.getProcessor();
		ip.invertLut();
		img.setProcessor(ip);
		particleAnalyser.analyze(img);
	}
	
	
	/**
	 *
	 */
	private void getTotalIntensity() {
		ImageConverter ic = new ImageConverter(raw);
		ic.convertToGray8();
		ImageProcessor ipBin = img.getProcessor();
		ImageProcessor ipRaw = raw.getProcessor();
		
		for (int i = 0; i < img.getWidth(); ++i) {
			for (int j = 0; j < img.getHeight(); ++j) {
				if (ipBin.getPixelValue(i, j) == label) {
					ipBin.setf(i, j, 255);
					sumIntensity += ipRaw.getPixelValue(i, j);
					nbPixel++;
				} else {
					ipBin.setf(i, j, 0);
				}
			}
			
		}
		ic = new ImageConverter(img);
		ic.convertToGray8();
		avgIntensity = sumIntensity / nbPixel;
		//_img.show();
	}
	
	
	/**
	 * Getter of the Aspect ratio vaue The aspect ratio of the particle’s fitted ellipse, i.e., [M ajor Axis]/Minor
	 * Axis] . If Fit Ellipse is selected the Major and Minor axis are displayed. Uses the heading AR. (Soucre :
	 * ImageJ)
	 *
	 * @return double aspect ratio value
	 */
	public double getAspectRatio() {
		return resultsTable.getValue("AR", 0);
	}
	
	
	public double getPerim() {
		return resultsTable.getValue("Perim.", 0);
	}
	
	
	public double getArea() {
		return resultsTable.getValue("Area", 0);
	}
	
	
	public double getRound() {
		return resultsTable.getValue("Round", 0);
	}
	
	
	public double getSolidity() {
		return resultsTable.getValue("Solidity", 0);
	}
	
	
	public int getAvgIntensity() {
		return this.avgIntensity;
	}
	
	
	public int getNbPixelObject() {
		return this.nbPixel;
	}
	
	
	/**
	 * Getter of the circularity
	 * <p>
	 * Circularity Particles with size circularity values outside the range specified in this field are also ignored.
	 * Circularity = (4π × [Area ] / [P erimeter]2 ) , see Set Measurements. . . ) ranges from 0 (infinitely elongated
	 * polygon) to 1 (perfect circle). (Soucre : ImageJ)
	 *
	 * @return double circularity value
	 */
	
	public double getCirculairty() {
		return resultsTable.getValue("Circ.", 0);
	}
	
	
}

