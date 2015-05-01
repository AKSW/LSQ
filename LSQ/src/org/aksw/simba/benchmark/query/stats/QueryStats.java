package org.aksw.simba.benchmark.query.stats;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class QueryStats
{
    public boolean union = false;
    public boolean filter = false;
    public boolean optional = false;
    public boolean subQuery = false;
    public boolean bind = false;
    public boolean service = false;
    public boolean notExists = false;
    public boolean exists = false;
    public boolean minus = false;
    public boolean namedGraph = false;
    public boolean propertyPath = false;
    public boolean regex= false;
    
    public boolean containsUnion(){
       	return this.union;
    }
    
    public boolean containsFilter(){
       	return this.filter;
    }
    
    public boolean containsOptional(){
       	return this.optional;
    }
    
    public boolean containsSubQuery(){
       	return this.subQuery;
    }
    public boolean containsBind(){
       	return this.bind;
    }
    public boolean containsServie(){
       	return this.service;
    }
    public boolean containsMinus(){
       	return this.minus;
    }
    
    public boolean containsExists(){
       	return this.exists;
    }
    public boolean containsNotExists(){
       	return this.notExists;
    }
    
    public boolean containsNamedGraph(){
       	return this.namedGraph;
    }
    
    public boolean containsPropertyPaths(){
       	return this.propertyPath;
    }
    public boolean containsRegex(){
       	return this.regex;
    }
    
 }


class ElementVisitorQueryStats  extends ElementVisitorBase
{
    private QueryStats queryStats = new QueryStats();

    @Override
    public void visit(ElementUnion el) {
        this.queryStats.union=true;
    }

    @Override
    public void visit(ElementOptional el) {
        this.queryStats.optional=true;
    }
    
  
    @Override
    public void visit(ElementFilter el) {
        this.queryStats.filter=true;
       Expr exp =  el.getExpr();
       if(exp.toString().startsWith("regex("))
       this.queryStats.regex=true;
    }
    
    @Override
    public void visit(ElementBind el) {
        this.queryStats.bind=true;
    }
    
    @Override
    public void visit(ElementService el) {
        this.queryStats.service=true;
    }
    
    @Override
    public void visit(ElementExists el) {
        this.queryStats.exists=true;
    }
    
    @Override
    public void visit(ElementNotExists el) {
        this.queryStats.notExists=true;
    }
    @Override
    public void visit(ElementMinus el) {
        this.queryStats.minus=true;
    }
    
    @Override
    public void visit(ElementNamedGraph el) {
        this.queryStats.namedGraph=true;
    }
    @Override
    public void visit(ElementPathBlock el) {
        this.queryStats.propertyPath=true;
    }
    @Override
    public void visit(ElementSubQuery el) {
        this.queryStats.subQuery=true;

        Element subEl = el.getQuery().getQueryPattern();

        ElementWalker.walk(subEl, this);
    }

    

    public QueryStats getQueryStats() {
        return this.queryStats;
    }
    

}


       