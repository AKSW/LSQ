package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
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
