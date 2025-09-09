package org.aksw.simba.lsq;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jenax.arq.util.quad.DatasetGraphUtils;
import org.aksw.jenax.dataaccess.sparql.connection.reconnect.SparqlQueryConnectionWithReconnect;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.io.input.registry.LsqInputFormatRegistry;
import org.aksw.simba.lsq.core.rx.io.input.LsqRxIo;
import org.aksw.simba.lsq.enricher.benchmark.core.LsqBenchmarkProcessor;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;
import org.aksw.simba.lsq.enricher.core.LsqEnricherShell;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LsqBenchmarkParams;
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
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteSource;

import io.reactivex.rxjava3.core.Flowable;

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

//        LsqLogRecordRdfizer rdfizer = LsqLogRecordRdfizerFull.newBuilder()
//            .setServiceUrl("htttp://www.example.org/lsq/sparql")
//            .build();
//
        SerializableSupplier<LsqEnricherRegistry> registrySupplier = LsqEnricherRegistry::get;
        LsqEnricherShell enricherFactory = new LsqEnricherShell("http://lsq.aksw.org/", LsqEnricherRegistry.get().getKeys(), registrySupplier);

        Function<Resource, Resource> rdfizer = enricherFactory.get();


        // TODO Use the registry as an internal default
        Map<String, ResourceParser> logFmtRegistry = LsqInputFormatRegistry.createDefaultLogFmtRegistry();

        List<Resource> list = LsqRxIo.createReader(inputLog, byteSource::openStream, "sparql", logFmtRegistry, rdfizer).toList().blockingGet();
        Assert.assertEquals(list.size(), 1);
        Resource r = list.get(0);
        Dataset ds = DatasetFactory.create();
        ds.getPrefixMapping().setNsPrefix("lsq", "http://lsq.aksw.org/");
        ds.getPrefixMapping().setNsPrefix("lsqo", LSQ.NS);
        ds.getDefaultModel().add(r.getModel());

        Instant testTime = Instant.parse("2020-01-02T03:04:05.678Z");

        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);

        String baseIri = LSQ.defaultLsqrNs;
        String datasetLabel = "lsq-test-dataset-01";

        String expId = LsqUtils.createExperimentId(datasetLabel);
        String expIri = baseIri + expId;

        Model model = ModelFactory.createDefaultModel();
        ExperimentConfig expCfg = model.createResource(expIri).as(ExperimentConfig.class);
        String runId = expCfg.getIdentifier();

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

        ExperimentExec expExec = LsqUtils.createExperimentExec(model, expCfg, testTime);
        ExperimentRun expRun = LsqUtils.createExperimentRun(model, expExec, 0); //testTime);

        LsqBenchmarkParams.setDefaults(expCfg);

        // expExec -> comomn settings for multiple runs
        // expRun -> expExec + runId + start time stamp


        Flowable<LsqQuery> queryFlow = Flowable.just(r.as(LsqQuery.class));

        Dataset dataset = TDB2Factory.createDataset();
        try (OutputStream outStream = StdIo.openStdOutWithCloseShield();
            RDFConnection indexConn = RDFConnection.connect(dataset)) {
            StreamRDF out = StreamRDFWriter.getWriterStream(outStream, RDFFormat.TRIG_BLOCKS);
            out.start();
            try (RdfDataPod dataPod = DataPods.fromDataset(testData)) {
                try (SparqlQueryConnection benchmarkConn =
                        SparqlQueryConnectionWithReconnect.create(() -> dataPod.getConnection())) {
                    LsqBenchmarkProcessor.process(out, queryFlow, baseIri, expCfg, expExec, expRun, rdfizer, benchmarkConn, indexConn);
                }
            }
            out.finish();
        } finally {
            dataset.close();
        }
    }


    public static class Builder {
        /* */
        private Supplier<Instant> timestampSupplier;

        public Builder setBenchTimestampFixed(Instant instant) {
            this.timestampSupplier = () -> instant;
            return this;
        }


        public Object build() {
            Supplier<Instant> finalTs = Optional.ofNullable(timestampSupplier).orElse(Instant::now);

            return null;
        }

    }

    // If custom formats were needed:
    // Instant instant = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(TimeZone.getTimeZone("UTC").toZoneId())
    // .parse(timestampUtcStr));
}
