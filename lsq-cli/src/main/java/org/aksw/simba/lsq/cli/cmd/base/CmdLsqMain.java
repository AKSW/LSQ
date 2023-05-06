package org.aksw.simba.lsq.cli.cmd.base;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.aksw.commons.picocli.VersionProviderFromClasspathProperties;
import org.aksw.simba.lsq.cli.cmd.rx.api.CmdLsqRxParent;
import org.aksw.simba.lsq.cli.cmd.spark.api.CmdLsqSparkParent;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name="lsq", version="LSQ version", versionProvider = CmdLsqMain.LsqVersionProvider.class, description = "LSQ Subcommands", subcommands = {
        CmdLsqRxParent.class,
        CmdLsqSparkParent.class
        // CmdLsqSparkRehash.class
})
public class CmdLsqMain {
    public static class LsqVersionProvider extends VersionProviderFromClasspathProperties {
        public @Override String getResourceName() { return "lsq-core.properties"; }
        public @Override List<String> getStrings(Properties p) { return Arrays.asList(p.get("lsq-core.version") + " built at " + p.get("lsq-core.build.timestamp")); }
    }

    @Option(names={"-h", "--help"}, usageHelp = true, description = "Show general help")
    public boolean help = false;

    @Option(names={"-v", "--version"}, versionHelp = true, description = "Show version")
    public boolean version = false;

}
