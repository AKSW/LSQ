package org.aksw.simba.lsq.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.topbraid.spin.vocabulary.SP;

/**
 * Class to extract a set of features (expressed in SPIN terms) from a
 * SPARQL element
 *
 * @author raven
 *
 */
public class ElementVisitorFeatureExtractor  extends ElementVisitorBase
{
    protected Set<Resource> features = new HashSet<Resource>();

    @Override
    public void visit(ElementUnion el) {
        features.add(SP.Union);
    }

    @Override
    public void visit(ElementOptional el) {
        features.add(SP.Optional);
    }

    @Override
    public void visit(ElementFilter el) {
        features.add(SP.Filter);

        Expr exp =  el.getExpr();
        if(exp.toString().startsWith("regex(")) {
            features.add(SP.regex);
        }
    }

    @Override
    public void visit(ElementBind el) {
        features.add(SP.Bind);
    }

    @Override
    public void visit(ElementService el) {
        features.add(SP.Service);
    }

    @Override
    public void visit(ElementExists el) {
        features.add(SP.Exists);
    }

    @Override
    public void visit(ElementNotExists el) {
        features.add(SP.NotExists);
    }

    @Override
    public void visit(ElementMinus el) {
        features.add(SP.Minus);
    }

    @Override
    public void visit(ElementNamedGraph el) {
        features.add(SP.NamedGraph);
    }

    @Override
    public void visit(ElementPathBlock el) {
        features.add(SP.TriplePath);
    }

    @Override
    public void visit(ElementSubQuery el) {
        features.add(SP.SubQuery);

        Element subEl = el.getQuery().getQueryPattern();

        ElementWalker.walk(subEl, this);
    }


    public Set<Resource> getFeatures() {
        return this.features;
    }

    public static Set<Resource> getFeatures(Query query) {
        Set<Resource> result = getFeatures(query.getQueryPattern());

        if(query.isDistinct()) {
            // TODO We are hijacking the SP namespace here
            result.add(ResourceFactory.createResource(SP.getURI() + "Distinct"));
        }

        return result;
    }

    public static Set<Resource> getFeatures(Element element) {
        Set<Resource> result;

        if(element != null)
        {
            ElementVisitorFeatureExtractor visitor = new ElementVisitorFeatureExtractor();
            ElementWalker.walk(element, visitor);
            result = visitor.getFeatures();
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

}
