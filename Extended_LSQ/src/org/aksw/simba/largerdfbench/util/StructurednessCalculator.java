package org.aksw.simba.largerdfbench.util;

import java.util.HashSet;
import java.util.Set;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;


/**
 * This class is not used in FEASIBLE Benchmark
 * Calculate the structuredness or coherence of a dataset as defined in Duan et al. paper titled "Apples and Oranges: A Comparison of
   RDF Benchmarks and Real RDF Datasets". The structuredness is measured in interval [0,1]  with values close to 0 corresponding to low structuredness, and
   1 corresponding to perfect structuredness. The paper concluded that synthetic datasets have high structurenes as compared to real datasets.
 * @author Saleem
 *
 */
public class StructurednessCalculator {

	public static RepositoryConnection con = null;
	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		String endpointUrl = "http://localhost:8890/sparql";
		String namedGraph = "http://aksw.org/feasible"; 
		double coherence = getStructurednessValue(endpointUrl, namedGraph);
		System.out.println("\nOverall Structuredness or Coherence: " + coherence);

	}

	/**
	 * Get the structuredness/coherence value [0,1] of a dataset
	 * @param endpointUrl SPARQL endpoint URL
	 * @param namedGraph Named Graph of dataset. Can be null, in that case all named graphs will be considered
	 * @return structuredness Structuredness or coherence value
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static double getStructurednessValue(String endpointUrl,
			String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		initializeRepoConnection (endpointUrl);
		Set<String> types = getRDFTypes(namedGraph);
		System.out.println("Total rdf:types: " + types.size());
		double weightedDenomSum = getTypesWeightedDenomSum(types,namedGraph);
		double structuredness = 0;
		long count = 1;
		for(String type:types)
		{
			long occurenceSum = 0;
			Set<String> typePredicates = getTypePredicates(type,namedGraph);
			long typeInstancesSize = getTypeInstancesSize(type,namedGraph);
			//System.out.println(typeInstancesSize);
			//System.out.println(type+" predicates: "+typePredicates);
			//System.out.println(type+" : "+typeInstancesSize+" x " + typePredicates.size());
			for (String predicate:typePredicates)
			{
				long predicateOccurences = getOccurences(predicate,type,namedGraph);
				occurenceSum = (occurenceSum + predicateOccurences);
				//System.out.println(predicate+ " occurences: "+predicateOccurences);
				//System.out.println(occurenceSum);
			}

			double denom = typePredicates.size()*typeInstancesSize;
			if(typePredicates.size()==0)
				denom = 1;
			//System.out.println("Occurence sum  = " + occurenceSum);
			//System.out.println("Denom = " + denom);
			double coverage = occurenceSum/denom;
			System.out.println("\n"+count+ " : Type: " + type );
			System.out.println("Coverage : "+ coverage);
			double weightedCoverage = (typePredicates.size()+ typeInstancesSize) / weightedDenomSum;
			System.out.println("Weighted Coverage : "+ weightedCoverage);
			structuredness = (structuredness + (coverage*weightedCoverage));
			count++;
		}

		return structuredness;
	}
	/**
	 * Get the denominator of weighted sum all types. Please see Duan et. all paper apple oranges
	 * @param types Set of rdf:types
	 * @param namedGraph Named graph
	 * @return sum Sum of weighted denominator
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static double getTypesWeightedDenomSum(Set<String> types, String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		double sum = 0 ; 
		for(String type:types)
		{
			long typeInstancesSize = getTypeInstancesSize(type,namedGraph);
			long typePredicatesSize = getTypePredicates(type,namedGraph).size();
			sum = sum + typeInstancesSize + typePredicatesSize;
		}
		return sum;
	}
	/**
	 * Get occurences of a predicate within a type
	 * @param predicate Predicate
	 * @param type Type
	 * @param namedGraph Named Graph
	 * @return predicateOccurences Predicate occurence value
	 * @throws NumberFormatException
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	public static long getOccurences(String predicate, String type, String namedGraph) throws NumberFormatException, QueryEvaluationException, RepositoryException, MalformedQueryException {
		long predicateOccurences = 0  ;
		String queryString ;
		if(namedGraph ==null)
			queryString = "SELECT Count(Distinct ?s) as ?occurences \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s <"+predicate+"> ?o"
					+ "           }" ;
		else
			queryString = "SELECT Count(Distinct ?s) as ?occurences From <"+ namedGraph+"> \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s <"+predicate+"> ?o"
					+ "           }" ;
		//System.out.println(queryString);
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult res = tupleQuery.evaluate();
		while(res.hasNext())
		{
			predicateOccurences = Long.parseLong(res.next().getValue("occurences").stringValue().toString());
		}
		return predicateOccurences;

	}
	/**
	 * Get the number of distinct instances of a specfici type
	 * @param type Type or class name
	 * @param namedGraph Named graph
	 * @return typeInstancesSize No of instances of type 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static long getTypeInstancesSize(String type, String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		long typeInstancesSize =0;
		String queryString ;
		if(namedGraph ==null)
			queryString = "SELECT Count(DISTINCT ?s)  as ?cnt  \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s ?p ?o"
					+ "           }" ;
		else
			queryString = "SELECT Count(DISTINCT ?s)  as ?cnt From <"+ namedGraph+"> \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s ?p ?o"
					+ "           }" ;
		//System.out.println(queryString);
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult res = tupleQuery.evaluate();
		while(res.hasNext())
		{
			typeInstancesSize =  Long.parseLong(res.next().getValue("cnt").stringValue().toString());
		}
		return typeInstancesSize;
	}
	/**
	 * Get all distinct predicates of a specific type
	 * @param type Type of class
	 * @param namedGraph Named Graph can be null
	 * @return typePredicates Set of predicates of type 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static Set<String> getTypePredicates(String type, String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Set<String> typePredicates =new HashSet<String>() ;
		String queryString ;
		if(namedGraph ==null)
			queryString = "SELECT DISTINCT ?typePred \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s ?typePred ?o"
					+ "           }" ;
		else
			queryString = "SELECT DISTINCT ?typePred From <"+ namedGraph+"> \n"
					+ "			WHERE { \n"
					+ "            ?s a <"+type+"> . "
					+ "            ?s ?typePred ?o"
					+ "           }" ;
		//System.out.println(queryString);
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult res = tupleQuery.evaluate();
		while(res.hasNext())
		{
			String predicate = res.next().getValue("typePred").toString();
			if (!predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
				typePredicates.add(predicate);
		}
		return typePredicates;
	}
	/**
	 * Initialize repository for a SPARQL endpoint
	 * @param endpointUrl Endpoint Url
	 * @throws RepositoryException
	 */
	public static void initializeRepoConnection(String endpointUrl) throws RepositoryException {
		Repository repo = new HTTPRepository(endpointUrl, "my-repoid");
		repo.initialize();
		con = repo.getConnection();

	}
	/**
	 *  Get distinct set of rdf:type
	 * @param namedGraph Named Graph of dataset can be null in that case all namedgraphs will be considered
	 * @return types Set of rdf:types
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public static Set<String> getRDFTypes(String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Set<String> types =new HashSet<String>() ;
		String queryString ="";
		if(namedGraph ==null)
			queryString = "SELECT DISTINCT ?type  \n"
					+ "			WHERE { \n"
					+ "            ?s a ?type"
					+ "           }" ;
		else
			queryString = "SELECT DISTINCT ?type From <"+ namedGraph+"> \n"
					+ "			WHERE { \n"
					+ "            ?s a ?type"
					+ "           }" ;
		//System.out.println(queryString);
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult res = tupleQuery.evaluate();
		while(res.hasNext())
		{
			types.add(res.next().getValue("type").toString());
		}
		return types;
	}
}
