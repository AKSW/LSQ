package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.mapper.annotation.IdPrefix;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


@ResourceView
@IdPrefix("bgpnodeexec-")
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

    @StringId
    default String getStringId(HashIdCxt cxt) {

//    	SpinBgpExec bgpPart = getBgpExec(); // .getBgp();
//        BgpNode bgpNode = getBgpNode();
        // TODO Replace the prefix with e.g. cxt.getClassLabel(SpinBgpExec.class)
//        String result = "bgpExec-" + cxt.getHashAsString(bgp) + "-" + getLocalExecution().getBenchmarkRun().getIdentifier();
        SpinBgpExec exec = getBgpExec();
        LocalExecution le = exec.getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String result = "bgpNodeExec-" + cxt.getHashAsString(this) + "-" + cxt.getString(bmr);
        return result;
    }

//
//    SpinBgpExec getBgpNodeExec();
//    JoinVertexExec setBgpNodeExec(SpinBgp exec);

}
