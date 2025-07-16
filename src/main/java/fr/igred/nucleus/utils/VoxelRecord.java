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

/**
 * Class to create a voxel with its coordinates in the three dimensions and its value
 *
 * @author Philippe Andrey, Tristan and Axel Poulet
 */
public class VoxelRecord {
	/** Voxel coordinates */
	private double i;
	private double j;
	private double k;
	
	/** Voxel value */
	private double value;
	
	
	/**
	 * Constructor
	 *
	 * @param i Coordinates x of voxel
	 * @param j Coordinates y of voxel
	 * @param k Coordinates z of voxel
	 */
	public void setLocation(double i, double j, double k) {
		this.i = i;
		this.j = j;
		this.k = k;
	}
	
	
	public void multiplie(double a, double b, double c) {
		setLocation(i * a, j * b, k * c);
	}
	
	
	/**
	 * Returns the x coordinates of a voxel
	 *
	 * @return
	 */
	public double getI() {
		return i;
	}
	
	
	/**
	 * Returns the y coordinates of a voxel
	 *
	 * @return
	 */
	public double getJ() {
		return j;
	}
	
	
	/**
	 * Returns the z coordinates of a voxel
	 *
	 * @return
	 */
	public double getK() {
		return k;
	}
	
	
	/**
	 * Returns the voxel value
	 *
	 * @return
	 */
	public double getValue() {
		return value;
	}
	
	
	/**
	 * Initializes the voxel value
	 *
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	
	/**
	 * Multiplies the coordinates of voxel with a different factor for each coordinates
	 *
	 * @param a
	 * @param b
	 * @param c
	 */
	public void multiply(double a, double b, double c) {
		setLocation(i * a, j * b, k * c);
	}
	
}