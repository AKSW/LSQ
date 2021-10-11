package org.aksw.simba.lsq.cli.cmd.spark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.LsqCmdSparkImpls;
import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name="probe", description = "Probe log files for known formats")
public class CmdLsqSparkProbe
    implements Callable<Integer>
{
    @Option(names = {"-h", "--help"}, usageHelp = true)
    public boolean help = false;

//	@Parameter(names={"-m", "--format"}, description="Input log format")
//	public String inputLogFormat;
//
//	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
//	public List<String> prefixSources = new ArrayList<>();

    @Parameters(arity="1..*", description="file-list to probe")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        LsqCmdSparkImpls.probe(this);
        return 0;
    }

}