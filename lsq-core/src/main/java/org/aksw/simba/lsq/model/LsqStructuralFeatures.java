package org.aksw.simba.lsq.model;

import java.util.Set;

import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


/**
 * Interface for access to structural features of an lsq query.
 *
 *
 * @author Claus Stadler, Jan 7, 2019
 *
 */
@ResourceView
@HashId
public interface LsqStructuralFeatures
    extends Resource
{
    @Iri(LSQ.Strs.hasStructuralFeatures)
    @Inverse
    @HashId
    LsqQuery getQuery();


    @StringId
    default String getStringId(HashIdCxt cxt) {
        LsqQuery query = getQuery();
        String queryId = cxt.getString(query) + "-sf"; // cxt.getHashAsString(query);
        return "sf-" + queryId;
    }


    // numProjectVars
    @Iri(LSQ.Strs.projectVars)
    Integer getNumProjectVars();
    LsqStructuralFeatures setNumProjectVars(Integer number);

    // TODO Add the attributes here
    // Bgp summary
//    targetRes.addLiteral(LSQ.bgps, totalBgpCount).addLiteral(LSQ.minBGPTriples, minBgpTripleCount)
//    .addLiteral(LSQ.maxBGPTriples, maxBgpTripleCount).addLiteral(LSQ.tps, triplePatternCount);


    //LSQ.usesFeature
    Set<Resource> getQueryFeatures();


//    Set<BgpNode> getBgpNodes()
//    .addLiteral(LSQ.joinVertices, degrees.size())


//    .addLiteral(LSQ.meanJoinVertexDegree, avgJoinVertexDegree)
//    .addLiteral(LSQ.medianJoinVertexsDegree, medianJoinVertexDegree);


    //Set<QualifiedFeatureUsage> getQualifiedFeatures();


//    default RdfMap<String, QualifiedFeatureUsage> getQualifiedFeatures() {
//
//    }
    //Set<Bgp> hasBGP();

    //hasTP
    //TriplePattern


    // usesService

//    SpinUtils.enrichWithHasTriplePattern(featureRes, spinRes);
//    SpinUtils.enrichWithTriplePatternText(spinRes);
//    //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);
//
//    //
//    QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, featureRes);
//
//    QueryStatistics2.enrichWithPropertyPaths(featureRes, query);

}


