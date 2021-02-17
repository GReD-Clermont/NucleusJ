/*
 * Copyright (C) 2016 by Rémy Malgouyres
 * http://malgouyres.org
 * File: ComponentRemovalLinear.java
 *
 * The program is distributed under the terms of the GNU General Public License
 *
 */

package gred.nucleus.componentRemoval;

import gred.nucleus.connectedComponent.ComponentInfo;
import gred.nucleus.utils.Voxel;

/**
 * This class is intended to implement the predicate on voxels and connected components to keep all components which are
 * within a thick plane. The thick plane's equation is: z >= m_x Coeff*x + m_yCoeff*y + m_constantCoeff > z-m_thickness
 *
 * @author Remy Malgouyres, Tristan Dubos and Axel Poulet
 */
public class ComponentRemovalLinear implements ComponentRemovalPredicate {
	
	private double m_xCoeff;
	private double m_yCoeff;
	private double m_constantCoeff;
	private double m_thickness;
	
	/**
	 * @param xCoeff        first coefficient of the plane's equation
	 * @param yCoeff        second coefficient of the plane's equation
	 * @param constantCoeff third coefficient of the plane's equation
	 * @param thickness     thickness of the plane
	 */
	public ComponentRemovalLinear(double xCoeff, double yCoeff, double constantCoeff, double thickness) {
		this.m_xCoeff = xCoeff;
		this.m_yCoeff = yCoeff;
		this.m_constantCoeff = constantCoeff;
		this.m_thickness = thickness;
	}
	
	/**
	 * @return true if the voxel is in the thick plane, false otherwise
	 *
	 * @see gred.nucleus.componentRemoval.ComponentRemovalPredicate)
	 */
	@Override
	public boolean keepVoxelComponent(Voxel voxel, ComponentInfo componentInfo) {
		double zValue = this.m_xCoeff * voxel.getX() + this.m_yCoeff * voxel.getY() + this.m_constantCoeff;
		return ((voxel.getZ() >= zValue) && (voxel.getZ() < zValue + this.m_thickness));
	}
	
}
