package org.aksw.simba.lsq.core.model;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.log4j.lf5.util.Resource;

public class JoinVertex
    extends ResourceImpl
{
    public JoinVertex(Node node, EnhGraph eh) {
        super(node, eh);
    }

    public Resource getBgpResource() {
        return null;
    }

    public Resource getVarResource() {
        return null;
    }

}
