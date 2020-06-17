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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jena_sparql_api.http.repository.api.HttpRepository;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.op.OperatorObserveThroughput;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinBgp;
import org.aksw.simba.lsq.spinx.model.SpinBgpNode;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;
import org.aksw.sparql_integrate.ngs.cli.main.ExceptionUtils;
import org.aksw.sparql_integrate.ngs.cli.main.ResourceInDatasetFlowOps;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.primitives.Ints;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.TriplePattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;


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


    public static Maybe<LsqQuery> enrichFull(LsqQuery lsqQuery) {
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
        SpinQueryEx spinRes = lsqQuery.getModel().createResource().as(SpinQueryEx.class);
        lsqQuery.setSpinQuery(spinRes);

        LsqProcessor.createSpinModel(query, spinRes);
        LsqProcessor.enrichSpinModelWithBgps(spinRes);
        LsqProcessor.enrichSpinBgpsWithNodes(spinRes);
        LsqProcessor.enrichSpinBgpNodesWithSubBgpsAndQueries(spinRes);

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
   public static LocalExecution rdfizeQueryExecutionBenchmark(SparqlQueryConnection conn, String queryStr, LocalExecution result) {

       Stopwatch sw = Stopwatch.createStarted();
       try(QueryExecution qe = conn.query(queryStr)) {
           long resultSetSize = QueryExecutionUtils.consume(qe);
           BigDecimal durationInMillis = new BigDecimal(sw.stop().elapsed(TimeUnit.NANOSECONDS))
                   .divide(new BigDecimal(1000000));


           //double durationInSeconds = duration.toNanos() / 1000000.0;
           result
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

    public static void foo() {
        SparqlQueryConnection benchmarkConn = RDFConnectionFactory.connect(DatasetFactory.create());

        Flowable<List<LsqQuery>> flow = RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
                .map(r -> r.as(LsqQuery.class))
//                .map(r -> ResourceUtils.renameResource(r, "http://lsq.aksw.org/q-" + r.getHash()).as(LsqQuery.class))
                .flatMapMaybe(lsqQuery -> enrichFull(lsqQuery), false, 128)
                .flatMap(lsqQuery -> Flowable.fromIterable(extractAllQueries(lsqQuery)), false, 128)
                .lift(OperatorObserveThroughput.create("throughput", 100))
                .buffer(30)
                ;

        Iterable<List<LsqQuery>> batches = flow.blockingIterable();



        // Create a database to ensure uniqueness of evaluation tasks
        Dataset dataset = TDB2Factory.connectDataset("/tmp/lsq-benchmark-index");
        try(RDFConnection indexConn = RDFConnectionFactory.connect(dataset)) {

            for(List<LsqQuery> batch : batches) {
                // LookupServiceUtils.createLookupService(indexConn, );
                Triple t = new Triple(Vars.s, LSQ.text.asNode(), Vars.o);
                BasicPattern bgp = new BasicPattern();
                bgp.add(t);
                DataQuery<RDFNode> dq = new DataQueryImpl<>(
                        indexConn,
                        ElementUtils.createElement(t),
                        Vars.o,
                        new Template(bgp),
                        RDFNode.class
                );

                List<Node> nodes = batch.stream()
                        .map(q -> q.getText())
                        .map(NodeFactory::createLiteral)
                        .collect(Collectors.toList());

                Expr filter = ExprUtils.oneOf(Vars.o, nodes);
                dq.filterDirect(new ElementFilter(filter));

                // Obtain the set of query strings already in the store
                Set<String> alreadyIndexed = Txn.calculate(indexConn, () ->
                      dq
                        .exec()
                        .map(x -> x.asLiteral().getString())
                        .blockingStream()
                        .collect(Collectors.toSet()));
                        ;

                List<Quad> inserts = new ArrayList<Quad>();

                for(LsqQuery item : batch) {

                    String queryStr = item.getText();

                    boolean isInIndex = alreadyIndexed.contains(queryStr);
                    if(!isInIndex) {
                        LocalExecution le = item.getModel().createResource().as(LocalExecution.class);

                        rdfizeQueryExecutionBenchmark(benchmarkConn, queryStr, le);
                        item.getLocalExecutions(LocalExecution.class).add(le);

                        inserts.add(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createBlankNode(), t.getPredicate(), t.getObject()));
    //                    RDFDataMgr.write(new FileOutputStream(FileDescriptor.out), item.getModel(), RDFFormat.TURTLE_PRETTY);
                    } else {
                        // System.out.println("Skipping");
                    }

                    UpdateRequest ur = UpdateRequestUtils.createUpdateRequest(inserts, null);
                    Txn.execute(indexConn, () -> indexConn.update(ur));
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
}
