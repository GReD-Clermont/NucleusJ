package gred.nucleus.core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ij.IJ;
import ij.ImagePlus;


/**
 * Several method to realise and create the outfile for the nuclear Analysis
 * 
 * @author Poulet Axel
 *
 */

public class NucleusAnalysis
{
	 @SuppressWarnings("unused")
	 private static class IOEception {  public IOEception() { } }

	 public NucleusAnalysis (){}

	 /**
	  * this method compute severals parameters of shape (sphericity, flataness and
	  * elongation) and parameters of lenght (volume and equivalent spherique radius)
	  * Take in input the path of results files output.
	  * 
	  * @param pathResultsFile path for the output file
	  * @param imagePlusSegmented image of the segmented nucleus
	  * @throws IOException
	  */
	  public void nucleusParameter3D (String pathResultsFile, ImagePlus imagePlusRaw ,ImagePlus imagePlusSegmented) throws IOException
	  {
		  Measure3D measure3D = new Measure3D();
		  File fileResults = new File(pathResultsFile);
		  boolean exist = fileResults.exists();
		  BufferedWriter bufferedWriterOutput;
		  double volume = measure3D.computeVolumeObject(imagePlusSegmented,255);
		  double surfaceArea = measure3D.computeSurfaceObject(imagePlusSegmented,255);

		  if (exist)
		  {
			  FileWriter fileWriter = new FileWriter(fileResults, true);
		      bufferedWriterOutput = new BufferedWriter(fileWriter);
		      bufferedWriterOutput.write
		      (
		    		  imagePlusSegmented.getTitle()+"\t"
		    		  +measure3D.computeVolumeObject(imagePlusSegmented,255)+"\t"
		    		  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[0]+"\t"
		    		  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[1]+"\t"
		    		  +measure3D.computeSphericity(volume, surfaceArea)+"\t"
		    		  +measure3D.equivalentSphericalRadius(volume)+"\t"
		    		  +surfaceArea+"\n"
		     );
		  }
		  else
		  {
			  FileWriter fileWriter = new FileWriter(fileResults, true);
		      bufferedWriterOutput = new BufferedWriter(fileWriter);
		      bufferedWriterOutput.write
		      (
		    		  "NucleusFileName\tVolume\tFlatness\tElongation\tSphericity\tEsr\tSurfaceArea\t\n"+
		    		  imagePlusSegmented.getTitle()+"\t"
		    		  +measure3D.computeVolumeObject(imagePlusSegmented,255)+"\t"
		    		  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[0]+"\t"
		    		  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[1]+"\t"
		    		  +measure3D.computeSphericity(volume, surfaceArea)+"\t"
		    		  +measure3D.equivalentSphericalRadius(volume)+"\t"
		    		  +surfaceArea+"\n"
		      );
		  } 
		  bufferedWriterOutput.flush();
		  bufferedWriterOutput.close();   
	  }
	  
	  /**
	   * this method compute severals parameters of shape (sphericity, flataness and
	   * elongation) and parameters of lenght (volume and equivalent spherique radius) for one nucleus
	   * the results are written on the IJ log windows
	   *  
	   * @param imagePlusSegmented image segmented
	   */
	  public void nucleusParameter3D (ImagePlus imagePlusRaw, ImagePlus imagePlusSegmented)
	  {
		  Measure3D measure3D = new Measure3D();
		  double volume = measure3D.computeVolumeObject(imagePlusSegmented,255);
		  double surfaceArea = measure3D.computeSurfaceObject(imagePlusSegmented,255);
		  double bis = measure3D.computeComplexSurface(imagePlusRaw, imagePlusSegmented);
		  IJ.log("3D parameters");
		  IJ.log("NucleusFileName Volume Flatness Elongation Sphericity Esr SurfaceArea");
		  IJ.log
		  (
				  imagePlusSegmented.getTitle()+" "
				  +measure3D.computeVolumeObject(imagePlusSegmented,255)+" "
				  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[0]+" "
	    		  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[1]+" "
				  +measure3D.computeSphericity(volume, surfaceArea)+" "
				  +measure3D.equivalentSphericalRadius(volume)+" la le bissss :"
				  +surfaceArea+" "+bis+ " "+"\n" +" re tadaaaaaaaaa    "+  bis +"\n\n\n\n\n"
		  );
		  IJ.log
			 (
			  "NucleusFileName "+imagePlusSegmented.getTitle()+"\n"
			  +"Volume "  +measure3D.computeVolumeObject(imagePlusSegmented,255)+"\n"
			  +"Flatness "	  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[0]+"\n"
			  +"Elongation "	  +measure3D.computeFlatnessAndElongation(imagePlusSegmented,255)[1]+"\n"
			  +"Sphericity "	  +measure3D.computeSphericity(volume, surfaceArea)+"\n"
			  +"Esr "	  +measure3D.equivalentSphericalRadius(volume) +"\n"
			  +"SurfaceArea "+surfaceArea+"\n"
			  +"SurfaceArea bis "+bis+ " "+"\n"
			  +" Sphericity with bis  "+  measure3D.computeSphericity(volume, bis) +"\n"
			  );

	  }

	  /**
	   * 
	   * this method compute severals 2D parameters of 2D shape for several nuclei.
	   * 
	   * @param pathResultsFile  path for the output file
	   * @param imagePlusInput image of the segmented nucleus
	   * @throws IOException
	   */
	  public void nucleusParameter2D (String pathResultsFile,ImagePlus imagePlusInput) throws IOException
	  {
		  Measure2D measure2D = new Measure2D();
		  measure2D.run(imagePlusInput);
		  File fileResu = new File(pathResultsFile);
		  boolean exist = fileResu.exists();
		  BufferedWriter bufferedWriterOutput;
		  if (exist)
		  {
			  FileWriter fileWriter = new FileWriter(fileResu, true);
		      bufferedWriterOutput = new BufferedWriter(fileWriter);
		      bufferedWriterOutput.write
		      (
		    		  imagePlusInput.getTitle()+"\t"
		    		  +measure2D.getAspectRatio()+"\t"
		    		  +measure2D.getCirculairty()+"\n"
		      );
		  }
		  else
		  {
			  FileWriter fileWriter = new FileWriter(fileResu, true);
		      bufferedWriterOutput = new BufferedWriter(fileWriter);
		      bufferedWriterOutput.write
		      (
		    		  "ImageTitle\tAspectRatio\tCircularity\n"+
		    		  imagePlusInput.getTitle()+"\t"
		    		  +measure2D.getAspectRatio()+"\t"
		    		  +measure2D.getCirculairty()+"\n"
		     );
		  } 
		  bufferedWriterOutput.flush();
		  bufferedWriterOutput.close();   
	  }
	  
	  /**
	   * 
	   * @param imagePlusInput
	   */
	  public void nucleusParameter2D (ImagePlus imagePlusInput)
	  {
		  Measure2D measure2D = new Measure2D();
		  measure2D.run(imagePlusInput);
		  IJ.log("2D parameters");
		  IJ.log("ImageTitle AspectRatio Circularity");
		  IJ.log
		  (
				  imagePlusInput.getTitle()+" "
				  +measure2D.getAspectRatio()+" "
				  +measure2D.getCirculairty()
		   );
	  }
}