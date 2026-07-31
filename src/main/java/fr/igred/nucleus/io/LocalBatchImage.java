package fr.igred.nucleus.io;

import fr.igred.nucleus.segmentation.NucleusSegmentation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import loci.formats.FormatException;
import loci.plugins.BF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static fr.igred.nucleus.io.ImageSaver.saveFile;

public class LocalBatchImage implements BatchImage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    File imageFile;
    int channel;


    public LocalBatchImage(File imageFile, int channel) {
        this.imageFile = imageFile;
        this.channel = channel;
    }


    @Override
    public ImagePlus getImagePlus() throws IOException, FormatException {
        ImagePlus[] currentImage = BF.openImagePlus(imageFile.getAbsolutePath());
        currentImage = ChannelSplitter.split(currentImage[channel]);
        return currentImage[0];
    }

    @Override
    public void loadImagePlus()  {

    }


    @Override
    public String getName() {
        return imageFile.getName();
    }

    @Override
    public void save(NucleusSegmentation seg, Map<String, Long> datasets)
    throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
        seg.saveOTSUSegmented_global(this,-1);
        seg.saveConvexHullSeg_global(this,-1);
    }
    @Override
    public void markAsBadCrop(String imageTitle) {
        File badCropFolder = new File(imageFile + File.separator + "BadCrop");
        LOGGER.debug("Saving bad crops to: {}", badCropFolder);

        if (badCropFolder.exists() || badCropFolder.mkdir()) {
            File    fileToMove = new File(imageFile + File.separator + imageTitle);
            File    newFile    = new File(badCropFolder + File.separator + imageTitle);
            boolean renamed    = fileToMove.renameTo(newFile);
            if (!renamed) {
                LOGGER.info("File not renamed: {}", fileToMove.getAbsolutePath());
            }
        } else {
            LOGGER.error("Directory does not exist and could not be created: {}", badCropFolder);
        }
    }

    public File getFile(){ return  imageFile;}

    @Override
    public void saveImage(ImagePlus image, String localPath, long datasetId){
        saveFile(image,localPath);
    }
}
