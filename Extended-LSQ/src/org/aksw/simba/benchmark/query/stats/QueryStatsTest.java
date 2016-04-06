package org.aksw.simba.benchmark.query.stats;

import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;

public class QueryStatsTest {

    public static void main(String[] args) {
         Query query = QueryFactory.create("SELECT  Min(?union) Avg(?p)  WHERE { Graph ?g {?union ?p ?o} Filter Regex(?o , 'dx') }",Syntax.syntaxARQ);
         QueryStatsTest qst = new QueryStatsTest();
         QueryStats queryStats = 	qst.getQueryStatsOjb(query);
         System.out.println("UNION: "+ queryStats.containsUnion());
        System.out.println("FILTER: "+queryStats.containsFilter());
        System.out.println("OPTIONAL: "+queryStats.containsOptional());
        System.out.println("SubQuery: "+queryStats.containsSubQuery());
        System.out.println("Bind: "+queryStats.containsBind());
        System.out.println("SERVICE: "+queryStats.containsServie());
        System.out.println("Minus: "+queryStats.containsMinus());
        System.out.println("Exists: "+queryStats.containsExists());
        System.out.println("NotExists: "+queryStats.containsNotExists());
        System.out.println("Named Graph: "+queryStats.containsNamedGraph());
        System.out.println("Property Paths: "+queryStats.containsPropertyPaths());
        System.out.println("Regex: "+queryStats.containsRegex());
        System.out.println("DISTINCT: "+query.isDistinct());
        System.out.println("Group By: "+query.hasGroupBy());
        System.out.println("LIMIT: "+query.hasLimit());
        System.out.println("Having: "+query.hasHaving());
        System.out.println("OFFSET: "+query.hasOffset());
        System.out.println("ORDER By: "+query.hasOrderBy());
        System.out.println("Values: "+query.hasValues());
      @SuppressWarnings("unused")
    boolean dsd ;
      if(( dsd = query.getDatasetDescription()!=null))
       {
           if(!query.getDatasetDescription().getNamedGraphURIs().isEmpty())
        System.out.println("From Named: true");
        if(query.getDatasetDescription().getNamedGraphURIs().isEmpty())
         System.out.println("From: true");
        }
       else
       {
           System.out.println("From Named: false");
           System.out.println("From: false");
       }
       boolean min = false, max =false, count = false, sum = false, avg = false;
       Iterator<ExprAggregator> itr = query.getAggregators().iterator();

       while(itr.hasNext())
       {
           String aggregator = itr.next().asSparqlExpr(new SerializationContext()).toString();
           if(aggregator.startsWith("min("))
             min = true;
           else  if(aggregator.startsWith("max("))
               max = true;
           else  if(aggregator.startsWith("sum("))
             sum = true;
           else  if(aggregator.startsWith("avg("))
             avg = true;
           else  if(aggregator.startsWith("count("))
             count = true;

       }
       System.out.println("Min: "+min);
       System.out.println("Max: "+max);
       System.out.println("Sum: "+sum);
       System.out.println("Avg: "+avg);
       System.out.println("Count: "+count);

 }

    public QueryStats getQueryStatsOjb(Query query) {
        QueryStats queryStats = null;
        ElementVisitorQueryStats visitor = new ElementVisitorQueryStats();
        Element element = query.getQueryPattern();
        if(!(element==null))
        {
        ElementWalker.walk(element, visitor);
        queryStats = visitor.getQueryStats();
        }
        return queryStats;
    }

}
