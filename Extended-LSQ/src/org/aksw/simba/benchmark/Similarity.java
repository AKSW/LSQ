package org.aksw.simba.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.log.operations.CleanQueryReader;


/**
 * Calculate the Similarity between the benchmark and log queries
 * @author Saleem
 *
 */
public class Similarity {

	public static void main(String[] args) {


	}
	/**
	 * Get similarity score between log and benchmark queries
	 * @param normalizedVectors Normalized vectors of log queries
	 * @param benchmarkQueriesIds query Ids of benchmark queries
	 * @return similarity score
	 */
	public static Double getSimilarityScore(Map<String, Double[]> normalizedVectors, Set<String> benchmarkQueriesIds) {
		double similarity = 0;
		List<Double[]> dimLogVectors = getDimensionWiseLogVectors(normalizedVectors);
		List<Double[]> dimBenchmarkVectors = getDimensionWiseBenchmarkVectors(normalizedVectors,benchmarkQueriesIds);
		Double[] meanLogVector = getMeanVector(dimLogVectors); 
		Double[] meanBenchmarkVector = getMeanVector(dimBenchmarkVectors); 
		Double[] sdLogVector = getStandardDeviationVector(dimLogVectors); 
		Double[] sdBenchmarkVector = getStandardDeviationVector(dimBenchmarkVectors); 
		double meanError = getError(meanLogVector,meanBenchmarkVector);
		double sdError = getError(sdLogVector,sdBenchmarkVector);
		System.out.println("Mean Error: " + meanError + " ,  S.D. Error: " + sdError);
		similarity = (2*meanError*sdError)/(meanError+sdError);
		return similarity;
	}
	/**
	 * Get either S.D or Mean Errors
	 * @param logVector Log vectors of mean or S.D
	 * @param benchmarkVector benchmark vectors of mean or S.D
	 * @return error value
	 */
	public static double getError(Double[] logVector, Double[] benchmarkVector) {
		double sum = 0;
		for(int i=0; i<logVector.length;i++)
		{
			sum = sum +  Math.pow((logVector[i]-benchmarkVector[i]), 2);
		}
		return sum/logVector.length;
	}
	/**
	 * Get vector of S.Ds
	 * @param vectors List of Vectors
	 * @return vector of S.Ds
	 */
	public static Double[] getStandardDeviationVector(List<Double[]> vectors) {
		Double[] sdVector = new Double[CleanQueryReader.length];
		int dim = 0;
		for(Double[] vector:vectors)
		{
			sdVector[dim]= getStandardDeviation(vector);
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
		Double[] meanVector = new Double[CleanQueryReader.length];
		int dim = 0;
		for(Double[] vector:vectors)
		{
			meanVector[dim]= getMean(vector);
			dim++;
		}
		return meanVector;
	}

	/**
	 * Get the dimension-wise vectors of the the normalized selected benchmark queries
	 * @param normalizedVectors Map of query ids to corresponding normalized vectors
	 * @param queriesIds Selected Benchmakr queries ids
	 * @return List of dimension wise vectors
	 */
	public static List<Double[]> getDimensionWiseBenchmarkVectors(Map<String, Double[]> normalizedVectors, Set<String> benchmarkQueriesIds) {
		List<Double[]> dimVectors = new ArrayList<Double[]>();
		for(int dim =0; dim <CleanQueryReader.length;dim++)
		{
			Double [] dimVector = new Double[benchmarkQueriesIds.size()];
			int index = 0 ;
			for(String  key: benchmarkQueriesIds)
			{
				dimVector[index] = normalizedVectors.get(key)[dim];
				index++;
			}
			dimVectors.add(dimVector);
		}
		return dimVectors;
	}
	/**
	 * Get the dimension-wise vectors of the the normalized  Log vectors
	 * @param normalizedVectors Map of query ids to corresponding normalized vectors
	 * @return List of dimension wise vectors
	 */
	public static List<Double[]> getDimensionWiseLogVectors(Map<String, Double[]> normalizedVectors	) {
		List<Double[]> dimVectors = new ArrayList<Double[]>();
		for(int dim =0; dim <CleanQueryReader.length;dim++)
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
	/**
	 * Calculate standard deviation of given vector
	 * @param vector Vector
	 * @return Standard Deviation
	 */
	public static double getStandardDeviation(Double [] vector) {

		double mean = getMean(vector);
		// sd is sqrt of sum of (values-mean) squared divided by n - 1
		// Calculate the mean
		//  double mean = 0;
		final int n = vector.length;
		if ( n < 2 )
		{
			return Double.NaN;
		}
		// calculate the sum of squares
		double sum = 0;
		for ( int i=0; i<n; i++ )
		{
			final double v = vector[i] - mean;
			sum += v * v;
		}
		// Change to ( n - 1 ) to n if you have complete data instead of a sample.
		return Math.sqrt( sum / ( n - 1 ) );
	}
	/**
	 * Get the mean of the vector values
	 * @param vector Vector
	 * @return Mean
	 */
	public static double getMean(Double[] vector) {
		double sum = 0;
		for(int i=0;i<vector.length;i++)
		{
			sum = sum + vector[i];
		}
		return (sum/vector.length);
	}

}
