package org.aksw.simba.lsq.model;

import java.util.Calendar;

import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RemoteExecution
    extends Resource
{
    @StringId
    default String getStringId(HashIdCxt cxt) {

        String serviceUrl = getEndpointUrl();

        String serviceId = serviceUrl == null
                ? "unknown-service"
                : UriToPathUtils.resolvePath(serviceUrl).toString()
                .replace('/', '-');

        Long seqId = getSequenceId();
        Calendar timestamp = getTimestamp();
        // TODO If there is a timestamp then use it
        // Otherwise, use sourceFileName + sequenceId
        String logEntryId = serviceId + "_" + (timestamp != null
                ? timestamp.toInstant().toString()
                : seqId);

        // String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // "remoteExecution"
        String prefix = "remoteExec";
        String result = prefix + "-" + logEntryId;

        return result;
    }

    @Iri(LSQ.Strs.host)
    @HashId
    String getHost();
    RemoteExecution setHost(String host);

    @Iri(LSQ.Strs.userAgent)
    String getUserAgent();
    RemoteExecution setUserAgent(String userAgent);

    @Iri(LSQ.Strs.hostHash)
    @HashId
    String getHostHash();
    RemoteExecution setHostHash(String hostHash);

    @Iri(LSQ.Strs.atTime)
    @HashId
    Calendar getTimestamp();
    RemoteExecution setTimestamp(Calendar calendar);

    @Iri(LSQ.Strs.endpoint)
    @IriType
    @HashId
    String getEndpointUrl();
    RemoteExecution setEndpointUrl(String endpointUrl);

    @Iri(LSQ.Strs.sequenceId)
    @HashId
    Long getSequenceId();
    RemoteExecution setSequenceId(Long value);
}
