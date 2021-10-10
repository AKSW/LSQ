package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.Optional;

import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetOneNg;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;
import org.aksw.simba.lsq.cli.main.MainCliLsq;
import org.aksw.simba.lsq.core.LsqRdfizeSpec;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import net.sansa_stack.rdf.spark.io.LsqSparkIo;
import net.sansa_stack.rdf.spark.io.LsqSparkUtils;
import net.sansa_stack.spark.io.rdf.output.RddRdfWriterFactory;
import net.sansa_stack.spark.rdd.op.rx.JavaRddRxOps;

public class LsqSparkCmdUtils {

    public static void runSparkJob(
            LsqRdfizeSpec inputSpec,
            CmdOutputSpecBase outputSpec,
            RxFunction<DatasetOneNg, DatasetOneNg> processor) {

        PrefixMapping prefixes = MainCliLsq.addLsqPrefixes(new PrefixMappingImpl());

        RddRdfWriterFactory rddRdfWriterFactory = RddRdfWriterFactory.create()
                .setGlobalPrefixMapping(prefixes)
                .setOutputFormat(outputSpec.outFormat)
                .setDeferOutputForUsedPrefixes(100)
                // .setOutputFormat(cmd.getOutFormat())
                .setMapQuadsToTriplesForTripleLangs(true)
                // .setAllowOverwriteFiles(true)
                /// .setPartitionFolder(cmd.getOutFolder())
                .setTargetFile(outputSpec.outFile)
                // .setUseElephas(true)
                .setAllowOverwriteFiles(true)
                .setDeletePartitionFolderAfterMerge(true)
                .validate();

            JavaSparkContext sc = LsqSparkUtils.createSparkContext(conf -> {
                Optional.ofNullable(inputSpec.getTemporaryDirectory()).ifPresent(v -> conf.set("spark.local.dir", v));
            });

            JavaRDD<DatasetOneNg> baseRdd = LsqSparkIo.createLsqRdfFlow(sc, inputSpec);
            JavaRDD<DatasetOneNg> outRdd = JavaRddRxOps.mapPartitions(baseRdd, processor);


            rddRdfWriterFactory.forDataset(outRdd).runUnchecked();
    }

//    public static RxFunction<Tuple2<String, Model>, Resource> namedModelToResource() {
//        return RxFunction.<Tuple2<String, Model>>identity()
//                .andThenMap(t -> t._2().createResource(t._1()));
//    }
}
