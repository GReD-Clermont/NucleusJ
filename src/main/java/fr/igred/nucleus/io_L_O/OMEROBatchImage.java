package fr.igred.nucleus.io_L_O;

import fr.igred.nucleus.segmentation.SegmentationParameters;
import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;

import fr.igred.omero.roi.ROIWrapper;
import ij.ImagePlus;

import java.util.concurrent.ExecutionException;



public class OMEROBatchImage implements BatchImage {


    private ImageWrapper image;
    private Client client;
    private ImagePlus imageplus = null;
    private int[] xBounds;
    private int[] yBounds;
    private int[] cBounds;
    private int[] zBounds;
    private int[] tBounds;
    public static final String IJ_ID_PROPERTY = "IMAGE_ID";
    ////////ROIs
    public ROIWrapper roi;
    public int i_roi;

    public OMEROBatchImage(ImageWrapper image, Client client,
                    int[] xBounds,
                    int[] yBounds,
                    int[] cBounds,
                    int[] zBounds,
                    int[] tBounds){
        this.image = image;
        this.client = client;
        this.xBounds = xBounds;
        this.yBounds = yBounds;
        this.cBounds = cBounds;
        this.zBounds = zBounds;
        this.tBounds = tBounds;
    }
    public OMEROBatchImage(ImageWrapper image, ImagePlus imageplus,
                           int[] xBounds,
                           int[] yBounds,
                           int[] cBounds,
                           int[] zBounds,
                           int[] tBounds){
        this.image = image;
        this.imageplus = imageplus;
        this.xBounds = xBounds;
        this.yBounds = yBounds;
        this.cBounds = cBounds;
        this.zBounds = zBounds;
        this.tBounds = tBounds;
    }

    @Override
    public ImagePlus getImagePlus() throws ServiceException, AccessException, ExecutionException {
        if(imageplus != null) {
            return imageplus;
        }
        return image.toImagePlus(client, xBounds, yBounds, cBounds, zBounds, tBounds);

    }

    @Override
    public String getName(){
        return image.getName();
    }
}
