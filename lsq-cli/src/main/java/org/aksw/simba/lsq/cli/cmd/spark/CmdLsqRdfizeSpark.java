package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import net.sansa_stack.rdf.spark.io.LsqSparkIo;
import net.sansa_stack.rdf.spark.io.LsqSparkUtils;
import net.sansa_stack.spark.io.rdf.output.RddRdfSaver;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfResourcesOps;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "rdfize", description = "RDFize query logs")
public class CmdLsqRdfizeSpark
    extends CmdLsqRdfizeBase
    implements Callable<Integer>
{
    @Mixin
    public CmdOutputSpecBase outputSpec;


    @Override
    public Integer call() throws Exception {

        JavaSparkContext sc = LsqSparkUtils.createSparkContext(conf -> {
            Optional.ofNullable(getTemporaryDirectory()).ifPresent(v -> conf.set("spark.local.dir", v));
        });

        JavaRDD<Dataset> logRdfEvents = JavaRddOfResourcesOps.mapToDatasets(LsqSparkIo.createLsqRdfFlow(sc, this));

        RddRdfSaver.createForDataset(logRdfEvents)
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(outputSpec.outFormat)
            // .setOutputFormat(cmd.getOutFormat())
            .setMapQuadsToTriplesForTripleLangs(true)
            // .setAllowOverwriteFiles(true)
            /// .setPartitionFolder(cmd.getOutFolder())
            .setTargetFile(outputSpec.outFile)
            // .setUseElephas(true)
            .setAllowOverwriteFiles(true)
            .setDeletePartitionFolderAfterMerge(true)
            .run();



        return 0;
    }
}
