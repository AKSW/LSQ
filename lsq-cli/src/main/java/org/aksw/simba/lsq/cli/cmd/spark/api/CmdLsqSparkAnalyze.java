package org.aksw.simba.lsq.cli.cmd.spark.api;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqAnalyzeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;
import org.aksw.simba.lsq.cli.util.spark.LsqCliSparkUtils;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;
import org.aksw.simba.lsq.enricher.core.LsqEnricherShell;
import org.apache.jena.rdf.model.Resource;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "analyze", description = "Analyze query logs")
public class CmdLsqSparkAnalyze
    extends CmdLsqAnalyzeBase
    implements Callable<Integer>
{
    @Mixin
    public CmdOutputSpecBase outputSpec;

    @Override
    public Integer call() throws Exception {
        CmdLsqRdfizeBase rdfizeCmd = new CmdLsqRdfizeBase();
        rdfizeCmd.nonOptionArgs = this.nonOptionArgs;
        rdfizeCmd.noMerge = true;
        String baseIri = rdfizeCmd.baseIri;

        SerializableSupplier<LsqEnricherRegistry> registrySupplier = LsqEnricherRegistry::get;
        LsqEnricherShell enricherFactory = new LsqEnricherShell(baseIri, enricherSpec.getEffectiveList(), registrySupplier);

        LsqCliSparkUtils.runSparkJob(rdfizeCmd, outputSpec, inFlow -> {
//            LsqEnricherFactory enricherFactory = MainCliLsq.createEnricherFactory(baseIri, enrichers, true, LsqEnricherRegistry::get);
            Function<Resource, Resource> enricher = enricherFactory.get();

            return RxFunction.<DatasetOneNg>identity()
                .andThenMap(ds -> {
                    Resource r = ds.getModel().createResource(ds.getGraphName());
                    // System.err.println("Starting processing: " + r);

//                    if (r.toString().contains("3B0r_bVVjj377f2RT0CXPK-XvLFN4CVMaPXrc6leOCw")) {
//                        System.out.println("here");
//                    }

                    Resource skolemized = enricher.apply(r);

                    // System.err.println("Finished processed: " + r);
                    // TODO Do something with the skolemized resource?

                    return ds;
                })
                // .andThen(FlowOfResourcesOps::mapToDatasets)
                .apply(inFlow);
        });
        return 0;
    }
}
