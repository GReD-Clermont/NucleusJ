package gred.nucleus.process;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import gred.nucleus.io.Directory;
import gred.nucleus.io.FilesNames;
import gred.nucleus.gui.Progress;
import gred.nucleus.plugins.ChromocenterParameters;
import gred.nucleus.utilsNj2.NucleusChromocentersAnalysis;
import ij.IJ;
import ij.ImagePlus;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.plugins.BF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ChromocenterCalling {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final ChromocenterParameters chromocenterParameters;
	
	private String         prefix;
	private boolean        is2DImg;
	private boolean        isGui;
	private Progress       progress;
	private File[]         tab;
	private ProjectWrapper project;
	private DatasetWrapper outDataset;
	private String         segImg;
	private String         gradImg;
	
	
	/**
	 * @param chromocenterParameters
	 */
	public ChromocenterCalling(ChromocenterParameters chromocenterParameters) {
		this.chromocenterParameters = chromocenterParameters;
	}
	
	
	public ChromocenterCalling(ChromocenterParameters chromocenterParameters, boolean gui) {
		this.chromocenterParameters = chromocenterParameters;
		this.isGui = gui;
	}
	
	
	/**
	 * Run the Chromocenter segmentation on several images
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public void runSeveralImages2() throws IOException, FormatException {
		DebugTools.enableLogging("OFF");
		Directory directoryInput = new Directory(chromocenterParameters.getInputFolder());
		directoryInput.listImageFiles(chromocenterParameters.getInputFolder());
		directoryInput.checkIfEmpty();
		String rhfChoice = "Volume";
		String diffDir   = chromocenterParameters.outputFolder + "gradientImage";
		File   file      = new File(diffDir);
		if (!file.exists()) {
			file.mkdir();
		}
		
		String segCcDir = chromocenterParameters.outputFolder + "SegCC";
		file = new File(segCcDir);
		if (!file.exists()) {
			file.mkdir();
		}
		// TODO A REFAIRE C EST MOCHE !!!!
		LOGGER.info("size: {}", directoryInput.getNumberFiles());
		
		if (isGui) {
			progress = new Progress("Images Analysis: ", directoryInput.getNumberFiles());
			progress.setValue(0);
		}
		for (short i = 0; i < directoryInput.getNumberFiles(); ++i) {
			File       currentFile      = directoryInput.getFile(i);
			String     fileImg          = currentFile.toString();
			FilesNames outPutFilesNames = new FilesNames(fileImg);
			FilesNames segCC = new FilesNames(chromocenterParameters.segInputFolder +
			                                  File.separator + currentFile.getName());
			this.prefix = outPutFilesNames.prefixNameFile();
			ImagePlus[] raw = BF.openImagePlus(currentFile.getAbsolutePath());
			//is2D => change
			imageType(raw[0]);
			String outputFileName   = segCcDir + File.separator + currentFile.getName();
			String gradientFileName = diffDir + File.separator + currentFile.getName();
			
			if (segCC.fileExists()) {
				ImagePlus[] segNuc = BF.openImagePlus(chromocenterParameters.segInputFolder +
				                                      File.separator + currentFile.getName());
				ChromocenterSegmentation chromocenterSegmentation = new ChromocenterSegmentation(raw,
				                                                                                 segNuc,
				                                                                                 outputFileName,
				                                                                                 chromocenterParameters);
				chromocenterSegmentation.runCC3D(gradientFileName);
				NucleusChromocentersAnalysis.compute3DParameters(rhfChoice,
				                                                 raw[0],
				                                                 segNuc[0],
				                                                 IJ.openImage(outputFileName),
				                                                 chromocenterParameters);
			} else {
				IJ.log(segCC.getPathFile() + " is missing");
			}
			if (isGui) {
				progress.setValue(1 + i);
			}
			
		}
		if (isGui) {
			progress.dispose();
		}
	}
	
	
	public void SegmentationOMERO(String inputDirectoryRaw,
	                              String inputDirectorySeg,
	                              String outputDirectory,
	                              Client client)
	throws AccessException, ServiceException, IOException, ExecutionException, InterruptedException, OMEROServerError {
		/* Get  image or Dataset ID */
		String[] param  = inputDirectoryRaw.split("/");
		String[] param1 = inputDirectorySeg.split("/");
		
		Long imageID = Long.parseLong(param[1]);
		Long maskID  = Long.parseLong(param1[1]);
		
		if (param.length >= 2 && param1.length >= 2) {
			if ("Image".equals(param[0]) && "Image".equals(param1[0])) {
				runOneImageOMERO(imageID, maskID, outputDirectory, client);
			} else if ("Dataset".equals(param[0]) && "Dataset".equals(param1[0])) {
				String             datasetName = client.getDataset(imageID).getName();
				List<ImageWrapper> images;
				List<ImageWrapper> masks;
				/* get raw images and masks datasets*/
				DatasetWrapper imageDataset = client.getDataset(imageID);
				DatasetWrapper maskDataset  = client.getDataset(maskID);
				/* get images List */
				images = imageDataset.getImages(client);
				/* Create Dataset named NodeJOMERO */
				outDataset = new DatasetWrapper("NODeJ_" + datasetName, "");
				DatasetWrapper outDatasetGradient = new DatasetWrapper("NODeJ_" + datasetName + "_Gradient", "");
				project = client.getProject(Long.parseLong(outputDirectory));
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
						runSeveralImagesOMERO(image, masks.get(0), datasetName, client);
						/* Import Segmented cc to the Dataset*/
						outDataset.importImages(client, segImg);
						outDatasetGradient.importImages(client, gradImg);
						/* Delete the files locally*/
					} catch (AccessException | OMEROServerError | ServiceException | IOException |
					         ExecutionException ignore) {
						//IGNORE
					}
					try {
						File segImgDelete  = new File(segImg);
						File gradImgDelete = new File(gradImg);
						Files.deleteIfExists(segImgDelete.toPath());
						Files.deleteIfExists(gradImgDelete.toPath());
					} catch (IOException ignore) {
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
	
	
	/** Function For OMERO */
	public void runOneImageOMERO(Long inputDirectoryRaw, Long inputDirectorySeg, String outputDirectory, Client client)
	throws AccessException, ServiceException, ExecutionException, IOException, OMEROServerError, InterruptedException {
		
		String rhfChoice = "Volume";
		
		/* Getting the image and mask from omero */
		ImageWrapper image = client.getImage(inputDirectoryRaw);
		ImageWrapper mask  = client.getImage(inputDirectorySeg);
		
		String imageName = image.getName();
		
		/* image to imagePlus */
		ImagePlus[] rawImage = {image.toImagePlus(client)};
		ImagePlus[] segImage = {mask.toImagePlus(client)};
		
		String diffDir = chromocenterParameters.outputFolder + "gradientImage";
		
		FilesNames outPutFilesNames = new FilesNames(imageName);
		this.prefix = outPutFilesNames.prefixNameFile();
		
		String outputFileName   = imageName;
		String gradientFileName = diffDir + imageName;
		
		/* Test if Raw image is 2D*/
		//is2D => change
		ImagePlus imp = segImage[0];
		imageType(imp);
		/* Processing */
		ChromocenterSegmentation chromocenterSegmentation = new ChromocenterSegmentation(rawImage,
		                                                                                 segImage,
		                                                                                 outputFileName,
		                                                                                 chromocenterParameters);
		
		chromocenterSegmentation.runCC3D(gradientFileName);
		
		File[] parameters3DTab = NucleusChromocentersAnalysis.compute3DParameters(rhfChoice, rawImage[0], segImage[0],
		                                                                          IJ.openImage(outputFileName),
		                                                                          chromocenterParameters);
		
		/* Import Segmented image to OMERO */
		project = client.getProject(Long.parseLong(outputDirectory));
		
		/* Creating a Dataset in the Project */
		outDataset = new DatasetWrapper("NODeJ_" + prefix, "");
		Long datasetId = project.addDataset(client, outDataset).getId();
		outDataset = client.getDataset(datasetId);
		/*Import images and tabs to OMERO */
		outDataset.importImages(client, outputFileName);
		outDataset.importImages(client, gradientFileName);
		outDataset.addFile(client, parameters3DTab[0]);
		outDataset.addFile(client, parameters3DTab[1]);
		project.addFile(client, parameters3DTab[2]);
		project.addFile(client, parameters3DTab[3]);
		
		File segImgDelete  = new File(outputFileName);
		File gradImgDelete = new File(gradientFileName);
		try {
			Files.deleteIfExists(segImgDelete.toPath());
			Files.deleteIfExists(gradImgDelete.toPath());
			Files.deleteIfExists(parameters3DTab[0].toPath());
			Files.deleteIfExists(parameters3DTab[1].toPath());
			Files.deleteIfExists(parameters3DTab[2].toPath());
			Files.deleteIfExists(parameters3DTab[3].toPath());
		} catch (IOException e) {
			//LOGGER.error("Could not delete file: {}", outputFileName);
		}
	}
	
	
	public void runSeveralImagesOMERO(ImageWrapper image, ImageWrapper mask, String datasetName, Client client)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException {
		
		String rhfChoice = "Volume";
		String imageName = image.getName();
		
		/* image to imagePlus */
		ImagePlus[] rawImage = {image.toImagePlus(client)};
		ImagePlus[] segImage = {mask.toImagePlus(client)};
		
		String diffDir = chromocenterParameters.outputFolder + "gradientImage";
		
		FilesNames outPutFilesNames = new FilesNames(imageName);
		
		this.prefix = outPutFilesNames.prefixNameFile();
		String outputFileName   = imageName;
		String gradientFileName = diffDir + imageName;
		
		/* Test if Raw image is 2D*/
		//is2D => change
		ImagePlus imp = rawImage[0];
		imageType(imp);
		/* Processing */
		ChromocenterSegmentation chromocenterSegmentation = new ChromocenterSegmentation(rawImage,
		                                                                                 segImage,
		                                                                                 outputFileName,
		                                                                                 chromocenterParameters);
		
		chromocenterSegmentation.runCC3D(gradientFileName);
		
		File[] parameters3DTab = NucleusChromocentersAnalysis.compute3DParametersOmero(rhfChoice,
		                                                                               image,
		                                                                               mask,
		                                                                               IJ.openImage(outputFileName),
		                                                                               chromocenterParameters,
		                                                                               datasetName,
		                                                                               client);
		
		tab = parameters3DTab;
		segImg = outputFileName;
		gradImg = gradientFileName;
	}
	
	
	/**
	 * @param img
	 */
	public void imageType(ImagePlus img) {
		img.getDimensions();
		if (img.getStackSize() == 1) {
			is2DImg = true;
		}
	}
	
}
