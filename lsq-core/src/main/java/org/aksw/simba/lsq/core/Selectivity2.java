package org.aksw.simba.lsq.core;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.simba.lsq.util.ConceptModelUtils;
import org.aksw.simba.lsq.util.SpinModelUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.vocabulary.RDFS;

public class Selectivity2 {

    public static final Concept triplePatterns = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "x", "?x sp:subject ?s ; sp:predicate ?p ; sp:object ?o");

    public static int fetchTriplePatternExtensionSize(QueryExecutionFactory qef, Triple triple) {

        Var c = Var.alloc("_c_");
        // TODO Move to QueryGenerationUtils
        Var d = Var.alloc("_d_");

        Query query = new Query();
        query.getProject().add(c, new ExprAggregator(d, new AggCount()));
        query.setQuerySelectType();
        query.setQueryPattern(ElementUtils.createElement(triple));

        int result = ServiceUtils.fetchInteger(qef, query, c);
        return result;
    }


    public static Map<Resource, Triple> indexTriplePatterns(Model spinModel) {
        Map<Resource, Triple> result = ConceptModelUtils.listResources(spinModel, triplePatterns)
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        t -> SpinModelUtils.readTriple(spinModel, t)));
        return result;
    }

    public static void enrichModelWithHasTriplePattern(Resource queryRes) {
        Model spinModel = queryRes.getModel();
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.keySet().forEach(r ->
            queryRes.addProperty(LSQ.hasTriplePattern, r)
        );
    }


    public static void enrichModelWithTriplePatternText(Resource queryRes) {
        Model spinModel = queryRes.getModel();
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach((r, t) -> r
                .addProperty(RDFS.label, TripleUtils.toNTripleString(t))
                .addProperty(LSQ.triplePatternText, TripleUtils.toNTripleString(t)));
    }


    public static void enrichModelWithTriplePatternExtensionSizes(Resource queryRes, QueryExecutionFactory dataQef) {
        Model spinModel = queryRes.getModel();
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach((r, t) -> {
            int tripleCount = fetchTriplePatternExtensionSize(dataQef, t);
            //double selectivity = tripleCount / (double)totalTripleCount;

            spinModel.add(r, LSQ.triplePatternExtensionSize, spinModel.createTypedLiteral(tripleCount));
        });
    }

    public static void enrichModelWithTriplePatternSelectivities(Model spinModel, long totalTripleCount) {
        //double selectivity = tripleCount / (double)totalTripleCount;
    }

//
//
//            stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+ " lsqv:hasTriplePattern lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+ " . \n";
//            stats = stats+" lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+" lsqv:triplePatternText \""+spText +"\" ; ";
//            stats = stats+ " lsqv:triplePatternSelectivity "+tpSel +" . ";
//
//
//
//        }
//
//
//
//                    stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+ " lsqv:hasTriplePattern lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+ " . \n";
//                    stats = stats+" lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+" lsqv:triplePatternText \""+spText +"\" ; ";
//                    stats = stats+ " lsqv:triplePatternSelectivity "+tpSel +" . ";
//
//                }
//                else
//                {
//                    tpSel = getTriplePatternSelectivity(stmt,tp,endpoint,graph,endpointSize);
//                    tpSelCache.put(tp, tpSel);
//                //  System.out.println(tp + "  " +tpSel );
//                    stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+ " lsqv:hasTriplePattern lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+ " . \n";
//                    stats = stats+" lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+" lsqv:triplePatternText \""+spText +"\" ; ";
//                    stats = stats+ " lsqv:triplePatternSelectivity "+tpSel +" . ";
//                }
//                meanQrySel  = meanQrySel+ tpSel;
//                tpNo++;
//                //meanTPSelectivities.add(tpSel);
                //System.out.println("Average (across all datasets) Triple pattern selectivity: "+ meanTripleSel);
//            }
//        }
//        if(totalTriplePatterns==0)
//            meanQrySel =0;
//        else
//            meanQrySel = meanQrySel/totalTriplePatterns;
//        con.close();
//        stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+" lsqv:meanTriplePatternSelectivity "+meanQrySel+" ;  ";
//
//        return stats;
//    }
}
