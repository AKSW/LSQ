package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.simba.lsq.core.LsqUtils;



public class MainCliLsqProbeLogFormat {
	public static void main(String[] args) throws IOException {
		for(int i = 0; i < args.length; ++i) {			
			String filename = args[i];

			List<Entry<String, Number>> bestCands = LsqUtils.probeLogFormat(filename);
			
			System.out.println(filename + "\t" + bestCands);
		}	
	}
}
