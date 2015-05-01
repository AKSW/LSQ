package org.aksw.simba.benchmark;
/**
 * This is main configuration class where you can set various filters on bechmark queries features and SPARQL clauses. 
 * Also you can turn on/of a specific dimension 
 * @author Saleem
 *
 */
public class Config {
	//public static String featureFilter = "(ResultSize >= 5 AND ResultSize <= 100 AND BGPs >= 2 AND TriplePatterns >=7) OR (MeanTriplePatternsSelectivity >= 0.0001)";
	//public static String clauseFilter = "(OPTIONAL AND DISTINCT) OR (UNION)";
	public static String featureFilter = "";  // by default we have no filter on SPARQL query features ( ResultSize, TriplePatterns, BGPs, JoinVertices, MeanJoinVerticesDegree, MeanTriplePatternsSelectivity) 
	public static String clauseFilter = ""; // by deafult we have no filter on SPARQL clauses. See examples above if you want any. 
	//Note the disjunctive Normal form (e.g. (A AND B AND C ..) OR (D AND E ..) can represent all combinations. 
	// You must specify your Filter in Disjuntive Normal Form with only >=  and <= operators for featureFilter and = operator for clauseFilter
	// If you want a filter on exact value  e.g, your benchmarch should only contains a triple patterns = 5 then use
	// (TriplePatterns >= 5 and TriplePatterns <= 5) 

	//--------Basic Query Types to to be considered into the benchmark. These boolean are only used if you wana consider a specific feature or clause as one of the dimension in multi dimension space. Just turn on those 
	//          which you think are important to be considered for your benchmark generation--------
	public static boolean CONSTRUCT  = true ;    // should the benchmark consider CONSTRUCT queries ?
	public static boolean ASK  = true ; 
	public static boolean DESCRIBE  = true ;   //by default we are not interested our benchmark can have DESCRIBE query. You can turn on this if interested
	public static boolean SELECT  = true ; 
	//-----------Features --------
	public static boolean triplePatternsCount  = true ;  // no of triple patterns in a query
	public static boolean resultSize  = true ;      //query resultset size
	public static boolean joinVertices  = true ;   // no. of join vertices
	public static boolean meanJoinVerticesDegree  = true ;
	public static boolean meanTriplePatternSelectivity  = true ;
	public static boolean BGPs  = true ;
	//-------SPARQL constructs ------
	public static boolean UNION  = true ;
	public static boolean FILTER  = true ;
	public static boolean OPTIONAL  = true;
	public static boolean DISTINCT  = true ;
	public static boolean ORDERBY  = true ;
	public static boolean GROUPBY  = true ;
	public static boolean LIMIT  = true ;
	public static boolean REGEX  = true ;
	public static boolean OFFSET  = true ;  // e.g., i dont consider OFFSET to be very important CLAUSE for benchmark generation. You can trun on if you disagree with me
	//-------Query Runtime---------
	public static boolean runTime = true;
	//---------Draw Voronoi Diagram--------
	public static boolean drawVoronoiDiagram = true; 
	public static void main(String[] args) {
		System.out.println(CONSTRUCT);
		CONSTRUCT =true;
		//considerConstruct(true);
		System.out.println(CONSTRUCT);
	}

}
