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
package fr.igred.nucleus.segmentation;

import fr.igred.nucleus.io_L_O.BatchImage;
import fr.igred.nucleus.io_L_O.LocalBatchImage;
import fr.igred.nucleus.io_L_O.OMEROBatchImage;
import fr.igred.nucleus.utils.ConvexHullDetection;
import fr.igred.nucleus.io.Directory;
import fr.igred.nucleus.io.OutputTextFile;
import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.roi.ROIWrapper;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.lineSeparator;
import static java.time.LocalDateTime.now;


/**
 * This class call the different segmentation methods available to detect the nucleus. The Otsu method modified and the
 * 3D convex hull algorithm. Methods can be call for analysis of several images or only one. The convex hull algorithm
 * is initialized by the Otsu method modified, then the convex hull algorithm process the result obtain with the first
 * method. If the first method doesn't detect a nucleus, a message is print on the console.
 * <p>
 * if the nucleus input image is 16bit, a preprocess is done to convert it in 8bit, and also increase the contrast and
 * decrease the noise, then the 8bits image is used for the nuclear segmentation.
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class SegmentationCalling {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Number of threads used to download images */
	private static final int DOWNLOADER_THREADS = 1;
	
	/** SegmentationParameters object containing the parameters for the segmentation */
	private final SegmentationParameters params;
	
	/** Output file for the Otsu crop general info */
	private String outputCropGeneralInfoOTSU;
	
	/** Output file for the Convex Hull crop general info */
	private String outputCropGeneralInfoConvexHull;
	
	/** String containing the info for Parade */
	private String outputInfoParade = getResultsColumnNames();
	
	/** String containing the info for parade (for Graham) */
	private String outputInfoParadeGraham = getResultsColumnNames();
	
	/** Number of threads used to process images */
	private int executorThreads = 1;
	
	/** ID of the input dataset in OMERO */
	private Long tID;
	
	/** Name of the image dataset in OMERO */
	private String imgDatasetName;
	
	/** ID of the image dataset in OMERO */
	private long imgDatasetId;
	
	
	/**
	 * Constructor for ImagePlus input
	 *
	 * @param params List of parameters in config file.
	 */
	public SegmentationCalling(SegmentationParameters params) {
		this.params = params;
		this.outputCropGeneralInfoOTSU = this.params.getAnalysisParameters();
		this.outputCropGeneralInfoConvexHull = this.params.getAnalysisParameters();
	}
	
	
	public static String getResultsColumnNames() {
		return "Image," +
		       "Dataset," +
		       "ImageName," +
		       "Volume," +
		       "Flatness," +
		       "Elongation," +
		       "Esr," +
		       "SurfaceArea," +
		       "Sphericity," +
		       "MeanIntensityNucleus," +
		       "MeanIntensityBackground," +
		       "StandardDeviation," +
		       "MinIntensity," +
		       "MaxIntensity," +
		       "MedianIntensityImage," +
		       "MedianIntensityNucleus," +
		       "MedianIntensityBackground," +
		       "ImageSize," +
		       "Moment 1," +
		       "Moment 2," +
		       "Moment 3," +
		       "AspectRatio," +
		       "Circularity," +
		       "OTSUThreshold," +
		       lineSeparator();
	}
	
	
	/**
	 * Returns the current date and time formatted according to the specified pattern.
	 *
	 * @param pattern the date-time pattern to use
	 *
	 * @return a formatted string representing the current date and time
	 */
	static String currentDateTime(String pattern) {
		return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(now());
	}
	
	
	/**
	 * Returns the current date and time formatted as "yyyy-MM-dd:HH-mm-ss".
	 *
	 * @return a formatted string representing the current date and time
	 */
	static String currentDateTime() {
		return currentDateTime("yyyy-MM-dd:HH-mm-ss");
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
	 * Method to run the nuclear segmentation of images stocked in input dir. First listing of the tif files contained
	 * in input dir. then for each images: the method will call method in NucleusSegmentation and ConvexHullSegmentation
	 * to segment the input nucleus. if the input boolean is true the convex hull algorithm will be use, if false the
	 * Otsu modified method will be used. If a segmentation results is find the method will then computed the different
	 * parameters with the NucleusAnalysis class, and save in file in the outputDir. If no nucleus is detected a log
	 * message is print in the console
	 * <p>
	 * Open the image with bio-formats plugin to obtain the metadata: ImagePlus[] imgTab = BF.openImagePlus(fileImg);
	 *
	 * @return String with the name files which failed in the segmentation step
	 *
	 * @throws IOException     if file doesn't existed
	 * @throws FormatException Bio-formats exception
	 */
	public String runSeveralImages() throws IOException, FormatException {
		String log = "";
		
		ExecutorService processExecutor = Executors.newFixedThreadPool(executorThreads);
		
		Directory directoryInput = new Directory(params.getInputFolder());
		directoryInput.listImageFiles(params.getInputFolder());
		directoryInput.checkIfEmpty();
		
		// Create output directories
		Path otsuDirectory = Paths.get(params.getOutputFolder() + File.separator + "OTSU");
		Path convexHullDirectory = Paths.get(params.getOutputFolder() + File.separator +
		                                     ConvexHullDetection.CONVEX_HULL_ALGORITHM);
		File otsuDir = new File(otsuDirectory.toString());
		if (!otsuDir.exists()) {
			Files.createDirectory(otsuDirectory);
		}
		File convexHullDir = new File(convexHullDirectory.toString());
		if (!convexHullDir.exists()) {
			Files.createDirectory(convexHullDirectory);
		}
		
		List<File>     files = directoryInput.listFiles();
		CountDownLatch latch = new CountDownLatch(files.size());
		
		Map<String, String> otsuResults       = new ConcurrentHashMap<>(files.size());
		Map<String, String> convexHullResults = new ConcurrentHashMap<>(files.size());
		
		class ImageProcessor implements Runnable {
			
			private final File file;
			
			
			ImageProcessor(File file) {
				this.file = file;
			}
			
			
			@Override
			public void run() {
				try {
					String fileImg = file.toString();

					String start = currentDateTime();
					LOGGER.info("Current image in process: {} {} Start : {}", fileImg, lineSeparator(), start);
					NucleusSegmentation nucleusSegmentation = load(new LocalBatchImage(file,0));
					compute(nucleusSegmentation);//////////////

					BatchImage badCropContext = new LocalBatchImage(new File(params.getInputFolder()), 0);
					nucleusSegmentation.checkBadCrop(badCropContext);
					badCropContext.save(nucleusSegmentation, null);
					otsuResults.put(file.getName(),
					                nucleusSegmentation.getImageCropInfoOTSU()); // Put in thread safe collection
					convexHullResults.put(file.getName(),
					                      nucleusSegmentation.getImageCropInfoConvexHull()); // Put in thread safe collection

					String end = currentDateTime();
					LOGGER.info("End: {} at {}", fileImg, end);
					latch.countDown();
				} catch (IOException | ServiceException | AccessException | ExecutionException | FormatException | OMEROServerError e) {
					LOGGER.error("Error processing image: {}", file.getName(), e);
				}
			}
			
		}
		
		for (File currentFile : files) {
			processExecutor.submit(new ImageProcessor(currentFile));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.error("Error waiting for image processing to finish", e);
			Thread.currentThread().interrupt();
		}
		processExecutor.shutdownNow();
		
		StringBuilder otsuInfoBuilder     = new StringBuilder();
		StringBuilder convHullInfoBuilder = new StringBuilder();
		for (File file : files) {
			otsuInfoBuilder.append(otsuResults.get(file.getName()));
			convHullInfoBuilder.append(convexHullResults.get(file.getName()));
		}
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		outputCropGeneralInfoOTSU += otsuInfoBuilder.toString();
		this.outputCropGeneralInfoConvexHull += getResultsColumnNames();
		outputCropGeneralInfoConvexHull += convHullInfoBuilder.toString();
		
		saveCropGeneralInfo();
		
		return log;
	}
	

	/** Input step — loads a local image into a NucleusSegmentation. */
	public NucleusSegmentation  load(BatchImage source) throws IOException, FormatException, ServiceException, AccessException, ExecutionException {
		return new NucleusSegmentation(source, params);
	}


	/** Compute step — runs the segmentation pipeline. Source-agnostic (local or OMERO). */
	public void compute(NucleusSegmentation seg) {
		seg.preProcessImage();
		seg.findOTSUMaximisingSphericity();
	}


	/**
	 * Output step — persists the segmented image and appends CSV info.
	 * <p>If {@code datasets == null} the source is treated as local (writes to disk);
	 * otherwise it uploads to the given OMERO datasets.
	 */
	public void saveOneImage(NucleusSegmentation seg, BatchImage source, OutputDatasets datasets)
	throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
		seg.checkBadCrop(source);
		source.save(seg, datasets);
		this.outputCropGeneralInfoOTSU       += getResultsColumnNames() + seg.getImageCropInfoOTSU();
		this.outputCropGeneralInfoConvexHull += getResultsColumnNames() + seg.getImageCropInfoConvexHull();
	}


	public String runOneImage(String filePath)
	throws IOException, FormatException, ServiceException, AccessException, ExecutionException, OMEROServerError {
		String log         = "";
		File   currentFile = new File(filePath);

		LOGGER.info("Current image in process: {}", currentFile);
		if (currentFile.exists()) {
			String start = currentDateTime();
			LOGGER.info("Start: {}", start);
			NucleusSegmentation seg = load(new LocalBatchImage(currentFile,0));
			compute(seg);
			saveOneImage(seg, new LocalBatchImage(new File(params.getInputFolder()), 0), null);
			String end = currentDateTime();
			LOGGER.info("End: {}", end);
		} else {
			log = "File " + currentFile + " does not exist.";
			LOGGER.error(log);
		}
		return log;
	}
	
	
	public void saveCropGeneralInfo() {
		String date = currentDateTime("yyyy-MM-dd_HH-mm-ss");
		LOGGER.info("Saving crop general info.");
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(params.getOutputFolder() +
		                                                         "OTSU" +
		                                                         File.separator +
		                                                         date +
		                                                         "-result_Segmentation_Analyse_OTSU.csv");
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, true);
		if (params.getConvexHullDetection()) {
			OutputTextFile outputConvexHull = new OutputTextFile(params.getOutputFolder() +
			                                                     ConvexHullDetection.CONVEX_HULL_ALGORITHM +
			                                                     File.separator +
			                                                     date +
			                                                     "-result_Segmentation_Analyse_" +
			                                                     ConvexHullDetection.CONVEX_HULL_ALGORITHM +
			                                                     ".csv");
			outputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, true);
		}
	}
	
	
	public void saveTestCropGeneralInfo() {
		LOGGER.info("Saving crop general info.");
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(params.getOutputFolder() +
		                                                         "OTSU" +
		                                                         File.separator +
		                                                         "result_Segmentation_Analyse_OTSU.csv");
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, true);
		if (params.getConvexHullDetection()) {
			OutputTextFile outputConvexHull = new OutputTextFile(params.getOutputFolder() +
			                                                     ConvexHullDetection.CONVEX_HULL_ALGORITHM +
			                                                     File.separator +
			                                                     "result_Segmentation_Analyse_" +
			                                                     ConvexHullDetection.CONVEX_HULL_ALGORITHM +
			                                                     ".csv");
			outputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, true);
		}
	}
	
	/** Pair of OMERO dataset IDs (OTSU and Convex Hull) used to upload segmentation outputs. */
	public static final class OutputDatasets {
		final long otsu;
		final long convexHull;

		OutputDatasets(long otsu, long convexHull) {
			this.otsu = otsu;
			this.convexHull = convexHull;
		}

		public long getOtsu()       { return otsu; }
		public long getConvexHull() { return convexHull; }
	}


	/** Prepares the OMERO output datasets (creates them if missing) and returns their IDs. */
	public OutputDatasets prepareOutputDatasetsOMERO(Long output, Client client)
	throws AccessException, ServiceException, ExecutionException {
		ProjectWrapper project = client.getProject(output);

		List<DatasetWrapper> datasets = project.getDatasets("OTSU");
		long otsuDataset;
		long convexHullDataset = -1;
		if (datasets.isEmpty()) {
			otsuDataset = project.addDataset(client, "OTSU", "").getId();
			project.reload(client);
		} else {
			otsuDataset = datasets.get(0).getId();
		}
		project.reload(client);
		if (params.getConvexHullDetection()) {
			datasets = project.getDatasets(ConvexHullDetection.CONVEX_HULL_ALGORITHM);
			if (datasets.isEmpty()) {
				convexHullDataset = project.addDataset(client, ConvexHullDetection.CONVEX_HULL_ALGORITHM, "").getId();
				project.reload(client);
			} else {
				convexHullDataset = datasets.get(0).getId();
			}
		}
		return new OutputDatasets(otsuDataset, convexHullDataset);
	}





	public String runOneImageOMERO(ImageWrapper image, Long output, Client client)
	throws IOException, FormatException, ServiceException, AccessException, ExecutionException, OMEROServerError {
		String log = "";

		LOGGER.info("Current image in process: {}", image.getName());
		OutputDatasets datasets = prepareOutputDatasetsOMERO(output, client);
		String start = currentDateTime();
		LOGGER.info("Start: {}", start);
		BatchImage source = new OMEROBatchImage(image, client, null, null, new int[]{0,0}, null, null);
		NucleusSegmentation seg = load(source);
		compute(seg);
		saveOneImage(seg, source, datasets);
		String end = currentDateTime();
		LOGGER.info("End: {}", end);

		return log;
	}
	
	
	public String runSeveralImagesOMERO(Collection<? extends ImageWrapper> images,
	                                    Long output,
	                                    Client client,
	                                    Long inputID)
	throws AccessException, ServiceException, ExecutionException, InterruptedException {
		ExecutorService downloadExecutor = Executors.newFixedThreadPool(DOWNLOADER_THREADS);
		ExecutorService processExecutor  = Executors.newFixedThreadPool(executorThreads);
		tID = inputID;
		
		OutputDatasets datasets = prepareOutputDatasetsOMERO(output, client);

		Map<Long, String> otsuResults       = new ConcurrentHashMap<>(images.size());
		Map<Long, String> convexHullResults = new ConcurrentHashMap<>(images.size());
		
		CountDownLatch latch       = new CountDownLatch(images.size());
		CountDownLatch uploadLatch = new CountDownLatch(1);

		class ImageProcessorOMERO implements Runnable {

			private final BatchImage batchImage;


			ImageProcessorOMERO(BatchImage batchImage) {
				this.batchImage = batchImage;
			}
			
			
			@Override
			public void run() {
				try {
					String fileImg = batchImage.getName();

					String start = currentDateTime();
					LOGGER.info("Current image in process: {} {} Start : {}", fileImg, lineSeparator(), start);
					NucleusSegmentation nucleusSegmentation = load(batchImage);
					compute(nucleusSegmentation);//////////////

					nucleusSegmentation.checkBadCrop(batchImage);
					batchImage.save(nucleusSegmentation, datasets);

					OMEROBatchImage obi = (OMEROBatchImage) batchImage;
					otsuResults.put(obi.getImage().getId(),
							nucleusSegmentation.getImageCropInfoOTSU()); // Put in thread safe collection
					convexHullResults.put(obi.getImage().getId(),
							nucleusSegmentation.getImageCropInfoConvexHull()); // Put in thread safe collection

					String end = currentDateTime();
					LOGGER.info("End: {} at {}", fileImg, end);

					latch.countDown();
				} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException | FormatException e) {
					LOGGER.error("Error processing image: {}", batchImage.getName(), e);
				}
			}
			
		}
		
		class ImageDownloaderOMERO implements Runnable {
			
			private final ImageWrapper img;
			
			
			ImageDownloaderOMERO(ImageWrapper img) {
				this.img = img;
			}
			
			
			@Override
			public void run() {
				try {
					LOGGER.info("Acquiring image");

					int[]     cBound = {0, 0}; // For each image

					BatchImage image = new OMEROBatchImage(img, client, null, null, cBound, null, null);
					image.loadImagePlus();

					processExecutor.submit(new ImageProcessorOMERO(image)); // Pass img to executor
					uploadLatch.countDown();
					LOGGER.info("Resource returned ({}).", img.getName());
				} catch (AccessException | ExecutionException | ServiceException e) {
					LOGGER.error("Error downloading image: {}", img.getName(), e);
				}
			}
			
		}
		
		for (ImageWrapper img : images) {
			if(!uploadLatch.await(1, TimeUnit.MINUTES)) {
				LOGGER.warn("Timeout while waiting for image download to start for: {}", img.getName());
			}
			downloadExecutor.submit(new ImageDownloaderOMERO(img));
		}
		
		latch.await();
		LOGGER.info("Finished processing");
		downloadExecutor.shutdownNow();
		processExecutor.shutdownNow();
		
		StringBuilder otsuInfoBuilder     = new StringBuilder();
		StringBuilder convHullInfoBuilder = new StringBuilder();
		for (ImageWrapper img : images) {
			/* create results file compatible with OMERO.Parade*/
			imgDatasetName = client.getDataset(inputID).getName();
			imgDatasetId = client.getDataset(inputID).getId();
			otsuInfoBuilder.append(img.getId()).append(",");
			otsuInfoBuilder.append(imgDatasetName).append(",");
			otsuInfoBuilder.append(otsuResults.get(img.getId()));
			convHullInfoBuilder.append(img.getId()).append(",");
			convHullInfoBuilder.append(imgDatasetName).append(",");
			convHullInfoBuilder.append(convexHullResults.get(img.getId()));
		}
		
		outputInfoParade += otsuInfoBuilder.toString();
		outputInfoParadeGraham += convHullInfoBuilder.toString();
		
		this.outputCropGeneralInfoOTSU += "#Dataset:" + imgDatasetId + lineSeparator() + getResultsColumnNames();
		
		outputCropGeneralInfoOTSU += otsuInfoBuilder.toString();
		this.outputCropGeneralInfoConvexHull += "#Dataset:" + imgDatasetId + lineSeparator() + getResultsColumnNames();
		outputCropGeneralInfoConvexHull += convHullInfoBuilder.toString();
		saveCropGeneralInfoOmero(client, output);
		return "";
	}
	
	
	public void saveCropGeneralInfoOmero(Client client, Long output)
	throws ServiceException, AccessException, ExecutionException, InterruptedException {
		String date = currentDateTime("yyyy-MM-dd_HH-mm-ss"); 
		LOGGER.info("Saving OTSU results.");
		DatasetWrapper dataset = client.getProject(output).getDatasets("OTSU").get(0);
		ProjectWrapper project = client.getProject(output);
		/* Get input dataset*/
		DatasetWrapper input = client.getDataset(tID);
		/* Create paths for the files (Otsu,Graham.Parade)*/
		String path = "." +
		              File.separator +
		              date +
		              "_" +
		              imgDatasetName +
		              "_" +
		              "result_Segmentation_OTSU.csv";
		String pathGraham = "." + File.separator +
		                    date + "_" +
		                    imgDatasetName + "_" +
		                    "result_Segmentation_GRAHAM.csv";
		String pathParade = "." +
		                    File.separator +
		                    date + "_" +
		                    imgDatasetName + "_" +
		                    "result_Segmentation_OTSU_parade.csv";
		String pathParadeGraham = "." + File.separator +
		                          date + "_" +
		                          imgDatasetName + "_" +
		                          "result_Segmentation_GRAHAM_parade.csv";
		try {
			path = new File(path).getCanonicalPath();
			pathParade = new File(pathParade).getCanonicalPath();
			pathParadeGraham = new File(pathParadeGraham).getCanonicalPath();
		} catch (IOException e) {
			LOGGER.error("Could not get canonical path for: {}", path, e);
		}
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(path);
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, false);
		/* Create results file for OMERO.Parade */
		OutputTextFile outputParade       = new OutputTextFile(pathParade);
		OutputTextFile outputParadeGraham = new OutputTextFile(pathParadeGraham);
		outputParade.saveTextFile(outputInfoParade, false);
		outputParadeGraham.saveTextFile(outputInfoParadeGraham, false);
		
		File file             = new File(path);
		File fileParade       = new File(pathParade);
		File fileParadeGraham = new File(pathParadeGraham);
		dataset.addFile(client, file);
		/* Put Otsu results file (for OMERO.Parade) in output Project and input dataset*/
		project.addFile(client, fileParade);
		input.addFile(client, fileParade);
		/* Put Graham results file (for OMERO.Parade) in output Project and input dataset*/
		project.addFile(client, fileParadeGraham);
		input.addFile(client, fileParadeGraham);
		
		try {
			Files.deleteIfExists(file.toPath());
			Files.deleteIfExists(fileParade.toPath());
			Files.deleteIfExists(fileParadeGraham.toPath());
		} catch (IOException e) {
			LOGGER.error("File not deleted: {}", path, e);
		}
		
		if (params.getConvexHullDetection()) {
			LOGGER.info("Saving Convex Hull algorithm results.");
			try {
				pathGraham = new File(pathGraham).getCanonicalPath();
			} catch (IOException e) {
				LOGGER.error("Could not get canonical path for: {}", pathGraham, e);
			}
			dataset = client.getProject(output).getDatasets(ConvexHullDetection.CONVEX_HULL_ALGORITHM).get(0);
			OutputTextFile outputConvexHull = new OutputTextFile(pathGraham);
			outputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, false);
			
			file = new File(pathGraham);
			dataset.addFile(client, file);
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				LOGGER.error("File not deleted: {}", pathGraham, e);
			}
		}
	}
	
	
	public String runOneImageOMERObyROIs(ImageWrapper image, Long output, Client client)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException, InterruptedException, FormatException {
		
		StringBuilder info = new StringBuilder();
		
		List<ROIWrapper> rois = image.getROIs(client);
		
		String log = "";
		
		String fileImg = image.getName();
		LOGGER.info("Current image in process: {}", fileImg);
		
		String start = currentDateTime();
		LOGGER.info("Start: {}", start);
		OutputDatasets datasets = prepareOutputDatasetsOMERO(output, client);
		int i = 0;
		
		for (ROIWrapper roi : rois) {
			LOGGER.info("Current ROI in process: {}", i);
			OMEROBatchImage source = new OMEROBatchImage(image, roi, i, params, client, null);
			NucleusSegmentation nucleusSegmentation = load(source);

			nucleusSegmentation.preProcessImage();
			nucleusSegmentation.findOTSUMaximisingSphericity();
			nucleusSegmentation.checkBadCrop(source);
			source.save(nucleusSegmentation, datasets);

			info.append(nucleusSegmentation.getImageCropInfoOTSU());
			info.append(nucleusSegmentation.getImageCropInfoConvexHull());
			i++;
		}
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		this.outputCropGeneralInfoOTSU += info.toString();
		
		String end = currentDateTime();
		LOGGER.info("End: {}", end);
		
		DatasetWrapper dataset = client.getProject(output).getDatasets("OTSU").get(0);
		String         path    = "." + File.separator + "result_Segmentation_Analyse.csv";
		try {
			path = new File(path).getCanonicalPath();
		} catch (IOException e) {
			LOGGER.error("Could not get canonical path for:{}", path, e);
		}
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(path);
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, false);
		
		File file = new File(path);
		dataset.addFile(client, file);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			LOGGER.error("File not deleted: {}", path, e);
		}
		
		if (params.getConvexHullDetection()) {
			dataset = client.getProject(output).getDatasets(ConvexHullDetection.CONVEX_HULL_ALGORITHM).get(0);
			OutputTextFile outputConvexHull = new OutputTextFile(path);
			outputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, false);
			
			file = new File(path);
			dataset.addFile(client, file);
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				LOGGER.error("File not deleted: {}", path, e);
			}
		}
		
		return log;
	}
	
	
	public String runSeveralImagesOMERObyROIs(Iterable<? extends ImageWrapper> images, Long output, Client client)
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, InterruptedException, FormatException {
		StringBuilder log = new StringBuilder();
		
		for (ImageWrapper image : images) {
			log.append(runOneImageOMERObyROIs(image, output, client));
		}
		
		return log.toString();
	}
	
}