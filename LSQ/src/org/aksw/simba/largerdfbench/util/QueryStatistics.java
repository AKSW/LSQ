package org.aksw.simba.largerdfbench.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.simba.dataset.lsq.LogRDFizer;
import org.aksw.simba.benchmark.query.stats.QueryStats;
import org.aksw.simba.benchmark.query.stats.QueryStatsTest;
import org.aksw.simba.hibiscus.hypergraph.HyperGraph.HyperEdge;
import org.aksw.simba.hibiscus.hypergraph.HyperGraph.Vertex;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;

/**
 * Generate various query statistics. 
 * @author Saleem
 *
 */
public class QueryStatistics {

	public static final int boundObjLiteralCount = 0;
	public static long boundSbjCount, boundPredCount, boundObjURICount, BoundObjLiteralCount,grandTotalTriplePatterns = 0 ; 

	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {

		String inputDir= "../BigRDFBench-Utilities/queries/";
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();
		long count = 1; 
		for (File qryFile : listOfFiles)
		{	
			BufferedReader br = new BufferedReader(new FileReader(inputDir+qryFile.getName()));
			String line;
			String queryStr="";
			while ((line = br.readLine()) != null) {
				queryStr= queryStr+" "+line;
			}
			br.close();

			System.out.println("--------------------------------------------------------------\n"+count+ ": "+qryFile.getName()+" Query: " + queryStr);
			//	System.out.println(getQueryStats(queryStr));
			getUsedSPARQLClauses(queryStr);
			count++;
		}
		//		System.out.println("\n\n--------------% triple pattern bound tuples distribution over all queries--------------------------------");
		//		System.out.println("Bound subject:" + (100*(double)boundSbjCount/grandTotalTriplePatterns));
		//		System.out.println("Bound Predicate:" + (100*(double)boundPredCount/grandTotalTriplePatterns));
		//		System.out.println("Bound Object URI:" + (100*(double)boundObjLiteralCount/grandTotalTriplePatterns));
		//		System.out.println("Bound Object Literal:" + (100*(double)boundObjLiteralCount/grandTotalTriplePatterns));

	}

	/**
	 * return query statistics as string
	 * @param query SPARQL query
	 * @param endpoint endpoint url
	 * @param graph Default Graph can be null
	 * @param datasetSize Dataset size
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 */
	public static String getQueryStats(String query, String endpoint, String graph, long datasetSize) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		String stats =  getDirectQueryRelatedStats(query); // Query type, total triple patterns, join vertices, mean join vertices degree
		//System.out.println(stats);
		stats = stats +"Mean triple patterns selectivity: " +Selectivity.getMeanTriplePatternSelectivity(query,endpoint,graph,datasetSize)+"\n";
		//System.out.println(stats);
		stats = stats+ getUsedSPARQLClauses(query);
		//System.out.println(stats);
		return stats; 
	}
	/**
	 * return query statistics as string for RDFization
	 * @param query SPARQL query
	 * @param endpoint endpoint url
	 * @param graph Default Graph can be null
	 * @param datasetSize Dataset size
	 * @param baseURI 
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 */
	public static String getRDFizedQueryStats(Query query, String endpoint, String graph, long datasetSize, String baseURI) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		String stats =  getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
		//System.out.println(stats);
		stats = stats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),endpoint,graph,datasetSize)  +" . \n ";
		//	stats = stats + " lsqv:meanTriplePatternSelectivity  0 . \n ";

		QueryStatsTest qst = new QueryStatsTest();
		QueryStats queryStats = 	qst.getQueryStatsOjb(query);
		if(!(queryStats==null))
		{
			String features = rdfizeUsedSPARQLClauses(queryStats,query);
			if(!features.equals("")){
			stats = stats +"lsqrd:q"+(LogRDFizer.queryNo-1)+" lsqv:usesFeature ";
			stats = stats+ features;}
		}
		//System.out.println(stats);
		return stats; 
	}
	public static String rdfizeUsedSPARQLClauses(QueryStats queryStats, Query query) {
		String stats = "";
		if(queryStats.containsUnion()==true)
			stats = stats + " lsqv:Union , ";
		if(queryStats.containsFilter()==true)
			stats = stats + " lsqv:Filter , ";
		if(queryStats.containsOptional()==true)
			stats = stats + " lsqv:Optional , ";
		if(queryStats.containsSubQuery()==true)
			stats = stats + " lsqv:SubQuery , ";
		if(queryStats.containsBind()==true)
			stats = stats + " lsqv:Bind , ";
		if(queryStats.containsServie()==true)
			stats = stats + " lsqv:Service , ";
		if(queryStats.containsMinus()==true)
			stats = stats + " lsqv:Minus , ";
		if(queryStats.containsExists()==true)
			stats = stats + " lsqv:Exists , ";
		if(queryStats.containsNotExists()==true)
			stats = stats + " lsqv:NotExists , ";
		if(queryStats.containsNamedGraph()==true)
			stats = stats + " lsqv:namedGraph , ";
		//if(queryStats.containsPropertyPaths()==true)
		//	stats = stats + " lsqv:proptertyPaths , ";
		if(queryStats.containsRegex()==true)
			stats = stats + " lsqv:Regex , ";
		if(query.isDistinct()==true)
			stats = stats + " lsqv:Distinct , ";
		if(query.hasGroupBy()==true)
			stats = stats + " lsqv:GroupBy , ";
		if(query.hasLimit()==true)
			stats = stats + " lsqv:Limit , ";
		if(query.hasHaving()==true)
			stats = stats + " lsqv:Having , ";
		if(query.hasOffset()==true)
			stats = stats + " lsqv:Offset , ";
		if(query.hasOrderBy()==true)
			stats = stats + " lsqv:OrderBy , ";
		if(query.hasValues()==true)
			stats = stats + " lsqv:Values , ";
		@SuppressWarnings("unused")
		boolean dsd ;
		if(( dsd = query.getDatasetDescription()!=null))
		{
			if(!query.getDatasetDescription().getNamedGraphURIs().isEmpty())
				stats = stats + " lsqv:FromNamed , ";
			if(query.getDatasetDescription().getNamedGraphURIs().isEmpty())
				stats = stats + " lsqv:From , ";
		} 
		boolean min = false, max =false, count = false, sum = false, avg = false;
		Iterator<ExprAggregator> itr = query.getAggregators().iterator();

		while(itr.hasNext())
		{
			String aggregator = itr.next().asSparqlExpr().toString();
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
		if(min==true)
			stats = stats + " lsqv:Min , ";
		if(max==true)
			stats = stats + " lsqv:Max , ";
		if(sum==true)
			stats = stats + " lsqv:Sum , ";
		if(avg==true)
			stats = stats + " lsqv:Avg , ";
		if(count==true)
			stats = stats + " lsqv:Count , ";
		if(!stats.equals(""))
		{
		stats = stats.substring(0,stats.lastIndexOf(",")-1);
		stats = stats + " . ";
		}
		return stats;
	}

	/**
	 * Get the important set of SPARQL clauses used in the query
	 * @param queryStr SPARQL query
	 * @return stats SPARQL clauses as string line by line
	 * @throws MalformedQueryException
	 */
	public static String getUsedSPARQLClauses( String queryStr) throws MalformedQueryException {
		Query query =QueryFactory.create(queryStr);
		QueryStatsTest qst = new QueryStatsTest();
		QueryStats queryStats = 	qst.getQueryStatsOjb(query);
		String stats = "";
		if(!(queryStats==null))
		{
			if(queryStats.containsUnion()==false)
				stats = stats +"UNION: No \n" ;
			else
				stats = stats +"UNION: Yes \n" ;

			if(query.isDistinct()==false)
				stats = stats +"DISTINCT: No \n" ;
			else
				stats = stats +"DISTINCT: Yes \n" ;

			if(query.hasOrderBy()==false)
				stats = stats +"ORDER BY: No \n" ;
			else
				stats = stats +"ORDER BY: Yes \n" ;

			if(queryStats.containsRegex()==false)
				stats = stats +"REGEX: No \n" ;
			else
				stats = stats +"REGEX: Yes \n" ;

			if(query.hasLimit()==true)
				stats = stats +"LIMIT: Yes \n" ;
			else
				stats = stats +"LIMIT: No \n" ;	

			if(query.hasOffset()==true)
				stats = stats +"OFFSET: Yes \n" ;
			else
				stats = stats +"OFFSET: No \n" ;
			if(queryStats.containsOptional()==true)
				stats = stats +"OPTIONAL: Yes \n" ;
			else
				stats = stats +"OPTIONAL: No \n" ;

			if(queryStats.containsFilter()==true)
				stats = stats +"FILTER: Yes \n" ;
			else
				stats = stats +"FILTER: No \n" ;
			if(query.hasGroupBy()==true)
				stats = stats +"GROUP BY: Yes \n" ;
			else
				stats = stats +"GROUP BY: No \n" ;
		}
		else
		{
			stats = stats +"UNION: No \n" ;
			stats = stats +"DISTINCT: No \n" ;
			stats = stats +"ORDER BY: No \n" ;
			stats = stats +"REGEX: No \n" ;
			stats = stats +"LIMIT: No \n" ;	
			stats = stats +"OFFSET: No \n" ;
			stats = stats +"OPTIONAL: No \n" ;
			stats = stats +"FILTER: No \n" ;
			stats = stats +"GROUP BY: No \n" ;
		}

		return stats;
	}
	/**
	 * Get the benchmark query features ( e.g resultsize, bgps mean join vertices etc)
	 * @param query SPARQL query
	 * @return stats Query Features as string
	 * @throws MalformedQueryException
	 */
	public static String getDirectQueryRelatedRDFizedStats(String query) throws MalformedQueryException {
		String stats = "";
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		//System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
		//stats = stats +"Basic Graph Patterns (BGPs): " +bgpGrps.size()+"\n";
		stats = stats + " lsqv:bgps "+bgpGrps.size()  +" ; ";
		long totalTriplePatterns = 0;
		HashSet<Vertex> joinVertices = new HashSet<Vertex>();
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
		{
			HashSet<Vertex> V = new HashSet<Vertex>();   //--Set of all vertices used in our hypergraph. each subject, predicate and object of a triple pattern is one node until it is repeated
			List<StatementPattern>	 stmts =  bgpGrps.get(DNFkey);
			totalTriplePatterns = totalTriplePatterns + stmts.size();
			for (StatementPattern stmt : stmts) 
			{		
				String sbjVertexLabel, objVertexLabel, predVertexLabel;
				Vertex sbjVertex, predVertex,objVertex ;
				//--------add vertices---
				sbjVertexLabel = getSubjectVertexLabel(stmt);
				predVertexLabel = getPredicateVertexLabel(stmt);
				objVertexLabel = getObjectVertexLabel(stmt);
				sbjVertex = new Vertex(sbjVertexLabel);
				predVertex = new Vertex(predVertexLabel);
				objVertex = new Vertex(objVertexLabel);
				if(!vertexExist(sbjVertex,V))
					V.add(sbjVertex);
				if(!vertexExist(predVertex,V))
					V.add(predVertex);
				if(!vertexExist(objVertex,V))
					V.add(objVertex);
				//--------add hyperedges
				HyperEdge hEdge = new HyperEdge(sbjVertex,predVertex,objVertex);
				if(!(getVertex(sbjVertexLabel,V)==null))
					sbjVertex = getVertex(sbjVertexLabel,V);
				if(!(getVertex(predVertexLabel,V)==null))
					predVertex = getVertex(predVertexLabel,V);
				if(!(getVertex(objVertexLabel,V)==null))
					objVertex = getVertex(objVertexLabel,V);
				sbjVertex.outEdges.add(hEdge); predVertex.inEdges.add(hEdge); objVertex.inEdges.add(hEdge);
			}
			vertices.addAll(V) ;
			joinVertices.addAll(getJoinVertexCount(V));
			// V.clear();
		}
		grandTotalTriplePatterns = grandTotalTriplePatterns + totalTriplePatterns;
		//stats = stats +"Triple Patterns: "+ totalTriplePatterns+"\n";
		stats = stats + " lsqv:triplePatterns "+totalTriplePatterns  +" ; ";
		//System.out.println("Triple Patterns: " +totalTriplePatterns);
		//System.out.println("Total Vertices:"+vertices.size() + " ==> "+vertices);
		//System.out.println("Join Vertices: " +joinVertices.size()+" ==> "+joinVertices);
		//stats = stats+"Join Vertices: " + joinVertices.size()+"\n";
		stats = stats + " lsqv:joinVertices "+joinVertices.size()  +" ; ";
		//System.out.println("Join Vertices to Total Vertices ratio: " +(double)joinVertices.size()/(double)vertices.size());
		double meanJoinVertexDegree = 0;
		//	String joinVertexType = "" ;   // {Star, path, hybrid, sink}
		for(Vertex jv:joinVertices)
		{
			long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
			meanJoinVertexDegree = meanJoinVertexDegree + joinVertexDegree;
		}
		if(joinVertices.size()==0)
			stats = stats + " lsqv:meanJoinVerticesDegree 0 ; ";
		//stats = stats +"Mean Join Vertices Degree: 0 \n";
		else
			stats = stats + " lsqv:meanJoinVerticesDegree "+(meanJoinVertexDegree/joinVertices.size())  +" ; ";
		//stats = stats +"Mean Join Vertices Degree: "+ +(meanJoinVertexDegree/joinVertices.size())+"\n";
		return stats;	
	}

	/**
	 * Get the benchmark query features ( e.g resultsize, bgps mean join vertices etc)
	 * @param query SPARQL query
	 * @return stats Query Features as string
	 * @throws MalformedQueryException
	 */
	public static String getDirectQueryRelatedStats(String query) throws MalformedQueryException {
		String stats = "";
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		//System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
		stats = stats +"Basic Graph Patterns (BGPs): " +bgpGrps.size()+"\n";
		long totalTriplePatterns = 0;
		HashSet<Vertex> joinVertices = new HashSet<Vertex>();
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		
		for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
		{
			HashSet<Vertex> V = new HashSet<Vertex>();   //--Set of all vertices used in our hypergraph. each subject, predicate and object of a triple pattern is one node until it is repeated
			List<StatementPattern>	 stmts =  bgpGrps.get(DNFkey);
			totalTriplePatterns = totalTriplePatterns + stmts.size();
			for (StatementPattern stmt : stmts) 
			{		
				String sbjVertexLabel, objVertexLabel = null, predVertexLabel;
				Vertex sbjVertex, predVertex,objVertex ;
				//--------add vertices---
				sbjVertexLabel = getSubjectVertexLabel(stmt);
				predVertexLabel = getPredicateVertexLabel(stmt);
				objVertexLabel = getObjectVertexLabel(stmt);
				sbjVertex = new Vertex(sbjVertexLabel);
				predVertex = new Vertex(predVertexLabel);
				objVertex = new Vertex(objVertexLabel);
				if(!vertexExist(sbjVertex,V))
					V.add(sbjVertex);
				if(!vertexExist(predVertex,V))
					V.add(predVertex);
				if(!vertexExist(objVertex,V))
					V.add(objVertex);
				//--------add hyperedges
				HyperEdge hEdge = new HyperEdge(sbjVertex,predVertex,objVertex);
				if(!(getVertex(sbjVertexLabel,V)==null))
					sbjVertex = getVertex(sbjVertexLabel,V);
				if(!(getVertex(predVertexLabel,V)==null))
					predVertex = getVertex(predVertexLabel,V);
				if(!(getVertex(objVertexLabel,V)==null))
					objVertex = getVertex(objVertexLabel,V);
				sbjVertex.outEdges.add(hEdge); predVertex.inEdges.add(hEdge); objVertex.inEdges.add(hEdge);
			}
			vertices.addAll(V) ;
			joinVertices.addAll(getJoinVertexCount(V));
			// V.clear();
		}
		grandTotalTriplePatterns = grandTotalTriplePatterns + totalTriplePatterns;
		stats = stats +"Triple Patterns: "+ totalTriplePatterns+"\n";
		//System.out.println("Triple Patterns: " +totalTriplePatterns);
		//System.out.println("Total Vertices:"+vertices.size() + " ==> "+vertices);
		//System.out.println("Join Vertices: " +joinVertices.size()+" ==> "+joinVertices);
		stats = stats+"Join Vertices: " + joinVertices.size()+"\n";
		//System.out.println("Join Vertices to Total Vertices ratio: " +(double)joinVertices.size()/(double)vertices.size());
		double meanJoinVertexDegree = 0;
		//	String joinVertexType = "" ;   // {Star, path, hybrid, sink}
		for(Vertex jv:joinVertices)
		{
			long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
			meanJoinVertexDegree = meanJoinVertexDegree + joinVertexDegree;
		}
		if(joinVertices.size()==0)
			stats = stats +"Mean Join Vertices Degree: 0 \n";
		else
			stats = stats +"Mean Join Vertices Degree: "+ +(meanJoinVertexDegree/joinVertices.size())+"\n";
		return stats;	
	}

	/**
	 * Get the the list of join vertices
	 * @param Vertices List of vertices in BGP
	 * @return V Collection of join vertices
	 */
	public static HashSet<Vertex> getJoinVertexCount(HashSet<Vertex> Vertices) {
		HashSet<Vertex> V = new HashSet<Vertex>();
		for (Vertex vertex:Vertices)
		{
			long inDeg = vertex.inEdges.size();
			long outDeg = vertex.outEdges.size();
			long degSum = inDeg + outDeg;
			if(degSum>1)
				V.add(vertex);
		}
		return V;
	}
	/**
	 * Get label for the object vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getObjectVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getObjectVar().getValue()!=null)
		{
			label = stmt.getObjectVar().getValue().stringValue();
			if(label.startsWith("http://") || label.startsWith("ftp://"))
				boundObjURICount++;
			else
				BoundObjLiteralCount++;
		}
		else
			label ="?"+stmt.getObjectVar().getName(); 
		return label;

	}
	/**
	 * Get label for the predicate vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getPredicateVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getPredicateVar().getValue()!=null)
		{
			label = stmt.getPredicateVar().getValue().stringValue();
			boundPredCount++;
		}
		else
			label ="?"+stmt.getPredicateVar().getName(); 
		return label;

	}
	/**
	 * Get label for the subject vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getSubjectVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getSubjectVar().getValue()!=null)
		{
			label = stmt.getSubjectVar().getValue().stringValue();
			boundSbjCount++;
		}
		else
			label ="?"+stmt.getSubjectVar().getName(); 
		return label;

	}

	/**
	 * Check if a  vertex already exists in set of all vertices
	 * @param sbjVertex
	 * @param V Set of all vertices
	 * @return 
	 * @return value Boolean value
	 */
	public static  boolean vertexExist(Vertex sbjVertex, HashSet<Vertex> V) {
		for(Vertex v:V)
		{
			if(sbjVertex.label.equals(v.label))
				return true;
		}
		return false;
	}
	/**
	 * Retrieve a vertex having a specific label from a set of Vertrices
	 * @param label Label of vertex to be retrieved
	 * @param V Set of vertices
	 * @return Vertex if exist otherwise null
	 */
	public static Vertex getVertex(String label, HashSet<Vertex> V) {
		for(Vertex v:V)
		{
			if(v.label.equals(label))
				return v;
		}
		return null;
	}

	/**
	 * Print query statistics
	 * @param query SPARQL query
	 * @param queryName Query Name
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 */
	public static String rdfizeTuples_JoinVertices(String query) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		String stats = "";
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		long totalTriplePatterns = 0;
		HashSet<Vertex> joinVertices = new HashSet<Vertex>();
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		Set<String> predicates = new HashSet<String> ();
		Set<String> subjects = new HashSet<String> ();
		Set<String> objects = new HashSet<String> ();
		for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
		{
			HashSet<Vertex> V = new HashSet<Vertex>();   //--Set of all vertices used in our hypergraph. each subject, predicate and object of a triple pattern is one node until it is repeated
			List<StatementPattern>   stmts =  bgpGrps.get(DNFkey);
			totalTriplePatterns = totalTriplePatterns + stmts.size();
			for (StatementPattern stmt : stmts) 
			{               
				String sbjVertexLabel, objVertexLabel, predVertexLabel;
				Vertex sbjVertex, predVertex,objVertex ;
				//--------add vertices---
				sbjVertexLabel = getSubjectVertexLabel(stmt);
				subjects.add(sbjVertexLabel);
				predVertexLabel = getPredicateVertexLabel(stmt);
				predicates.add(predVertexLabel);
				objVertexLabel = getObjectVertexLabel(stmt);
				objects.add(objVertexLabel);
				sbjVertex = new Vertex(sbjVertexLabel);
				predVertex = new Vertex(predVertexLabel);
				objVertex = new Vertex(objVertexLabel);
				if(!vertexExist(sbjVertex,V))
					V.add(sbjVertex);
				if(!vertexExist(predVertex,V))
					V.add(predVertex);
				if(!vertexExist(objVertex,V))
					V.add(objVertex);
				//--------add hyperedges
				HyperEdge hEdge = new HyperEdge(sbjVertex,predVertex,objVertex);
				if(!(getVertex(sbjVertexLabel,V)==null))
					sbjVertex = getVertex(sbjVertexLabel,V);
				if(!(getVertex(predVertexLabel,V)==null))
					predVertex = getVertex(predVertexLabel,V);
				if(!(getVertex(objVertexLabel,V)==null))
					objVertex = getVertex(objVertexLabel,V);
				sbjVertex.outEdges.add(hEdge); predVertex.inEdges.add(hEdge); objVertex.inEdges.add(hEdge);
			}
			vertices.addAll(V) ;
			joinVertices.addAll(getJoinVertexCount(V));
			// V.clear();
		}
       if(!subjects.isEmpty())
       {
    	   	stats = stats + "\nlsqrd:q"+(LogRDFizer.queryNo-1)+" lsqv:mentionsSubject ";
    	   stats = stats + getMentionsTuple(subjects);
       }
       if(!predicates.isEmpty())
       {
    	   	stats = stats+ "\nlsqrd:q"+(LogRDFizer.queryNo-1)+" lsqv:mentionsPredicate ";
    	   stats = stats + getMentionsTuple(predicates);
       }
       
       if(!objects.isEmpty())
       {
    	   	stats = stats+ "\nlsqrd:q"+(LogRDFizer.queryNo-1)+" lsqv:mentionsObject ";
    	   stats = stats + getMentionsTuple(objects);
       }
		
       String joinVertexType = "" ;   // {Star, path, hybrid, sink}
		if(!joinVertices.isEmpty()){
			
		for(Vertex jv:joinVertices)
		{
			String joinVertex = jv.label;
			 if(joinVertex.startsWith("http://") || joinVertex.startsWith("ftp://"))
				 joinVertex =  "lsqrd:q"+(LogRDFizer.queryNo-1)+"-"+joinVertex;
				 else{
					 joinVertex =  "lsqrd:q"+(LogRDFizer.queryNo-1)+"-"+joinVertex;
					 joinVertex = joinVertex.replace("?", "");
				 }
			 
			stats = stats + "\nlsqrd:q"+(LogRDFizer.queryNo-1)+" lsqv:joinVertex " + joinVertex + " . \n";
			long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
			joinVertexType =  getJoinVertexType(jv);
			stats = stats + joinVertex + " lsqv:joinVertexDegree " + joinVertexDegree + " ; lsqv:joinVertexType lsqv:" + joinVertexType + " . " ;
					
			//System.out.println("     " + jv+ " Join Vertex Degree: " + joinVertexDegree + ", Join Vertex Type: " + joinVertexType);
		}
		}
		return stats ;
	}
	public static String getMentionsTuple(Set<String> subjects) {
	 String	stats = "";
	 for (String sbj:subjects)
	 {
		 if(sbj.startsWith("http://") || sbj.startsWith("ftp://"))
		 stats = stats + "<"+sbj+"> , ";
		 else
			 stats = stats + "\""+sbj+"\" , ";
		 
	 }
	 stats = stats.substring(0,stats.lastIndexOf(",")-1);
		stats = stats + " . ";
		return stats;
	}

	/**
	 * Get join vertex type
	 * @param jv Join Vertex
	 * @return Type
	 */
	public static String getJoinVertexType(Vertex jv) {
		String joinVertexType = "";
		if(jv.inEdges.size()==0)
			joinVertexType = "Star" ; 
		else if (jv.outEdges.size()==0)
			joinVertexType = "Sink" ; 
		else if (jv.inEdges.size()==1 &&jv.outEdges.size()==1 )
			joinVertexType = "Path" ; 
		else
			joinVertexType = "Hybrid" ; 
		return joinVertexType;

	}


}