package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.List;

import org.aksw.simba.lsq.core.LsqUtils;



public class MainLsqProbeLogFormat {
	public static void main(String[] args) throws IOException {
		for(int i = 0; i < args.length; ++i) {			
			String filename = args[i];

			List<String> bestCands = LsqUtils.probeLogFormat(filename);
			
			System.out.println(filename + "\t" + bestCands);
		}	
	}
}
