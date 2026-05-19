package fr.igred.nucleus.io_L_O;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import loci.formats.FormatException;
import loci.plugins.BF;

import java.io.File;
import java.io.IOException;

public class LocalBatchImage implements BatchImage {


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
    public String getName() {
        return imageFile.getName();
    }
}
