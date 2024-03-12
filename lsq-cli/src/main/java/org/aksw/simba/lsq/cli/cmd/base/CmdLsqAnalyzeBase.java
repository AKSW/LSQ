package org.aksw.simba.lsq.cli.cmd.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.simba.lsq.cli.main.MainCliLsq;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CmdLsqAnalyzeBase {
    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    public EnricherSpec enricherSpec = new EnricherSpec();

    public static class EnricherSpec {
        @Option(names = { "--only" }, completionCandidates = CompletionCandidatesEnrichers.class)
        public List<String> inclusions = null;

        @Option(names = { "--exclude" }, completionCandidates = CompletionCandidatesEnrichers.class)
        public List<String> exclusions = null;

        public boolean isWhitelist() {
            return inclusions != null;
        }

        public List<String> getRawList() {
            return inclusions != null
                    ? inclusions
                    : exclusions != null
                        ? exclusions
                        : List.of();
        }

        public List<String> getEffectiveList() {
            List<String> rawList = getRawList();
            boolean isWhitelist = isWhitelist();
            List<String> result = MainCliLsq.effectiveList(rawList, isWhitelist, new ArrayList<>(LsqEnricherRegistry.get().getKeys()));
            return result;
        }
    }

    public static class CompletionCandidatesEnrichers
        implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LsqEnricherRegistry.get().getKeys().iterator();
        }
    }

    @Parameters(arity = "1..*", description = "file-list to probe")
    public List<String> nonOptionArgs = new ArrayList<>();
}
