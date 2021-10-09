package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDFWriter;
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

        RDFFormat fmt = Optional.ofNullable(RDFLanguagesEx.findRdfFormat(outputSpec.outFormat))
            .orElseThrow(() -> new IllegalAccessException("Unknown format: " + outputSpec.outFormat));


        if (!StreamRDFWriter.registered(fmt)) {
            throw new IllegalArgumentException(fmt + " is not a streaming format");
        }

        JavaSparkContext sc = LsqSparkUtils.createSparkContext(conf -> {
            Optional.ofNullable(getTemporaryDirectory()).ifPresent(v -> conf.set("spark.local.dir", v));
        });

        JavaRDD<Dataset> logRdfEvents = JavaRddOfResourcesOps.mapToDatasets(LsqSparkIo.createLsqRdfFlow(sc, this));

        RddRdfSaver.createForDataset(logRdfEvents)
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(fmt)
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
