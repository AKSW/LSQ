package org.aksw.simba.benchmark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.clustring.QueryClustering;
import org.aksw.simba.benchmark.log.operations.CleanQueryReader;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
/**
 * Benchmark Queries. 
 * @author Saleem
 *
 */
public class Queries {
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {

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
	 * @param outputDir output dir
	 * @throws IOException 
	 */
	public static void printBenchmarkQueries(Set<String> queries, String outputDir) throws IOException {
		System.out.println("Total Queries: "+ queries.size());
		int count = 1;
		BufferedWriter	bwq= new BufferedWriter(new FileWriter(outputDir+"queries.txt")); //queries writer
		BufferedWriter	bw= new BufferedWriter(new FileWriter(outputDir+"queries-stats.txt"));
		for(String queryStr:queries)
		{
			String stats = "";
			//System.out.println(queryStr);
			String [] qryParts = queryStr.split("Query String:");
			Query query = QueryFactory.create(qryParts[1].trim(),Syntax.syntaxARQ);
			stats = "#-------------------------------------------------------\n";
			bwq.write(stats);
			stats=stats+"Query No: "+count+"\n";
			if(query.isAskType())
				stats=stats+"Query Type: ASK\n";
			else if (query.isConstructType())
				stats=stats+"Query Type: SELECT\n";
			else if (query.isDescribeType())
				stats=stats+"Query Type: DESCRIBE\n";
			else if (query.isSelectType())
				stats=stats+"Query Type: SELECT\n";
			stats=stats+qryParts[0];
			//System.out.print(stats);
			bwq.write(qryParts[1].trim()+"\n");
			//System.out.println("Query String: "+qryParts[1].trim());
			stats=stats+"Query String: "+java.net.URLEncoder.encode(qryParts[1].trim(), "UTF-8")+"\n";
			bw.write(stats);
			count++;
		}
		bw.write("#--end---");
		bwq.close();
  bw.close();
	}


}


