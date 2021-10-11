package org.aksw.simba.lsq.cli.cmd.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.cmd.rx.api.CmdRdfIoBase;
import org.aksw.simba.lsq.cli.main.MainCliLsq;
import org.aksw.simba.lsq.cli.trash.CmdLsqRehashSparkImpl;

import picocli.CommandLine.Command;
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
@Command(name = "rehash", description = "Update IDs of lsq query objects generated with older versions of LSQ")
public class CmdLsqSparkRehash
    extends CmdRdfIoBase
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        CmdLsqRehashSparkImpl.mainSpark(this);
        return 0;
    }
}
