package gred.nucleus.utils;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.ChannelSplitter;
import loci.common.DebugTools;
import loci.formats.FormatException;
import loci.plugins.BF;

import java.io.IOException;


public class Rd2ToTif {
	
	
	private final String pathInput;
	private final String pathOutput;
	
	
	/**
	 * @param pathInput
	 * @param pathOutput
	 */
	public Rd2ToTif(String pathInput, String pathOutput) {
		this.pathInput = pathInput;
		this.pathOutput = pathOutput;
	}
	
	
	/**
	 * Method to get specific channel to compute OTSU threshold
	 *
	 * @param channelNumber : number of channel to compute OTSU for crop
	 *
	 * @return image of specific channel
	 *
	 * @throws FormatException
	 * @throws IOException
	 */
	public static ImagePlus getImageChannel(int channelNumber, String input)
	throws FormatException, IOException {
		DebugTools.enableLogging("OFF");    // DEBUG INFO BIOFORMAT OFF
		ImagePlus[] currentImage = BF.openImagePlus(input);
		currentImage = ChannelSplitter.split(currentImage[0]);
		return currentImage[channelNumber];
	}
	
	
	/**
	 * Save the image file
	 *
	 * @param imagePlusInput image to save
	 * @param pathFile       path to save the image
	 */
	public static void saveFile(ImagePlus imagePlusInput, String pathFile) {
		FileSaver fileSaver = new FileSaver(imagePlusInput);
		fileSaver.saveAsTiff(pathFile);
	}
	
	
	public void run(int channel) throws IOException, FormatException {
		ImagePlus img = getImageChannel(channel);
		saveFile(img, pathOutput);
	}
	
	
	/**
	 * Method to get specific channel to compute OTSU threshold
	 *
	 * @param channelNumber : number of channel to compute OTSU for crop
	 *
	 * @return image of specific channel
	 */
	private ImagePlus getImageChannel(int channelNumber)
	throws IOException, FormatException {
		DebugTools.enableLogging("OFF");    // DEBUG INFO BIOFORMAT OFF
		ImagePlus[] currentImage = BF.openImagePlus(pathInput);
		currentImage = ChannelSplitter.split(currentImage[0]);
		return currentImage[channelNumber];
	}
	
}
