package gred.nucleus.utilsNj2;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import gred.nucleus.utils.Histogram;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import gred.nucleus.plugins.ChromocenterParameters;
import gred.nucleus.utils.Chromocenter;
import gred.nucleus.utils.Parameters2D;
import gred.nucleus.core.RadialDistance;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import fr.igred.omero.Client;
/**
 * Several method to realise and create the outfile for the nuclear Analysis
 * this class contains the chromocenter parameters
 *
 * @author Tristan Dubos and Axel Poulet
 *
 */
public class NucleusChromocentersAnalysis {
	/**
	 *
	 */
	Client client;
	public NucleusChromocentersAnalysis(){
	}
	
	//TODO INTEGRATION CLASS NEW MEASURE 3D
	
	
	
	/**
	 *
	 * Analysis for several nuclei, the results are stock on output file
	 *
	 * @param rhfChoice
	 * @param imagePlusInput
	 * @param imagePlusSegmented
	 * @param imagePlusChromocenter
	 * @throws IOException
	 */
	public File[] compute3DParameters ( String rhfChoice,
	                                    ImagePlus imagePlusInput, ImagePlus imagePlusSegmented,
	                                    ImagePlus imagePlusChromocenter,
	                                    ChromocenterParameters chromocenterParameters) throws IOException {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		Calibration calibration = imagePlusInput.getCalibration();
		double voxelVolume = calibration.pixelDepth*calibration.pixelHeight*calibration.pixelWidth;
		
		imagePlusSegmented.setCalibration(calibration);
		Measure3D measure3D = new Measure3D(imagePlusSegmented,imagePlusInput,imagePlusInput.getCalibration().pixelWidth,imagePlusInput.getCalibration().pixelHeight,imagePlusInput.getCalibration().pixelDepth);
		File fileResults = new File(chromocenterParameters.outputFolder+"NucAndCcParameters3D.tab");
		File fileResultsCC = new File(chromocenterParameters.outputFolder+"CcParameters3D.tab");
		boolean exist = fileResults.exists();
		

		String text = "";
		String textCC = "";
		if (exist == false) {
			text = chromocenterParameters.getAnalysisParametersNodej();
			text += getResultsColumnNames();
			textCC = chromocenterParameters.getAnalysisParametersNodej();
			textCC += getResultsColumnNamesCC();
			
		}
		
		text += measure3D.nucleusParameter3D()+"," +
		        measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter)+",";
		
		if (histogram.getNbLabels() > 0) {
			double [] tVolumesObjects =  measure3D.computeVolumeofAllObjects(imagePlusChromocenter);
			double volumeCcMean = computeMeanOfTable(tVolumesObjects);
			int nbCc = measure3D.getNumberOfObject(imagePlusChromocenter);
			RadialDistance radialDistance = new RadialDistance ();
			double [] tBorderToBorderDistance = radialDistance.computeBorderToBorderDistances(imagePlusSegmented,imagePlusChromocenter);
			double [] tBarycenterToBorderDistance = radialDistance.computeBarycenterToBorderDistances (imagePlusSegmented,imagePlusChromocenter);
			double [] tIntensity = measure3D.computeIntensityofAllObjects(imagePlusChromocenter);
			double [] tBarycenterToBorderDistanceTableNucleus = radialDistance.computeBarycenterToBorderDistances (imagePlusSegmented,imagePlusSegmented);
			text += nbCc+","
			        +volumeCcMean+","
			        +volumeCcMean*nbCc+","
			        +computeMeanOfTable(tIntensity)+","
			        +computeMeanOfTable(tBorderToBorderDistance)+","
			        +computeMeanOfTable(tBarycenterToBorderDistance)+",";
			
			
			for (int i = 0; i < tBorderToBorderDistance.length;++i ) {
				textCC += imagePlusInput.getTitle()+"_"+i+","
				          +tVolumesObjects[i]+","
				          +tIntensity[i]+","
				          +tBarycenterToBorderDistance[i]+","
				          +tBorderToBorderDistance[i]+","
				          +tBarycenterToBorderDistanceTableNucleus[0]+",";
			}
		}
		else
			text += "0\t0\t0\tNaN\tNaN\t";
		
		text += voxelVolume+"\n";
		
		BufferedWriter bufferedWriterOutput = new BufferedWriter(new FileWriter(fileResults, true));
		
		bufferedWriterOutput.write(text);
		bufferedWriterOutput.flush();
		bufferedWriterOutput.close();
		
		BufferedWriter bufferedWriterOutputCC = new BufferedWriter(new FileWriter(fileResultsCC, true));
		bufferedWriterOutputCC.write(textCC);
		bufferedWriterOutputCC.flush();
		bufferedWriterOutputCC.close();
		return new File[] {fileResults, fileResultsCC};
		
	}

	public File[] compute3DParametersOmero(String rhfChoice,
										   ImageWrapper imageInput, ImageWrapper imageSegmented,
										   ImagePlus imagePlusChromocenter,
										   ChromocenterParameters chromocenterParameters, String datasetName,Client client) throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {

		this.client = client;
		long imageId = imageInput.getId();  // Get the image ID
		String imageName = imageInput.getName();


		// Image to ImagePlus conversion
		ImagePlus[] RawImage = new ImagePlus[]{imageInput.toImagePlus(client)};
		ImagePlus[] SegImage = new ImagePlus[]{imageSegmented.toImagePlus(client)};
		ImagePlus imagePlusInput = RawImage[0];
		ImagePlus imagePlusSegmented = SegImage[0];

		Histogram histogram = new Histogram();
		histogram.run(imagePlusChromocenter);
		Calibration calibration = imagePlusInput.getCalibration();
		double voxelVolume = calibration.pixelDepth * calibration.pixelHeight * calibration.pixelWidth;

		imagePlusSegmented.setCalibration(calibration);
		Measure3D measure3D = new Measure3D(imagePlusSegmented, imagePlusInput, imagePlusInput.getCalibration().pixelWidth, imagePlusInput.getCalibration().pixelHeight, imagePlusInput.getCalibration().pixelDepth);
		File fileResults = new File(chromocenterParameters.outputFolder + "NucAndCcParameters3D.csv");
		File fileResultsParade = new File(chromocenterParameters.outputFolder + "NucAndCcParameters3D_Parade.csv");
		File fileResultsCC = new File(chromocenterParameters.outputFolder + "CcParameters3D.csv");
		File fileResultsCCParade = new File(chromocenterParameters.outputFolder + "CcParameters3D_Parade.csv");
		boolean exist = fileResults.exists();

		String text = "";
		String textCC = "";
		String textParade = "";
		String textCCParade = "";

		// Add header if file does not exist
		if (!exist) {
			text =  chromocenterParameters.getAnalysisParametersNodej();  // Add image as the first column
			text += getResultsColumnNames();  // Existing column names
			textCC =  chromocenterParameters.getAnalysisParametersNodej();
			textCC += getResultsColumnNamesCC();

			textParade = "image,Dataset," +getResultsColumnNames();  // Add image to Parade version too
			textCCParade = "image,Dataset," + getResultsColumnNamesCC();
		}

		// Append the imageId at the beginning of the result string
		text +=  measure3D.nucleusParameter3D() + "," +
				measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter) + ",";
		textParade += imageId + "," + datasetName + ","+ measure3D.nucleusParameter3D() + "," +
				measure3D.computeVolumeRHF(imagePlusSegmented, imagePlusChromocenter) + ",";

		if (histogram.getNbLabels() > 0) {
			double[] tVolumesObjects = measure3D.computeVolumeofAllObjects(imagePlusChromocenter);
			double volumeCcMean = computeMeanOfTable(tVolumesObjects);
			int nbCc = measure3D.getNumberOfObject(imagePlusChromocenter);
			RadialDistance radialDistance = new RadialDistance();
			double[] tBorderToBorderDistance = radialDistance.computeBorderToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
			double[] tBarycenterToBorderDistance = radialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusChromocenter);
			double[] tIntensity = measure3D.computeIntensityofAllObjects(imagePlusChromocenter);
			double[] tBarycenterToBorderDistanceTableNucleus = radialDistance.computeBarycenterToBorderDistances(imagePlusSegmented, imagePlusSegmented);

			text += nbCc + "," + volumeCcMean + "," + volumeCcMean * nbCc + "," +
					computeMeanOfTable(tIntensity) + "," +
					computeMeanOfTable(tBorderToBorderDistance) + "," +
					computeMeanOfTable(tBarycenterToBorderDistance) + ",";

			textParade += nbCc + "," + volumeCcMean + "," + volumeCcMean * nbCc + "," +
					computeMeanOfTable(tIntensity) + "," +
					computeMeanOfTable(tBorderToBorderDistance) + "," +
					computeMeanOfTable(tBarycenterToBorderDistance) + ",";

			for (int i = 0; i < tBorderToBorderDistance.length; ++i) {
				textCC += imagePlusInput.getTitle() + "_" + i + "," +
						tVolumesObjects[i] + "," +
						tIntensity[i] + "," +
						tBarycenterToBorderDistance[i] + "," +
						tBorderToBorderDistance[i] + "," +
						tBarycenterToBorderDistanceTableNucleus[0] + "\n";

				textCCParade += imageId + "," + datasetName + ","+  imagePlusInput.getTitle() + "_" + i + "," +
						tVolumesObjects[i] + "," +
						tIntensity[i] + "," +
						tBarycenterToBorderDistance[i] + "," +
						tBorderToBorderDistance[i] + "," +
						tBarycenterToBorderDistanceTableNucleus[0] + "\n";
			}
		} else {
			text += "0\t0\t0\tNaN\tNaN\t\n";  // Default values if no labels
		}

		text += voxelVolume + "\n";  // Append voxelVolume at the end of the row
		textParade += voxelVolume + "\n";  // Same for Parade

		BufferedWriter bufferedWriterOutput = new BufferedWriter(new FileWriter(fileResults, true));
		bufferedWriterOutput.write(text);
		bufferedWriterOutput.flush();
		bufferedWriterOutput.close();

		BufferedWriter bufferedWriterOutputParade = new BufferedWriter(new FileWriter(fileResultsParade, true));
		bufferedWriterOutputParade.write(textParade);
		bufferedWriterOutputParade.flush();
		bufferedWriterOutputParade.close();

		BufferedWriter bufferedWriterOutputCC = new BufferedWriter(new FileWriter(fileResultsCC, true));
		bufferedWriterOutputCC.write(textCC);
		bufferedWriterOutputCC.flush();
		bufferedWriterOutputCC.close();

		BufferedWriter bufferedWriterOutputCCParade = new BufferedWriter(new FileWriter(fileResultsCCParade, true));
		bufferedWriterOutputCCParade.write(textCCParade);
		bufferedWriterOutputCCParade.flush();
		bufferedWriterOutputCCParade.close();

		return new File[]{fileResults, fileResultsCC, fileResultsParade, fileResultsCCParade};
	}

	/**
	 * Method wich compute the mean of the value in the table
	 *
	 * @param tInput Table of value
	 * @return Mean of the table
	 */
	public double computeMeanOfTable (double [] tInput) {
		double mean = 0;
		for (int i = 0; i < tInput.length; ++i)
			mean += tInput[i];
		mean = mean / (tInput.length);
		return mean;
	}
	
	
	/**
	 *
	 * @param img
	 * @return
	 */
	private ArrayList<Float> getLabels(ImagePlus img){
		ArrayList<Float> label = new ArrayList<Float>();
		ImageProcessor ip = img.getProcessor();
		
		for(int i = 0; i < img.getWidth(); ++i){
			for(int j = 0; j < img.getHeight();++j){
				float value = ip.getPixelValue(i,j);
				if(value > 0) {
					if (!label.contains(value)) {
						label.add(value);
					}
				}
			}
		}
		
		
		return label;
	}
	
	/**
	 *
	 * @param imgCc
	 * @param raw
	 * @param labels
	 * @return
	 */
	private Chromocenter getCcParam(ImagePlus imgCc, ImagePlus raw, ArrayList<Float> labels,String name, double avgNuc ){
		Chromocenter cc =  new Chromocenter(0,0,0,
		                                    0,0, 0, 0, "plopi");
		for(int j = 0; j < labels.size(); ++j) {
			//System.out.println(labels.get(j));
			Parameters2D param = new Parameters2D(imgCc,raw,labels.get(j));
			param.computePrameters();
			
			Chromocenter tmp = new Chromocenter(param.getCirculairty(), param.getNbPixelObject(), param.getAspectRatio(), param.getPerim(), param.getArea(),
			                                    param.getSolidity(), param.getRound(), name+"_"+j);
			
			tmp.setCCValue(param.getAvgIntensity(),avgNuc);
			cc.addChromocenter(param.getCirculairty(), param.getNbPixelObject(),
			                   param.getAspectRatio(), param.getPerim(), param.getArea(),
			                   param.getSolidity(), param.getRound(), tmp.getCCValue());
			
		}
		cc.avgChromocenters(labels.size()+1);
		return cc;
		
	}
	public String getResultsColumnNames() {
		return "ImageName," +
		       "Volume," +
		       "Flatnes," +
		       "Elongation," +
		       "Esr," +
		       //"SurfaceArea\t" +
		       //"Sphericity\t" +
		       "MeanIntensityNucleus," +
		       "MeanIntensityBackground," +
		       "StandardDeviation," +
		       "MinIntensity," +
		       "MaxIntensity," +
		       "MedianIntensityImage," +
		       "MedianIntensityNucleus," +
		       "MedianIntensityBackground," +
		       "ImageSize," +
		       "VolumeRHF," +
		       "NbCc," +
		       "VCcMean," +
		       "VCcTotal," +
		       "normIntensityMean," +
		       "DistanceBorderToBorderMean," +
		       "DistanceBarycenterToBorderMean," +
		       "VoxelVolume\n";
	}
	
	public String getResultsColumnNamesCC() {
		return "ImageName," +
		       "Volume," +
		       "NormIntensity," +
		       "BorderToBorderDistance," +
		       "BarycenterToBorderDistance," +
		       "BarycenterToBorderDistanceNucleus\n";
	}
}