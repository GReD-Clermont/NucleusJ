package gred.nucleus.gradient;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import imagescience.feature.Edges;
import imagescience.image.Aspects;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import imagescience.segment.Thresholder;
import imagescience.utility.Progressor;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class FJ_Edges implements PlugIn, ItemListener, WindowListener {
	
	private static final Point    pos      = new Point(-1, -1);
	private static       boolean  compute  = true;
	private static       boolean  suppress;
	private static       String   scale    = "1.0";
	private static       String   lower    = "";
	private static       String   higher   = "";
	private              Checkbox computeBox;
	private              Checkbox suppressBox;
	
	
	public void run(String arg) {
		
		if (!FJ.libCheck()) {
			return;
		}
		ImagePlus imp = FJ.imageplus();
		if (imp == null) {
			return;
		}
		
		FJ.log(FJ.name() + " " + FJ.version() + ": Edges");
		
		GenericDialog gd = new GenericDialog(FJ.name() + ": Edges");
		gd.addCheckbox(" Compute gradient-magnitude image     ", compute);
		gd.addStringField("                Smoothing scale:", scale);
		gd.addPanel(new Panel(), GridBagConstraints.LINE_END, new Insets(0, 0, 0, 0));
		gd.addCheckbox(" Suppress non-maximum gradients     ", suppress);
		gd.addPanel(new Panel(), GridBagConstraints.LINE_END, new Insets(0, 0, 0, 0));
		gd.addStringField("                Lower threshold value:", lower);
		gd.addStringField("                Higher threshold value:", higher);
		computeBox = (Checkbox) gd.getCheckboxes().get(0);
		computeBox.addItemListener(this);
		suppressBox = (Checkbox) gd.getCheckboxes().get(1);
		suppressBox.addItemListener(this);
		
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
		
		compute = gd.getNextBoolean();
		scale = gd.getNextString();
		suppress = gd.getNextBoolean();
		lower = gd.getNextString();
		higher = gd.getNextString();
		
		new FJEdges().run(imp, compute, scale, suppress, lower, higher);
	}
	
	
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getSource() == computeBox) {
			if (!computeBox.getState()) {
				suppressBox.setState(false);
			}
		} else if (e.getSource() == suppressBox) {
			if (suppressBox.getState()) {
				computeBox.setState(true);
			}
		}
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

class FJEdges {
	
	void run(ImagePlus imp,
	         boolean compute,
	         String scale,
	         boolean suppress,
	         String lower,
	         String higher) {
		
		try {
			double  scaleVal;
			double  lowVal        = 0;
			double  highVal       = 0;
			boolean lowThreshold  = true;
			boolean highThreshold = true;
			try {
				scaleVal = Double.parseDouble(scale);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid smoothing scale value");
			}
			try {
				if (lower.isEmpty()) {
					lowThreshold = false;
				} else {
					lowVal = Double.parseDouble(lower);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid lower threshold value");
			}
			try {
				if (higher.isEmpty()) {
					highThreshold = false;
				} else {
					highVal = Double.parseDouble(higher);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid higher threshold value");
			}
			int thresholdMode = (lowThreshold ? 10 : 0) + (highThreshold ? 1 : 0);
			
			Image img    = Image.wrap(imp);
			Image       newImg = new FloatImage(img);
			
			double[] pls = {0, 1};
			int      pl  = 0;
			if ((compute || suppress) && thresholdMode > 0) {
				pls = new double[]{0, 0.9, 1};
			}
			Progressor progressor = new Progressor();
			progressor.display(FJ_Options.pgs);
			
			if (compute || suppress) {
				Aspects aspects = newImg.aspects();
				if (!FJ_Options.isotropic) {
					newImg.aspects(new Aspects());
				}
				Edges edges = new Edges();
				++pl;
				progressor.range(pls[pl], pls[pl]);
				edges.progressor.parent(progressor);
				edges.messenger.log(FJ_Options.log);
				edges.messenger.status(FJ_Options.pgs);
				newImg = edges.run(newImg, scaleVal, suppress);
				newImg.aspects(aspects);
			}
			
			if (thresholdMode > 0) {
				Thresholder thresholder = new Thresholder();
				++pl;
				progressor.range(pls[pl], pls[pl]);
				thresholder.progressor.parent(progressor);
				thresholder.messenger.log(FJ_Options.log);
				thresholder.messenger.status(FJ_Options.pgs);
				switch (thresholdMode) {
					case 1: {
						thresholder.hard(newImg, highVal);
						break;
					}
					case 10: {
						thresholder.hard(newImg, lowVal);
						break;
					}
					case 11: {
						thresholder.hysteresis(newImg, lowVal, highVal);
						break;
					}
				}
			}
			
			FJ.show(newImg, imp);
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
