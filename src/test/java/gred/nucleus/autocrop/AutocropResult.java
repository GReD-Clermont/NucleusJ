package gred.nucleus.autocrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AutocropResult {
	private int              cropNb      = 0;
	private List<CropResult> coordinates = new ArrayList<>(0);
	
	
	public int getCropNb() {
		return cropNb;
	}
	
	
	public void setCropNb(int cropNb) {
		this.cropNb = cropNb;
	}
	
	
	public List<CropResult> getCoordinates() {
		return Collections.unmodifiableList(coordinates);
	}
	
	
	public void setCoordinates(List<? extends CropResult> coordinates) {
		this.coordinates = new ArrayList<>(coordinates);
	}
	
}
