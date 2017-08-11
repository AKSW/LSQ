package org.aksw.simba.lsq.core;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.beast.vocabs.PROV;
import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;


/**
 * Reads a log entry resource and yields a resource for the query model
 *
 * @author raven
 *
 */
public class LsqProcessor
    implements Function<Resource, Resource>
{
    private static final Logger logger = LoggerFactory.getLogger(LsqProcessor.class);


    protected Function<String, SparqlStmt> stmtParser;
    protected SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh:mm:ss");


    protected boolean reuseLogIri;

    // Config attributes
    protected String baseUri;
    protected boolean isRdfizerQueryStructuralFeaturesEnabled;
    protected boolean isRdfizerQueryExecutionEnabled;
    protected boolean isRdfizerQueryLogRecordEnabled;
    //protected boolean isQueryExecutionRemote;

    protected Pattern queryIdPattern;

    public Pattern getQueryIdPattern() {
        return queryIdPattern;
    }

    public void setQueryIdPattern(Pattern queryIdPattern) {
        this.queryIdPattern = queryIdPattern;
    }

    protected Long workloadSize;

    //protected Function<String, NestedResource> queryAspectFn;
    //protected Resource rawLogEndpointRes; // TODO Rename to remoteEndpointRes?


    protected QueryExecutionFactory dataQef;
    protected String datasetLabel;
    protected Resource expRes;
    protected String datasetEndpointUri;
    protected Long datasetSize;


    // Processing state variables
    protected int logFailCount = 0;
    protected long logEntryIndex = 0l;
    protected int batchSize = 10;

    public Function<String, SparqlStmt> getStmtParser() {
        return stmtParser;
    }

    public void setStmtParser(Function<String, SparqlStmt> stmtParser) {
        this.stmtParser = stmtParser;
    }

    public SimpleDateFormat getDt() {
        return dt;
    }

    public void setDt(SimpleDateFormat dt) {
        this.dt = dt;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isReuseLogIri() {
        return reuseLogIri;
    }

    public void setReuseLogIri(boolean reuseLogIri) {
        this.reuseLogIri = reuseLogIri;
    }

    public boolean isRdfizerQueryStructuralFeaturesEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }

    public void setRdfizerQueryStructuralFeaturesEnabled(boolean isRdfizerQueryStructuralFeaturesEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryStructuralFeaturesEnabled;
    }

    public boolean isRdfizerQueryExecutionEnabled() {
        return isRdfizerQueryExecutionEnabled;
    }

    public void setRdfizerQueryExecutionEnabled(boolean isRdfizerQueryExecutionEnabled) {
        this.isRdfizerQueryExecutionEnabled = isRdfizerQueryExecutionEnabled;
    }

    public boolean isRdfizerQueryLogRecordEnabled() {
        return isRdfizerQueryLogRecordEnabled;
    }

    public void setRdfizerQueryLogRecordEnabled(boolean isRdfizerQueryLogRecordEnabled) {
        this.isRdfizerQueryLogRecordEnabled = isRdfizerQueryLogRecordEnabled;
    }

//    public boolean isQueryExecutionRemote() {
//        return isQueryExecutionRemote;
//    }
//
//    public void setQueryExecutionRemote(boolean isQueryExecutionRemote) {
//        this.isQueryExecutionRemote = isQueryExecutionRemote;
//    }

    public Long getWorkloadSize() {
        return workloadSize;
    }

    public void setWorkloadSize(Long workloadSize) {
        this.workloadSize = workloadSize;
    }

//    public Function<String, NestedResource> getQueryAspectFn() {
//        return queryAspectFn;
//    }
//
//    public void setQueryAspectFn(Function<String, NestedResource> queryAspectFn) {
//        this.queryAspectFn = queryAspectFn;
//    }

//    public Resource getRawLogEndpointRes() {
//        return rawLogEndpointRes;
//    }
//
//    public void setRawLogEndpointRes(Resource rawLogEndpointRes) {
//        this.rawLogEndpointRes = rawLogEndpointRes;
//    }

    public QueryExecutionFactory getDataQef() {
        return dataQef;
    }

    public void setDataQef(QueryExecutionFactory dataQef) {
        this.dataQef = dataQef;
    }

    public String getDatasetLabel() {
        return datasetLabel;
    }

    public void setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
    }

    public Resource getExpRes() {
        return expRes;
    }

    public void setExpRes(Resource expRes) {
        this.expRes = expRes;
    }

    public String getDatasetEndpointUri() {
        return datasetEndpointUri;
    }

    public void setDatasetEndpointUri(String datasetEndpointUrl) {
        this.datasetEndpointUri = datasetEndpointUrl;
    }

    public Long getDatasetSize() {
        return datasetSize;
    }

    public void setDatasetSize(Long datasetSize) {
        this.datasetSize = datasetSize;
    }

    public int getLogFailCount() {
        return logFailCount;
    }

    public void setLogFailCount(int logFailCount) {
        this.logFailCount = logFailCount;
    }

    public long getLogEntryIndex() {
        return logEntryIndex;
    }

    public void setLogEntryIndex(long logEntryIndex) {
        this.logEntryIndex = logEntryIndex;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public static Logger getLogger() {
        return logger;
    }

//    pu LsqProcessor() {
//        super();
//    }

    @Override
    public Resource apply(Resource r) {

        // logger.debug(RDFDataMgr.write(out, dataset, lang););

        // Extract the query and add it with the lsq:query property
        WebLogParser.extractQuery(r);

        // If the resource is null, we could not parse the log entry
        // therefore count this as an error

        boolean fail = false;
        boolean parsed = r.getProperty(LSQ.processingError) == null ? true : false;

        Resource result = null;

        try {
            if(parsed) {
                Optional<String> str = Optional.ofNullable(r.getProperty(LSQ.query))
                        .map(queryStmt -> queryStmt.getString());

                //Model m = ResourceUtils.reachableClosure(r);
                SparqlStmt stmt = str
                        .map(stmtParser)
                        .orElse(null);

                if(stmt != null && stmt.isQuery()) {

                    SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();

                    String queryStr;
                    Query query;
                    if(!queryStmt.isParsed()) {
                        query = null;
                        queryStr = queryStmt.getOriginalString();
                    } else {
                        query = queryStmt.getQuery();
                        queryStr = "" + queryStmt.getQuery();
                    }

//                    long rnd = (new Random()).nextLong();
//                    query = QueryFactory.create("SELECT COUNT(*) { ?s ?p ?o . ?o ?x ?y } Order By ?y Limit 10", Syntax.syntaxARQ);
//                    queryStr = "" + query;

                    if(logEntryIndex % batchSize == 0) {
                        long batchEndTmp = logEntryIndex + batchSize;
                        long batchEnd = workloadSize == null ? batchEndTmp : Math.min(batchEndTmp, workloadSize);
                        logger.info("Processing query batch from " + logEntryIndex + " - "+ batchEnd); // + ": " + queryStr.replace("\n", " ").substr);
                    }


                    Model queryModel = ModelFactory.createDefaultModel();


                    //Resource logEndpointRes = rawLogEndpointRes == null ? null : rawLogEndpointRes.inModel(queryModel);

                    //NestedResource baseRes = new NestedResource(generatorRes).nest(datasetLabel).nest("-");

                    NestedResource baseRes = new NestedResource(queryModel.createResource(baseUri));

                    Function<String, NestedResource> queryAspectFn;
                    NestedResource queryRes;
                    if(reuseLogIri && r.isURIResource()) {
                        // Check whether to parse out a specific part of the logIri to use as the queryHash
                        if(queryIdPattern != null) {
                            String uriStr = r.getURI();
                            Matcher m = queryIdPattern.matcher(uriStr);
                            if(m.find()) {
                                // TODO This is the same code as below, maybe we can reduce redundancy
                                String queryHash = m.group(1);
                                //System.out.println("Matched query id: " + queryHash);
                                logger.debug("Matched query id: " + queryHash + " with pattern " + queryIdPattern + " in " + uriStr);
                                queryRes = baseRes.nest("q-" + queryHash);
                                queryAspectFn = (aspect) -> baseRes.nest(aspect + "-").nest("q-" + queryHash);
                            } else {
                                throw new RuntimeException("No match for a query id with pattern " + queryIdPattern + " in '" + uriStr + "'" );
                            }
                        } else {

                            queryRes = new NestedResource(r);

                            queryAspectFn = (aspect) -> queryRes.nest("-" + aspect); //.nest("q-" + queryHash);
                        }
                    } else {

                        String queryHash = StringUtils.md5Hash(queryStr).substring(0, 8);
                        queryRes = baseRes.nest("q-" + queryHash);
                        queryAspectFn = (aspect) -> baseRes.nest(aspect + "-").nest("q-" + queryHash);
                    }

                    result = queryRes.get();


                    result
                        .addProperty(RDF.type, LSQ.Query)
                        //.addLiteral(LSQ.text, ("" + queryStr).replace("\n", " "));
                        .addLiteral(LSQ.text, ("" + queryStr).replace("\n", " "));


                    if(!queryStmt.isParsed()) {
                        String msg = queryStmt.getParseException().getMessage();
                        result
                            .addLiteral(LSQ.parseError, msg);
                    } else {
                        if(isRdfizerQueryStructuralFeaturesEnabled) {
                            rdfizeQueryStructuralFeatures(result, queryAspectFn, query);
                        }
                    }

                    if(isRdfizerQueryLogRecordEnabled) {
                        rdfizeLogRecord(baseRes, r, queryRes, queryAspectFn);
                    }


                    if(isRdfizerQueryExecutionEnabled) {
//                        if(isQueryExecutionRemote) {
//                            rdfizeLogRecord(baseRes, r, queryRes, queryAspectFn);
//                        } else {
                            doLocalExecution(query, queryRes, queryAspectFn);
//                        }
                    }


                    // Post processing: Craft global IRIs for SPIN variables
                    Set<Statement> stmts = queryModel.listStatements(null, SP.varName, (RDFNode)null).toSet();
                    for(Statement st : stmts) {
                        Resource s = st.getSubject();
                        String varName = st.getLiteral().getString();
                        //String varResUri = baseRes.nest("var-").nest(varName).str();
                        String varResUri = queryRes.nest("-var-").nest(varName).str();
                        ResourceUtils.renameResource(s, varResUri);
                    }

                    // Post processing: Remove skolem identifiers
                    queryModel.removeAll(null, Skolemize.skolemId, null);


                    //RDFDataMgr.write(out, queryModel, outFormat);

                    // TODO Frequent flushing may decrease performance
                    // out.flush();
                } else {
                    logger.debug("Skipping non-sparql-query log entry #" + logEntryIndex);
                    logger.debug(toString(r));
                }

            } else {
                ++logFailCount;
                double ratio = logEntryIndex == 0 ? 0.0 : logFailCount / logEntryIndex;
                if(logEntryIndex == 10 && ratio > 0.8) {
                    fail = true;
                }

                logger.warn("Skipping non-parsable log entry #" + logEntryIndex);
                logger.warn(toString(r));
            }

        } catch(Exception e) {
            logger.warn("Unexpected exception encountered at item " + logEntryIndex + " - ", e);
            logger.warn(toString(r));
        }

        if(fail) {
            throw new RuntimeException("Encountered too many non processable log entries. Probably not a log file.");
        }

        //.write(System.err, "TURTLE");
        ++logEntryIndex;

        return result;
    }


    public void doLocalExecution(Query query, NestedResource queryRes, Function<String, NestedResource> queryAspectFn) {
        //boolean hasBeenExecuted = executedQueries.contains(query);

        boolean hasBeenExecuted = false;
        if(!hasBeenExecuted) {
            //executedQueries.add(query);


            Calendar now = Calendar.getInstance();
            String nowStr = dt.format(now.getTime());
            Resource queryExecRes = queryAspectFn.apply("le-" + datasetLabel).nest("-" + nowStr).get();

            if(expRes != null) {
                queryExecRes
                    .addProperty(PROV.wasGeneratedBy, expRes);
            }

            // TODO Switch between local / remote execution
            if(query != null) {
                queryRes.get()
                    .addProperty(LSQ.hasLocalExec, queryExecRes);

                rdfizeQueryExecution(queryRes.get(), query, queryExecRes, dataQef, datasetSize);
            }
        }
    }

    public void rdfizeLogRecord(NestedResource baseRes, Resource r, NestedResource queryRes, Function<String, NestedResource> queryAspectFn) {

        // Deal with log entry (remote execution)
        String hashedIp = StringUtils.md5Hash("someSaltPrependedToTheIp" + r.getProperty(LSQ.host).getString()).substring(0, 16);

        Resource agentRes = baseRes.nest("agent-" + hashedIp).get();


        Literal timestampLiteral = r.getProperty(PROV.atTime).getObject().asLiteral();
        Calendar timestamp = ((XSDDateTime)timestampLiteral.getValue()).asCalendar();
        String timestampStr = dt.format(timestamp.getTime());
        //String timestampStr = StringUtils.md5Hash("someSaltPrependedToTheIp" + r.getProperty(LSQ.host).getString()).substring(0, 16);

        Resource queryExecRecRes = queryAspectFn.apply("re-" + datasetLabel).nest("-" + hashedIp + "-" + timestampStr).get();

        // Express that the query execution was recorded
        // at some point in time by some user at some service
        // according to some source (e.g. the log file)
        queryRes.get()
            .addProperty(LSQ.hasRemoteExec, queryExecRecRes);

        queryExecRecRes
            //.addProperty(RDF.type, LSQ.)
            .addLiteral(PROV.atTime, timestampLiteral) //.inModel(queryModel))
            .addProperty(PROV.wasAssociatedWith, agentRes)
            ;

        if(datasetEndpointUri != null) {
            Resource tmp = queryExecRecRes.getModel().createResource(datasetEndpointUri);
            queryExecRecRes.addProperty(LSQ.endpoint, tmp); // TODO Make it possible to specify the dataset configuration that was used to execute the query
        }
    }

    public static void rdfizeQueryStructuralFeatures(Resource queryRes, Function<String, NestedResource> queryAspectFn, Query query) {

        //Resource execRes = queryAspectFn.apply("exec").nest("-execX").get();


        try {
            query = query.cloneQuery();
            query.getGraphURIs().clear();

            Resource spinRes = queryAspectFn.apply("spin").get();
//          queryNo++;

        // .. generate the spin model ...
            //Model spinModel = queryRes.getModel();
            Model spinModel = ModelFactory.createDefaultModel();
          LSQARQ2SPIN arq2spin = new LSQARQ2SPIN(spinModel);
          Resource tmpSpinRes = arq2spin.createQuery(query, null);



//          System.out.println("TEST {");
//          SpinUtils.indexTriplePatterns2(tmpSpinRes.getModel()).forEach(System.out::println);
//          SpinUtils.itp(tmpSpinRes).forEach(System.out::println);
//          System.out.println("}");
//tmpSpinRes.as(org.topbraid.spin.model.Query.class).getWhereElements().forEach(e -> {
//    System.out.println("XXElement: " + e.asResource().getId() + ": " + e);
//});


          // ... and rename the blank node of the query
          ResourceUtils.renameResource(tmpSpinRes, spinRes.getURI());

          spinRes.getModel().add(spinModel);

          // ... and skolemize the rest
          Skolemize.skolemize(spinRes);

          queryRes.addProperty(LSQ.hasSpin, spinRes);


//          } catch (Exception ex) {
//              String msg = ex.getMessage();// ExceptionUtils.getFullStackTrace(ex);
//              queryRes.addLiteral(LSQ.runtimeError, msg);
//
//              // TODO Port the getRDFUserExceptions function
//              // String queryStats =
//              // this.getRDFUserExecutions(queryToSubmissions.get(queryStr),separator);
//              parseErrorCount++;
//          }

            //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            Resource featureRes = queryAspectFn.apply("sf").get(); // model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash

            queryRes.addProperty(LSQ.hasStructuralFeatures, featureRes);


            // Add used features
            QueryStatistics2.enrichResourceWithQueryFeatures(featureRes, query);

            if(query.isSelectType()) {
                featureRes.addLiteral(LSQ.projectVars, query.getProjectVars().size());
            }

//            Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
//            features.forEach(f -> featureRes.addProperty(LSQ.usesFeature, f));

            // TODO These methods have to be ported
            //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

            SpinUtils.enrichWithHasTriplePattern(featureRes, spinRes);
            SpinUtils.enrichWithTriplePatternText(spinRes);
            //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);

            //
            QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, featureRes);

            QueryStatistics2.enrichWithPropertyPaths(featureRes, query);


            // TODO Move to a util function
            Set<Resource> serviceUris = spinModel.listStatements(null, SP.serviceURI, (RDFNode)null)
                    .mapWith(stmt -> stmt.getObject().asResource()).toSet();

            for(Resource serviceUri : serviceUris) {
                featureRes.addProperty(LSQ.usesService, serviceUri);
            }




            //QueryStatistics2.enrichWithMentions(featureRes, query); //the mentions subjects, predicates and objects can be obtained from Spin


        } catch (Exception ex) {
            String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
            queryRes.addLiteral(LSQ.processingError, msg);
            logger.warn("Failed to process query " + query, ex);
        }

        // TODO Add getRDFUserExecutions
    }



    public static void rdfizeQueryExecution(Resource queryRes, Query query, Resource queryExecRes, QueryExecutionFactory qef, Long datasetSize) {

        try {
    //        Stopwatch sw = Stopwatch.createStarted();
    //      long runtimeInMs = sw.stop().elapsed(TimeUnit.MILLISECONDS);



            Calendar start = Calendar.getInstance();
            //long resultSetSize = QueryExecutionUtils.countQuery(query, qef);
//System.out.println(query);
            QueryExecution qe = qef.createQueryExecution(query);
            long resultSetSize = QueryExecutionUtils.consume(qe);

            Calendar end = Calendar.getInstance();
            Duration duration = Duration.between(start.toInstant(), end.toInstant());


            queryExecRes
                .addLiteral(LSQ.resultSize, resultSetSize)
                .addLiteral(LSQ.runTimeMs, duration.getNano() / 1000000l)
                //.addLiteral(PROV.startedAtTime, start)
                //.addLiteral(PROV.endAtTime, end)
                ;

            SpinUtils.enrichModelWithTriplePatternExtensionSizes(queryRes, queryExecRes, qef);


            BiMap<org.topbraid.spin.model.Triple, Resource> tpToTpExecRess = SpinUtils.createTriplePatternExecutions(queryRes, queryExecRes);
            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinUtils.indexBasicPatterns2(queryRes);

            Collection<org.topbraid.spin.model.Triple> tps = bgpToTps.values();

            // Note: We assume that each var only originates from a single resource - which is the case for lsq
            // In general, we would have to use a multimap
            Map<Var, Resource> varToQueryVarRes = tps.stream()
                    .flatMap(tp -> SpinUtils.indexTripleNodes2(tp).entrySet().stream())
                    .filter(e -> e.getValue().isVariable())
                    .collect(Collectors.toMap(
                            e -> (Var)e.getValue(),
                            e -> e.getKey().asResource(),
                            (old, now) -> now));

            if(datasetSize != null) {

                SpinUtils.enrichModelWithTriplePatternSelectivities(tpToTpExecRess.values(), qef, datasetSize);

                SpinUtils.enrichModelWithBGPRestrictedTPSelectivities(qef, queryExecRes.getModel(), bgpToTps);
            }


            // For the id part, we can index the structural bgps, tps, joinVars
            //

            // BGP restricted triple pattern selectivity
            // These selectivities can be related directly to the TP executions
            // - [[bgp-vars]] / [[tp-vars]]

            // Join restricted triple pattern selectivity
            // For these selectivities we need to introduce _observation_ resources for
            // (1) each (join) var in the bgp
            // (2) each (join) var in a tp
            // - [[join-bgp-var]] / [[tp-var]]

            // Note, that there are resources for structural information on (join) bgp-vars
            // but I suppose there is no structural information for tp-vars
            // So the latter does not have a correspondence - TODO: how are var-resources allocated by topbraid's spin?
            //
            // We also need an API for working with LSQ data, otherwise it seems to me its too
            // cumbersome - for this we need some use cases, such as

            // - getLatestObservation(bgpRes); but instead of java methods,
            // we would also benefit from more powerful navigation and filtering of resources:
            // bgpRes.as(ResourceEnh.class).in(LSQ.onBGP).orderBy(LSQ.date).first()
            // .startOrder().newItem().in(LSQ.date).endItem().endOrder()
            // - getBGPStats(bgpRes)


            // For each variable in the BGP create a new resource
            for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {


                // Map each resource to the corresponding jena element
                Map<org.topbraid.spin.model.Triple, Element> resToEl = e.getValue().stream()
                        .collect(Collectors.toMap(
                                r -> r,
                                r -> ElementUtils.createElement(SpinUtils.toJenaTriple(r))));


                Set<Var> bgpVars = resToEl.values().stream()
                        .flatMap(el -> PatternVars.vars(el).stream())
                        .collect(Collectors.toSet());


                //String queryId = "";
                String bgpId = e.getKey().getProperty(Skolemize.skolemId).getString();

                Resource bgpCtxRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-bgp-" + bgpId);

                Map<Var, Resource> varToBgpVar = bgpVars.stream()
                        .collect(Collectors.toMap(
                                v -> v,
                                v -> NestedResource.from(bgpCtxRes).nest("-var-").nest(v.getName()).get()));

                // Link the var occurrence
                varToBgpVar.values().forEach(vr -> bgpCtxRes.addProperty(LSQ.hasVarExec, vr));

                queryExecRes.addProperty(LSQ.hasBGPExec, bgpCtxRes);

                // Obtain the selectivity for the variable in that tp
                //for(e.getValue())
                Map<Var, Long> varToCount = QueryStatistics2.fetchCountJoinVarGroup(qef, resToEl.values());


                // Add the BGP var statistics
                varToCount.forEach((v, c) -> {
                    Resource queryVarRes = varToQueryVarRes.get(v);
                    //System.out.println("queryVar: " + queryVar);

                    Resource bgpVar = varToBgpVar.get(v);

                    bgpVar.addLiteral(LSQ.resultSize, c);
                    bgpVar.addProperty(LSQ.proxyFor, queryVarRes);
                });
                    //
                    //vr.addLiteral(LSQ.tpSelectivity, o);


                Map<org.topbraid.spin.model.Triple, Map<Var, Long>> elToVarToCount = QueryStatistics2.fetchCountJoinVarElement(qef, resToEl);

                elToVarToCount.forEach((t, vToC) -> {
                    Map<Node, RDFNode> varToRes = SpinUtils.indexTripleNodes(t);

                    Resource execTp = tpToTpExecRess.get(t);

                    String tpId = e.getKey().getProperty(Skolemize.skolemId).getString();

                    //String tpResBase = queryExecRes.getURI() + "-tp-" + tpId;
                    String tpResBase = execTp.getURI();

                    vToC.forEach((v, c) -> {
                        Resource execTpVarRes = queryExecRes.getModel().createResource(tpResBase + "-var-" + v.getName());

                        long bgpJoinVarCount = varToCount.get(v);

                        double tpSelJoinVarRestricted = c == 0 ? 0d : bgpJoinVarCount / (double)c;
                        execTpVarRes
                            .addLiteral(LSQ.resultSize, c)
                            .addLiteral(LSQ.tpSelJoinVarRestricted, tpSelJoinVarRestricted)
                            .addProperty(LSQ.hasVar, varToRes.get(v))
                            //.addLiteral(LSQ.hasVar, )
                            ;

                        execTp.addProperty(LSQ.hasJoinVarExec, execTpVarRes);
                    });

                    //Resource tpRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-" + tpId);


                });

//                    System.out.println(varToCount);
//                    System.out.println(elToVarToCount);


                /*
                bgp hasTp tp1
                tp1 hasJoinVar tp1-jv-x
                tp1-jv-x hasEval e1-tp1-jv-x
                e1-tp1-jv-x selectivity 0.5
                e1 inExperiment/onDataset DBpedia




                */
            }

            // For each triple in the BGP create a new resource


            // We need to create observation resources for each variable in each bgp
            for(Entry<Resource, org.topbraid.spin.model.Triple> e : bgpToTps.entries()) {
                org.topbraid.spin.model.Triple t = e.getValue();
                org.apache.jena.graph.Triple tr = SpinUtils.toJenaTriple(t);

                Set<Var> vars;

                // allocate iris for each variable in the bgp
                queryExecRes.getModel().createResource(queryExecRes.getURI() + "");


                //queryExecRes + tp-id + var




                // create the



            }
            //bgpToTps = SpinUtils.indexBasicPatterns2(queryRes);



            // Now create the resources for stats on the join vars
            //Set<Resource> joinVarRess = SpinUtils.createJoinVarObservations(queryRes, queryExecRes);

            // And now compute selectivities of the join variables
            //SpinUtils.enrichModelWithJoinRestrictedTPSelectivities(qef, queryExecRes.getModel(), bgpToTps);




            //SpinUtils.enrichModelWithTriplePatternSelectivities(queryRes, queryExecRes, qef, datasetSize); //subModel, resultSetSize);

            //SpinUtils.enrichModelWithBGPRestrictedTPSelectivities(queryRes, queryExecRes, qef, totalTripleCount);


            //  queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            //long resultSize = QueryExecutionUtils.countQuery(query, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
        }
        catch(Exception e) {
            Throwable f = ExceptionUtilsAksw.unwrap(e, QueryExceptionHTTP.class).orElse(e);
            String msg = f.getMessage();
            msg = msg == null ? "" + ExceptionUtils.getStackTrace(f) : msg;
            queryExecRes.addLiteral(LSQ.execError, msg);
            String queryStr = ("" + query).replace("\n", " ");
            logger.warn("Query execution exception [" + msg + "] for query " + queryStr, e);
        }
    }

    /**
     * Nicely format a resource.
     * Used for logging.
     *
     * @param r
     * @return
     */
    public static String toString(Resource r) {
        Model m = ResourceUtils.reachableClosure(r);
        m.setNsPrefixes(PrefixMapping.Extended);
        m.setNsPrefix("ex", "http://example.org/");
        m.setNsPrefix("lsq", LSQ.ns);
        m.setNsPrefix("prov", PROV.ns);
        String result = ModelUtils.toString(m, "TTL");
        return result;
    }

}
