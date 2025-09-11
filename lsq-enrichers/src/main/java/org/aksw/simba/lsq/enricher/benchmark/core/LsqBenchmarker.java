package org.aksw.simba.lsq.enricher.benchmark.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.DatasetGraphOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.aksw.jenax.arq.util.triple.ModelUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.jenax.reprogen.util.Skolemize;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;
import org.aksw.simba.lsq.enricher.core.LsqEnricherShell;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqBenchmarkParamsMutable;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.model.util.LsqUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LsqBenchmarker {
    private static final Logger logger = LoggerFactory.getLogger(LsqBenchmarker.class);

    private String lsqBaseIri;
    private ExperimentConfig expConfig;
    private ExperimentExec expExec;
    private ExperimentRun expRun;
    // private Function<Resource, Resource> enricher;
    private SparqlQueryConnection benchmarkConn;
    // private RDFConnection indexConn;

    private LsqBenchmarker(String lsqBaseIri, ExperimentConfig expConfig, ExperimentExec expExec, ExperimentRun expRun,
            // Function<Resource, Resource> enricher,
            SparqlQueryConnection benchmarkConn
            // RDFConnection indexConn
        ) {
        super();
        this.lsqBaseIri = lsqBaseIri;
        this.expConfig = expConfig;
        this.expExec = expExec;
        this.expRun = expRun;
        // this.enricher = enricher;
        this.benchmarkConn = benchmarkConn;
        // this.indexConn = indexConn;
    }

    /**
     * The input "List<Set<LsqQuery>> batch" is a list of query packs (pack = linked hash set).
     * The database that tracks which queries were processed is updated in bulk for the whole batch.
     *
     * @param batch A batch of query packs.
     *              The first query in a pack is the primary one, all others are secondary queries.
     * @param lsqBaseIri
     * @param expConfig
     * @param expExec
     * @param expRun
     * @param benchmarkConn
     * @param lsqQueryExecFn
     * @param indexConn
     * @return
     */
    public List<ResourceInDataset> processBatchOfQueries(List<QueryPack> batch, RDFConnection indexConn) {

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
        Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> lsqQueryBaseIriFn.apply(lsqQuery.getHash()) + expSuffix;

        // Skolemization is blocked for all resources appearing in the unionModel:
        // The staticModel is used as a base layer with blank nodes.
        //   Skolemize.skolemize(resource, baseLayer) skolemizes the reachable graph of resource
        //   Triples from the staticModel are not copied into the benchmark outcome graph.
        Model staticModel = ModelUtils.union(expConfig.getModel(), expExec.getModel(), expRun.getModel());

        // Combine the query hash and the exprRun id to form the benchmark task id.
        List<ResourceInDataset> result = new ArrayList<>();

        Map<Node, LsqQuery> inputTasks = new HashMap<>();
        for (QueryPack pack : batch) {
            for (LsqQuery query : pack.list()) {
                String taskIdStr = lsqQueryExecFn.apply(query);
                Node taskId = NodeFactory.createURI(taskIdStr);
                if (inputTasks.containsKey(taskId)) {
                    throw new RuntimeException("Task already exists: " + taskId);
                }
                inputTasks.put(taskId, query);
            }
        }

        Map<String, Dataset> taskIdToDataset = Txn.calculate(indexConn, () ->
            LsqBenchmarkProcessor.fetchDatasets(indexConn, inputTasks.keySet())
            .toMap(Entry::getKey, Entry::getValue)
            .blockingGet());

        // Obtain the set of query strings already in the store
        Set<String> completedTaskIds = new LinkedHashSet<>(taskIdToDataset.keySet());;

        Map<Node, LsqQuery> pendingTasks = inputTasks.entrySet().stream()// batch.stream()
            .filter(e -> !completedTaskIds.contains(e.getKey().toString()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        List<Quad> inserts = new ArrayList<>();

        // The logic is to use TDB2 as an index for which queries have been processed.
        // The dataset for each query is NOT stored in the TDB2 but returned via the result stream.
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

            LsqBenchmarkProcessor.rdfizeQueryExecutionBenchmark(benchmarkConn, queryStr, newQueryExec, expConfig);

            newLsqQuery.getLocalExecutions().add(newLocalExec);
            newLocalExec.setBenchmarkRun(expRun);
            newLocalExec.setQueryExec(newQueryExec);

            // Skolemization is blocked for all resources appearing in the unionModel
            Model unionModel = ModelUtils.union(staticModel, lsqQuery.getModel());

            // So we only skolemize all resources related to the newLocalExec
            LocalExecution finalLocalExec = Skolemize.skolemize(newLocalExec, unionModel, lsqBaseIri, LocalExecution.class);

            Dataset newDataset = new DatasetOneNgImpl(DatasetGraphOneNgImpl.create(queryExecId, finalLocalExec.getModel().getGraph()));
            ExtendedIterator<Triple> it = newQueryExec.getModel().getGraph().find();
            try {
                while (it.hasNext()) {
                    Triple t = it.next();
                    inserts.add(Quad.create(queryExecId, t));
                }
            } finally {
                it.close();
            }

            inserts.add(new Quad(queryExecId, queryExecId, LSQ.execStatus.asNode(), NodeFactory.createLiteralString("processed")));
            taskIdToDataset.put(queryExecIri, newDataset);
        }

        UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(inserts, null);
        Txn.executeWrite(indexConn, () -> indexConn.update(ur));

        // Remove the execStatus "processed" triples from the COPIES[!] of the fetched datasets
        // The copies will become part of the result flowable of this method.
        for(Dataset ds : taskIdToDataset.values()) {
            for(Entry<String, Model> e : DatasetUtils.listModels(ds)) {
                e.getValue().removeAll(null, LSQ.execStatus, null);
            }
        }

        // Txn.executeRead(indexConn, () -> System.out.println(ResultSetFormatter.asText(indexConn.query("SELECT ?s { ?s ?p ?o }").execSelect())));

        for(QueryPack pack : batch) {

            List<LsqQuery> queries = pack.list();
            logger.info("Processing pack of size: " + queries.size());

            // TODO Move all the code into a nice processPack method of a new class
            try {
                // The primary query is assumed to always be the first element of a pack
                LsqQuery primaryQueryRaw = pack.primaryQuery();
                String primaryQueryExecId = lsqQueryExecFn.apply(primaryQueryRaw);

                if (completedTaskIds.contains(primaryQueryExecId)) {
                    logger.info("Primary benchmark task " + primaryQueryExecId + " has already been processed and emitted");
                    continue;
                }


                Model primaryQueryModel = ModelFactory.createDefaultModel();

                // We need to add the config model in order to include the benchmark run id
                // We remove the config once we are done

                // TODO We should ensure that only the minimal necessary config model is added
                // expRoot.getModel().add(configModel);

//                primaryQueryModel.add(configModel);
//                primaryQueryModel.add(expRun.getModel());


                // Extend the rootQuery's model with all related query executions
                for (LsqQuery item : queries) {
                    String key = lsqQueryExecFn.apply(item);

//                    if (completedTaskIds.contains(key)) {
//                        logger.info("Secondary Benchmark task " + key + " has already been processed and emitted");
//                        continue;
//                    }

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
                    if (false) {
                        System.err.println(primaryQuery.getHash());
                        System.err.println("*******************************************");
                        RDFDataMgr.write(System.err, primaryQuery.getModel(), RDFFormat.TURTLE_PRETTY);
                    }

                    LsqExec.createAllExecs(primaryQuery, expRun);
                }

                Skolemize.skolemize(primaryQuery, staticModel, lsqBaseIri, LsqQuery.class);

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


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder{
        private Builder() { super(); }

        private String lsqBaseIri;

        private ExperimentConfig expConfig;

        // Fallbacks are used if expConfig is absent.
        private String fallbackDatasetLabel;
        private Instant fallbackBenchmarkTime;
        private Long fallbackDatasetSize;

        private ExperimentExec expExec;
        private ExperimentRun expRun;
        private Function<Resource, Resource> enricher;

        private SparqlQueryConnection benchmarkConn;
        private RDFDataSource benchmarkDataSource;

        private RDFConnection indexConn;

        public Builder setLsqBaseIri(String lsqBaseIri) {
            this.lsqBaseIri = lsqBaseIri;
            return this;
        }

        public Builder setExpConfig(ExperimentConfig expConfig) {
            this.expConfig = expConfig;
            return this;
        }

        public Builder setExpExec(ExperimentExec expExec) {
            this.expExec = expExec;
            return this;
        }

        public Builder setExpRun(ExperimentRun expRun) {
            this.expRun = expRun;
            return this;
        }

        public Builder setEnricher(Function<Resource, Resource> enricher) {
            this.enricher = enricher;
            return this;
        }

        public Builder setBenchmarkConn(SparqlQueryConnection benchmarkConn) {
            this.benchmarkConn = benchmarkConn;
            return this;
        }

//        public Builder setBenchmarkConn(Dataset dataset) {
//            SparqlQueryConnection conn = RDFConnection.connect(dataset);
//            setBenchmarkConn(conn);
//            return this;
//        }

        public Builder setIndexConn(RDFConnection indexConn) {
            this.indexConn = indexConn;
            return this;
        }

        public Builder setFallbackDatasetLabel(String fallbackDatasetLabel) {
            this.fallbackDatasetLabel = fallbackDatasetLabel;
            return this;
        }

        public Builder setFallbackBenchmarkTime(Instant fallbackBenchmarkTime) {
            this.fallbackBenchmarkTime = fallbackBenchmarkTime;
            return this;
        }

        public Builder setFallbackDatasetSize(Long fallbackDatasetSize) {
            this.fallbackDatasetSize = fallbackDatasetSize;
            return this;
        }

        public Builder setBenchmarkDataset(Dataset dataset) {
            this.benchmarkDataSource = RDFDataSources.of(dataset);
            SparqlQueryConnection conn = benchmarkDataSource.getConnection();
            setBenchmarkConn(conn);
            return this;
        }
        // DatasetGraphUtils.tupleCount(testData.asDatasetGraph()

        public LsqBenchmarker build() {
            String finalLsqBaseIri = Optional.ofNullable(lsqBaseIri).orElse(LSQ.defaultLsqrNs);

            String finalDatasetLabel = Objects.requireNonNull(Optional
                .ofNullable(expConfig).map(ExperimentConfig::getDatasetLabel)
                .orElse(fallbackDatasetLabel), "No dataset label configured.");

            Long finalDatasetSize = Optional
                .ofNullable(expConfig).map(ExperimentConfig::getDatasetSize)
                .orElse(null);

            Objects.requireNonNull(benchmarkConn, "Connection to benchmark data not configured.");

            if (finalDatasetSize == null) {
                finalDatasetSize = QueryExecUtils.fetchDatasetSize(benchmarkConn::query);
            }

            Objects.requireNonNull(finalDatasetSize, "No dataset size configured.");

            Instant finalBenchmarkTime = Objects.requireNonNull(Optional
                .ofNullable(expExec).map(ExperimentExec::getTimestampAsInstant)
                .orElse(fallbackBenchmarkTime), "No benchmark timestamp configured.");

            Model model = null;

            ExperimentConfig finalExpCfg = expConfig;
            if (finalExpCfg == null) {
                if (model == null) {
                    model = ModelFactory.createDefaultModel();
                }

                String expId = LsqUtils.createExperimentId(finalDatasetLabel);
                String expIri = finalLsqBaseIri + expId;

                finalExpCfg = model.createResource(expIri).as(ExperimentConfig.class);
                String runId = finalExpCfg.getIdentifier();

                finalExpCfg
                    .setIdentifier(expId)
                    // .setCreationDate(nowCal)
                    // .setDataRef(dataRef)
                    .setUserAgent("dummy-user-agent")
                    .benchmarkSecondaryQueries(true)
                    .setDatasetSize(finalDatasetSize)
                    .setDatasetLabel(finalDatasetLabel)
                    // .setDatasetIri(datasetIri) // a shared iri that identifies a dataset may not exist and is thus is optional
                    .setBaseIri(finalLsqBaseIri)
                    ;
            }

            ExperimentExec finalExpExec = expExec;
            if (expExec == null) {
                if (model == null) {
                    model = ModelFactory.createDefaultModel();
                }

                finalExpExec = LsqUtils.createExperimentExec(model, finalExpCfg, finalBenchmarkTime);
            }

            ExperimentRun finalExpRun = Optional.ofNullable(expRun)
                .orElse(LsqUtils.createExperimentRun(model, finalExpExec, 0)); //testTime);

            LsqBenchmarkParamsMutable.setDefaults(finalExpCfg);

            // expExec -> comomn settings for multiple runs
            // expRun -> expExec + runId + start time stamp
            // SerializableSupplier<LsqEnricherRegistry> registrySupplier = regi

//            SerializableSupplier<LsqEnricherRegistry> registrySupplier = LsqEnricherRegistry::get;
//            LsqEnricherShell enricherFactory = new LsqEnricherShell("http://lsq.aksw.org/", LsqEnricherRegistry.get().getKeys(), registrySupplier);
//
//            Function<Resource, Resource> enricher = enricherFactory.get();


            return new LsqBenchmarker(finalLsqBaseIri, finalExpCfg, finalExpExec, finalExpRun,
                    // enricher,
                    benchmarkConn);
                    //indexConn);
        }
    }
}
