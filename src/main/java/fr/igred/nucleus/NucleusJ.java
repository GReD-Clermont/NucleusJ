/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.igred.nucleus;

import fr.igred.nucleus.cli.CLIActionOptionCmdLine;
import fr.igred.nucleus.cli.CLIActionOptionOMERO;
import fr.igred.nucleus.cli.CLIHelper;
import fr.igred.nucleus.cli.CLIRunAction;
import fr.igred.nucleus.cli.CLIRunActionOMERO;
import fr.igred.nucleus.dialogs.MainGui;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import ij.util.ThreadUtil;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public final class NucleusJ {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/** Default constructor: private to prevent instantiation */
	private NucleusJ() {
		// DO NOTHING
	}
	
	
	public static void runOMEROCLI(String[] args) {
		CLIActionOptionOMERO command = new CLIActionOptionOMERO(args);
		
		CLIRunActionOMERO cliOMERO = new CLIRunActionOMERO(command.getCmd());
		try {
			cliOMERO.run();
		} catch (AccessException | ServiceException | OMEROServerError | ExecutionException e) {
			LOGGER.error("Error while running OMERO CLI command", e);
		} catch (IOException | FormatException e) {
			LOGGER.error("IO error while running OMERO CLI command", e);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while running CLI command", e);
			Thread.currentThread().interrupt(); // Restore interrupted status
		}
	}
	
	
	public static void runCLI(String[] args) {
		CLIActionOptionCmdLine command = new CLIActionOptionCmdLine(args);
		
		CLIRunAction cli = new CLIRunAction(command.getCmd());
		try {
			cli.run();
		} catch (IOException | FormatException e) {
			LOGGER.error("IO error while running CLI command", e);
		}
	}
	
	
	public static void main(String[] args) {
		List<String> listArgs = Arrays.asList(args);
		LOGGER.info("Starting NucleusJ version: {}", Version.get());
		
		// Allow threads from thread pool to timeout
		ThreadUtil.threadPoolExecutor.allowCoreThreadTimeOut(true);
		
		if (listArgs.isEmpty()) {
			LOGGER.info("Starting GUI...");
			SwingUtilities.invokeLater(() -> {
				MainGui gui = new MainGui();
				gui.setVisible(true);
			});
		} else {
			if (listArgs.contains("-h") || listArgs.contains("-help")) {
				CLIHelper.run(args);
			} else if (listArgs.contains("-ome") || listArgs.contains("-omero")) {
				runOMEROCLI(args);
			} else {
				runCLI(args);
			}
			LOGGER.info("Shutting down thread pool executor...");
			// Shutdown the thread pool executor to clean up resources
			ThreadUtil.threadPoolExecutor.shutdown();
			LOGGER.info("NucleusJ is now closing.");
		}
	}
	
}


