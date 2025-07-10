package gred.nucleus.gradient;

import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;

import java.io.IOException;


public class FJ_Website implements PlugIn {
	
	public void run(String arg) {
		
		try {
			BrowserLauncher.openURL("http://www.imagescience.org/meijering/software/featurej/");
		} catch (IOException e) {
			FJ.error("Could not open default internet browser");
		}
	}
	
}
