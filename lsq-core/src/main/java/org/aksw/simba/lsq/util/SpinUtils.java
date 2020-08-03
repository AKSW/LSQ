package org.aksw.simba.lsq.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

/**
 * SPIN utils - mainly for extracting Jena Triple and BasicPattern objects from SPIN RDF.
 * TODO Maybe the spin library already comes with a proper spin reader?
 *
 *
 */
public class SpinUtils {

    public static final Concept triplePatterns = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "x", "?x sp:subject ?s ; sp:predicate ?p ; sp:object ?o");

//    public static final Concept basicPatterns = Concept.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX sp: <http://spinrdf.org/sp#>", "x", "?foo !rdf:rest ?x . ?x (rdf:rest)*/rdf:first [ sp:subject ?s ; sp:predicate ?p ; sp:object ?o ]");

//  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
    // Not yet used
    public static final BinaryRelation rootedlistStarts = BinaryRelationImpl.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "?listStart rdf:first ?item "
            + "FILTER(NOT EXISTS { ?foo rdf:rest ?listStart} ) ",
            "root",
            "listStart"
            );

    // Not yet used
    public static final BinaryRelation tpRootedListStarts = BinaryRelationImpl.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX sp: <http://spinrdf.org/sp#>",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "?listStart rdf:first [] "
            + "FILTER(NOT EXISTS { [] rdf:rest ?listStart }) "
            + "?listStart (rdf:rest*/rdf:first) [ sp:subject ?s ; sp:predicate ?p ; sp:object ?o ] ",
            "root",
            "listStart"
            );

    public static final UnaryRelation tpListStarts = Concept.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX sp: <http://spinrdf.org/sp#>",
            "listStart",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "?listStart rdf:first [] "
            + "FILTER(NOT EXISTS { [] rdf:rest ?listStart }) "
            + "?listStart (rdf:rest*/rdf:first) [ sp:subject ?s ; sp:predicate ?p ; sp:object ?o ] "
     );

    public static final UnaryRelation tpNoList = Concept.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX sp: <http://spinrdf.org/sp#>",
            "listStart",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "FILTER(NOT EXISTS { [] rdf:first ?listStart }) "
            + "?listStart sp:subject ?s ; sp:predicate ?p ; sp:object ?o "
     );

    public static final Concept subjects = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:subject ?y");
    public static final Concept predicates = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:predicate ?y");
    public static final Concept objects = Concept.create("PREFIX sp: <http://spinrdf.org/sp#>", "y", "?x sp:object ?y");

    public static Map<Node, RDFNode> indexTripleNodes(org.topbraid.spin.model.Triple t) {
        Triple jt = SpinUtils.toJenaTriple(t);
        Map<Node, RDFNode> result = new HashMap<>();
        result.put(jt.getSubject(), t.getSubject());
        result.put(jt.getPredicate(), t.getPredicate());
        result.put(jt.getObject(), t.getObject());

        return result;
    }

    public static Map<RDFNode, Node> indexTripleNodes2(org.topbraid.spin.model.Triple t) {
        Triple jt = SpinUtils.toJenaTriple(t);
        Map<RDFNode, Node> result = new HashMap<>();
        result.put(t.getSubject(), jt.getSubject());
        result.put(t.getPredicate(), jt.getPredicate());
        result.put(t.getObject(), jt.getObject());

        return result;
    }

    public static int fetchTriplePatternExtensionSize(QueryExecutionFactory qef, Triple triple) {

        Var c = Var.alloc("_c_");
        // TODO Move to QueryGenerationUtils
        //Var d = Var.alloc("_d_");

        Query query = new Query();
        Expr expr = query.allocAggregate(new AggCount());
        query.getProject().add(c, expr);
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

    public static boolean isSpinTriple(RDFNode rdfNode) {
        boolean result = false;
        if(rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            result = r.hasProperty(SP.subject) && r.hasProperty(SP.predicate) && r.hasProperty(SP.object);
        }
        return result;
    }

    public static Multimap<Resource, org.topbraid.spin.model.Triple> indexBasicPatterns2(Model spinModel) {
        Multimap<Resource, org.topbraid.spin.model.Triple> result = ArrayListMultimap.create();

        {
            Set<Resource> ress = ConceptModelUtils
                    .listResourcesUnchecked(spinModel, tpListStarts, Resource.class)
                    .collect(Collectors.toSet()).blockingGet();


            for(Resource r : ress) {
                RDFList list = r.as(RDFList.class);
                for(RDFNode item : list.asJavaList()) {
                    boolean isSpinTriple = isSpinTriple(item);
                    if(isSpinTriple) {
                        TriplePattern tp = item.as(TriplePattern.class);
                        result.put(r, tp);
                    }
                }
            }
        }

        {
            Set<TriplePattern> ress = ConceptModelUtils
                    .listResourcesUnchecked(spinModel, tpNoList, TriplePattern.class)
                    .collect(Collectors.toSet()).blockingGet();

            for(TriplePattern item : ress) {
                result.put(item, item);
            }
        }

        return result;
    }

    public static Map<Resource, BasicPattern> indexBasicPatterns(Resource r) {
        Model spinModel = ResourceUtils.reachableClosure(r);
        Map<Resource, BasicPattern> result = indexBasicPatterns(spinModel);
        return result;
    }

    public static Map<Resource, BasicPattern> indexBasicPatterns(Model spinModel) {
//        spinModel.write(System.out, "NTRIPLES");

        List<Resource> ress = ConceptModelUtils.listResourcesUnchecked(spinModel, tpListStarts, Resource.class)
                .toList().blockingGet();

//        ress.stream().forEach(x -> System.out.println("GOT RES: " + x));

        Map<Resource, BasicPattern> result = ress
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        t -> {
                            Set<org.topbraid.spin.model.Triple> tmp = indexTriplePatterns(t);
                            BasicPattern r = new BasicPattern();
                            tmp.forEach(x -> r.add(toJenaTriple(x)));
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


    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns(Resource res) {
        Model spinModel = ResourceUtils.reachableClosure(res);
        Set<org.topbraid.spin.model.Triple> result = indexTriplePatterns(spinModel);
        return result;
    }

    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns(Model spinModel) {
        Set<org.topbraid.spin.model.Triple> result = ConceptModelUtils.listResourcesUnchecked(
                    spinModel, triplePatterns, org.topbraid.spin.model.TriplePattern.class)
                .map(x -> (org.topbraid.spin.model.Triple)x)
                .collect(Collectors.toSet())
                .blockingGet();
//        Map<Resource, Triple> result = ConceptModelUtils.listResources(spinModel, triplePatterns, )
//                .stream()
//                .collect(Collectors.toMap(
//                        Function.identity(),
//                        t -> readTriple(t, modelToNode).get()));
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

//    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns2(Resource res) {
//        Model spinModel = ResourceUtils.reachableClosure(res);
//        Set<org.topbraid.spin.model.Triple> result = indexTriplePatterns2(spinModel);
//        return result;
//    }
//    public static Set<org.topbraid.spin.model.Triple> indexTriplePatterns2(Model spinModel) {
//        Set<org.topbraid.spin.model.Triple> result = ConceptModelUtils.<org.topbraid.spin.model.Triple>listResourcesUnchecked(spinModel, triplePatterns, org.topbraid.spin.model.TriplePattern.class)
//                .collect(Collectors.toSet())
//                .blockingGet();
//        return result;
//    }

    public static void enrichWithHasTriplePattern(Resource targetRes, Resource spinRes) {
        Model spinModel = ResourceUtils.reachableClosure(spinRes);
        Set<org.topbraid.spin.model.Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach(r ->
            targetRes.addProperty(LSQ.hasTP, r)
        );
    }


    public static void enrichWithTriplePatternText(Resource queryRes) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Set<org.topbraid.spin.model.Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach(r -> r.inModel(queryRes.getModel())
                .addProperty(RDFS.label, FmtUtils.stringForTriple(toJenaTriple(r)) + " .")
                // .addProperty(RDFS.label, TripleUtils.toNTripleString(t))
                );
                //.addProperty(LSQ.triplePatternText, TripleUtils.toNTripleString(t)));
    }


    public static void enrichModelWithTriplePatternExtensionSizes(Resource queryRes, Resource queryExecRes, QueryExecutionFactory dataQef) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Set<org.topbraid.spin.model.Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        triplePatternIndex.forEach(r -> {
            int tripleCount = fetchTriplePatternExtensionSize(dataQef, toJenaTriple(r));
            //double selectivity = tripleCount / (double)totalTripleCount;

            spinModel.add(r, LSQ.itemCount, spinModel.createTypedLiteral(tripleCount));
        });
    }

    public static long countTriplePattern(QueryExecutionFactory qef, Triple t) {
        Query query = new Query();
        Expr aggExpr = query.allocAggregate(new AggCount());

        query.setQuerySelectType();
        query.getProject().add(Vars.c, aggExpr);
        query.setQueryPattern(ElementUtils.createElement(t));

        long result;
        try(QueryExecution qe = qef.createQueryExecution(query)) {
            result = ServiceUtils.fetchInteger(qe, Vars.c);
        }
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





    /**
     * Maps each triple pattern resource to the corresponding execution ressource
     *
     * @param queryRes
     * @param queryExecRes
     * @return
     */
    public static BiMap<org.topbraid.spin.model.Triple, Resource> createTriplePatternExecutions(Resource queryRes, Resource queryExecRes) {
        Model spinModel = ResourceUtils.reachableClosure(queryRes);
        Set<org.topbraid.spin.model.Triple> triplePatternIndex = indexTriplePatterns(spinModel);

        //Set<Resource> result = new HashSet<>();
        BiMap<org.topbraid.spin.model.Triple, Resource> result = HashBiMap.create();

        int i = 0;
        //triplePatternIndex.entrySet().forEach(e -> {
        for(org.topbraid.spin.model.Triple r : triplePatternIndex) {
            ++i;

            Resource queryTpExecRes = queryRes.getModel().createResource(queryExecRes.getURI() + "-tp-" + i);

            queryExecRes.addProperty(LSQ.hasTpExec, queryTpExecRes);

            queryTpExecRes
                //.addProperty(RDF.type, LSQ.tpExec)
                .addProperty(LSQ.hasTP, r);


            //result.add(queryTpExecRes);
            result.put(r.as(TriplePattern.class), queryTpExecRes);
        }

        return result;
    }

    public static void enrichModelWithTriplePatternSelectivities(Set<Resource> tpExecRess, QueryExecutionFactory qef, long totalTripleCount) {

        for(Resource tpExecRes : tpExecRess) {
            org.topbraid.spin.model.Triple spinTriple = tpExecRes.getProperty(LSQ.hasTP).getObject().as(TriplePattern.class);
            Triple triple = toJenaTriple(spinTriple);

            long count = countTriplePattern(qef, triple);

            double selectivity = totalTripleCount == 0 ? 0 : count / (double)totalTripleCount;

            tpExecRes
                .addLiteral(LSQ.itemCount, count)
                .addLiteral(LSQ.tpSel, selectivity);
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
//    @Deprecated
//    public static void enrichModelWithTriplePatternSelectivities(Resource queryRes, Resource queryExecRes, QueryExecutionFactory qef, long totalTripleCount) {
//
//        Model spinModel = ResourceUtils.reachableClosure(queryRes);
//        Map<Resource, Triple> triplePatternIndex = indexTriplePatterns(spinModel, null);
//
//        int i = 0;
//        //triplePatternIndex.entrySet().forEach(e -> {
//        for(Entry<Resource, Triple> e : triplePatternIndex.entrySet()) {
//            ++i;
//            Resource r = e.getKey();
//            Triple t = e.getValue();
//            long count = countTriplePattern(qef, t);
//
//            Resource queryTpExecRes = queryRes.getModel().createResource(queryExecRes.getURI() + "-tp-" + i);
//
//            queryExecRes
//                .addProperty(LSQ.hasTPExec, queryTpExecRes);
//
//
//            double selectivity = totalTripleCount == 0 ? 0 : count / (double)totalTripleCount;
//
//            queryTpExecRes
//                .addProperty(LSQ.hasTP, r)
//                .addLiteral(LSQ.resultSize, count)
//                .addLiteral(LSQ.tpSel, selectivity);
//        }
//
////        triplePatternIndex.keySet().forEach(r ->
////            queryRes.addProperty(LSQ.hasTriplePattern, r)
////        );
//
//
//        //double selectivity = tripleCount / (double)totalTripleCount;
//
//
//    }

//
//    public static void enrichModelWithJoinRestrictedTPSelectivities(
//        QueryExecutionFactory qef,
//        Model observationModel,
//        Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps)
//    {
//
//        for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {
//            // For each bgp, we now have a mapping for the vars
//            Map<Var, Long> joinVarCounts = QueryStatistics2.fetchCountVarJoin(qef, e.getValue());
//            //System.out.println("TP/BGP join var counts " + joinVarCounts);
//
//
//        }
//    }



    /**
     * TODO How to link to the join variable?
     * @param tpExecResource
     */
    public static void enrichModelWithBGPRestrictedTPSelectivities(
            QueryExecutionFactory qef,
            Model observationModel,
            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps) {

        // Map each triple pattern to the resource which will carry the observed metrics
        Map<org.topbraid.spin.model.Triple, Resource> tpToObservation = observationModel.listObjectsOfProperty(LSQ.hasTpExec).toSet().stream()
            .map(o -> o.asResource())
            .collect(Collectors.toMap(
                    o -> o.getPropertyResourceValue(LSQ.hasTP).as(TriplePattern.class),
                    o -> o));

        //Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = indexBasicPatterns2(queryRes);

        // TODO We need the absolute count for reference of the (unconstrained/filtered) TP
        //for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {

        // Compute the absolute counts for each triple in the bgp
        Map<org.topbraid.spin.model.Triple, Long> sels = QueryStatistics2.fetchRestrictedResultSetRowCount(qef, tpToObservation.keySet());

        for(Entry<org.topbraid.spin.model.Triple, Long> e : sels.entrySet()) {
            Resource observation = tpToObservation.get(e.getKey());
            Long count = e.getValue();

            long tpResultSetSize = observation.getProperty(LSQ.itemCount).getLong(); //e.getKey().getProperty(LSQ.triplePatternResultSize).getLong();
            double tpSelectivity = tpResultSetSize == 0 ? 0d : count / (double)tpResultSetSize;

            observation
                .addLiteral(LSQ.tpSelBGPRestricted, tpSelectivity);

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
     * @param rdfNode
     * @return
     */
    public static Node readNode(RDFNode rdfNode, Map<RDFNode, Node> rdfNodeToNode) {
        Node result = rdfNodeToNode == null
                ? SpinUtils.readNode(rdfNode)
                : rdfNodeToNode.computeIfAbsent(rdfNode, SpinUtils::readNode);

        return result;
    }

    public static Set<RDFNode> listRDFNodes(org.topbraid.spin.model.Triple triple) {
        Set<RDFNode> result = new LinkedHashSet<>();
        result.add(triple.getSubject());
        result.add(triple.getPredicate());
        result.add(triple.getObject());
        return result;
    }


    public static RDFNode writeNode(Model tgtModel, Node node) {
        RDFNode result = null;
        if(node != null) {
            if(node.isVariable()) {
                String varName = node.getName();
                Resource tmp = tgtModel.createResource();
                org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setLiteralProperty(
                        tmp, SP.varName, varName);
                result = tmp;
            } else {
                result = tgtModel.asRDFNode(node);
            }
        }

        return result;
    }

    public static Node readNode(RDFNode rdfNode) {
        Node result = null;
        if(rdfNode != null && rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            String varName = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.getLiteralPropertyValue(r, SP.varName, String.class);
            if(varName != null) {
                result = Var.alloc(varName);
            }
        }

        result = result == null ? rdfNode.asNode() : result;

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
