package org.aksw.simba.lsq.cli.trash;

import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmarkRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class MainCliLsqLogRdfizer {
    private static final Logger logger = LoggerFactory.getLogger(MainCliLsqLogRdfizer.class);

    public static void main(String[] args) throws Exception {
        CmdLsqBenchmarkRun cm = new CmdLsqBenchmarkRun();

        JCommander jc = new JCommander.Builder()
              .addObject(cm)
              .build();

        jc.parse(args);

    }
}
