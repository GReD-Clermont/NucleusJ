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

import java.util.ArrayList;
import java.util.List;


public class ConnectedComponents {
	private final List<Double> listLabel = new ArrayList<>();
	private       double[][]   image;
	
	
	/**
	 * Iterates over the image pixels and look for these connected components
	 *
	 * @param labelIni
	 */
	void computeLabel(double labelIni) {
		int currentLabel = 2;
		// Iterate over the image pixels
		for (int i = 0; i < image.length; ++i) {
			for (int j = 0; j < image[i].length; ++j) {
				if (image[i][j] == labelIni) {
					image[i][j] = currentLabel;
					VoxelRecord voxelRecord = new VoxelRecord();
					voxelRecord.setLocation(i, j, 0);
					breadthFirstSearch(labelIni, voxelRecord, currentLabel);
					listLabel.add((double) currentLabel);
					currentLabel++;
				}
			}
		}
	}
	
	
	/**
	 * @param labelIni
	 * @param voxelRecord
	 * @param currentLabel
	 */
	private void breadthFirstSearch(double labelIni, VoxelRecord voxelRecord, int currentLabel) {
		List<VoxelRecord> voxelBoundary = detectVoxelBoundary(labelIni);
		voxelBoundary.add(0, voxelRecord);
		image[(int) voxelRecord.getI()][(int) voxelRecord.getJ()] = currentLabel;
		while (!voxelBoundary.isEmpty()) {
			VoxelRecord voxelRemove = voxelBoundary.remove(0);
			for (int ii = (int) voxelRemove.getI() - 1; ii <= (int) voxelRemove.getI() + 1; ii++) {
				for (int jj = (int) voxelRemove.getJ() - 1; jj <= (int) voxelRemove.getJ() + 1; jj++) {
					if (ii >= 0 && ii <= image.length - 1 && jj >= 0 && jj <= image[0].length - 1) {
						if (ii > 0 && ii < image.length - 1 && jj > 0 && jj < image[0].length - 1) {
							if (image[ii][jj] == labelIni &&
							    (image[ii - 1][jj] == currentLabel ||
							     image[ii + 1][jj] == currentLabel ||
							     image[ii][jj - 1] == currentLabel ||
							     image[ii][jj + 1] == currentLabel)) {
								image[ii][jj] = currentLabel;
								VoxelRecord voxel = new VoxelRecord();
								voxel.setLocation(ii, jj, 0);
								voxelBoundary.add(0, voxel);
							}
						} else if (ii == 0) {
							if (jj == 0) {
								if (image[ii][jj] == labelIni &&
								    (image[ii + 1][jj] == currentLabel || image[ii][jj + 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							} else if (jj == image[0].length - 1) {
								if (image[ii][jj] == labelIni &&
								    (image[ii + 1][jj] == currentLabel || image[ii][jj - 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							} else {
								if (image[ii][jj] == labelIni &&
								    (image[ii + 1][jj] == currentLabel ||
								     image[ii][jj - 1] == currentLabel ||
								     image[ii][jj + 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							}
						} else if (ii == image.length - 1) {
							if (jj == 0) {
								if (image[ii][jj] == labelIni &&
								    (image[ii - 1][jj] == currentLabel || image[ii][jj + 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							} else if (jj == image[0].length - 1) {
								if (image[ii][jj] == labelIni &&
								    (image[ii - 1][jj] == currentLabel || image[ii][jj - 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							} else {
								if (image[ii][jj] == labelIni &&
								    (image[ii - 1][jj] == currentLabel ||
								     image[ii][jj - 1] == currentLabel ||
								     image[ii][jj + 1] == currentLabel)) {
									image[ii][jj] = currentLabel;
									VoxelRecord voxel = new VoxelRecord();
									voxel.setLocation(ii, jj, 0);
									voxelBoundary.add(0, voxel);
								}
							}
						} else if (jj == 0) {
							if (image[ii][jj] == labelIni &&
							    (image[ii - 1][jj] == currentLabel ||
							     image[ii + 1][jj] == currentLabel ||
							     image[ii][jj + 1] == currentLabel)) {
								image[ii][jj] = currentLabel;
								VoxelRecord voxel = new VoxelRecord();
								voxel.setLocation(ii, jj, 0);
								voxelBoundary.add(0, voxel);
							}
						} else if (jj == image[0].length - 1 &&
						           image[ii][jj] == labelIni &&
						           (image[ii - 1][jj] == currentLabel ||
						            image[ii + 1][jj] == currentLabel ||
						            image[ii][jj - 1] == currentLabel)) {
							image[ii][jj] = currentLabel;
							VoxelRecord voxel = new VoxelRecord();
							voxel.setLocation(ii, jj, 0);
							voxelBoundary.add(0, voxel);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * @param label
	 *
	 * @return
	 */
	private List<VoxelRecord> detectVoxelBoundary(double label) {
		List<VoxelRecord> lVoxelBoundary = new ArrayList<>();
		for (int i = 0; i < image.length; ++i) {
			for (int j = 0; j < image[i].length; ++j) {
				if (image[i][j] == label) {
					if (i > 0 && i < image.length - 1 && j > 0 && j < image[i].length - 1) {
						if (image[i - 1][j] == 0 ||
						    image[i + 1][j] == 0 ||
						    image[i][j - 1] == 0 ||
						    image[i][j + 1] == 0) {
							VoxelRecord voxelTest = new VoxelRecord();
							voxelTest.setLocation(i, j, 0);
							lVoxelBoundary.add(voxelTest);
						}
					} else if (i == 0) {
						if (j == 0) {
							if (image[i + 1][j] == 0 || image[i][j + 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						} else if (j == image[0].length - 1) {
							if (image[i + 1][j] == 0 || image[i][j - 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						} else {
							if (image[i + 1][j] == 0 || image[i][j - 1] == 0 || image[i][j + 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						}
					} else if (i == image.length - 1) {
						if (j == 0) {
							if (image[i - 1][j] == 0 || image[i][j + 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						} else if (j == image[0].length - 1) {
							if (image[i - 1][j] == 0 || image[i][j - 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						} else {
							if (image[i - 1][j] == 0 || image[i][j - 1] == 0 || image[i][j + 1] == 0) {
								VoxelRecord voxelTest = new VoxelRecord();
								voxelTest.setLocation(i, j, 0);
								lVoxelBoundary.add(voxelTest);
							}
						}
					} else if (j == 0) {
						if (image[i - 1][j] == 0 || image[i + 1][j] == 0 || image[i][j + 1] == 0) {
							VoxelRecord voxelTest = new VoxelRecord();
							voxelTest.setLocation(i, j, 0);
							lVoxelBoundary.add(voxelTest);
						}
					} else if (j == image[0].length - 1 &&
					           (image[i - 1][j] == 0 || image[i + 1][j] == 0 || image[i][j - 1] == 0)) {
						VoxelRecord voxelTest = new VoxelRecord();
						voxelTest.setLocation(i, j, 0);
						lVoxelBoundary.add(voxelTest);
					}
				}
			}
		}
		return lVoxelBoundary;
	}
	
	
	/**
	 * @param labelIni
	 *
	 * @return
	 */
	public List<Double> getListLabel(double labelIni) {
		computeLabel(labelIni);
		return new ArrayList<>(listLabel);
	}
	
	
	/** @return  */
	public double[][] getImageTable() {
		return image;
	}
	
	
	/** @param image  */
	public void setImageTable(double[][] image) {
		this.image = image;
	}
	
}