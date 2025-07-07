package gred.nucleus.process;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import gred.nucleus.files.Directory;
import gred.nucleus.files.FilesNames;
import ij.IJ;
import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.plugins.BF;
import gred.nucleus.gui.Progress;
import gred.nucleus.plugins.ChromocenterParameters;
import gred.nucleus.utilsNj2.NucleusChromocentersAnalysis;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ChromocenterCalling {
	ChromocenterParameters chromocenterParameters ;
	
	
	private String _prefix;
	private boolean _is2DImg =false;
	private boolean _isGui=false;
	private Progress _p;
	private File[] tab;
	private ProjectWrapper project;
	private DatasetWrapper outDataset;
	private DatasetWrapper outDatasetGradient;
	private String segImg;
	private String gradImg;
	private String dataset_name;
	
	/**
	 *
	 * @param chromocenterParameters
	 */
	public ChromocenterCalling(ChromocenterParameters chromocenterParameters ){
		this.chromocenterParameters=chromocenterParameters;
	}
	
	
	public ChromocenterCalling(ChromocenterParameters chromocenterParameters, boolean gui ){
		this.chromocenterParameters=chromocenterParameters;
		_isGui = gui;
	}
	
	
	/**
	 * Run the Chromocenter segmentation on several images
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public void runSeveralImages2() throws IOException, FormatException {
		DebugTools.enableLogging("OFF");
		Directory directoryInput = new Directory(this.chromocenterParameters.getInputFolder());
		directoryInput.listImageFiles(this.chromocenterParameters.getInputFolder());
		directoryInput.checkIfEmpty();
		String rhfChoice = "Volume";
		String diffDir = this.chromocenterParameters.outputFolder+"gradientImage";
		File file = new File (diffDir);
		if(!file.exists()) file.mkdir();
		
		String segCcDir = this.chromocenterParameters.outputFolder+"SegCC";
		file = new File (segCcDir);
		if(!file.exists()) file.mkdir();
		// TODO A REFAIRE C EST MOCHE !!!!
		System.out.println("size: "+ directoryInput.getNumberFiles());
		
		if (this._isGui){
			_p = new Progress("Images Analysis: ",directoryInput.getNumberFiles());
			_p._bar.setValue(0);
		}
		for (short i = 0; i < directoryInput.getNumberFiles(); ++i) {
			File currentFile = directoryInput.getFile(i);
			String fileImg = currentFile.toString();
			FilesNames outPutFilesNames = new FilesNames(fileImg);
			FilesNames segCC = new FilesNames(this.chromocenterParameters._segInputFolder +
			                                  File.separator + currentFile.getName());
			this._prefix = outPutFilesNames.prefixNameFile();
			ImagePlus [] _raw = BF.openImagePlus(currentFile.getAbsolutePath());
			//is2D => change
			imageType(_raw[0]);
			String outputFileName= segCcDir+File.separator+currentFile.getName();
			String gradientFileName= diffDir+File.separator+currentFile.getName();
			
			if(segCC.fileExists()) {
				ImagePlus [] segNuc =BF.openImagePlus(this.chromocenterParameters._segInputFolder +
				                                      File.separator+currentFile.getName());
				ChromencenterSegmentation chromencenterSegmentation = new ChromencenterSegmentation(
						_raw,
						segNuc,
						outputFileName,
						this.chromocenterParameters);
				chromencenterSegmentation.runCC3D(gradientFileName);
				NucleusChromocentersAnalysis nucleusChromocenterAnalysis = new NucleusChromocentersAnalysis();
				nucleusChromocenterAnalysis.compute3DParameters(
						rhfChoice,
						_raw[0],
						segNuc[0],
						IJ.openImage(outputFileName),
						this.chromocenterParameters);
			}
			else{
				IJ.log(segCC.getPathFile()+" is missing");
			}
			if(this._isGui){_p._bar.setValue(1 + i);}
			
		}
		if (this._isGui){
			_p.dispose();
		}
	}
	
	
	public void SegmentationOMERO(String inputDirectoryRaw,String inputDirectorySeg,String outputDirectory,Client client)
	throws AccessException, ServiceException, IOException, ExecutionException, InterruptedException, OMEROServerError {
		/* Get  image or Dataset ID */
		String[] param = inputDirectoryRaw.split("/");
		String[] param1 = inputDirectorySeg.split("/");
		
		Long imageID = Long.parseLong(param[1]);
		Long maskID = Long.parseLong(param1[1]);

		if (param.length >= 2 && param1.length >= 2) {
			if ("Image".equals(param[0]) && "Image".equals(param1[0])) {
				runOneImageOMERO(imageID,maskID,outputDirectory,client);
			} else if ("Dataset".equals(param[0]) && "Dataset".equals(param1[0])) {
				dataset_name = client.getDataset(imageID).getName();
				List<ImageWrapper> images;
				List<ImageWrapper> masks;
				/* get raw images and masks datasets*/
				DatasetWrapper imageDataset = client.getDataset(imageID);
				DatasetWrapper maskDataset = client.getDataset(maskID);
				/* get images List */
				images = imageDataset.getImages(client);
				/* Create Dataset named NodeJOMERO */
				outDataset = new DatasetWrapper("NODeJ_"+ dataset_name, "");
				outDatasetGradient = new DatasetWrapper("NODeJ_"+ dataset_name+ "_Gradient", "");
				project  = client.getProject(Long.parseLong(outputDirectory));
				/* Add Dataset To the Project */
				Long datasetId = project.addDataset(client, outDataset).getId();
				outDataset = client.getDataset(datasetId);
				Long gradientDatasetId = project.addDataset(client, outDatasetGradient).getId();
				outDatasetGradient = client.getDataset(gradientDatasetId);
				
				for (ImageWrapper image : images) {
					try {
						/* Get Image name */
						String imageName = image.getName();
						/* Get the mask with the same name */
						masks = maskDataset.getImages(client, imageName);
						/* Run Segmentation */
						runSeveralImagesOMERO(image, masks.get(0), dataset_name, client);
						/* Import Segmented cc to the Dataset*/
						outDataset.importImages(client, segImg);
						outDatasetGradient.importImages(client, gradImg);
						/* Delete the files locally*/
					} catch (Exception ignore) {
						//IGNORE
					}
					try {
						File segImgDelete  = new File(segImg);
						File gradImgDelete = new File(gradImg);
						Files.deleteIfExists(segImgDelete.toPath());
						Files.deleteIfExists(gradImgDelete.toPath());
					} catch (Exception ignore) {
						//IGNORE
					}
					
				}
				/* import Result Tabs to the Dataset */
				outDataset.addFile(client, tab[0]);
				outDataset.addFile(client, tab[1]);
				project.addFile(client, tab[2]);
				project.addFile(client, tab[3]);
				/* Delete the tabs Locally*/
				try {
					Files.deleteIfExists(tab[0].toPath());
					Files.deleteIfExists(tab[1].toPath());
					Files.deleteIfExists(tab[2].toPath());
					Files.deleteIfExists(tab[3].toPath());
				} catch (IOException e) {
					//LOGGER.error("Could not delete file: {}", outputFileName);
				}
			}
		}
	}
	/** Function For OMERO  */
	public void runOneImageOMERO(Long inputDirectoryRaw, Long inputDirectorySeg, String outputDirectory, Client client)
	throws AccessException, ServiceException, ExecutionException, IOException, OMEROServerError, InterruptedException {
		
		String rhfChoice = "Volume";
		
		/* Getting the image and mask from omero */
		ImageWrapper image = client.getImage(inputDirectoryRaw);
		ImageWrapper mask = client.getImage(inputDirectorySeg);
		
		String imageName = image.getName();
		
		/* image to imagePlus */
		ImagePlus[] RawImage = new ImagePlus[]{image.toImagePlus(client)};
		ImagePlus[] SegImage = new ImagePlus[]{mask.toImagePlus(client)};
		
		String diffDir = this.chromocenterParameters.outputFolder+"gradientImage";
		String segCcDir = this.chromocenterParameters.outputFolder+"SegCC";
		
		FilesNames outPutFilesNames = new FilesNames(imageName);
		this._prefix = outPutFilesNames.prefixNameFile();
		
		String outputFileName = imageName;
		String gradientFileName = diffDir+imageName;
		
		/* Test if Raw image is 2D*/
		//is2D => change
		ImagePlus imp = SegImage[0];
		imageType(imp);
		/* Processing */
		ChromencenterSegmentation chromencenterSegmentation = new ChromencenterSegmentation(
				RawImage,
				SegImage,
				outputFileName,
				this.chromocenterParameters);
		
		chromencenterSegmentation.runCC3D(gradientFileName);
		
		NucleusChromocentersAnalysis nucleusChromocenterAnalysis = new NucleusChromocentersAnalysis();
		File[] Parameters3DTab = nucleusChromocenterAnalysis.compute3DParameters(
				rhfChoice,
				RawImage[0],
				SegImage[0],
				IJ.openImage(outputFileName),
				this.chromocenterParameters);
		
		
		/* Import Segmented image to OMERO */
		project  = client.getProject(Long.parseLong(outputDirectory));
		
		/* Creating a Dataset in the Project */
		outDataset = new DatasetWrapper("NODeJ_"+ this._prefix, "");
		Long datasetId = project.addDataset(client, outDataset).getId();
		outDataset = client.getDataset(datasetId);
		/*Import images and tabs to OMERO */
		outDataset.importImages(client, outputFileName);
		outDataset.importImages(client, gradientFileName);
		outDataset.addFile(client, Parameters3DTab[0]);
		outDataset.addFile(client, Parameters3DTab[1]);
		project.addFile(client, Parameters3DTab[2]);
		project.addFile(client, Parameters3DTab[3]);

		File segImgDelete = new File(outputFileName);
		File gradImgDelete = new File(gradientFileName);
		try {
			Files.deleteIfExists(segImgDelete.toPath());
			Files.deleteIfExists(gradImgDelete.toPath());
			Files.deleteIfExists(Parameters3DTab[0].toPath());
			Files.deleteIfExists(Parameters3DTab[1].toPath());
			Files.deleteIfExists(Parameters3DTab[2].toPath());
			Files.deleteIfExists(Parameters3DTab[3].toPath());
		} catch (IOException e) {
			//LOGGER.error("Could not delete file: {}", outputFileName);
		}
	}
	
	public void runSeveralImagesOMERO(ImageWrapper image,ImageWrapper mask, String datasetName,Client client )
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException {
		
		String rhfChoice = "Volume";
		String imageName = image.getName();
		
		/* image to imagePlus */
		ImagePlus[] rawImage = new ImagePlus[]{image.toImagePlus(client)};
		ImagePlus[] segImage = new ImagePlus[]{mask.toImagePlus(client)};
		
		String diffDir  = this.chromocenterParameters.outputFolder + "gradientImage";
		String segCcDir = this.chromocenterParameters.outputFolder + "SegCC";
		FilesNames outPutFilesNames = new FilesNames(imageName);
		
		this._prefix = outPutFilesNames.prefixNameFile();
		String outputFileName   = imageName;
		String gradientFileName = diffDir + imageName;
		
		/* Test if Raw image is 2D*/
		//is2D => change
		ImagePlus imp = rawImage[0];
		imageType(imp);
		/* Processing */
		ChromencenterSegmentation chromencenterSegmentation = new ChromencenterSegmentation(
				rawImage,
				segImage,
				outputFileName,
				this.chromocenterParameters);
		
		chromencenterSegmentation.runCC3D(gradientFileName);
		
		NucleusChromocentersAnalysis nucleusChromocenterAnalysis = new NucleusChromocentersAnalysis();

		File[] parameters3DTab = nucleusChromocenterAnalysis.compute3DParametersOmero(
				rhfChoice,
				image,
				mask,
				IJ.openImage(outputFileName),
				this.chromocenterParameters,
				datasetName,
				client);
		
		tab = parameters3DTab;
		segImg = outputFileName;
		gradImg = gradientFileName;
		
	}
	
	
	/**
	 * Run the Chromocenter segmentation on a single image
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public void just3D() throws IOException, FormatException {
		DebugTools.enableLogging("OFF");
		Directory directoryInput = new Directory(this.chromocenterParameters.getInputFolder());
		directoryInput.listImageFiles(this.chromocenterParameters.getInputFolder());
		directoryInput.checkIfEmpty();
		String rhfChoice = "Volume";
		String nameFileChromocenter = this.chromocenterParameters.outputFolder+"CcParameters.tab";
		
		String segCcDir = this.chromocenterParameters.outputFolder;
		
		// TODO A REFAIRE C EST MOCHE !!!!
		System.out.println("size: "+ directoryInput.getNumberFiles());
		
		for (short i = 0; i < directoryInput.getNumberFiles(); ++i) {
			File currentFile = directoryInput.getFile(i);
			String fileImg = currentFile.toString();
			ImagePlus [] _raw = BF.openImagePlus(currentFile.getAbsolutePath());
			imageType(_raw[0]);
			String outputFileName= segCcDir+File.separator+currentFile.getName();
			ImagePlus [] segNuc =BF.openImagePlus(this.chromocenterParameters._segInputFolder+File.separator+currentFile.getName());
			// ChromocenterAnalysis chromocenterAnalysis = new ChromocenterAnalysis(_raw[0],segNuc[0], IJ.openImage(outputFileName));
			// chromocenterAnalysis.computeParametersChromocenter();
			
			NucleusChromocentersAnalysis nucleusChromocenterAnalysis = new NucleusChromocentersAnalysis();
			nucleusChromocenterAnalysis.compute3DParameters(
					rhfChoice,
					_raw[0],
					segNuc[0],
					IJ.openImage(outputFileName),
					this.chromocenterParameters);
		}
	}
	
	/**
	 *
	 * @param ramImage
	 */
	public void imageType(ImagePlus ramImage){
		ramImage.getDimensions();
		if(ramImage.getStackSize()==1){
			this._is2DImg =true;
		}
	}
	
}
