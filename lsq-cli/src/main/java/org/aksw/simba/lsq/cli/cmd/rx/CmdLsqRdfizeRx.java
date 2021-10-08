package org.aksw.simba.lsq.cli.cmd.rx;

import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;

@Command(name = "rdfize", description = "RDFize query logs into query centric named graphs")
public class CmdLsqRdfizeRx
    extends CmdLsqRdfizeBase
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        MainCliLsq.rdfize(this);
        return 0;
    }
}
