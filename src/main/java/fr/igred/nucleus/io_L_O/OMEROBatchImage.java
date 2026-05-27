package fr.igred.nucleus.io_L_O;

import fr.igred.nucleus.segmentation.NucleusSegmentation;
import fr.igred.nucleus.segmentation.SegmentationCalling;
import fr.igred.nucleus.segmentation.SegmentationParameters;
import fr.igred.omero.Client;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.roi.GenericShapeWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import ij.ImagePlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class OMEROBatchImage implements BatchImage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    @Override
    public void save(NucleusSegmentation seg, SegmentationCalling.OutputDatasets datasets)
    throws IOException, AccessException, ServiceException, ExecutionException, OMEROServerError {
        seg.saveOTSUSegmentedOMERO(client, datasets.getOtsu());
        seg.saveConvexHullSegOMERO(client, datasets.getConvexHull());
    }

    @Override
    public void markAsBadCrop(String imageTitle) {
        if (roi == null) {
            tagImageAsBadCrop();
        } else {
            markRoiAsBadCrop();
        }
    }

    private void tagImageAsBadCrop() {
        List<TagAnnotationWrapper> tags;
        TagAnnotationWrapper       tagBadCrop;

        try {
            tags = client.getTags("BadCrop");
        } catch (OMEROServerError | ServiceException e) {
            LOGGER.error("Could not get list of \"BadCrop\" tags", e);
            return;
        }

        if (tags.isEmpty()) {
            try {
                tagBadCrop = new TagAnnotationWrapper(client, "BadCrop", "");
            } catch (AccessException | ServiceException | ExecutionException e) {
                LOGGER.error("Could not create new \"BadCrop\" tag", e);
                return;
            }
        } else {
            tagBadCrop = tags.get(0);
        }

        LOGGER.info("Adding Bad Crop tag");
        try {
            image.link(client, tagBadCrop);
        } catch (AccessException | ServiceException | ExecutionException e) {
            LOGGER.error("Tag already added", e);
        }
    }

    private void markRoiAsBadCrop() {
        for (GenericShapeWrapper<?> shape : roi.getShapes()) {
            shape.setStroke(Color.RED);
        }
        try {
            roi.saveROI(client);
        } catch (OMEROServerError | ServiceException e) {
            LOGGER.error("Could not save bad crop ROI id: {}", roi.getId());
        }
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
