package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.spinx.model.BgpInfo;
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
    extends Resource, BgpInfo
{
    @Iri(LSQ.Terms.hasStructuralFeatures)
    @Inverse
    @HashId(excludeRdfProperty = true)
    LsqQuery getQuery();


    @StringId
    default String getStringId(HashIdCxt cxt) {
        LsqQuery query = getQuery();
        String queryId = cxt.getStringId(query) + "-sf"; // cxt.getHashAsString(query);
        return queryId;
    }


    /**
     * Only applies to the SELECT query form.
     * Number of variables of the query's result set. Hence should also be set for 'SELECT *' queries.
     *
     * @return
     */
    @Iri(LSQ.Terms.projectVarCount)
    Integer getProjectVarCount();
    LsqStructuralFeatures setProjectVarCount(Integer number);

    // TODO Add the attributes here
    // Bgp summary
//    targetRes.addLiteral(LSQ.bgps, totalBgpCount).addLiteral(LSQ.minBGPTriples, minBgpTripleCount)
//    .addLiteral(LSQ.maxBGPTriples, maxBgpTripleCount).addLiteral(LSQ.tps, triplePatternCount);


//    @Iri(LSQ.Strs.hasBGP)
//    Set<SpinBgp> getBgps();

    @Iri(LSQ.Terms.bgpCount)
    Integer getBgpCount();
    LsqStructuralFeatures setBgpCount(Integer bgpCount);

    @Iri(LSQ.Terms.tpCount)
    Integer getTpCount();
    LsqStructuralFeatures setTpCount(Integer tpCount);


    @Iri(LSQ.Terms.tpInBgpCountMin)
    Integer getTpInBgpCountMin();
    LsqStructuralFeatures setTpInBgpCountMin(Integer tpInBgpCountMin);

    @Iri(LSQ.Terms.tpInBgpCountMax)
    Integer getTpInBgpCountMax();
    LsqStructuralFeatures setTpInBgpCountMax(Integer tpInBgpCountMax);

    @Iri(LSQ.Terms.tpInBgpCountMean)
    BigDecimal getTpInBgpCountMean();
    LsqStructuralFeatures setTpInBgpCountMean(BigDecimal tpInBgpCountMean);

    @Iri(LSQ.Terms.tpInBgpCountMedian)
    BigDecimal getTpInBgpCountMedian();
    LsqStructuralFeatures setTpInBgpCountMedian(BigDecimal tpInBgpCountMedian);


    /*
     * join vertex information
     */

    @Iri(LSQ.Terms.joinVertexCount)
    Integer getJoinVertexCount();
    LsqStructuralFeatures setJoinVertexCount(Integer bgpCount);

    @Iri(LSQ.Terms.joinVertexDegreeMean)
    BigDecimal getJoinVertexDegreeMean();
    LsqStructuralFeatures setJoinVertexDegreeMean(BigDecimal bgpCount);

    @Iri(LSQ.Terms.joinVertexDegreeMedian)
    BigDecimal getJoinVertexDegreeMedian();
    LsqStructuralFeatures setJoinVertexDegreeMedian(BigDecimal bgpCount);

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


