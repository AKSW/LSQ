package org.aksw.simba.benchmark.log.operations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.aksw.simba.trash.QueryStatistics;
import org.aksw.simba.trash.Selectivity;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
/**
 * Write clean queries with stats into file, later used for benchmark generation
 * @author Saleem
 *
 */
public class CleanQueryWriter {
	public static long count =0;
	public static long zeroCount =0;
	public static long errorCount = 0;
	public static long  timeOutCount = 0;
	public static BufferedWriter	bw;
	public static long datasetSize = 0;
	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		String endpoint = "http://localhost:8890/sparql";
		String graph = "http://aksw.org/feasible"; //can be null
		Selectivity.maxRunTime= -1;
		writeCleanQueriesFromQueryFiles("dbpedia-unique-queries.txt", endpoint, graph, "DBpedia3.5.1-CleanQueries-ask.txt");

	}
	/**
	 * This will read queries (one per line) from the file and write into another file with stats
	 * @param inputFile Input query file
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph. Can be null
	 * @param outputQueryFile 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public static void writeCleanQueriesFromQueryFiles(String inputFile, String endpoint, String graph, String outputQueryFile) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		datasetSize = Selectivity.getEndpointTotalTriples(endpoint, graph);
		bw= new BufferedWriter(new FileWriter(outputQueryFile));
		Selectivity.toBW= new BufferedWriter(new FileWriter("timeoutQueries.txt"));
		BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
		String line = "";
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			String prts [] = line.split("\t");
			String query = decodeQuery(prts[1]);
			String type = prts [0];
			if(type.equals("select"))
				writeSelectQueries(query,endpoint,graph);
			else if (type.equals("construct"))
				writeConstructQueries(query,endpoint,graph);
			if (type.equals("ask"))
				writeAskQueries(query,endpoint,graph);
			else if (type.equals("describe"))
				writeDescribeQueries(query,endpoint,graph);

		}
		bw.write("#--end---");
		bw.close();
		br.close();
		Selectivity.toBW.close();
		System.out.println("Query with stats successfully writen to "+outputQueryFile+"\nTotal queries with runtime error: " + errorCount);
		System.out.println("Total queries with zero results: "+zeroCount+"\nNote: Zero result queries are not considered");
		System.out.println("Total Queries with timeOuts: " + timeOutCount +"\nTime out queries are written to timeOutQueries.txt");
	}
	/**
	 * First We need to store all the distinct queries into a file then read one by one for attaching stats. 
	 * Note we do this in order to save the memory and the query stats calculation becomes faster. 
	 * @param queriesMap Set of queries in Map
	 * @throws IOException 
	 */
	private static void writeCleanQueries(HashMap<String, Set<String>> queriesMap) throws IOException
	{
		bw= new BufferedWriter(new FileWriter("dummyQueries.txt"));
		for(String type:queriesMap.keySet())
		{
			Set<String> queries = queriesMap.get(type);
			for(String query:queries)
				bw.write(type+"\t"+encodeQuery(query));
		}
		bw.write("#--end---");
		bw.close();
		queriesMap.clear();
	}
	/**
	 * Encode query 
	 * @param query query string
	 * @return encoded query
	 */
	public static String encodeQuery(String query) {
		try{
			query  = java.net.URLEncoder.encode(query, "UTF-8");
		}
		catch (Exception e) {//System.err.println(query+ " "+ e.getMessage());
		}

		return query+"\n";
	}
	/**
	 * Write clean queries with stats into file
	 * @param queries Set of queries
	 * @param endpoint the SPARQL endpoint to be used for calculating the results size and selectivities of queries
	 * @param graph  Default named graph can be null as well where complete SPARQL endpoint will be queried
	 * @param outputQueryFile Output file to store the clean queries with stats
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException
	 */
	public static void writeCleanQueriesWithStats ( HashMap<String, Set<String>> queries, String endpoint, String graph, String outputQueryFile) throws QueryEvaluationException, RepositoryException, MalformedQueryException, IOException
	{		
		System.out.println("Clean queries writting started ...");
		writeCleanQueries(queries);
		queries.clear();
		datasetSize = Selectivity.getEndpointTotalTriples(endpoint, graph);
		bw= new BufferedWriter(new FileWriter(outputQueryFile));
		Selectivity.toBW= new BufferedWriter(new FileWriter("timeoutQueries.txt"));
		BufferedReader br = new BufferedReader(new FileReader(new File("dummyQueries.txt")));
		String line = "";
		//br.readLine();
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			String prts [] = line.split("\t");
			String query = decodeQuery(prts[1]);
			String type = prts [0];
			if(type.equals("select"))
				writeSelectQueries(query,endpoint,graph);
			else if (type.equals("construct"))
				writeConstructQueries(query,endpoint,graph);
			else if (type.equals("ask"))
				writeAskQueries(query,endpoint,graph);
			else if (type.equals("describe"))
				writeDescribeQueries(query,endpoint,graph);
		}
		bw.write("#--end---");
		bw.close();
		br.close();
		Selectivity.toBW.close();
		System.out.println("Query with stats successfully writen to "+outputQueryFile+"\nTotal queries with runtime error: " + errorCount);
		System.out.println("Total queries with zero results: "+zeroCount+"\nNote: Zero result queries are not considered");
		System.out.println("Total Queries with timeOuts: " + timeOutCount +"\nTime out queries are written to timeOutQueries.txt");
		File file = new File("dummyQueries.txt");
		file.delete();
	}
	/**
	 * Decode query 
	 * @param query query string
	 * @return decoded query
	 */
	public static String decodeQuery(String query) {
		try{
			query  = java.net.URLDecoder.decode(query, "UTF-8");
		}
		catch (Exception e) {//System.err.println(query+ " "+ e.getMessage());
		}

		return query+"\n";
	}
	/**
	 * Wrtie DESCRIBE queries in to file
	 * @param query SPARQL DESCRIBE query
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph, cann be null
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static void writeDescribeQueries(String query,
			String endpoint, String graph) throws RepositoryException, MalformedQueryException, IOException {
		long resultSize = -1;
		long curTime = System.currentTimeMillis();
		resultSize = Selectivity.getQueryResultSize(query, endpoint,"describe");
		long exeTime = System.currentTimeMillis() - curTime ;
		//System.out.println(resultSize);
		if (resultSize>0)
		{

			try {
				String queryStats = "#-------------------------------------------------------\nQuery No: "+count+"\n";  //new query identifier
				queryStats = queryStats + "Query Type: DESCRIBE \n";
				queryStats = queryStats + "Results Size: "+resultSize  +"\n";
				queryStats = queryStats+QueryStatistics.getQueryStats(query,endpoint,graph,datasetSize);
				queryStats = queryStats+"Query Execution Time (ms): "+ exeTime +"\n";
				queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(query, "UTF-8")+"\n";
				//System.out.println(queryStats);
				bw.write(queryStats);
				System.out.println(count+ ": written...");
				count++;
			} catch (Exception e) {	errorCount++; }  
		}
		else if (resultSize==0)		
			zeroCount++;
		else if (resultSize==-1)	
			errorCount++;
		else if (resultSize==-2)	
			timeOutCount++;	
	}
	/**
	 * Write SPARQL ASK queries into file
	 * @param query SPARQL ASK query
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph, can be null as well
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public static void writeAskQueries(String query,
			String endpoint, String graph) throws MalformedQueryException, RepositoryException, QueryEvaluationException, IOException {
		long resultSize = -1;
		long curTime = System.currentTimeMillis();
		resultSize = Selectivity.getQueryResultSize(query, endpoint,"ask");
		long exeTime = System.currentTimeMillis() - curTime ;
		if (resultSize==1)
		{
			try {

				String queryStats = "#-------------------------------------------------------\nQuery No: "+count+"\n";  //new query identifier
				queryStats = queryStats + "Query Type: ASK \n";
				queryStats = queryStats + "Results Size: "+resultSize  +"\n";
				queryStats = queryStats+QueryStatistics.getQueryStats(query,endpoint,graph,datasetSize);
				queryStats = queryStats+"Query Execution Time (ms): "+ exeTime +"\n";
				queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(query, "UTF-8")+"\n";
				//System.out.println(queryStats);
				bw.write(queryStats);
				System.out.println(count+ ": writing...");
				count++;
			} catch (Exception e) {	errorCount++; }  
		} 	
		else if (resultSize==0)		
			zeroCount++;
		else if (resultSize==-1)	
			errorCount++;
		else if (resultSize==-2)	
			timeOutCount++; 
	}
	/**
	 * Wrtie CONSTRUCT queries in to file
	 * @param query SPARQL query
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph, can be null
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static void writeConstructQueries(String query,
			String endpoint, String graph) throws RepositoryException, MalformedQueryException, IOException {
		long resultSize = -1;
		long curTime = System.currentTimeMillis();
		resultSize = Selectivity.getQueryResultSize(query, endpoint,"construct");
		long exeTime = System.currentTimeMillis() - curTime ;
		if (resultSize>0)
		{

			try {
				String queryStats = "#-------------------------------------------------------\nQuery No: "+count+"\n";  //new query identifier
				queryStats = queryStats + "Query Type: CONSTRUCT \n";
				queryStats = queryStats + "Results Size: "+resultSize  +"\n";
				queryStats = queryStats+QueryStatistics.getQueryStats(query,endpoint,graph,datasetSize);
				queryStats = queryStats+"Query Execution Time (ms): "+ exeTime +"\n";
				queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(query, "UTF-8")+"\n";
				//System.out.println(queryStats);
				bw.write(queryStats);
				System.out.println(count+ ": written...");
				count++;
			} catch (Exception e) {	errorCount++; }  
		}
		else if (resultSize==0)		
			zeroCount++;
		else if (resultSize==-1)	
			errorCount++;
		else if (resultSize==-2)	
			timeOutCount++;
	}


	/**
	 * Wrtie SELECT queries in to file
	 * @param query query
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph, cann be null
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	public static void writeSelectQueries(String query, String endpoint, String graph) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException 
	{
		long resultSize = -1;
		long curTime = System.currentTimeMillis();
		resultSize = Selectivity.getQueryResultSize(query, endpoint,"select");
		long exeTime = System.currentTimeMillis() - curTime ;
		//System.out.println(query);
		if (resultSize>0)
		{

			try {
				String queryStats = "#-------------------------------------------------------\nQuery No: "+count+"\n";  //new query identifier
				queryStats = queryStats + "Query Type: SELECT \n";
				queryStats = queryStats + "Results Size: "+resultSize  +"\n";
				queryStats = queryStats+QueryStatistics.getQueryStats(query,endpoint,graph,datasetSize);
				queryStats = queryStats+"Query Execution Time (ms): "+ exeTime +"\n";
				queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(query, "UTF-8")+"\n";
				//System.out.println(queryStats);
				bw.write(queryStats);
				System.out.println(count+ ": written...");
				count++;
			} catch (Exception e) {	errorCount++; }  
		}
		else if (resultSize==0)		
			zeroCount++;
		else if (resultSize==-1)	
			errorCount++;
		else if (resultSize==-2)	
			timeOutCount++;
	}
}
