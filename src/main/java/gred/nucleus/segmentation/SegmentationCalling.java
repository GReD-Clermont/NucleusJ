package gred.nucleus.segmentation;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.roi.ROIWrapper;
import gred.nucleus.core.ConvexHullSegmentation;
import gred.nucleus.core.NucleusSegmentation;
import gred.nucleus.files.Directory;
import gred.nucleus.files.FilesNames;
import gred.nucleus.files.OutputTextFile;
import gred.nucleus.nucleuscaracterisations.NucleusAnalysis;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.ContrastEnhancer;
import ij.plugin.GaussianBlur3D;
import ij.process.StackConverter;
import ij.process.StackStatistics;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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
	private final int downloaderThreads = 1;
	
	/** Prefix of the output files */
	private String prefix = "";
	
	/** ImagePlus raw image */
	private ImagePlus imgInput = new ImagePlus();
	
	/** ImagePlus segmented image */
	private ImagePlus imgSeg = new ImagePlus();
	
	/** String of the path for the output files */
	private String output;
	
	/** Input directory */
	private String inputDir = "";
	
	/** SegmentationParameters object containing the parameters for the segmentation */
	private SegmentationParameters segmentationParameters;
	
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
	
	
	public SegmentationCalling() {
	}
	
	
	/**
	 * Constructor for ImagePlus input
	 *
	 * @param segmentationParameters List of parameters in config file.
	 */
	public SegmentationCalling(SegmentationParameters segmentationParameters) {
		this.segmentationParameters = segmentationParameters;
		this.outputCropGeneralInfoOTSU = this.segmentationParameters.getAnalysisParameters();
		this.outputCropGeneralInfoConvexHull = this.segmentationParameters.getAnalysisParameters();
	}
	
	
	public SegmentationCalling(String inputDir, String outputDir) {
		this.inputDir = inputDir;
		this.output = outputDir;
		this.outputCropGeneralInfoOTSU = segmentationParameters.getAnalysisParameters();
		this.outputCropGeneralInfoConvexHull = segmentationParameters.getAnalysisParameters();
	}
	
	
	/**
	 * Constructor for ImagePlus input
	 *
	 * @param img       ImagePlus raw image
	 * @param vMin      volume min of the detected object
	 * @param vMax      volume max of the detected object
	 * @param outputImg String of of the path to save the img of the segmented nucleus.
	 */
	public SegmentationCalling(ImagePlus img, short vMin, int vMax, String outputImg) {
		segmentationParameters.setMinVolumeNucleus(vMin);
		segmentationParameters.setMaxVolumeNucleus(vMax);
		this.imgInput = img;
		this.output = outputImg + File.separator + "Segmented" + imgInput.getTitle();
	}
	
	
	/**
	 * Constructor for ImagePlus input
	 *
	 * @param img  ImagePlus raw image
	 * @param vMin volume min of the detected object
	 * @param vMax volume max of the detected object
	 */
	public SegmentationCalling(ImagePlus img, short vMin, int vMax) {
		segmentationParameters.setMinVolumeNucleus(vMin);
		segmentationParameters.setMaxVolumeNucleus(vMax);
		this.imgInput = img;
	}
	
	
	/**
	 * Constructor for directory input
	 *
	 * @param inputDir  String path of the input containing the tif/TIF file
	 * @param outputDir String of of the path to save results img of the segmented nucleus.
	 * @param vMin      volume min of the detected object
	 * @param vMax      volume max of the detected object
	 */
	public SegmentationCalling(String inputDir, String outputDir, short vMin, int vMax) {
		segmentationParameters.setMinVolumeNucleus(vMin);
		segmentationParameters.setMaxVolumeNucleus(vMax);
		this.inputDir = inputDir;
		this.output = outputDir;
		Directory dirOutput = new Directory(output);
		dirOutput.checkAndCreateDir();
		this.output = dirOutput.getDirPath();
	}
	
	
	/**
	 * Getter of the image segmented
	 *
	 * @return See above.
	 */
	public ImagePlus getImageSegmented() {
		return imgSeg;
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
	public String runSeveralImages2() throws IOException, FormatException {
		String              log                   = "";
		ExecutorService     processExecutor       = Executors.newFixedThreadPool(executorThreads);
		Map<String, String> otsuResultLines       = new ConcurrentHashMap<>();
		Map<String, String> convexHullResultLines = new ConcurrentHashMap<>();
		
		Directory directoryInput = new Directory(segmentationParameters.getInputFolder());
		directoryInput.listImageFiles(segmentationParameters.getInputFolder());
		directoryInput.checkIfEmpty();
		
		// Create output directories
		Path otsuDirectory = Paths.get(segmentationParameters.getOutputFolder() + File.separator + "OTSU");
		Path convexHullDirectory = Paths.get(segmentationParameters.getOutputFolder() + File.separator +
		                                     NucleusSegmentation.CONVEX_HULL_ALGORITHM);
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
		
		class ImageProcessor implements Runnable {
			
			private final File file;
			
			
			private ImageProcessor(File file) {
				this.file = file;
			}
			
			
			@Override
			public void run() {
				try {
					String     fileImg         = file.toString();
					FilesNames outputFilenames = new FilesNames(fileImg);
					String     prefix          = outputFilenames.prefixNameFile();
					
					String timeStampStart =
							new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
					LOGGER.info("Current image in process: {} \n Start : {}", fileImg, timeStampStart);
					NucleusSegmentation nucleusSegmentation =
							new NucleusSegmentation(file, prefix, segmentationParameters);
					
					nucleusSegmentation.preProcessImage();
					nucleusSegmentation.findOTSUMaximisingSphericity();
					nucleusSegmentation.checkBadCrop(segmentationParameters.inputFolder);
					nucleusSegmentation.saveOTSUSegmented();
					otsuResultLines.put(file.getName(),
					                    nucleusSegmentation.getImageCropInfoOTSU()); // Put in thread safe collection
					nucleusSegmentation.saveConvexHullSeg();
					convexHullResultLines.put(file.getName(),
					                          nucleusSegmentation.getImageCropInfoConvexHull()); // Put in thread safe collection
					
					timeStampStart =
							new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
					LOGGER.info("End: {} at {}", fileImg, timeStampStart);
					
					latch.countDown();
				} catch (IOException | FormatException e) {
					e.printStackTrace();
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
		
		StringBuilder otsuInfoBuilder       = new StringBuilder();
		StringBuilder convexHullInfoBuilder = new StringBuilder();
		for (File file : files) {
			otsuInfoBuilder.append(otsuResultLines.get(file.getName()));
			convexHullInfoBuilder.append(convexHullResultLines.get(file.getName()));
		}
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		outputCropGeneralInfoOTSU += otsuInfoBuilder.toString();
		this.outputCropGeneralInfoConvexHull += getResultsColumnNames();
		outputCropGeneralInfoConvexHull += convexHullInfoBuilder.toString();
		
		saveCropGeneralInfo();
		
		return log;
	}
	
	
	public String runOneImage(String filePath) throws IOException, FormatException {
		
		String     log              = "";
		File       currentFile      = new File(filePath);
		String     fileImg          = currentFile.toString();
		FilesNames outPutFilesNames = new FilesNames(fileImg);
		this.prefix = outPutFilesNames.prefixNameFile();
		LOGGER.info("Current image in process: {}", currentFile);
		
		String timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("Start: {}", timeStampStart);
		NucleusSegmentation nucleusSegmentation =
				new NucleusSegmentation(currentFile, prefix, segmentationParameters);
		nucleusSegmentation.preProcessImage();
		nucleusSegmentation.findOTSUMaximisingSphericity();
		nucleusSegmentation.checkBadCrop(segmentationParameters.inputFolder);
		nucleusSegmentation.saveOTSUSegmented();
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		this.outputCropGeneralInfoOTSU += nucleusSegmentation.getImageCropInfoOTSU();
		nucleusSegmentation.saveConvexHullSeg();
		this.outputCropGeneralInfoConvexHull += getResultsColumnNames();
		this.outputCropGeneralInfoConvexHull += nucleusSegmentation.getImageCropInfoConvexHull();
		
		timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("End: {}", timeStampStart);
		return log;
	}
	
	
	public void saveCropGeneralInfo() {
		Date             date       = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		LOGGER.info("Saving crop general info.");
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(segmentationParameters.getOutputFolder()
		                                                         + "OTSU"
		                                                         + File.separator
		                                                         + dateFormat.format(date)
		                                                         + "-result_Segmentation_Analyse_OTSU.csv");
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, true);
		if (segmentationParameters.getConvexHullDetection()) {
			OutputTextFile resultFileOutputConvexHull = new OutputTextFile(segmentationParameters.getOutputFolder()
			                                                               + NucleusSegmentation.CONVEX_HULL_ALGORITHM
			                                                               + File.separator
			                                                               + dateFormat.format(date)
			                                                               + "-result_Segmentation_Analyse_" +
			                                                               NucleusSegmentation.CONVEX_HULL_ALGORITHM +
			                                                               ".csv");
			resultFileOutputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, true);
		}
	}
	
	
	public void saveTestCropGeneralInfo() {
		LOGGER.info("Saving crop general info.");
		OutputTextFile resultFileOutputOTSU = new OutputTextFile(segmentationParameters.getOutputFolder()
		                                                         + "OTSU"
		                                                         + File.separator
		                                                         + "result_Segmentation_Analyse_OTSU.csv");
		resultFileOutputOTSU.saveTextFile(outputCropGeneralInfoOTSU, true);
		if (segmentationParameters.getConvexHullDetection()) {
			OutputTextFile resultFileOutputConvexHull = new OutputTextFile(segmentationParameters.getOutputFolder()
			                                                               + NucleusSegmentation.CONVEX_HULL_ALGORITHM
			                                                               + File.separator
			                                                               + "result_Segmentation_Analyse_" +
			                                                               NucleusSegmentation.CONVEX_HULL_ALGORITHM +
			                                                               ".csv");
			resultFileOutputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, true);
		}
	}
	
	
	public String runOneImageOMERO(ImageWrapper image, Long output, Client client)
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException {
		String log = "";
		
		ProjectWrapper project = client.getProject(output);
		// Get OTSU dataset ID
		List<DatasetWrapper> datasets                       = project.getDatasets("OTSU");
		long                 otsuDataset, convexHullDataset = -1;
		if (datasets.isEmpty()) {
			otsuDataset = project.addDataset(client, "OTSU", "").getId();
		} else {
			otsuDataset = datasets.get(0).getId();
		}
		project = client.getProject(output);
		// Get Convex Hull dataset ID
		if (segmentationParameters.getConvexHullDetection()) {
			datasets = project.getDatasets(NucleusSegmentation.CONVEX_HULL_ALGORITHM);
			if (datasets.isEmpty()) {
				convexHullDataset = project.addDataset(client, NucleusSegmentation.CONVEX_HULL_ALGORITHM, "").getId();
			} else {
				convexHullDataset = datasets.get(0).getId();
			}
		}
		
		String fileImg = image.getName();
		LOGGER.info("Current image in process: {}", fileImg);
		
		String timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("Start: {}", timeStampStart);
		NucleusSegmentation nucleusSegmentation = new NucleusSegmentation(image, segmentationParameters, client);
		nucleusSegmentation.preProcessImage();
		nucleusSegmentation.findOTSUMaximisingSphericity();
		nucleusSegmentation.checkBadCrop(image, client);
		
		nucleusSegmentation.saveOTSUSegmentedOMERO(client, otsuDataset);
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		this.outputCropGeneralInfoOTSU += nucleusSegmentation.getImageCropInfoOTSU();
		nucleusSegmentation.saveConvexHullSegOMERO(client, convexHullDataset);
		this.outputCropGeneralInfoConvexHull += getResultsColumnNames();
		this.outputCropGeneralInfoConvexHull += nucleusSegmentation.getImageCropInfoConvexHull();
		
		timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("End: {}", timeStampStart);
		
		return log;
	}
	
	
	public String runSeveralImagesOMERO(Collection<ImageWrapper> images, Long output, Client client, Long inputID)
	throws AccessException, ServiceException, ExecutionException, InterruptedException {
		ExecutorService   downloadExecutor      = Executors.newFixedThreadPool(downloaderThreads);
		ExecutorService   processExecutor       = Executors.newFixedThreadPool(executorThreads);
		Map<Long, String> otsuResultLines       = new ConcurrentHashMap<>();
		Map<Long, String> convexHullResultLines = new ConcurrentHashMap<>();
		tID = inputID;
		
		ProjectWrapper project = client.getProject(output);
		// Get OTSU dataset ID
		List<DatasetWrapper> datasets = project.getDatasets("OTSU");
		long                 otsuDataset, convexHullDataset;
		if (datasets.isEmpty()) {
			otsuDataset = project.addDataset(client, "OTSU", "").getId();
		} else {
			otsuDataset = datasets.get(0).getId();
		}
		project = client.getProject(output);
		// Get Convex Hull dataset ID
		if (segmentationParameters.getConvexHullDetection()) {
			datasets = project.getDatasets(NucleusSegmentation.CONVEX_HULL_ALGORITHM);
			if (datasets.isEmpty()) {
				convexHullDataset = project.addDataset(client, NucleusSegmentation.CONVEX_HULL_ALGORITHM, "").getId();
			} else {
				convexHullDataset = datasets.get(0).getId();
			}
		} else {
			convexHullDataset = -1;
		}
		
		CountDownLatch latch       = new CountDownLatch(images.size());
		CountDownLatch uploadLatch = new CountDownLatch(1);
		class ImageProcessorOMERO implements Runnable {
			
			private final ImageWrapper img;
			private final ImagePlus    imp;
			
			
			ImageProcessorOMERO(ImageWrapper img, ImagePlus imp) {
				this.img = img;
				this.imp = imp;
			}
			
			
			@Override
			public void run() {
				try {
					String fileImg = img.getName();
					String timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
					LOGGER.info("Current image in process: {} \n Start : {}", fileImg, timeStampStart);
					NucleusSegmentation nucleusSegmentation =
							new NucleusSegmentation(img, imp, segmentationParameters, client);
					nucleusSegmentation.preProcessImage();
					nucleusSegmentation.findOTSUMaximisingSphericity();
					nucleusSegmentation.checkBadCrop(img, client);
					
					nucleusSegmentation.saveOTSUSegmentedOMERO(client, otsuDataset); // Upload
					otsuResultLines.put(img.getId(),
					                    nucleusSegmentation.getImageCropInfoOTSU()); // Put in thread safe collection
					nucleusSegmentation.saveConvexHullSegOMERO(client, convexHullDataset); // Upload
					convexHullResultLines.put(img.getId(),
					                          nucleusSegmentation.getImageCropInfoConvexHull()); // Put in thread safe collection
					
					timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
					LOGGER.info("End: {} at {}", fileImg, timeStampStart);
					
					latch.countDown();
				} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
					LOGGER.error("Error processing image: {}", img.getName(), e);
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
					ImagePlus imp    = img.toImagePlus(client, null, null, cBound, null, null); // Download image
					
					processExecutor.submit(new ImageProcessorOMERO(img, imp)); // Pass img to executor
					uploadLatch.countDown();
					LOGGER.info("Resource returned ({}).", img.getName());
				} catch (AccessException | ExecutionException | ServiceException e) {
					LOGGER.error("Error downloading image: {}", img.getName(), e);
				}
			}
			
		}
		
		for (ImageWrapper img : images) {
			uploadLatch.await(3, TimeUnit.SECONDS);
			downloadExecutor.submit(new ImageDownloaderOMERO(img));
		}
		
		latch.await();
		LOGGER.info("Finished processing");
		downloadExecutor.shutdownNow();
		processExecutor.shutdownNow();
		
		StringBuilder otsuInfoBuilder       = new StringBuilder();
		StringBuilder convexHullInfoBuilder = new StringBuilder();
		for (ImageWrapper img : images) {
			/* create results file compatible with OMERO.Parade*/
			imgDatasetName = client.getDataset(inputID).getName();
			imgDatasetId = client.getDataset(inputID).getId();
			otsuInfoBuilder.append(img.getId() + ",");
			otsuInfoBuilder.append(imgDatasetName + ",");
			otsuInfoBuilder.append(otsuResultLines.get(img.getId()));
			convexHullInfoBuilder.append(img.getId() + ",");
			convexHullInfoBuilder.append(imgDatasetName + ",");
			convexHullInfoBuilder.append(convexHullResultLines.get(img.getId()));
		}
		
		outputInfoParade += otsuInfoBuilder.toString();
		outputInfoParadeGraham += convexHullInfoBuilder.toString();
		
		this.outputCropGeneralInfoOTSU += "#Dataset:" + imgDatasetId + "\n" + getResultsColumnNames();
		
		outputCropGeneralInfoOTSU += otsuInfoBuilder.toString();
		this.outputCropGeneralInfoConvexHull += "#Dataset:" + imgDatasetId + "\n" + getResultsColumnNames();
		outputCropGeneralInfoConvexHull += convexHullInfoBuilder.toString();
		saveCropGeneralInfoOmero(client, output);
		return "";
	}
	
	
	public void saveCropGeneralInfoOmero(Client client, Long output)
	throws ServiceException, AccessException, ExecutionException, InterruptedException {
		Date             date       = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		LOGGER.info("Saving OTSU results.");
		DatasetWrapper dataset = client.getProject(output).getDatasets("OTSU").get(0);
		ProjectWrapper project = client.getProject(output);
		/* Get input dataset*/
		DatasetWrapper input = client.getDataset(tID);
		/* Create paths for the files (Otsu,Graham.Parade)*/
		String path = "." +
		              File.separator +
		              dateFormat.format(date) +
		              "_" +
		              imgDatasetName +
		              "_" +
		              "result_Segmentation_OTSU.csv";
		String pathGraham = "." + File.separator +
		                    dateFormat.format(date) + "_" +
		                    imgDatasetName + "_" +
		                    "result_Segmentation_GRAHAM.csv";
		String pathParade = "." +
		                    File.separator +
		                    dateFormat.format(date) + "_" +
		                    imgDatasetName + "_" +
		                    "result_Segmentation_OTSU_parade.csv";
		String pathParadeGraham = "." + File.separator +
		                          dateFormat.format(date) + "_" +
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
		OutputTextFile resultFileOutputParade       = new OutputTextFile(pathParade);
		OutputTextFile resultFileOutputParadeGraham = new OutputTextFile(pathParadeGraham);
		resultFileOutputParade.saveTextFile(outputInfoParade, false);
		resultFileOutputParadeGraham.saveTextFile(outputInfoParadeGraham, false);
		
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
		
		if (segmentationParameters.getConvexHullDetection()) {
			LOGGER.info("Saving Convex Hull algorithm results.");
			try {
				pathGraham = new File(pathGraham).getCanonicalPath();
				
			} catch (IOException e) {
				LOGGER.error("Could not get canonical path for: {}", pathGraham, e);
			}
			dataset = client.getProject(output).getDatasets(NucleusSegmentation.CONVEX_HULL_ALGORITHM).get(0);
			OutputTextFile resultFileOutputConvexHull = new OutputTextFile(pathGraham);
			resultFileOutputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, false);
			
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
	throws AccessException, ServiceException, ExecutionException, OMEROServerError, IOException, InterruptedException {
		
		StringBuilder info = new StringBuilder();
		
		List<ROIWrapper> rois = image.getROIs(client);
		
		String log = "";
		
		String fileImg = image.getName();
		LOGGER.info("Current image in process: {}", fileImg);
		
		String timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("Start: {}", timeStampStart);
		
		int i = 0;
		
		for (ROIWrapper roi : rois) {
			LOGGER.info("Current ROI in process: {}", i);
			
			NucleusSegmentation nucleusSegmentation = new NucleusSegmentation(image,
			                                                                  roi,
			                                                                  i,
			                                                                  segmentationParameters,
			                                                                  client);
			nucleusSegmentation.preProcessImage();
			nucleusSegmentation.findOTSUMaximisingSphericity();
			nucleusSegmentation.checkBadCrop(roi, client);
			
			nucleusSegmentation.saveOTSUSegmentedOMERO(client, output);
			info.append(nucleusSegmentation.getImageCropInfoOTSU());
			
			nucleusSegmentation.saveConvexHullSegOMERO(client, output);
			info.append(nucleusSegmentation.getImageCropInfoConvexHull());
			
			i++;
		}
		this.outputCropGeneralInfoOTSU += getResultsColumnNames();
		this.outputCropGeneralInfoOTSU += info.toString();
		
		timeStampStart = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(Calendar.getInstance().getTime());
		LOGGER.info("End: {}", timeStampStart);
		
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
		
		if (segmentationParameters.getConvexHullDetection()) {
			dataset = client.getProject(output).getDatasets(NucleusSegmentation.CONVEX_HULL_ALGORITHM).get(0);
			OutputTextFile resultFileOutputConvexHull = new OutputTextFile(path);
			resultFileOutputConvexHull.saveTextFile(outputCropGeneralInfoConvexHull, false);
			
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
	
	
	public String runSeveralImagesOMERObyROIs(Iterable<ImageWrapper> images, Long output, Client client)
	throws AccessException, ServiceException, OMEROServerError, IOException, ExecutionException, InterruptedException {
		StringBuilder log = new StringBuilder();
		
		for (ImageWrapper image : images) {
			log.append(runOneImageOMERObyROIs(image, output, client));
		}
		
		return log.toString();
	}
	
	
	/**
	 * Method which save the image in the directory.
	 *
	 * @param imagePlusInput Image to be save
	 * @param pathFile       path of directory
	 */
	private void saveFile(ImagePlus imagePlusInput, String pathFile) {
		FileSaver fileSaver = new FileSaver(imagePlusInput);
		fileSaver.saveAsTiffStack(pathFile);
	}
	
	
	/**
	 * 16bits image preprocessing normalised the histogram distribution apply a gaussian filter to smooth the signal
	 * convert the image in 8bits
	 *
	 * @param img 16bits ImagePlus
	 */
	//TODO A ENLEVER APRES RESTRUCTURATION ATTENTION INTEGRATION DANS LES FENETRES GRAPHIQUES PAS ENCORE UPDATE DC CA CRASH!!!!!
	private void preProcessImage(ImagePlus img) {
		ContrastEnhancer enh = new ContrastEnhancer();
		enh.setNormalize(true);
		enh.setUseStackHistogram(true);
		enh.setProcessStack(true);
		enh.stretchHistogram(img, 0.05);
		StackStatistics statistics = new StackStatistics(img);
		img.setDisplayRange(statistics.min, statistics.max);
		
		GaussianBlur3D.blur(img, 0.5, 0.5, 1);
		StackConverter stackConverter = new StackConverter(img);
		stackConverter.convertToGray8();
	}
	
	
	public String getResultsColumnNames() {
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
		       "OTSUThreshold," + "\n";
	}
	
}