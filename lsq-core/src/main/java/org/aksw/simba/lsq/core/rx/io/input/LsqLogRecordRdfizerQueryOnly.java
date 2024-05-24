package org.aksw.simba.lsq.core.rx.io.input;

import java.util.function.Function;

import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.simba.lsq.core.LsqRdfizer;
import org.aksw.simba.lsq.core.util.SkolemizeBackport;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class LsqLogRecordRdfizerQueryOnly
    implements Function<Resource, Resource>
{
    protected Function<String, SparqlStmt> sparqlStmtParser;
    protected String baseIri;
    protected Function<String, String> hashFn;

    public LsqLogRecordRdfizerQueryOnly(Function<String, SparqlStmt> sparqlStmtParser, String baseIri, Function<String, String> hashFn) {
        super();
        this.sparqlStmtParser = sparqlStmtParser;
        this.baseIri = baseIri;
        this.hashFn = hashFn;
    }

    @Override
    public Resource apply(Resource logEntry) {
        // RemoteExecution re = logEntry.as(RemoteExecution.class);

        // If we cannot obtain a query from the log record, we omit the entry
        // Optional<Resource> result;
        Resource result;

        // Invert; map from query to log entry
        //ResourceInDataset queryInDataset = x.wrapCreate(Model::createResource);
        Resource queryInDataset = ModelFactory.createDefaultModel().createResource(); //new ResourceInDatasetImpl();  // re.getModel().createResource();

        LsqQuery q = queryInDataset.as(LsqQuery.class);

        SparqlStmtQuery parsedQuery = LsqRdfizer.getParsedQuery(logEntry, sparqlStmtParser);
        if(parsedQuery != null) {
            String str = parsedQuery.isParsed()
                    ? parsedQuery.getQuery().toString()
                    : parsedQuery.getOriginalString();

            q.setQueryAndHash(str);

            Throwable t = parsedQuery.getParseException();
            if(t != null) {
                q.setParseError(t.toString());
            }

            Resource r = SkolemizeBackport.skolemize(queryInDataset, baseIri, LsqQuery.class, (newRoot, renames) -> {
//                Optional.ofNullable(renames.get(re.asNode()))
//                    .map(newRoot.getModel()::wrapAsResource)
//                    .ifPresent(newRe -> newRe.as(RemoteExecution.class).setSequenceId(null));
            });

            result = r; //Optional.of(r);

        } else {
            // re.setSequenceId(null);
            result = null;
        }

        return result;
    }
}
