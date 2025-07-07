package gred.nucleus.segmentation;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public final class SegmentationTestRunner {
	public static final String PATH_TO_INPUT = "input" + File.separator;
	public static final String PATH_TO_CONFIG = PATH_TO_INPUT +
	                                            "config" + File.separator +
	                                            "seg.config";
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/** Private constructor to prevent instantiation. */
	private SegmentationTestRunner() {
	}
	
	
	public static int getNumberOfImages(String dir) {
		int nImages = 0;
		File[] files = new File(dir + PATH_TO_INPUT).listFiles();
		if (files != null) {
			for (File file : files) {
				String extension = FilenameUtils.getExtension(file.getName())
				                                .toLowerCase(Locale.ROOT);
				if (file.isFile() && "tif".equals(extension)) {
					nImages++;
				}
			}
		}
		LOGGER.debug("{} image(s) found in segmentation folder.", nImages);
		return nImages;
	}
	
	
	public static void run(String segDir, String outDir) throws Exception {
		File file = new File(segDir + PATH_TO_INPUT);
		File[] files = file.listFiles();
		LOGGER.info("Running test on directory: {}", segDir + PATH_TO_INPUT);
		
		if (files != null) {
			for (File f : files) {
				String name = f.getName();
				if (f.isDirectory()) {
					LOGGER.info("Directory skipped: {}", name);
				} else {
					String extension = FilenameUtils.getExtension(name).toLowerCase(Locale.ROOT);
					if ("tif".equals(extension)) {
						LOGGER.info("Beginning process on: {}", name);
						runSegmentation(f.getPath(), outDir + name);
						LOGGER.info("Finished process on: {}", name);
						
						LOGGER.info("Checking results:");
						TimeUnit.SECONDS.sleep(3);
						SegmentationTestChecker checker = new SegmentationTestChecker(name);
						checker.checkValues(f);
					} else {
						LOGGER.info("File of type {} skipped", extension);
					}
				}
			}
		}
	}
	
	
	private static void runSegmentation(String imageSourceFile, String output) throws Exception {
		SegmentationParameters segmentationParams = new SegmentationParameters(imageSourceFile, output);
		SegmentationCalling segmentation = new SegmentationCalling(segmentationParams);
		segmentation.runOneImage(imageSourceFile);
		segmentation.saveTestCropGeneralInfo();
	}
	
	
}
