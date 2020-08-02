package org.aksw.simba.lsq.model;

import java.util.Set;

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


    /**
     * Only applies to the SELECT query form.
     * Number of variables of the query's result set. Hence should also be set for 'SELECT *' queries.
     *
     * @return
     */
    @Iri(LSQ.Strs.projectVarCount)
    Integer getProjectVarCount();
    LsqStructuralFeatures setProjectVarCount(Integer number);

    // TODO Add the attributes here
    // Bgp summary
//    targetRes.addLiteral(LSQ.bgps, totalBgpCount).addLiteral(LSQ.minBGPTriples, minBgpTripleCount)
//    .addLiteral(LSQ.maxBGPTriples, maxBgpTripleCount).addLiteral(LSQ.tps, triplePatternCount);

    @Iri(LSQ.Strs.bgpCountTotal)
    Integer getBgpCount();
    LsqStructuralFeatures setBgpCount(Integer bgpCount);

    @Iri(LSQ.Strs.tpCountTotal)
    Integer getTpsCount();
    LsqStructuralFeatures setTpCount(Integer tpCount);


    @Iri(LSQ.Strs.tpInBgpCountMin)
    Integer getTpInBgpMinCount();
    LsqStructuralFeatures setTpInBgpMinCount(Integer tpInBgpMinCount);

    @Iri(LSQ.Strs.tpInBgpCountMax)
    Integer getTpInBgpMaxCount();
    LsqStructuralFeatures setTpInBgpMaxCount(Integer tpInBgpMaxCount);

    @Iri(LSQ.Strs.tpInBgpCountMean)
    Integer getTpInBgpMeanCount();
    LsqStructuralFeatures setTpInBgpMeanCount(Integer tpInBgpAvgCount);

    @Iri(LSQ.Strs.tpInBgpCountMedian)
    Integer getTpInBgpMedianCount();
    LsqStructuralFeatures setTpInBgpMedianCount(Integer tpInBgpMedianCount);


    /*
     * join vertex information
     */

    @Iri(LSQ.Strs.joinVertexCountTotal)
    Integer getJoinVertexCount();
    LsqStructuralFeatures setJoinVertexCount(Integer bgpCount);

    @Iri(LSQ.Strs.joinVertexDegreeMean)
    Integer getJoinVertexDegreeMean();
    LsqStructuralFeatures setJoinVertexDegreeMean(Integer bgpCount);

    @Iri(LSQ.Strs.joinVertexDegreeMedian)
    Integer getJoinVertexDegreeMedian();
    LsqStructuralFeatures setJoinVertexDegreeMedian(Integer bgpCount);

//    @Iri(LSQ.Strs.joinVertexDegreeMin)
//    Integer getJoinVertexDegreeMin();
//    LsqStructuralFeatures setJoinVertexDegreeMin(Integer bgpCount);
//
//    @Iri(LSQ.Strs.joinVertexDegreeMax)
//    Integer getJoinVertexDegreeMax();
//    LsqStructuralFeatures setJoinVertexDegreeMax(Integer bgpCount);


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


