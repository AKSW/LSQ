package org.aksw.simba.lsq.util;


import java.util.Optional;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.topbraid.spin.vocabulary.SP;

public class SpinModelUtils {
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
    public static <T> T readObject(Model model, Resource s, Property p, Function<RDFNode, T> fn) {
        Optional<T> tmp = model.listObjectsOfProperty(s, p).toList().stream()
                .findFirst()
                .map(fn);

        T result = tmp.isPresent() ? tmp.get() : null;
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
    public static Node readNode(Model model, RDFNode node) {
        Node result;

        Node tmp = null;
        if(node != null & node.isResource()) {
            Resource r = node.asResource();
            RDFNode o = Iterables.getFirst(model.listObjectsOfProperty(r, SP.varName).toList(), null);
            if(o != null) {
                String varName = o.asLiteral().getString();
                tmp = Var.alloc(varName);
            }
        }

        result = tmp == null
                ? node.asNode()
                : tmp;

        return result;
    }

    public static Triple readTriple(Model model, Resource r) {
        Node s = readObject(model, r, SP.subject, x -> readNode(model, x));
        Node p = readObject(model, r, SP.predicate, x -> readNode(model, x));
        Node o = readObject(model, r, SP.object, x -> readNode(model, x));

        Triple result = new Triple(s, p, o);
        return result;
    }
}
