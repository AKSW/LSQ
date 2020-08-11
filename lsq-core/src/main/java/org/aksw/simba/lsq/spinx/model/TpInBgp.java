package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

/**
 * A mention of a triple pattern in a bgp
 *
 *
 * @author raven
 *
 */
@ResourceView
public interface TpInBgp
    extends Resource
{
    @HashId
    @Iri(LSQ.Strs.hasBgp)
    SpinBgp getBgp();
    TpInBgp setBgp(Resource bgp);

    @HashId
    @Iri(LSQ.Strs.hasTp)
    LsqTriplePattern getTriplePattern();
    TpInBgp setTriplePattern(Resource tp);

    @Iri(LSQ.Strs.hasTpExec)
    Set<TpInBgpExec> getExecs();

//    default Map<Resource, TpInBgpExec> indexExecs() {
//        Set<TpInBgpExec> res = getExecs();
//        Map<Resource, TpInBgpExec> result = res.stream()
//                .collect(Collectors.toMap(r -> r.getBenchmarkRun(), r -> r));
//        return result;
//    }

    // @SortedBy(TIME.atTime)
    //Map<String, TpMentionedInBgpExec> getExecutions();
}
