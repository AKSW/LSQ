package org.aksw.simba.benchmark.comparisons;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.Similarity;
import org.aksw.simba.benchmark.clustring.QueryClustering;
import org.aksw.simba.benchmark.log.operations.CleanQueryReader;
import org.aksw.simba.benchmark.log.operations.DBpediaLogReader;
import org.aksw.simba.trash.QueryStatistics;
import org.aksw.simba.trash.Selectivity;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
/**
 * Benchmark Queries. 
 * @author Saleem
 *
 */
public class DBPSBSimilarityError {
	static BufferedWriter bwd;
	public static int dbq = 0;
	
		public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
			Selectivity.maxRunTime= -1;
			//String endpoint = "http://localhost:8890/sparql";   
			//String graph = "http://aksw.org/feasible"; 
			//String dbpediaBenchmarkQueriesFile = "../FEASIBLE/queries/DBPSBQueries2011.txt";
			String outPutFile = "DBPSB-25.txt"; //replace 15 with 25 for dbpedia-25
			String queryFileWithStats = "DBpedia3.5.1-CleanQueries.txt";	
			Map<String, Double[]> normalizedVectors  = 	CleanQueryReader.getNormalizedFeaturesVectors(queryFileWithStats);
			//writeDBpediaBenchQueriesWithStats(dbpediaBenchmarkQueriesFile,outPutFile,endpoint,graph);
			Double [] maxIndexVal ={1406396.0, 14.0, 18.0, 11.0, 11.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 56041.0}; // this shows the highest values for each dimension in the dbpedia query logs. we hard coded this to save processing
			Map<String, Double[]> dbpediaNormalizedVectors = getDBpediaNormalizedVectors(outPutFile,maxIndexVal);
			Double similarityScore = getDBpediaBenchmarkSimilarityScore(normalizedVectors,dbpediaNormalizedVectors);
			System.out.println("Similarity Error : " + similarityScore);
	}

	private static Double getDBpediaBenchmarkSimilarityScore(Map<String, Double[]> normalizedVectors,Map<String, Double[]> dbpediaNormalizedVectors) {
		double similarity = 0;
		List<Double[]> dimLogVectors = getDimensionWiseVectors(normalizedVectors);
		List<Double[]> dimBenchmarkVectors =getDimensionWiseVectors(dbpediaNormalizedVectors);
		Double[] meanLogVector = Similarity.getMeanVector(dimLogVectors); 
		Double[] meanBenchmarkVector = Similarity.getMeanVector(dimBenchmarkVectors); 
		Double[] sdLogVector = Similarity.getStandardDeviationVector(dimLogVectors); 
		Double[] sdBenchmarkVector = Similarity.getStandardDeviationVector(dimBenchmarkVectors); 
		double meanError = Similarity.getError(meanLogVector, meanBenchmarkVector);
		double sdError = Similarity.getError(sdLogVector, sdBenchmarkVector);
		System.out.println("Mean Error: " + meanError + " ,  S.D. Error: " + sdError);
		similarity = (2*meanError*sdError)/(meanError+sdError);
		return similarity;
		}

//	private static List<Double[]> getDimensionWiseBenchmarkVectors(	Map<String, Double[]> normalizedVectors, Map<String, Double[]> dbpediaNormalizedVectors) {
//		List<Double[]> dimVectors = new ArrayList<Double[]>();
//        for(int dim =0; dim <16;dim++)
//        {
//        	Double [] dimVector = new Double[dbpediaNormalizedVectors.size()];
//        	int index = 0 ;
//            for(int vector = 0; vector <dbpediaNormalizedVectors.size(); vector++)
//            {
//            	dimVector[index] = normalizedVectors.get(key)[dim];
//            	index++;
//            }
//            dimVectors.add(dimVector);
//         }
//	return dimVectors;
//	
//	}

	
		/**
		 * Get the dimension-wise vectors of the the normalized  vectors
		 * @param normalizedVectors Map of query ids to corresponding normalized vectors
		 * @return List of dimension wise vectors
		 */
			public static List<Double[]> getDimensionWiseVectors(Map<String, Double[]> normalizedVectors	) {
				List<Double[]> dimVectors = new ArrayList<Double[]>();
			        for(int dim =0; dim <16;dim++)
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
	

	private static Map<String, Double[]> getDBpediaNormalizedVectors(String queryFileWithStats, Double[] maxIndexVal) throws IOException
	{
		Map<String, Double[]> vectors = new HashMap<String, Double[]>();
		BufferedReader br = new BufferedReader(new FileReader(new File(queryFileWithStats)));
		System.out.println("Feature vectors loading in progress...");
		String line= br.readLine();
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			Double [] vector = new Double[16];
			String queryNumberLine [] = line.split(":"); // query number
			String queryNumber = queryNumberLine[1];
			    br.readLine(); //query type
				vector[0] = CleanQueryReader.getFeatureValue(br.readLine()); //ResutSize
				vector[1] = CleanQueryReader.getFeatureValue(br.readLine()); // bgps
				vector[2] = CleanQueryReader.getFeatureValue(br.readLine()); //triple pattern count
				vector[3] = CleanQueryReader.getFeatureValue(br.readLine()); // join vertices
				vector[4] = CleanQueryReader.getFeatureValue(br.readLine()); // mean join vertices degree
				vector[5] = CleanQueryReader.getFeatureValue(br.readLine()); // mean triple pattern selectivity
				vector[6] = CleanQueryReader.getClauseValue(br.readLine()); // union
				vector[7] = CleanQueryReader.getClauseValue(br.readLine()); // distinct
				vector[8] = CleanQueryReader.getClauseValue(br.readLine()); // order by
				vector[9] = CleanQueryReader.getClauseValue(br.readLine()); //regex
				vector[10]= CleanQueryReader.getClauseValue(br.readLine()); // limit
				vector[11] = CleanQueryReader.getClauseValue(br.readLine()); // offset
				vector[12] = CleanQueryReader.getClauseValue(br.readLine()); //optional
				vector[13] = CleanQueryReader.getClauseValue(br.readLine()); //fitler
				vector[14] = CleanQueryReader.getClauseValue(br.readLine()); //group by
				vector[15] = CleanQueryReader.getFeatureValue(br.readLine()); //Query execution time
				
			line = br.readLine();  //query string
			line = br.readLine();  //start of new query
			vectors.put(queryNumber, vector);
		}
		br.close();
			return CleanQueryReader.getNormalizedVectors(vectors, maxIndexVal);
		}

	/**
	 * Get benchmark queries 
	 * @param queryFileWithStats Clean queries file with stats
	 * @param numberOfQueries The number of queries to be included in benchmark
	 * @return queries The required benchmark queries 
	 * @throws IOException
	 */
	public static Set<String> getBenchmarkQueries(String queryFileWithStats,int numberOfQueries) throws IOException {
		Map<String, Double[]> normalizedVectors  = 	CleanQueryReader.getNormalizedFeaturesVectors(queryFileWithStats);
		QueryClustering qc = new QueryClustering();
		Set<String> queriesIds = qc.getPrototypicalQueries(normalizedVectors,numberOfQueries);
		Set<String> queries = CleanQueryReader.getQueriesWithStats(queryFileWithStats,queriesIds);
		return queries;
	}
	/**
	 * Print benchmark queries into console
	 * @param queries queries
	 */
	public static void printBenchmarkQueries(Set<String> queries) {
		System.out.println("Total Queries: "+ queries.size());
		int count = 1;
		for(String query:queries)
		{
			String [] qryParts = query.split("Query String:");
			System.out.println(count+":----------------------\nQuery Features: \n\n"+qryParts[0]+ "\nQuery: \n"+qryParts[1].trim());
			count++;
		}

	}
/**
 *  Write Dbpedia SPARQL benchmark queries with stats into a file
 * @param graph Named Graph
 * @param endpoint  Endpoint
 * @param outPutFile  Output file 
 * @param dbpediaBenchmarkQueriesFile Dbpedia benchmark queries file 
 * @throws IOException 
 * @throws RepositoryException
 * @throws MalformedQueryException
 * @throws QueryEvaluationException
 */
	@SuppressWarnings("unused")
	private static void writeDBpediaBenchQueriesWithStats(String dbpediaBenchmarkQueriesFile, String outPutFile, String endpoint, String graph) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		BufferedReader br = new BufferedReader(new FileReader(new File(dbpediaBenchmarkQueriesFile)));
		long datasetSize = 14274113;
		bwd= new BufferedWriter(new FileWriter(outPutFile));
		String line= "";
		//int i = 0;
		while ((line = br.readLine()) != null)
		{
			String [] prts = line.split("\t");
			//System.out.println(prts[2]);
			Query query = QueryFactory.create(prts[2]);
			query = DBpediaLogReader.removeNamedGraphs(query);
			//System.out.println(i + ": " + query.toString());
			writeCleanDbpediaBenchQueries(query.toString(),endpoint,graph,datasetSize);
			//i++;
		}
		bwd.write("#--end---");
		bwd.close();
		br.close();
		System.out.println("DBpedia SPARQL benchmark queries with stats written to DBpediaBenchmark-CleanQueries.txt");
	}

private static void writeCleanDbpediaBenchQueries(String query, String endpoint, String graph, long datasetSize) throws RepositoryException, MalformedQueryException, IOException, QueryEvaluationException {
	//System.out.println(query);
	long curTime = System.currentTimeMillis();
	long resultSize = Selectivity.getQueryResultSize(query, endpoint,"select");
	//System.out.println(resultSize);
	long exeTime = System.currentTimeMillis() - curTime ;
	if (resultSize>0)
	{

		try {
			String queryStats = "#-------------------------------------------------------\nQuery No: "+dbq+"\n";  //new query identifier
			queryStats = queryStats + "Query Type: SELECT \n";
			queryStats = queryStats + "Results Size: "+resultSize  +"\n";
			queryStats = queryStats+QueryStatistics.getQueryStats(query,endpoint,graph,datasetSize);
			queryStats = queryStats+"Query Execution Time (ms): "+ exeTime +"\n";
			queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(query, "UTF-8")+"\n";
			//System.out.println(queryStats);
			bwd.write(queryStats);
			System.out.println(dbq+ ": written...");
			dbq++;
			} catch (Exception e) {	 }  
	}

}
	
}



