package org.aksw.sparql.query.algebra.helpers;

import java.util.HashMap;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
/**
 * Generate basic graph patterns also called Disjunctive Normal Form (DNF) groups from a SPARQL query
 * @author Saleem
 *
 */
public class BGPGroupGenerator 

{
	/**
	 * Generate BGPs from a SPARQL query
	 * @param strQuery SPARQL query
	 * @return bgpGrps BGPs 
	 * @throws MalformedQueryException
	 */
	public static HashMap<Integer, List<StatementPattern>>  generateBgpGroups(String strQuery) throws MalformedQueryException
	{
		HashMap<Integer, List<StatementPattern>> bgpGrps = new HashMap<Integer, List<StatementPattern>>();
		int grpNo = 0;
		SPARQLParser parser = new SPARQLParser();
		ParsedQuery parsedQuery = parser.parseQuery(strQuery, null);
		TupleExpr query = parsedQuery.getTupleExpr();
		// collect all basic graph patterns

		for (TupleExpr bgp : BasicGraphPatternExtractor.process(query)) {
			//System.out.println(bgp);
			List<StatementPattern> patterns = StatementPatternCollector.process(bgp);	
			bgpGrps.put(grpNo, patterns );
			grpNo++;
		}

		return bgpGrps;
	}

}
