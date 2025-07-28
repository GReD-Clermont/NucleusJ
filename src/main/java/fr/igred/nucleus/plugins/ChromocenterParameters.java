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
package fr.igred.nucleus.plugins;


public class ChromocenterParameters extends PluginParameters {
	/** Activation of gaussian filter */
	private boolean gaussianOnRaw;
	
	/** Factor for gradient thresholding */
	private double factor = 1.5;
	
	/** Number of neighbour to explore */
	private int neighbours = 3;
	
	/** Folder containing segmented images */
	private String segInputFolder;
	
	/** Gaussian parameters */
	private double xGaussianSigma = 1;
	private double yGaussianSigma = 1;
	private double zGaussianSigma = 2;
	
	/** Filter connected component */
	private boolean sizeFiltered;
	
	
	private boolean noChange;
	
	/** Max volume connected component filter */
	private double maxSize = 3;
	
	/** Min volume connected component filter */
	private double minSize = 0.003;
	
	
	/** ChromocenterParameters Constructor For OMERO */
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double xGaussianSigma,
	                              double yGaussianSigma,
	                              double zGaussianSigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian,
	                              boolean sizeFiltered,
	                              double maxSize,
	                              double minSize) {
		super(inputFolder, outputFolder, xGaussianSigma, yGaussianSigma, zGaussianSigma, gaussian);
		this.segInputFolder = segInputFolder;
		this.xGaussianSigma = xGaussianSigma;
		this.yGaussianSigma = yGaussianSigma;
		this.zGaussianSigma = zGaussianSigma;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
		this.sizeFiltered = sizeFiltered;
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double xGaussianSigma,
	                              double yGaussianSigma,
	                              double zGaussianSigma,
	                              boolean gaussian,
	                              boolean sizeFiltered,
	                              double maxSize,
	                              double minSize) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.xGaussianSigma = xGaussianSigma;
		this.yGaussianSigma = yGaussianSigma;
		this.zGaussianSigma = zGaussianSigma;
		this.gaussianOnRaw = gaussian;
		this.sizeFiltered = sizeFiltered;
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
	
	/**
	 * Get whether to use Gaussian filter on raw images
	 *
	 * @return See above.
	 */
	public boolean useGaussianOnRaw() {
		return gaussianOnRaw;
	}
	
	
	/**
	 * Set whether to use Gaussian filter on raw images
	 *
	 * @param gaussianOnRaw See above.
	 */
	public void setGaussianOnRaw(boolean gaussianOnRaw) {
		this.gaussianOnRaw = gaussianOnRaw;
	}
	
	
	/**
	 * Get the gradient factor for thresholding
	 *
	 * @return See above.
	 */
	public double getFactor() {
		return factor;
	}
	
	
	/**
	 * Set the gradient factor for thresholding
	 *
	 * @param factor See above.
	 */
	public void setFactor(double factor) {
		this.factor = factor;
	}
	
	
	/**
	 * Get the number of neighbours to explore
	 *
	 * @return See above.
	 */
	public int getNeighbours() {
		return neighbours;
	}
	
	
	/**
	 * Set the number of neighbours to explore
	 *
	 * @param neighbours See above.
	 */
	public void setNeighbours(int neighbours) {
		this.neighbours = neighbours;
	}
	
	
	/**
	 * Get the segmented input folder
	 *
	 * @return See above.
	 */
	public String getSegmentedInputFolder() {
		return segInputFolder;
	}
	
	
	/**
	 * Set the segmented input folder
	 *
	 * @param segInputFolder See above.
	 */
	public void setSegmentedInputFolder(String segInputFolder) {
		this.segInputFolder = segInputFolder;
	}
	
	
	/**
	 * Get X Gaussian sigma value
	 *
	 * @return See above.
	 */
	public double getXGaussianSigma() {
		return xGaussianSigma;
	}
	
	
	/**
	 * Set X Gaussian sigma value
	 *
	 * @param xGaussianSigma See above.
	 */
	public void setXGaussianSigma(double xGaussianSigma) {
		this.xGaussianSigma = xGaussianSigma;
	}
	
	
	/**
	 * Get Y Gaussian sigma value
	 *
	 * @return See above.
	 */
	public double getYGaussianSigma() {
		return yGaussianSigma;
	}
	
	
	/**
	 * Set Y Gaussian sigma value
	 *
	 * @param yGaussianSigma See above.
	 */
	public void setYGaussianSigma(double yGaussianSigma) {
		this.yGaussianSigma = yGaussianSigma;
	}
	
	
	/**
	 * Get Z Gaussian sigma value
	 *
	 * @return See above.
	 */
	public double getZGaussianSigma() {
		return zGaussianSigma;
	}
	
	
	/**
	 * Set Z Gaussian sigma value
	 *
	 * @param zGaussianSigma See above.
	 */
	public void setZGaussianSigma(double zGaussianSigma) {
		this.zGaussianSigma = zGaussianSigma;
	}
	
	
	/**
	 * Get whether to filter connected components by size
	 *
	 * @return See above.
	 */
	public boolean isSizeFiltered() {
		return sizeFiltered;
	}
	
	
	/**
	 * Set whether to filter connected components by size
	 *
	 * @param sizeFiltered See above.
	 */
	public void setSizeFiltered(boolean sizeFiltered) {
		this.sizeFiltered = sizeFiltered;
	}
	
	
	/**
	 * Get the max. volume for connected component filter
	 *
	 * @return See above.
	 */
	public double getMaxSize() {
		return maxSize;
	}
	
	
	/**
	 * Set the max. volume for connected component filter
	 *
	 * @param maxSize See above.
	 */
	public void setMaxSize(double maxSize) {
		this.maxSize = maxSize;
	}
	
	
	/**
	 * Get the min. volume for connected component filter
	 *
	 * @return See above.
	 */
	public double getMinSize() {
		return minSize;
	}
	
	
	/**
	 * Set the min. volume for connected component filter
	 *
	 * @param minSize See above.
	 */
	public void setMinSize(double minSize) {
		this.minSize = minSize;
	}
	
	
	public boolean isNoChange() {
		return noChange;
	}
	
	
	public void setNoChange(boolean noChange) {
		this.noChange = noChange;
	}
	
}