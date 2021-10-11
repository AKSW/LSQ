package org.aksw.simba.lsq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.simba.lsq.cli.trash.LsqProcessor;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.util.SpinUtilsOld;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.Triple;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

public class TestLsqSelectivity {

    @Test
    public void testSelectivityRdfOutput() throws IOException {
        Model dataModel = RDFDataMgr.loadModel("lsq-tests/01/data.ttl");
        QueryExecutionFactory dataQef = FluentQueryExecutionFactory.from(dataModel).create();

        Stream<String> queryStrs = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("lsq-tests/01/query.sparql.log"))).lines()
                .map(String::trim)
                .filter(str -> !Strings.isNullOrEmpty(str));//.collect(Collectors.joining("\n"));
        //Stream<String> queryStr = queryStrs.findFirst();

        // TODO Consider specifying the pattern directly without going over the registry
        Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));
        Mapper mapper = logFmtRegistry.get("sparql2");

        //Function<String, NestedResource> queryAspectFn = (aspect) -> baseRes.nest(aspect).nest("q-" + queryHash);


        LsqProcessor processor = new LsqProcessor();
        processor.setDatasetSize(dataModel.size());
        processor.setStmtParser(SparqlStmtParserImpl.create(Syntax.syntaxARQ, false));
        processor.setBaseUri("http://example.org/");
        processor.setDatasetLabel("test");
        processor.setRdfizerQueryStructuralFeaturesEnabled(true);
        processor.setRdfizerQueryExecutionEnabled(true);
        processor.setRdfizerQueryLogRecordEnabled(true);
        processor.setDataQef(dataQef);
        processor.setLegacyMode(true);
        //processor.setQueryAspectFn(queryAspectFn);


        queryStrs.forEach(queryStr -> {

            Resource logRes = ModelFactory.createDefaultModel().createResource();
            mapper.parse(logRes, queryStr);
            Resource queryRes = processor.applyForQueryOrWebLogRecord(logRes);
            if(queryRes != null) {
                RDFDataMgr.write(System.out, queryRes.getModel(), RDFFormat.TURTLE_BLOCKS);
            }
        });


        //RDFDataMgr.write(System.err, logRes.getModel(), RDFFormat.TURTLE_BLOCKS);





        // configure the reader and the processor...



//queryStr = "SELECT * { ?s ?p ?o }";
        //System.out.println(queryStr);
        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution(queryStr).execSelect()));
        //MainLSQ.rd


    }

    @Test
    public void testSelectivity() {
        Model dataModel = RDFDataMgr.loadModel("test-data.ttl");

        Model spinModel = ModelFactory.createDefaultModel();
        Query q = ARQ2SPIN.parseQuery("PREFIX ex:<http://example.org/> SELECT * { ?s ex:p1 ?o1 ; ex:p2 ?o2 }", spinModel);
        //spinModel = q.getModel();

        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(dataModel).create();

        Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinUtilsOld.indexBasicPatterns2(spinModel);

        for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {
            Map<Triple, Long> sel = QueryStatistics2.fetchRestrictedResultSetRowCount(qef, e.getValue());
            System.out.println("TP/BGP compatibility counts: " + sel);

            Map<Var, Long> joinVarCounts = QueryStatistics2.fetchCountVarJoin(qef, e.getValue());
            System.out.println("TP/BGP join var counts " + joinVarCounts);
        }

    }
}
