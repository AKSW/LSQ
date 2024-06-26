package org.aksw.simba.lsq.enricher.benchmark.core;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jena_sparql_api.rx.query_flow.QueryFlowOps;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.DatasetGraphOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.binding.ResultSetUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecutionUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.aksw.jenax.arq.util.quad.Quads;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.connection.reconnect.ConnectionLostException;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.rx.op.FlowOfQuadsOps;
import org.aksw.simba.lsq.core.util.SkolemizeBackport;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.spinx.model.Bgp;
import org.aksw.simba.lsq.spinx.model.BgpNode;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spinrdf.model.TriplePattern;

import com.google.common.base.Stopwatch;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;


/**
 * This is the LSQ2 processor for benchmarking RDFized query log
 *
 * @author raven
 *
 */
public class LsqBenchmarkProcessor {
    static final Logger logger = LoggerFactory.getLogger(LsqBenchmarkProcessor.class);


    public static FlowableTransformer<LsqQuery, LsqQuery> createProcessor() {
        return null;
    }

//
//    public static void run() {
//        SparqlQueryConnection benchmarkConn = RDFConnectionFactory.connect(DatasetFactory.create());
//
//        Model configModel = ModelFactory.createDefaultModel();
//
//        String expId = "testrun";
//        String expSuffix = "_" + expId;
//        String lsqBaseIri = "http://lsq.aksw.org/";
//
//        ExperimentConfig config = configModel
//                .createResource("http://someconfig.at/now")
//                .as(ExperimentConfig.class)
//                .setIdentifier(expId);
//
//        Instant benchmarkRunStartTimestamp = Instant.ofEpochMilli(0);
//        ZonedDateTime zdt = ZonedDateTime.ofInstant(benchmarkRunStartTimestamp, ZoneId.systemDefault());
//        Calendar cal = GregorianCalendar.from(zdt);
//        XSDDateTime xsddt = new XSDDateTime(cal);
//
//        ExperimentRun expRun = configModel
//                .createResource()
//                .as(ExperimentRun.class)
//                .setConfig(config)
//                .setTimestamp(xsddt);
//
//        HashIdCxt tmp = MapperProxyUtils.getHashId(expRun);
//        String expRunIri = lsqBaseIri + tmp.getStringId(expRun);
//        expRun = ResourceUtils.renameResource(expRun, expRunIri).as(ExperimentRun.class);
//
//        Flowable<LsqQuery> queryFlow = RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
//                .map(r -> r.as(LsqQuery.class));
//
//        // TODO Need to set up an index connection
//        process(queryFlow, lsqBaseIri, config, expRun, benchmarkConn, null);
//    }

    /**
     * If something goes wrong when running the wrapped action
     * then log an error return an empty maybe
     *
     * @param action A callable encapsulating some action
     * @return A maybe with the action's return value or empty
     */
   public static <T> Maybe<T> safeMaybe(Callable<T> action) {
       Maybe<T> result;
       try {
           T value = action.call();
           result = Maybe.just(value);
       } catch (Exception e) {
           logger.warn("Internal error; trying to continue", e);
           result = Maybe.empty();
       }
       return result;
   }

   public static void process(
           StreamRDF out,
           Flowable<LsqQuery> rawQueryFlow,
           String lsqBaseIri,
           ExperimentConfig expConfig,
           ExperimentExec expExec,
           ExperimentRun expRun,
           Function<Resource, Resource> enricher,
           SparqlQueryConnection benchmarkConn,
           RDFConnection indexConn) {
       Flowable<ResourceInDataset> flowable = processCore(rawQueryFlow, lsqBaseIri, expConfig, expExec, expRun, enricher, benchmarkConn, indexConn);

       Iterable<ResourceInDataset> items = flowable.blockingIterable();
       for(ResourceInDataset item : items) {
           // RDFDataMgr.write(out, item.getDataset(), RDFFormat.TRIG_BLOCKS);
           StreamRDFOps.sendDatasetToStream(item.getDataset().asDatasetGraph(), out);
       }
   }

    /**
     *
     * @param lsqBaseIri The IRI prefix to prepend to generated resources
     * @param expSuffix A suffix to be appended to the automatically derived query hashes
     *        in order to generate the full id used for database lookup (and possibly update)
     *        of benchmark information
     * @param benchmarkConn The connection on which to perform benchmarking
     */
    public static Flowable<ResourceInDataset> processCore(
            Flowable<LsqQuery> rawQueryFlow,
            String lsqBaseIri,
            ExperimentConfig expConfig,
            ExperimentExec expExec,
            ExperimentRun expRun,
            Function<Resource, Resource> enricher,
            SparqlQueryConnection benchmarkConn,
            RDFConnection indexConn) {


        //ExperimentConfig config = expRun.getConfig();
        //String expSuffix


        String datasetLabel = expConfig.getDatasetLabel();
        XSDDateTime benchmarkExecTimestamp = expExec.getTimestamp();
        Instant instant = benchmarkExecTimestamp.asCalendar().toInstant();
        String runId = "run" + (Optional.ofNullable(expRun.getRunId()).map(Object::toString).orElse(""));
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        String benchmarkExecTimestampStr = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);
        String expId = datasetLabel + "_" + benchmarkExecTimestampStr;
        String expSuffix = "_" + expId + "_" + runId;

        boolean benchmarkSecondaryQueries = Optional.ofNullable(expConfig.benchmarkSecondaryQueries()).orElse(false);

        Function<String, String> lsqQueryBaseIriFn = hash -> lsqBaseIri + "q-" + hash;

        // Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> lsqQueryBaseIriFn.apply(lsqQuery.getHash()) + expSuffix;

//        Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> "urn://" + lsqQuery.getHash() + expSuffix;
        Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> lsqQueryBaseIriFn.apply(lsqQuery.getHash()) + expSuffix;

//        Flowable<List<Set<LsqQuery>>> queryFlow = RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
//                Flowable<List<Set<LsqQuery>>> queryFlow = RDFDataMgrRx.createFlowableResources("../tmp/saleem.trig", Lang.TRIG, null)
        Flowable<List<Set<LsqQuery>>> queryFlow = rawQueryFlow
//                .map(r -> r.as(LsqQuery.class))
//                .skip(1)
//                .take(1)
                .concatMapMaybe(lsqQuery -> {
                    Maybe<LsqQuery> r = safeMaybe(() -> enricher.apply(lsqQuery).as(LsqQuery.class));
                    return r;
                })
                /*
                .concatMapMaybe(lsqQuery ->
                    safeMaybe(() -> LsqEnrichments.enrichWithFullSpinModelCore(lsqQuery)))
//                .concatMapMaybe(lsqQuery -> enrichWithFullSpinModel(lsqQuery))
                .map(anonQuery -> updateLsqQueryIris(anonQuery, q -> lsqQueryBaseIriFn.apply(q.getHash())))
                .concatMapMaybe(lsqQuery ->
                    safeMaybe(() -> LsqEnrichments.enrichWithStaticAnalysis(lsqQuery)))
//                .doAfterNext(x -> {
//                    RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE_FLAT);
//                    System.exit(1);
//                })
   */
                //.flatMap(lsqQuery -> Flowable.fromIterable(extractAllQueries(lsqQuery)), false, 128)
                //.map(lsqQuery -> extractAllQueries(lsqQuery))
                //.map(batch -> benchmarkSecondaryQueries ? batch : Collections.singleton(batch.iterator().next()))
                .map(lsqQuery -> benchmarkSecondaryQueries ? extractAllQueries(lsqQuery) : Collections.singleton(lsqQuery))
//                .doAfterNext(lsqQuery -> lsqQuery.updateHash())
//                .doOnNext(r -> ResourceUtils.renameResource(r, "http://lsq.aksw.org/q-" + r.getHash()).as(LsqQuery.class))
//                .lift(OperatorObserveThroughput.create("throughput", 100))
                .buffer(1)
//                .lift(OperatorObserveThroughput.create("buffered", 100))
                ;

        Flowable<ResourceInDataset> result = queryFlow.flatMapIterable(batch -> {
            List<ResourceInDataset> items = processBatchOfQueries(
                    batch,
                    lsqBaseIri,
                    expConfig,
                    expExec,
                    expRun,
                    benchmarkConn,
                    lsqQueryExecFn,
                    indexConn);
            return items;
        });

        if (false) {
            Iterable<List<Set<LsqQuery>>> batches = queryFlow.blockingIterable();
            Iterator<List<Set<LsqQuery>>> itBatches = batches.iterator();

            // Create a database to ensure uniqueness of evaluation tasks
            while(itBatches.hasNext()) {
                List<Set<LsqQuery>> batch = itBatches.next();
                List<ResourceInDataset> items = processBatchOfQueries(
                        batch,
                        lsqBaseIri,
                        expConfig,
                        expExec,
                        expRun,
                        benchmarkConn,
                        lsqQueryExecFn,
                        indexConn);

                for(ResourceInDataset item : items) {
                    RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), item.getDataset(), RDFFormat.TRIG_BLOCKS);
                }
            }
        }
        return result;


//
//        Model model = ModelFactory.createDefaultModel();
//        SpinQueryEx spinRes = model.createResource("http://test.ur/i").as(SpinQueryEx.class);
//
//
//
//        // Create a stream of tasks which to benchmark:
//        // Create a stream of bgps
//        // Create a stream of tps
//        // create a stream of sub-bgps
//
//        // Then for each of these resources,
//
//
//
////        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
//
//        for(SpinBgp bgp : spinRes.getBgps()) {
//            System.out.println(bgp);
//            Collection<TriplePattern> tps = bgp.getTriplePatterns();
//
//            for(TriplePattern tp : tps) {
//                System.out.println(tp);
//            }
//        }
//
//        QueryStatistics2.enrichSpinQueryWithBgpStats(spinRes);
//        // QueryStatistics2.setUpJoinVertices(spinRes);
//        QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, spinRes);
//
//        // TODO Make skolemize reuse skolem ID resources
//        Skolemize.skolemize(spinRes);
//
//
//
//        // TODO How to perform triple pattern and join evaluation?
//        // Actually we would need to create a stream of unique Triple Patterns and BGPs
//        // So should we use an (embedded DB) to keep track of for which items the statistics have already been computed
//        // within a benchmark experiment?
//
//
//
////        QueryStatistics2.fetchCountJoinVarElement(qef, itemToElement)
//
//        // Now to create the evaluation results...
//        // LsqProcessor.rdfizeQueryExecutionStats
////        SpinUtils.enrichModelWithTriplePatternExtensionSizes(queryRes, queryExecRes, cachedQef);
//
//
//
//        //Skolemize.skolemize(spinRes);
//
//        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }

    /** Create a union model of all unique non-null arguments */
    public static Model unionAll(Model ...models) {
        Model result = Stream.of(models)
                .filter(Objects::nonNull)
                .distinct()
                .collect(ModelUtils.unionCollector());
        return result;
    }

    public static List<ResourceInDataset> processBatchOfQueries(
            List<Set<LsqQuery>> batch,
            String lsqBaseIri,
            ExperimentConfig expConfig,
            ExperimentExec expExec,
            ExperimentRun expRun,
            SparqlQueryConnection benchmarkConn,
            Function<LsqQuery, String> lsqQueryExecFn,
            RDFConnection indexConn) {

        // Skolemization is blocked for all resources appearing in the unionModel
        Model staticModel = unionAll(expConfig.getModel(), expExec.getModel(), expRun.getModel());

        // Combine the query hash and the exprRun id to form the benchmark task id.
        List<ResourceInDataset> result = new ArrayList<>();

        Map<Node, LsqQuery> inputTasks = new HashMap<>();
        for (Set<LsqQuery> queries : batch) {
            for (LsqQuery query : queries) {
                String taskIdStr = lsqQueryExecFn.apply(query);
                Node taskId = NodeFactory.createURI(taskIdStr);
                if (inputTasks.containsKey(taskId)) {
                    throw new RuntimeException("Task already exists: " + taskId);
                }
                inputTasks.put(taskId, query);
            }
        }

        Map<String, Dataset> taskIdToDataset = Txn.calculate(indexConn, () ->
            fetchDatasets(indexConn, inputTasks.keySet())
            .toMap(Entry::getKey, Entry::getValue)
            .blockingGet());

        // Obtain the set of query strings already in the store
        Set<String> completedTaskIds = taskIdToDataset.keySet();

        Map<Node, LsqQuery> pendingTasks = inputTasks.entrySet().stream()// batch.stream()
            .filter(e -> !completedTaskIds.contains(e.getKey().toString()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        List<Quad> inserts = new ArrayList<>();

        for (Entry<Node, LsqQuery> task : pendingTasks.entrySet()) {
            Node queryExecId = task.getKey();
            String queryExecIri = queryExecId.getURI();

            LsqQuery lsqQuery = task.getValue();
            String queryStr = lsqQuery.getText();

            Model newModel = ModelFactory.createDefaultModel();
            LsqQuery newLsqQuery = lsqQuery.inModel(newModel).as(LsqQuery.class);

            // Create fresh local execution and query exec resources
            LocalExecution newLocalExec = newModel.createResource().as(LocalExecution.class);
            QueryExec newQueryExec = newModel.createResource().as(QueryExec.class);

            rdfizeQueryExecutionBenchmark(
                    benchmarkConn,
                    queryStr,
                    newQueryExec,
                    expConfig.getConnectionTimeoutForRetrieval(),
                    expConfig.getExecutionTimeoutForRetrieval(),
                    expConfig.getMaxResultCountForCounting(),
                    expConfig.getMaxByteSizeForCounting(),
                    expConfig.getMaxResultCountForSerialization(),
                    expConfig.getMaxByteSizeForSerialization(),
                    expConfig.getConnectionTimeoutForCounting(),
                    expConfig.getConnectionTimeoutForRetrieval(),
                    expConfig.getMaxCount(),
                    expConfig.getMaxCountAffectsTp());

            newLsqQuery.getLocalExecutions().add(newLocalExec);
            newLocalExec.setBenchmarkRun(expRun);
            newLocalExec.setQueryExec(newQueryExec);

            // Skolemization is blocked for all resources appearing in the unionModel
            Model unionModel = unionAll(staticModel, lsqQuery.getModel());

            // So we only skolemize all resources related to the newLocalExec
            LocalExecution finalLocalExec = SkolemizeBackport.skolemize(newLocalExec, unionModel, lsqBaseIri, LocalExecution.class);

            Dataset newDataset = new DatasetOneNgImpl(DatasetGraphOneNgImpl.create(queryExecId, finalLocalExec.getModel().getGraph()));
            inserts.add(new Quad(queryExecId, queryExecId, LSQ.execStatus.asNode(), NodeFactory.createLiteralString("processed")));
            taskIdToDataset.put(queryExecIri, newDataset);
        }

        UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(inserts, null);
        Txn.executeWrite(indexConn, () -> indexConn.update(ur));

        // Remove the execStatus "processed" triples from the fetched datasets
        for(Dataset ds : taskIdToDataset.values()) {
            for(Entry<String, Model> e : DatasetUtils.listModels(ds)) {
                e.getValue().removeAll(null, LSQ.execStatus, null);
            }
        }

        // Txn.executeRead(indexConn, () -> System.out.println(ResultSetFormatter.asText(indexConn.query("SELECT ?s { ?s ?p ?o }").execSelect())));

        for(Set<LsqQuery> pack : batch) {

            logger.info("Processing pack of size: " + pack.size());
            // TODO Move all the code into a nice processPack method of a new class
            try {
                // The primary query is assumed to always be the first element of a pack
                LsqQuery primaryQueryRaw = pack.iterator().next();


                Model primaryQueryModel = ModelFactory.createDefaultModel();

                // We need to add the config model in order to include the benchmark run id
                // We remove the config once we are done

                // TODO We should ensure that only the minimal necessary config model is added
                // expRoot.getModel().add(configModel);

//                primaryQueryModel.add(configModel);
//                primaryQueryModel.add(expRun.getModel());


                // Extend the rootQuery's model with all related query executions
                for(LsqQuery item : pack) {
                    String key = lsqQueryExecFn.apply(item);
                    Dataset ds = taskIdToDataset.get(key);
                    Objects.requireNonNull(ds, "Expected dataset for key "  + key);
                    Model m = ds.getNamedModel(key);
                    Objects.requireNonNull(m, "Should not happen: No query execution model for " + key);

//                        System.err.println("BEGIN***********************************************");
//                        if(item.getModel().contains(ResourceFactory.createResource("http://www.bigdata.com/rdf#serviceParam"), null, (RDFNode)null)) {
//                            System.out.println("here");
//                        }
//                        RDFDataMgr.write(System.err, item.getModel(), RDFFormat.TURTLE_PRETTY);
//                        System.err.println("END***********************************************");

                    // Adding the primary query's model to itself should be harmless
                    primaryQueryModel.add(m);
                    primaryQueryModel.add(item.getModel());
                }


                LsqQuery primaryQuery = primaryQueryRaw.inModel(primaryQueryModel).as(LsqQuery.class);

                // Update triple pattern selectivities
                // LocalExecution expRoot = model.createResource().as(LocalExecution.class);
                Map<Resource, LocalExecution> rleMap = primaryQuery.getLocalExecutionMap();
                LocalExecution expRoot = rleMap.get(expRun);

                if (expRoot == null) {
                    throw new RuntimeException("Should not happen: No local execution with id " + expRun.asNode() +
                            ". Candidates: " + rleMap.keySet().stream().map(RDFNode::asNode).collect(Collectors.toSet()));
                }

                // expRoot.setBenchmarkRun(expRun);

                // If there is no spin model then don't try to create executions for its elements
                if (primaryQuery.getSpinQuery() != null) {
                    LsqExec.createAllExecs(primaryQuery, expRun);
                }

                if (false) {
                    Model configModel = expConfig.getModel();

                    HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(expRoot);//.getHash(bgp);
                    //Map<RDFNode, HashCode> renames = hashIdCxt.getMapping();
                    Map<RDFNode, String> renames = hashIdCxt.getStringIdMapping();

        //                    Map<Resource, String> renames = new LinkedHashMap<>();
        //                    for(SpinBgp bgp : spinRoot.getBgps()) {
        //                        HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(bgp);//.getHash(bgp);
        //
        //                        for(Entry<RDFNode, HashCode> e : hashIdCxt.getMapping().entrySet()) {
        //                            if(e.getKey().isResource()) {
        //                                renames.put(e.getKey().asResource(), e.getValue().toString());
        //                            }
        //                        }
        //                    }

        //                    for(Entry<RDFNode, HashCode> e : renames.entrySet()) {

                    primaryQueryModel.remove(expRun.getModel());
                    primaryQueryModel.remove(configModel);

                    Map<Resource, Resource> remap = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.renameResources(lsqBaseIri, renames);


                    // If the primaryQuery was renamed
                    Resource tgtPrimaryQuery = remap.getOrDefault(primaryQuery, primaryQuery);
                }

                // primaryQueryModel.remove(expRun.getModel());
                // primaryQueryModel.remove(configModel);

                Resource tgtPrimaryQuery = primaryQuery;
                //String graphIri = primaryQuery.getURI();
                ResourceInDataset item = ResourceInDatasetImpl.createFromCopyIntoResourceGraph(tgtPrimaryQuery);
                result.add(item);
            } catch (Exception e) {
                logger.warn("Internal error; trying to continue", e);
            }
            //RDFDataMgr.write(System.out, spinRoot.getModel(), RDFFormat.TURTLE_BLOCKS);
        }

        return result;
    }

    public static LsqQuery updateLsqQueryIris(
            LsqQuery start,
            Function<? super LsqQuery, String> genIri)
    {
        LsqQuery result = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.renameResources(
                start,
                LsqQuery.class,
                r -> r.getModel().listResourcesWithProperty(LSQ.text),
                r -> genIri.apply(r)
                );
        return result;
    }

    /**
     * Extract all queries associated with elements of the lsq query's spin representation
     *
     * @param primaryQuery
     * @return
     */
    public static Set<LsqQuery> extractAllQueries(LsqQuery primaryQuery) {
        Set<LsqQuery> result = new LinkedHashSet<>();

        // Add self by default
        result.add(primaryQuery);

        //SpinQueryEx spinNode = primaryQuery.getSpinQuery().as(SpinQueryEx.class);
        LsqStructuralFeatures bgpInfo = primaryQuery.getStructuralFeatures();


        for(Bgp bgp : bgpInfo.getBgps()) {
            extractAllQueriesFromBgp(result, bgp);
        }

        return result;
    }


    public static void extractAllQueriesFromBgp(Set<LsqQuery> result, Bgp bgp) {
        LsqQuery extensionQuery = bgp.getExtensionQuery();
        if(extensionQuery != null) {
            result.add(extensionQuery);
        }

        Map<Node, BgpNode> bgpNodeMap = bgp.indexBgpNodes();

        for(BgpNode bgpNode : bgpNodeMap.values()) {
            extensionQuery = bgpNode.getJoinExtensionQuery();
            if(extensionQuery != null) {
                result.add(extensionQuery);
            }

            Bgp subBgp = bgpNode.getSubBgp();

            if(subBgp != null) {
                extensionQuery = subBgp.getExtensionQuery();
                if(extensionQuery != null) {
                    result.add(extensionQuery);
                }
            }

            // Get extension queries from triple patterns
            for(TriplePattern tp : bgp.getTriplePatterns()) {
                LsqTriplePattern ltp = tp.as(LsqTriplePattern.class);

                extensionQuery = ltp.getExtensionQuery();
                if(extensionQuery != null) {
                    result.add(extensionQuery);
                }
            }
        }

        // Extract queries from subBgp
        for(BgpNode bgpNode : bgp.getBgpNodes()) {
            Bgp subBgp = bgpNode.getSubBgp();

            // SubBgp may is typically null if the bgp node is not a variable
//            if (subBgp != null) {

            // The sub-bgp of a variable in a bgp with a single triple pattern is the original bgp;
            // prevent infinite recursion
                if(!subBgp.equals(bgp)) {
                    extractAllQueriesFromBgp(result, subBgp);
                }
            }
//        }
    }

//    public SparqlQueryConnection configureConnection(SparqlQueryConnection rawConn, ExperimentConfig config) {
//        ExperimentConfig config;
//        BigDecimal connectionTimeout = config.getConnectionTimeout();
//        BigDecimal queryExecutionTimeout = config.getQueryTimeout();
//        Long effectiveConnectionTimeout = connectionTimeout == null
//                ? -1l
//                : connectionTimeout.divide(new BigDecimal(1000)).longValue();
//
//        Long effectiveQueryExecutionTimeout = queryExecutionTimeout == null
//                ? -1l
//                : queryExecutionTimeout.divide(new BigDecimal(1000)).longValue();
//
//        qe.setTimeout(effectiveConnectionTimeout, effectiveQueryExecutionTimeout);
//
//        return null;
//    }

    /*
        * Benchmark the combined execution and retrieval time of a given query
        *
        * @param query
        * @param queryExecRes
        * @param qef
        */
       public static QueryExec rdfizeQueryExecutionBenchmark(
               SparqlQueryConnection conn,
               String queryStr,
               QueryExec result,
               BigDecimal rawConnectionTimeoutForRetrieval,
               BigDecimal rawExecutionTimeoutForRetrieval,
               Long rawMaxResultCountForCounting,
               Long rawMaxByteSizeForCounting,
               Long rawMaxResultCountForSerialization,
               Long rawMaxByteSizeForSerialization,
               BigDecimal rawConnectionTimeoutForCounting,
               BigDecimal rawExecutionTimeoutForCounting,
               Long rawMaxCount,
               Boolean rawMaxCountAffectsTp
               ) {


           long connectionTimeoutForRetrieval = Optional.ofNullable(rawConnectionTimeoutForRetrieval)
                   .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);
           long executionTimeoutForRetrieval = Optional.ofNullable(rawExecutionTimeoutForRetrieval)
                   .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);

           long maxResultCountForCounting = Optional.ofNullable(rawMaxResultCountForCounting).orElse(-1l);
           long maxByteSizeForCounting = Optional.ofNullable(rawMaxByteSizeForCounting).orElse(-1l);

           long maxResultCountForSerialization = Optional.ofNullable(rawMaxResultCountForSerialization).orElse(-1l);
           long maxByteSizeForSerialization = Optional.ofNullable(rawMaxByteSizeForSerialization).orElse(-1l);

           long connectionTimeoutForCounting = Optional.ofNullable(rawConnectionTimeoutForCounting)
                   .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);
           long executionTimeoutForCounting = Optional.ofNullable(rawExecutionTimeoutForCounting)
                   .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);

           long maxCount = Optional.ofNullable(rawMaxCount).orElse(-1l);

           boolean maxCountAffectsTp = Optional.ofNullable(rawMaxCountAffectsTp).orElse(false);

           boolean exceededMaxResultCountForSerialization = false;
           boolean exceededMaxByteSizeForSerialization = false;

           boolean exceededMaxResultCountForCounting = false;
           boolean exceededMaxByteSizeForCounting = false;

           Instant now = Instant.now();
           ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
           Calendar cal = GregorianCalendar.from(zdt);
           XSDDateTime xsdDateTime = new XSDDateTime(cal);

           result.setTimestamp(xsdDateTime);

           Query query;

           try {
               query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
           } catch (Exception e) {
               logger.warn("Skipping benchmark because query failed to parse", e);
               return result;
           }

           // TODO For COUNT queries add the LSQ.countValue to the output model

           Stopwatch evalSw = Stopwatch.createStarted(); // Total time spent evaluating

           List<String> varNames = new ArrayList<>();

           boolean isResultCountComplete = false;
           long itemCount = 0; // We could use rs.getRowNumber() but let's not rely on it
           List<Binding> cache = new ArrayList<>();

           if (maxResultCountForCounting != 0 && maxByteSizeForCounting != 0) {
               logger.info("Benchmarking " + queryStr);
               Stopwatch retrievalSw = Stopwatch.createStarted();

               try(QueryExecution qe = conn.newQuery()
                       .timeout(executionTimeoutForRetrieval, TimeUnit.MILLISECONDS)
                       .query(query)
                       .build()) {
                   // https://github.com/apache/jena/issues/1384
                   // qe.setTimeout(connectionTimeoutForRetrieval, executionTimeoutForRetrieval);
                   // qe.setTimeout(executionTimeoutForRetrieval);

                   ResultSet rs = qe.execSelect();
                   varNames.addAll(rs.getResultVars());

                   long estimatedByteSize = 0;

                   while(rs.hasNext()) {
                       ++itemCount;

                       Binding binding = rs.nextBinding();

                       if(cache != null) {
                           // Estimate the size of the binding (e.g. I once had polygons in literals of size 50MB)
                           long bindingSizeContrib = binding.toString().length();
                           estimatedByteSize += bindingSizeContrib;

                           exceededMaxResultCountForSerialization = maxResultCountForSerialization >= 0
                                   && itemCount > maxResultCountForSerialization;

                           if(exceededMaxResultCountForSerialization) {
                               // Disable serialization but keep on counting
                               cache = null;
                           }

                           exceededMaxByteSizeForSerialization = maxByteSizeForSerialization >= 0
                                   && estimatedByteSize > maxByteSizeForSerialization;
                           if(exceededMaxByteSizeForSerialization) {
                               // Disable serialization but keep on counting
                               cache = null;
                           }


                           if(cache != null) {
                               cache.add(binding);
                           }
                       }

                       exceededMaxResultCountForCounting = maxResultCountForCounting >= 0
                               && itemCount > maxResultCountForCounting;
                       if(exceededMaxByteSizeForSerialization) {
                           break;
                       }

                       exceededMaxByteSizeForCounting = maxByteSizeForCounting >= 0
                               && estimatedByteSize > maxByteSizeForCounting;
                       if(exceededMaxByteSizeForSerialization) {
                           break;
                       }
                   }

                   if(exceededMaxResultCountForSerialization) {
                       result.setExceededMaxResultCountForSerialization(exceededMaxResultCountForSerialization);
                   }

                   if(exceededMaxByteSizeForSerialization) {
                       result.setExceededMaxByteSizeForSerialization(exceededMaxByteSizeForSerialization);
                   }


                   if(exceededMaxResultCountForCounting) {
                       result.setExceededMaxResultCountForCounting(exceededMaxResultCountForCounting);
                   }

                   if(exceededMaxByteSizeForCounting) {
                       result.setExceededMaxByteSizeForCounting(exceededMaxByteSizeForCounting);
                   }

                   // Try obtaining a count with a separate query
                   isResultCountComplete = !exceededMaxResultCountForCounting && !exceededMaxByteSizeForCounting;
//               } catch (QueryExecException ce) {
//                   // FIXME
               } catch (ConnectionLostException e) {
                   throw new ConnectionLostException(e);
               } catch (Exception e) {

                   // Set the cache to null so we don't serialize result sets of failed queries
                   cache = null;

                   logger.warn("Retrieval error: ", e);
    //                   String errorMsg = Optional.ofNullable(ExceptionUtils.getRootCause(e)).orElse(e).getMessage();
                   String errorMsg = ExceptionUtils.getStackTrace(e);
                   result.setRetrievalError(errorMsg);
               }

               BigDecimal retrievalDuration = new BigDecimal(retrievalSw.stop().elapsed(TimeUnit.NANOSECONDS))
                       .divide(new BigDecimal(1000000000));

               result.setRetrievalDuration(retrievalDuration);
           }


           if (!isResultCountComplete) {
               // Try to count using a query and discard the current elapsed time

               Long countItemLimit = maxCount >= 0 ? maxCount : null;
               // SparqlRx.fetchCountQuery(conn, query, countItemLimit, null)
               Stopwatch countingSw = null;
               try {

                   // Check whether to disable thee count limit for single pattern queries
                   if (!maxCountAffectsTp && countItemLimit != null) {
                       Map<Resource, Integer> features = ElementVisitorFeatureExtractor.getFeatures(query);

                       // Triple patterns and triple paths are counted separately so we need to sum them up
                       int tpCount = features.getOrDefault(LSQ.TriplePattern, 0)
                               + features.getOrDefault(LSQ.TriplePath, 0);

                       if (tpCount == 1) {
                           countItemLimit = null;
                       }
                   }


                   Entry<Var, Query> queryAndVar = QueryGenerationUtils.createQueryCount(query, countItemLimit, null);

                   Var countVar = queryAndVar.getKey();
                   Query countQuery = queryAndVar.getValue();

                   if (logger.isInfoEnabled()) {
                       logger.info("Counting " + countQuery);
                   }

                   countingSw = Stopwatch.createStarted();

                   try(QueryExecution qe = conn.newQuery()
                           .query(countQuery)
                           .timeout(executionTimeoutForCounting, TimeUnit.MILLISECONDS)
                           .build()) {
                       // qe.setTimeout(connectionTimeoutForCounting, executionTimeoutForCounting);
                       // https://github.com/apache/jena/issues/1384
                       // qe.setTimeout(executionTimeoutForCounting);
                       Number count = QueryExecutionUtils.fetchNumber(qe, countVar);
                       if(count != null) {
                           itemCount = count.longValue();

                           isResultCountComplete = countItemLimit == null || itemCount < countItemLimit;
                       }
                   }
               } catch (ConnectionLostException e) {
                   throw new ConnectionLostException(e);
               } catch(Exception e) {
                   if (logger.isWarnEnabled()) {
                       logger.warn("Counting error: ", e);
                   }
    //                   String errorMsg = Optional.ofNullable(ExceptionUtils.getRootCause(e)).orElse(e).getMessage();
                   String errorMsg = ExceptionUtils.getStackTrace(e);
                   result.setCountingError(errorMsg);
               }

               if (countingSw != null) {
                   BigDecimal countingDuration = new BigDecimal(countingSw.stop().elapsed(TimeUnit.NANOSECONDS))
                           .divide(new BigDecimal(1000000000));

                   result.setCountDuration(countingDuration);
               }
           }

           if(isResultCountComplete) {
               result.setResultSetSize(itemCount);
           }

           if(cache != null) {
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               ResultSet replay = ResultSetUtils.create(varNames, cache.iterator());
               ResultSetFormatter.outputAsJSON(baos, replay);
               result.setSerializedResult(baos.toString());
           }


           BigDecimal evalDuration = new BigDecimal(evalSw.stop().elapsed(TimeUnit.NANOSECONDS))
                   .divide(new BigDecimal(1000000000));


           result.setEvalDuration(evalDuration);

           if(logger.isInfoEnabled()) {
               String errMsg = result.getRetrievalError();
               logger.info("Benchmark result after " + evalDuration + " seconds: " + result.getResultSetSize() + " results"
                       + (errMsg == null ? " (success)" : " and error message: " + errMsg));
           }

           return result;
           //Calendar end = Calendar.getInstance();
           //Duration duration = Duration.between(start.toInstant(), end.toInstant());
    }


    public static Flowable<Entry<String, Dataset>> fetchDatasets(SparqlQueryConnection conn, Iterable<Node> graphNames) {
        Query query = createFetchNamedGraphQuery(graphNames);
        QuadAcc quadAcc = new QuadAcc();
        quadAcc.addQuad(Quads.GSPO);
        Template template = new Template(quadAcc);
        //template.getQuads().add(gspo);

        return SparqlRx.execSelectRaw(() -> conn.query(query))
            .concatMap(QueryFlowOps.createMapperQuads(template)::apply)
            .compose(FlowOfQuadsOps.groupConsecutiveQuadsRaw(Quad::getGraph, DatasetGraphFactory::create))
            .map(e -> new SimpleEntry<>(e.getKey().getURI(), DatasetFactory.wrap(e.getValue())));
    }


    /**
     * SELECT * {
     *   GRAPH ?g { ?s ?p ?o }
     *   FILTER(?g IN (args))
     * }
     *
     */
    public static Query createFetchNamedGraphQuery(Iterable<Node> graphNames) {
        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryResultStar(true);
        result.setQueryPattern(
                ElementUtils.groupIfNeeded(
                        ElementUtils.createElement(Quads.GSPO),
                        new ElementFilter(ExprUtils.oneOf(Vars.g, graphNames))
                ));
        result.addOrderBy(Vars.g, Query.ORDER_ASCENDING);

        return result;
    }



}





/*
 * TpInBgp
 *
 * CONSTRUCT { ?s lsq:tpSel ?o } {
 *   ?root lsq:hasBgp [ lsq:hasTpInBgp [ lsq:hasTP [ lsq:hasTPExec ?s ] ] ]
 *   ?s lsq:benchmarkRun ?env_run ; lsq:resultSetSize ?rs
 *   ?env_run lsq:datasetSize ?datasetSize
 *   BIND(?rs / ?datasetSize AS ?o)
 * }
 */

//Long datasetSize = config.getDatasetSize();
//if(datasetSize != null) {
//    //for(SpinBgp xbgp : spinRoot.getBgps()) {
//    for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//        SpinBgp xbgp = bgpExec.getBgp();
//        for(TpInBgp tpInBgp : xbgp.getTpInBgp()) {
//            LsqTriplePattern tp = tpInBgp.getTriplePattern();
//
//            LsqQuery extensionQuery = tp.getExtensionQuery();
//            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
//            LocalExecution le = leMap.get(expRun);
//            if(le == null) {
//                logger.warn("Missing local execution result");
//            } else {
//                Set<TpInBgpExec> tpInBgpExecs = bgpExec.getTpInBgpExecs();
///*                                    if(tpInBgpExec == null) {
//                    Long rsSize = le.getQueryExec().getResultSetSize();
//                    BigDecimal value = new BigDecimal(rsSize).divide(new BigDecimal(datasetSize));
////                    le.addLiteral(LSQ.tpSel, value);
//
//                    tpInBgpExec = model.createResource(tpInBgpExec.class);
//                    tpInBgpExec
//                        .setTpInBgp(tpInBgp)
//                        .setBgpExec(bgpExec)
//                        .setTpExec()
//                        .setSelectivity(value)
//                        ;
//                }
//                */
//
//
//            }
//        }
//    }
//}


/*
 * BGP restricted tp sel
 * CONSTRUCT { ?s lsq:tpSel ?o } {
 *   ?root lsq:hasBgps [ lsq:hasTP [ lsq:hasTPExec ?s ] ]
 *   ?s lsq:benchmarkRun ?env_run ; lsq:resultSetSize ?rs
 *   ?env_run lsq:datasetSize ?datasetSize
 *   BIND(?rs / ?datasetSize AS ?o)
 * }
 */
//{
//    for(SpinBgp bgp : spinRoot.getBgps()) {
//        for(TpInBgp tpInBgp : bgp.getTpInBgp()) {
//    for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//        for(TpInBgpExec tpInBgpExec : bgpExec.getTpInBgpExecs()) {
//    for(SpinBgpNode bgpNode : spinRoot.getBgps()) {
//        //for(TpInBgp tpInBgpExec : expRoot.getTpInBgpExec()) {
//            LsqTriplePattern tp = tpInBgp.getTriplePattern();
//
//            LocalExecution bgpLe = bgp.getExtensionQuery().getLocalExecutionMap().get(expRun);
//            LocalExecution tpLe = tp.getExtensionQuery().getLocalExecutionMap().get(expRun);
//
//            Long bgpSize = bgpLe.getQueryExec().getResultSetSize();
//            Long tpSize = tpLe.getQueryExec().getResultSetSize();
//
//            BigDecimal bgpRestrictedTpSel = bgpSize == 0 ? new BigDecimal(0) : new BigDecimal(tpSize).divide(new BigDecimal(bgpSize));
//
////                            LocalExecution le = tp.getExtensionQuery().indexLocalExecs().get(expRun);
//
//            TpInBgpExec tpInBgpExec = model.createResource().as(TpInBgpExec.class);
//            tpInBgpExec
//                .setBgpExec(bgpLe)
//                .setSelectivity(bgpRestrictedTpSel);
////            tpInBgpExec.setBenchmarkRun(expRun);
//
//        }
//    }
//}


// Update join restricted tp selectivity
// For this purpose iterate the prior computed bgpExecs
//{
//    for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//        SpinBgp bgp = bgpExec.getBgp();
//        for(SpinBgpNode bgpNode : bgp.getBgpNodes()) {
////    for(SpinBgp bgp : spinRoot.getBgps()) {
////        for(SpinBgpNode bgpNode : bgp.getBgpNodes()) {
//            if(bgpNode.toJenaNode().isVariable()) {
//
//                LocalExecution bgpLe = bgpNode.getSubBgp().getExtensionQuery().getLocalExecutionMap().get(expRun);
//                LocalExecution bgpNodeLe = bgpNode.getJoinExtensionQuery().getLocalExecutionMap().get(expRun);
//
//
//                Long bgpExtSize = bgpLe.getQueryExec().getResultSetSize();
//                Long bgpNodeExtSize = bgpNodeLe.getQueryExec().getResultSetSize();
//
//                BigDecimal bgpRestrictedJoinVarSel = bgpExtSize == 0 ? new BigDecimal(0) : new BigDecimal(bgpNodeExtSize).divide(new BigDecimal(bgpExtSize));
//
//                JoinVertexExec bgpVarExec = model.createResource().as(JoinVertexExec.class);
//                // bgpVarExec.setBenchmarkRun(expRun);
////                bgpVarExec.setBgpNode(bgpNode);
//                bgpVarExec
//                    .setBgpExec(bgpExec)
//                    .setBgpRestrictedSelectivitiy(bgpRestrictedJoinVarSel);
//
//                // BgpVarExec is a child of BgpExec, hence there is no need for a back pointer
//                //bgpVarExec.setBgpExec(bgpLe);
//            }
//        }
//    }
//}



//
//// LookupServiceUtils.createLookupService(indexConn, );
//// Triple t = new Triple(Vars.s, LSQ.execStatus.asNode(), Vars.o);
//Triple t = new Triple(Vars.s, Vars.p, Vars.o);
//Quad quad = new Quad(Vars.g, t);
//BasicPattern basicPatteren = new BasicPattern();
//basicPatteren.add(t);
////QuadPattern quadPattern = new QuadPattern();
////quadPattern.add(quad);
//
//DataQuery<RDFNode> dq = new DataQueryImpl<>(
//      indexConn,
//      ElementUtils.createElement(quad),
//      Vars.s,
//      new Template(basicPatteren),
//      RDFNode.class
//);
//
//
//System.out.println("Generated: " + dq.toConstructQuery());

