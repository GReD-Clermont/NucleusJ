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

public class OMEROBatchimagebyROIs implements BatchImage{
    private ImageWrapper image;
    private Client client;
    private int[] xBounds;
    private int[] yBounds;
    private int[] cBounds;
    private int[] zBounds;
    private int[] tBounds;
    private ROIWrapper roi;
    private int i_roi;

    public OMEROBatchimagebyROIs(ImageWrapper image,
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

        this.image = image;
        this.roi = roi;
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
        return image.toImagePlus(client, xBounds, yBounds, cBounds, zBounds, tBounds);
    }

    @Override
    public String getName() {
        return image.getName();
    }
}
