package org.aksw.simba.benchmark.log.operations;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.benchmark.Config;
import org.aksw.simba.benchmark.clustring.QueryClustering;
/**
 * Read Clean Queries (containing stats) from File
 * @author Saleem
 *
 */
public class CleanQueryReader {
	public static int length; //vectors length
	static BufferedReader br ;
	public static void main(String[] args) throws IOException {
		String queryFileWithStats = "CleanQueries.txt";
		Map<String, Double[]> normalizedVectors = 	getNormalizedFeaturesVectors(queryFileWithStats);
		QueryClustering qc = new QueryClustering();
		System.err.println(qc.getPrototypicalQueries(normalizedVectors, 20));
	}
	/**
	 * Get the normalized features vectors of the queries. 
	 * @param queryFileWithStats The file containing all the queries along with stats
	 * @return Normalized Vectors
	 * @throws IOException
	 */
	public static Map<String, Double[]> getNormalizedFeaturesVectors(String queryFileWithStats) throws IOException {
		Map<String, Double[]> vectors = new HashMap<String, Double[]>();
		length = getVectorLength();
		Double [] maxIndexVal = new Double[length]; 
		for(int i=0;i<length;i++)
			maxIndexVal[i] = 0d;
		//System.out.println(length);
		br = new BufferedReader(new FileReader(new File(queryFileWithStats)));
		System.out.println("SPARQL clauses filter condition: "+ Config.clauseFilter);
		System.out.println("Query features filter condition: "+ Config.featureFilter);
		System.out.println("Feature vectors loading in progress...");
		String line= br.readLine();
		String queryNumber = "";
		Double[] features = new Double[16];
		while ((line = br.readLine()) != null && !line.equals("#--end---"))
		{
			String queryNumberLine [] = line.split(":"); // query number
			queryNumber = queryNumberLine[1];
			String queryType = br.readLine(); //query type
			features = getQueryFeatures();
			line = br.readLine();  //query string
			line = br.readLine();  //start of new query
			if(queryTypeTest(queryType)==true &&  costimizedClauseTest(features)==true && costimizedFeaturesTest(features)==true )
			{	
				Double [] vector = new Double[length]; 
				int index = 0;
				//----- selected get features
				if (Config.resultSize==true)
				{
					vector[index] =features[0];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.BGPs==true)
				{
					vector[index] =features[1];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.triplePatternsCount==true)
				{
					vector[index] =features[2];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.joinVertices==true)
				{
					vector[index] =features[3];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.meanJoinVerticesDegree==true)
				{
					vector[index] =features[4];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.meanTriplePatternSelectivity==true)
				{
					vector[index] =features[5];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}

				if (Config.UNION==true)
				{
					vector[index] = features[6];
					maxIndexVal[index] = 1d;
					index++;

				}

				if (Config.DISTINCT==true)
				{
					vector[index] = features[7];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.ORDERBY==true)
				{
					vector[index] = features[8];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.REGEX==true)
				{
					vector[index] = features[9];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.LIMIT==true)
				{
					vector[index] = features[10];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.OFFSET==true)
				{
					vector[index] = features[11];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.OPTIONAL==true)
				{
					vector[index] = features[12];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.FILTER==true)
				{
					vector[index] = features[13];
					maxIndexVal[index] = 1d;
					index++;
				}

				if (Config.GROUPBY==true)
				{
					vector[index] = features[14];
					maxIndexVal[index] = 1d;
					index++;
				}
				if (Config.runTime==true)
				{
					vector[index] =features[15];
					if(maxIndexVal[index]<vector[index])
						maxIndexVal[index] = vector[index];
					index++;
				}
				vectors.put(queryNumber, vector);
			}

		}
		System.out.println("Feature vectors loading completed. Vectors returned");
		System.out.println("Total queries to be considered for benchmark: "+ vectors.size());
		//for(int k=0; k<maxIndexVal.length;k++)
		//System.out.print(maxIndexVal[k] + ", ");
		return getNormalizedVectors(vectors,maxIndexVal);

	}
	/**
	 * Check whether the Filtering test of the features (e.g. ResultSize, BGPs etc)  of a SPARQL query  passes or not
	 * @param features Features vector of the qiven query
	 * @return true or false
	 */
	private static boolean costimizedFeaturesTest(Double[] features) {
		boolean test = false;
		if (Config.featureFilter.equals(""))
			test= true;
		else
		{
			String[] featureCombinations = Config.featureFilter.replace("(", "").replace(")", "").split("OR");
			for(int i =0 ; i <featureCombinations.length;i++) //here we need to check for OR
			{
				test = true;
				String [] filterFeatures = featureCombinations[i].split("AND");
				for(int j=0;j<filterFeatures.length;j++)   // here we need to check for AND
				{
					// we have x > = y  or x <= y . need to get the operator and operands
					String featureFilter = filterFeatures[j].trim();
					String leftOpr = "";
					@SuppressWarnings("unused")
					String opr = "" ;
					double rightOpr ;
					if (featureFilter.contains(">="))
					{
						opr = ">=";
						String prts [] = featureFilter.split(">=");
						leftOpr = prts[0].trim();
						rightOpr = Double.parseDouble(prts[1].trim());
						if( (leftOpr.equals("ResultSize") && features[0] < rightOpr) || (leftOpr.equals("BGPs") && features[1] < rightOpr) || (leftOpr.equals("TriplePatternsCount") && features[2] < rightOpr) || (leftOpr.equals("JoinVertices") && features[3] < rightOpr)|| (leftOpr.equals("MeanJoinVerticesDegree") && features[4] < rightOpr)|| (leftOpr.equals("MeanTriplePatternsSelectivity") && features[5] < rightOpr)|| (leftOpr.equals("RunTime") && features[15] < rightOpr))
						{
							test = false;
							break ;
						}
					}
					else
					{
						opr = "<=";
						String prts [] = featureFilter.split("<=");
						leftOpr = prts[0].trim();
						rightOpr = Double.parseDouble(prts[1].trim()); 
						if( (leftOpr.equals("ResultSize") && features[0] > rightOpr) || (leftOpr.equals("BGPs") && features[1] > rightOpr) || (leftOpr.equals("TriplePatternsCount") && features[2] > rightOpr) || (leftOpr.equals("JoinVertices") && features[3] > rightOpr)|| (leftOpr.equals("MeanJoinVerticesDegree") && features[4] > rightOpr)|| (leftOpr.equals("MeanTriplePatternsSelectivity") && features[5] > rightOpr)|| (leftOpr.equals("RunTime") && features[15] > rightOpr))
						{
							test = false;
							break ;
						}
					}


				}
				if (test==true)   // if any of the combination in disjunctive normal form get true the overall confidition is fullfilled and query should be selected
					return true;
			}

		}
		return test;

	}
	/**
	 * Get the features vectors of the query reading from log file
	 * @return features Feature vector
	 * @throws IOException
	 */
	private static Double[] getQueryFeatures() throws IOException {
		Double[] features = new Double[16];
		features[0] = getFeatureValue(br.readLine()); //ResutSize
		features[1] = getFeatureValue(br.readLine()); // bgps
		features[2] = getFeatureValue(br.readLine()); //triple pattern count
		features[3] = getFeatureValue(br.readLine()); // join vertices
		features[4] = getFeatureValue(br.readLine()); // mean join vertices degree
		features[5] = getFeatureValue(br.readLine()); // mean triple pattern selectivity
		features[6] = getClauseValue(br.readLine()); // union
		features[7] = getClauseValue(br.readLine()); // distinct
		features[8] = getClauseValue(br.readLine()); // order by
		features[9] = getClauseValue(br.readLine()); //regex
		features[10]= getClauseValue(br.readLine()); // limit
		features[11] = getClauseValue(br.readLine()); // offset
		features[12] = getClauseValue(br.readLine()); //optional
		features[13] = getClauseValue(br.readLine()); //fitler
		features[14] = getClauseValue(br.readLine()); //group by
		features[15] = getFeatureValue(br.readLine()); //Query execution time
		return features;
	}
	/**
	 * Check whether the Filtering test of clauses (e.g., UNION, Filter etc)  of a SPARQL query  passes or not
	 * @param features Features vector of the qiven query
	 * @return true or false
	 */
	private static boolean costimizedClauseTest(Double[] features) {
		boolean test = false;
		if (Config.clauseFilter.equals(""))
			test= true;
		else
		{
			String[] clauseCombinations = Config.clauseFilter.replace("(", "").replace(")", "").split("OR");
			for(int i =0 ; i <clauseCombinations.length;i++) //here we need to check for OR
			{
				test = true;
				String [] clauses = clauseCombinations[i].split("AND");
				for(int j=0;j<clauses.length;j++)   // here we need to check for AND
				{
					String clause = clauses[j].trim();
					// System.out.println(clause);
					if( (clause.equals("UNION") && features[6] == 0) || (clause.equals("DISTINCT") && features[7] == 0)|| (clause.equals("ORDERBY") && features[8] == 0)|| (clause.equals("REGEX") && features[9] == 0)|| (clause.equals("LIMIT") && features[10] == 0)|| (clause.equals("OFFSET") && features[11] == 0)|| (clause.equals("OPTIONAL") && features[12] == 0)|| (clause.equals("FILTER") && features[13] == 0)|| (clause.equals("GROUPBY") && features[14] == 0))
					{
						test = false;
						break ;
					}
				}
				if (test==true)   // if any of the combination in disjunctive normal form get true the overall confidition is fullfilled and query should be selected
					return true;
			}

		}
		return test;		
	}
	/**
	 * Get the SPARQL clause value i.e. 1 or 0
	 * @param line Line can be like UNION = true so value will be 1 for true and 0 for false
	 * @return boolean value
	 */
	public static double getClauseValue(String line) {
		double value ;
		String prts [] = line.split(":");
		if(prts[1].trim().equals("No"))
			value=0d;
		else 
			value = 1d;
		return value;
	}
	/**
	 * Get the SPARQL feature value
	 * @param line Line can be like BGPs = 5 so value will be 5 
	 * @return double value of the feature
	 */
	public static double getFeatureValue(String line) {
		String prts [] = line.split(":");
		return Double.parseDouble(prts[1].trim());
	}
	/**
	 * This checks what basic type of SPARQL queries (SELECT, DESCRIBE, ASK, CONSTRUCT) the user is interested to be included into benchmark
	 * @param queryType Type of the query
	 * @return boolean value
	 */
	private static boolean queryTypeTest(String queryType) {
		boolean test ;
		if ((queryType.contains("SELECT")&& Config.SELECT==true) || (queryType.contains("CONSTRUCT")&& Config.CONSTRUCT==true) || (queryType.contains("DESCRIBE")&& Config.DESCRIBE==true) || (queryType.contains("ASK")&& Config.ASK==true) || (queryType.contains("CONSTRUCT")&& Config.CONSTRUCT==true))
			test= true;
		else
			test= false;
		return test;
	}
	/**
	 * Get normalized vectors from the given vectors
	 * @param vectors Features vectors
	 * @param maxIndexVal The max feature value
	 * @return normalizedVectors
	 */
	public static Map<String, Double[]> getNormalizedVectors(Map<String, Double[]> vectors, Double[] maxIndexVal)
	{
		Map<String, Double[]> normalizedVectors = new HashMap<String, Double[]>();
		for(String query:vectors.keySet())
		{
			Double[] vector = vectors.get(query);
			for(int i=0;i<vector.length;i++)
			{
				if(maxIndexVal[i]==0d)
					vector[i] = 0d;
				else
					vector[i] =vector[i]/ maxIndexVal[i];
			}
			normalizedVectors.put(query, vector);
		}
		vectors.clear();
		return normalizedVectors;
	}
	/**
	 * Get the length of the vectors or dimensions
	 * @return lenght lenght of the vectors
	 */
	public static int getVectorLength() {
		int length = 0;
		if (Config.resultSize==true)
			length++;
		if (Config.BGPs==true)
			length++;
		if (Config.triplePatternsCount==true)
			length++;
		if (Config.joinVertices==true)
			length++;
		if (Config.meanJoinVerticesDegree==true)
			length++;
		if (Config.meanTriplePatternSelectivity==true)
			length++;
		if (Config.UNION==true)
			length++;
		if (Config.DISTINCT==true)
			length++;
		if (Config.ORDERBY==true)
			length++;
		if (Config.REGEX==true)
			length++;
		if (Config.LIMIT==true)
			length++;
		if (Config.OFFSET==true)
			length++;
		if (Config.OPTIONAL==true)
			length++;
		if (Config.FILTER==true)
			length++;
		if (Config.GROUPBY==true)
			length++;
		if (Config.runTime==true)
			length++;
		return length;
	}
	/**
	 * Get the queries along with stats from clean queries file
	 * @param queryFileWithStats  Clean queries file
	 * @param queriesIds Set of queries ids for which stats are required 
	 * @return Queries with stats
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static Set<String> getQueriesWithStats(String queryFileWithStats, Set<String> queriesIds) throws IOException {
		Set<String> queries = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(queryFileWithStats)));
		String line= br.readLine();
		String queryNumber = "";
		String queryFeatures ="";
		while ((line = br.readLine()) != null && !line.equals("#--end---")  && ! queriesIds.isEmpty())
		{
			String queryNumberLine [] = line.split(":"); // query number
			//System.out.println(line);
			queryNumber = queryNumberLine[1];
			br.readLine(); // for query type;
			if(queriesIds.contains(queryNumber))
			{
				line = br.readLine();
				if (Config.resultSize==true)
					queryFeatures = queryFeatures + line+"\n";
				line = br.readLine();
				if (Config.BGPs==true)
					queryFeatures = queryFeatures + line+"\n";
				line = br.readLine();
				if (Config.triplePatternsCount==true)
					queryFeatures = queryFeatures + line+"\n";
				line = br.readLine();
				if (Config.joinVertices==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.meanJoinVerticesDegree==true)
					queryFeatures = queryFeatures + line+"\n";					
				line = br.readLine();
				if (Config.meanTriplePatternSelectivity==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.UNION==true)
					queryFeatures = queryFeatures + line+"\n";					
				line = br.readLine();
				if(Config.DISTINCT==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.ORDERBY==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.REGEX==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.LIMIT==true)
					queryFeatures = queryFeatures + line+"\n";				
				line = br.readLine();
				if (Config.OFFSET==true)
					queryFeatures = queryFeatures + line+"\n";
				line = br.readLine();
				if (Config.OPTIONAL==true)
					queryFeatures = queryFeatures + line+"\n";		
				line = br.readLine();
				if (Config.FILTER==true)
					queryFeatures = queryFeatures + line+"\n";	
				line = br.readLine();
				if (Config.GROUPBY==true)
					queryFeatures = queryFeatures + line+"\n";
				line = br.readLine();
				if (Config.runTime==true)
					queryFeatures = queryFeatures + line+"\n";
				line = java.net.URLDecoder.decode(br.readLine(), "UTF-8");  //query string
				queries.add(queryFeatures+line+"\n");
				queryFeatures="";
				line = br.readLine();  //start of new query
				queriesIds.remove(queryNumber);
			}
			else
				for(int i=0;i<18;i++)
					line = br.readLine();
		}
		return queries;
	}


}
