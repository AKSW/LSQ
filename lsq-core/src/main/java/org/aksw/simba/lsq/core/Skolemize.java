package org.aksw.simba.lsq.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.Closure;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.util.ResourceUtils;

public class Skolemize {

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
                    n -> baseUri + "-bn" + nodeToGlobalHash.get(n)
                ));


        map.entrySet().forEach(e -> ResourceUtils.renameResource(e.getKey(), e.getValue()));
    }

    public static String createSignature(Graph g, Node n, Function<? super Node, ? extends Node> nodeTransform) {
        List<Triple> rawSig = createRawSignature(g, n, nodeTransform);
        String result = StringUtils.md5Hash("" + rawSig).substring(0, 8);
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

}
