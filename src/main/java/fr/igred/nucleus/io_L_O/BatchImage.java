package fr.igred.nucleus.io_L_O;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import ij.ImagePlus;
import ij.macro.Variable;
import loci.formats.FormatException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface BatchImage {
    ImagePlus getImagePlus() throws IOException, FormatException, ServiceException, AccessException, ExecutionException;
    void loadImagePlus() throws ServiceException, AccessException, ExecutionException;
    String getName();
    //ImageWrapper getImage();
    //Variable getID();
}
