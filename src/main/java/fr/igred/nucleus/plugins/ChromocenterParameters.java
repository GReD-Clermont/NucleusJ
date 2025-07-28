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

import fr.igred.omero.Client;


public class ChromocenterParameters extends PluginParameters {
	/** Activation of gaussian filter */
	public boolean gaussianOnRaw;
	
	/** Factor for gradient thresholding */
	public double factor = 1.5;
	
	/** Number of neighbour to explore */
	public int neighbours = 3;
	
	/** Folder containing segmented images */
	public String segInputFolder;
	
	public Client omeroClient;
	
	/** Gaussian parameters */
	public double xGaussianSigma = 1;
	public double yGaussianSigma = 1;
	public double zGaussianSigma = 2;
	
	/** Filter connected component */
	public boolean sizeFilter;
	
	/** Filter connected component */
	public boolean noChange;
	
	/** Max volume connected component filter */
	public double maxSize = 3;
	
	/** Min volume connected component filter */
	public double minSize = 0.003;
	
	
	/** ChromocenterParameters Constructor For OMERO */
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              Client omeroClient,
	                              double xGaussianSigma,
	                              double yGaussianSigma,
	                              double zGaussianSigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian,
	                              boolean sizeFilter,
	                              double maxSize,
	                              double minSize) {
		super(inputFolder, outputFolder, xGaussianSigma, yGaussianSigma, zGaussianSigma, gaussian);
		this.segInputFolder = segInputFolder;
		this.omeroClient = omeroClient;
		this.xGaussianSigma = xGaussianSigma;
		this.yGaussianSigma = yGaussianSigma;
		this.zGaussianSigma = zGaussianSigma;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
		this.sizeFilter = sizeFilter;
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
	                              boolean sizeFilter,
	                              double maxSize,
	                              double minSize) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.xGaussianSigma = xGaussianSigma;
		this.yGaussianSigma = yGaussianSigma;
		this.zGaussianSigma = zGaussianSigma;
		this.gaussianOnRaw = gaussian;
		this.sizeFilter = sizeFilter;
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
}