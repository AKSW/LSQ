package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


@ResourceView
public interface JoinVertexExec
    extends ElementExec
{
    @Iri(LSQ.Strs.hasJoinVarExec)
    @Inverse
    BgpNode getBgpNode();
    JoinVertexExec setBgpNode(Resource bpgNode);


    @Iri(LSQ.Strs.tpSelBGPRestricted)
    BigDecimal getBgpRestrictedSelectivitiy();
    JoinVertexExec setBgpRestrictedSelectivitiy(BigDecimal selectivity);


    @Iri(LSQ.Strs.hasJoinVarExec)
    @Inverse
    SpinBgpExec getBgpExec();
    JoinVertexExec setBgpExec(SpinBgpExec exec);
//
//    SpinBgpExec getBgpNodeExec();
//    JoinVertexExec setBgpNodeExec(SpinBgp exec);

}
