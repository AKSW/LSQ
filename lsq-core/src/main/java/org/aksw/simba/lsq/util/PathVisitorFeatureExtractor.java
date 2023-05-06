package org.aksw.simba.lsq.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jenax.sparql.path.PathWalker;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;


public class PathVisitorFeatureExtractor
    implements PathVisitor
{
    protected Map<Resource, Integer> featureToFrequency = new LinkedHashMap<>();

    public int incrementFeatureCount(Resource feature) {
        int newCount = ElementVisitorFeatureExtractor.addAndGet(featureToFrequency, feature, 1);
        return newCount;
    }


    public Map<Resource, Integer> getFeatures() {
        return featureToFrequency;
    }


    public static Map<Resource, Integer> getFeatures(Path path) {
        Map<Resource, Integer> result;

        if(path != null)
        {
            PathVisitorFeatureExtractor visitor = new PathVisitorFeatureExtractor();
            PathWalker.walk(path, visitor);
            result = visitor.getFeatures();
        } else {
            result = Collections.emptyMap();
        }
        return result;

    }


    @Override
    public void visit(P_Link pathNode) {
        incrementFeatureCount(LSQ.LinkPath);
    }

    @Override
    public void visit(P_ReverseLink pathNode) {
        incrementFeatureCount(LSQ.ReverseLinkPath);
    }

    @Override
    public void visit(P_NegPropSet pathNotOneOf) {
        incrementFeatureCount(LSQ.NegPropSetPath);
    }

    @Override
    public void visit(P_Inverse inversePath) {
        incrementFeatureCount(LSQ.InversePath);
    }

    @Override
    public void visit(P_Mod pathMod) {
        incrementFeatureCount(LSQ.ModPath);
    }

    @Override
    public void visit(P_FixedLength pFixedLength) {
        incrementFeatureCount(LSQ.FixedLengthPath);
    }

    @Override
    public void visit(P_Distinct pathDistinct) {
        incrementFeatureCount(LSQ.DistinctPath);
    }

    @Override
    public void visit(P_Multi pathMulti) {
        incrementFeatureCount(LSQ.MultiPath);
    }

    @Override
    public void visit(P_Shortest pathShortest) {
        incrementFeatureCount(LSQ.ShortestPath);
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        incrementFeatureCount(LSQ.ZeroOrOnePath);
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        incrementFeatureCount(LSQ.ZeroOrMore1Path);
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        incrementFeatureCount(LSQ.ZeroOrMoreNPath);
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        incrementFeatureCount(LSQ.OneOrMore1Path);
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        incrementFeatureCount(LSQ.OneOrMoreNPath);
    }

    @Override
    public void visit(P_Alt pathAlt) {
        incrementFeatureCount(LSQ.AltPath);
    }

    @Override
    public void visit(P_Seq pathSeq) {
        incrementFeatureCount(LSQ.SeqPath);
    }

}
