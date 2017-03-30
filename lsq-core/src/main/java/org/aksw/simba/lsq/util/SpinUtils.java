package org.aksw.simba.lsq.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * SPIN utils - mainly for extracting Jena Triple and BasicPattern objects from SPIN RDF.
 * TODO Maybe the spin library already comes with a proper spin reader?
 *
 *
 */
public class SpinUtils {

    public static final Concept triplePatterns = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "x", "?x sp:subject ?s ; sp:predicate ?p ; sp:object ?o");
    public static final Concept basicPatterns = Concept.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX sp: <http://spinrdf.org/sp#>", "x", "?foo !rdf:rest ?x . ?x (rdf:rest)*/rdf:first [ sp:subject ?s ; sp:predicate ?p ; sp:object ?o ]");

    public static final Concept subjects = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:subject ?y");
    public static final Concept predicates = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:predicate ?y");
    public static final Concept objects = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:object ?y");

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


    public static Multimap<Resource, org.topbraid.spin.model.Triple> indexBasicPatterns2(Resource r) {
        Model spinModel = ResourceUtils.reachableClosure(r);
        Multimap<Resource, org.topbraid.spin.model.Triple> result = indexBasicPatterns2(spinModel);
        return result;
    }

    public static Multimap<Resource, org.topbraid.spin.model.Triple> indexBasicPatterns2(Model spinModel) {
        List<Resource> ress = ConceptModelUtils.listResources(spinModel, basicPatterns);

        Multimap<Resource, org.topbraid.spin.model.Triple> result = ArrayListMultimap.create();

        ress.forEach(t -> {
            Set<org.topbraid.spin.model.Triple> tmp = indexTriplePatterns2(t);
            result.putAll(t, tmp);
        });

        return result;
    }

    public static Map<Resource, BasicPattern> indexBasicPatterns(Resource r, Map<RDFNode, Node> modelToNode) {
        Model spinModel = ResourceUtils.reachableClosure(r);
        Map<Resource, BasicPattern> result = indexBasicPatterns(spinModel, modelToNode);
        return result;
    }

    public static Map<Resource, BasicPattern> indexBasicPatterns(Model spinModel, Map<RDFNode, Node> modelToNode) {
//        spinModel.write(System.out, "NTRIPLES");

        List<Resource> ress = ConceptModelUtils.listResources(spinModel, basicPatterns);

//        ress.stream().forEach(x -> System.out.println("GOT RES: " + x));

        Map<Resource, BasicPattern> result = ress
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        t -> {
                            Map<Resource, Triple> tmp = indexTriplePatterns(t, modelToNode);
                            BasicPattern r = new BasicPattern();
                            tmp.values().forEach(r::add);
                            return r;
                        }));

//        result.entrySet().forEach(x -> System.out.println("GOT: " + x));
        return result;
    }



//    public static Set<org.topbraid.spin.model.Triple> itp(Resource res) {
//        Set<org.topbraid.spin.model.Triple> result = new HashSet<>();
//        org.topbraid.spin.model.Query q = res.as(org.topbraid.spin.model.Query.class);
//        for(Element e : q.getWhereElements()) {
//            if(e.canAs(org.topbraid.spin.model.Triple.class)) {
//                result.add(e.as(org.topbraid.spin.model.Triple.class));
//            }
//        }
//        return result;
//        //e = res.as(Element.class);
//    }


    public static Map<Resource, Triple> indexTriplePatterns(Resource res, Map<RDFNode, Node> modelToNode) {
        Model spinModel = ResourceUtils.reachableClosure(res);
        Map<Resource, Triple> result = indexTriplePatterns(spinModel, modelToNode);
        return result;
    }

    public static Map<Resource, Triple> indexTriplePatterns(Model spinModel, Map<RDFNode, Node> modelToNode) {
        Map<Resource, Triple> result = ConceptModelUtils.listResources(spinModel, triplePatterns)
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        t -> readTriple(t, modelToNode).get()));
        return result;
    }

    public static Node toNode(RDFNode node) {
        Node result = node.canAs(Variable.class)
            ? Var.alloc(node.as(Variable.class).getName())
            : node.asNode();
        return result;
    }

    public static Triple toJenaTriple(org.topbraid.spin.model.Triple t) {
        Triple result = new Triple(toNode(t.getSubject()), toNode(t.getPredicate()), toNode(t.getObject()));
        return result;
    }

    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns2(Resource res) {
        Model spinModel = ResourceUtils.reachableClosure(res);
        Set<org.topbraid.spin.model.Triple> result = indexTriplePatterns2(spinModel);
        return result;
    }
    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns2(Model spinModel) {
        Set<org.topbraid.spin.model.Triple> result = ConceptModelUtils.listResources(spinModel, triplePatterns)
                .stream()
                .map(r -> r.as(org.topbraid.spin.model.TriplePattern.class))
                //.peek(x -> System.out.println(x.getSubject()))
                //.map(r -> {System.out.println(r + ": " + r.getModel()); return r.as(org.topbraid.spin.model.TriplePattern.class); })
                .collect(Collectors.toSet());
        return result;
    }

    public static void enrichWithHasTriplePattern(Resource targetRes, Resource spinRes) {
        Model spinModel = ResourceUtils.reachableClosure(spinRes);
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);

        triplePatternIndex.keySet().forEach(r ->
            targetRes.addProperty(LSQ.hasTriplePattern, r)
        );
    }


    public static void enrichWithTriplePatternText(Resource queryRes) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);

        triplePatternIndex.forEach((r, t) -> r.inModel(queryRes.getModel())
                .addProperty(RDFS.label, TripleUtils.toNTripleString(t))
                );
                //.addProperty(LSQ.triplePatternText, TripleUtils.toNTripleString(t)));
    }


    public static void enrichModelWithTriplePatternExtensionSizes(Resource queryRes, Resource queryExecRes, QueryExecutionFactory dataQef) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);

        triplePatternIndex.forEach((r, t) -> {
            int tripleCount = fetchTriplePatternExtensionSize(dataQef, t);
            //double selectivity = tripleCount / (double)totalTripleCount;

            spinModel.add(r, LSQ.triplePatternResultSize, spinModel.createTypedLiteral(tripleCount));
        });
    }

    public static long countTriplePattern(QueryExecutionFactory qef, Triple t) {
        Query query = new Query();
        query.setQuerySelectType();
        query.getProject().add(Vars.c, new ExprAggregator(Vars.x, new AggCount()));
        query.setQueryPattern(ElementUtils.createElement(t));

        QueryExecution qe = qef.createQueryExecution(query);
        long result = ServiceUtils.fetchInteger(qe, Vars.c);
        return result;
    }

//
//    public static void allocateTriplePatternResources(Resource queryRes, Resource queryExecRes) {
//
//        Model spinModel = ResourceUtils.reachableClosure(queryRes);
//        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);
//
//        int i = 0;
//        //triplePatternIndex.entrySet().forEach(e -> {
//        for(Entry<Resource, Triple> e : triplePatternIndex.entrySet()) {
//            ++i;
//            Resource r = e.getKey();
//            Resource queryTpExecRes = queryRes.getModel().createResource(queryExecRes.getURI() + "-tp-" + i);
//
//            queryExecRes
//                .addProperty(LSQ.hasTriplePatternExecution, queryTpExecRes);
//
//
//            queryTpExecRes
//                .addProperty(LSQ.hasTriplePattern, r);
//        }
//
//
//    }


    /**
     *
     * @param queryRes
     * @param queryExecRes
     * @return
     */
    public static Map<Resource, Map<Var, Resource>> createJoinVarObservations(Resource queryRes, Resource queryExecRes) {
//        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Set<Resource> result = queryRes.getModel().listObjectsOfProperty(LSQ.joinVertex)
                .mapWith(o -> o.asResource()).toSet();

        // Note: the initial bgp resource is at present allocated by spin; so the name is just a hash

        // For each join vertex, determine the BGP in which it appears and the var name
        // then allocate a resource with (bgp-id, varName)


        return null;
//        return result;
    }





    public static Set<Resource> createTriplePatternExecutions(Resource queryRes, Resource queryExecRes) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);

        Set<Resource> result = new HashSet<>();

        int i = 0;
        //triplePatternIndex.entrySet().forEach(e -> {
        for(Entry<Resource, Triple> e : triplePatternIndex.entrySet()) {
            ++i;
            Resource r = e.getKey();

            Resource queryTpExecRes = queryRes.getModel().createResource(queryExecRes.getURI() + "-tp-" + i);

            queryExecRes.addProperty(LSQ.hasTriplePatternExecution, queryTpExecRes);

            queryTpExecRes
                .addProperty(RDF.type, LSQ.TriplePatternExecution)
                .addProperty(LSQ.hasTriplePattern, r);


            result.add(queryTpExecRes);
        }

        return result;
    }

    public static void enrichModelWithTriplePatternSelectivities(Set<Resource> tpExecRess, QueryExecutionFactory qef, long totalTripleCount) {

        for(Resource tpExecRes : tpExecRess) {
            org.topbraid.spin.model.Triple spinTriple = tpExecRes.getProperty(LSQ.hasTriplePattern).getObject().as(org.topbraid.spin.model.TriplePattern.class);
            Triple triple = toJenaTriple(spinTriple);

            long count = countTriplePattern(qef, triple);

            double selectivity = totalTripleCount == 0 ? 0 : count / (double)totalTripleCount;

            tpExecRes
                .addLiteral(LSQ.triplePatternResultSize, count)
                .addLiteral(LSQ.tpSelectivity, selectivity);
        }
    }

    /**
     * :qe-123
     *     tripleSelec
     *
     * tpqe-123-p1
     *   forExec qe-123
     *   ofPattern q-123-p1
     *
     * @param queryRes
     * @param queryExecRes
     * @param qef
     * @param totalTripleCount
     */
    @Deprecated
    public static void enrichModelWithTriplePatternSelectivities(Resource queryRes, Resource queryExecRes, QueryExecutionFactory qef, long totalTripleCount) {

        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);

        int i = 0;
        //triplePatternIndex.entrySet().forEach(e -> {
        for(Entry<Resource, Triple> e : triplePatternIndex.entrySet()) {
            ++i;
            Resource r = e.getKey();
            Triple t = e.getValue();
            long count = countTriplePattern(qef, t);

            Resource queryTpExecRes = queryRes.getModel().createResource(queryExecRes.getURI() + "-tp-" + i);

            queryExecRes
                .addProperty(LSQ.hasTriplePatternExecution, queryTpExecRes);


            double selectivity = totalTripleCount == 0 ? 0 : count / (double)totalTripleCount;

            queryTpExecRes
                .addProperty(LSQ.hasTriplePattern, r)
                .addLiteral(LSQ.triplePatternResultSize, count)
                .addLiteral(LSQ.tpSelectivity, selectivity);
        }

//        triplePatternIndex.keySet().forEach(r ->
//            queryRes.addProperty(LSQ.hasTriplePattern, r)
//        );


        //double selectivity = tripleCount / (double)totalTripleCount;


    }


    public static void enrichModelWithJoinRestrictedTPSelectivities(
        QueryExecutionFactory qef,
        Model observationModel,
        Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps)
    {

        for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {
            // For each bgp, we now have a mapping for the vars
            Map<Var, Long> joinVarCounts = QueryStatistics2.fetchCountVarJoin(qef, e.getValue());
            System.out.println("TP/BGP join var counts " + joinVarCounts);


        }
    }



    /**
     * TODO How to link to the join variable?
     * @param tpExecResource
     */
    public static void enrichModelWithBGPRestrictedTPSelectivities(
            QueryExecutionFactory qef,
            Model observationModel,
            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps) {

        // Map each triple pattern to the resource which will carry the observed metrics
        Map<org.topbraid.spin.model.Triple, Resource> tpToObservation = observationModel.listObjectsOfProperty(LSQ.hasTriplePatternExecution).toSet().stream()
            .map(o -> o.asResource())
            .collect(Collectors.toMap(
                    o -> o.getPropertyResourceValue(LSQ.hasTriplePattern).as(org.topbraid.spin.model.TriplePattern.class),
                    o -> o));

        //Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = indexBasicPatterns2(queryRes);

        // TODO We need the absolute count for reference of the (unconstrained/filtered) TP
        //for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {

        // Compute the absolute counts for each triple in the bgp
        Map<org.topbraid.spin.model.Triple, Long> sels = QueryStatistics2.computeSelectivity(qef, tpToObservation.keySet());

        for(Entry<org.topbraid.spin.model.Triple, Long> e : sels.entrySet()) {
            Resource observation = tpToObservation.get(e.getKey());
            Long count = e.getValue();

            long tpResultSetSize = observation.getProperty(LSQ.triplePatternResultSize).getLong(); //e.getKey().getProperty(LSQ.triplePatternResultSize).getLong();
            double tpSelectivity = tpResultSetSize == 0 ? 0d : count / (double)tpResultSetSize;

            observation
                .addLiteral(LSQ.tpSelectivityBgpRestricted, tpSelectivity);

        }

            //Map<org.topbraid.spin.model.Triple, Long> sel = QueryStatistics2.computeSelectivity(qef, e.getValue());
            //System.out.println("TP/BGP compatibility counts: " + sel);
            //Map<Var, Long> joinVarCounts = QueryStatistics2.fetchCountVarJoin(qef, e.getValue());
            //System.out.println("TP/BGP join var counts " + joinVarCounts);
    }


    /**
     * Reads a single object for a given subject - predicate pair and
     * maps the corresponding object through a transformation function.
     *
     * Convenience function. model.listObjectsOfProperty(s, p).toList().stream().map(
     *
     *
     *
     * @param model
     * @param s
     * @param p
     * @param fn
     * @return
     */
    public static Optional<RDFNode> readObject(Resource s, Property p) {
        Optional<RDFNode> result = s.listProperties(p).toList().stream()
                .map(stmt -> stmt.getObject())
                .findFirst();

        //T result = tmp.isPresent() ? tmp.get() : null;
        return result;
    }

    /**
     * If the node is a variable, returns the variable.
     * Otherwise returns the given node
     *
     * @param model
     * @param node
     * @return
     */
    public static Node readNode(RDFNode node, Map<RDFNode, Node> modelToNode) {
        Model model = node.getModel();

        Node result;

        Node tmp = null;
        if(node != null & node.isResource()) {
            Resource r = node.asResource();
            RDFNode o = model.listObjectsOfProperty(r, SP.varName).toList().stream().findFirst().orElse(null);
            if(o != null) {
                String varName = o.asLiteral().getString();
                tmp = Var.alloc(varName);
            }
        }

        result = tmp == null
                ? node.asNode()
                : tmp;

        if(modelToNode != null) {
            modelToNode.putIfAbsent(node, result);
        }

        return result;
    }

    public static Optional<Triple> readTriple(Resource r, Map<RDFNode, Node> modelToNode) {
        Node s = readObject(r, SP.subject).map(x -> readNode(x, modelToNode)).orElse(null);
        Node p = readObject(r, SP.predicate).map(x -> readNode(x, modelToNode)).orElse(null);
        Node o = readObject(r, SP.object).map(x -> readNode(x, modelToNode)).orElse(null);

        Optional<Triple> result = s == null || p == null || o == null
                ? Optional.empty()
                : Optional.of(new Triple(s, p, o));

        return result;
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
