package gred.nucleus.gradient;

import ij.ImagePlus;
import ij.Prefs;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import imagescience.utility.Progressor;


/**
 * Modification of plugin featureJ to integrate of this work,
 * <p>
 * => Use to imagescience.jar library
 *
 * @author poulet axel
 */
public class MyGradient {
	private static final boolean ISOTROPIC = Prefs.get("fj.isotropic", false);
	private static final boolean PGS       = Prefs.get("fj.pgs", true);
	private static final boolean LOG       = Prefs.get("fj.log", false);
	
	private static final boolean COMPUTE  = true;
	private static final boolean SUPPRESS = false;
	private static final String  SCALE    = "1.0";
	private final        boolean mask;
	
	private final ImagePlus imagePlus;
	
	private ImagePlus imagePlusBinary = null;
	
	
	public MyGradient(ImagePlus imp, ImagePlus imagePlusBinary) {
		imagePlus = imp;
		this.imagePlusBinary = imagePlusBinary;
		mask = true;
	}
	
	
	/**
	 * Run the gradient computation.
	 *
	 * @return a new ImagePlus with the gradient image
	 */
	public ImagePlus run() {
		double scaleVal;
		try {
			scaleVal = Double.parseDouble(SCALE);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid smoothing scale value", e);
		}
		Image      image      = Image.wrap(imagePlus);
		Image      newImage   = new FloatImage(image);
		double[]   pls        = {0, 1};
		int        pl         = 0;
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
		ImagePlus newImagePlus = newImage.imageplus();
		imagePlus.setCalibration(newImagePlus.getCalibration());
		double[] minMax = newImage.extrema();
		double   min    = minMax[0];
		double   max    = minMax[1];
		newImagePlus.setDisplayRange(min, max);
		return newImagePlus;
	}
	
}
