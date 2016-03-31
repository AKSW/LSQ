package org.aksw.simba.benchmark.log.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.simba.benchmark.log.operations.DateConverter.DateParseException;
import org.aksw.simba.largerdfbench.util.Selectivity;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
/**
 * Read Sesame Queries log
 * @author Saleem
 *
 */
@SuppressWarnings("unused")
public class SesameLogReader {
	public static HashMap<String, HashMap<String, Set<Long>>> normalizedQueries  = new HashMap<String, HashMap<String, Set<Long>>> ();
	
	public static void main(String[] args) throws IOException, MalformedQueryException, RepositoryException, QueryEvaluationException {
		//String queryLogsDir = "D:/Query Logs/RKBExplorer/";
		String queryLogDir = "D:/Query Logs/SWDF/";
		String endpoint = "http://localhost:8890/sparql";
		String graph = "http://aksw.org/benchmark"; //can be null
//		Selectivity.maxRunTime= -1; //query timeout in second. zero or negative means no timout limit
		
//		String queryLogDir = args[0];
//		String endpoint = args[1];
//		String graph = args[2]; //can be null
//		Selectivity.maxRunTime= -1; //query timeout in second. zero or negative means no timout limit
	
		HashMap<String, Set<String>> queries = getSesameLogQueries(queryLogDir);
		CleanQueryWriter.writeCleanQueriesWithStats(queries,endpoint,graph, "newSWDFCleanQueriesTest.txt");

	}
	/**
	 * Get the set of all distinct queries from the log.
	 * @param queryLogDir Query Log Directory
	 * @return queries A Map of queries. Where keys of the Map are:select, ask, describe, construct
	 * @throws IOException
	 */
	public static HashMap<String, Set<String>> getSesameLogQueries(String queryLogDir) throws IOException  {
		HashMap<String, Set<String>> queries = new HashMap<String, Set<String>>();
		long totalLogQueries = 0 ;
		long parseErrorCount =0;
		Set<String> selectQueries = new HashSet<String> ();
		Set<String> constructQueries = new HashSet<String> ();
		Set<String> askQueries = new HashSet<String> ();
		Set<String> describeQueries = new HashSet<String> ();
		File dir = new File(queryLogDir);
		File[] listOfQueryLogs = dir.listFiles();
		System.out.println("Query Log Parsing in progress...");
		for (File queryLogFile : listOfQueryLogs)
		{
			System.out.println(queryLogFile.getName()+ ": in progress...");
			BufferedReader br = new BufferedReader(new FileReader(queryLogDir+queryLogFile.getName()));
			String line;
			while ((line = br.readLine()) != null)
			{	
				//System.out.println(totalLogQueries);
				if(line.contains("sparql?query="))
				{
					totalLogQueries++;
					String queryStr = getQuery(line); 
					try{
						Query query = QueryFactory.create(queryStr);
						query = removeNamedGraphs(query);
						if(query.isDescribeType())
						{
							if (!describeQueries.contains(query.toString()))
								describeQueries.add(query.toString());
						}
						else if (query.isSelectType())
						{
							if (!selectQueries.contains(query.toString()))
								selectQueries.add(query.toString());
						}
						else if (query.isAskType()){
							if (!askQueries.contains(query.toString()))
								askQueries.add(query.toString());
						}
						else if (query.isConstructType()){
							if (!constructQueries.contains(query.toString()))
								constructQueries.add(query.toString());
						}
					}
					catch (Exception ex){parseErrorCount++;}
				}
			}
			br.close();

		}
		queries.put("select", selectQueries);
		queries.put("construct", constructQueries);
		queries.put("ask", askQueries);
		queries.put("describe", describeQueries);
		System.out.println("Query log parsing completed\nTotal Number of queries (including duplicates): " + totalLogQueries );
		System.out.println("Number of queries with parse errors:" + parseErrorCount);
		System.out.println("Total distinct log queries: "+ (selectQueries.size()+constructQueries.size()+askQueries.size()+describeQueries.size()));
		System.out.println(" SELECT: "+selectQueries.size());
		System.out.println(" CNOSTRUCT: "+constructQueries.size());
		System.out.println(" ASK: "+askQueries.size() );
		System.out.println(" DESCRIBE: "+describeQueries.size() );
		return queries;
	}
	/**
	 * This method reads all the query logs in a directory and returns a map of the query to all its corresponding submissions.
	 * Where a submission contain the sender IP and the submission date and time
	 * @param queryLogDir Query Log Directory	
	 * @return Query to corresponding submissions hash map
	 * @throws IOException Io exceptions
	 * @throws DateParseException 
	 */
	public Map<String, Set<String>> getSesameQueryExecutions(String queryLogDir) throws IOException, DateParseException {
		Map<String, Set<String>> queries = new ConcurrentHashMap<String, Set<String>>();
		long totalLogQueries = 0 ;
		File dir = new File(queryLogDir);
		File[] listOfQueryLogs = dir.listFiles();
		String queryStr = "";
		System.out.println("Log parsing started for duplicates...");
		for (File queryLogFile : listOfQueryLogs)
		{
			System.out.println(queryLogFile.getName()+ ": in progress...");
			BufferedReader br = new BufferedReader(new FileReader(queryLogDir+queryLogFile.getName()));
			String line;
			while ((line = br.readLine()) != null)
			{	
				//System.out.println(line);
				if(line.contains("sparql?query="))
				{
					totalLogQueries++;
					queryStr = SesameLogReader.getQuery(line);
					queryStr = queryStr.replace("\"", "'");
					queryStr = queryStr.replaceAll("\n", " ").replace("\r", "");
					String [] prts = line.split("\"GET ");
					String [] ipTimeParts = prts[0].split("- -");
					String ip = ipTimeParts[0];
					String dateTime =ipTimeParts[1].replace(" ", "").replace("[", "").replace("]","");
					String dtPrts[] = dateTime.split("\"");
					dateTime = dtPrts[0];
					//System.out.println(dateTime);
					dateTime = DateConverter.convertDate(dateTime);
					String ipDateTime = ip.trim() + "- -"+dateTime;
					if(queries.containsKey(queryStr))
					{
						Set<String> submissions = queries.get(queryStr);
						//System.out.println(ipDateTime);
						synchronized (submissions) {
							submissions.add(ipDateTime);   //prts[0] of each line contains the sunder ip and submission date, time
						}
					}	
					else
					{
						Set<String> submissions = new HashSet<String>();
						submissions.add(ipDateTime);  //add current duplicate line
						queries.put(queryStr, submissions);
					}
				}
			}
			br.close();
		}
		System.out.println("Query log parsing completed\nTotal Number of queries (including duplicates): " + totalLogQueries );

		return queries;
	}

	/**
	 * Remove Named Graphs from query
	 * @param query Jena parsed query
	 * @return Jena parsed query with no named graphs
	 */
	public static Query removeNamedGraphs(Query query) {
		query.getGraphURIs().clear();
		return query;
	}
	/**
	 * Parse the log line and get the required SAPARQL query 
	 * @param line Log Line
	 * @return query SPARQL quey
	 */
	public static String getQuery(String line) {
		String prts[] = line.split("query=");
		String queryPrts [] =  prts[1].split("HTTP/1.");
		String query = queryPrts[0];
		String [] parts  = query.split("&results=");
		query = parts[0];
		parts  = query.split("&format=");
		query = parts[0];
		parts  = query.split("&timeout=");
		query = parts[0];
		parts  = query.split("&maxrows=");
		query = parts[0];
		parts  = query.split("&_=");
		query = parts[0];
		parts  = query.split("&Accept=");
		query = parts[0];
		parts  = query.split("&graph=");
		query = parts[0];
		parts  = query.split("&output=");
		query = parts[0];
		parts  = query.split("&callback=");
		query = parts[0];
		parts  = query.split("&stylesheet");
		query = parts[0];
		parts  = query.split("&default-graph-uri=");
		query = parts[0];

		// query = queryPrts[0].substring(7,queryPrts[0].length());
		try{
			query  = java.net.URLDecoder.decode(query, "UTF-8");
		}
		catch (Exception e) {//System.err.println(query+ " "+ e.getMessage());
		}

		return query;
	}

}
