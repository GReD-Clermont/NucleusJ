package gred.nucleus.autocrop;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;


class AutoCropTest {
	public static final String PATH_TO_AUTOCROP = "test-images" + File.separator + "autocrop" + File.separator;
	public static final String PATH_TO_OUTPUT   = PATH_TO_AUTOCROP + "output" + File.separator;
	// Make sure the output folder is empty before running the test otherwise the checker might use the wrong files
	
	public static final String PATH_TO_INPUT = "input" + File.separator;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public static int getNumberOfImages(String dir) {
		int    nImages = 0;
		File[] files   = new File(dir + PATH_TO_INPUT).listFiles();
		if (files != null) {
			for (File file : files) {
				String extension = FilenameUtils.getExtension(file.getName())
				                                .toLowerCase(Locale.ROOT);
				if (file.isFile() && "tif".equals(extension)) {
					nImages++;
				}
			}
		}
		LOGGER.debug("{} image(s) found in autocrop folder.", nImages);
		return nImages;
	}
	
	
	private static void runAutoCrop(String imageSourceFile, String output) {
		AutocropParameters autocropParameters = new AutocropParameters(imageSourceFile, output);
		AutoCropCalling    autoCrop           = new AutoCropCalling(autocropParameters);
		autoCrop.runFile(imageSourceFile);
		autoCrop.saveGeneralInfo();
	}
	
	
	@Test
	@Tag("functional")
	void test() {
		int nImages = getNumberOfImages(PATH_TO_AUTOCROP);
		LOGGER.debug("Number of images: {}", nImages);
		assumeFalse(nImages == 0, "No images found in the autocrop folder, skipping test.");
		
		String dir = PATH_TO_AUTOCROP + PATH_TO_INPUT;
		
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
						runAutoCrop(f.toString(), PATH_TO_OUTPUT + name);
						LOGGER.debug("Finished process on: {}", name);
						LOGGER.debug("Checking results...");
						AutocropChecker checker = new AutocropChecker(name);
						assertTrue(checker.checkValues(f), "Autocrop test failed for file: " + name);
					} else {
						LOGGER.debug("File of type {} skipped: {}", extension, name);
					}
				}
			}
		}
	}
	
}
