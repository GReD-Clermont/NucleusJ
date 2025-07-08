package gred.nucleus.utils;

public class Nucleus extends ObjectCharacteristics {
	
	private int          nbCc;
	private double       RHF;
	private Chromocenter chromocenter;
	
	
	public Nucleus(double circularity, int nbPixel, double averageIntesnity,
	               double stdDevIntesnity, double aspectRatio, double perimeter,
	               double area, double solidity, double round, String name) {
		super(circularity, nbPixel, averageIntesnity,
		      stdDevIntesnity, aspectRatio, perimeter,
		      area, solidity, round, name);
	}
	
	
	public Nucleus(double circularity, double aspectRatio, double perimeter,
	               double area, double solidity, double round, String name) {
		super(circularity, aspectRatio, perimeter, area, solidity, round, name);
	}
	
	
	public Chromocenter getChromocenter() {
		return this.chromocenter;
	}
	
	
	public void setChromocenter(Chromocenter chromocenter) {
		this.chromocenter = chromocenter;
	}
	
	
	public int getNbCc() {
		return this.nbCc;
	}
	
	
	public void setNbCc(int nbCc) {
		this.nbCc = nbCc;
	}
	
	
	public double getRHF() {
		return this.RHF;
	}
	
	
	public void setRHF(double a) {
		RHF = a;
	}
	
	
	public String ToString() {
		double nucIntesnity = getAverageIntesnity() * getNbPixel();
		
		return getName() + "\t" +
		       getArea() + "\t" +
		       getPerimeter() + "\t" +
		       getCircularity() + "\t" +
		       getAspectRatio() + "\t" +
		       getSolidity() + "\t" +
		       getRound() + "\t" +
		       getNbCc() + "\t" +
		       getRHF() + "\t" +
		       chromocenter.getArea();
	}
	
	
}
