package gred.nucleus.segmentation;

import loci.formats.FormatException;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;


class SegmentationTest {
	public static final String PATH_TO_SEGMENTATION = "test-images" + File.separator + "segmentation" + File.separator;
	public static final String PATH_TO_OUTPUT       = PATH_TO_SEGMENTATION + "output" + File.separator;
	
	public static final String PATH_TO_INPUT  = "input" + File.separator;
	public static final String PATH_TO_CONFIG = PATH_TO_INPUT +
	                                            "config" + File.separator +
	                                            "seg.config";
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public static int getNumberOfImages(String dir) {
		int    nImages = 0;
		File[] files   = new File(dir).listFiles();
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
	
	
	private static void runSegmentation(String imageSourceFile, String output)
	throws IOException, FormatException {
		SegmentationParameters segmentationParams = new SegmentationParameters(imageSourceFile, output);
		SegmentationCalling    segmentation       = new SegmentationCalling(segmentationParams);
		segmentation.runOneImage(imageSourceFile);
		segmentation.saveTestCropGeneralInfo();
	}
	
	
	@Test
	@Tag("functional")
	void test() throws Exception {
		int nImages = getNumberOfImages(PATH_TO_SEGMENTATION + PATH_TO_INPUT);
		LOGGER.debug("Number of images: {}", nImages);
		assumeFalse(nImages == 0, "No images found in segmentation folder, skipping test.");
		
		String dir = PATH_TO_SEGMENTATION + PATH_TO_INPUT;
		
		File   file  = new File(dir);
		File[] files = file.listFiles();
		LOGGER.info("Running test on directory: {}", dir);
		
		if (files != null) {
			for (File f : files) {
				String name = f.getName();
				if (f.isDirectory()) {
					LOGGER.info("Directory skipped: {}", name);
				} else {
					String extension = FilenameUtils.getExtension(name).toLowerCase(Locale.ROOT);
					if ("tif".equals(extension)) {
						LOGGER.debug("Beginning process on: {}", name);
						runSegmentation(f.getPath(), PATH_TO_OUTPUT + name);
						LOGGER.debug("Finished process on: {}", name);
						LOGGER.debug("Checking results...");
						SegmentationChecker checker = new SegmentationChecker(name);
						assertTrue(checker.checkValues(f), "Too many differences in segmentation results for " + name);
					} else {
						LOGGER.debug("File of type {} skipped", extension);
					}
				}
			}
		}
	}
	
}