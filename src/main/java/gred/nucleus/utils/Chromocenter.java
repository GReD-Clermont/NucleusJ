package gred.nucleus.utils;


/**
 *
 */
public class Chromocenter extends ObjectCharacteristics {
	
	private double totalIntensity;
	private double ccValue;
	
	
	/**
	 * @param circularity
	 * @param nbPixel
	 * @param aspectRatio
	 * @param perimeter
	 * @param area
	 * @param solidity
	 * @param round
	 * @param name
	 */
	public Chromocenter(double circularity, int nbPixel, double aspectRatio, double perimeter,
	                    double area, double solidity, double round, String name) {
		super(circularity, nbPixel, 0, 0, aspectRatio, perimeter, area, solidity, round, name);
	}
	
	
	public double getTotalIntensity() {
		return totalIntensity;
	}
	
	
	public void setTotalIntensity(double a) {
		this.totalIntensity = a;
	}
	
	
	public void setCCValue(double a, double b) {
		this.ccValue = a / b;
	}
	
	
	public double getCCValue() {
		return ccValue;
	}
	
	
	private void setCCValue(double a) {
		this.ccValue = a;
	}
	
	
	public void addChromocenter(double circularity, int nbPixel, double aspectRatio,
	                            double perimeter, double area, double solidity, double round,
	                            double ccVal) {
		setCircularity(getCircularity() + circularity);
		setNbPixel(getNbPixel() + nbPixel);
		setAspectRatio(getAspectRatio() + aspectRatio);
		setPerimeter(getPerimeter() + perimeter);
		setArea(getArea() + area);
		setSolidity(getSolidity() + solidity);
		setRound(getRound() + round);
		this.ccValue += ccVal;
	}
	
	
	/**
	 * @param nbCc
	 */
	public void avgChromocenters(int nbCc) {
		if (nbCc > 0) {
			setCircularity(getCircularity() / nbCc);
			setNbPixel(getNbPixel() / nbCc);
			setAspectRatio(getAspectRatio() / nbCc);
			setPerimeter(getPerimeter() / nbCc);
			setArea(getArea() / nbCc);
			setSolidity(getSolidity() / nbCc);
			setRound(getRound() / nbCc);
			this.ccValue /= nbCc;
		} else {
			setCircularity(0);
			setNbPixel(0);
			setAspectRatio(0);
			setPerimeter(0);
			setArea(0);
			setSolidity(0);
			setRound(0);
			this.ccValue = 0;
		}
	}
	
	
	public String toString() {
		return getName() + "\t" +
		       getArea() + "\t" +
		       getPerimeter() + "\t" +
		       getCircularity() + "\t" +
		       getAspectRatio() + "\t" +
		       getSolidity() + "\t" +
		       ccValue;
	}
	
}
