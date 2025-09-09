package org.aksw.simba.lsq.core.rx.io.input;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.simba.lsq.core.LsqRdfizer;
import org.apache.jena.rdf.model.Resource;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class LsqLogRecordRdfizerFull
    implements LsqLogRecordRdfizer
{
    protected Function<String, SparqlStmt> sparqlStmtParser;
    protected String baseIri;
    protected String hostHashSalt;

    /** The service URL that received the queries that ended up in the log file. */
    protected String serviceUrl;
    protected Function<String, String> hashFn;

    public LsqLogRecordRdfizerFull(Function<String, SparqlStmt> sparqlStmtParser, String baseIri, String hostHashSalt,
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Function<String, SparqlStmt> sparqlStmtParser;
        private String baseIri;
        private String hostHashSalt;
        private String serviceUrl;
        private Function<String, String> hostHashFn;

        private Builder() {
            super();
        }

        public Builder setSparqlStmtParser(Function<String, SparqlStmt> sparqlStmtParser) {
            this.sparqlStmtParser = sparqlStmtParser;
            return this;
        }

        public Builder setBaseIri(String baseIri) {
            this.baseIri = baseIri;
            return this;
        }

        public Builder setHostHashSalt(String hostHashSalt) {
            this.hostHashSalt = hostHashSalt;
            return this;
        }

        public Builder setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
            return this;
        }

        public Builder setHostHashFn(Function<String, String> hostHashFn) {
            this.hostHashFn = hostHashFn;
            return this;
        }

        public LsqLogRecordRdfizerFull build() {
            Objects.requireNonNull(serviceUrl, "A service IRI that received the queries of the log must be set. Typically this should be the URL of the original SPARQL endpoint.");

            String finalBaseIri = Optional.ofNullable(baseIri).orElse("http://lsq.aksw.org/");
            Function<String, SparqlStmt> finalSparqlStmtParser = Optional.ofNullable(sparqlStmtParser).orElse(SparqlStmtParserImpl.create());

            Function<String, String> finalHostHashFn = Optional.ofNullable(hostHashFn).orElse(LsqLogRecordRdfizerFull::defaultHostHashFn);
            return new LsqLogRecordRdfizerFull(finalSparqlStmtParser, finalBaseIri, hostHashSalt, serviceUrl, finalHostHashFn);
        }
    }

    public static String defaultHostHashFn(String str) {
        return BaseEncoding.base64Url().omitPadding().encode(Hashing.sha256()
            .hashString(str, StandardCharsets.UTF_8)
            .asBytes());
    }
}
