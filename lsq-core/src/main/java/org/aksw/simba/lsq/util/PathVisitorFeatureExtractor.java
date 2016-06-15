package org.aksw.simba.lsq.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathWalker;
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
    protected Set<Resource> features = new HashSet<>();

    public Set<Resource> getFeatures() {
        return features;
    }

    public static Set<Resource> getFeatures(Path path) {
        Set<Resource> result;

        if(path != null)
        {
            PathVisitorFeatureExtractor visitor = new PathVisitorFeatureExtractor();
            PathWalker.walk(path, visitor);
            result = visitor.getFeatures();
        } else {
            result = Collections.emptySet();
        }
        return result;

    }


    @Override
    public void visit(P_Link pathNode) {
        features.add(LSQ.LinkPath);
    }

    @Override
    public void visit(P_ReverseLink pathNode) {
        features.add(LSQ.ReverseLinkPath);
    }

    @Override
    public void visit(P_NegPropSet pathNotOneOf) {
        features.add(LSQ.NegPropSetPath);
    }

    @Override
    public void visit(P_Inverse inversePath) {
        features.add(LSQ.InversePath);
    }

    @Override
    public void visit(P_Mod pathMod) {
        features.add(LSQ.ModPath);
    }

    @Override
    public void visit(P_FixedLength pFixedLength) {
        features.add(LSQ.FixedLengthPath);
    }

    @Override
    public void visit(P_Distinct pathDistinct) {
        features.add(LSQ.DistinctPath);
    }

    @Override
    public void visit(P_Multi pathMulti) {
        features.add(LSQ.MultiPath);
    }

    @Override
    public void visit(P_Shortest pathShortest) {
        features.add(LSQ.ShortestPath);
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        features.add(LSQ.ZeroOrOnePath);
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        features.add(LSQ.ZeroOrMore1Path);
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        features.add(LSQ.ZeroOrMoreNPath);
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        features.add(LSQ.OneOrMore1Path);
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        features.add(LSQ.OneOrMoreNPath);
    }

    @Override
    public void visit(P_Alt pathAlt) {
        features.add(LSQ.AltPath);
    }

    @Override
    public void visit(P_Seq pathSeq) {
        features.add(LSQ.SeqPath);
    }

}
