/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.igred.nucleus.autocrop;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.FilesNames;
import fr.igred.nucleus.io.OutputTextFile;
import ij.IJ;
import loci.formats.FormatException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Core method calling the autocrop method.
 * <p>This method can be run on only one file or on directory containing multiple tuple file.
 * <p>This class will call AutoCrop class to detect nuclei in the image.
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class AutoCropCalling {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Number of threads used to download images */
	private static final int DOWNLOADER_THREADS = 1;
	
	/** Column names */
	private static final String HEADERS = "FileName\tNumberOfCrop\tOTSUThreshold\tDefaultOTSUThreshold\n";
	
	/** Get general information of cropping analysis */
	private String             outputCropGeneralInfo = "#HEADER\n";
	/** Parameters for crop analysis */
	private AutocropParameters autocropParameters;
	
	/** Number of threads to used process images */
	private int    executorThreads  = 1;
	/** Type of thresholding method used process images */
	private String typeThresholding = "Otsu";
	
	
	public AutoCropCalling(AutocropParameters autocropParameters) {
		this.autocropParameters = autocropParameters;
		this.outputCropGeneralInfo = autocropParameters.getAnalysisParameters() + HEADERS;
	}
	
	
	/**
	 * Setter for the number of threads used to process images
	 *
	 * @param threadNumber number of executors threads
	 */
	public void setExecutorThreads(int threadNumber) {
		this.executorThreads = threadNumber;
	}
	
	
	/**
	 * Setter for the thresholding method used to process images
	 */
	public void setTypeThresholding(String typeThresholding) {
		this.typeThresholding = typeThresholding;
	}
	
	
	/**
	 * Run auto crop on image's folder: -If input is a file: open the image with bio-formats plugin to obtain the
	 * metadata then run the auto crop. -If input is directory, listed the file, foreach tif file loaded file with
	 * bio-formats, run the auto crop.
	 */
	public void runFolder() {
		ExecutorService processExecutor = Executors.newFixedThreadPool(executorThreads);
		
		ConcurrentHashMap<String, String> outputCropGeneralLines = new ConcurrentHashMap<>();
		
		Directory directoryInput = new Directory(autocropParameters.getInputFolder());
		directoryInput.listImageFiles(autocropParameters.getInputFolder());
		directoryInput.checkIfEmpty();
		directoryInput.checkAndActualiseNDFiles();
		
		List<File>     files = directoryInput.listFiles();
		CountDownLatch latch = new CountDownLatch(files.size());
		
		class ImageProcessor implements Runnable {
			
			private final File file;
			
			
			ImageProcessor(File file) {
				this.file = file;
			}
			
			
			@Override
			public void run() {
				LOGGER.info("Current file: {}", file.getAbsolutePath());
				String     fileImg          = file.toString();
				FilesNames outPutFilesNames = new FilesNames(fileImg);
				String     prefix           = outPutFilesNames.prefixNameFile();
				try {
					AutoCrop autoCrop = new AutoCrop(file, prefix, autocropParameters);
					autoCrop.thresholdKernels(typeThresholding);
					autoCrop.computeConnectedComponent();
					autoCrop.componentBorderFilter();
					autoCrop.componentSizeFilter();
					autoCrop.computeBoxes();
					autoCrop.addCropParameter();
					autoCrop.boxIntersection();
					autoCrop.cropKernels();
					autoCrop.writeAnalyseInfo();
					AnnotateAutoCrop annotate = new AnnotateAutoCrop(autoCrop.getFileCoordinates(),
					                                                 file,
					                                                 autocropParameters.getOutputFolder() +
					                                                 File.separator,
					                                                 prefix,
					                                                 autocropParameters);
					annotate.run();
					
					outputCropGeneralLines.put(file.getName(), autoCrop.getImageCropInfo());
					
					latch.countDown();
				} catch (IOException | FormatException e) {
					LOGGER.error("Cannot run autocrop on: {}", file.getName(), e);
					IJ.error("Cannot run autocrop on " + file.getName());
				}
			}
			
		}
		
		for (File currentFile : files) {
			processExecutor.submit(new ImageProcessor(currentFile));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted while waiting for image processing to finish.", e);
			Thread.currentThread().interrupt();
		}
		processExecutor.shutdownNow();
		
		StringBuilder generalInfoBuilder = new StringBuilder();
		for (File file : files) {
			generalInfoBuilder.append(outputCropGeneralLines.get(file.getName()));
		}
		outputCropGeneralInfo += generalInfoBuilder.toString();
		
		saveGeneralInfo();
	}
	
	
	/**
	 * Run auto crop on one image : -If input is a file: open the image with bio-formats plugin to obtain the metadata
	 * then run the auto crop. -If input is directory, listed the file, foreach tif file loaded file with bio-formats,
	 * run the auto crop.
	 *
	 * @param file
	 */
	public void runFile(String file) {
		File currentFile = new File(file);
		LOGGER.info("Current file: {}", currentFile.getAbsolutePath());
		String     fileImg          = currentFile.toString();
		FilesNames outPutFilesNames = new FilesNames(fileImg);
		/* image prefix name */
		String prefix = outPutFilesNames.prefixNameFile();
		try {
			AutoCrop autoCrop = new AutoCrop(currentFile, prefix, autocropParameters);
			autoCrop.thresholdKernels(typeThresholding);
			autoCrop.computeConnectedComponent();
			autoCrop.componentBorderFilter();
			autoCrop.componentSizeFilter();
			autoCrop.computeBoxes();
			autoCrop.addCropParameter();
			autoCrop.boxIntersection();
			autoCrop.cropKernels();
			LOGGER.info("ENDED CROPPING");
			autoCrop.writeAnalyseInfo();
			AnnotateAutoCrop annotate = new AnnotateAutoCrop(autoCrop.getFileCoordinates(),
			                                                 currentFile,
			                                                 autocropParameters.getOutputFolder() + File.separator,
			                                                 prefix,
			                                                 autocropParameters);
			annotate.run();
			this.outputCropGeneralInfo += autoCrop.getImageCropInfo();
		} catch (IOException | FormatException e) {
			LOGGER.error("Cannot run autocrop on: {}", currentFile.getName(), e);
			IJ.error("Cannot run autocrop on " + currentFile.getName());
		}
	}
	
	
	public void saveGeneralInfo() {
		LOGGER.info("{}result_Autocrop_Analyse", autocropParameters.getInputFolder());
		OutputTextFile resultFileOutput = new OutputTextFile(autocropParameters.getOutputFolder() + "result_Autocrop_Analyse.csv");
		resultFileOutput.saveTextFile(outputCropGeneralInfo, true);
	}
	
	
	public void runImageOMERO(ImageWrapper image, Long[] outputsDatImages, Client client)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException {
		String fileImg = image.getName();
		LOGGER.info("Current file: {}", fileImg);
		FilesNames outPutFilesNames = new FilesNames(fileImg);
		String     prefix           = outPutFilesNames.prefixNameFile();
		AutoCrop   autoCrop         = new AutoCrop(image, autocropParameters, client);
		autoCrop.thresholdKernels(typeThresholding);
		autoCrop.computeConnectedComponent();
		autoCrop.componentBorderFilter();
		autoCrop.componentSizeFilter();
		autoCrop.computeBoxes();
		autoCrop.addCropParameter();
		autoCrop.boxIntersection();
		autoCrop.cropKernelsOMERO(image, outputsDatImages, client);
		autoCrop.writeAnalyseInfoOMERO(outputsDatImages[autocropParameters.getChannelToComputeThreshold()], client);
		AnnotateAutoCrop annotate = new AnnotateAutoCrop(autoCrop.getFileCoordinates(),
		                                                 autoCrop.getRawImage(),
		                                                 autocropParameters.getOutputFolder() + File.separator,
		                                                 prefix,
		                                                 autocropParameters);
		annotate.run();
		long outputProject = -1;
		// TODO Find a better way to get output project (maybe just pass it as a parameter)
		for (ProjectWrapper p : client.getProjects()) {
			for (DatasetWrapper d : p.getDatasets()) {
				if (d.getId() == outputsDatImages[0]) {
					outputProject = p.getId();
					break;
				}
			}
		}
		annotate.saveProjectionOMERO(client, outputProject);
		this.outputCropGeneralInfo += autoCrop.getImageCropInfoOmero(image.getName());
	}
	
	
	public void runSeveralImageOMERO(Collection<? extends ImageWrapper> images, Long[] outputsDatImages, Client client)
	throws AccessException, ServiceException, ExecutionException, InterruptedException {
		ExecutorService downloadExecutor = Executors.newFixedThreadPool(DOWNLOADER_THREADS);
		ExecutorService processExecutor  = Executors.newFixedThreadPool(executorThreads);
		
		ConcurrentHashMap<String, String> outputCropGeneralLines = new ConcurrentHashMap<>();
		
		CountDownLatch latch = new CountDownLatch(images.size());
		
		long outputFound = -1;
		// TODO Find a better way to get output project (maybe just pass it as a parameter)
		for (ProjectWrapper p : client.getProjects()) {
			for (DatasetWrapper d : p.getDatasets()) {
				if (d.getId() == outputsDatImages[0]) {
					outputFound = p.getId();
					break;
				}
			}
		}
		long outputProject = outputFound;
		
		class ImageProcessor implements Runnable {
			private final AutoCrop     autoCrop;
			private final ImageWrapper image;
			
			
			ImageProcessor(AutoCrop autoCrop, ImageWrapper image) {
				this.autoCrop = autoCrop;
				this.image = image;
			}
			
			
			@Override
			public void run() {
				autoCrop.thresholdKernels(typeThresholding);
				autoCrop.computeConnectedComponent();
				autoCrop.componentBorderFilter();
				autoCrop.componentSizeFilter();
				autoCrop.computeBoxes();
				autoCrop.addCropParameter();
				autoCrop.boxIntersection();
				try {
					autoCrop.cropKernelsOMERO(image, outputsDatImages, client);
					autoCrop.writeAnalyseInfoOMERO(outputsDatImages[autocropParameters.getChannelToComputeThreshold()],
					                               client);
					
					AnnotateAutoCrop annotate = new AnnotateAutoCrop(autoCrop.getFileCoordinates(),
					                                                 autoCrop.getRawImage(),
					                                                 autocropParameters.getOutputFolder() +
					                                                 File.separator,
					                                                 FilenameUtils.removeExtension(image.getName()),
					                                                 autocropParameters);
					annotate.run();
					annotate.saveProjectionOMERO(client, outputProject);
				} catch (AccessException | ServiceException | OMEROServerError | IOException | ExecutionException e) {
					LOGGER.error("Cannot run autocrop on: {}", image.getName(), e);
				}
				
				outputCropGeneralLines.put(image.getName(), autoCrop.getImageCropInfo());
				
				latch.countDown();
			}
			
		}
		
		
		class ImageDownloader implements Runnable {
			
			private final ImageWrapper image;
			
			
			ImageDownloader(ImageWrapper image) {
				this.image = image;
			}
			
			
			@Override
			public void run() {
				String fileImg = image.getName();
				LOGGER.info("Current file: {}", fileImg);
				AutoCrop autoCrop = null;
				try {
					autoCrop = new AutoCrop(image, autocropParameters, client);
				} catch (ServiceException | AccessException | ExecutionException e) {
					LOGGER.error("Cannot create AutoCrop for image: {}", fileImg, e);
				}
				processExecutor.submit(new ImageProcessor(autoCrop, image));
			}
			
		}
		
		for (ImageWrapper image : images) {
			downloadExecutor.submit(new ImageDownloader(image));
		}
		latch.await();
		downloadExecutor.shutdownNow();
		processExecutor.shutdownNow();
		
		StringBuilder generalInfoBuilder = new StringBuilder();
		for (ImageWrapper image : images) {
			generalInfoBuilder.append(outputCropGeneralLines.get(image.getName()));
		}
		outputCropGeneralInfo += generalInfoBuilder.toString();
		
		saveGeneralInfoOmero(client, outputsDatImages);
	}
	
	
	public void saveGeneralInfoOmero(Client client, Long[] outputsDatImages)
	throws InterruptedException {
		String         resultPath       = autocropParameters.getOutputFolder() + "result_Autocrop_Analyse.csv";
		File           resultFile       = new File(resultPath);
		OutputTextFile resultFileOutput = new OutputTextFile(resultPath);
		resultFileOutput.saveTextFile(outputCropGeneralInfo, false);
		
		try {
			client.getDataset(outputsDatImages[autocropParameters.getChannelToComputeThreshold()])
			      .addFile(client, resultFile);
		} catch (ServiceException se) {
			LOGGER.error("Could not connect to OMERO.", se);
		} catch (AccessException ae) {
			LOGGER.error("Could not access data on OMERO.", ae);
		} catch (ExecutionException e) {
			LOGGER.error("Could not add file to dataset.", e);
		}
		try {
			Files.deleteIfExists(resultFile.toPath());
		} catch (IOException io) {
			LOGGER.error("Problem while deleting file: {}", resultPath, io);
		}
	}
	
}
