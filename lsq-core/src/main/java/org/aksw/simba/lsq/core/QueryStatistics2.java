package org.aksw.simba.lsq.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.model.util.SpinCoreUtils;
import org.aksw.simba.lsq.spinx.model.Bgp;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

class ElementJoin {
    protected Element lhs;
    protected Element rhs;
    protected Set<Var> vars;

    public ElementJoin(Element lhs, Element rhs, Set<Var> vars) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
        this.vars = vars;
    }

}

public class QueryStatistics2 {

    /**
     *
     * SELECT COUNT(*) { SELECT DISTINCT $lhsVars$ { lhs rhs } }
     *
     *
     *
     *
     * @param qef
     * @param lhs
     * @param rhs
     * @param lhsVars
     */
    public static long fetchCountDistinctBindings(QueryExecutionFactory qef, Element e, Set<Var> lhsVars) {
        Concept c = createConceptCountDistinctBindings(e, lhsVars);

//        Query q = ((ElementSubQuery)c.getElement()).getQuery();
//        System.out.println(q);
//        System.out.println(ResultSetFormatter.asText(qef.createQueryExecution(q).execSelect()));

        //ServiceUtils.fetchCountConcept(sparqlService, concept, itemLimit, rowLimit)L
        Query query = ConceptUtils.createQueryList(c);
        Integer tmp = ServiceUtils.fetchInteger(qef, query, c.getVar());
        Long result = tmp.longValue();

        return result;
    }

    public static Concept createConceptCountDistinctBindings(Element lhs, Set<Var> lhsVars) {
        Element sub;
        lhsVars = lhsVars == null ? new HashSet<>(PatternVars.vars(lhs)) : lhsVars;
        if(!lhsVars.isEmpty()) {
            Query subQuery = new Query();
            subQuery.setQuerySelectType();
            subQuery.setDistinct(true);
            subQuery.addProjectVars(lhsVars);
            //ElementGroup pattern = new ElementGroup();
            //pattern.addElement(lhs);
            //pattern.addElement(rhs);
            subQuery.setQueryPattern(lhs);
            sub = new ElementSubQuery(subQuery);
        } else {
            sub = lhs;
        }

        Query query = new Query();
        Expr agg = query.allocAggregate(new AggCount());
        query.getProject().add(Vars.c, agg);
        query.setQuerySelectType();
        query.setQueryPattern(sub);

        Concept result = new Concept(new ElementSubQuery(query), Vars.c);
        //System.out.println(result);
        return result;
    }

    /**
     * For each triple, return its corresponding number of distinct bindings in regard to the other triples (or element in general)
     * @param qef
     * @param triples
     * @return
     */
    public static Map<org.topbraid.spin.model.Triple, Long> fetchRestrictedResultSetRowCount(QueryExecutionFactory qef, Collection<? extends org.topbraid.spin.model.Triple> triples) {
        Map<org.topbraid.spin.model.Triple, Element> map = MapUtils.index(triples, t -> ElementUtils.createElement(SpinCoreUtils.toJenaTriple(t)));

        Map<org.topbraid.spin.model.Triple, Long> result = fetchRestrictedResultSetRowCount(qef, map);
        return result;
    }


    public static Map<Var, Long> fetchCountVarJoin(QueryExecutionFactory qef, Collection<org.topbraid.spin.model.Triple> triples) {
        Set<Element> map = triples.stream()
                .map(t -> ElementUtils.createElement(SpinCoreUtils.toJenaTriple(t)))
                .collect(Collectors.toSet());

        Map<Var, Long>  result = fetchCountJoinVarGroup(qef, map);
        return result;
    }


    // TODO: For each element we might need a function to the set of variables we are counting for
    /**
     * Return for each element the number of bindings for every incident join variable
     *
     * @param qef
     * @param itemToElement
     * @return
     */
    public static <T> Map<T, Map<Var, Long>> fetchCountJoinVarElement(QueryExecutionFactory qef, Map<T, Element> itemToElement) {
        Multimap<Var, Element> varToEls = indexElementsByVar(itemToElement.values());

        Set<Var> joinVars = varToEls.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        Map<T, Map<Var, Long>> result = itemToElement.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                // for each var of the element that is a join var...
                e -> Sets.intersection(new HashSet<>(PatternVars.vars(e.getValue())), joinVars).stream()
                    .collect(Collectors.toMap(
                        jv -> jv,
                        jv -> fetchCountDistinctBindings(qef, e.getValue(), Collections.singleton(jv))
                    ))
            ));

        return result;
    }


    // Note: join vars are those mapping to more than 1 element
    public static Multimap<Var, Element> indexElementsByVar(Collection<Element> elements) {
        Multimap<Var, Element> result = ArrayListMultimap.create();

        for(Element e : elements) {
            Collection<Var> vars = PatternVars.vars(e);
            for(Var var : vars) {
                result.put(var, e);
            }
        }

        return result;
    }

    /**
     * Fetches the number of distinct bindings for each join variable in the given collection of elements
     *
     * @param qef
     * @param elements
     * @return
     */
    public static Map<Var, Long> fetchCountJoinVarGroup(QueryExecutionFactory qef, Collection<Element> elements) { //Collection<T> items, Function<T, Element> itemToElement) {
        // Index the elements by their mentioned variables
        Multimap<Var, Element> varToElements = indexElementsByVar(elements);

        // For each variable, get the number of bindings based on all elements in which the var occurs
        Map<Var, Long> result = varToElements.asMap().entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .collect(Collectors.toMap(
                Entry::getKey,
                e -> {
                    Var v = e.getKey();
                    ElementGroup group = new ElementGroup();
                    e.getValue().forEach(group::addElement);

                    Long value = fetchCountDistinctBindings(qef, group, Collections.singleton(v));

                    return value;
                }));

        return result;
    }



    // public static Map<org.topbraid.spin.model.Triple, Long>
    // computeSelectivity(Collection<) {
    /**
     * The restricted result set row count per element is for each element the number of distinct bindings for its mentioned variables
     *
     * @param qef
     * @param map
     * @return
     */
    public static <T> Map<T, Long> fetchRestrictedResultSetRowCount(QueryExecutionFactory qef, Map<T, Element> map) { //Collection<T> items, Function<T, Element> itemToElement) {
        ElementGroup group = new ElementGroup();
        map.values().forEach(group::addElement);

        // TODO flatten group

        Map<T, Long> result = new LinkedHashMap<>();//new IdentityHashMap<>();

        map.entrySet().forEach(e -> {
            Element el = e.getValue();
            Set<Var> vars = Sets.newLinkedHashSet(PatternVars.vars(el));
            Long value = fetchCountDistinctBindings(qef, group, vars);
            result.put(e.getKey(), value);
        });

        return result;
    }

    // public static Stream<ElementJoin> computeSelectivity(Set<Triple> quads) {
    // Stream<ElementJoin> result = quads.stream().map(lhsTriple -> {
    // Set<Triple> rhsTriples = Sets.difference(quads,
    // Collections.singleton(lhsTriple));
    //
    // ElementJoin r = new ElementJoin();
    // return r;
    // });
    //
    // result.map(join -> {
    //
    // })
    //
    // return result;
    // }

    // public static Stream<ElementJoin> computeSelectivity(Set<Quad> quads,
    // List<Expr> filters) {
    // Stream<ElementJoin> result = quads.stream().map(lhsQuad -> {
    // Set<Quad> rhsQuads = Sets.difference(quads,
    // Collections.singleton(lhsQuad));
    //
    // return null;
    // });
    //
    // return result;
    // }

    /**
     * linearize any structure into a flat list
     *
     * @param op
     * @param stopMarker
     * @param getChildren
     * @return
     */
    public static <T> Stream<T> linearizePrefix(T op, T stopMarker,
            Function<? super T, Iterable<? extends T>> getChildren) {
        boolean isIdentity = op == stopMarker || (stopMarker != null && stopMarker.equals(op));
        Stream<T> tmp;
        if (isIdentity) {
            tmp = Stream.empty();
        } else {
            Iterable<? extends T> children = getChildren.apply(op);
            Stream<? extends T> x = StreamSupport.stream(children.spliterator(), false);
            tmp = Stream.concat(x, Stream.of(stopMarker));
        }

        Stream<T> result = Stream.concat(Stream.of(op), // Emit parent
                tmp.flatMap(e -> linearizePrefix(e, stopMarker, getChildren)));

        return result;
    }

    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if (op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1) op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2) op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN) op).getElements();
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static int propertyDegree(Resource r, Property... ps) {
        int result = Arrays.asList(ps)
                .stream()
                .distinct()
                .mapToInt(p -> r.listProperties(p)
                .toList().size()).sum();

        return result;
    }

    public static Resource getJoinVertexType(Resource r) {
        int indeg = propertyDegree(r, LSQ.in);
        int outdeg = propertyDegree(r, LSQ.out);

        Resource result;
        if (indeg == 0) { // && outdeg > 0
            result = LSQ.star;
        } else if (outdeg == 0) {
            result = LSQ.sink;
        } else if (indeg == 1 && outdeg == 1) {
            result = LSQ.path;
        } else {
            result = LSQ.hybrid;
        }

        return result;
    }

    public static Set<Resource> getJoinVertexCount(Model model) {
        Set<Resource> vertices = model.listResourcesWithProperty(RDF.type, LSQ.Vertex).toSet();

        // Note: We do this intermediate map for debugging / tracability reasons
        Map<Resource, Integer> vToDeg = vertices.stream()
                .collect(Collectors.toMap(v -> v, v -> propertyDegree(v, LSQ.in, LSQ.out)));

        // Return those keys in the map, whose degree is non-zero
        Set<Resource> result = vToDeg.entrySet().stream().filter(e -> e.getValue() > 0).map(Entry::getKey)
                .collect(Collectors.toSet());

        return result;
    }





    /**
     * Get the benchmark query features ( e.g resultsize, bgps mean join
     * vertices etc)
     *
     * @param query
     *            SPARQL query
     * @return stats Query Features as string
     * @throws MalformedQueryException
     */
    public static void getDirectQueryRelatedRDFizedStats(SpinQueryEx queryRes, Resource featureRes) {



        // Map<RDFNode, Node> modelToNode = new HashMap<>();
//        Map<Resource, BasicPattern> resToBgp = SpinUtils.indexBasicPatterns(queryRes);

        // Get the node objects from the model and map them to the appropriate resources in the model
        // Especially Vars are represented by Resources having a SP.varName property
        Map<Node, RDFNode> nodeToModel = new IdentityHashMap<>();
//        Map<RDFNode, Node> rdfNodeToNode = new HashMap<>();
//        rdfNodeToNode.forEach((k, v) -> nodeToModel.put(v, k));

        // Make sure the BGP resources exist in the target model
//        resToBgp = resToBgp.entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey().inModel(targetRes.getModel()), Entry::getValue));

//        resToBgp.keySet().forEach(r -> targetRes.addProperty(LSQ.hasBGP, r));

        //getDirectQueryRelatedRDFizedStats(targetRes, resToBgp.values());

        List<Integer> degrees = queryRes.getBgps().stream()
                .flatMap(e -> setUpJoinVertices(e, nodeToModel).stream()).sorted()
                .collect(Collectors.toList());

        int n = degrees.size();
        int nhalf = n / 2;

        double avgJoinVertexDegree = degrees.stream().mapToInt(x -> x).average().orElse(0.0);

        // 1 2 3 4
        double medianJoinVertexDegree = n == 0 ? 0
                : (n % 2 == 0 ? (degrees.get(nhalf - 1) + degrees.get(nhalf)) / 2 : degrees.get(nhalf));

        // LSQ.me
        // queryRes.addProperty(LSQ.joinVert, o)
        // double meanJoinVertexDegree = joinVertexToDegree.values().stream()
        // .mapToInt(x -> x)
        // ???
        // .orElse(0.0);

        // This is on the query level
        featureRes
            .addLiteral(LSQ.joinVertices, degrees.size())
            .addLiteral(LSQ.meanJoinVertexDegree, avgJoinVertexDegree)
            .addLiteral(LSQ.medianJoinVertexsDegree, medianJoinVertexDegree);
    }


    /**
     * Requires BGP information to be present in the SPIN model.
     *
     * Extends the spin with min/max/total triples in bgps
     *
     * @param queryRes
     */
//    public static void enrichSpinQueryWithBgpStats(SpinQueryEx queryRes) {
//
//        List<Integer> bgpSizes = queryRes.getBgps().stream()
//                .map(SpinBgp::toBasicPattern)
//                .map(BasicPattern::size)
//                .collect(Collectors.toList());
//
//        // Find out minimum and maximum size of the bgpgs
//        int totalBgpCount = bgpSizes.size();
//        int maxBgpTripleCount = bgpSizes.stream().max(Integer::max).orElse(0);
//        int minBgpTripleCount = bgpSizes.stream().min(Integer::min).orElse(0);
//        int triplePatternCount = bgpSizes.stream().mapToInt(x -> x).sum();
//
//        queryRes
//            .setTotalBgpCount(totalBgpCount)
//            .setMinBgpTriples(minBgpTripleCount)
//            .setMaxBgpTriples(maxBgpTripleCount)
//            .setTriplePatternCount(triplePatternCount);
//    }



    /**
     * join vertex stats
     *
     * @param bgpRes
     * @param bgp
     * @param nodeToModel
     * @return
     */
    public static List<Integer> setUpJoinVertices(
            Bgp bgpRes,
            //Resource bgpRes,
            // BasicPattern bgp,
            Map<Node, RDFNode> nodeToModel
            ) {

//        BasicPattern bgp = bgpRes.toBasicPattern();
        // int bgpHash = (new HashSet<>(bgp.getList())).hashCode();

        // Create the hypergraph model over all bgps
        // (Could be changed if individual stats are desired)
        //Model hyperGraph = ModelFactory.createDefaultModel();
        Model hyperGraph = bgpRes.getModel();

        // for(BasicPattern bgp : bgps) {
        LsqEnrichments.enrichModelWithHyperGraphData(bgpRes);

        // System.out.println("HYPER");
        // hyperGraph.write(System.out, "TURTLE");

        Set<Resource> rawJoinVertices = bgpRes.getBgpNodes().stream()
            .filter(x -> x.hasProperty(RDF.type, LSQ.Vertex))
            .collect(Collectors.toSet());

//        Set<Resource> rawJoinVertices = hyperGraph.listResourcesWithProperty(RDF.type, LSQ.Vertex).toSet();

        Map<Resource, Integer> joinVertexToDegree = rawJoinVertices.stream()
                .collect(Collectors.toMap(r -> r, r -> propertyDegree(r, LSQ.out, LSQ.in)));

        // .filter(x -> x != 1) // Remove vertices that do not join
        joinVertexToDegree = joinVertexToDegree.entrySet().stream()
                .filter(e -> e.getValue() != 1)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<Resource> joinVertices = joinVertexToDegree.keySet();

        List<Integer> degrees = joinVertexToDegree.values().stream().sorted().collect(Collectors.toList());

        // list.add(value);
        // stats = stats + " lsqv:triplePatterns "+totalTriplePatterns +" ; ";
        // stats = stats + " lsqv:joinVertices "+joinVertices.size() +" ; ";
        // stats = stats + " lsqv:meanJoinVerticesDegree 0 . ";

        // stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsObject ";
        // stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsSubject ";
        // stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsPredicate ";
        // stats = stats + getMentionsTuple(predicates); // subjects and objects
        // ModelUtils.
        // ResourceUtils.
        NestedResource joinVertexNres = new NestedResource(bgpRes);

        for (Resource v : joinVertices) {
            // TODO Allocate a resource for the join vertex
            // Resource queryRes = null;
            Statement t = v.getProperty(RDFS.label);
            RDFNode o = t.getObject();
            String name = "" + toPrettyString(o);

            // System.out.println(name);
//            Resource joinVertexRes = joinVertexNres.nest("-jv-" + name).get();// lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex
            Resource joinVertexRes = v;

//            bgpRes.addProperty(LSQ.joinVertex, joinVertexRes);

            Resource joinVertexType = getJoinVertexType(v);
            int degree = joinVertexToDegree.get(v);

            //rdfNodeToNode = v.getProperty(LSQ.proxyFor).getObject();

//            Node proxyNode = v.getProperty(LSQ.proxyFor).getObject().asNode();
//            RDFNode proxyRdfNode = nodeToModel.get(proxyNode);

//            if(proxyRdfNode == null) {
//                throw new NullPointerException("Should not happen");
//            }

            joinVertexRes
                .addLiteral(LSQ.joinVertexDegree, degree)
                .addProperty(LSQ.joinVertexType, joinVertexType)
//                .addProperty(LSQ.proxyFor, proxyRdfNode)
            // .addProperty(LSQ.proxyFor,
            // v)//v.getPropertyResourceValue(LSQ.proxyFor))
            ;

        }

        return degrees;
    }

    public static String toPrettyString(RDFNode node) {
        String result;

        if (node.isURIResource()) {
            Resource r = node.asResource();
            String ns = r.getNameSpace();
            String localName = r.getLocalName();

            String nsHash = StringUtils.md5Hash(ns).substring(0, 8);
            String safeLocalName = StringUtils.urlEncode(localName);

            result = nsHash + "-" + safeLocalName;
        } else {
            result = StringUtils.urlEncode("" + node);
        }
        return result;
    }

    // Resource joinVertexType = getJoinVertexType(r);
    // String joinVertexType = "" ; // {Star, path, hybrid, sink}
    // for(Vertex jv:joinVertices)
    // {
    // String joinVertex = jv.label;
    // if(joinVertex.startsWith("http://") || joinVertex.startsWith("ftp://"))
    // joinVertex = "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
    // else{
    // joinVertex = "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
    // joinVertex = joinVertex.replace("?", "");
    // }
    //
    // stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:joinVertex "
    // + joinVertex + " . \n";
    // long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
    // joinVertexType = getJoinVertexType(jv);
    // stats = stats + joinVertex + " lsqv:joinVertexDegree " + joinVertexDegree
    // + " ; lsqv:joinVertexType lsqv:" + joinVertexType + " . " ;
    //
    // //System.out.println(" " + jv+ " Join Vertex Degree: " + joinVertexDegree
    // + ", Join Vertex Type: " + joinVertexType);
    // }
    // }
    // return stats ;
    // }

    public static void enrichWithMentions(Resource queryRes, Triple triple) {
        Node[] nodes = TripleUtils.toArray(triple);
        enrichWithMentions(queryRes, nodes);
    }

    public static void enrichWithMentions(Resource queryRes, TriplePath triplePath) {
        Node[] nodes = new Node[] { triplePath.getSubject(), triplePath.getPredicate(), triplePath.getObject() };
        enrichWithMentions(queryRes, nodes);
    }

    public static void enrichWithMentions(Resource queryRes, Node[] nodes) {
        Model model = queryRes.getModel();

        Property[] props = new Property[] { LSQ.mentionsSubject, LSQ.mentionsPredicate, LSQ.mentionsObject };
        // IntStream.range(0, 3).
        for (int i = 0; i < 3; ++i) {
            Property prop = props[i];
            Node node = nodes[i];
            if (node != null) {
                if (!node.isVariable()) {
                    RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(node, model);
                    // NodeUtils.
                    queryRes.addProperty(prop, rdfNode);
                } else {
                    // TODO I don't like turning variables into string literals
                    // ~ Claus
                    queryRes.addLiteral(prop, "?" + ((Var) node).getName());
                }
            }
        }
    }

    // TODO This method is useless for our use case as it does not establish a
    // relation to the SPIN model
//    public static void getDirectQueryRelatedRDFizedStats(Query query) {
//        Op op = Algebra.compile(query);
//
//        // Get all BGPs from the algebra
//        List<BasicPattern> bgps = linearizePrefix(op, null, QueryStatistics2::getSubOps)
//                .filter(o -> o != null && o instanceof OpBGP).map(o -> ((OpBGP) o).getPattern())
//                .collect(Collectors.toList());
//    }

    public static void enrichWithPropertyPaths(Resource queryRes, Query query) {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        List<Path> paths = linearizePrefix(op, null, QueryStatistics2::getSubOps)
                .filter(o -> o != null && o instanceof OpPath).map(o -> ((OpPath) o).getTriplePath().getPath())
                .collect(Collectors.toList());

        paths.forEach(path -> queryRes.addLiteral(LSQ.triplePath, "" + path));
    }

    public static void enrichWithMentions(Resource queryRes, Query query) {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        linearizePrefix(op, null, QueryStatistics2::getSubOps).filter(o -> o != null).forEach(o -> {
            if (o instanceof OpBGP) {
                BasicPattern bgp = ((OpBGP) o).getPattern();
                for (Triple triple : bgp) {
                    enrichWithMentions(queryRes, triple);
                }
            } else if (o instanceof OpPath) {
                TriplePath triplePath = ((OpPath) o).getTriplePath();
                enrichWithMentions(queryRes, triplePath);
            }
        });
    }

    // for(BasicPattern bgp : bgps) {
    // for(Triple t : bgp) {
    // enrichWithMentions(queryRes, t);;
    // }
    // }

}
