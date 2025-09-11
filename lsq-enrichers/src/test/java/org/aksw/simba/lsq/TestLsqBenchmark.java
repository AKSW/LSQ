package org.aksw.simba.lsq;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.quad.DatasetGraphUtils;
import org.aksw.jenax.dataaccess.sparql.connection.reconnect.SparqlQueryConnectionWithReconnect;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.io.input.registry.LsqInputFormatRegistry;
import org.aksw.simba.lsq.core.rx.io.input.LsqLogRecordRdfizer;
import org.aksw.simba.lsq.core.rx.io.input.LsqLogRecordRdfizerFull;
import org.aksw.simba.lsq.core.rx.io.input.LsqRxIo;
import org.aksw.simba.lsq.enricher.benchmark.core.LsqBenchmarkProcessor;
import org.aksw.simba.lsq.enricher.benchmark.core.LsqBenchmarker;
import org.aksw.simba.lsq.enricher.benchmark.core.QueryPack;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;
import org.aksw.simba.lsq.enricher.core.LsqEnricherShell;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LsqBenchmarkParamsMutable;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.util.LsqUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteSource;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

public class TestLsqBenchmark {
    @Test
    public void test01() throws Exception {
        Dataset testData = RDFParserBuilder.create().fromString("""
            PREFIX eg: <http://www.example.org/>
            eg:a eg:p1 eg:b .
            eg:a eg:p2 eg:c .
            eg:c eg:p1 eg:d .
        """).lang(Lang.TRIG).toDataset();

        String inputLog = """
            PREFIX eg: <http://www.example.org/> SELECT * { ?s eg:p2 ?o . ?o ?x ?y }
        """;

        ByteSource byteSource = ByteSource.wrap(inputLog.getBytes(StandardCharsets.UTF_8));

        // Convert a log-entry-centric resource into a query-centric one.
        LsqLogRecordRdfizer logToQueryRdfizer = LsqLogRecordRdfizerFull.newBuilder()
            .setServiceUrl("htttp://www.example.org/lsq/sparql")
            .build();

        // TODO Use the registry as an internal default - remove need for explicit creation.
        Map<String, ResourceParser> logFmtRegistry = LsqInputFormatRegistry.createDefaultLogFmtRegistry();

        List<Resource> list = LsqRxIo.createReader(inputLog, byteSource::openStream, "sparql", logFmtRegistry, logToQueryRdfizer).toList().blockingGet();
        Assert.assertEquals(list.size(), 1);
        Resource r = list.get(0);
        Dataset ds = DatasetFactory.create();
        LSQ.addPrefixes(ds.getDefaultModel());

        Instant testTime = Instant.parse("2020-01-02T03:04:05.678Z");
        String baseIri = LSQ.defaultLsqrNs;
        String datasetLabel = "lsq-test-dataset-01";

        // RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);

        String expId = LsqUtils.createExperimentId(datasetLabel);
        String expIri = baseIri + expId;

        Model model = ModelFactory.createDefaultModel();
        ExperimentConfig expCfg = model.createResource(expIri).as(ExperimentConfig.class);
        // String runId = expCfg.getIdentifier();

        expCfg
            .setIdentifier(expId)
            // .setCreationDate(nowCal)
            // .setDataRef(dataRef)
            .setUserAgent("dummy-user-agent")
            .benchmarkSecondaryQueries(true)
            .setDatasetSize(DatasetGraphUtils.tupleCount(testData.asDatasetGraph()))
            .setDatasetLabel(datasetLabel)
            // .setDatasetIri(datasetIri) // a shared iri that identifies a dataset may not exist and is thus is optional
            .setBaseIri(baseIri)
            ;

        // ExperimentExec expExec = LsqUtils.createExperimentExec(model, expCfg, testTime);
        // ExperimentRun expRun = LsqUtils.createExperimentRun(model, expExec, 0); //testTime);

        LsqBenchmarkParamsMutable.setDefaults(expCfg);

        // expExec -> comomn settings for multiple runs
        // expRun -> expExec + runId + start time stamp

        SerializableSupplier<LsqEnricherRegistry> registrySupplier = LsqEnricherRegistry::get;
        LsqEnricherShell enricherFactory = new LsqEnricherShell("http://lsq.aksw.org/", LsqEnricherRegistry.get().getKeys(), registrySupplier);

        Function<Resource, Resource> enricher = enricherFactory.get();
        Flowable<LsqQuery> rawQueryFlow = Flowable.just(r.as(LsqQuery.class));

        boolean benchmarkSecondaryQueries = Optional.ofNullable(expCfg.benchmarkSecondaryQueries()).orElse(false);
        Flowable<List<QueryPack>> queryFlow = rawQueryFlow
            .concatMapMaybe(lsqQuery -> {
                Maybe<LsqQuery> rr = LsqBenchmarkProcessor.safeMaybe(() -> enricher.apply(lsqQuery).as(LsqQuery.class));
                return rr;
            })
            .map(lsqQuery -> benchmarkSecondaryQueries ? LsqBenchmarkProcessor.extractAllQueries(lsqQuery) : new QueryPack(lsqQuery, List.of()))
            .buffer(1)
            ;

        LsqBenchmarker benchmarkProcessor = LsqBenchmarker.newBuilder()
            .setFallbackDatasetLabel(datasetLabel)
            .setFallbackBenchmarkTime(testTime)
            .setLsqBaseIri(baseIri)
            .setBenchmarkDataset(testData)
            .setEnricher(logToQueryRdfizer)
            .build();

        run(testData, queryFlow, benchmarkProcessor);
    }

    public static void run(Dataset testData, Flowable<List<QueryPack>> queryFlow, LsqBenchmarker benchEngine) throws Exception {
        Dataset dataset = TDB2Factory.createDataset();
        try (OutputStream outStream = StdIo.openStdOutWithCloseShield();
            RDFConnection indexConn = RDFConnection.connect(dataset)) {
            StreamRDF out = StreamRDFWriter.getWriterStream(outStream, RDFFormat.TRIG_BLOCKS);
            StreamRDFOps.sendPrefixesToStream(LSQ.addPrefixes(new PrefixMappingImpl()), out);
            out.start();
            try (RdfDataPod dataPod = DataPods.fromDataset(testData)) {
                try (SparqlQueryConnection benchmarkConn =
                    SparqlQueryConnectionWithReconnect.create(() -> dataPod.getConnection())) {

                    Flowable<ResourceInDataset> flowable = queryFlow.flatMapIterable(
                            pack-> benchEngine.processBatchOfQueries(pack, indexConn));

                    Iterable<ResourceInDataset> items = flowable.blockingIterable();
                    for(ResourceInDataset item : items) {
                        StreamRDFOps.sendDatasetToStream(item.getDataset().asDatasetGraph(), out);
                    }

                }
            }
            out.finish();
        } finally {
            dataset.close();
        }
    }

    // If custom formats were needed:
    // Instant instant = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(TimeZone.getTimeZone("UTC").toZoneId())
    // .parse(timestampUtcStr));
}
