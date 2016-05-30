package org.aksw.simba.lsq.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class QueryStatistics2 {
    /**
     * Analyze the query for a set of structural features (e.g. use of optional, union, exists, etc...)
     * and attach them to the given resource
     *
     * @param resource The resource on which to attach the features
     * @param query The query object from which to extract the features
     */
    public static void enrichResourceWithQueryFeatures(Resource resource, Query query) {
        Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
        for(Resource feature : features) {
            resource.addProperty(LSQ.usesFeature, feature);
        }

        if(features.isEmpty()) {
            // TODO hack; use proper vocab
            resource.addProperty(LSQ.usesFeature, RDF.nil);
        }
    }


    /**
     * linearize any structure into a flat list
     *
     * @param op
     * @param stopMarker
     * @param getChildren
     * @return
     */
    public static <T> Stream<T> linearizePrefix(T op, T stopMarker, Function<? super T, Iterable<? extends T>> getChildren) {
        boolean isIdentity = op == stopMarker || (stopMarker != null && stopMarker.equals(op));
        Stream<T> tmp;
        if(isIdentity) {
            tmp = Stream.empty();
        } else {
            Iterable<?extends T> children = getChildren.apply(op);
            Stream<? extends T> x = StreamSupport.stream(children.spliterator(), false);
            tmp = Stream.concat(x, Stream.of(stopMarker));
        }

        Stream<T> result = Stream.concat(
                Stream.of(op), // Emit parent
                tmp.flatMap(e -> linearizePrefix(e, stopMarker, getChildren)));

        return result;
    }

    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if(op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1)op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2)op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN)op).getElements();
        } else {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static int propertyDegree(Resource r, Property... ps) {
        int result = new HashSet<>(Arrays.asList(ps)).stream()
            .mapToInt(p -> r.listProperties(p).toList().size())
            .sum();

        return result;
    }

    public static Resource getJoinVertexType(Resource r) {
        int indeg = propertyDegree(r, LSQ.in);
        int outdeg = propertyDegree(r, LSQ.out);

        Resource result;
        if(indeg == 0) {
            result = LSQ.Star;
        } else if(outdeg == 0) {
            result = LSQ.Sink;
        } else if(indeg == 1 && outdeg == 1) {
            result = LSQ.Path;
        } else {
            result = LSQ.Hybrid;
        }

        return result;
    }


    public static Set<Resource> getJoinVertexCount(Model model) {
        Set<Resource> vertices = model.listResourcesWithProperty(RDF.type, LSQ.Vertex).toSet();

        // Note: We do this intermediate map for debugging / tracability reasons
        Map<Resource, Integer> vToDeg = vertices.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> propertyDegree(v, LSQ.in, LSQ.out)));

        // Return those keys in the map, whose degree is non-zero
        Set<Resource> result = vToDeg.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .map(Entry::getKey)
            .collect(Collectors.toSet());

        return result;
    }



    /**
     * Creates a hypergraph model.
     *
     *
     *
     * @param result
     * @param nodeToResource Mapping from nodes to resources. Can be used to
     *   control whether e.g. nodes of different graph patters should map to the
     *   same or to different resources. This is an in/out argument.
     * @param triples
     */
    public static void enrichModelWithHyperGraphData(Model result, Map<Node, Resource> nodeToResource, Iterable<Triple> triples) {
        //result = result == null ? ModelFactory.createDefaultModel() : result;

        for(Triple t : triples)
        {
            // Get the triple's nodes
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();

            // Create anonymous resources as proxies for the original
            // triple and nodes - needed because RDF literals cannot appear in subject position
            // TODO We treat each triple different from the other even if they happen to be equal - is this desired?
            Resource tx = result.createResource();
            Resource sx = nodeToResource.merge(s, result.createResource(), (x, y) -> x);
            Resource px = nodeToResource.merge(p, result.createResource(), (x, y) -> x);
            Resource ox = nodeToResource.merge(o, result.createResource(), (x, y) -> x);

                        


            // Add the orginal nodes as annotations
            Resource ss = result.wrapAsResource(t.getSubject());
            Resource pp = result.wrapAsResource(t.getPredicate());
            RDFNode oo = result.asRDFNode(t.getObject());

            sx
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.subject, ss)
                .addProperty(LSQ.proxyFor, ss)
                .addLiteral(RDFS.label, "" + s);

            px
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.predicate, pp)
                .addProperty(LSQ.proxyFor, pp)
                .addLiteral(RDFS.label, "" + p);

            ox
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.object, oo)
                .addProperty(LSQ.proxyFor, oo)
                .addLiteral(RDFS.label, "" + p);

            tx
                .addProperty(RDF.type, LSQ.Edge)
                .addLiteral(RDFS.label, "" + t);


            result.add(sx, LSQ.out, tx);
            result.add(px, LSQ.in, tx);
            result.add(ox, LSQ.in, tx);
        }
        //return result;
    }



    /**
     * Get the benchmark query features ( e.g resultsize, bgps mean join vertices etc)
     * @param query SPARQL query
     * @return stats Query Features as string
     * @throws MalformedQueryException
     */
    public static void getDirectQueryRelatedRDFizedStats(Resource queryRes) {
        Map<Resource, BasicPattern> resToBgp = SpinUtils.indexBasicPatterns(queryRes);
        getDirectQueryRelatedRDFizedStats(queryRes, resToBgp.values());
    }


    public static void getDirectQueryRelatedRDFizedStats(Resource queryRes, Collection<BasicPattern> bgps) {
        Model model = queryRes.getModel();

        List<Integer> bgpSizes = bgps.stream()
                .map(BasicPattern::size)
                .collect(Collectors.toList());
        
        
        // Find out minimum and maximum size of the bgpgs
        int totalBgpCount = bgps.size();
        int maxBgpTripleCount = bgpSizes.stream().max(Integer::max).orElse(0);
        int minBgpTripleCount = bgpSizes.stream().min(Integer::min).orElse(0);
        int triplePatternCount = bgpSizes.stream().mapToInt(x -> x).sum();

        queryRes
            .addLiteral(LSQ.bgps, totalBgpCount)
            .addLiteral(LSQ.minBgpTriples, minBgpTripleCount)
            .addLiteral(LSQ.maxBgpTriples, maxBgpTripleCount)
            .addLiteral(LSQ.triplePatterns, triplePatternCount)
            ;
            
        
        
        // Create the hypergraph model over all bgps
        // (Could be changed if individual stats are desired)
        Model hyperGraph = ModelFactory.createDefaultModel();
        Map<Node, Resource> nodeToResource = new HashMap<Node, Resource>();
        for(BasicPattern bgp : bgps) {
            enrichModelWithHyperGraphData(hyperGraph, nodeToResource, bgp);
        }

        System.out.println("HYPER");
        hyperGraph.write(System.out, "TURTLE");

        Set<Resource> joinVertices = hyperGraph
                .listResourcesWithProperty(RDF.type, LSQ.Vertex)
                .toSet();

        Map<Resource, Integer> joinVertexToDegree = joinVertices.stream()
                .collect(Collectors.toMap(
                        r -> r,
                        r -> propertyDegree(r, LSQ.out, LSQ.in))
                );

        List<Integer> degrees = joinVertexToDegree.values().stream()
                .sorted().collect(Collectors.toList());
        int n = degrees.size();
        int nhalf = n / 2;

        double avgJoinVertexDegree = degrees.stream()
                .mapToInt(x -> x).average().orElse(0.0);

        // 1 2 3 4
        double medianJoinVertexDegree = n == 0 ? 0 : (n % 2 == 0
                ? (degrees.get(nhalf - 1) + degrees.get(nhalf)) / 2
                : degrees.get(nhalf));

//LSQ.me
        //queryRes.addProperty(LSQ.joinVert, o)
//        double meanJoinVertexDegree = joinVertexToDegree.values().stream()
//                .mapToInt(x -> x)
//                ???
//                .orElse(0.0);

        queryRes
            .addLiteral(LSQ.avgJoinVertexDegree, avgJoinVertexDegree)
            .addLiteral(LSQ.medianJoinVertexsDegree, medianJoinVertexDegree);

//        stats = stats + " lsqv:triplePatterns "+totalTriplePatterns  +" ; ";
//        stats = stats + " lsqv:joinVertices "+joinVertices.size()  +" ; ";
//        stats = stats + " lsqv:meanJoinVerticesDegree 0 . ";

//        stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsObject ";
//        stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsSubject ";
//            stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:mentionsPredicate ";
//        stats = stats + getMentionsTuple(predicates); // subjects and objects
//ModelUtils.
        //ResourceUtils.
        NestedResource joinVertexNres = new NestedResource(queryRes);


        for(Resource v : joinVertices) {
            // TODO Allocate a resource for the join vertex
            //Resource queryRes = null;
            Statement t = v.getProperty(RDFS.label);
            RDFNode o = t.getObject();
            String name = "" + toPrettyString(o);

            //System.out.println(name);
            Resource joinVertexRes = joinVertexNres.nest("jv-" + name).get();//lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex

            queryRes.addProperty(LSQ.joinVertex, joinVertexRes);

            Resource joinVertexType = getJoinVertexType(joinVertexRes);
            joinVertexRes.addProperty(LSQ.joinVertexType, joinVertexType);

            int degree = joinVertexToDegree.get(v);
            joinVertexRes.addLiteral(LSQ.joinVertexDegree, degree);
        }

    }

    public static String toPrettyString(RDFNode node) {
        String result;

        if(node.isURIResource()) {
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



//    Resource joinVertexType = getJoinVertexType(r);
//       String joinVertexType = "" ;   // {Star, path, hybrid, sink}
//        for(Vertex jv:joinVertices)
//        {
//            String joinVertex = jv.label;
//             if(joinVertex.startsWith("http://") || joinVertex.startsWith("ftp://"))
//                 joinVertex =  "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
//                 else{
//                     joinVertex =  "lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex;
//                     joinVertex = joinVertex.replace("?", "");
//                 }
//
//            stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+" lsqv:joinVertex " + joinVertex + " . \n";
//            long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
//            joinVertexType =  getJoinVertexType(jv);
//            stats = stats + joinVertex + " lsqv:joinVertexDegree " + joinVertexDegree + " ; lsqv:joinVertexType lsqv:" + joinVertexType + " . " ;
//
//            //System.out.println("     " + jv+ " Join Vertex Degree: " + joinVertexDegree + ", Join Vertex Type: " + joinVertexType);
//        }
//        }
//        return stats ;
//    }

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

        Property[] props = new Property[] {LSQ.mentionsSubject, LSQ.mentionsPredicate, LSQ.mentionsObject };
        //IntStream.range(0, 3).
        for(int i = 0; i < 3; ++i) {
            Property prop = props[i];
            Node node = nodes[i];
            if(node != null) {
                if(!node.isVariable()) {
                    RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(node, model);
                //NodeUtils.
                    queryRes.addProperty(prop, rdfNode);                    
                } else {
                    queryRes.addLiteral(prop, ((Var)node).getName());
                }
            }
        }
    }


    // TODO This method is useless for our use case as it does not establish a relation to the SPIN model
    public static void getDirectQueryRelatedRDFizedStats(Query query) {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        List<BasicPattern> bgps = linearizePrefix(op, null, QueryStatistics2::getSubOps)
                .filter(o -> o != null && o instanceof OpBGP)
                .map(o -> ((OpBGP)o).getPattern())
                .collect(Collectors.toList());
    }

    public static void enrichWithPropertyPaths(Resource queryRes, Query query) {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        List<Path> paths = linearizePrefix(op, null, QueryStatistics2::getSubOps)
                .filter(o -> o != null && o instanceof OpPath)
                .map(o -> ((OpPath)o).getTriplePath().getPath())
                .collect(Collectors.toList());

        paths.forEach(path -> queryRes.addLiteral(LSQ.triplePath, "" + path));
    }

    public static void enrichWithMentions(Resource queryRes, Query query) {
        Op op = Algebra.compile(query);

        // Get all BGPs from the algebra
        linearizePrefix(op, null, QueryStatistics2::getSubOps)
            .filter(o -> o != null)
            .forEach(o -> {
                if(o instanceof OpBGP) {
                    BasicPattern bgp = ((OpBGP)o).getPattern();
                    for(Triple triple : bgp) {
                        enrichWithMentions(queryRes, triple);
                    }
                } else if(o instanceof OpPath) {
                    TriplePath triplePath = ((OpPath)o).getTriplePath();
                    enrichWithMentions(queryRes, triplePath);
                }
            });
    }

//    for(BasicPattern bgp : bgps) {
//        for(Triple t : bgp) {
//            enrichWithMentions(queryRes, t);;
//        }
//    }

}
