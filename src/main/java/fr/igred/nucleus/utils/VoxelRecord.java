package fr.igred.nucleus.utils;

/**
 * Class to create a voxel with its coordinates in the three dimensions and its value
 *
 * @author Philippe Andrey, Tristan and Axel Poulet
 */
public class VoxelRecord {
	/** Coordinates voxel */
	public double i, j, k;
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