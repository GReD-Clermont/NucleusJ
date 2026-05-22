package fr.igred.nucleus.io_L_O;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.ImagePlus;
import loci.formats.FormatException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface BatchImage {
    ImagePlus getImagePlus() throws IOException, FormatException, ServiceException, AccessException, ExecutionException;
    void loadImagePlus() throws ServiceException, AccessException, ExecutionException;
    String getName();
    void markAsBadCrop(String imageTitle);
}
