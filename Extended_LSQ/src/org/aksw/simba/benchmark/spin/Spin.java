package org.aksw.simba.benchmark.spin;
import java.io.StringWriter;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
@SuppressWarnings("deprecation")
/**
 * SPIN Representation of the SPARQL query
 * @author Saleem
 *
 */
public class Spin {

	public static void main(String[] args) {
		String [] queries = new String [11];	

       Spin sp = new Spin();
		queries[0] = "PREFIX : <http://www.wikidata.org/entity/> \n"
				+ "ASK WHERE { \n"
				+ " ?lighthouse a :Q39715 .\n"
				+ "}\n" ;
		String spinRDF = sp.getSpinRDF(queries[0],"NT");  
		System.out.println(spinRDF);
		queries[1] = "PREFIX : <http://www.wikidata.org/entity/>\n"
				+ "DESCRIBE :Q39715";
		queries[2] = "PREFIX : <http://www.wikidata.org/entity/>\n"
				+ "CONSTRUCT{ \n"
				+ " _:blank a :Lighthouse . \n"
				+ "} \n"
				+ "WHERE { \n"
				+ " ?lighthouse a :Q39715 . \n"
				+ "}";
		queries[3] = "PREFIX : <http://www.wikidata.org/entity/> \n"
				+ "CONSTRUCT{ \n"
				+ " _:blank a :Lighthouse . \n"
				+ "} \n"
				+ "WHERE { \n"
				+ " { ?lighthouse a :Q39715 . } UNION { ?lighthouse a :Q39716 . } \n"
				+ "}";
		queries[4] = "PREFIX : <http://www.wikidata.org/entity/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "SELECT * \n"
				+ "WHERE { \n"
				+ " ?lighthouse a :Q39715 . \n"
				+ " ?lighthouse rdfs:label ?label FILTER(LANG(?label) = \"en\") \n"
				+ "} LIMIT 100 ";
		queries[5] = "PREFIX : <http://www.wikidata.org/entity/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT DISTINCT * \n"
				+ "WHERE {\n"
				+ " :Q9439 ((^:P25c|^:P22c)+) ?person .\n"
				+ " ?person rdfs:label ?label\n"
				+ " FILTER(LANG(?label) = \"en\")\n"
				+ "} LIMIT 1000 ";
		queries[6] = "PREFIX : <http://www.wikidata.org/entity/>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT (COUNT (DISTINCT ?neighbour) AS ?neighbours)\n"
				+ "WHERE {\n"
				+ "  ?country :P31s ?statement .\n"
				+ "  ?statement :P31v :Q3624078 .\n"
				+ "     FILTER NOT EXISTS { ?statement :P582q ?endDate }\n"
				+ "  ?country rdfs:label ?countryName FILTER(lang(?countryName)=\"en\")\n"
				+ "  OPTIONAL { ?country (:P47s/:P47v) ?neighbour .\n"
				+ "         ?neighbour :P31s ?statement2 .\n"
				+ "             ?statement2 :P31v :Q3624078 .\n"
				+ "             FILTER NOT EXISTS { ?statement2 :P582q ?endDate2 }\n"
				+ "  }"
				+ "} ORDER BY DESC(?neighbours) ";
		queries[7]= "PREFIX : <http://www.wikidata.org/entity/>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT ?countryName (COUNT (DISTINCT ?neighbour) AS ?neighbours)\n"
				+ "WHERE {\n"
				+ "  ?country :P31s ?statement .\n"
				+ "  ?statement :P31v :Q3624078 .\n"
				+ "     FILTER NOT EXISTS { ?statement :P582q ?endDate }\n"
				+ "  ?country rdfs:label ?countryName FILTER(lang(?countryName)=\"en\")\n"
				+ "  OPTIONAL { ?country (:P47s/:P47v) ?neighbour .\n"
				+ "             ?neighbour :P31s ?statement2 .\n"
				+ "             ?statement2 :P31v :Q3624078 .\n"
				+ "             FILTER NOT EXISTS { ?statement2 :P582q ?endDate2 }\n"
				+ "  }"
				+ "} ORDER BY DESC(?neighbours) ";
		queries[8] = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
				+ "SELECT *\n"
				+ "WHERE\n"
				+ "{    SELECT *\n"
				+ "     {\n"
				+ "       VALUES (?p ?pref) { \n"
				+ "          (dc:title 1) (dc:creator 2) \n"
				+ "       }\n"
				+ "       ?book ?p ?label .\n"
				+ "     } ORDER BY ?pref LIMIT 1 \n"
				+ "}";
		queries[9] = "SELECT * \n"
				+ "FROM <http://graph.com>\n"
				+ "FROM NAMED <http://graph2.com>\n"
				+ "WHERE\n"
				+ "{\n"
				+ "    GRAPH ?g { ?book ?p ?label . }\n"
				+ "}";
		queries[10] =  "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX  akt:  <http://www.aktors.org/ontology/portal#> PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX  akt-date: <http://www.aktors.org/ontology/date#>  ASK WHERE   { 2012 rdf:type rdfs:Class     OPTIONAL       { 2012 rdf:type rdf:Property }   }  " ; 
 
		
		// arq to spin
         
		for (int i=0 ; i < queries.length;i++)
		{
			System.out.println("\n\n\n"+i+"-------------Original Query-----------\n"+queries[i]);
			try{
				System.out.println( sp.getSpinRDF(queries[i], "Turtle"));
				
			}
			catch (Exception ex){ex.getMessage();}

		}
		//	System.out.println("-----");
		//	String str = spinQuery.toString();
		//	System.out.println("SPIN query:\n" + str);

		// Now turn it back into a Jena Query
		//Query parsedBack = ARQFactory.get().createQuery(spinQuery);
		//System.out.println("Jena query:\n" + parsedBack);
	}
	/**
	 * Get Spin RDF of the given SPARQL query
	 * @param query SPARQL query
	 * @param format {NT, N3, XML, Turtle}
	 * @return SPin RDF 
	 */
	public  String getSpinRDF(String query, String format) {
		//System.out.println("-------------Original Query-----------\n"+query);
		StringWriter out = new StringWriter();
		try{
			// Register system functions (such as sp:gt (>))
			SPINModuleRegistry.get().init();
			// Create an empty OntModel importing SP
			Model model = ModelFactory.createDefaultModel(ReificationStyle.Minimal);
			model.setNsPrefix("rdf", RDF.getURI());
			model.setNsPrefix("ex", "http://example.org/demo#");
			Query arqQuery = ARQFactory.get().createQuery(model, query);
			ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
			arq2SPIN.createQuery(arqQuery, null);
			//System.out.println("----SPIN query :---");
			if(format.equals("N3"))
				model.write(out, FileUtils.langN3);
			else if(format.equals("NT"))
				model.write(out, FileUtils.langNTriple);
			else if(format.equals("Turtle"))
				model.write(out, FileUtils.langTurtle);
			else if(format.equals("XML"))
				model.write(out, FileUtils.langXML);

		}
		catch (Exception ex){ex.getMessage();}
		return out.toString();
	}



}
