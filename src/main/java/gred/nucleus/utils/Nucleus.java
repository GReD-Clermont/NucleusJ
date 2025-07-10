package gred.nucleus.utils;

public class Nucleus extends ObjectCharacteristics {
	
	private int          nbCc;
	private double       rhf;
	private Chromocenter chromocenter = null;
	
	
	public Nucleus(double circularity, int nbPixel, double averageIntensity,
	               double stdDevIntensity, double aspectRatio, double perimeter,
	               double area, double solidity, double round, String name) {
		super(circularity, nbPixel, averageIntensity,
		      stdDevIntensity, aspectRatio, perimeter,
		      area, solidity, round, name);
	}
	
	
	public Nucleus(double circularity, double aspectRatio, double perimeter,
	               double area, double solidity, double round, String name) {
		super(circularity, aspectRatio, perimeter, area, solidity, round, name);
	}
	
	
	public Chromocenter getChromocenter() {
		return chromocenter;
	}
	
	
	public void setChromocenter(Chromocenter chromocenter) {
		this.chromocenter = chromocenter;
	}
	
	
	public int getNbCc() {
		return nbCc;
	}
	
	
	public void setNbCc(int nbCc) {
		this.nbCc = nbCc;
	}
	
	
	public double getRHF() {
		return rhf;
	}
	
	
	public void setRHF(double a) {
		rhf = a;
	}
	
	
	public String ToString() {
		double nucIntesnity = getAverageIntensity() * getNbPixel();
		
		return getName() + "\t" +
		       getArea() + "\t" +
		       getPerimeter() + "\t" +
		       getCircularity() + "\t" +
		       getAspectRatio() + "\t" +
		       getSolidity() + "\t" +
		       getRound() + "\t" +
		       nbCc + "\t" +
		       rhf + "\t" +
		       chromocenter.getArea();
	}
	
	
}
