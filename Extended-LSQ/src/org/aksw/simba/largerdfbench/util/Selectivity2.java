package org.aksw.simba.largerdfbench.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.simba.lsq.core.SpinModelUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

public class Selectivity2 {

    public static final Concept triplePatterns = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "x", "?x sp:subject ?s ; sp:predicate ?p ; sp:object ?o");

//    public static final String TRIPLE_PATTERN_QUERY_STR
//            = "PREFIX sp: <http://lsq.aksw.org/ontology/>\n"
//            + "PREFIX sp: <http://spinrdf.org/sp#>\n"
//            + "SELECT ?x ?i ?s ?sv ?p ?pv ?o ?ov {\n"
//            + "  ?x\n"
//            + "    sp:subject ?s ;\n"
//            + "    sp:predicate ?p ;\n"
//            + "    sp:object ?o . \n"
//            + "\n"
//            + "  Optional { lsq:triplePatternIndex ?i }\n"
//            + "  Optional { ?s sp:varName ?sv }\n"
//            + "  Optional { ?p sp:varName ?pv }\n"
//            + "  Optional { ?o sp:varName ?ov }\n"
//            + "}"
//            ;

    //public static final Query TRIPLE_PATTERN_QUERY = QueryFactory.create(TRIPLE_PATTERN_QUERY_STR, Syntax.syntaxARQ);



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

    public static void enrichModelWithHasTriplePattern(Model spinModel, Resource queryRes) {
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.keySet().forEach(r ->
            spinModel.add(queryRes, LSQ.hasTriplePattern, r)
        );
    }


    public static void enrichModelWithTriplePatternText(Model spinModel) {
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach((r, t) -> {
            spinModel.add(r, RDFS.label, TripleUtils.toNTripleString(t));
            spinModel.add(r, LSQ.triplePatternText, TripleUtils.toNTripleString(t));
        });
    }


    public static void enrichModelWithTriplePatternExtensionSizes(Model spinModel, QueryExecutionFactory dataQef) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        for(Resource r : triplePatternIndex.keySet()) {
            Triple t = SpinModelUtils.readTriple(spinModel, r);

            int tripleCount = fetchTriplePatternExtensionSize(dataQef, t);
            //double selectivity = tripleCount / (double)totalTripleCount;

            spinModel.add(r, LSQ.triplePatternExtensionSize, spinModel.createTypedLiteral(tripleCount));
        }
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
