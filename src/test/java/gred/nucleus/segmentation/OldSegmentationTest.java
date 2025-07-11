package gred.nucleus.segmentation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assumptions.assumeFalse;


class OldSegmentationTest {
	public static final String PATH_TO_SEGMENTATION = "test-images" + File.separator + "segmentation" + File.separator;
	public static final String PATH_TO_OUTPUT       = PATH_TO_SEGMENTATION + "output" + File.separator;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	@Test
	@Tag("functional")
	void test() throws Exception {
		int nImages = SegmentationTest.getNumberOfImages(PATH_TO_SEGMENTATION);
		LOGGER.info("Number of images: {}", nImages);
		assumeFalse(nImages == 0);
		SegmentationTest.run(PATH_TO_SEGMENTATION, PATH_TO_OUTPUT);
	}
	
}