package org.aksw.simba.lsq.core;

import java.util.Collection;

import org.aksw.simba.lsq.spinx.model.SpinBgp;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.util.SpinUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.topbraid.spin.model.TriplePattern;


public class TestExtendedSpinModel {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        SpinQueryEx spinRes = model.createResource("http://test.ur/i").as(SpinQueryEx.class);

        Query query = QueryFactory.create("SELECT * {  { ?s a ?x ; ?p ?o } UNION { ?s ?j ?k } }");

        LsqProcessor.createSpinModel(query, spinRes);
        LsqProcessor.enrichSpinModelWithBgps(spinRes);
        LsqProcessor.enrichSpinBgpsWithNodes(spinRes);


//        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

        for(SpinBgp bgp : spinRes.getBgps()) {
            System.out.println(bgp);
            Collection<TriplePattern> tps = bgp.getTriplePatterns();

            for(TriplePattern tp : tps) {
                System.out.println(tp);
            }
        }

        QueryStatistics2.enrichSpinQueryWithBgpStats(spinRes);
        // QueryStatistics2.setUpJoinVertices(spinRes);
        QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, spinRes);

        // TODO Make skolemize reuse skolem ID resources
        Skolemize.skolemize(spinRes);


//        QueryStatistics2.fetchCountJoinVarElement(qef, itemToElement)

        // Now to create the evaluation results...
        // LsqProcessor.rdfizeQueryExecutionStats
//        SpinUtils.enrichModelWithTriplePatternExtensionSizes(queryRes, queryExecRes, cachedQef);



        //Skolemize.skolemize(spinRes);

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }
}
