package org.aksw.simba.lsq.cli.cmd.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * For a given {@link org.aksw.simba.lsq.model.LsqQuery} read out its hash, then
 * generate a new hash using the latest approach. If the new hash differes, then
 * replace any occurrences of the hash in any of the underlying model's RDF
 * terms with the new hash
 *
 * @author raven
 *
 */
//@Command(name = "rehash", description = "Update IDs of lsq query objects generated with older versions of LSQ")
//public class CmdLsqRehash implements Callable<Integer> {
//    @Option(names = { "-h", "--help" }, usageHelp = true)
//    public boolean help = false;
//
//    @Parameters(arity = "0..*", description = "file-list to probe")
//    public List<String> nonOptionArgs = new ArrayList<>();
//
//    @Override
//    public Integer call() throws Exception {
//        MainCliLsq.rehash(this);
//        return 0;
//    }
//}
