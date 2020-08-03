package org.aksw.simba.lsq.core;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
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
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.cache.Cache;
import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;


/**
 * Reads a log entry resource and yields a resource for the query model
 *
 * @author raven
 *
 */
public class LsqProcessor
    //implements Function<Resource, Resource>
{
    private static final Logger logger = LoggerFactory.getLogger(LsqProcessor.class);

    // public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss");


    protected Function<String, SparqlStmt> stmtParser;
    protected SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");


    protected boolean reuseLogIri;

    // Config attributes
    protected String baseUri;
    protected boolean isRdfizerQueryStructuralFeaturesEnabled;
    protected boolean isRdfizerQueryExecutionEnabled;
    protected boolean isRdfizerQueryLogRecordEnabled;
    //protected boolean isQueryExecutionRemote;

    protected boolean useDeterministicPseudoTimestamps;

    protected Pattern queryIdPattern;

    protected Cache<String, Object> seenQueryCache;


    // Delayer for benchmarking requests
    protected Delayer delayer;


    public boolean isUseDeterministicPseudoTimestamps() {
        return useDeterministicPseudoTimestamps;
    }

    public void setUseDeterministicPseudoTimestamps(boolean useDeterministicPseudoTimestamps) {
        this.useDeterministicPseudoTimestamps = useDeterministicPseudoTimestamps;
    }

    public Pattern getQueryIdPattern() {
        return queryIdPattern;
    }

    public void setQueryIdPattern(Pattern queryIdPattern) {
        this.queryIdPattern = queryIdPattern;
    }

    public Delayer getDelayer() {
        return delayer;
    }

    public void setDelayer(Delayer delayer) {
        this.delayer = delayer;
    }


    protected Long workloadSize;

    //protected Function<String, NestedResource> queryAspectFn;
    //protected Resource rawLogEndpointRes; // TODO Rename to remoteEndpointRes?


    protected QueryExecutionFactory dataQef;
    protected QueryExecutionFactory benchmarkQef;

    protected String datasetLabel;

    protected String experimentId;
    protected Resource expRes;
    protected String datasetEndpointUri;
    protected Long datasetSize;


    // Processing state variables
    protected int logFailCount = 0;
    protected long logEntryIndex = 0l;
    protected int batchSize = 10;

    protected boolean isLegacyMode = false;


    // If an RDFized web log neither provides a sequence id
    // nor a timestamp, use this counter to label resources in rdfizeLogRecord()
    protected long fallbackWebLogRecordSeqId = 0;

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

    public void setLegacyMode(boolean isLegacyMode) {
        this.isLegacyMode = isLegacyMode;
    }

    public boolean isLegacyMode() {
        return isLegacyMode;
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

    public Cache<String, ? extends Object> getSeenQueryCache() {
        return seenQueryCache;
    }

    public LsqProcessor setSeenQueryCache(Cache<String, Object> seenQueryCache) {
        this.seenQueryCache = seenQueryCache;
        return this;
    }

    public QueryExecutionFactory getDataQef() {
        return dataQef;
    }

    public void setDataQef(QueryExecutionFactory dataQef) {
        this.dataQef = dataQef;
    }

    public QueryExecutionFactory getBenchmarkQef() {
        return benchmarkQef;
    }

    public void setBenchmarkQef(QueryExecutionFactory benchmarkQef) {
        this.benchmarkQef = benchmarkQef;
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

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentId() {
        return experimentId;
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

    //@Override
    /**
     * LSQ2 approach:  The resource already corresponds to an LsqQuery
     * which gets enriched by the process
     *
     * @param r
     * @return
     */
    public LsqQuery applyForQueryRecord(Resource r) {
        LsqQuery result = applyForQueryOrWebLogRecord(r);
        return result;
    }

    //@Override

    public LsqQuery benchmark(LsqQuery q) {
        String queryStr = q.getText();
        String parseError = q.getParseError();
        if(queryStr != null && parseError == null) {
            Query query = QueryFactory.create(queryStr);

//			String distributionId = UriToPathUtils.resolvePath(datasetLabel).toString()
//					.replace('/', '-');

            // String timestamp = Instant.now().toString();

            NestedResource baseRes = NestedResource.from(q);

            // baseRes.nest("_").nest(experimentId);

            //Function<String, NestedResource> queryAspectFn = aspect -> baseRes.nest(aspect + "-").nest(serviceId + "_" + timestamp);

            // Function<String, NestedResource> queryAspectFn = aspect -> baseRes.nest(aspect + "-").nest(serviceId + "_" + timestamp);


            NestedResource queryExecRes = baseRes.nest("_le-").nest(experimentId);
            doLocalExecutionNew(query, q, queryExecRes);
        }

        return q;
    }

    public void doLocalExecutionNew(Query query, LsqQuery queryRes, NestedResource xqueryExecRes) {
        //boolean hasBeenExecuted = executedQueries.contains(query);

        boolean hasBeenExecuted = seenQueryCache.getIfPresent("" + query) != null;
        seenQueryCache.put("" + query, true);

        if(!hasBeenExecuted) {
            //executedQueries.add(query);

            String nowStr;
            if(useDeterministicPseudoTimestamps) {
                //nowStr = queryRes.get().getProperty(p)
                nowStr = "now";
            } else {
                Instant now = Instant.now();
                // ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
                // Calendar nowCal = GregorianCalendar.from(zdt);
                //String timestamp = now.toString();
                nowStr = now.toString(); //DateTimeFormatter..format(zdt);
            }

            Resource queryExecRes = xqueryExecRes.nest("_at-").nest(nowStr).get();

            if(expRes != null) {
                queryExecRes
                    .addProperty(PROV.wasGeneratedBy, expRes);
            }

            // TODO Switch between local / remote execution
            if(query != null) {
                throw new RuntimeException("old code and incompatible model - should not be used anymore");
//                queryRes
//                    .getLocalExecutions(Resource.class).add(queryExecRes);

//                rdfizeQueryExecution(queryRes, query, queryExecRes, delayer, benchmarkQef, dataQef, datasetSize);
            }
        }
    }


    @Deprecated
    public LsqQuery applyForQueryOrWebLogRecord(Resource r) {
        // logger.debug(RDFDataMgr.write(out, dataset, lang););

        // Extract the query and add it with the lsq:query property
        WebLogParser.extractRawQueryString(r);

        // If the resource is null, we could not parse the log entry
        // therefore count this as an error

        boolean fail = false;
        boolean parsed = r.getProperty(LSQ.processingError) == null ? true : false;

        LsqQuery result = null;


        NestedResource tmpBaseRes;
        try {
            if(parsed) {
                Optional<String> str;
                if(isLegacyMode) {
                    // In legacy mode, use the raw query string
                    // (which has not yet been parsed with e.g. external namespaces)
                    str = Optional.ofNullable(r.getProperty(LSQ.hasRemoteExec))
                            .map(x -> x.getProperty(LSQ.query))
                            .map(queryStmt -> queryStmt.getString());

                    // In legacy mode, we create a fresh model - so the output does not
                    // contain the information we got on input
                    Model queryModel = ModelFactory.createDefaultModel();
                    tmpBaseRes = NestedResource.from(queryModel, baseUri);
                } else {
                    str = Optional.ofNullable(r.getProperty(LSQ.text))
                            .map(queryStmt -> queryStmt.getString());

                    tmpBaseRes = NestedResource.from(r);
                }

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
                        QueryUtils.optimizePrefixes(query);

//                        PrefixMapping pm = org.aksw.jena_sparql_api.utils.QueryUtils.usedPrefixes(query);
//                        //Query prefixCleanedQuery = QueryTransformOps.shallowCopy(query);
//                        //prefixCleanedQuery.setPrefixMapping(pm);
//                        query.setPrefixMapping(pm);

                        //org.aksw.jena_sparql_api.utils.QueryUtils

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


                    //Model queryModel = ModelFactory.createDefaultModel();


                    //Resource logEndpointRes = rawLogEndpointRes == null ? null : rawLogEndpointRes.inModel(queryModel);

                    //NestedResource baseRes = new NestedResource(generatorRes).nest(datasetLabel).nest("-");

                    r = tmpBaseRes.get();
                    if(r.isAnon() || isLegacyMode) {
                        r = ResourceUtils.renameResource(r, baseUri);
                        tmpBaseRes = NestedResource.from(r);
                    }

                    NestedResource baseRes = tmpBaseRes;

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

                            queryRes = NestedResource.from(r);

                            queryAspectFn = (aspect) -> queryRes.nest("-" + aspect); //.nest("q-" + queryHash);
                        }
                    } else {

                        String queryHash = isLegacyMode
                                ? StringUtils.md5Hash(queryStr).substring(0, 8)
                                : Hashing.sha256().hashString(queryStr, StandardCharsets.UTF_8).toString();

                        queryRes = baseRes.nest("q-" + queryHash);
                        queryAspectFn = (aspect) -> baseRes.nest(aspect + "-").nest("q-" + queryHash);
                    }

                    if(isLegacyMode) {
                        result = queryRes.get()
                                .addProperty(RDF.type, LSQ.Query)
                                .as(LsqQuery.class)
                                .setText(("" + queryStr).replace("\n", " "));
                    }

                    if(!queryStmt.isParsed()) {
                        String msg = queryStmt.getParseException().getMessage();
                        result
                            .addLiteral(LSQ.parseError, msg);
                    } else {
                        if(isRdfizerQueryStructuralFeaturesEnabled) {
                            rdfizeQueryStructuralFeatures(result, queryAspectFn, query);
                        }
                    }

                    // If the input is a query record, web log record rdfization does not apply
                    if(isLegacyMode) {
                        if(isRdfizerQueryLogRecordEnabled) {
                            rdfizeLogRecord(baseRes, r, queryRes, queryAspectFn);
                        }
                    }


                    if(isRdfizerQueryExecutionEnabled) {
//                        if(isQueryExecutionRemote) {
//                            rdfizeLogRecord(baseRes, r, queryRes, queryAspectFn);
//                        } else {
                            doLocalExecution(query, queryRes.get().as(LsqQuery.class), queryAspectFn);
//                        }
                    }


                    Model queryModel = r.getModel();
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


    public void doLocalExecution(Query query, LsqQuery queryRes, Function<String, NestedResource> queryAspectFn) {
        //boolean hasBeenExecuted = executedQueries.contains(query);

        boolean hasBeenExecuted = seenQueryCache.getIfPresent("" + query) != null;
        seenQueryCache.put("" + query, true);

        if(!hasBeenExecuted) {
            //executedQueries.add(query);

            String nowStr;
            if(useDeterministicPseudoTimestamps) {
                //nowStr = queryRes.get().getProperty(p)
                nowStr = "now";
            } else {
                Calendar now = Calendar.getInstance();
                nowStr = dt.format(now.getTime());
            }

            String suffix = isLegacyMode ? "-" + nowStr : "_run-" + nowStr;

            Resource queryExecRes = queryAspectFn.apply("le-" + datasetLabel).nest(suffix).get();

            if(expRes != null) {
                queryExecRes
                    .addProperty(PROV.wasGeneratedBy, expRes);
            }

            // TODO Switch between local / remote execution
            if(query != null) {
                throw new RuntimeException("old code and incompatible model - should not be used anymore");

//                queryRes
//                    .getLocalExecutions(Resource.class).add(queryExecRes);
//
//                rdfizeQueryExecution(queryRes, query, queryExecRes, delayer, benchmarkQef, dataQef, datasetSize);
            }
        }
    }

    public String getTimestampStr(Resource r) {
        String result;
        if(r.hasProperty(PROV.atTime)) {
            Literal timestampLiteral = r.getProperty(PROV.atTime).getObject().asLiteral();
            Calendar timestamp = ((XSDDateTime)timestampLiteral.getValue()).asCalendar();
            result = dt.format(timestamp.getTime());
        } else if(r.hasProperty(LSQ.sequenceId)) {
            result = "" + r.getProperty(LSQ.sequenceId).getObject().asLiteral().getLong();
        } else {
            // Fallback sequence id
            result = "" + (fallbackWebLogRecordSeqId++);
            //result = dt.format(new GregorianCalendar().getTime());
        }
        return result;
    }



    @Deprecated
    public void rdfizeLogRecord(NestedResource baseRes, Resource r, NestedResource queryRes, Function<String, NestedResource> queryAspectFn) {

        // Deal with log entry (remote execution)
        String host = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils
                .tryGetLiteralPropertyValue(r, LSQ.host, String.class).orElse("unknown-host");
        String hashedIp = StringUtils.md5Hash("someSaltPrependedToTheIp" + host).substring(0, 16);

        Resource agentRes = baseRes.nest("agent-" + hashedIp).get();

        String timestampStr = getTimestampStr(r);

        //String timestampStr = StringUtils.md5Hash("someSaltPrependedToTheIp" + r.getProperty(LSQ.host).getString()).substring(0, 16);

        Resource queryExecRecRes = queryAspectFn.apply("re-" + datasetLabel).nest("-" + hashedIp + "-" + timestampStr).get();

        // Express that the query execution was recorded
        // at some point in time by some user at some service
        // according to some source (e.g. the log file)
        queryRes.get()
            .addProperty(LSQ.hasRemoteExec, queryExecRecRes);

        if(r.hasProperty(PROV.atTime)) {
            RDFNode tmp = r.getProperty(PROV.atTime).getObject();
            queryExecRecRes
                //.addProperty(RDF.type, LSQ.)
                .addProperty(PROV.atTime, tmp); //.inModel(queryModel))
        }

        queryExecRecRes
            //.addProperty(RDF.type, LSQ.)
            .addProperty(PROV.wasAssociatedWith, agentRes)
            ;

        if(datasetEndpointUri != null) {
            Resource tmp = queryExecRecRes.getModel().createResource(datasetEndpointUri);
            queryExecRecRes.addProperty(LSQ.endpoint, tmp); // TODO Make it possible to specify the dataset configuration that was used to execute the query
        }
    }


    public static org.topbraid.spin.model.Query createSpinModel(
            Query query,
            Model tgtModel
            ) {

        return createSpinModel(query, tgtModel.createResource());
    }
    public static org.topbraid.spin.model.Query createSpinModel(
            Query query,
            Resource spinRes
//            BiFunction<? super Resource, String, String> lsqResToIri
            ) {
        query = query.cloneQuery();
        query.getGraphURIs().clear();

        // queryNo++;
        // .. generate the spin model ...
        //Model spinModel = queryRes.getModel();
        // Model spinModel = ModelFactory.createDefaultModel();
        Model spinModel = spinRes == null ? null : spinRes.getModel();
        if(spinModel == null) {
            spinModel = ModelFactory.createDefaultModel();
        }
        ARQ2SPIN arq2spin = new ARQ2SPIN(spinModel);
        org.topbraid.spin.model.Query tmpSpinRes = arq2spin.createQuery(query, spinRes == null ? null : spinRes.getURI());

        // ... and rename the blank node of the query
        // ResourceUtils.renameResource(tmpSpinRes, spinRes.getURI());

        // ... and skolemize the rest
        //Skolemize.skolemize(spinRes);

        return tmpSpinRes;

//      System.out.println("TEST {");
//      SpinUtils.indexTriplePatterns2(tmpSpinRes.getModel()).forEach(System.out::println);
//      SpinUtils.itp(tmpSpinRes).forEach(System.out::println);
//      System.out.println("}");
//tmpSpinRes.as(org.topbraid.spin.model.Query.class).getWhereElements().forEach(e -> {
//System.out.println("XXElement: " + e.asResource().getId() + ": " + e);
//});


    }


//    Map<RDFNode, Node> rdfNodeToNode = new HashMap<>();
//    Map<Resource, BasicPattern> resToBgp = SpinUtils.indexBasicPatterns(queryRes, rdfNodeToNode);
//
//    Map<Node, RDFNode> nodeToModel = new IdentityHashMap<>();
//    rdfNodeToNode.forEach((k, v) -> nodeToModel.put(v, k));
//
//    // Make sure the BGP resources exist in the target model
//    resToBgp = resToBgp.entrySet().stream()
//            .collect(Collectors.toMap(e -> e.getKey().inModel(targetRes.getModel()), Entry::getValue));
//
//    resToBgp.keySet().forEach(r -> targetRes.addProperty(LSQ.hasBGP, r));
//

//    public static void enrichSpinElementsWithQueries(SpinQueryEx spinNode) {
//        for(SpinBgp bgp : spinNode.getBgps()) {
//            LsqQuery extensionQuery = bgp.getExtensionQuery();
//            if(extensionQuery == null) {
//                extensionQuery = bgp.getModel().createResource().as(LsqQuery.class);
//
//                Query query = QueryUtils.elementToQuery(new ElementTriplesBlock(bgp.toBasicPattern()));
//                extensionQuery.setQueryAndHash(query);
//            }
//
//            Map<Node, SpinBgpNode> bgpNodeMap = bgp.indexBgpNodes();
//
//            for(SpinBgpNode bgpNode : bgpNodeMap.values()) {
//                SpinBgp subBgp = bgpNode.getSubBgp();
//
//                if(subBgp == null) {
//                    subBgp = bgpNode.getModel().createResource().as(SpinBgp.class);
//                    bgpNode.setSubBgp(subBgp);
//                }
//                Node jenaNode = bgpNode.toJenaNode();
//
//                List<TriplePattern> subBgpTps = bgp.getTriplePatterns().stream()
//                    .filter(tp -> TripleUtils.streamNodes(SpinUtils.toJenaTriple(tp)).collect(Collectors.toSet()).contains(jenaNode))
//                    .collect(Collectors.toList());
//
//                Collection<TriplePattern> dest = subBgp.getTriplePatterns();
//                for(TriplePattern tp : subBgpTps) {
//                    dest.add(tp);
//                }
//            }
//        }
//    }

    public static void rdfizeQueryStructuralFeatures(
            Resource queryRes,
            Function<String, NestedResource> queryAspectFn,
            Query query) {

        //Resource execRes = queryAspectFn.apply("exec").nest("-execX").get();
        try {

            SpinQueryEx spinRes = queryAspectFn.apply("spin").get().as(SpinQueryEx.class);

            org.topbraid.spin.model.Query spinQuery = createSpinModel(query, spinRes);

            Model spinModel = spinQuery.getModel();





            // spinRes.getModel().add(spinModel);
            queryRes.getModel().add(spinModel);


            queryRes.addProperty(LSQ.hasSpin, spinRes);

          //RDFDataMgr.write(System.out, spinRes.getModel(), RDFFormat.TURTLE);


//          } catch (Exception ex) {
//              String msg = ex.getMessage();// ExceptionUtils.getFullStackTrace(ex);
//              queryRes.addLiteral(LSQ.runtimeError, msg);
//
//              // TODO Port the getRDFUserExceptions function
//              // String queryStats =
//              // this.getRDFUserExecutions(queryToSubmissions.get(queryStr),sSeparator);
//              parseErrorCount++;
//          }

            //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            Resource featureRes = queryAspectFn.apply("sf").get(); // model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash

            queryRes.addProperty(LSQ.hasStructuralFeatures, featureRes);


            // Add used features
            LsqEnrichments.enrichResourceWithQueryFeatures(featureRes, query);

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


    /**
     * Perform all rdfizations
     *
     * @param queryRes
     * @param query
     * @param queryExecRes
     * @param qef
     * @param cachedQef
     * @param datasetSize
     */
    public static void rdfizeQueryExecution(LsqQuery queryRes, Query query, Resource queryExecRes, Delayer delayer, QueryExecutionFactory qef, QueryExecutionFactory cachedQef, Long datasetSize) {
        try {
            if(delayer != null) {
                cachedQef = FluentQueryExecutionFactory.from(cachedQef).config().withDelay(delayer).end().create();

                delayer.doDelay();
            }
            logger.info("Benchmarking " + queryRes.asNode());

            rdfizeQueryExecutionBenchmark(query, queryExecRes, qef);
            rdfizeQueryExecutionStats(queryRes, query, queryExecRes, cachedQef, datasetSize);
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
     * Benchmark the combined execution and retrieval time of a given query
     *
     * @param query
     * @param queryExecRes
     * @param qef
     */
    public static void rdfizeQueryExecutionBenchmark(Query query, Resource queryExecRes, QueryExecutionFactory qef) {

        Stopwatch sw = Stopwatch.createStarted();
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            long resultSetSize = QueryExecutionUtils.consume(qe);
            double durationInMillis = sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000.0;


            //double durationInSeconds = duration.toNanos() / 1000000.0;
            queryExecRes
                .addLiteral(LSQ.itemCount, resultSetSize)
                .addLiteral(LSQ.runTimeMs, durationInMillis)
                //.addLiteral(PROV.startedAtTime, start)
                //.addLiteral(PROV.endAtTime, end)
                ;

        }
        //Calendar end = Calendar.getInstance();
        //Duration duration = Duration.between(start.toInstant(), end.toInstant());
    }


    public static void rdfizeQueryExecutionStats(LsqQuery queryRes, Query query, Resource queryExecRes, QueryExecutionFactory cachedQef, Long datasetSize) {

//        try {
    //        Stopwatch sw = Stopwatch.createStarted();
    //      long runtimeInMs = sw.stop().elapsed(TimeUnit.MILLISECONDS);


            // Benchmark the query execution

            //Calendar start = Calendar.getInstance();
            //long resultSetSize = QueryExecutionUtils.countQuery(query, qef);
//System.out.println(query);

            SpinUtils.enrichModelWithTriplePatternExtensionSizes(queryRes, queryExecRes, cachedQef);

            BiMap<org.topbraid.spin.model.Triple, Resource> tpToTpExecRess = SpinUtils.createTriplePatternExecutions(queryRes, queryExecRes);


            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinUtils.indexBasicPatterns2(queryRes);


            if(datasetSize != null) {

                SpinUtils.enrichModelWithTriplePatternSelectivities(tpToTpExecRess.values(), cachedQef, datasetSize);

                SpinUtils.enrichModelWithBGPRestrictedTPSelectivities(cachedQef, queryExecRes.getModel(), bgpToTps);
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
            // a variable is a join var (of a bgp) if it is mentioned in more than 1 triple pattern
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

                queryExecRes.addProperty(LSQ.hasBgpExec, bgpCtxRes);

                // Obtain the selectivity for the variable in that tp
                //for(e.getValue())
                Map<Var, Long> varToCount = QueryStatistics2.fetchCountJoinVarGroup(cachedQef, resToEl.values());

                Collection<org.topbraid.spin.model.Triple> tps = bgpToTps.values();
//                 Note: We assume that each var only originates from a single resource - which is the case for lsq
                Map<Var, Resource> varToQueryVarRes = tps.stream()
                    .flatMap(tp -> SpinUtils.indexTripleNodes2(tp).entrySet().stream())
                    .filter(x -> x.getValue().isVariable())
                    .collect(Collectors.toMap(
                            x -> (Var)x.getValue(),
                            x -> x.getKey().asResource(),
                            (old, now) -> now));

                // Add the BGP var statistics
                varToCount.forEach((v, c) -> {
                    Resource queryVarRes = varToQueryVarRes.get(v);
                    //System.out.println("queryVar: " + queryVar);

                    Resource bgpVar = varToBgpVar.get(v);

                    bgpVar.addLiteral(LSQ.itemCount, c);
                    bgpVar.addProperty(LSQ.proxyFor, queryVarRes);
                });
                    //
                    //vr.addLiteral(LSQ.tpSelectivity, o);


                Map<org.topbraid.spin.model.Triple, Map<Var, Long>> elToVarToCount = QueryStatistics2.fetchCountJoinVarElement(cachedQef, resToEl);

                elToVarToCount.forEach((t, vToC) -> {
                    Map<Node, RDFNode> varToRes = SpinUtils.indexTripleNodes(t);

                    Resource execTp = tpToTpExecRess.get(t);

//                    String tpId = e.getKey().getProperty(Skolemize.skolemId).getString();

                    //String tpResBase = queryExecRes.getURI() + "-tp-" + tpId;
                    String tpResBase = execTp.getURI();

                    vToC.forEach((v, c) -> {
                        Resource execTpVarRes = queryExecRes.getModel().createResource(tpResBase + "-var-" + v.getName());

                        long bgpJoinVarCount = varToCount.get(v);

                        double tpSelJoinVarRestricted = c == 0 ? 0d : bgpJoinVarCount / (double)c;
                        execTpVarRes
                            .addLiteral(LSQ.itemCount, c)
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
//        }
//        catch(Exception e) {
//            Throwable f = ExceptionUtilsAksw.unwrap(e, QueryExceptionHTTP.class).orElse(e);
//            String msg = f.getMessage();
//            msg = msg == null ? "" + ExceptionUtils.getStackTrace(f) : msg;
//            queryExecRes.addLiteral(LSQ.execError, msg);
//            String queryStr = ("" + query).replace("\n", " ");
//            logger.warn("Query execution exception [" + msg + "] for query " + queryStr, e);
//        }
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
        m.setNsPrefix("lsq", LSQ.NS);
        m.setNsPrefix("prov", PROV.ns);
        String result = ModelUtils.toString(m, "TTL");
        return result;
    }

}
