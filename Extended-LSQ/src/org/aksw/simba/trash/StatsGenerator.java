package org.aksw.simba.trash;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
/**
 * Precision, Recall, and F1 generator
 * @author Saleem
 *
 */
public class StatsGenerator {


	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		// TODO Auto-generated method stub
		//String results = "D:/workspace/BigRDFBench-Utilities/results.n3";
		//ResultsLoader.loadResults(results);

		//  String resultsDir = "D:/workspace/BigRDFBench-Utilities/results/";
		// System.out.println( getActualResultsFromFile("D:/workspace/BigRDFBench-Utilities/results/B1").size());
		//  System.out.println(getActualResults("B1").size());

	}
	/**
	 * The actual query results from file
	 * @param resultsFile Specific query results file
	 * @return actualResults Set of actual results
	 * @throws IOException 
	 */
	public static ArrayList<String> getActualResultsFromFile(String resultsFile) throws IOException {
		ArrayList<String> actualResults =  new ArrayList<String>() ;
		@SuppressWarnings("resource")
		BufferedReader br  = new BufferedReader(new InputStreamReader(new FileInputStream(resultsFile),"UTF-8"));
		String line ="";
		br.readLine();
		while (( line= br.readLine()) != null)
			actualResults.add(line.toString().replace("\"", "\'"));

		// System.out.println(count);
		return actualResults ;

	}
	/**
	 * Get Precison, Recall, and F1 Score for the given query results
	 * @param queryFile Query file containing actual results of the query.
	 * @param res ResultSet Iterator
	 * @return Fscores Precision, Recall, and F1 scores
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static String getFscores(String queryFile,TupleQueryResult res) throws QueryEvaluationException, RepositoryException, MalformedQueryException, IOException 
	{
		String Fscores = "" ;
		double precision, recall,F1;
		ArrayList<String> curResults =  getCurrentResult(res) ;
		//System.out.println("current:"+ curResults);
		ArrayList<String> actualResults = getActualResultsFromFile(queryFile) ;
		///System.out.println("actual:" +actualResults);
		long actualResultSize = actualResults.size();
		actualResults.removeAll(curResults);
		// Set<String> diffSet = Sets.difference(actualResults, curResults);
		//Set<String> diff = differenceSet(actualResults, curResults);
		//System.out.println(diff.size());
		//System.out.println(diffSet.size());
		//System.out.println("Actual-Cur: "+ diffSet);
		//System.out.println("Cur-Actual: "+Sets.difference( curResults,actualResults));
		double correctResults = actualResultSize-actualResults.size();   //actualResults already showing set difference
		precision = (correctResults/curResults.size());
		recall = correctResults/actualResultSize;
		F1 = 2*(precision*recall)/(precision+recall);
		Fscores = "Precision: "+precision+", Recall: " + recall +", F1: "+F1;
		return Fscores;

	}
	/**
	 * Get Precison, Recall, and F1 Score for the given query results
	 * @param queryFile Query file containing actual results of the query.
	 * @param curResults Current Results in array each bindings values should be separated by the 
	 * same separator as used in actual results files. In our BigRDFBench results, we used <===> as separator. 
	 * @return Fscores Precision, Recall, and F1 scores
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static String getFscores(String queryFile,ArrayList<String> curResults) throws QueryEvaluationException, RepositoryException, MalformedQueryException, IOException 
	{
		String Fscores = "" ;
		double precision, recall,F1;
		//ArrayList<String> curResults =  getCurrentResult(res) ;
		//System.out.println("current:"+ curResults);
		ArrayList<String> actualResults = getActualResultsFromFile(queryFile) ;
		//System.out.println("actual:" +actualResults);
		long actualResultSize = actualResults.size();
		actualResults.removeAll(curResults);
		// Set<String> diffSet = Sets.difference(actualResults, curResults);
		//Set<String> diff = differenceSet(actualResults, curResults);
		//System.out.println(diff.size());
		//System.out.println(diffSet.size());
		//System.out.println("Actual-Cur: "+ diffSet);
		//System.out.println("Cur-Actual: "+Sets.difference( curResults,actualResults));
		double correctResults = actualResultSize-actualResults.size();   //actualResults already showing set difference
		precision = (correctResults/curResults.size());
		recall = correctResults/actualResultSize;
		F1 = 2*(precision*recall)/(precision+recall);
		Fscores = "Precision: "+precision+", Recall: " + recall +", F1: "+F1;
		return Fscores;

	}
	//	/**
	//	 * Calculate difference set of two sets
	//	 * @param actualResults Set A
	//	 * @param curResults Set B
	//	 * @return diffSet Difference set of A and B
	//	 * @throws UnsupportedEncodingException 
	//	 */
	//	public static Set<String> differenceSet(Set<String> actualResults,
	//		Set<String> curResults) throws UnsupportedEncodingException {
	//		Set<String> diffSet = new HashSet<String>();
	//		for(String e:actualResults)
	//		{
	//			//e = new String(e.getBytes("UTF-8"), "UTF-8");
	//			if(!curResults.contains(e))
	//				diffSet.add(e);
	//		}
	//	return diffSet;
	//}
	/**
	 * Get the list of missing results (if any) the query execution
	 * @param actualResultsFile Query file of actual results
	 * @param res ResultSet Iterator
	 * @return Fscores Precision, Recall, and F1 scores
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static ArrayList<String> getMissingResults(String actualResultsFile,TupleQueryResult res) throws QueryEvaluationException, RepositoryException, MalformedQueryException, IOException 
	{
		ArrayList<String> curResults =  getCurrentResult(res) ;
		//System.out.println("current:"+ curResults);
		ArrayList<String> actualResults = getActualResultsFromFile(actualResultsFile) ;
		//System.out.println("actual:" +actualResults);
		////Set<String> diffSet = Sets.difference(actualResults, curResults);
		actualResults.removeAll(curResults);
		//System.out.println(actualResults);
		return actualResults;

	}

	/**
	 * Get the list of missing results (if any) the query execution
	 * @param actualResultsFile Query file of actual results
	 * @param curResults current Results
	 * @return Fscores Precision, Recall, and F1 scores
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static ArrayList<String> getMissingResults(String actualResultsFile,ArrayList<String> curResults) throws QueryEvaluationException, RepositoryException, MalformedQueryException, IOException 
	{
		//ArrayList<String> curResults =  getCurrentResult(res) ;
		//System.out.println("current:"+ curResults);
		ArrayList<String> actualResults = getActualResultsFromFile(actualResultsFile) ;
		//System.out.println("actual:" +actualResults);
		//Set<String> diffSet = Sets.difference(actualResults, curResults);
		actualResults.removeAll(curResults);
		return actualResults;

	}

	//	public static float getPrecision(String queryNo,TupleQueryResult res) throws QueryEvaluationException, RepositoryException, MalformedQueryException 
	//	{
	//		
	//		float precision = 0 ;
	//		Set<String> curResults =  getCurrentResult(res) ;
	//		Set<String> actualResults = getActualResults(queryNo) ;
	//		Set<String> diffSet = Sets.difference(actualResults, curResults);
	//		precision = diffSet.size();
	//		System.out.println(diffSet);
	//		System.out.println(Sets.difference(curResults,actualResults));
	//		return precision;
	//		
	//	}
	/**
	 * Get the current results of the query execution
	 * @param res ResultSet iterator
	 * @return curResults List of results
	 * @throws QueryEvaluationException
	 */
	public static ArrayList<String> getCurrentResult(TupleQueryResult res) throws QueryEvaluationException 
	{
		List<String> bindingNames = res.getBindingNames();
		ArrayList<String> curResults = new ArrayList<String>() ;
		while (res.hasNext()) {
			BindingSet result = res.next();
			String recordLine ="";
			for(int i = 0 ; i < bindingNames.size(); i++)
			{
				String bindingName = bindingNames.get(i);
				try{
					String bindingVal = result.getBinding(bindingName).getValue().toString().replace("\"", "'").replaceAll("\n", " ");
					byte ptext[] = bindingVal.getBytes();
					bindingVal = new String(ptext, "UTF-8");
					if(i< bindingNames.size()-1)					
						recordLine = recordLine+bindingVal+"<===>";
					else
						recordLine = recordLine+bindingVal;	
				} catch (Exception e){
					String bindingVal ="\"null\"";
					if(i< bindingNames.size()-1)					
						recordLine = recordLine+bindingVal+"<===>";
					else
						recordLine = recordLine+bindingVal;	
				}
			}
			curResults.add(recordLine);
		}

		return curResults;
	}
	//	/**
	//	 * Get the actual result of the given query
	//	 * @param queryName Name of the query e.g. S1, B1 , C1 etc. 
	//	 * @return actualResults List of actual results
	//	 * @throws RepositoryException
	//	 * @throws MalformedQueryException
	//	 * @throws QueryEvaluationException
	//	 */
	//	private static ArrayList<String> getActualResults(String queryName) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
	//		ArrayList<String> actualResults = new ArrayList<String>() ;
	//		
	//		String queryString = "PREFIX bigrdfbench: <http://bigrdfbench.aksw.org/schema/> \n"
	//				+ "			Select ?names ?values \n"
	//				+ "			WHERE \n"
	//				+ "			{\n"
	//				+ "			?s bigrdfbench:queryName <http://aksw.org/bigrdfbench/query/"+queryName+">.\n"
	//				+ "			<http://aksw.org/bigrdfbench/query/"+queryName+"> bigrdfbench:bindingNames ?names.\n"
	//				+ "			<http://aksw.org/bigrdfbench/query/"+queryName+"> bigrdfbench:bindingValues ?values\n"
	//				+ "			}";
	//		 
	//	 	      TupleQuery tupleQuery = ResultsLoader.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
	//	 		  TupleQueryResult res = tupleQuery.evaluate();
	//	 		   while(res.hasNext())
	//	 		   {
	//	 			  BindingSet result = res.next();
	//	 		     String[] bindingNames =  result.getValue("names").stringValue().replace("\n", " ").replace("'", "\"").replace("[", "").replace("]", "").split("<===>");
	//	 		     String[] bindingValues =  result.getValue("values").stringValue().replace("\n", " ").replace("'", "\"").replace("[", "").replace("]", "").split("<===>");
	//	 		     String actualResult = "[";
	//	 		     for(int i=0 ; i < bindingNames.length;i++)
	//	 		     {
	//	 		    	if(i<bindingNames.length-1)
	//	 		    	 actualResult= actualResult+bindingNames[i]+"="+bindingValues[i]+"<===>";
	//	 		    	else
	//	 		    		actualResult= actualResult+bindingNames[i]+"="+bindingValues[i]+"]";
	//	 		     }
	//	 		     actualResults.add(actualResult);
	//	 		  //   System.out.println(actualResult);
	//	 		   }
	//	 		   System.out.println(actualResults.size());
	//		return actualResults;
	//	}

}
