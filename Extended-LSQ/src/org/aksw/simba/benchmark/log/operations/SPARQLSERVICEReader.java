package org.aksw.simba.benchmark.log.operations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.largerdfbench.util.QueryStatistics;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class SPARQLSERVICEReader {

	public static void main(String[] args) throws IOException, MalformedQueryException, RepositoryException, QueryEvaluationException {
		String inputFile = "D:/QueryLogs/SPARQL11Logs/service_logs_bio2rdf.txt";
		String outputFile = "D:/QueryLogs/SPARQL11Logs/service_logs_bio2rdf_output.txt";
		Set<String> queries = getLogQueries(inputFile);

		System.out.println(queries.size());
	//	writeCleanSERVICEQueries(queries, outputFile);

	}

	private static void writeCleanSERVICEQueries(Set<String> queries,String outputFile) throws IOException, MalformedQueryException, RepositoryException, QueryEvaluationException {

		BufferedWriter	bw= new BufferedWriter(new FileWriter(outputFile));
		int count = 0;
		String queryStats ="";
		for(String queryStr:queries){
			
				//System.out.println(queryStr);
				Query query = QueryFactory.create(queryStr);
				queryStats = "#-------------------------------------------------------\nQuery No: "+count+"\n";  //new query identifier
				if(query.isSelectType())
					queryStats = queryStats + "Query Type: SELECT \n";
				else if(query.isConstructType())
					queryStats = queryStats + "Query Type: CONSTRUCT \n";
				else if(query.isAskType())
					queryStats = queryStats + "Query Type: ASK \n";
				else
					queryStats = queryStats + "Query Type: DESCRIBE \n";
		
            try{
			queryStats = queryStats + "Results Size: -1\n";
			queryStats = queryStats+QueryStatistics.getQueryStats(queryStr,"http:dumy.com/sparql",null,-1);
			queryStats = queryStats+"Query Execution Time (ms): -1\n";
			queryStats = queryStats+"Query String: "+java.net.URLEncoder.encode(queryStr, "UTF-8")+"\n";
			//System.out.println(queryStats);
			bw.write(queryStats);
			System.out.println(count+ ": written...");
			count++;
            }
            catch(Exception ex){queryStats="";};
			
		}

	}

	public static Set<String> getLogQueries(String file) throws IOException  {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		long parseErrorCount =0;
		Set<String> queries = new HashSet<String> ();
		BufferedWriter	bw= new BufferedWriter(new FileWriter("D:/workspace/FEASIBLE/benchmarks/Bio2RDF_Log_SERVICE_Queries.txt"));
		while ((line = br.readLine()) != null)
		{	
			String [] prts = line.split("<sep>");
			//System.out.println(prts[2]);
			try{
				Query query = QueryFactory.create(prts[2]);
				queries.add(prts[2]);
				
				bw.write(prts[2]+"\n");
			}
			catch (Exception ex){parseErrorCount++;}
		}
		System.out.println(parseErrorCount);
		bw.close();
		return queries;
	}
}
