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
package fr.igred.nucleus.utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Running a convex hull algorithm for each axis combined
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class ConvexHullImageMaker {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private List<Double> listLabel;
	
	// Axes name used to determine the order of axes, default is "xy"
	private String axesName = "xy";
	
	
	/**
	 * Throws an exception (used when the axis name is not valid)
	 *
	 * @param axes axes name
	 *
	 * @throws IllegalArgumentException if axes name is not valid
	 */
	private static void wrongAxesName(String axes) {
		LOGGER.error("Invalid axes name: {}", axes);
		throw new IllegalArgumentException("Invalid axes name: " + axes);
	}
	
	
	/**
	 * Run the convex hull algorithm on the image input for a given axe
	 *
	 * @param imagePlusBinary input imagePlus
	 *
	 * @return segmented image in axes concerned corrected by a convex hull algorithm
	 *
	 * @see ConvexHullSegmentation
	 */
	@SuppressWarnings("HardcodedFileSeparator") // ratio, not file separator
	public ImagePlus runConvexHullDetection(ImagePlus imagePlusBinary) {
		LOGGER.debug("Computing convex hull algorithm for axes {}.", axesName);
		ImagePlus imagePlusCorrected = new ImagePlus();
		int       depth;
		int       width;
		int       height;
		// Defining plane
		if ("xy".equals(axesName)) {
			width = imagePlusBinary.getWidth();
			height = imagePlusBinary.getHeight();
			depth = imagePlusBinary.getNSlices();
		} else if ("xz".equals(axesName)) {
			width = imagePlusBinary.getWidth();
			height = imagePlusBinary.getNSlices();
			depth = imagePlusBinary.getHeight();
		} else {
			width = imagePlusBinary.getHeight();
			height = imagePlusBinary.getNSlices();
			depth = imagePlusBinary.getWidth();
		}
		// Create 2D image used to create each slice (depth) of a plane
		ImageProcessor imagePlusBlack = new ByteProcessor(width, height);
		// Create a new image stack to store the result
		ImageStack imageStackOutput = new ImageStack(width, height);
		for (int k = 0; k < depth; ++k) {
			LOGGER.trace("Processing slice {}/{} of plane \"{}\"", k, depth, axesName);
			
			// Return image with labelled components (& initialize listLabel)
			double[][] image = giveTable(imagePlusBinary, width, height, k);
			
			ImageProcessor ip;
			// Calculate boundaries
			if (listLabel.size() == 1) {  // If 1 single connected component
				LOGGER.trace("Processing the only label {} on slice {}/{}",
				             listLabel.get(0), k, depth);
				// List the voxels of boundary of the component
				List<VoxelRecord> lVoxelBoundary = detectVoxelBoundary(image, listLabel.get(0), k);
				// If component is big enough
				if (lVoxelBoundary.size() > 5) {
					// Create temporary image of the component using the convex hull detection algorithm
					ip = imageMaker(lVoxelBoundary, width, height);
				} else {
					ip = imagePlusBlack.duplicate();
				}
			} else if (listLabel.size() > 1) { // If several connected components
				ip = imagePlusBlack.duplicate();
				for (Double label : listLabel) {
					LOGGER.trace("Processing label {} ({}/{}) on slice: {}/{}",
					             label, listLabel.indexOf(label) + 1, listLabel.size(), k, depth);
					// List the voxels of boundary of the component
					List<VoxelRecord> lVoxelBoundary = detectVoxelBoundary(image, label, k);
					if (lVoxelBoundary.size() > 5) { // When the component is big enough make image
						// Create temporary image of the component using the convex hull detection algorithm
						ImageProcessor tmpProcessor = imageMaker(lVoxelBoundary, width, height);
						
						for (int i = 0; i < width; ++i) {
							// For each labelled voxels of the component put a corresponding white voxel on the result
							for (int j = 0; j < height; ++j) {
								if (tmpProcessor.get(i, j) > 0) {
									ip.set(i, j, 255);
								}
							}
						}
					}
				}
			} else { // In case nothing is found return black image
				ip = imagePlusBlack.duplicate();
			}
			// Add the image to the result
			imageStackOutput.addSlice(ip);
		}
		imagePlusCorrected.setStack(imageStackOutput);
		return imagePlusCorrected;
	}
	
	
	/**
	 * Find all the voxels of the boundaries (near black pixels)
	 *
	 * @param image image used
	 * @param label current label
	 * @param index slice index
	 *
	 * @return list of boundary voxels
	 */
	List<VoxelRecord> detectVoxelBoundary(double[][] image, double label, int index) {
		LOGGER.trace("Detecting voxel boundary.");
		List<VoxelRecord> lVoxelBoundary = new ArrayList<>();
		
		// Use axesName to determine the order of axes
		// 0: x, 1: y, 2: z
		int[] axeIndex;
		if ("yz".equals(axesName)) {
			//y, z, x
			axeIndex = new int[]{2, 0, 1};
		} else if ("xz".equals(axesName)) {
			//x, z, y
			axeIndex = new int[]{0, 2, 1};
		} else {
			// Default is xy: x, y, z
			axeIndex = new int[]{0, 1, 2};
		}
		
		// Browse through the pixels of the 2D image
		int[] xyz = {1, 1, index};
		for (int i = 1; i < image.length; ++i) {
			for (int j = 1; j < image[i].length; ++j) {
				if (image[i][j] == label) {
					// Check if the current pixel is a boundary pixel
					boolean isBoundary = image[i - 1][j] == 0 ||
					                     image[i + 1][j] == 0 ||
					                     image[i][j - 1] == 0 ||
					                     image[i][j + 1] == 0;
					if (isBoundary) {
						xyz[0] = i;
						xyz[1] = j;
						int x = xyz[axeIndex[0]];
						int y = xyz[axeIndex[1]];
						int z = xyz[axeIndex[2]];
						
						VoxelRecord voxelTest = new VoxelRecord();
						voxelTest.setLocation(x, y, z);
						lVoxelBoundary.add(voxelTest);
					}
				}
			}
		}
		return lVoxelBoundary;
	}
	
	
	/**
	 * Make binary image processor of the convex hull detection result
	 *
	 * @param lVoxelBoundary voxels of the boundaries
	 * @param width          slice width
	 * @param height         slice height
	 *
	 * @return See above.
	 */
	public ImageProcessor imageMaker(List<? extends VoxelRecord> lVoxelBoundary, int width, int height) {
		LOGGER.trace("Making image.");
		
		List<VoxelRecord> convexHull = ConvexHullDetection.runGrahamScan(axesName, lVoxelBoundary); // For testing
		return makePolygon(convexHull, width, height);
	}
	
	
	/**
	 * Connect all result voxels of the convex hull and create an image plus
	 *
	 * @param convexHull voxel of the convex hull
	 * @param width      slice width
	 * @param height     slice height
	 *
	 * @return ImagePlus result
	 */
	public ImageProcessor makePolygon(List<? extends VoxelRecord> convexHull, int width, int height) {
		ImageProcessor ip = new BinaryProcessor(new ByteProcessor(width, height));
		
		int[] tableWidth  = new int[convexHull.size() + 1];
		int[] tableHeight = new int[convexHull.size() + 1];
		for (int i = 0; i < convexHull.size(); ++i) {
			switch (axesName) {
				case "xy":
					tableWidth[i] = (int) convexHull.get(i).getI();
					tableHeight[i] = (int) convexHull.get(i).getJ();
					break;
				case "xz":
					tableWidth[i] = (int) convexHull.get(i).getI();
					tableHeight[i] = (int) convexHull.get(i).getK();
					break;
				case "yz":
					tableWidth[i] = (int) convexHull.get(i).getJ();
					tableHeight[i] = (int) convexHull.get(i).getK();
					break;
				default:
					wrongAxesName(axesName);
			}
		}
		
		switch (axesName) {
			case "xy":
				tableWidth[convexHull.size()] = (int) convexHull.get(0).getI();
				tableHeight[convexHull.size()] = (int) convexHull.get(0).getJ();
				break;
			case "xz":
				tableWidth[convexHull.size()] = (int) convexHull.get(0).getI();
				tableHeight[convexHull.size()] = (int) convexHull.get(0).getK();
				break;
			case "yz":
				tableWidth[convexHull.size()] = (int) convexHull.get(0).getJ();
				tableHeight[convexHull.size()] = (int) convexHull.get(0).getK();
				break;
			default:
				wrongAxesName(axesName);
		}
		
		ip.setValue(255);
		ip.fill(new PolygonRoi(tableWidth, tableHeight, tableWidth.length, Roi.POLYGON));
		return ip;
	}
	
	
	/**
	 * Label all the connected components (set of voxels) found on the slice
	 *
	 * @param imagePlusInput stack
	 * @param width          width of the slice
	 * @param height         height of the slice
	 * @param index          index of the slice
	 *
	 * @return list of label
	 */
	double[][] giveTable(ImagePlus imagePlusInput, int width, int height, int index) {
		ImageStack imageStackInput = imagePlusInput.getStack();
		double[][] image           = new double[width][height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if ("xy".equals(axesName)) {
					image[i][j] = imageStackInput.getVoxel(i, j, index);
				} else if ("xz".equals(axesName)) {
					image[i][j] = imageStackInput.getVoxel(i, index, j);
				} else {
					image[i][j] = imageStackInput.getVoxel(index, i, j);
				}
			}
		}
		ConnectedComponents connectedComponents = new ConnectedComponents();
		connectedComponents.setImageTable(image);
		// One label per connected components
		listLabel = connectedComponents.getListLabel(255);
		// Return the image where all connected components have a different label (value)
		return connectedComponents.getImageTable();
	}
	
	
	/**
	 * Return current combined axis analysing
	 *
	 * @return current combined axis analysing
	 */
	public String getAxes() {
		return axesName;
	}
	
	
	/**
	 * Set the current combined axis analysing
	 *
	 * @param axes Current combined axis analysing
	 */
	public void setAxes(String axes) {
		if (!Pattern.compile("^[xyz]{2,3}$").matcher(axes).matches() ||
		    axes.chars().distinct().count() != axes.length()) {
			wrongAxesName(axes);
		}
		axesName = axes;
	}
	
}