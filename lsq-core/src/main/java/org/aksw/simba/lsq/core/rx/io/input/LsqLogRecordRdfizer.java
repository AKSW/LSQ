package org.aksw.simba.lsq.core.rx.io.input;

import java.util.function.Function;

import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.simba.lsq.core.LsqRdfizer;
import org.apache.jena.rdf.model.Resource;

public class LsqLogRecordRdfizer
    implements Function<Resource, Resource>
{
    protected Function<String, SparqlStmt> sparqlStmtParser;
    protected String baseIri;
    protected String hostHashSalt;
    protected String serviceUrl;
    protected Function<String, String> hashFn;

    public LsqLogRecordRdfizer(Function<String, SparqlStmt> sparqlStmtParser, String baseIri, String hostHashSalt,
            String serviceUrl, Function<String, String> hashFn) {
        super();
        this.sparqlStmtParser = sparqlStmtParser;
        this.baseIri = baseIri;
        this.hostHashSalt = hostHashSalt;
        this.serviceUrl = serviceUrl;
        this.hashFn = hashFn;
    }

    @Override
    public Resource apply(Resource logEntry) {
        return LsqRdfizer.rdfizeLogRecord(sparqlStmtParser, baseIri, hostHashSalt, serviceUrl, hashFn, logEntry).orElse(null);
    }
}
