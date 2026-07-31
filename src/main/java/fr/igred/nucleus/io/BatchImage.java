package fr.igred.nucleus.io;

import fr.igred.nucleus.segmentation.NucleusSegmentation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import ij.ImagePlus;
import loci.formats.FormatException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface BatchImage {
    ImagePlus getImagePlus() throws IOException, FormatException, ServiceException, AccessException, ExecutionException;
    void loadImagePlus() throws ServiceException, AccessException, ExecutionException;
    String getName();
    void markAsBadCrop(String imageTitle);
    void save(NucleusSegmentation seg, Map<String, Long> datasets)
            throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError;
    void saveImage(ImagePlus image, String localPath, long datasetId)
            throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError;
}
