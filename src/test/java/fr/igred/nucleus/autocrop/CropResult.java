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


public class CropResult {
	private final int cropNumber;
	private final Box box;
	private final int channel;
	
	
	public CropResult(int cropNumber,
	                  int channel,
	                  int xStart,
	                  int yStart,
	                  int zStart,
	                  int width,
	                  int height,
	                  int depth) {
		this.cropNumber = cropNumber;
		this.channel = channel;
		
		box = new Box((short) xStart,
		              (short) (xStart + width),
		              (short) yStart,
		              (short) (yStart + height),
		              (short) zStart,
		              (short) (zStart + depth)
		);
	}
	
	
	public Box getBox() {
		return box;
	}
	
	
	public int getCropNumber() {
		return cropNumber;
	}
	
}
