package gred.nucleus.autocrop;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assumptions.assumeFalse;


class OldAutoCropTest {
	public static final String PATH_TO_AUTOCROP = "test-images" + File.separator + "autocrop" + File.separator;
	public static final String PATH_TO_OUTPUT   = PATH_TO_AUTOCROP + "output" + File.separator;
	// Make sure the output folder is empty before running the test otherwise the checker might use the wrong files
	
	
	@Test
	@Tag("functional")
	void test() {
		assumeFalse(AutocropTestRunner.getNumberOfImages(PATH_TO_AUTOCROP) == 0);
		AutocropTestRunner.run(PATH_TO_AUTOCROP);
	}
	
}
