package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import net.sansa_stack.rdf.spark.io.LsqSparkIo;
import net.sansa_stack.rdf.spark.io.LsqSparkUtils;
import net.sansa_stack.spark.io.rdf.output.RddRdfSaver;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfResourcesOps;
import picocli.CommandLine.Command;

@Command(name = "rdfize", description = "RDFize query logs")
public class CmdLsqRdfizeSpark
    extends CmdLsqRdfizeBase
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        JavaSparkContext sc = LsqSparkUtils.createSparkContext();
        JavaRDD<Dataset> logRdfEvents = JavaRddOfResourcesOps.mapToDatasets(LsqSparkIo.createLsqRdfFlow(sc, this));

        RddRdfSaver.createForDataset(logRdfEvents)
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(RDFFormat.TRIG_BLOCKS)
            // .setOutputFormat(cmd.getOutFormat())
            .setMapQuadsToTriplesForTripleLangs(true)
            // .setAllowOverwriteFiles(true)
            /// .setPartitionFolder(cmd.getOutFolder())
            .setTargetFile("/tmp/result.trig")
            // .setUseElephas(true)
            .setAllowOverwriteFiles(true)
            .setDeletePartitionFolderAfterMerge(true)
            .run();



        return 0;
    }
}
