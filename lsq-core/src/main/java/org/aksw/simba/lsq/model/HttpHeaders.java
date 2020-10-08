package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

/**
 * A resource whose properties correspond to individual http headers.
 *
 * Note: The class name is purposely in plural because it refers to a collection of headers.
 *
 * @author raven
 *
 */
@ResourceView
@HashId
public interface HttpHeaders
    extends Resource
{
    @Iri(LSQ.Strs.headers)
    @Inverse
    @HashId(excludeRdfProperty = true)
    RemoteExecution getHttpRequestSpec();
    HttpHeaders setHttpRequestSpec(RemoteExecution httpRequestSpec);

    @StringId
    default String getStringId(HashIdCxt cxt) {
        RemoteExecution spec = getHttpRequestSpec();
        String queryId = cxt.getStringId(spec) + "-headers"; // cxt.getHashAsString(query);
        return queryId;
    }
}
