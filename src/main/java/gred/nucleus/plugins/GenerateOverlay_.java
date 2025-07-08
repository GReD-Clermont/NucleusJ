package gred.nucleus.plugins;

import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import gred.nucleus.autocrop.GenerateOverlay;
import gred.nucleus.dialogs.GenerateOverlayDialog;
import gred.nucleus.dialogs.IDialogListener;
import ij.IJ;
import ij.Prefs;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class GenerateOverlay_ implements PlugIn, IDialogListener {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final GenerateOverlay generateOverlay = new GenerateOverlay();
	
	private GenerateOverlayDialog generateOverlayDialog = null;
	
	
	@Override
	public void run(String arg) {
		if (IJ.versionLessThan("1.32c")) {
			return;
		}
		generateOverlayDialog = new GenerateOverlayDialog(this);
	}
	
	
	@Override
	public void OnStart() throws AccessException, ServiceException, ExecutionException {
		if (generateOverlayDialog.isOmeroEnabled()) {
			runOMERO();
		} else {
			runLocal();
		}
	}
	
	
	void runLocal() {
		String dicFile         = generateOverlayDialog.getDICInput();
		String zProjectionFile = generateOverlayDialog.getZprojectionInput();
		if (dicFile == null || dicFile.isEmpty() || zProjectionFile == null || zProjectionFile.isEmpty()) {
			IJ.error("Input file or directory is missing");
		} else {
			try {
				LOGGER.info("Begin Overlay process ");
				GenerateOverlay generateOverlay1 = new GenerateOverlay(zProjectionFile, dicFile);
				generateOverlay1.run(); // Run Overlay process
				
				LOGGER.info("Overlay  process has ended successfully");
			} catch (Exception e) {
				LOGGER.info("Overlay process has failed");
				LOGGER.error("An error occurred.", e);
			}
		}
	}
	
	
	public void runOMERO() {
		// Check connection
		String hostname = generateOverlayDialog.getHostname();
		String port     = generateOverlayDialog.getPort();
		String username = generateOverlayDialog.getUsername();
		String password = generateOverlayDialog.getPassword();
		String group    = generateOverlayDialog.getGroup();
		String output   = generateOverlayDialog.getOutputProject();
		// Set user prefs
		Prefs.set("omero.host", hostname);
		Prefs.set("omero.port", port);
		Prefs.set("omero.user", username);
		// Connect to OMERO
		Client client = checkOMEROConnection(hostname, port, username, password.toCharArray(), group);
		// Handle the source according to the type given
		String zProjectionDataType = generateOverlayDialog.getZprojectionDataType();
		String dicDataType         = generateOverlayDialog.getDICDataType();
		//Get Datasets IDs
		String zProjectionID = generateOverlayDialog.getSourceID();
		String dicID         = generateOverlayDialog.getzProjectionID();
		
		try {
			if ("Dataset".equals(dicDataType) && "Dataset".equals(zProjectionDataType)) {
				try {
					LOGGER.info("Begin Overlay process ");
					DatasetWrapper     dicDataset = client.getDataset(Long.parseLong(dicID));
					List<ImageWrapper> dicImages  = dicDataset.getImages(client);
					if (!dicImages.isEmpty()) {
						generateOverlay.runFromOMERO(zProjectionID, dicID, output, client); // Run Overlay process
					}
					LOGGER.info("Overlay process has ended successfully");
				} catch (AccessException | OMEROServerError | ServiceException | IOException | ExecutionException e) {
					LOGGER.error("Overlay process has failed: ", e);
				} catch (NumberFormatException e) {
					LOGGER.error("Invalid Dataset ID", e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
	
	public Client checkOMEROConnection(String hostname,
	                                   String port,
	                                   String username,
	                                   char[] password,
	                                   String group) {
		Client client = new Client();
		try {
			client.connect(hostname, Integer.parseInt(port),
			               username, password, Long.valueOf(group));
		} catch (ServiceException exp) {
			LOGGER.error("ServiceException: ", exp);
			return null;
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid port or group value: ", e);
			return null;
		}
		return client;
	}
	
}
