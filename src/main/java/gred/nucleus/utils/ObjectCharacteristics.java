package gred.nucleus.utils;

public class ObjectCharacteristics {
	private final String name;
	
	private double circularity;
	private int    nbPixel;
	private double averageIntensity;
	private double stdDevIntensity;
	private double aspectRatio;
	private double perimeter;
	private double area;
	private double solidity;
	private double round;
	
	
	public ObjectCharacteristics(double circularity, int nbPixel,
	                             double averageIntensity, double stdDevIntensity,
	                             double aspectRatio, double perimeter, double area,
	                             double solidity, double round, String name) {
		this.circularity = circularity;
		this.nbPixel = nbPixel;
		this.averageIntensity = averageIntensity;
		this.stdDevIntensity = stdDevIntensity;
		this.aspectRatio = aspectRatio;
		this.perimeter = perimeter;
		this.area = area;
		this.solidity = solidity;
		this.round = round;
		this.name = name;
	}
	
	
	public ObjectCharacteristics(double circularity, double aspectRatio,
	                             double perimeter, double area,
	                             double solidity, double round, String name) {
		this.circularity = circularity;
		this.aspectRatio = aspectRatio;
		this.perimeter = perimeter;
		this.area = area;
		this.solidity = solidity;
		this.round = round;
		this.name = name;
	}
	
	
	public double getArea() {
		return area;
	}
	
	
	public void setArea(double area) {
		this.area = area;
	}
	
	
	public double getAspectRatio() {
		return aspectRatio;
	}
	
	
	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	
	
	public double getPerimeter() {
		return perimeter;
	}
	
	
	public void setPerimeter(double a) {
		this.perimeter = a;
	}
	
	
	public double getCircularity() {
		return circularity;
	}
	
	
	public void setCircularity(double a) {
		this.circularity = a;
	}
	
	
	public double getAverageIntensity() {
		return averageIntensity;
	}
	
	
	public void setAverageIntensity(double a) {
		this.averageIntensity = a;
	}
	
	
	public double getStdDevIntensity() {
		return stdDevIntensity;
	}
	
	
	public void setStdDevIntensity(double a) {
		this.stdDevIntensity = a;
	}
	
	
	public double getSolidity() {
		return solidity;
	}
	
	
	public void setSolidity(double a) {
		this.solidity = a;
	}
	
	
	public double getRound() {
		return round;
	}
	
	
	public void setRound(double a) {
		this.round = a;
	}
	
	
	public int getNbPixel() {
		return nbPixel;
	}
	
	
	public void setNbPixel(int a) {
		this.nbPixel = a;
	}
	
	
	public String getName() {
		return name;
	}
	
}
