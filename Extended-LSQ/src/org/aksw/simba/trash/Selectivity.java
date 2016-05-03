package org.aksw.simba.trash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.benchmark.log.operations.CleanQueryWriter;
import org.aksw.simba.lsq.core.LogRDFizer;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
/**
 * Dataset-restricted Triple pattern selectivities. 
 * @author Saleem
 *
 */
public class Selectivity {

	public static RepositoryConnection con = null;
	public static HashMap<String,Double> tpSelCache = new HashMap<String,Double>();
	public static int maxRunTime ;  //max query execution time in seconds
	public static BufferedWriter toBW= null;
	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		String inputDir= "../BigRDFBench-Utilities/queries/";
		String endpoint = "http://localhost:8890/sparql";
		String graph = null; //can be null
		long endpointSize = getEndpointTotalTriples(endpoint,graph);
		System.out.println("Dataset size: " + endpointSize);
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();
		long count = 1; 
		for (File qryFile : listOfFiles)
		{	
			BufferedReader br = new BufferedReader(new FileReader(inputDir+qryFile.getName()));
			String line;
			String queryStr="";
			while ((line = br.readLine()) != null) {
				queryStr= queryStr+" "+line;
			}
			br.close();

			System.out.println("--------------------------------------------------------------\n"+count+ ": "+qryFile.getName()+" Query: " + queryStr);
			//Query jenaQuery = QueryFactory.create(queryStr);
			//System.out.println(jenaQuery.getProject());
			long queryResultSize = getQueryResultSize(queryStr,endpoint,"tuple");
			System.out.println(queryResultSize);
			double meanSel=	getMeanTriplePatternSelectivity(queryStr,endpoint,graph, endpointSize);

			System.out.println("Mean triple patterns selectivity: " + meanSel);
			count++;
		}

	}
	/**
	 * Get the result Size of the query
	 * @param queryStr SPARQL query
	 * @param endpoint SPARQL endpoint
	 * @param sesameQueryType Query Type (ask, select, describe, construct )
	 * @return totalSize Result size
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException 
	 */
	public static long getQueryResultSize(String queryStr, String endpoint, String sesameQueryType) throws RepositoryException, MalformedQueryException, IOException {
		long totalSize = -1;
		initializeRepoConnection(endpoint);
		if(sesameQueryType.equals("select") || sesameQueryType.equals("ask") )
		{
			try {
				if (sesameQueryType.equals("select"))
				{
					TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,queryStr );
					//System.out.println(queryStr);
					tupleQuery.setMaxQueryTime(maxRunTime);
					TupleQueryResult res;
					res = tupleQuery.evaluate();
					//System.out.println(res);
					totalSize = 0;
					while(res.hasNext())
					{
						res.next();
						totalSize++;
					}
				}
				else
				{
					BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL,queryStr );
					//System.out.println(queryStr);
					booleanQuery.setMaxQueryTime(maxRunTime);
					booleanQuery.evaluate();
					//System.out.println(res);
					totalSize = 1;

				}

			} catch (QueryEvaluationException e) {// System.out.println(queryStr);
				if(e.getMessage().contains("Transaction timed out")){
					totalSize=-2;
					//System.out.println("Yes");
					try{
						toBW.write(CleanQueryWriter.encodeQuery(queryStr));	
					}
					catch(Exception e1){System.out.println(queryStr);}
				}
			}
		}
		else
		{
			try {
				GraphQuery gq = con.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
				gq.setMaxQueryTime(maxRunTime);
				GraphQueryResult graphResult = gq.evaluate();
				totalSize = 0;
				while (graphResult.hasNext()) 
				{
					graphResult.next();
					totalSize++;
				}
			} catch (QueryEvaluationException e) {
				if(e.getMessage().contains("Transaction timed out")){
					try{
						toBW.write(CleanQueryWriter.encodeQuery(queryStr));	
					}
					catch(Exception e1){System.out.println(queryStr);}
				}
			}

		}
		con.close();
		return totalSize;

	}
	//	/**
	//	 * Load endpoint to results map from a file containing endpoint urls and corresponding results. This is good for big datasets where on-the-fly total results calculation need a lot of time. 
	//	 * @param file File containing resultset sizes of endpoints
	//	 * @throws IOException 
	//	 */
	//	public static void loadEPtoRSfromFile(String file) throws IOException {
	//		BufferedReader br = new BufferedReader(new FileReader(file));
	//		String line;
	//		while ((line = br.readLine()) != null) {
	//			String prts [] = line.split("\t");
	//			//System.out.println(prts[0]);
	//			// System.out.println(prts[1]);
	//			epToResults.put(prts[0],prts[1]);
	//		}
	//		br.close();	
	//	}
	//	/**
	//	 * Load those triple patterns whose selectivity is depended upon Filter clause into a set
	//	 * @param file File containing filter information. note: strict formating required
	//	 * @throws IOException
	//	 */
	//	public static void loadStatementFilters(String file) throws IOException {
	//		BufferedReader br = new BufferedReader(new FileReader(file));
	//		String line;
	//		while ((line = br.readLine()) != null) {
	//			String prts [] = line.split("\t");
	//			//System.out.println(prts[0]);
	//			// System.out.println(prts[1]);
	//			stmtFilters.put(prts[0],prts[1]);
	//		}
	//		br.close();
	//
	//	}
	/**
	 * get Mean Triple pattern selectivity
	 * @param query SPARQL query
	 * @param endpointSize Total number of triples patterns in the endpoint
	 * @param endpoint Endpoint Url
	 * @param graph Default graph can be null
	 * @return meanQrySel Mean triple pattern selectivity across complete query
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 */
	public static double getMeanTriplePatternSelectivity(String query, String endpoint,String graph, Long endpointSize) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		//List<Double> meanTPSelectivities = new ArrayList<Double>()  ;
		//System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
		long totalTriplePatterns = 0;
		double meanQrySel =0 ; //mean of the avg triple pattern selectivity of all the triple patterns in a query
		for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
		{
			List<StatementPattern>	 stmts =  bgpGrps.get(DNFkey);
			totalTriplePatterns = totalTriplePatterns + stmts.size();
			for (StatementPattern stmt : stmts) 
			{		
				String sbjVertexLabel, objVertexLabel, predVertexLabel;
				sbjVertexLabel = getSubjectVertexLabel(stmt);
				predVertexLabel = getPredicateVertexLabel(stmt);
				objVertexLabel = getObjectVertexLabel(stmt);
				String tp = sbjVertexLabel+"_"+predVertexLabel+"_"+objVertexLabel;
				double tpSel;
				if(tpSelCache.containsKey(tp))
				{
					tpSel = tpSelCache.get(tp);
					//System.out.println("TPSel");
				}
				else
				{
					tpSel = getTriplePatternSelectivity(stmt,tp,endpoint,graph,endpointSize);
					tpSelCache.put(tp, tpSel);
				//	System.out.println(tp + "  " +tpSel );
				}
				meanQrySel  = meanQrySel+ tpSel;
				//meanTPSelectivities.add(tpSel);
				//System.out.println("Average (across all datasets) Triple pattern selectivity: "+ meanTripleSel);
			}
		}
		if(totalTriplePatterns==0)
			meanQrySel =0;
		else
			meanQrySel = meanQrySel/totalTriplePatterns;
		con.close();
		return meanQrySel;
		//System.out.println("\nMean query selectivity (average of the mean triple pattern selectivities): " + meanQrySel);
		// System.out.println(meanTPSelectivities);
		//	double stdv = getStandardDeviation(meanTPSelectivities,meanQrySel);
		//	System.out.println("Query Selectivities standard deviation: " + stdv );
		//	System.out.println("Triple Patterns: " +totalTriplePatterns);
	}
	
	/**
	 * Get Triple pattern selectivity String
	 * @param query SPARQL query
	 * @param endpointSize Total number of triples patterns in the endpoint
	 * @param endpoint Endpoint Url
	 * @param graph Default graph can be null
	 * @return meanQrySel Mean triple pattern selectivity across complete query
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException 
	 * @throws RepositoryException 
	 */
	public static String getTriplePatternSelectivity(String query, String endpoint,String graph, Long endpointSize) throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
		//List<Double> meanTPSelectivities = new ArrayList<Double>()  ;
		//System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
		long totalTriplePatterns = 0;
		String stats = "";
		double meanQrySel =0 ; //mean of the avg triple pattern selectivity of all the triple patterns in a query
		int tpNo = 1;
		for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
		{
			List<StatementPattern>	 stmts =  bgpGrps.get(DNFkey);
			totalTriplePatterns = totalTriplePatterns + stmts.size();
			for (StatementPattern stmt : stmts) 
			{		
				String sbjVertexLabel, objVertexLabel, predVertexLabel;
				sbjVertexLabel = getSubjectVertexLabel(stmt);
				predVertexLabel = getPredicateVertexLabel(stmt);
				objVertexLabel = getObjectVertexLabel(stmt);
				String tp = sbjVertexLabel+"_"+predVertexLabel+"_"+objVertexLabel;
				String spText = sbjVertexLabel+"  "+predVertexLabel+"  "+objVertexLabel;
				double tpSel;
				if(tpSelCache.containsKey(tp))
				{
					tpSel = tpSelCache.get(tp);
					//System.out.println("TPSel");
					stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+ " lsqv:hasTriplePattern lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+ " . \n";
					stats = stats+" lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+" lsqv:triplePatternText \""+spText +"\" ; ";
					stats = stats+ " lsqv:triplePatternSelectivity "+tpSel +" . ";
			
				}
				else
				{
					tpSel = getTriplePatternSelectivity(stmt,tp,endpoint,graph,endpointSize);
					tpSelCache.put(tp, tpSel);
				//	System.out.println(tp + "  " +tpSel );
					stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+ " lsqv:hasTriplePattern lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+ " . \n";
					stats = stats+" lsqr:q"+LogRDFizer.queryHash+"-p"+ tpNo+" lsqv:triplePatternText \""+spText +"\" ; ";
					stats = stats+ " lsqv:triplePatternSelectivity "+tpSel +" . ";
				}
				meanQrySel  = meanQrySel+ tpSel;
				tpNo++;
				//meanTPSelectivities.add(tpSel);
				//System.out.println("Average (across all datasets) Triple pattern selectivity: "+ meanTripleSel);
			}
		}
		if(totalTriplePatterns==0)
			meanQrySel =0;
		else
			meanQrySel = meanQrySel/totalTriplePatterns;
		con.close();
		stats = stats + "\nlsqr:le-"+LogRDFizer.acronym+"-q"+LogRDFizer.queryHash+" lsqv:meanTriplePatternSelectivity "+meanQrySel+" ;  ";
		
		return stats;
		//System.out.println("\nMean query selectivity (average of the mean triple pattern selectivities): " + meanQrySel);
		// System.out.println(meanTPSelectivities);
		//	double stdv = getStandardDeviation(meanTPSelectivities,meanQrySel);
		//	System.out.println("Query Selectivities standard deviation: " + stdv );
		//	System.out.println("Triple Patterns: " +totalTriplePatterns);
	}
	/**
	 * Get dataset/endpoint total number of triples
	 * @param endpoint SPARQL endpoint
	 * @param graph Named Graph, can be null
	 * @return totalSize Total size
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static long getEndpointTotalTriples(String endpoint, String graph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		long totalSize = 0;
		initializeRepoConnection(endpoint);
		String queryStr ="";
		if(graph==null)
			queryStr = "SELECT count(?s) AS ?totalSize WHERE {?s ?p ?o}";
		else
			queryStr = "SELECT count(?s) AS ?totalSize FROM <"+graph+"> WHERE {?s ?p ?o}";
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,queryStr );
		TupleQueryResult res = tupleQuery.evaluate();
		while(res.hasNext())
			totalSize = Long.parseLong(res.next().getValue("totalSize").stringValue().toString());
		con.close();
		return totalSize;
	}
	//	/**
	//	 * Calculate standard deviation of given set of selectivities
	//	 * @param meanTPSelectivities Set of mean triple patterns selectivities
	//	 * @param mean Average of selectivities
	//	 * @return Standard Deviation
	//	 */
	//	public static double getStandardDeviation(List<Double> meanTPSelectivities, double mean) {
	//
	//		// sd is sqrt of sum of (values-mean) squared divided by n - 1
	//		// Calculate the mean
	//		//  double mean = 0;
	//		final int n = meanTPSelectivities.size();
	//		if ( n < 2 )
	//		{
	//			return Double.NaN;
	//		}
	//		// calculate the sum of squares
	//		double sum = 0;
	//		for ( int i=0; i<n; i++ )
	//		{
	//			final double v = meanTPSelectivities.get(i) - mean;
	//			sum += v * v;
	//		}
	//		// Change to ( n - 1 ) to n if you have complete data instead of a sample.
	//		return Math.sqrt( sum / ( n - 1 ) );
	//	}
	/**
	 * Get Triple pattern selectivity
	 * @param stmt Statement pattern
	 * @param tp  triples representation of statement pattern
	 * @param endpoint Endpoint Url
	 * @param graph Default graph can be null
	 * @param endpointSize Total number of triples in the endpoint
	 * @return Selectivity of the filtered triple pattern
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static double getTriplePatternSelectivity(StatementPattern stmt,
			String tp, String endpoint, String graph, long endpointSize) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		double selectivity = 0 ;
		long resultSize = 0;
		if (stmt.getSubjectVar().getValue()==null && stmt.getPredicateVar().getValue()==null && stmt.getObjectVar().getValue()==null )
			selectivity = 1; 
		else
		{
			String queryString = getQueryString(stmt,tp,graph);
			//System.out.println("\nTriple pattern: " + triplePattern );
			//System.out.println(queryString);
			initializeRepoConnection(endpoint);
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			try{
			TupleQueryResult res = tupleQuery.evaluate();
			while(res.hasNext())
				resultSize = Long.parseLong(res.next().getValue("size").stringValue().toString());
			}
			catch(Exception ex) {}
			if(resultSize>0)
				selectivity = (double) resultSize/ endpointSize;
		}
		return selectivity;
	}
	/**
	 * Get triple pattern from statement pattern
	 * @param stmt Statement pattern
	 * @return triplePattern Triple pattern
	 */
	public static String getTriplePattern(StatementPattern stmt) {
		String subject = getSubject(stmt);
		String object = getObject(stmt);
		String predicate = getPredicate(stmt);
		String triplePattern = subject + predicate + object ;
		return triplePattern;
	}
	/**
	 * Get query to the count of results against triple pattern
	 * @param stmt Triple pattern
	 * @param tp Triple pattern as key for stmtFilters hash map. For checking whether stmt should contain Filter clause as well
	 * @param graph Default graph can be null
	 * @return query SPARQL query
	 */
	public static String getQueryString(StatementPattern stmt, String tp, String graph) {
		String query = "";
		String triplePattern = getTriplePattern(stmt);
		if(graph==null)
			query ="SELECT COUNT(*) AS ?size WHERE { "+triplePattern +"} ";
		else
			query = "SELECT COUNT(*) AS ?size FROM <"+graph+"> WHERE { "+triplePattern +"} ";

		return query;

	}

	/**
	 * Get Predicate from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getPredicate(StatementPattern stmt) {
		String tuple;
		if (stmt.getPredicateVar().getValue()!=null)
			tuple = " <"+stmt.getPredicateVar().getValue().stringValue()+"> ";
		else{
			tuple =" ?"+stmt.getPredicateVar().getName(); 
			if(tuple.contains("-"))
			{
				String prts [] = tuple.split("-");
				tuple = prts[0];
			}
		}
		return tuple;
	}
	/**
	 * Get object from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getObject(StatementPattern stmt) {
		String tuple;
		if (stmt.getObjectVar().getValue()!=null && (stmt.getObjectVar().getValue().toString().startsWith("http://") || stmt.getObjectVar().getValue().toString().startsWith("ftp://")))
			tuple = " <"+stmt.getObjectVar().getValue().stringValue()+"> ";
		else if (stmt.getObjectVar().getValue()!=null)
			tuple = " '"+stmt.getObjectVar().getValue().stringValue()+"' ";
		else{
			tuple =" ?"+stmt.getObjectVar().getName(); 
			if(tuple.contains("-"))
			{
				String prts [] = tuple.split("-");
				tuple = prts[0];
			}
		}
		return tuple;
	}
	/**
	 * Get subject from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getSubject(StatementPattern stmt) {
		String tuple;
		if (stmt.getSubjectVar().getValue()!=null )
			tuple = "<"+stmt.getSubjectVar().getValue().stringValue() + "> ";
		else if (stmt.getSubjectVar().getValue()!=null )
			tuple = "'"+stmt.getSubjectVar().getValue().stringValue() + "' ";
		else{
			tuple ="?"+stmt.getSubjectVar().getName(); 
			if(tuple.contains("-"))
			{
				String prts [] = tuple.split("-");
				tuple = prts[0];
			}
		}
		return tuple;
	}

	/**
	 * Get label for the object vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getObjectVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getObjectVar().getValue()!=null)
			label = stmt.getObjectVar().getValue().stringValue();
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
			label = stmt.getPredicateVar().getValue().stringValue();
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
			label = stmt.getSubjectVar().getValue().stringValue();
		else
			label =stmt.getSubjectVar().getName(); 
		return label;

	}

	/**
	 * Initialize repository for a SPARQL endpoint
	 * @param endpointUrl Endpoint Url
	 * @throws RepositoryException
	 */
	public static void initializeRepoConnection(String endpointUrl) throws RepositoryException {
		Repository repo = new SPARQLRepository(endpointUrl);
		repo.initialize();
		con = repo.getConnection();

	}
}