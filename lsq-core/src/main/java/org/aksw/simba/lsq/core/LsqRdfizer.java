package org.aksw.simba.lsq.core;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.util.iri.PrefixMappingTrie;
import org.aksw.simba.lsq.core.util.Skolemize;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;

public class LsqRdfizer {


    /**
     * Extends a list of prefix sources by adding a snapshot of prefix.cc
     *
     * @param prefixSources
     * @return
     */
    public static Iterable<String> prependDefaultPrefixSources(Iterable<String> prefixSources) {
        Iterable<String> sources = Iterables.concat(
                Collections.singleton("rdf-prefixes/prefix.cc.2019-12-17.jsonld"),
                Optional.ofNullable(prefixSources).orElse(Collections.emptyList()));
        return sources;
    }

    public static SparqlStmtParser createSparqlParser(Iterable<String> prefixSources) {
        PrefixMapping prefixMapping = new PrefixMappingTrie();
        for(String source : prefixSources) {
            PrefixMapping tmp = RDFDataMgr.loadModel(source);
            prefixMapping.setNsPrefixes(tmp);
        }

        SparqlStmtParser result = SparqlStmtParserImpl.create(
                Syntax.syntaxARQ, null, prefixMapping, true);

        return result;
    }



    /**
     * If a log entry could be processed without error, it is assumed that there is a
     * LSQ.query property available.
     * LSQ.query is a raw query, i.e. it may not be parsable as is because namespaces may be implicit
     *
     *
     * @param r A log entry resource
     * @param sparqlStmtParser
     */
    public static SparqlStmtQuery getParsedQuery(Resource r, Function<String, SparqlStmt> sparqlStmtParser) {

        SparqlStmtQuery result = null; // the parsed query string - if possible
        // logger.debug(RDFDataMgr.write(out, dataset, lang););

        // Extract the raw query string and add it with the lsq:query property to the log entry resource
        WebLogParser.extractRawQueryString(r);

        // If the resource is null, we could not parse the log entry
        // therefore count this as an error

        boolean logLineSuccessfullyParsed = r.getProperty(LSQ.processingError) == null;

        if(logLineSuccessfullyParsed) {
            Optional<String> str = Optional.ofNullable(r.getProperty(LSQ.query))
                    .map(queryStmt -> queryStmt.getString());

            //Model m = ResourceUtils.reachableClosure(r);
            SparqlStmt stmt = str
                    .map(sparqlStmtParser)
                    .orElse(null);

            if(stmt != null && stmt.isQuery()) {
                if(stmt.isParsed()) {
                    SparqlStmtUtils.optimizePrefixes(stmt);
//                    SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();

                    //result = queryStmt.getQuery();
                    //String queryStr = Objects.toString(query);
                    //r.addLiteral(LSQ.text, queryStr);
                }

                result = stmt.getAsQueryStmt();
            }
        }

        return result;
    }



    public static Optional<Resource> rdfizeLogRecord(
            Function<String, SparqlStmt> sparqlStmtParser,
            String baseIri,
            String hostHashSalt,
            String serviceUrl,
            Function<String, String> hashFn,
            Resource x) {
        RemoteExecution re = x.as(RemoteExecution.class);

        // If we cannot obtain a query from the log record, we omit the entry
        Optional<Resource> result;

        // Invert; map from query to log entry
        //ResourceInDataset queryInDataset = x.wrapCreate(Model::createResource);
        Resource queryInDataset = re.getModel().createResource();

        LsqQuery q = queryInDataset.as(LsqQuery.class);
        q.getRemoteExecutions().add(re);

            //try {
        SparqlStmtQuery parsedQuery = getParsedQuery(x, sparqlStmtParser);
        if(parsedQuery != null) {
            String str = parsedQuery.isParsed()
                    ? parsedQuery.getQuery().toString()
                    : parsedQuery.getOriginalString();

            //String queryHash = hashFn.apply(str); // Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
            q.setQueryAndHash(str);
            //q.setHash(queryHash);

            Throwable t = parsedQuery.getParseException();
            if(t != null) {
                q.setParseError(t.toString());
            }

            // Map<Resource, Resource> remap = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.renameResources(baseIri, renames);



            // String graphAndResourceIri = "urn:lsq:query:sha256:" + hash;
            // String graphAndResourceIri = baseIri + "q-" + queryHash;
            // Note: We could also leave the resource as a blank node
            // The only important part is to have a named graph with the query hash
            // in order to merge records about equivalent queries
            // qq = ResourceInDatasetImpl.renameResource(qq, graphAndResourceIri);


            /*
             * Generate IRI for log record
             */

            //if(host != null) {

            // http://www.example.org/sparql -> example.org-sparql

            // Hashing.sha256().hashString(hostHash, StandardCharsets.UTF_8);

            String host = re.getHost();
            String hostHash = host == null
                    ? null
                    : hashFn.apply(hostHashSalt + host);

            // FIXME Respect the noHoshHash = true flag
            re.setHostHash(hostHash);
            re.setHost(null);

            re.setEndpointUrl(serviceUrl);

//            Calendar timestamp = re.getTimestamp();


//            String reIri = baseIri + "re-" + logEntryId;
//            org.apache.jena.util.ResourceUtils.renameResource(re, reIri);


//            HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(q);
//            Map<Node, Node> renames = hashIdCxt.getNodeMapping(baseIri);
//            Node newRoot = renames.get(q.asNode());
//
//            // Also rename the original graph name to match the IRI of the new lsq query root
//            renames.put(NodeFactory.createURI(queryInDataset.getGraphName()), newRoot);
//
//            Dataset dataset = queryInDataset.getDataset();
//            // Apply an in-place node transform on the dataset
//            // queryInDataset = ResourceInDatasetImpl.applyNodeTransform(queryInDataset, NodeTransformLib2.makeNullSafe(renames::get));
//            NodeTransformLib2.applyNodeTransform(NodeTransformLib2.makeNullSafe(renames::get), dataset);
//            result = Maybe.just(new ResourceInDatasetImpl(dataset, newRoot.getURI(), newRoot));

            Resource r = Skolemize.skolemize(queryInDataset, baseIri, LsqQuery.class, (newRoot, renames) -> {
                Optional.ofNullable(renames.get(re.asNode()))
                    .map(newRoot.getModel()::wrapAsResource)
                    .ifPresent(newRe -> newRe.as(RemoteExecution.class).setSequenceId(null));
            });

            // After skolemization the sequence id is no longer needed
            // Remove it from the skolemized executon

            result = Optional.of(r);

//            RDFDataMgr.write(System.out, dataset, RDFFormat.NQUADS);

            // qq = ResourceInDatasetImpl.renameGraph(qq, graphAndResourceIri);


        } else {
            re.setSequenceId(null);

            result = Optional.empty();
        }


        //LsqUtils.postProcessSparqlStmt(x, sparqlStmtParser);
//	        	} catch(Exception e) {
//	                qq.addLiteral(LSQ.processingError, e.toString());
//	        	}

            // Remove text and query properties, as LSQ.text is
            // the polished one
            // xx.removeAll(LSQ.query);
            // xx.removeAll(RDFS.label);

            return result;
    }

}
