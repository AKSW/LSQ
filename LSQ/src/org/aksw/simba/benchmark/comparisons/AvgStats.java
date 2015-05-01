package org.aksw.simba.benchmark.comparisons;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.Similarity;
import org.aksw.simba.benchmark.log.operations.CleanQueryReader;
import org.aksw.simba.benchmark.log.operations.SesameLogReader;
import org.aksw.simba.benchmark.query.stats.QueryStats;
import org.aksw.simba.benchmark.query.stats.QueryStatsTest;
import org.aksw.simba.hibiscus.hypergraph.HyperGraph.HyperEdge;
import org.aksw.simba.hibiscus.hypergraph.HyperGraph.Vertex;
import org.aksw.simba.largerdfbench.util.Selectivity;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

/**
 * Get  min, max, avg, s.d of the benchmark for different features like triple patterns count, bgps, selectivity etc
 * @author Saleem
 *
 */
public class AvgStats {

	public static final int boundObjLiteralCount = 0;
	public static long boundSbjCount, boundPredCount, boundObjURICount, BoundObjLiteralCount,grandTotalTriplePatterns = 0 ; 
	public static Map<Integer,String> featureDimensions = new HashMap<Integer,String>();
	public static long endpointSize1 ;
	@SuppressWarnings("unused")
	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {

		//String inputDir= "../BigRDFBench-Utilities/queries/";  //dont forget last /
		String endpoint = "http://localhost:8891/sparql";
		String graph = null;
		//String graph = "http://aksw.org/feasible"; //can be null
		//String dbpediaBenchmarkQueriesFile = "../FEASIBLE/queries/DBpediaBenchmarkQueries.txt";
		//String watdivQueriesDir = "/home/akswadmin/saleem/feasible/code/feasible-master/queries/";
	//	String sp2benchDir = "/home/akswadmin/saleem/feasible/code/feasible-master/queries/sp2bench/";
		//String bsbmRunFile = "/home/akswadmin/saleem/feasible/code/feasible-master/queries/bsbm/run.log";
		//String lubmDir = "../FEASIBLE/queries/lubm/";
		String sp2benchDir = "C:/Users/Saleem/Downloads/sp2b/queries/";
		//Set<String> queries = getbsbmueries(bsbmRunFile);
		 Set <String> queries = getQueriesFromDirectory(sp2benchDir);
		//Set<String> queries = getDBPSBQueries(dbpediaBenchmarkQueriesFile);
		//Set<String> queries = getLineByLineQueries(watdivQueriesDir);
		//System.out.println(queries.size());
		Map<String,Double> percentClauses = getPercentUsedConstructs(queries);
		Map<String,Double> avgLogFeatures = getAvgLogFeatures(queries,endpoint,graph);

		//-------------------Get percentages, min, max, mean, S.D of SPARQL Clauses, query features using clean query files
		String queryFileWithStats = "DBpedia3.5.1-CleanQueries-fixed.txt";
		//String queryFileWithStats = "LinkedSWDF-fixed.txt";
		// String queryFileWithStats = "SWDF-CleanQueries.txt";
		// String queryFileWithStats = "DBpediaBenchmark-CleanQueries.txt";
	//	Map<String,Double> percentLogConstruct = getPercentUsedLogConstructs(queryFileWithStats);
	//	Map<String,Double> avgLogFeatures = getAvgLogFeatures(queryFileWithStats);
		//printPercentClauses(percentClauses);
	}


	public static Set<String> getbsbmueries(String bsbmRunFile) throws IOException {
		Set <String> queries = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(bsbmRunFile));
			String line = "";
			String query ="";
			while ((line = br.readLine()) != null)
			{				
				query="";
				while (!(line = br.readLine()).startsWith("__"))
					query = query + "\n"+line;
				
				String[] prts = query.split("Query string:");
				String [] newPrts1  = prts[1].split("Query results");
				String [] newPrts = newPrts1[0].split("Query");
				//System.out.println(newPrts[0]);
				queries.add(newPrts[0]);
				if(queries.size()==4261)
					break;
             }
			br.close();
		
		return queries;
		
	}


	public static Set<String> getLineByLineQueries(String inputDir) throws IOException {	
		Set <String> queries = new HashSet<String>();
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();
		for (File qryFile : listOfFiles)
		{	
			BufferedReader br = new BufferedReader(new FileReader(inputDir+qryFile.getName()));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				
				queries.add(line);
               
			}
			br.close();
		}

		return queries;
	}


	public static Map<String, Double> getAvgLogFeatures(Set<String> queries,String endpoint, String graph) throws MalformedQueryException, RepositoryException, QueryEvaluationException, IOException {
		loadFeatureDimensions();
		endpointSize1 =Selectivity.getEndpointTotalTriples(endpoint, graph);
		Map<String, Double[]> vectors  = getFeaturesStatsVectors(queries,endpoint,graph);
		List<Double[]> dimLogVectors = getDimensionWiseVectors(vectors);
		Double[] minLogVector = getMinVector(dimLogVectors);
		Double[] maxLogVector = getMaxVector(dimLogVectors);
		Double[] meanLogVector = getMeanVector(dimLogVectors); 
		Double[] sdLogVector = getStandardDeviationVector(dimLogVectors);
		for(int dim=0;dim<meanLogVector.length;dim++)
		{
			String dimension = featureDimensions.get(dim);
			System.out.println(dimension+"\tMin\t"+minLogVector[dim] );
			System.out.println("                               \tMax\t"+maxLogVector[dim] );
			System.out.println("                               \tMean\t"+meanLogVector[dim] );
			System.out.println("                               \tS.D.\t"+sdLogVector[dim] );
		}

		return null;
	}


	public static Map<String, Double> getPercentUsedLogConstructs(String queryFileWithStats) throws IOException 
	{
		//System.out.println("Process started...");
		Map<String,Double> percentClauses = new HashMap<String,Double>();
		double select=0, construct=0, ask=0, describe=0, union = 0 , distinct =0, orderby = 0, regex = 0, limit = 0, offset = 0, optional =0, filter = 0, groupby = 0;
		BufferedReader br = new BufferedReader(new FileReader(new File(queryFileWithStats)));
		String queryStr;
		int size = 0 ;
		String line= br.readLine();
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			if(line.startsWith("Query String:"))
			{
				size++;
				String prts [] = line.split("Query String:");
				queryStr = java.net.URLDecoder.decode(prts[1].trim(), "UTF-8");  //query string)
				try{
					Query query = QueryFactory.create(queryStr,Syntax.syntaxARQ);
					QueryStatsTest qst = new QueryStatsTest(); 
					QueryStats queryStats = 	qst.getQueryStatsOjb(query);
					if(query.isAskType())
						ask++;
					else if (query.isConstructType())
						construct++;
					else if (query.isDescribeType())
						describe++;
					else if (query.isSelectType())
						select++;
					if(queryStats.containsUnion())
						union++;
					if(query.isDistinct())
						distinct++;
					if(query.hasOrderBy())
						orderby++;
					if(queryStats.containsRegex())
						regex++;
					if(query.hasLimit())
						limit++;
					if(query.hasOffset())
						offset++;
					if(queryStats.containsOptional())
						optional++;
					if(queryStats.containsFilter())
						filter++;
					if(query.hasGroupBy())
						groupby++;

				}
				catch(Exception ex) {}
			}

		}
		br.close();

		percentClauses.put("Queries", (double) size);
		System.out.println("Queris\t"+ (double) size);

		percentClauses.put("Select", (double) ((select/size)*100));
		System.out.println("Select\t"+ (double) ((select/size)*100)+ "%");

		percentClauses.put("Ask", (double) ((ask/size)*100));
		System.out.println("Ask\t"+ (double) ((ask/size)*100)+ "%");

		percentClauses.put("Construct", (double) ((construct/size)*100));
		System.out.println("Construct\t"+ (double) ((construct/size)*100)+ "%");

		percentClauses.put("Describe", (double) ((describe/size)*100));
		System.out.println("Describe\t"+ (double) ((describe/size)*100)+ "%");

		percentClauses.put("Union", (double) ((union/size)*100));
		System.out.println("Union\t"+ (double) ((union/size)*100)+ "%");

		percentClauses.put("Distinct", (double) ((distinct/size)*100));
		System.out.println("Distinct\t"+ (double) ((distinct/size)*100)+ "%");

		percentClauses.put("OrderBy", (double) ((orderby/size)*100));
		System.out.println("OrderBy\t"+ (double) ((orderby/size)*100)+ "%");

		percentClauses.put("Regex", (double) ((regex/size)*100));
		System.out.println("Regex\t"+ (double) ((regex/size)*100)+ "%");

		percentClauses.put("Limit", (double) ((limit/size)*100));
		System.out.println("Limit\t"+ (double) ((limit/size)*100)+ "%");

		percentClauses.put("Offset", (double) ((offset/size)*100));
		System.out.println("Offset\t"+ (double) ((offset/size)*100)+ "%");

		percentClauses.put("Optional", (double) ((optional/size)*100));
		System.out.println("Optional\t"+ (double) ((optional/size)*100)+ "%");

		percentClauses.put("Filter", (double) ((filter/size)*100));
		System.out.println("Filter\t"+ (double) ((filter/size)*100)+ "%");

		percentClauses.put("GroupBy", (double) ((groupby/size)*100));
		System.out.println("GroupBy\t"+ (double) ((groupby/size)*100)+ "%");
		return percentClauses;
	}


	public static Map<String, Double> getAvgLogFeatures(String queryFileWithStats) throws IOException {
		loadFeatureDimensions();
		Map<String, Double[]> vectors  = 	getCleanQueriesFeaturesVectors(queryFileWithStats);
		List<Double[]> dimLogVectors = getDimensionWiseVectors(vectors);
		Double[] minLogVector = getMinVector(dimLogVectors);
		Double[] maxLogVector = getMaxVector(dimLogVectors);
		Double[] meanLogVector = getMeanVector(dimLogVectors); 
		Double[] sdLogVector = getStandardDeviationVector(dimLogVectors);
		for(int dim=0;dim<meanLogVector.length;dim++)
		{
			String dimension = featureDimensions.get(dim);
			System.out.println(dimension+"\tMin\t"+minLogVector[dim] );
			System.out.println("                               \tMax\t"+maxLogVector[dim] );
			System.out.println("                               \tMean\t"+meanLogVector[dim] );
			System.out.println("                               \tS.D.\t"+sdLogVector[dim] );
		}
		return null; //map yet not implemented 
	}
	public static Double[] getMinVector(List<Double[]> dimLogVectors) {
		Double[] minVector = new Double[7];
		int dim = 0;
		for(Double[] vector:dimLogVectors)
		{
			List<Double> lstVector = Arrays.asList(vector);
			minVector[dim]= Collections.min(lstVector);
			dim++;
		}
		return minVector;
	}
	public static Double[] getMaxVector(List<Double[]> dimLogVectors) {
		Double[] maxVector = new Double[7];
		int dim = 0;
		for(Double[] vector:dimLogVectors)
		{
			List<Double> lstVector = Arrays.asList(vector);
			maxVector[dim]= Collections.max(lstVector);
			dim++;
		}
		return maxVector;
	}


	private static void loadFeatureDimensions() {
		featureDimensions.put(0, "ResultSize                     ");featureDimensions.put(1, "BGPs                           ");
		featureDimensions.put(2, "Triple Patterns                ");featureDimensions.put(3, "Join Vertices                  ");
		featureDimensions.put(4, "Mean Join Vertices Degree      ");featureDimensions.put(5, "Mean Triple Pattern Selectivity");
		featureDimensions.put(6, "Run Time                       ");		
	}


	/**
	 * Get vector of S.Ds
	 * @param vectors List of Vectors
	 * @return vector of S.Ds
	 */
	public static Double[] getStandardDeviationVector(List<Double[]> vectors) {
		Double[] sdVector = new Double[7];
		int dim = 0;
		for(Double[] vector:vectors)
		{
			sdVector[dim]= Similarity.getStandardDeviation(vector);
			dim++;
		}
		return sdVector;
	}
	/**
	 * Get vector of Means
	 * @param vectors List of Vectors
	 * @return vector of means
	 */
	public static Double[] getMeanVector(List<Double[]> vectors) {
		Double[] meanVector = new Double[7];
		int dim = 0;
		for(Double[] vector:vectors)
		{
			meanVector[dim]= Similarity.getMean(vector);
			dim++;
		}
		return meanVector;
	}

	/**
	 * Get the dimension-wise vectors of the the normalized  vectors
	 * @param normalizedVectors Map of query ids to corresponding normalized vectors
	 * @return List of dimension wise vectors
	 */
	public static List<Double[]> getDimensionWiseVectors(Map<String, Double[]> normalizedVectors	) {
		List<Double[]> dimVectors = new ArrayList<Double[]>();
		for(int dim =0; dim <7;dim++)
		{
			Double [] dimVector = new Double[normalizedVectors.size()];
			int index = 0 ;
			for(String  key: normalizedVectors.keySet())
			{
				dimVector[index] = normalizedVectors.get(key)[dim];
				index++;
			}
			dimVectors.add(dimVector);
		}
		return dimVectors;
	}

	private static Map<String, Double[]> getCleanQueriesFeaturesVectors(String queryFileWithStats) throws IOException
	{
		Map<String, Double[]> vectors = new HashMap<String, Double[]>();
		BufferedReader br = new BufferedReader(new FileReader(new File(queryFileWithStats)));
		//System.out.println("Feature vectors loading in progress...");
		String line= br.readLine();
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			Double [] vector = new Double[7];
			String queryNumberLine [] = line.split(":"); // query number
			String queryNumber = queryNumberLine[1];
			br.readLine(); //query type
			vector[0] = CleanQueryReader.getFeatureValue(br.readLine()); //ResutSize
			vector[1] = CleanQueryReader.getFeatureValue(br.readLine()); // bgps
			vector[2] = CleanQueryReader.getFeatureValue(br.readLine()); //triple pattern count
			vector[3] = CleanQueryReader.getFeatureValue(br.readLine()); // join vertices
			vector[4] = CleanQueryReader.getFeatureValue(br.readLine()); // mean join vertices degree
			vector[5] = CleanQueryReader.getFeatureValue(br.readLine()); // mean triple pattern selectivity
			br.readLine(); // union
			br.readLine(); // distinct
			br.readLine(); // order by
			br.readLine(); //regex
			br.readLine(); // limit
			br.readLine(); // offset
			br.readLine(); //optional
			br.readLine(); //fitler
			br.readLine(); //group by
			vector[6] = CleanQueryReader.getFeatureValue(br.readLine()); //Query execution time

			line = br.readLine();  //query string
			line = br.readLine();  //start of new query
			vectors.put(queryNumber, vector);
		}
		br.close();
		return vectors;
	}

	public static Set<String> getDBPSBQueries(String dbpediaBenchmarkQueriesFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(dbpediaBenchmarkQueriesFile)));
		String line = "";
		Set <String> queries = new HashSet<String>();
		while ((line = br.readLine()) != null)
		{
			String [] prts = line.split("\t");
			Query query = QueryFactory.create(prts[2]);
			query = SesameLogReader.removeNamedGraphs(query);
			queries.add(query.toString());

		}
		br.close();
		return queries;
	}


	public static void printPercentClauses(Map<String, Double> percentClauses) {
		for(String clause:percentClauses.keySet())
		{
			System.out.println(clause + ":\t"+ percentClauses.get(clause) + "%");
		}

	}


	public static Map<String, Double> getPercentUsedConstructs(Set<String> queries) {
		Map<String,Double> percentClauses = new HashMap<String,Double>();
		double select=0, construct=0, ask=0, describe=0, union = 0 , distinct =0, orderby = 0, regex = 0, limit = 0, offset = 0, optional =0, filter = 0, groupby = 0;
		for(String queryStr:queries)
		{  // System.out.println(queryStr);
			try{
				Query query = QueryFactory.create(queryStr,Syntax.syntaxARQ);
				QueryStatsTest qst = new QueryStatsTest(); 
				QueryStats queryStats = 	qst.getQueryStatsOjb(query);
				if(query.isAskType())
					ask++;
				else if (query.isConstructType())
					construct++;
				else if (query.isDescribeType())
					describe++;
				else if (query.isSelectType())
					select++;
				if(queryStats.containsUnion())
					union++;
				if(query.isDistinct())
					distinct++;
				if(query.hasOrderBy())
					orderby++;
				if(queryStats.containsRegex())
					regex++;
				if(query.hasLimit())
					limit++;
				if(query.hasOffset())
					offset++;
				if(queryStats.containsOptional())
					optional++;
				if(queryStats.containsFilter())
					filter++;
				if(query.hasGroupBy())
					groupby++;
			}
			catch(Exception ex) {System.err.print(queryStr+"\n"+ex.getMessage());}
		}
		int size = queries.size();
		percentClauses.put("Queries", (double) size);
		System.out.println("Queris\t"+ (double) size);

		percentClauses.put("Select", (double) ((select/size)*100));
		System.out.println("Select\t"+ (double) ((select/size)*100)+ "%");

		percentClauses.put("Ask", (double) ((ask/size)*100));
		System.out.println("Ask\t"+ (double) ((ask/size)*100)+ "%");

		percentClauses.put("Construct", (double) ((construct/size)*100));
		System.out.println("Construct\t"+ (double) ((construct/size)*100)+ "%");

		percentClauses.put("Describe", (double) ((describe/size)*100));
		System.out.println("Describe\t"+ (double) ((describe/size)*100)+ "%");

		percentClauses.put("Union", (double) ((union/size)*100));
		System.out.println("Union\t"+ (double) ((union/size)*100)+ "%");

		percentClauses.put("Distinct", (double) ((distinct/size)*100));
		System.out.println("Distinct\t"+ (double) ((distinct/size)*100)+ "%");

		percentClauses.put("OrderBy", (double) ((orderby/size)*100));
		System.out.println("OrderBy\t"+ (double) ((orderby/size)*100)+ "%");

		percentClauses.put("Regex", (double) ((regex/size)*100));
		System.out.println("Regex\t"+ (double) ((regex/size)*100)+ "%");

		percentClauses.put("Limit", (double) ((limit/size)*100));
		System.out.println("Limit\t"+ (double) ((limit/size)*100)+ "%");

		percentClauses.put("Offset", (double) ((offset/size)*100));
		System.out.println("Offset\t"+ (double) ((offset/size)*100)+ "%");

		percentClauses.put("Optional", (double) ((optional/size)*100));
		System.out.println("Optional\t"+ (double) ((optional/size)*100)+ "%");

		percentClauses.put("Filter", (double) ((filter/size)*100));
		System.out.println("Filter\t"+ (double) ((filter/size)*100)+ "%");

		percentClauses.put("GroupBy", (double) ((groupby/size)*100));
		System.out.println("GroupBy\t"+ (double) ((groupby/size)*100)+ "%");
		return percentClauses;
	}


	public static Set<String> getQueriesFromDirectory(String inputDir) throws IOException {
		Set <String> queries = new HashSet<String>();
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();
		for (File qryFile : listOfFiles)
		{	
			BufferedReader br = new BufferedReader(new FileReader(inputDir+qryFile.getName()));
			String line;
			String queryStr="";
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				queryStr= queryStr+"\n"+line;

			}
			queries.add(queryStr);
			br.close();

		}
		return queries;
	}

	public static Map<String, Double[]> getFeaturesStatsVectors(Set<String> queries, String endpoint, String graph) throws MalformedQueryException, RepositoryException, QueryEvaluationException, IOException {
		Map<String, Double[]> vectors = new HashMap<String, Double[]>();
		String type = "";
		int count =1;
		System.out.println("Total: "+queries.size());
		for(String query:queries)
		{
			System.out.println(count+": Started...");
			count++;
			Query jenaQuery = QueryFactory.create(query);
			if(jenaQuery.isSelectType())
				type ="select";
			else if (jenaQuery.isConstructType())
				type = "construct";
			else if (jenaQuery.isAskType())
				type = "ask";
			else if (jenaQuery.isDescribeType())
				type = "describe";
			vectors.put(query,getFeaturesStateVector(query,endpoint,graph,type));
		}
		return vectors;
	}

	/**
	 * Print query statistics and return them as an array as well
	 * @param query SPARQL query
	 * @param graph Named graph
	 * @param endpoint endpoint
	 * @param type 
	 * @return Stats vector
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 * @throws IOException 
	 */
	public static Double[] getFeaturesStateVector(String query, String endpoint, String graph, String type) throws MalformedQueryException, RepositoryException, QueryEvaluationException, IOException {
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		Double[] stats = new Double[7];  //we have total of 7
		//System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
		stats[1] = (double) bgpGrps.size();
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
		//System.out.println("Triple Patterns: " +totalTriplePatterns);
		stats[2] = (double) totalTriplePatterns;
		//System.out.println("Total Vertices:"+vertices.size() + " ==> "+vertices);
		//System.out.println("Join Vertices: " +joinVertices.size()+" ==> "+joinVertices);
		stats[3] = (double) joinVertices.size();
		//System.out.println("Join Vertices to Total Vertices ratio: " +(double)joinVertices.size()/(double)vertices.size());
		double meanJoinVertexDegree = 0;
		@SuppressWarnings("unused")
		String joinVertexType = "" ;   // {Star, path, hybrid, sink}
		for(Vertex jv:joinVertices)
		{
			long joinVertexDegree = (jv.inEdges.size() + jv.outEdges.size());
			if(jv.inEdges.size()==0)
				joinVertexType = "Star" ; 
			else if (jv.outEdges.size()==0)
				joinVertexType = "Sink" ; 
			else if (jv.inEdges.size()==1 &&jv.outEdges.size()==1 )
				joinVertexType = "path" ; 
			else
				joinVertexType = "Hybrid" ; 
			//System.out.println("     " + jv+ " Join Vertex Degree: " + joinVertexDegree + ", Join Vertex Type: " + joinVertexType);
			meanJoinVertexDegree = meanJoinVertexDegree + joinVertexDegree;
		}

		//System.out.println("Mean Join Vertices Degree: " +(meanJoinVertexDegree/joinVertices.size()));
		if(joinVertices.size()==0)
			stats[4] = 0d;
		else
		stats[4] = (double) (meanJoinVertexDegree/joinVertices.size());
		long startTime = System.currentTimeMillis();
		
		stats [0] = (double) Selectivity.getQueryResultSize(query, endpoint, type);
		if(stats[0]==-1 ||stats[0]==-2)   // -1 is runtime error and -2 is timeout
			stats[0] = 0d;
		stats[6] = (double) (System.currentTimeMillis()-startTime);
		stats[5] = Selectivity.getMeanTriplePatternSelectivity(query, endpoint, graph, endpointSize1);
		
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
			label =stmt.getObjectVar().getName(); 
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
			label =stmt.getPredicateVar().getName(); 
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
			label =stmt.getSubjectVar().getName(); 
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


}
