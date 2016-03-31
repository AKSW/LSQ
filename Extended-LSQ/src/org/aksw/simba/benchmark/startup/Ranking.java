package org.aksw.simba.benchmark.startup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rank the given set of systems in terms of precentages. e.g, System X rank 1st in Y% of queries, 2nd in Z% of queries and so on. 
 * You need to provide the results as input file one column per system separated by tab
 * @author Saleem
 *
 */
public class Ranking {

	public static void main(String[] args) throws IOException {
		String resultsFile = "D:/workspace/FEASIBLE/ranking/results.txt";
		printRanking(resultsFile);

	}
	/**
	 * Prints rank-wise queries distributions. 
	 * @param resultsFile Results file
	 * @throws IOException
	 */
	public static void printRanking(String resultsFile) throws IOException {
		ConcurrentMap<String, int[]> systemToRanks = new ConcurrentHashMap<String, int[]>();
		BufferedReader br = new BufferedReader(new FileReader(resultsFile));
		String[] systems = br.readLine().split("\t");
		for (int i = 0 ; i <systems.length;i++)
		{
			int [] counts = new int[systems.length];
			for(int j=0; j<counts.length;j++)
				counts[j] = 0;
			systemToRanks.put(systems[i], counts);
		}
		String line;

		double totalResults = 0d;
		while ((line = br.readLine()) != null   )
		{
			String [] values = line.split("\t");
			List<Double> resultsVals = 	convertToDouble(values);
			//  System.out.println("["+ values[0] + ", "+  values[1]+ ", "+  values[2]+ ", "+  values[3]+"]");
			Collections.sort(resultsVals);
			int rnk = 0;
			for(int i =0 ; i <resultsVals.size();)
			{
				List<Integer> indexes = getIndexes(resultsVals.get(i),values);
				//  System.out.println(indexes);
				for(int index:indexes){
					int [] sysCounts = systemToRanks.get(systems[index]);
					synchronized (sysCounts){
						sysCounts[rnk] = sysCounts[rnk]+1;}
				}
				rnk++;
				i = i + indexes.size();
			}
			//    counts[index] = counts[index]+1;
			totalResults ++;
		}
		br.close();
		System.out.println("System\t1st\t2nd\t3rd\t4th");
		for(String system:systemToRanks.keySet())
		{
			System.out.print(system+"\t");
			int [] curCounts =  systemToRanks.get(system);
			for(int i=0 ; i <curCounts.length;i++)
				System.out.print((curCounts[i]/totalResults*100)+ "%\t");
			//System.out.print((curCounts[i])+ "\t");

			System.out.println();
		}
		//		System.out.println();
		//		for(int i = 0 ; i < systems.length;i++)
		//			System.out.print((counts[i]/totalResults*100)+"\t");
	}

	/**
	 * get index of the given value in array
	 * @param value value
	 * @param values array
	 * @return index
	 */
	private static List<Integer> getIndexes(Double value, String[] values) {
		List<Integer> indexes = new ArrayList<Integer>(); ; 
		for (int i=0; i<values.length;i++)
		{
			if(Double.parseDouble(values[i]) == value)
			{
				indexes.add(i);

			}
		}
		return indexes;
	}
	/**
	 * Convert array of Strings to array of doubles
	 * @param values Array of string
	 * @return array of doubles
	 */
	public static List<Double> convertToDouble(String[] values) {
		List<Double> resultsVals =new ArrayList<Double>() ;
		for(int i = 0; i < values.length; i++)
		{
			resultsVals.add(Double.parseDouble(values[i]));
		}
		return resultsVals;
	}

}
