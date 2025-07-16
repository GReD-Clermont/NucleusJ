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
	public double gaussianBlurXsigma = 1;
	public double gaussianBlurYsigma = 1;
	public double gaussianBlurZsigma = 2;
	
	/** Filter connected component */
	public boolean sizeFilterConnectedComponent;
	
	/** Filter connected component */
	public boolean noChange;
	
	/** Max volume connected component filter */
	public double maxSizeConnectedComponent = 3;
	
	/** Min volume connected component filter */
	public double minSizeConnectedComponent = 0.003;
	
	
	/** ChromocenterParameters Constructor For OMERO */
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              Client omeroClient,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder, gaussianBlurXsigma, gaussianBlurYsigma, gaussianBlurZsigma, gaussian);
		this.segInputFolder = segInputFolder;
		this.omeroClient = omeroClient;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
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
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
	}
	
}