package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.concurrent.Callable;

import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetOneNg;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdOutputSpecBase;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "rdfize", description = "RDFize query logs")
public class CmdLsqSparkRdfize
    extends CmdLsqRdfizeBase
    implements Callable<Integer>
{
    @Mixin
    public CmdOutputSpecBase outputSpec;


    @Override
    public Integer call() throws Exception {
        LsqSparkCmdUtils.runSparkJob(this, outputSpec, RxFunction.<DatasetOneNg>identity());
        return 0;
    }


}
