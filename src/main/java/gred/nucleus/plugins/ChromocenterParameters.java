package gred.nucleus.plugins;

import fr.igred.omero.Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;


public class ChromocenterParameters extends PluginParameters {
	/** Activation of gaussian filter */
	public boolean gaussianOnRaw;
	
	/** Factor for gradient thresholding */
	public double factor = 1.5;
	
	/** Number of neighbour to explore */
	public int neighbours = 3;
	
	/** Folder containing segmented images */
	public String segInputFolder;
	
	public Client omeroClient;
	
	/** Gaussian parameters */
	public double gaussianBlurXsigma = 1;
	public double gaussianBlurYsigma = 1;
	public double gaussianBlurZsigma = 2;
	
	/** Filter connected component */
	public boolean sizeFilterConnectedComponent;
	
	/** Filter connected component */
	public boolean noChange;
	
	/** Max volume connected component filter */
	public double maxSizeConnectedComponent = 3;
	
	/** Min volume connected component filter */
	public double minSizeConnectedComponent = 0.003;
	
	
	/** ChromocenterParameters Constructor For OMERO */
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              Client omeroClient) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.omeroClient = omeroClient;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              Client omeroClient,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder, gaussianBlurXsigma, gaussianBlurYsigma, gaussianBlurZsigma, gaussian);
		this.segInputFolder = segInputFolder;
		this.omeroClient = omeroClient;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              String pathToConfigFile) {
		super(inputFolder, outputFolder, pathToConfigFile);
		addPropertiesFromFile(pathToConfigFile);
		this.segInputFolder = segInputFolder;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              boolean gaussian) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianOnRaw = gaussian;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double factor) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.factor = factor;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              int neigh) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.neighbours = neigh;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double factor,
	                              int neigh) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.factor = factor;
		this.neighbours = neigh;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double factor,
	                              int neigh,
	                              boolean gaussian) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.gaussianOnRaw = true;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent,
	                              double factor,
	                              int neigh) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
		this.factor = factor;
		this.neighbours = neigh;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              double factor,
	                              int neigh,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.factor = factor;
		this.neighbours = neigh;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              boolean noChange,
	                              double factor,
	                              int neigh) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.factor = factor;
		this.neighbours = neigh;
		this.noChange = noChange;
		
	}
	
	
	public ChromocenterParameters(String inputFolder,
	                              String segInputFolder,
	                              String outputFolder,
	                              double gaussianBlurXsigma,
	                              double gaussianBlurYsigma,
	                              double gaussianBlurZsigma,
	                              boolean gaussian,
	                              boolean sizeFilterConnectedComponent,
	                              double maxSizeConnectedComponent,
	                              double minSizeConnectedComponent) {
		super(inputFolder, outputFolder);
		this.segInputFolder = segInputFolder;
		this.gaussianBlurXsigma = gaussianBlurXsigma;
		this.gaussianBlurYsigma = gaussianBlurYsigma;
		this.gaussianBlurZsigma = gaussianBlurZsigma;
		this.gaussianOnRaw = gaussian;
		this.sizeFilterConnectedComponent = sizeFilterConnectedComponent;
		this.maxSizeConnectedComponent = maxSizeConnectedComponent;
		this.minSizeConnectedComponent = minSizeConnectedComponent;
	}
	
	
	public void addProperties(String pathToConfigFile) {
		addPropertiesFromFile(pathToConfigFile);
	}
	
	
	private void addPropertiesFromFile(String pathToConfigFile) {
		Properties prop = new Properties();
		try (FileInputStream fis = new FileInputStream(pathToConfigFile)) {
			prop.load(fis);
		} catch (IOException ex) {
			System.err.println(pathToConfigFile + " : can't load the config file !");
			System.exit(-1);
		}
		for (String idProp : prop.stringPropertyNames()) {
			if ("_neigh".equals(idProp)) {
				this.neighbours = parseInt(prop.getProperty("_neigh"));
			} else if ("_factor".equals(idProp)) {
				this.factor = parseInt(prop.getProperty("_factor"));
			} else if ("_gaussianOnRaw".equals(idProp)) {
				this.gaussianOnRaw = parseBoolean(prop.getProperty("_gaussianOnRaw"));
			} else if ("_gaussianBlurXsigma;".equals(idProp)) {
				this.gaussianBlurXsigma = parseDouble(prop.getProperty("_gaussianBlurXsigma"));
			} else if ("_gaussianBlurYsigma;".equals(idProp)) {
				this.gaussianBlurYsigma = parseDouble(prop.getProperty("_gaussianBlurYsigma"));
			} else if ("_gaussianBlurZsigma;".equals(idProp)) {
				this.gaussianBlurZsigma = parseDouble(prop.getProperty("_gaussianBlurZsigma"));
			} else if ("_sizeFilterConnectedComponent".equals(idProp)) {
				this.sizeFilterConnectedComponent = parseBoolean(prop.getProperty("_sizeFilterConnectedComponent"));
			} else if ("_maxSizeConnectedComponent;".equals(idProp)) {
				this.maxSizeConnectedComponent = parseDouble(prop.getProperty("_maxSizeConnectedComponent"));
			} else if ("_minSizeConnectedComponent;".equals(idProp)) {
				this.minSizeConnectedComponent = parseDouble(prop.getProperty("_minSizeConnectedComponent"));
			}
			
		}
	}
	
}