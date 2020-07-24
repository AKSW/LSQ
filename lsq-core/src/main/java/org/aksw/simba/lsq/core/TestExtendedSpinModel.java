package org.aksw.simba.lsq.core;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.http.repository.api.HttpRepository;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;
import org.aksw.jena_sparql_api.rx.DatasetGraphOpsRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.rx.op.OperatorObserveThroughput;
import org.aksw.jena_sparql_api.rx.query_flow.QueryFlowOps;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.spinx.model.JoinVertexExec;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinBgp;
import org.aksw.simba.lsq.spinx.model.SpinBgpExec;
import org.aksw.simba.lsq.spinx.model.SpinBgpNode;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.spinx.model.TpExec;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.spinx.model.TpInBgpExec;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;
import org.aksw.sparql_integrate.ngs.cli.main.ExceptionUtils;
import org.aksw.sparql_integrate.ngs.cli.main.ResourceInDatasetFlowOps;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.primitives.Ints;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.disposables.Disposable;


interface PathHasher
{
    HashCode hash(SeekableByteChannel channel) throws IOException;
    HashCode hash(Path path) throws IOException;
}

/**
 * Hashes file content
 * By default, the hash considers the file size and the first and last 16MB of content
 *
 * @author raven
 *
 */
class PathHasherImpl
    implements PathHasher
{
    protected HashFunction hashFunction;

    /**
     * number of bytes to use for hashing from start and tail.
     * if the file size is less than 2 * numBytes, the whole file will be hashed
     *
     */
    protected int numBytes;

    public static PathHasher createDefault() {
        return new PathHasherImpl(Hashing.sha256(), 16 * 1024 * 1024);
    }

    public PathHasherImpl(HashFunction hashFunction, int numBytes) {
        super();
        this.hashFunction = hashFunction;
        this.numBytes = numBytes;
    }

    @Override
    public HashCode hash(Path path) throws IOException {
        HashCode result;
        try(SeekableByteChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            result = hash(channel);
        }

        return result;
    }

    @Override
    public HashCode hash(SeekableByteChannel channel) throws IOException {
        long channelSize = channel.size();
        Hasher hasher = hashFunction.newHasher();

        hasher.putLong(channelSize);
        hasher.putChar('-');

        Iterable<Entry<Long, Integer>> posAndLens;
        if(channelSize < numBytes * 2) {
            posAndLens = Collections.singletonMap(0l, Ints.checkedCast(channelSize)).entrySet();
        } else {
            posAndLens = ImmutableMap.<Long, Integer>builder()
                    .put(0l, numBytes)
                    .put(channelSize - numBytes, numBytes)
                    .build()
                    .entrySet()
                    ;
        }

        ByteBuffer buffer = null;
        for(Entry<Long, Integer> e : posAndLens) {
            Long pos = e.getKey();
            Integer len = e.getValue();

            if(buffer == null || buffer.remaining() < len) {
                buffer = ByteBuffer.wrap(new byte[len]);
            }

            channel.position(pos);
            channel.read(buffer.duplicate());
            hasher.putBytes(buffer.duplicate());
        }

        HashCode result = hasher.hash();
        return result;
    }
}

interface VersionedTransform<T>
//    extends MvnEntityCore
{
    public String getId();
    public String getHash();

}

//class DatasetTransform
//	extends VersionedTransform<Dataset, Dataset>
//{
//
//}

class RdfDerive {
    public static void derive(
            HttpRepository repo,
            Flowable<Dataset> input,
            String transformId,
            FlowableTransformer<Dataset, Dataset> transformer
            ) {

    }

}

public class TestExtendedSpinModel {
    private static final Logger logger = LoggerFactory.getLogger(TestExtendedSpinModel.class);


    public static void createLsqIndex() throws IOException {
        Path path = null;
        PathHasher hasher = PathHasherImpl.createDefault();
        hasher.hash(path);


        // We should use the dcat system here to track of created indexes...
        // Create a hash from head, tail
    }

    public static void createIndexBgps(Flowable<LsqQuery> flow) {
        // TODO How to get the shape triples??

        //SorterFactory sf = new SorterFactoryFromSysCall();
        CmdNgsSort sortCmd = new CmdNgsSort();
        SparqlQueryParser sparqlParser = SparqlQueryParserImpl.create();
        OutputStream out = new FileOutputStream(FileDescriptor.out);

        try {
            flow
                .flatMap(x -> Flowable.fromIterable(x.getSpinQuery().as(SpinQueryEx.class).getBgps()))
                .flatMap(bgp -> Flowable.fromIterable(bgp.getTriplePatterns()))
                .map(tp -> ResourceInDatasetImpl.createFromCopyIntoResourceGraph(tp))
                .compose(ResourceInDatasetFlowOps.createTransformerFromGroupedTransform(
                        ResourceInDatasetFlowOps.createSystemSorter(sortCmd, sparqlParser)))
                .map(rid -> rid.getDataset())
                .compose(RDFDataMgrRx.createDatasetWriter(out, RDFFormat.TRIG_PRETTY))
                .singleElement()
                .blockingGet()
                ;

        } catch (Exception e) {
            ExceptionUtils.rethrowIfNotBrokenPipe(e);
        }
    }

    public static void createIndexTriplePatterns(Flowable<LsqQuery> flow) {

    }


    public static void main(String[] args) {
        foo();
//        Flowable<LsqQuery> flow = RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
//                .map(r -> r.as(LsqQuery.class));
//
//        createIndexBgps(flow);
    }


    public static LsqQuery updateLsqQueryIris(
            LsqQuery start,
            Function<? super LsqQuery, String> genIri)
    {
        LsqQuery result = renameResources(
                start,
                LsqQuery.class,
                r -> r.getModel().listResourcesWithProperty(LSQ.text),
                r -> genIri.apply(r)
                );
        return result;
    }

    /**
     * Update all matching resources in a model
     *
     * @param start
     * @param resAndHashToIri
     * @return
     */
    public static <T extends Resource> T renameResources(
            T start,
            Class<T> clazz,
            Function<? super Resource, ? extends Iterator<? extends RDFNode>> listReosurces,
            Function<? super T, String> resAndHashToIri) {
        // Rename the query resources - Done outside of this method
        T result = start;
        //Set<Resource> qs = model.listResourcesWithProperty(LSQ.text).toSet();

        Iterator<? extends RDFNode> it = listReosurces.apply(start);
        while(it.hasNext()) {
            RDFNode tmpQ = it.next();
            T q = tmpQ.as(clazz);
            String iri = resAndHashToIri.apply(q);

            Resource newRes = ResourceUtils.renameResource(q, iri);
            if(q.equals(start)) {
                result = newRes.as(clazz);
            }
        }

        return result;
    }

    public static Maybe<LsqQuery> enrichWithFullSpinModel(LsqQuery lsqQuery) {
//        Maybe<LsqQuery> result;

        // Query query = QueryFactory.create("SELECT * {  { ?s a ?x ; ?p ?o } UNION { ?s ?j ?k } }");
        String queryStr = lsqQuery.getText();
        Query query;
        try {
            query = QueryFactory.create(queryStr);
        } catch(Exception e) {
            return Maybe.empty();
        }


//        SpinQueryEx spinRes = lsqQuery.getSpinQuery().as(SpinQueryEx.class);

        Resource spinQuery = LsqProcessor.createSpinModel(query, lsqQuery.getModel());

        // Immediately skolemize the spin model - before attachment of
        // additional properties changes the hashes
//        String part = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());
        spinQuery = Skolemize.skolemizeTree(spinQuery, false,
                (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                (r, d) -> true).asResource();

        SpinQueryEx spinRes = spinQuery.as(SpinQueryEx.class);

        lsqQuery.setSpinQuery(spinRes);

        LsqProcessor.enrichSpinModelWithBgps(spinRes);
        LsqProcessor.enrichSpinBgpsWithNodes(spinRes);
        LsqProcessor.enrichSpinBgpNodesWithSubBgpsAndQueries(spinRes);


        // Add tpInBgp resources
        // for bgp restricted triple pattern selectivity
//        for(SpinBgp bgp : spinRes) {
//            for(TriplePattern tp : bgp.getTriplePatterns()) {
//
//            }
//        }

        // Skolemize the remaining model
        Skolemize.skolemizeTree(spinRes, true,
                (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                (n, d) -> !(n.isResource() && n.asResource().hasProperty(LSQ.text)));


        // Now that the bgps and tps are skolemized, create the tpInBgp nodes
        for(SpinBgp bgp : spinRes.getBgps()) {
//            System.err.println("BGP: " + bgp);
            Set<TpInBgp> tpInBgps = bgp.getTpInBgp();
            for(TriplePattern tp : bgp.getTriplePatterns()) {
//            	System.err.println("BGP: " + bgp);
//                System.err.println("  TP: " + tp);
                TpInBgp tpInBgp = spinRes.getModel().createResource().as(TpInBgp.class);
                tpInBgp.setBgp(bgp);
                tpInBgp.setTriplePattern(tp);
                tpInBgps.add(tpInBgp);
            }

            Skolemize.skolemizeTree(bgp, true,
                    (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                    (n, d) -> true);

            // Clear the bgp attribute that was only required for computing the hash id
            for(TpInBgp tpInBgp : tpInBgps) {
                tpInBgp.setBgp(null);
            }
        }

//        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_FLAT);
//        System.exit(1);


//        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_BLOCKS);
//
//        System.exit(0);
        return Maybe.just(lsqQuery);
    }


    /**
     * Extract all queries associated with elements of the lsq query's spin representation
     *
     * @param lsqQuery
     * @return
     */
    public static Set<LsqQuery> extractAllQueries(LsqQuery lsqQuery) {
        Set<LsqQuery> result = new LinkedHashSet<>();

        // Add self by default
        result.add(lsqQuery);

        SpinQueryEx spinNode = lsqQuery.getSpinQuery().as(SpinQueryEx.class);

        for(SpinBgp bgp : spinNode.getBgps()) {
            LsqQuery extensionQuery = bgp.getExtensionQuery();
            if(extensionQuery != null) {
                result.add(extensionQuery);
            }

            Map<Node, SpinBgpNode> bgpNodeMap = bgp.indexBgpNodes();

            for(SpinBgpNode bgpNode : bgpNodeMap.values()) {
                extensionQuery = bgpNode.getJoinExtensionQuery();
                if(extensionQuery != null) {
                    result.add(extensionQuery);
                }

                SpinBgp subBgp = bgpNode.getSubBgp();

                if(subBgp == null) {
                    subBgp = bgpNode.getModel().createResource().as(SpinBgp.class);
                    bgpNode.setSubBgp(subBgp);
                }


                extensionQuery = subBgp.getExtensionQuery();
                if(extensionQuery != null) {
                    result.add(extensionQuery);
                }


                // Create triple pattern extension queries
                for(TriplePattern tp : bgp.getTriplePatterns()) {
                    LsqTriplePattern ltp = tp.as(LsqTriplePattern.class);

                    extensionQuery = ltp.getExtensionQuery();
                    if(extensionQuery == null) {
                        result.add(extensionQuery);
                    }
                }
            }
        }

        return result;
    }

   /*
    * Benchmark the combined execution and retrieval time of a given query
    *
    * @param query
    * @param queryExecRes
    * @param qef
    */
   public static QueryExec rdfizeQueryExecutionBenchmark(SparqlQueryConnection conn, String queryStr, QueryExec result) {

       Stopwatch sw = Stopwatch.createStarted();
       try(QueryExecution qe = conn.query(queryStr)) {
           long resultSetSize = QueryExecutionUtils.consume(qe);
           BigDecimal durationInMillis = new BigDecimal(sw.stop().elapsed(TimeUnit.NANOSECONDS))
                   .divide(new BigDecimal(1000000));


           //double durationInSeconds = duration.toNanos() / 1000000.0;
           result
           //result
               .setResultSetSize(resultSetSize)
               .setRuntimeInMs(durationInMillis)
           ;

       } catch(Exception e) {
           logger.warn("Failed to benchmark query", e);
       }

       return result;
       //Calendar end = Calendar.getInstance();
       //Duration duration = Duration.between(start.toInstant(), end.toInstant());
   }

   public static LsqQuery staticAnalysis(LsqQuery queryRes) {
       String queryStr = queryRes.getText();
       // TODO Avoid repeated parse
       Query query = QueryFactory.create(queryStr);
//       LsqProcessor.rdfizeQueryStructuralFeatures(lsqQuery, x -> NestedResource.from(lsqQuery).nest(x), query);

       Function<String, NestedResource> queryAspectFn = x -> NestedResource.from(queryRes).nest(x);
       //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
       LsqStructuralFeatures featureRes = queryAspectFn.apply("-sf").get().as(LsqStructuralFeatures.class); // model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash

       queryRes.setStructuralFeatures(featureRes);
       //queryRes.addProperty(LSQ.hasStructuralFeatures, featureRes);


       // Add used features
       QueryStatistics2.enrichResourceWithQueryFeatures(featureRes, query);

       if(query.isSelectType()) {
           featureRes.addLiteral(LSQ.projectVars, query.getProjectVars().size());
       }

//       Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
//       features.forEach(f -> featureRes.addProperty(LSQ.usesFeature, f));

       // TODO These methods have to be ported
       //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
       //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

//       SpinUtils.enrichWithHasTriplePattern(featureRes, spinRes);
//       SpinUtils.enrichWithTriplePatternText(spinRes);
       //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);

       //
//       QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, featureRes);

       QueryStatistics2.enrichWithPropertyPaths(featureRes, query);

       Model spinModel = queryRes.getModel();

       // TODO Move to a util function
       Set<Resource> serviceUris = spinModel.listStatements(null, SP.serviceURI, (RDFNode)null)
               .mapWith(stmt -> stmt.getObject().asResource()).toSet();

       for(Resource serviceUri : serviceUris) {
           featureRes.addProperty(LSQ.usesService, serviceUri);
       }




       //QueryStatistics2.enrichWithMentions(featureRes, query); //the mentions subjects, predicates and objects can be obtained from Spin


//   } catch (Exception ex) {
//       String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
//       queryRes.addLiteral(LSQ.processingError, msg);
//       logger.warn("Failed to process query " + query, ex);
//   }
//
        return queryRes;
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
                        ElementUtils.createElement(gspo),
                        new ElementFilter(ExprUtils.oneOf(Vars.g, graphNames))
                ));
        result.addOrderBy(Vars.g, Query.ORDER_ASCENDING);

        return result;
    }

    public static Quad gspo = new Quad(Vars.g, Vars.s, Vars.p, Vars.o);

    public static Flowable<Entry<String, Dataset>> fetchDatasets(SparqlQueryConnection conn, Iterable<Node> graphNames) {
        Query query = createFetchNamedGraphQuery(graphNames);
        QuadAcc quadAcc = new QuadAcc();
        quadAcc.addQuad(gspo);
        Template template = new Template(quadAcc);
        //template.getQuads().add(gspo);

        return SparqlRx.execSelectRaw(() -> conn.query(query))
            .concatMap(QueryFlowOps.createMapperQuads(template)::apply)
            .compose(DatasetGraphOpsRx.groupConsecutiveQuadsRaw(Quad::getGraph, DatasetGraphFactory::create))
            .map(e -> new SimpleEntry<>(e.getKey().getURI(), DatasetFactory.wrap(e.getValue())));
    }

    public static void foo() {
        SparqlQueryConnection benchmarkConn = RDFConnectionFactory.connect(DatasetFactory.create());

        Model configModel = ModelFactory.createDefaultModel();

        String expId = "testrun";
        String expSuffix = "_" + expId;
        String lsqBaseIri = "http://lsq.aksw.org/";

        Instant benchmarkRunStartTimestamp = Instant.ofEpochMilli(0);

        //Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(benchmarkRunStartTimestamp, ZoneId.systemDefault());
        Calendar cal = GregorianCalendar.from(zdt);
        XSDDateTime xsddt = new XSDDateTime(cal);
        //String timestamp = now.toString();
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);


        ExperimentConfig config = configModel
                .createResource("http://someconfig.at/now")
                .as(ExperimentConfig.class)
                .setIdentifier(expId);

        ExperimentRun expRun = configModel
                .createResource()
                .as(ExperimentRun.class)
                .setConfig(config)
                .setTimestamp(xsddt);


        HashIdCxt tmp = MapperProxyUtils.getHashId(expRun);
        String expRunIri = lsqBaseIri + tmp.getString(expRun);
        expRun = ResourceUtils.renameResource(expRun, expRunIri).as(ExperimentRun.class);



        Function<String, String> lsqQueryBaseIriFn = hash -> lsqBaseIri + "q-" + hash;

        // Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> lsqQueryBaseIriFn.apply(lsqQuery.getHash()) + expSuffix;

//        Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> "urn://" + lsqQuery.getHash() + expSuffix;
        Function<LsqQuery, String> lsqQueryExecFn = lsqQuery -> lsqQueryBaseIriFn.apply(lsqQuery.getHash()) + expSuffix;

        Flowable<List<Set<LsqQuery>>> flow = RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
                .map(r -> r.as(LsqQuery.class))
                .take(1)
                .flatMapMaybe(lsqQuery -> enrichWithFullSpinModel(lsqQuery), false, 128)
                .map(anonQuery -> updateLsqQueryIris(anonQuery, q -> lsqQueryBaseIriFn.apply(q.getHash())))
                .map(lsqQuery -> staticAnalysis(lsqQuery))
//                .doAfterNext(x -> {
//                    RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE_FLAT);
//                    System.exit(1);
//                })
                //.flatMap(lsqQuery -> Flowable.fromIterable(extractAllQueries(lsqQuery)), false, 128)
                .map(lsqQuery -> extractAllQueries(lsqQuery))
//                .doAfterNext(lsqQuery -> lsqQuery.updateHash())
//                .doOnNext(r -> ResourceUtils.renameResource(r, "http://lsq.aksw.org/q-" + r.getHash()).as(LsqQuery.class))
                .lift(OperatorObserveThroughput.create("throughput", 100))
                .buffer(30)
                ;

        Iterable<List<Set<LsqQuery>>> batches = flow.blockingIterable();
        Iterator<List<Set<LsqQuery>>> itBatches = batches.iterator();

        // Create a database to ensure uniqueness of evaluation tasks
        Dataset dataset = TDB2Factory.connectDataset("/tmp/lsq-benchmark-index");
        try(RDFConnection indexConn = RDFConnectionFactory.connect(dataset)) {

            while(itBatches.hasNext()) {
                List<Set<LsqQuery>> batch = itBatches.next();
//
//                // LookupServiceUtils.createLookupService(indexConn, );
//                // Triple t = new Triple(Vars.s, LSQ.execStatus.asNode(), Vars.o);
//                Triple t = new Triple(Vars.s, Vars.p, Vars.o);
//                Quad quad = new Quad(Vars.g, t);
//                BasicPattern basicPatteren = new BasicPattern();
//                basicPatteren.add(t);
////                QuadPattern quadPattern = new QuadPattern();
////                quadPattern.add(quad);
//
//                DataQuery<RDFNode> dq = new DataQueryImpl<>(
//                        indexConn,
//                        ElementUtils.createElement(quad),
//                        Vars.s,
//                        new Template(basicPatteren),
//                        RDFNode.class
//                );
//
//
//                System.out.println("Generated: " + dq.toConstructQuery());

                Set<Node> lookupHashNodes = batch.stream()
                        .flatMap(item -> item.stream())
                        .map(lsqQueryExecFn)
                        .map(NodeFactory::createURI)
                        .collect(Collectors.toSet());

//                for(Node n : lookupHashNodes) {
//                    System.out.println("Lookup with: " + n.getURI());
//                }
//                Expr filter = ExprUtils.oneOf(Vars.g, lookupHashNodes);
//                dq.filterDirect(new ElementFilter(filter));

                Map<String, Dataset> nodeToDataset = Txn.calculate(indexConn, () ->
                    fetchDatasets(indexConn, lookupHashNodes)
                    .toMap(Entry::getKey, Entry::getValue)
                    .blockingGet());

//                System.out.println(hashNodes);
                // Obtain the set of query strings already in the store

                Set<String> alreadyIndexedHashUrns = nodeToDataset.keySet();
//                Set<String> alreadyIndexedHashUrns = Txn.calculate(indexConn, () ->
//                      dq
//                        .exec()
//                        .map(x -> x.asNode().getURI())
//                        .blockingStream()
//                        .collect(Collectors.toSet()));
//                        ;

//                for(String str : alreadyIndexedHashUrns) {
//                    System.out.println("Already indexed: " + str);
//                }
                Set<LsqQuery> nonIndexed = batch.stream()
                    .flatMap(item -> item.stream())
                    .filter(item -> !alreadyIndexedHashUrns.contains(lsqQueryExecFn.apply(item)))
                    .collect(Collectors.toSet());

//                System.out.println(nonIndexed);

                List<Quad> inserts = new ArrayList<Quad>();

                boolean stop = false;
//                System.err.println("Batch: " + nonIndexed.size() + "/" + lookupHashNodes.size() + " (" + batch.size() + ") need processing");
                for(LsqQuery item : nonIndexed) {
//                    stop = true;
                    String queryStr = item.getText();
                    String queryExecIri = lsqQueryExecFn.apply(item); //"urn://" + item.getHash();
                    Node s = NodeFactory.createURI(queryExecIri);
                    System.out.println("Processing: " + s);
                    System.out.println(item.getText());

                    Dataset newDataset = DatasetFactory.create();
                    Model newModel = newDataset.getNamedModel(queryExecIri);

                    item = item.inModel(newModel).as(LsqQuery.class);

                    // Create fresh local execution and query exec resources
                    LocalExecution le = newModel.createResource().as(LocalExecution.class);
                    QueryExec qe = newModel.createResource().as(QueryExec.class);

                    // TODO Skolemize these executions!

                    rdfizeQueryExecutionBenchmark(benchmarkConn, queryStr, qe);

                    item.getLocalExecutions(LocalExecution.class).add(le);
                    le.setBenchmarkRun(expRun);
                    le.setQueryExec(qe);

                    newDataset.asDatasetGraph().find().forEachRemaining(inserts::add);


                    inserts.add(new Quad(s, s, LSQ.execStatus.asNode(), NodeFactory.createLiteral("processed")));
//                    RDFDataMgr.write(new FileOutputStream(FileDescriptor.out), item.getModel(), RDFFormat.TURTLE_PRETTY);

//                    System.out.println(s.getURI().equals("urn://33c4205f90fdeb7d1db68426806a816e3ffbfa3980461b556b32fded407568c9"));

                    nodeToDataset.put(queryExecIri, newDataset);
                }

                UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(inserts, null);
                Txn.executeWrite(indexConn, () -> indexConn.update(ur));

//                Txn.executeRead(indexConn, () -> System.out.println(ResultSetFormatter.asText(indexConn.query("SELECT ?s { ?s ?p ?o }").execSelect())));
                if(stop) {
                    ((Disposable)itBatches).dispose();
                    break;
                }

                for(Set<LsqQuery> pack : batch) {

                    logger.info("Processing pack of size: " + pack.size());
                    // The root query is always the first element of a pack
                    // packs are assumed to must be LinkedHashSets
                    LsqQuery rootQuery = pack.iterator().next();


                    // Extend the rootQuery's model with all related query executions
                    for(LsqQuery item : pack) {
                        String key = lsqQueryExecFn.apply(item);
                        Dataset ds = nodeToDataset.get(key);
                        Objects.requireNonNull(ds, "Expected dataset for key "  + key);
                        Model m = ds.getNamedModel(key);
                        Objects.requireNonNull(m, "Should not happen: No query execution model for " + key);

                        rootQuery.getModel().add(m);
                    }

                    SpinQueryEx spinRoot = rootQuery.getSpinQuery().as(SpinQueryEx.class);

                    // Update triple pattern selectivities
                    Model model = rootQuery.getModel();
//                    LocalExecution expRoot = model.createResource().as(LocalExecution.class);
                    Map<Resource, LocalExecution> rleMap = rootQuery.getLocalExecutionMap();
                    LocalExecution expRoot = rleMap.get(expRun);

//                    expRoot.setBenchmarkRun(expRun);
                    Long datasetSize = config.getDatasetSize();

                    if(datasetSize == null) {
                        datasetSize = 0l;
                    }


                    /*
                     * BgpExecs, TpInBgpExecs and TpExec
                     *
                     *
                     */
                    // Iterate the query's. bgps. For each bgp:
                    // 1.) get-or-create the execution
                    // 2.) then descend into the tp-in-bgp and the tps and update their stats
                    for(SpinBgp xbgp : spinRoot.getBgps()) {
                        // Get the bgp's execution in this experiment
                        SpinBgpExec bgpExec = expRoot.findBgpExec(xbgp);

                        if(bgpExec == null) {
                            bgpExec = model.createResource().as(SpinBgpExec.class);
                            LsqQuery extensionQuery = xbgp.getExtensionQuery();
                            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
                            LocalExecution le = leMap.get(expRun);
                            QueryExec qe = le.getQueryExec();

                            // Link the bgp with the corresponding query execution
                            bgpExec
                                .setLocalExecution(expRoot) /* inverse link */
                                .setBgp(xbgp) /* inverse link */
//                                .setBenchmarkRun(expRun)
                                .setQueryExec(qe)
                                ;

//                            expRoot.getBgpExecs().add(bgpExec);
                        }
                    }

                    // Now that all BgpExecs are set up
                    // set up tpExecs
                    for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
                        for(TpInBgp tpInBgp : bgpExec.getBgp().getTpInBgp()) {
                            LsqTriplePattern tp = tpInBgp.getTriplePattern();
                            //TpExec tpExec = tp.getExtensionQuery().getLocalExecutionMap().get(expRun);
                            //TpExec tpExec = tp.getExtensionQuery()
                            TpInBgpExec tpInBgpExec = bgpExec.findTpInBgpExec(tpInBgp);

                            if(tpInBgpExec == null) {
                                tpInBgpExec = model.createResource().as(TpInBgpExec.class);
                                // LsqQuery extensionQuery = tp.getExtensionQuery();

                                // Link the bgp with the corresponding query execution
                                tpInBgpExec
                                    .setBgpExec(bgpExec);
                            }

                            TpExec tpExec = tpInBgpExec.getTpExec();
                            if(tpExec == null) {
                                tpExec = model.createResource().as(TpExec.class);
                                LsqQuery extensionQuery = tp.getExtensionQuery();
                                Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
                                LocalExecution le = leMap.get(expRun);
                                QueryExec qe = le.getQueryExec();

                                tpExec
                                    .setTpInBgpExec(tpInBgpExec) /* inverse link */
                                    .setTp(tp)
                                    .setQueryExec(qe)
                                    ;
                            }

                            Long tpResultSetSize = tpExec.getQueryExec().getResultSetSize();
                            BigDecimal tpSel = safeDivide(tpResultSetSize, datasetSize);
                            tpExec
                                .setSelectivity(tpSel);

                            Long bgpSize = bgpExec.getQueryExec().getResultSetSize();
                            Long tpSize = tpExec.getQueryExec().getResultSetSize();
                            BigDecimal value = safeDivide(tpSize, bgpSize);
                            tpInBgpExec.setSelectivity(value);
                            // BigDecimal value = new BigDecimal(rsSize).divide(new BigDecimal(datasetSize));
                            // tpInBgpExec.setSelectivity(value);
                        }


                        for(SpinBgpNode bgpNode : bgpExec.getBgp().getBgpNodes()) {
                            JoinVertexExec bgpNodeExec = bgpExec.findBgpNodeExec(bgpNode);
                            if(bgpNodeExec == null) {
                                bgpNodeExec = model.createResource().as(JoinVertexExec.class);

                                LsqQuery extensionQuery = bgpNode.getJoinExtensionQuery();
                                QueryExec queryExec = extensionQuery.getLocalExecutionMap().get(expRun).getQueryExec();
                                // LsqQuery extensionQuery = tp.getExtensionQuery();

                                // Link the bgp with the corresponding query execution
                                bgpNodeExec
                                    .setBgpNode(bgpNode)
                                    .setBgpExec(bgpExec) /* inverse link */
                                    .setQueryExec(queryExec)
                                    ;
                            }

                            Long bgpNodeSize = bgpNodeExec.getQueryExec().getResultSetSize();
                            Long bgpSize = bgpExec.getQueryExec().getResultSetSize();
                            BigDecimal value = safeDivide(bgpNodeSize, bgpSize);

                            bgpNodeExec.setBgpRestrictedSelectivitiy(value);
                        }

                    }

                    // We need to add the config model in order to include the benchmark run id
                    // TODO We should ensure that only the minimal necessary config model is added
                    expRoot.getModel().add(configModel);

                    HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(expRoot);//.getHash(bgp);
                    //Map<RDFNode, HashCode> renames = hashIdCxt.getMapping();
                    Map<RDFNode, String> renames = hashIdCxt.getStringMapping();

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
                    for(Entry<RDFNode, String> e : renames.entrySet()) {
//                        HashCode hashCode = e.getValue();
//                        String part = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());
                        String part = e.getValue();

                        String iri = lsqBaseIri + part;
                        RDFNode n = e.getKey();
                        if(n.isResource()) {
//                            System.out.println("--- RENAME: ");
//                            System.out.println(iri);
//                            System.out.println(n);
//                            System.out.println("------------------------");
//
                            ResourceUtils.renameResource(n.asResource(), iri);
                        }
                    }




                    RDFDataMgr.write(System.out, spinRoot.getModel(), RDFFormat.TURTLE_PRETTY);

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

//                    Long datasetSize = config.getDatasetSize();
//                    if(datasetSize != null) {
//                        //for(SpinBgp xbgp : spinRoot.getBgps()) {
//                        for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//                            SpinBgp xbgp = bgpExec.getBgp();
//                            for(TpInBgp tpInBgp : xbgp.getTpInBgp()) {
//                                LsqTriplePattern tp = tpInBgp.getTriplePattern();
//
//                                LsqQuery extensionQuery = tp.getExtensionQuery();
//                                Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
//                                LocalExecution le = leMap.get(expRun);
//                                if(le == null) {
//                                    logger.warn("Missing local execution result");
//                                } else {
//                                    Set<TpInBgpExec> tpInBgpExecs = bgpExec.getTpInBgpExecs();
///*                                    if(tpInBgpExec == null) {
//                                        Long rsSize = le.getQueryExec().getResultSetSize();
//                                        BigDecimal value = new BigDecimal(rsSize).divide(new BigDecimal(datasetSize));
////                                        le.addLiteral(LSQ.tpSel, value);
//
//                                        tpInBgpExec = model.createResource(tpInBgpExec.class);
//                                        tpInBgpExec
//                                            .setTpInBgp(tpInBgp)
//                                            .setBgpExec(bgpExec)
//                                            .setTpExec()
//                                            .setSelectivity(value)
//                                            ;
//                                    }
//                                    */
//
//
//                                }
//                            }
//                        }
//                    }


                    /*
                     * BGP restricted tp sel
                     * CONSTRUCT { ?s lsq:tpSel ?o } {
                     *   ?root lsq:hasBgps [ lsq:hasTP [ lsq:hasTPExec ?s ] ]
                     *   ?s lsq:benchmarkRun ?env_run ; lsq:resultSetSize ?rs
                     *   ?env_run lsq:datasetSize ?datasetSize
                     *   BIND(?rs / ?datasetSize AS ?o)
                     * }
                     */
//                    {
//                        for(SpinBgp bgp : spinRoot.getBgps()) {
//                            for(TpInBgp tpInBgp : bgp.getTpInBgp()) {
//                        for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//                            for(TpInBgpExec tpInBgpExec : bgpExec.getTpInBgpExecs()) {
//                        for(SpinBgpNode bgpNode : spinRoot.getBgps()) {
//                            //for(TpInBgp tpInBgpExec : expRoot.getTpInBgpExec()) {
//                                LsqTriplePattern tp = tpInBgp.getTriplePattern();
//
//                                LocalExecution bgpLe = bgp.getExtensionQuery().getLocalExecutionMap().get(expRun);
//                                LocalExecution tpLe = tp.getExtensionQuery().getLocalExecutionMap().get(expRun);
//
//                                Long bgpSize = bgpLe.getQueryExec().getResultSetSize();
//                                Long tpSize = tpLe.getQueryExec().getResultSetSize();
//
//                                BigDecimal bgpRestrictedTpSel = bgpSize == 0 ? new BigDecimal(0) : new BigDecimal(tpSize).divide(new BigDecimal(bgpSize));
//
//    //                            LocalExecution le = tp.getExtensionQuery().indexLocalExecs().get(expRun);
//
//                                TpInBgpExec tpInBgpExec = model.createResource().as(TpInBgpExec.class);
//                                tpInBgpExec
//                                    .setBgpExec(bgpLe)
//                                    .setSelectivity(bgpRestrictedTpSel);
////                                tpInBgpExec.setBenchmarkRun(expRun);
//
//                            }
//                        }
//                    }


                    // Update join restricted tp selectivity
                    // For this purpose iterate the prior computed bgpExecs
//                    {
//                        for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {
//                            SpinBgp bgp = bgpExec.getBgp();
//                            for(SpinBgpNode bgpNode : bgp.getBgpNodes()) {
////                        for(SpinBgp bgp : spinRoot.getBgps()) {
////                            for(SpinBgpNode bgpNode : bgp.getBgpNodes()) {
//                                if(bgpNode.toJenaNode().isVariable()) {
//
//                                    LocalExecution bgpLe = bgpNode.getSubBgp().getExtensionQuery().getLocalExecutionMap().get(expRun);
//                                    LocalExecution bgpNodeLe = bgpNode.getJoinExtensionQuery().getLocalExecutionMap().get(expRun);
//
//
//                                    Long bgpExtSize = bgpLe.getQueryExec().getResultSetSize();
//                                    Long bgpNodeExtSize = bgpNodeLe.getQueryExec().getResultSetSize();
//
//                                    BigDecimal bgpRestrictedJoinVarSel = bgpExtSize == 0 ? new BigDecimal(0) : new BigDecimal(bgpNodeExtSize).divide(new BigDecimal(bgpExtSize));
//
//                                    JoinVertexExec bgpVarExec = model.createResource().as(JoinVertexExec.class);
//                                    // bgpVarExec.setBenchmarkRun(expRun);
////                                    bgpVarExec.setBgpNode(bgpNode);
//                                    bgpVarExec
//                                        .setBgpExec(bgpExec)
//                                        .setBgpRestrictedSelectivitiy(bgpRestrictedJoinVarSel);
//
//                                    // BgpVarExec is a child of BgpExec, hence there is no need for a back pointer
//                                    //bgpVarExec.setBgpExec(bgpLe);
//                                }
//                            }
//                        }
//                    }



                }
            }
        } finally {
            dataset.close();

        }


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


    public static BigDecimal safeDivide(Long counter, Long denominator) {
        BigDecimal result = denominator.longValue() == 0
                ? new BigDecimal(0)
                : new BigDecimal(counter).divide(new BigDecimal(denominator));
        return result;
    }
}
