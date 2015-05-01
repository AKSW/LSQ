package org.aksw.simba.benchmark.startup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.Config;
import org.aksw.simba.benchmark.Queries;
import org.aksw.simba.benchmark.Similarity;
import org.aksw.simba.benchmark.clustring.QueryClustering;
import org.aksw.simba.benchmark.clustring.VoronoiPanel;
import org.aksw.simba.benchmark.comparisons.AvgStats;
import org.aksw.simba.benchmark.log.operations.CleanQueryReader;
/**
 * From  here you start your benchmark generation
 * @author Saleem
 *
 */
public class QuerySelecter {

	public static void main(String[] args) throws IOException {
		//--Configuration and input files specifications ------------
				//String queryFileWithStats = "SWDF-CleanQueries.txt";
				String queryFileWithStats = "DBpedia3.5.1-CleanQueries.txt";
				int numberOfQueries = 5;  // number of queries to be generated for a benchmark
				selectCustomFilters();
				long curTime = System.currentTimeMillis();
				//Set<String> queries = Queries.getBenchmarkQueries(queryFileWithStats,10);
				QueryClustering qc = new QueryClustering();
				Map<String, Double[]> normalizedVectors  = 	CleanQueryReader.getNormalizedFeaturesVectors(queryFileWithStats);
				Set<String> queriesIds = qc.getPrototypicalQueries(normalizedVectors,numberOfQueries);
				Set<String> benchmarkQueriesIds = new HashSet<String>(queriesIds); //we need to copy the query ids here/ later being used for similarity calculation. The next function will clear the queriesIds. So we have to copy before
				Set<String> queries = CleanQueryReader.getQueriesWithStats(queryFileWithStats,queriesIds);
				String outputDir = "../FEASIBLE/benchmarks/";
				Queries.printBenchmarkQueries(queries,outputDir);
				System.out.println("\n-----\nBenchmark details saved to "+outputDir+"\nBenchmark generation time (sec): "+(System.currentTimeMillis()-curTime)/1000);
				Double similarityScore = Similarity.getSimilarityScore(normalizedVectors,benchmarkQueriesIds);
				System.out.println("Similarity Error : " + similarityScore);
				VoronoiPanel.drawVoronoiDiagram(normalizedVectors,outputDir+"voronoi.png");
				System.out.println("------------Detailed Analysis of the Generated Benchmark--------------");
				 AvgStats.getPercentUsedLogConstructs(outputDir+"queries-stats.txt");
				 AvgStats.getAvgLogFeatures(outputDir+"queries-stats.txt");	
	}
	/**
	 * Customize your benchmark by activating various filters. See examples below
	 */
	private static void selectCustomFilters() {
		Config.drawVoronoiDiagram = true ;
		// You can set various Filters on benchmark query features and SPARQL clauses . e.g  Resultsize should be between 5 to 10 and BGPs must be greater than 2
		// and Triple patterns should be less or equal to 10 or Mean triple pattern selectivity >= 0.0001
		//See the config file for further deatils
		//Config.featureFilter = "(RunTime >= 50)";
		//Config.featureFilter = "(ResultSize >= 5 AND ResultSize <= 100 AND BGPs >= 2 AND TriplePatterns <=10) OR (MeanTriplePatternsSelectivity >= 0.0001)";
		//Config.clauseFilter = "(OPTIONAL AND DISTINCT) OR (UNION)";
		//Config.featureFilter = "(ResultSize >= 100 AND TriplePatternsCount >= 2 AND TriplePatternsCount <= 5)";
		//Config.clauseFilter = "(DISTINCT AND FILTER) OR (GROUPBY)";
		//------ You can turn on/of basic query types -----
		//Config.ASK =false;
		//Config.DESCRIBE = false; 
		//Config.SELECT=false;
		//Config.CONSTRUCT = false;

	}



}
