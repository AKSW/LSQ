package org.aksw.simba.lsq.core.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

import com.google.common.hash.HashCode;

// Eventually switch to http://blabel.github.io/
public class SkolemizeExtra {

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
            BiFunction<Resource, HashCode, String> getIRI,
            BiPredicate<? super RDFNode, ? super Integer> filterKeep) {
        Map<RDFNode, HashCode> map = ResourceTreeUtils.createGenericHashMap(start, useInnerIris, filterKeep);

        RDFNode result = start;

        for(Entry<RDFNode, HashCode> e : map.entrySet()) {
            RDFNode rdfNode = e.getKey();
            if(rdfNode.isAnon()) {
                Resource r = rdfNode.asResource();
                HashCode hashCode = e.getValue();
                //String hash = e.getValue().toString();
                // rdfNode.asResource().addLiteral(skolemId, hash);
                String newIri = getIRI.apply(r, hashCode);
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
    public static <T extends RDFNode> ResourceInDataset skolemize(
            ResourceInDataset queryInDataset,
            String baseIri,
            Class<T> cls) {
        T q = queryInDataset.as(cls);
        HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(q);
        Map<Node, Node> renames = hashIdCxt.getNodeMapping(baseIri);
        Node newRoot = renames.get(q.asNode());

        // Also rename the original graph name to match the IRI of the new lsq query root
        renames.put(NodeFactory.createURI(queryInDataset.getGraphName()), newRoot);

        Dataset dataset = queryInDataset.getDataset();
        // Apply an in-place node transform on the dataset
        // queryInDataset = ResourceInDatasetImpl.applyNodeTransform(queryInDataset, NodeTransformLib2.makeNullSafe(renames::get));
        NodeTransformLib2.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(renames::get), dataset);
        ResourceInDataset result = new ResourceInDatasetImpl(dataset, newRoot.getURI(), newRoot);
        return result;
    }
}
