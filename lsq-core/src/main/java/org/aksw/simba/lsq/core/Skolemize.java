package org.aksw.simba.lsq.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.Closure;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.util.ResourceUtils;


//class ConciseBoundedDescription {
//    public Set<RDFNode> resolveCbd(RDFNode root) {
//        Set<RDFNode> open = new LinkedHashSet<>();
//        Set<RDFNode> seen = new HashSet<>();
//        Set<RDFNode> border = new LinkedHashSet<>();
//        open.add(root);
//
//        while(!open.isEmpty()) {
//            Iterator<RDFNode> it = open.iterator();
//            RDFNode node = it.next();
//            it.remove();
//
//            if(!root.isAnon()) {
//                border.add(node);
//            } else {
//                open.add(node);
//            }
//
//
//        }
//    }
//
//    /**
//     *
//     * @param model
//     * @param tgtId Read existing id from and write to this property (if present)
//     * @param srcId Read id from this property if tgtId has not yet been set
//     */
//    public void skolemize(Model model, Property tgtId, Property srcId) {
//        Traverser.forGraph(graph)
//        // For each blank node find the set of all paths to non-blank node resources
//        // So there is only the set of property paths across nodes to non-blank-node resources
//        // (fwd/bwd traversals?)
//    }
//}

// Eventually switch to http://blabel.github.io/
public class Skolemize {
    // Property for the skolemized id (without uri prefix and such)
    public static final Property skolemId = ResourceFactory.createProperty("http://tmp.aksw.org/skolemId");



    public static void skolemize2(Resource r) {

    }

    /**
     * Skolemizes blank nodes using a two phase approach:
     * First, for each individual blank node a signature string is created from its direct neighbors with blank nodes replaced with a constant.
     * Finally, computes the signature again, with blank node neighbors replaced by their signature string of the first pass.
     *
     * @param r
     */
    public static void skolemize(Resource r) {
        if(!r.isURIResource()) {
            throw new RuntimeException("This skolemization function requires a URI resource as input");
        }

        String baseUri = r.getURI();
        Model model = r.getModel();
        Model closure = Closure.closure(r, false);//ResourceUtils.reachableClosure(r);

        Graph g = closure.getGraph();
        Iterable<Node> allNodes = () -> GraphUtils.allNodes(g);

        Set<Node> blankNodes = StreamSupport.stream(allNodes.spliterator(), false)
                .filter(x -> x.isBlank() || x.isVariable())
                .collect(Collectors.toSet());


        NodeTransform unifyBlankNodes = (node) -> node.isBlank() || node.isVariable() ? Vars.a : node;

        Map<Node, Node> nodeToLocalHash = blankNodes.stream()
            .collect(Collectors.toMap(
                    x -> x,
                    x -> NodeFactory.createLiteral(createSignature(g, x, unifyBlankNodes))
            ));

        Map<Node, String> nodeToGlobalHash = blankNodes.stream()
                .collect(Collectors.toMap(
                        x -> x,
                        x -> createSignature(g, x, node -> nodeToLocalHash.getOrDefault(node, node))
                ));

        Map<Resource, String> map = blankNodes.stream()
                .collect(Collectors.toMap(
                    n -> (Resource)ModelUtils.convertGraphNodeToRDFNode(n, model),
                    n -> nodeToGlobalHash.get(n).substring(0, 8)
                ));


        map.entrySet().forEach(e -> e.getKey().addLiteral(skolemId, e.getValue()));

        map.entrySet().forEach(e -> ResourceUtils.renameResource(e.getKey(), baseUri + "-bn" + e.getValue()));
    }

    public static String createSignature(Graph g, Node n, Function<? super Node, ? extends Node> nodeTransform) {
        List<Triple> rawSig = createRawSignature(g, n, nodeTransform);
        String result = StringUtils.md5Hash("" + rawSig);
        return result;
    }

    public static List<Triple> createRawSignature(Graph g, Node n, Function<? super Node, ? extends Node> nodeTransform) {
        List<Triple> triples = g.find(n, Node.ANY, Node.ANY).andThen(g.find(Node.ANY, Node.ANY, n)).toList();

        NodeTransform fn = (node) -> nodeTransform.apply(node);

        List<Triple> result = triples.stream()
                .map(triple -> NodeTransformLib.transform(fn, triple))
                .sorted((a, b) -> ("" + a).compareTo("" + b))
                .collect(Collectors.toList());

        return result;
    }



//    public static void skolemizeOld(Resource r) {
//        Map<Resource, String> map = new HashMap<>();
//
//        // Casual hack to increment the count on function application
//        int[] counter = new int[] { 0 };
//        BiFunction<Resource, List<Property>, String> fn = (x, path) -> x.getURI() + "-bn" + (counter[0]++);
//
//        skolemizeOld(r, r, Collections.emptyList(), fn, map);
//
//        map.entrySet().forEach(e -> ResourceUtils.renameResource(e.getKey(), e.getValue()));
//    }
//
//    public static void skolemizeOld(Resource baseResource, Resource targetResource, List<Property> path, BiFunction<Resource, List<Property>, String> fn, Map<Resource, String> map) {
//        Set<Statement> stmts = targetResource.listProperties().toSet();
//        for(Statement stmt : stmts) {
////            if(stmt.getPredicate().equals(SP.where)) {
////                System.out.println("STMT: " + stmt);
////            }
//
//            RDFNode o = stmt.getObject();
//
//            if(o.isAnon()) {
//                Resource or = o.asResource();
//                String uri = fn.apply(baseResource, path);
//                if(uri != null) {
//                    map.put(or, uri);
//                }
//
//                Property p = stmt.getPredicate();
//
//                List<Property> newPath = new ArrayList<>(path);
//                newPath.add(p);
//
//                skolemizeOld(baseResource, or, newPath, fn, map);
//            }
//        }
//    }


    /**
     * Perform a depth first post order traversal.
     * Renames all encountered blank nodes that qualify for renaming.
     * Returns the (possibly renamed) start node
     *
     * @param r
     */
    public static RDFNode skolemizeTree(
            RDFNode start,
            boolean useInnerIris,
            BiFunction<Resource, String, String> getIRI,
            BiPredicate<? super RDFNode, ? super Integer> filterKeep) {
        Map<RDFNode, HashCode> map = ResourceTreeUtils.createGenericHashMap(start, useInnerIris, filterKeep);

        RDFNode result = start;

        for(Entry<RDFNode, HashCode> e : map.entrySet()) {
            RDFNode rdfNode = e.getKey();
            if(rdfNode.isAnon()) {
                Resource r = rdfNode.asResource();
                String hash = e.getValue().toString();
                // rdfNode.asResource().addLiteral(skolemId, hash);
                String newIri = getIRI.apply(r, hash);
                if(newIri != null) {
                    Resource tmp = ResourceUtils.renameResource(r, newIri);
                    if(r.equals(start)) {
                        result = tmp;
                    }
                }
            }
        }

        return result;
    }

}
