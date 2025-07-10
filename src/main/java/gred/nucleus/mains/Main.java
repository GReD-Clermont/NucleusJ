package gred.nucleus.mains;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import gred.nucleus.cli.CLIActionOptionCmdLine;
import gred.nucleus.cli.CLIActionOptionOMERO;
import gred.nucleus.cli.CLIHelper;
import gred.nucleus.cli.CLIRunAction;
import gred.nucleus.cli.CLIRunActionOMERO;
import gred.nucleus.dialogs.MainGui;
import loci.formats.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Main {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public static void main(String[] args)
	throws AccessException, ServiceException, OMEROServerError,
	       IOException, ExecutionException, InterruptedException, FormatException {
		List<String> listArgs = Arrays.asList(args);
		
		/* Allow IJ threads from thread pool to timeout */
		//ThreadUtil.threadPoolExecutor.allowCoreThreadTimeOut(true);
		
		if (listArgs.contains("-h") || listArgs.contains("-help")) {
			CLIHelper.run(args);
		} else if (listArgs.contains("-ome") || listArgs.contains("-omero")) {
			CLIActionOptionOMERO command = new CLIActionOptionOMERO(args);
			
			CLIRunActionOMERO cliOMERO = new CLIRunActionOMERO(command.getCmd());
			cliOMERO.run();
		} else if (listArgs.contains("-nj") || listArgs.contains("-cli") || listArgs.contains("-CLI")) {
			CLIActionOptionCmdLine command = new CLIActionOptionCmdLine(args);
			
			CLIRunAction cli = new CLIRunAction(command.getCmd());
			cli.run();
		} else {
			SwingUtilities.invokeLater(() -> {
				MainGui gui = new MainGui();
				gui.setVisible(true);
			});
		}
		//ThreadUtil.threadPoolExecutor.shutdown();
	}
	
}


