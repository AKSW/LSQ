package org.aksw.simba.lsq.spinx.model;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.model.JoinVertex;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.topbraid.spin.model.Triple;
import org.topbraid.spin.model.TriplePattern;


@ResourceView
public interface SpinBgp
    extends Resource
{
    @Iri(LSQ.Strs.hasTP)
    List<TriplePattern> getTriplePatterns();

    @Iri(LSQ.Strs.joinVertex)
    Set<JoinVertex> getJoinVertices();

    @Iri(LSQ.Strs.hasBGPExec)
    Set<SpinBgpExec> getSpinBgpExecs();


    @Iri(LSQ.Strs.joinVertices)
    Integer getJoinVertexCount();
    SpinQueryEx setJoinVertexCount(Integer cnt);

    @Iri(LSQ.Strs.meanJoinVertexDegree)
    Integer getAvgJoinVertexDegree();
    SpinQueryEx setAvgJoinVertexDegree(Integer cnt);

    @Iri(LSQ.Strs.medianJoinVertexsDegree)
    Integer getMedianJoinVertexDegree();
    SpinQueryEx setMedianJoinVertexDegree(Integer cnt);


    default BasicPattern toBasicPattern() {
        BasicPattern result = new BasicPattern();

        List<TriplePattern> tps = getTriplePatterns();
        for(Triple tp : tps) {
            org.apache.jena.graph.Triple t = SpinUtils.toJenaTriple(tp);
            result.add(t);
        }

        return result;
    }
}
