/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.igred.nucleus.plugins;

import fr.igred.nucleus.core.ChromocentersEnhancement;
import fr.igred.nucleus.dialogs.ChromocenterSegmentationPipelineBatchDialog;
import fr.igred.nucleus.dialogs.IDialogListener;
import fr.igred.nucleus.io.FileList;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.GaussianBlur3D;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author Tristan Dubos and Axel Poulet
 */
public class ChromocenterSegmentationBatchPlugin implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private ChromocenterSegmentationPipelineBatchDialog ccSegDialog;
	
	/* This method is used by plugins.config */
	public void run(String arg) {
		ccSegDialog = new ChromocenterSegmentationPipelineBatchDialog(this);
	}
	
	
	@Override
	public void onStart() throws AccessException, ServiceException, ExecutionException {
		FileList fileList     = new FileList();
		File[]   tFileRawData = fileList.run(ccSegDialog.getRawDataDirectory());
		if (fileList.isDirectoryOrFileExist(".+RawDataNucleus.+", tFileRawData) &&
		    fileList.isDirectoryOrFileExist(".+SegmentedDataNucleus.+", tFileRawData)) {
			List<String> segmentedNucImages = fileList.fileSearchList(".+SegmentedDataNucleus.+", tFileRawData);
			String       workDirectory      = ccSegDialog.getWorkDirectory();
			for (int i = 0; i < segmentedNucImages.size(); ++i) {
				LOGGER.info("image {}/{}", i + 1, segmentedNucImages.size());
				String pathImgSegmentedNuc = segmentedNucImages.get(i);
				
				String pathNucleusRaw = pathImgSegmentedNuc.replace("SegmentedDataNucleus", "RawDataNucleus");
				LOGGER.info(pathNucleusRaw);
				if (fileList.isDirectoryOrFileExist(pathNucleusRaw, tFileRawData)) {
					ImagePlus imagePlusSegmented = IJ.openImage(pathImgSegmentedNuc);
					ImagePlus imagePlusInput     = IJ.openImage(pathNucleusRaw);
					processOneNucleus(imagePlusInput, imagePlusSegmented, workDirectory);
				}
			}
			LOGGER.info("End of the chromocenter segmentation , the results are in {}", workDirectory);
		} else {
			IJ.showMessage("There are no the two subdirectories (See the directory name) or subDirectories are empty");
		}
	}
	
	
	/**
	 * Process one nucleus image and save the contrast data.
	 *
	 * @param imagePlusInput     The input image of the nucleus.
	 * @param imagePlusSegmented The segmented image of the nucleus.
	 * @param workDirectory      The directory where to save the results.
	 */
	private void processOneNucleus(ImagePlus imagePlusInput, ImagePlus imagePlusSegmented, String workDirectory) {
		GaussianBlur3D.blur(imagePlusInput, 0.25, 0.25, 1);
		ImageStack imageStack = imagePlusInput.getStack();
		int        max        = 0;
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int b = 0; b < imagePlusInput.getWidth(); ++b) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					if (max < imageStack.getVoxel(b, j, k)) {
						max = (int) imageStack.getVoxel(b, j, k);
					}
				}
			}
		}
		IJ.setMinAndMax(imagePlusInput, 0, max);
		IJ.run(imagePlusInput, "Apply LUT", "stack");
		Calibration calibration = new Calibration();
		if (ccSegDialog.getCalibrationStatus()) {
			calibration.pixelWidth = ccSegDialog.getXCalibration();
			calibration.pixelHeight = ccSegDialog.getYCalibration();
			calibration.pixelDepth = ccSegDialog.getZCalibration();
			calibration.setUnit(ccSegDialog.getUnit());
		} else {
			calibration = imagePlusInput.getCalibration();
		}
		ImagePlus imagePlusContrast = ChromocentersEnhancement.applyEnhanceChromocenters(imagePlusInput,
		                                                                                 imagePlusSegmented);
		imagePlusContrast.setTitle(imagePlusInput.getTitle());
		imagePlusContrast.setCalibration(calibration);
		saveFile(imagePlusContrast, workDirectory + File.separator + "ContrastDataNucleus");
	}
	
	
	/**
	 * saving file method
	 *
	 * @param imagePlus imagePus to save
	 * @param pathFile  the path where save the image
	 */
	public static void saveFile(ImagePlus imagePlus, String pathFile) {
		
		FileSaver fileSaver = new FileSaver(imagePlus);
		File      file      = new File(pathFile);
		if (file.exists() || file.mkdirs()) {
			fileSaver.saveAsTiffStack(pathFile + File.separator + imagePlus.getTitle());
		} else {
			LOGGER.error("Directory does not exist and could not be created: {}", pathFile);
		}
	}
	
}