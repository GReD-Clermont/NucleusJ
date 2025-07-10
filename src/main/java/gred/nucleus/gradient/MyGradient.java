package gred.nucleus.gradient;

import ij.ImagePlus;
import ij.Prefs;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import imagescience.utility.Progressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


/**
 * Modification of plugin featureJ to integrate of this work,
 * <p>
 * => Use to imagescience.jar library
 *
 * @author poulet axel
 */
public class MyGradient {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final boolean ISOTROPIC = Prefs.get("fj.isotropic", false);
	private static final boolean PGS       = Prefs.get("fj.pgs", true);
	private static final boolean LOG       = Prefs.get("fj.log", false);
	
	private static final boolean COMPUTE  = true;
	private static final boolean SUPPRESS = false;
	private static final String  SCALE    = "1.0";
	private static final String  LOWER    = "";
	private static final String  HIGHER   = "";
	private final        boolean mask;
	
	private final ImagePlus imagePlus;
	
	private ImagePlus imagePlusBinary;
	
	
	public MyGradient(ImagePlus imp, ImagePlus imagePlusBinary) {
		imagePlus = imp;
		this.imagePlusBinary = imagePlusBinary;
		mask = true;
	}
	
	
	public MyGradient(ImagePlus imp) {
		imagePlus = imp;
		mask = false;
	}
	
	
	/**
	 * Run the gradient computation.
	 *
	 * @return a new ImagePlus with the gradient image
	 */
	public ImagePlus run() {
		ImagePlus newImagePlus = new ImagePlus();
		try {
			double  scaleVal;
			boolean lowThreshold  = true;
			boolean highThreshold = true;
			try {
				scaleVal = Double.parseDouble(SCALE);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid smoothing scale value");
			}
			try {
				if (LOWER.isEmpty()) {
					lowThreshold = false;
				} else {
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid lower threshold value");
			}
			try {
				if (HIGHER.isEmpty()) {
					highThreshold = false;
				} else {
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid higher threshold value");
			}
			int      threshMode = (lowThreshold ? 10 : 0) + (highThreshold ? 1 : 0);
			Image    image      = Image.wrap(imagePlus);
			Image    newImage   = new FloatImage(image);
			double[] pls        = {0, 1};
			int      pl         = 0;
			if ((COMPUTE || SUPPRESS) && threshMode > 0) {
				pls = new double[]{0, 0.9, 1};
			}
			Progressor progressor = new Progressor();
			progressor.display(PGS);
			if (COMPUTE || SUPPRESS) {
				Aspects aspects = newImage.aspects();
				if (!ISOTROPIC) {
					newImage.aspects(new Aspects());
				}
				MyEdges myEdges = new MyEdges();
				if (mask) {
					myEdges.setMask(imagePlusBinary);
				}
				++pl;
				progressor.range(pls[pl], pls[pl]);
				myEdges.progressor.parent(progressor);
				myEdges.messenger.log(LOG);
				myEdges.messenger.status(PGS);
				newImage = myEdges.run(newImage, scaleVal, SUPPRESS);
				newImage.aspects(aspects);
			}
			newImagePlus = newImage.imageplus();
			imagePlus.setCalibration(newImagePlus.getCalibration());
			double[] minMax = newImage.extrema();
			double   min    = minMax[0];
			double   max    = minMax[1];
			newImagePlus.setDisplayRange(min, max);
		} catch (OutOfMemoryError e) {
			LOGGER.error("Not enough memory for this operation", e);
		} catch (IllegalArgumentException | IllegalStateException e) {
			LOGGER.error("Error in gradient computation: {}", e.getMessage(), e);
		}
		return newImagePlus;
	}
	
}
