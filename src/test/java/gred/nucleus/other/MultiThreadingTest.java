package gred.nucleus.other;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import gred.nucleus.autocrop.AutoCropCalling;
import gred.nucleus.autocrop.AutocropParameters;
import gred.nucleus.segmentation.SegmentationCalling;
import gred.nucleus.segmentation.SegmentationParameters;
import ij.ImagePlus;
import ij.io.FileSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Class used to test image processing (local/Omero autocrop and segmentation) using multithreading.
 */
public class MultiThreadingTest {
	public static final long OUTPUT_PROJECT_ID = 10905L;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	// Omero segmentation
	private static final Client client = new Client();
	
	// Thread methods tests attributes :
	private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(1);
	private static final ExecutorService tasksExecutor    = Executors.newFixedThreadPool(2);
	private static       CountDownLatch  latch;
	
	private static ConcurrentHashMap<Integer, String> result;
	
	
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime();
		localSegmentation(8);
		long duration = System.nanoTime() - start;
		LOGGER.info("Duration = {}", TimeUnit.NANOSECONDS.toSeconds(duration));
	}
	
	
	static void downloadAllFromOtherDataset() throws AccessException, ServiceException, ExecutionException {
		checkOMEROConnection("".toCharArray());
		DatasetWrapper pbGrahamDat = client.getDataset(20965L);
		DatasetWrapper rawDat      = client.getDataset(20934L);
		
		for (ImageWrapper img : pbGrahamDat.getImages(client)) {
			ImageWrapper rawImg = rawDat.getImages(client, img.getName()).get(0);
			ImagePlus    imp    = rawImg.toImagePlus(client);
			saveFile(imp, "E:\\alexw\\Desktop\\testinput\\" + imp.getTitle());
		}
		
	}
	
	
	static void threadsStatistics(int threads) throws Exception {
		File directory = new File("/data/home/rongiera/input");
		System.out.println("--TESTING FOR " + threads + " THREADS & " + directory.listFiles().length + " NUCLEI--");
		
		FileWriter csvWriter = new FileWriter("stats.csv", true);
		csvWriter.append("-;")
		         .append(String.valueOf(threads))
		         .append(" threads\n")
		         .append(String.valueOf(directory.listFiles().length))
		         .append(" nuclei;");
		
		long start = System.nanoTime();
		localSegmentation(threads);
		long duration = System.nanoTime() - start;
		long time     = TimeUnit.NANOSECONDS.toSeconds(duration);
		LOGGER.info(" > Duration = {}", time);
		csvWriter.append(String.valueOf(time)).append("\n");
		
		csvWriter.flush();
		csvWriter.close();
	}
	
	
	static void localAutocrop(int executorThreads) {
		AutocropParameters autocropParameters = new AutocropParameters(
				"E:\\alexw\\Desktop\\GReD\\testinput",
				"E:\\alexw\\Desktop\\GReD\\testoutput"
				//"/mnt/e/alexw/Desktop/GReD/testinput",
				//"/mnt/e/alexw/Desktop/GReD/testoutput"
				//"/data/home/rongiera/input",
				//"/data/home/rongiera/output"
		);
		AutoCropCalling autoCrop = new AutoCropCalling(autocropParameters);
		autoCrop.setExecutorThreads(executorThreads); // Set thread number
		autoCrop.runFolder();
	}
	
	
	static void localSegmentation(int executorThreads) throws Exception {
		SegmentationParameters parameters = new SegmentationParameters(
				//"E:\\alexw\\Desktop\\GReD\\testinput",
				//"E:\\alexw\\Desktop\\GReD\\testouput"
				"E:\\alexw\\Desktop\\testinput",
				"E:\\alexw\\Desktop\\out2"
		);
		SegmentationCalling segmentation = new SegmentationCalling(parameters);
		segmentation.setExecutorThreads(executorThreads); // Set thread number
		segmentation.runSeveralImages2();
	}
	
	
	static void omeroSegmentation(int executorThreads) throws Exception {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Password for \"demo\" :");
		String password = scanner.nextLine();
		checkOMEROConnection(password.toCharArray());
		
		SegmentationParameters parameters   = new SegmentationParameters(".", ".");
		SegmentationCalling    segmentation = new SegmentationCalling(parameters);
		segmentation.setExecutorThreads(executorThreads); // Set thread number
		
		long sourceDataset = 20375;
		
		DatasetWrapper     dataset = client.getDataset(sourceDataset);
		List<ImageWrapper> images  = dataset.getImages(client);
		
		segmentation.runSeveralImagesOMERO(images, OUTPUT_PROJECT_ID, client, sourceDataset);
	}
	
	
	static void checkOMEROConnection(char[] password) {
		try {
			client.connect("omero.igred.fr",
			               4064,
			               "",
			               password,
			               203L);
		} catch (Exception exp) {
			LOGGER.error("OMERO connection error: " + exp.getMessage(), exp);
			System.exit(1);
		}
	}
	
	
	public static void saveFile(ImagePlus imagePlusInput, String pathFile) {
		FileSaver fileSaver = new FileSaver(imagePlusInput);
		fileSaver.saveAsTiff(pathFile);
	}
	
	
	/**
	 * Class used to understand the mechanism involved in the multi-threading optimization for processing images from
	 * Omero as well as locally
	 */
	static void testThreadPoolDownloadAndTask() throws InterruptedException {
		int n = 10; // Depending on the id list retrieved
		result = new ConcurrentHashMap<>();
		latch = new CountDownLatch(n);
		for (int i = 0; i < n; i++) {
			downloadExecutor.submit(new MyDownloader(i));
		}
		latch.await();
		LOGGER.info("Finished execution");
		downloadExecutor.shutdownNow();
		tasksExecutor.shutdownNow();
		
		LOGGER.info("Result (sorted by id):");
		SortedSet<Integer> keys = new TreeSet<>(result.keySet());
		for (int key : keys) {
			String value = result.get(key);
			System.out.println(value);
		}
	}
	
	
	static class MyDownloader implements Runnable {
		/** Logger */
		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		
		private int id;
		
		
		public MyDownloader(int id) {
			this.id = id;
		}
		
		
		@Override
		public void run() {
			try {
				long duration = (long) (Math.random() * 10);
				LOGGER.info("Acquiring for {}s ({})...", duration, id);
				TimeUnit.SECONDS.sleep(duration); // Image img = client.getImageByID(id);
				tasksExecutor.submit(new MyTask(duration, id)); // Pass img to executor
				LOGGER.info("Resource returned ({}).", id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	static class MyTask implements Runnable {
		/** Logger */
		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		
		private long value;
		private int  id;
		
		
		public MyTask(long value, int id) {
			this.value = value;
			this.id = id;
		}
		
		
		@Override
		public void run() {
			long duration = (long) (Math.random() * 10);
			try {
				LOGGER.info("Executing for {}s ({})...", duration, id);
				TimeUnit.SECONDS.sleep(duration); // Process image
				result.put(id, "[" + id + "]->" + duration); // Append the result line in CSV (OTSU + CONVEXHULL)
				LOGGER.info("Ended ({}).", id);
				latch.countDown();
			} catch (InterruptedException e) {
				LOGGER.error("An error occurred during task execution.", e);
			}
		}
		
	}
	
}
