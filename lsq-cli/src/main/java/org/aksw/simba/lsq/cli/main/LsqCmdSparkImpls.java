package org.aksw.simba.lsq.cli.main;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.simba.lsq.cli.cmd.spark.CmdLsqProbeSpark;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sansa_stack.rdf.spark.io.LsqRegistrySparkAdapter;
import net.sansa_stack.rdf.spark.io.LsqSparkIo;
import net.sansa_stack.rdf.spark.io.LsqSparkUtils;
import net.sansa_stack.rdf.spark.io.SourceOfRddOfResources;

public class LsqCmdSparkImpls {
    private static final Logger logger = LoggerFactory.getLogger(LsqCmdSparkImpls.class);

    public static void probe(CmdLsqProbeSpark cmdProbe) {
        List<String> nonOptionArgs = cmdProbe.nonOptionArgs;
        if(nonOptionArgs.isEmpty()) {
            logger.error("No arguments provided.");
            logger.error("Argument must be one or more log files which will be probed against all registered LSQ log formats");
        }


        JavaSparkContext sc = LsqSparkUtils.createSparkContext();

        Map<String, SourceOfRddOfResources> registry = LsqRegistrySparkAdapter.createDefaultLogFmtRegistry(sc);

        for(int i = 0; i < nonOptionArgs.size(); ++i) {
            String filename = nonOptionArgs.get(i);

            List<Entry<String, Number>> bestCands = LsqSparkIo.probeLogFormat(registry, filename);

            System.out.println(filename + "\t" + bestCands);
        }
    }

}
