package gred.nucleus.gradient;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import imagescience.feature.Structure;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;
import imagescience.image.Image;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;


public class FJ_Structure implements PlugIn, WindowListener {
	
	private static final Point pos = new Point(-1, -1);
	
	private static boolean largest  = true;
	private static boolean middle;
	private static boolean smallest = true;
	
	private static String smoothingScale   = "1.0";
	private static String integrationScale = "3.0";
	
	
	public void run(String arg) {
		
		if (!FJ.libCheck()) {
			return;
		}
		ImagePlus imp = FJ.imageplus();
		if (imp == null) {
			return;
		}
		
		FJ.log(FJ.name() + " " + FJ.version() + ": Structure");
		
		GenericDialog gd = new GenericDialog(FJ.name() + ": Structure");
		gd.addCheckbox(" Largest eigenvalue of structure tensor    ", largest);
		gd.addCheckbox(" Middle eigenvalue of structure tensor    ", middle);
		gd.addCheckbox(" Smallest eigenvalue of structure tensor    ", smallest);
		gd.addPanel(new Panel(), GridBagConstraints.EAST, new Insets(5, 0, 0, 0));
		gd.addStringField("                Smoothing scale:", smoothingScale);
		gd.addStringField("                Integration scale:", integrationScale);
		
		if (pos.x >= 0 && pos.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(pos);
		} else {
			gd.centerDialog(true);
		}
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) {
			return;
		}
		
		largest = gd.getNextBoolean();
		middle = gd.getNextBoolean();
		smallest = gd.getNextBoolean();
		smoothingScale = gd.getNextString();
		integrationScale = gd.getNextString();
		
		new FJStructure().run(imp, largest, middle, smallest, smoothingScale, integrationScale);
	}
	
	
	public void windowActivated(WindowEvent e) {
	}
	
	
	public void windowClosed(WindowEvent e) {
		
		pos.x = e.getWindow().getX();
		pos.y = e.getWindow().getY();
	}
	
	
	public void windowClosing(WindowEvent e) {
	}
	
	
	public void windowDeactivated(WindowEvent e) {
	}
	
	
	public void windowDeiconified(WindowEvent e) {
	}
	
	
	public void windowIconified(WindowEvent e) {
	}
	
	
	public void windowOpened(WindowEvent e) {
	}
	
}

class FJStructure {
	
	void run(ImagePlus imp,
	         boolean largest,
	         boolean middle,
	         boolean smallest,
	         String smoothingScale,
	         String integrationScale) {
		try {
			double sScaleVal;
			double iScaleVal;
			try {
				sScaleVal = Double.parseDouble(smoothingScale);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid smoothing scale value");
			}
			try {
				iScaleVal = Double.parseDouble(integrationScale);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid integration scale value");
			}
			
			Image   img     = Image.wrap(imp);
			Aspects aspects = img.aspects();
			if (!FJ_Options.isotropic) {
				img.aspects(new Aspects());
			}
			Structure structure = new Structure();
			structure.messenger.log(FJ_Options.log);
			structure.messenger.status(FJ_Options.pgs);
			structure.progressor.display(FJ_Options.pgs);
			
			Vector<Image> eigenImages = structure.run(new FloatImage(img), sScaleVal, iScaleVal);
			
			int nImages = eigenImages.size();
			for (Image eigenImage : eigenImages) {
				eigenImage.aspects(aspects);
			}
			if (nImages == 2) {
				if (largest) {
					FJ.show(eigenImages.get(0), imp);
				}
				if (smallest) {
					FJ.show(eigenImages.get(1), imp);
				}
			} else if (nImages == 3) {
				if (largest) {
					FJ.show(eigenImages.get(0), imp);
				}
				if (middle) {
					FJ.show(eigenImages.get(1), imp);
				}
				if (smallest) {
					FJ.show(eigenImages.get(2), imp);
				}
			}
			FJ.close(imp);
		} catch (OutOfMemoryError e) {
			FJ.error("Not enough memory for this operation");
		} catch (IllegalArgumentException | IllegalStateException e) {
			FJ.error(e.getMessage());
		} catch (Exception e) {
			FJ.error("An unidentified error occurred while running the plugin");
		}
	}
	
}
