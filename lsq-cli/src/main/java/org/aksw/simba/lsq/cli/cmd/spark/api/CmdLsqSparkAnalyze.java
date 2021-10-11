package org.aksw.simba.lsq.cli.cmd.spark.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetOneNg;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;
import org.aksw.simba.lsq.cli.main.MainCliLsq;
import org.aksw.simba.lsq.cli.util.spark.LsqCliSparkUtils;
import org.apache.jena.rdf.model.Resource;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "analyze", description = "Analyze query logs")
public class CmdLsqSparkAnalyze
    implements Callable<Integer>
{
    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @Parameters(arity = "1..*", description = "file-list to probe")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Mixin
    public CmdOutputSpecBase outputSpec;

    @Override
    public Integer call() throws Exception {
        CmdLsqRdfizeBase rdfizeCmd = new CmdLsqRdfizeBase();
        rdfizeCmd.nonOptionArgs = this.nonOptionArgs;
        rdfizeCmd.noMerge = true;
        String baseIri = rdfizeCmd.baseIri;

        LsqCliSparkUtils.runSparkJob(rdfizeCmd, outputSpec, inFlow -> {
            SerializableFunction<Resource, Resource> enricher = MainCliLsq.createEnricher(baseIri);

            return RxFunction.<DatasetOneNg>identity()
                .andThenMap(ds -> {
                    Resource r = ds.getModel().createResource(ds.getGraphName());

                    Resource skolemized = enricher.apply(r);

                    // TODO Do something with the skolemized resource?

                    return ds;
                })
                // .andThen(FlowOfResourcesOps::mapToDatasets)
                .apply(inFlow);
        });
        return 0;
    }
}
