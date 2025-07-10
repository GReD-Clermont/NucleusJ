package gred.nucleus.gradient;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.LUT;
import imagescience.ImageScience;
import imagescience.image.Image;
import imagescience.utility.I5DResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


public final class FJ {
	static final int SINGLE_IMAGE    = 1;
	static final int IMAGE_STACK     = 2;
	static final int HYPERSTACK      = 3;
	static final int COMPOSITE_IMAGE = 4;
	static final int IMAGE5D         = 5;
	
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String NAME           = "FeatureJ";
	private static final String VERSION        = "1.6.0";
	private static final String MIN_IJ_VERSION = "1.44a";
	private static final String MIN_IS_VERSION = "2.4.0";
	
	
	public static String name() {
		return NAME;
	}
	
	
	public static String version() {
		return VERSION;
	}
	
	
	static boolean libCheck() {
		
		if (IJ.getVersion().compareTo(MIN_IJ_VERSION) < 0) {
			error("This plugin requires ImageJ version " + MIN_IJ_VERSION + " or higher");
			return false;
		}
		
		try {
			if (ImageScience.version().compareTo(MIN_IS_VERSION) < 0) {
				throw new IllegalStateException();
			}
		} catch (IllegalStateException e) {
			error("This plugin requires ImageScience version " + MIN_IS_VERSION + " or higher");
			return false;
		}
		return true;
	}
	
	
	static ImagePlus imageplus() {
		
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			error("There are no images open");
			return null;
		}
		
		int type = imp.getType();
		if (type != ImagePlus.GRAY8 && type != ImagePlus.GRAY16 && type != ImagePlus.GRAY32) {
			error("The image is not a gray-scale image");
			return null;
		}
		
		return imp;
	}
	
	
	static void show(Image img, ImagePlus imp) {
		
		ImagePlus newImagePlus = img.imageplus();
		newImagePlus.setCalibration(imp.getCalibration());
		double[] minMax = img.extrema();
		double   min    = minMax[0];
		double   max    = minMax[1];
		newImagePlus.setDisplayRange(min, max);
		
		switch (type(imp)) {
			
			case IMAGE5D: {
				newImagePlus = I5DResource.convert(newImagePlus, true);
				I5DResource.transfer(imp, newImagePlus);
				I5DResource.minmax(newImagePlus, min, max);
				I5DResource.mode(newImagePlus, I5DResource.GRAY);
				break;
			}
			case COMPOSITE_IMAGE: {
				CompositeImage newCompositeImage = new CompositeImage(newImagePlus);
				newCompositeImage.copyLuts(imp);
				newCompositeImage.setMode(CompositeImage.GRAYSCALE);
				int nc = newCompositeImage.getNChannels();
				for (int c = 1; c <= nc; ++c) {
					LUT lut = newCompositeImage.getChannelLut(c);
					lut.min = min;
					lut.max = max;
				}
				newImagePlus = newCompositeImage;
				break;
			}
			case HYPERSTACK: {
				newImagePlus.setOpenAsHyperStack(true);
				break;
			}
		}
		
		newImagePlus.changes = FJ_Options.save;
		
		log("Showing result image");
		newImagePlus.show();
	}
	
	
	static void close(ImagePlus imp) {
		
		if (FJ_Options.close) {
			log("Closing input image");
			imp.close();
		}
	}
	
	
	static int type(ImagePlus imp) {
		
		int     type     = SINGLE_IMAGE;
		boolean i5dExist = false;
		try {
			Class.forName("i5d.Image5D");
			i5dExist = true;
		} catch (ClassNotFoundException e) {
			LOGGER.error("An error occurred.", e);
		}
		if (i5dExist && I5DResource.instance(imp)) {
			type = IMAGE5D;
		} else if (imp.isComposite()) {
			type = COMPOSITE_IMAGE;
		} else if (imp.isHyperStack()) {
			type = HYPERSTACK;
		} else if (imp.getImageStackSize() > 1) {
			type = IMAGE_STACK;
		}
		return type;
	}
	
	
	static void error(String message) {
		IJ.showMessage(NAME + ": Error", message + ".");
		IJ.showProgress(1);
		IJ.showStatus("");
	}
	
	
	static void log(String message) {
		if (FJ_Options.log) {
			IJ.log(message);
		}
	}
	
}
