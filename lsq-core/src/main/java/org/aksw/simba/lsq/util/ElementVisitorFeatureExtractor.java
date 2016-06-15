package org.aksw.simba.lsq.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathLib;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ElementVisitorFeatureExtractor.class);

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

        Expr baseExpr = el.getExpr();
        List<Expr> exprs = ExprUtils.linearizePrefix(baseExpr, Collections.emptySet()).collect(Collectors.toList());

        for(Expr expr : exprs) {
            features.add(LSQ.Functions);

            if(expr.isFunction()) {
                // TODO Will use full URIs for custom sparql functions - may want to shorten them with prefixes
                String fnName = ExprUtils.getFunctionId(expr.getFunction());

                Resource fnRes = ResourceFactory.createResource(LSQ.ns + "fn-" + fnName);
                features.add(fnRes);
            }
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
        Op op = PathLib.pathToTriples(el.getPattern());

        if(op instanceof OpBGP) {
            features.add(SP.TriplePattern);
        } else if (op instanceof OpPath) {
            OpPath opPath = (OpPath)op;
            Path path = opPath.getTriplePath().getPath();

            features.add(SP.TriplePath);

            Set<Resource> pathFeatures = PathVisitorFeatureExtractor.getFeatures(path);
            features.addAll(pathFeatures);


        } else {
            logger.warn("Unexpected algebra expression: " + op);
        }
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
            result.add(LSQ.Distinct);
        }

        if(query.isReduced()) {
            result.add(LSQ.Reduced);
        }

        if(query.hasOrderBy()) {
            result.add(LSQ.OrderBy);
        }

        if(query.hasGroupBy()) {
            result.add(LSQ.GroupBy);
        }

        if(query.getLimit() != query.NOLIMIT) {
            result.add(LSQ.Limit);
        }

        if(query.getOffset() != query.NOLIMIT && query.getOffset() != 0) {
            result.add(LSQ.Offset);
        }

        if(query.hasAggregators()) {
            result.add(LSQ.Aggregators);


            List<ExprAggregator> aggs = query.getAggregators();
            for(ExprAggregator agg : aggs) {
                Resource fnRes = ResourceFactory.createResource(LSQ.ns + "agg-" + agg.getAggregator().getName().toLowerCase());
                result.add(fnRes);
            }

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
