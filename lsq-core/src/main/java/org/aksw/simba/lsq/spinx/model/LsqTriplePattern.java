package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.impl.TriplePatternImpl;

@ResourceView
public abstract class LsqTriplePattern
    extends TriplePatternImpl
    implements LsqElement
{
    public LsqTriplePattern(Node node, EnhGraph graph) {
        super(node, graph);
    }

    public Triple toJenaTriple() {
        Triple result = SpinUtils.toJenaTriple(this);
        return result;
    }

    @Iri(LSQ.Strs.hasTPExec)
    public abstract Set<TpExec> getTpExecs();

    public TpExec findTpExec(Resource expRun) {
        return null;
    }
}
