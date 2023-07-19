package gred.nucleus.mains;

import gred.nucleus.cli.CLIActionOptionCmdLine;
import gred.nucleus.cli.CLIActionOptionOMERO;
import gred.nucleus.cli.CLIHelper;
import gred.nucleus.cli.CLIRunAction;
import gred.nucleus.cli.CLIRunActionOMERO;
import ij.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;


public class Main {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static void main(String[] args) throws Exception {
		List<String> listArgs = Arrays.asList(args);
		
		/* Allow IJ threads from thread pool to timeout */
		ThreadUtil.threadPoolExecutor.allowCoreThreadTimeOut(true);
		
		if (listArgs.contains("-h") || listArgs.contains("-help")) {
			CLIHelper.run(args);
		} else if ((listArgs.contains("-ome")) || (listArgs.contains("-omero"))) {
			CLIActionOptionOMERO command = new CLIActionOptionOMERO(args);
			CLIRunActionOMERO cliOMERO = new CLIRunActionOMERO(command.getCmd());
			cliOMERO.run();
		} else {
			CLIActionOptionCmdLine command = new CLIActionOptionCmdLine(args);
			CLIRunAction cli = new CLIRunAction(command.getCmd());
			cli.run();
		}
		ThreadUtil.threadPoolExecutor.shutdown();
	}
	
}


