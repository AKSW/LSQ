package org.aksw.simba.lsq.model.util;

import java.util.Map;
import java.util.Optional;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.spinrdf.model.Variable;
import org.spinrdf.vocabulary.SP;

public class SpinCoreUtils {

    public static Node readNode(RDFNode rdfNode) {
        Node result = null;
        if(rdfNode != null && rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            String varName = ResourceUtils.getLiteralPropertyValue(r, SP.varName, String.class);
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
                ? readNode(rdfNode)
                : rdfNodeToNode.computeIfAbsent(rdfNode, SpinCoreUtils::readNode);

        return result;
    }


    public static Node toNode(RDFNode node) {
        Node result = node.canAs(Variable.class)
            ? Var.alloc(node.as(Variable.class).getName())
            : node.asNode();
        return result;
    }

    public static Triple toJenaTriple(org.spinrdf.model.Triple t) {
        Triple result = new Triple(toNode(t.getSubject()), toNode(t.getPredicate()), toNode(t.getObject()));
        return result;
    }

}
