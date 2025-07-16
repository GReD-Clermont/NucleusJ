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
package fr.igred.nucleus.autocrop;

import java.util.ArrayList;
import java.util.List;


public class AutocropResult {
	private int              cropNb;
	private List<CropResult> coordinates = new ArrayList<>(0);
	
	
	public int getCropNb() {
		return cropNb;
	}
	
	
	public void setCropNb(int cropNb) {
		this.cropNb = cropNb;
	}
	
	
	public List<CropResult> getCoordinates() {
		return new ArrayList<>(coordinates);
	}
	
	
	public void setCoordinates(List<? extends CropResult> coordinates) {
		this.coordinates = new ArrayList<>(coordinates);
	}
	
}
