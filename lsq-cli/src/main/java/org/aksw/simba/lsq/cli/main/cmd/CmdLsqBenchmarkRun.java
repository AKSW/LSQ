package org.aksw.simba.lsq.cli.main.cmd;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import com.google.common.base.StandardSystemProperty;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "run", description = "Execute a benchmark run")
public class CmdLsqBenchmarkRun
    implements Callable<Integer>
{
    @Option(names={"-h", "--help"}, help=true)
    public boolean help = false;

    @Option(names= {"-c", "--config"}, required=true, description="Configuration file (RDF)")
    public String config = null;

    @Option(names= {"--tdb"}, description="Base path to the TDB2 database directory for indexing benchmark results", showDefaultValue = Visibility.ALWAYS)
    public Path tdb2BasePath = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value()).resolve("lsq");

    @Parameters(arity = "1..*", paramLabel="FILE", description="Log files to process")
    public List<String> logSources = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        MainCliLsq.benchmarkExecute(this);
        return 0;
    }


}

