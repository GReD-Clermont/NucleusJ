package fr.igred.nucleus.io_L_O;

import fr.igred.nucleus.segmentation.SegmentationParameters;
import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;

import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import ij.ImagePlus;

import java.util.List;
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
    //public static final String IJ_ID_PROPERTY = "IMAGE_ID";
    ////////ROIs
    public int i_roi;
    public ROIWrapper roi = null;

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
    public OMEROBatchImage(ImageWrapper image,
                                 ROIWrapper roi,
                                 int i,
                                 SegmentationParameters params,
                                 Client client, int[] tBound) {
        List<RectangleWrapper> rectangles = roi.getShapes().getElementsOf(RectangleWrapper.class);

        RectangleWrapper rectangle = rectangles.get(0);

        int roiThickness = rectangles.size();
        int channel      = rectangle.getC();
        int slice        = rectangle.getZ();

        double[] coordinates = rectangle.getCoordinates();
        int      x           = (int) coordinates[0];
        int      y           = (int) coordinates[1];
        int      width       = (int) coordinates[2];
        int      height      = (int) coordinates[3];

        int[] cBound = {channel, channel};
        int[] zBound = {slice, slice + roiThickness - 1};
        int[] xBound = {x, x + width - 1};
        int[] yBound = {y, y + height - 1};

        this.roi = roi;             //c'est pour checkBadCrop pour les ROIs
        this.image = image;
        this.i_roi = i;
        this.client = client;
        this.xBounds = xBound;
        this.yBounds = yBound;
        this.cBounds = cBound;
        this.zBounds = zBound;
        this.tBounds = tBound;
    }

    @Override
    public ImagePlus getImagePlus() throws ServiceException, AccessException, ExecutionException {
        if(imageplus == null) {
            loadImagePlus();
        }
        return imageplus;

    }

    @Override
    public void loadImagePlus() throws ServiceException, AccessException, ExecutionException {
        imageplus = image.toImagePlus(client, xBounds, yBounds, cBounds, zBounds, tBounds);
    }

    @Override
    public String getName(){
        return image.getName();
    }


    public ImageWrapper getImage() {
        return image;
    }

    public Client getClient() {
        return client;
    }

    public ROIWrapper getROI() {
        return  roi;
    }
}
