package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.simba.lsq.cli.main.cmd.CmdLsqMain;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqProbe;
import org.aksw.simba.lsq.core.LsqUtils;

import com.beust.jcommander.JCommander;



/**
 * This should become the new main class
 * 
 * @author raven
 *
 */
public class MainCliLsq {
	public static void main(String[] args) throws IOException {
		CmdLsqMain cmdMain = new CmdLsqMain();
		
		CmdLsqProbe cmdProbe = new  CmdLsqProbe();

		JCommander jc = JCommander.newBuilder()
				.addObject(cmdMain)
				.addCommand("probe", cmdProbe)
				.build();


		jc.parse(args);

        if (cmdMain.help || jc.getParsedCommand() == null) {
            jc.usage();
            return;
        }

        // TODO Change this to a plugin system - for now I hack this in statically
		String cmd = jc.getParsedCommand();
		switch (cmd) {
		case "probe": {
			List<String> nonOptionArgs = cmdProbe.nonOptionArgs;
			if(nonOptionArgs.size() == 0) {
				System.out.println("No arguments provided.");
				System.out.println("Argument must be one or more log files which will be probed against all registered LSQ log formats");
			}
			
			for(int i = 0; i < nonOptionArgs.size(); ++i) {			
				String filename = nonOptionArgs.get(i);

				List<Entry<String, Number>> bestCands = LsqUtils.probeLogFormat(filename);
				
				System.out.println(filename + "\t" + bestCands);
			}
			break;
		}
		default:
			throw new RuntimeException("Unsupported command: " + cmd);
		}
	}
}
