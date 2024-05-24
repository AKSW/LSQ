package org.aksw.simba.lsq.cli.cmd.rx.api;

import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.cmd.base.CmdLsqAnalyzeBase;
import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;

/**
 * Static analysis of queries
 *
 * @author raven
 *
 */
@Command(name = "analyze", description = "Analyze queries and emit structural features")
public class CmdLsqRxAnalyze
    extends CmdLsqAnalyzeBase
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        MainCliLsq.analyze(this);
        return 0;
    }
}
