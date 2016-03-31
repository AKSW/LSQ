package org.aksw.simba.dataset.lsq;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import org.aksw.simba.benchmark.encryption.EncryptUtils;
import org.aksw.simba.benchmark.log.operations.DateConverter.DateParseException;
import org.aksw.simba.benchmark.log.operations.LinkedGeoDataLogReader;
import org.aksw.simba.benchmark.log.operations.SesameLogReader;
import org.aksw.simba.benchmark.spin.Spin;
import org.aksw.simba.largerdfbench.util.QueryStatistics;
import org.aksw.simba.largerdfbench.util.Selectivity;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
/**
 * This is the main class used to RDFise query logs 
 * @author Saleem
 *
 */
public class LogRDFizer {
	public static BufferedWriter 	bw ;
	public  RepositoryConnection con = null;
	public static  BufferedWriter tobw= null;
	public static long queryNo = 1;
	public  static int  maxRunTime ;  //max query execution time in seconds
	public int runtimeErrorCount;
	public long endpointSize = 0;
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException, DateParseException {
		//String queryLogDir = "D:/QueryLogs/SWDF-Test/";  //dont forget last /
		 // String queryLogDir = "/home/MuhammadSaleem/dbpedia351logs/";
		  String queryLogDir = "D:/QueryLogs/USEWOD2014/data/LinkedGeoData/";
		// String queryLogDir = "D:/QueryLogs/RKBExplorer/";
				 
		 String acronym = "LGD" ; //  a short acronym of the dataset
		
		 String localEndpoint = "http://linkedgeodata.org/sparql";
		// String localEndpoint = "http://linkedgeodata.org/sparql";
		
		//String graph = "http://aksw.org/benchmark"; //Named graph. can be null
		String graph = "http://linkedgeodata.org"; //can be null
		//String graph = null;
		
		String outputFile = "Linked-SQ-LGD.ttl";
		//String outputFile = "LinkedDBpedia351SQL.ttl";
		//String outputFile = "Linked-SQ-DBpedia-Fixed.ttl";
		
		// String publicEndpoint = "http://data.semanticweb.org/sparql";
		//String publicEndpoint = "http://dbpedia.org/sparql";
		String publicEndpoint = "http://linkedgeodata.org/sparql";
		
		maxRunTime = 900;  //Max query runtime in seconds
		tobw = new BufferedWriter(new FileWriter("timeOutQueries.txt")); // the location where time out queries will be stored
		String separator = "- -";   // this is separator which separates the agent ip (encrypted) and corresponding exe time. can be null if there is no user I.P provided in log
		//String separator = null;  //null is when IP is missing. like in BM
		
		//SesameLogReader slr = new SesameLogReader();
		// DBpediaLogReader dblr = new DBpediaLogReader();
		//RKBExplorerLogReader rkblr = new RKBExplorerLogReader();
		LinkedGeoDataLogReader lglr = new LinkedGeoDataLogReader();  //here is your parser class
		
		LogRDFizer rdfizer = new LogRDFizer();
		
		//Map<String, Set<String>> queryToSubmissions = slr.getSesameQueryExecutions(queryLogDir);  // this map contains a query as key and their all submissions. 
		//Note submission is combination  of  three strings: hashed I.P + separator String+ Exectuion time in xsd:dateTimeFormat. Note the the dateTime format is strict. 
	//	Map<String, Set<String>> queryToSubmissions = dblr.getVirtuosoQueryExecutions(queryLogDir);  // this map contains a query as key and their all submissions
	//	Map<String, Set<String>> queryToSubmissions = rkblr.getBritishMuseumQueryExecutions(queryLogDir); 
		Map<String, Set<String>> queryToSubmissions = lglr.getVirtuosoQueryExecutions(queryLogDir);
		
		System.out.println(queryToSubmissions.keySet().size());
		System.out.println("Number of Distinct queries: " +  queryToSubmissions.keySet().size());
		//-----Once you get the query to submissions map by parsing the log then the below method is general---
		rdfizer.rdfizeLog(queryToSubmissions,localEndpoint,publicEndpoint,graph,outputFile,separator,acronym);
		System.out.println("Dataset stored at " + outputFile);
	}
	/**
	 * RDFize Log	
	 * @param queryToSubmissions A map which store a query string (single line) as key and all the corresponding submissions as List. Where a submission is a combination
	 * of User encrypted ip and the data,time of the query request. The I.P and the time is separated by a separator
	 * @param localEndpoint Endpoint which will be used for feature generation
	 * @param publicEndpoint Public endpoint of the log
	 * @param graph named Graph, can be null
	 * @param outputFile The output RDF file
	 * @param separator Submission separator. Explained above
	 * @param acronym A Short acronym of the dataset log, e.g., DBpedia or SWDF
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws ParseException 
	 */
	public void rdfizeLog(Map<String, Set<String>> queryToSubmissions, String localEndpoint, String publicEndpoint, String graph, String outputFile, String separator, String acronym) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException {
		System.out.println("RDFization started...");
		endpointSize = Selectivity.getEndpointTotalTriples(localEndpoint, graph);
		long parseErrorCount =0;
		bw = new BufferedWriter(new FileWriter(outputFile));
		this.writePrefixes(acronym);
	  for(String queryStr: queryToSubmissions.keySet())
		{
		    System.out.println(queryNo+" Started...");
			//bw.write("\nlsqv:LinkedSQL  lsqv:hasLogOf    lsqrd:q-"+queryNo+ " . \n");
			bw.write("lsqrd:q"+queryNo+ " lsqv:endpoint <" + publicEndpoint + "> ; \n");
			bw.write(" sp:text \""+queryStr+"\" ; \n"); 
			queryNo++;
			Query query =  new Query();
			try{	
				 query = QueryFactory.create(queryStr);
			}
			catch (Exception ex){
				String parseError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
				bw.write(" lsqv:parseError \""+parseError+ "\" . ");
				String queryStats = this.getRDFUserExecutions(queryToSubmissions.get(queryStr),separator);
				bw.write(queryStats);
				parseErrorCount++;}
			try{
				if(query.isDescribeType())
					this.RDFizeDescribe(query,localEndpoint,graph,queryToSubmissions.get(queryStr),separator);
				else if (query.isSelectType())
					this.RDFizeSelect(query,localEndpoint,graph,queryToSubmissions.get(queryStr),separator);
				else if (query.isAskType())
					this.RDFizeASK(query,localEndpoint,graph,queryToSubmissions.get(queryStr),separator);
				else if (query.isConstructType())
					this.RDFizeConstruct(query,localEndpoint,graph,queryToSubmissions.get(queryStr),separator);
			}
			catch(Exception ex){}
		}
		bw.close();
		System.out.println("Total Number of Queries with Parse Errors: " + parseErrorCount);	
		System.out.println("Total Number of Queries with Runtime Errors: " + runtimeErrorCount);	
	}

	/**
	 * RDFized SELECT query
	 * @param query Query
	 * @param localEndpoint Local endpoint
	 * @param graph Named Graph, can be null
	 * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
	 * @param separator Separator string between I.P and execution time
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws ParseException 
	 * @throws QueryEvaluationException 
	 */
	public void RDFizeSelect(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
		String queryStats ="";
		long curTime = System.currentTimeMillis();
		try {
			Query queryNew = SesameLogReader.removeNamedGraphs(query);
			long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
			long exeTime = System.currentTimeMillis() - curTime ;
			//queryStats =queryStats+" lsqv:queryType \"SELECT\" ; " ;
			//queryStats =queryStats+" lsqv:hasFeatures lsqrd:f-q"+(queryNo-1)+" . \n " ;
			//queryStats =queryStats+" lsqv:hasClauses clause:q"+(queryNo-1)+" . \n" ;
			//queryStats = queryStats +"lsqrd:f-q"+(queryNo-1);
			queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
			queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" ; ";
			queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
			bw.write(queryStats);

		} catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
		bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
		runtimeErrorCount++; }
		queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
		bw.write(queryStats);
		queryStats = this.getRDFUserExecutions(submissions,separator);
		bw.write(queryStats);
		queryStats = this.getSpinRDFStats(query);
		bw.write(queryStats);
	}
	/**
	 * RDFized DESCRIBE query
	 * @param query Query
	 * @param localEndpoint Local endpoint
	 * @param graph Named Graph, can be null
	 * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
	 * @param separator Separator string between I.P and execution time
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws ParseException 
	 * @throws QueryEvaluationException 
	 */
	public void RDFizeDescribe(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
		String queryStats ="";
		long curTime = System.currentTimeMillis();
		try {
			Query queryNew = SesameLogReader.removeNamedGraphs(query);
			long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"describe");
			long exeTime = System.currentTimeMillis() - curTime ;
			//queryStats =queryStats+" lsqv:queryType \"DESCRIBE\" ; " ;
			//queryStats =queryStats+" lsqv:hasFeatures lsqrd:f-q"+(queryNo-1)+" . \n" ;
			//queryStats =queryStats+" lsqv:hasClauses clause:q"+(queryNo-1)+" . \n" ;
			//queryStats = queryStats +"lsqrd:f-q"+(queryNo-1);
			queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
			queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" ; ";
			queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
			bw.write(queryStats);

		} catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
		bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
		runtimeErrorCount++; }
		queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
		bw.write(queryStats);
		queryStats = this.getRDFUserExecutions(submissions,separator);
		bw.write(queryStats);
		queryStats = this.getSpinRDFStats(query);
		bw.write(queryStats);
	}
	/**
	 * RDFized CONSTRUCT query
	 * @param query Query
	 * @param localEndpoint Local endpoint
	 * @param graph Named Graph, can be null
	 * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
	 * @param separator Separator string between I.P and execution time
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws ParseException 
	 * @throws QueryEvaluationException 
	 */
	public void RDFizeConstruct(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
		String queryStats ="";
		long curTime = System.currentTimeMillis();
		try {
			Query queryNew = SesameLogReader.removeNamedGraphs(query);
			long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"construct");
			long exeTime = System.currentTimeMillis() - curTime ;
			//queryStats =queryStats+" lsqv:queryType \"CONSTRUCT\" ; " ;
			//queryStats =queryStats+" lsqv:hasFeatures lsqrd:f-q"+(queryNo-1)+" . \n " ;
			//queryStats =queryStats+" lsqv:hasClauses clause:q"+(queryNo-1)+" . \n" ;
			//queryStats = queryStats +"lsqrd:f-q"+(queryNo-1);
			queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
			queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" ; ";
			queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
			bw.write(queryStats);

		} catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
		bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
		runtimeErrorCount++; }
		queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
		bw.write(queryStats);
		queryStats = this.getRDFUserExecutions(submissions,separator);
		bw.write(queryStats);
		queryStats = this.getSpinRDFStats(query);
		bw.write(queryStats);
	}
	/**
	 * RDFized ASK query
	 * @param query Query
	 * @param localEndpoint Local endpoint
	 * @param graph Named Graph, can be null
	 * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
	 * @param separator Separator string between I.P and execution time
	 * @throws IOException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws ParseException 
	 * @throws QueryEvaluationException 
	 */
	public void RDFizeASK(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
		String queryStats ="";
		long curTime = System.currentTimeMillis();
		try {
			Query queryNew = SesameLogReader.removeNamedGraphs(query);
			long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"ask");
			long exeTime = System.currentTimeMillis() - curTime ;
			//queryStats =queryStats+" lsqv:queryType \"ASK\" ; " ;
			//queryStats =queryStats+" lsqv:hasFeatures lsqrd:f-q"+(queryNo-1)+" . \n " ;
			//queryStats =queryStats+" lsqv:hasClauses clause:q"+(queryNo-1)+" . \n" ;
			//queryStats = queryStats +"lsqrd:f-q"+(queryNo-1);
			queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
			queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" ; ";
			queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
			bw.write(queryStats);

		} catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
		bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
		runtimeErrorCount++; }
		queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
		bw.write(queryStats);
		queryStats = this.getQueryTuples(query);
		queryStats = this.getRDFUserExecutions(submissions,separator);
		bw.write(queryStats);
		queryStats = this.getSpinRDFStats(query);
		bw.write(queryStats);
	}
	public String getQueryTuples(Query query) {
		
		return null;
	}
	/**
	 * Get all executions (IP,Time) of the given query
	 * @param query Query
	 * @param submissions  Query submissions in form of IP:Time
	 * @param separator String separator between IP:Time 
	 * @return Stats
	 * @throws ParseException 
	 */
	public String getRDFUserExecutions(Set<String> submissions, String separator) throws ParseException {
		String queryStats = "\nlsqrd:q"+(LogRDFizer.queryNo-1);
		queryStats = queryStats+ " lsqv:execution ";
		int subCount = 1;
		for(int i=0; i<submissions.size();i++)
		{
			if(i<submissions.size()-1)
			{
				queryStats = queryStats + "lsqrd:q"+(queryNo-1)+"-e"+subCount+ " , ";
			}
			else
			{
				queryStats = queryStats + "lsqrd:q"+(queryNo-1)+"-e"+subCount+ " . \n ";
			}
			subCount++;
		}
		int j = 1;
		if(!(separator==null))  //i.e both I.P and Time is provided in log
		{
		for(String submission:submissions)
		{
			String prts [] = submission.split(separator);
			String txt=prts[0].replace(".", "useanystring") ; // of course we used different one
			String key="what is your key string?";  //of course we use different key in LSQ. 
			txt=EncryptUtils.xorMessage( txt, key );
			String encoded=EncryptUtils.base64encode( txt ); 
			encoded = encoded.replace("=", "-");
			encoded = encoded.replace("+", "-");
		   	queryStats = queryStats + "lsqrd:q"+(queryNo-1)+"-e"+j+ " lsqv:agent lsqr:A-"+encoded+"  ; dct:issued \""+prts[1]+"\"^^xsd:dateTimeStamp . \n";
			j++;
		}
		}
		else  //only exe time is stored
		{
			for(String submission:submissions)
			{
				queryStats = queryStats + "lsqrd:q"+(queryNo-1)+"-e"+j+ " dct:issued \""+submission+"\"^^xsd:dateTimeStamp . \n";
				j++;
			}
			
		}
		return queryStats;

	}

	/**
	 * Get Spin RDF stats of the qiven query
	 * @param query Query
	 * @return Spin Stas
	 * @throws IOException
	 */
	private String getSpinRDFStats(Query query) throws IOException {
		String queryStats = "";
		try {
			Spin sp = new Spin();
			String spinQuery = sp.getSpinRDF(query.toString(), "Turtle");
			String prefix = spinQuery.substring(0,spinQuery.indexOf("[")-1);
			String body = spinQuery.substring(spinQuery.indexOf("[")+1,spinQuery.lastIndexOf("]"));
			spinQuery = prefix+" lsqrd:q"+(queryNo-1)+"  "+ body;
			//queryStats = queryStats+ "lsqrd:q"+(LogRDFizer.queryNo-1);
			//queryStats = queryStats+" lsqv:spinQuery lsqrd:q"+(queryNo-1)  +" . \n";
			queryStats = queryStats+spinQuery +" . \n";	

		} catch (Exception ex) {
			String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
			bw.write(" lsqrd:q"+(queryNo-1)+" lsqv:spinError \""+runtimeError+ "\" . "); }
		return queryStats;
	}

	/**
	 * Get result size of the given query
	 * @param queryStr Query
	 * @param localEndpoint Endpoint url where this query has to be executed
	 * @param sesameQueryType Query type {SELECT, ASK, CONSTRUCT, DESCRIBE}
	 * @return ResultSize
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws IOException
	 */
	public long getQueryResultSize(String queryStr, String localEndpoint,String sesameQueryType) throws RepositoryException, MalformedQueryException, IOException
	{
		long totalSize = -1;
		this.initializeRepoConnection(localEndpoint);
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

			} catch (QueryEvaluationException ex) { 
				String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
				if(runtimeError.length()>1000)  //this is to avoid sometime too big errors
					runtimeError = "Unknown runtime error";
				bw.write(" lsqv:runtimeError \""+runtimeError+ "\" ; ");
				runtimeErrorCount++;
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
			} catch (QueryEvaluationException ex) {
				String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
				if(runtimeError.length()>1000)  //this is to avoid sometime too big errors
					runtimeError = "Unknown runtime error";
				bw.write(" lsqv:runtimeError \""+runtimeError+ "\" ; ");
				runtimeErrorCount++;
			}

		}
		con.close();
		return totalSize;
	}
	/**
	 * Write RDF Prefixes
	 * @param acronym Acronym of the dataset e.g. DBpedia or SWDF
	 * @throws IOException
	 */
	public void writePrefixes(String acronym) throws IOException {
		bw.write("@prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n");
		bw.write("@prefix lsqr:<http://lsq.aksw.org/res/> . \n");
		bw.write("@prefix lsqrd:<http://lsq.aksw.org/res/"+acronym+"-> . \n");
		bw.write("@prefix lsqv:<http://lsq.aksw.org/vocab#> . \n");
		bw.write("@prefix sp:<http://spinrdf.org/sp#> . \n");
		bw.write("@prefix void:<http://rdfs.org/ns/void#> . \n");
	    bw.write("@prefix dct:<http://purl.org/dc/terms/> . \n");
	    bw.write("@prefix xsd:<http://www.w3.org/2001/XMLSchema#> . \n");
	    bw.write("@prefix sd:<http://www.w3.org/ns/sparql-service-description#> . \n\n");
	    	    

	}
		
	/**
	 * Initialize repository for a SPARQL endpoint
	 * @param endpointUrl Endpoint Url
	 * @throws RepositoryException
	 */
	public void initializeRepoConnection(String endpointUrl) throws RepositoryException {
		Repository repo = new SPARQLRepository(endpointUrl);
		repo.initialize();
		con = repo.getConnection();

	}

}