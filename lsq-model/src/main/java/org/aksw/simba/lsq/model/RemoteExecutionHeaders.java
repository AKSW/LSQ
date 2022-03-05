package org.aksw.simba.lsq.model;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@HashId
public interface RemoteExecutionHeaders
    extends Resource
{
    @Inverse
    @Iri(LSQ.Terms.headers)
    @HashId(excludeRdfProperty = true)
    RemoteExecution getRemoteExecution();

    @StringId
    default String getStringId(HashIdCxt cxt) {
        RemoteExecution remoteExecution = getRemoteExecution();
        String prefix = "remoteExecHeaders";
        String result = prefix + "-" + RemoteExecution.getLogEntryId(remoteExecution);
        return result;
    }
}
