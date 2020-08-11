package org.aksw.simba.lsq.cli.main;

import java.io.File;
import java.util.Iterator;

import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.riot.RDFDataMgr;

public class MainDeleteme {

    public static void main(String[] args) {
    QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://akswnc7.informatik.uni-leipzig.de:8897/sparql")
        .config()
        .withCache(new CacheBackendFile(new File("/tmp/cache/foo2"), 7 * 24 * 60 * 60 * 1000 /* ms */)) // Das ist der letzte Wrapper in der Chain
        .withPagination((int) 1000000)
        //.withClientSideConstruct()
        .withParser(SparqlQueryParserImpl.create()) // Der wrapper kommt zuerst
            .end()
            .create();

        Iterator<Triple> rs = qef.createQueryExecution("PREFIX georss: <http://www.georss.org/georss/>\n" +
                  "PREFIX bag: <http://bag.basisregistraties.overheid.nl/def/bag#>\n" +
                  "PREFIX gsp: <http://www.opengis.net/ont/geosparql#>\n" +
                  "prefix dbo:     <http://dbpedia.org/ontology/>\n" +
                  "\n" +
                  "\n" +
                  "CONSTRUCT{\n" +
                  "    ?pand dbo:location ?woonplaats;\n" +
                  "          dbo:locationName ?naamwoonplaats;\n" +
                  "          dbo:address ?address;\n" +
                  "          dbo:postalCode ?postcode;\n" +
                  "          georss:point ?point.\n" +
                  "}\n" +
                  "{\n" +
                  "    ?verblijfsobject a bag:Verblijfsobject;\n" +
                  "              bag:pandrelatering ?pand;\n" +
                  "              bag:hoofdadres ?hoof.\n" +
                  "    ?pand <http://www.opengis.net/ont/geosparql#hasGeometry> ?pandGeometry.\n" +
                  "    ?pandGeometry <http://www.opengis.net/ont/geosparql#asWKT> ?geometry.\n" +
                  "    ?hoof bag:bijbehorendeOpenbareRuimte ?openBare;\n" +
                  "          bag:huisnummer ?huisnummer;\n" +
                  "          bag:huisletter ?huisletter;\n" +
                  "          bag:postcode ?postcode.\n" +
                  "    ?openBare a bag:Weg;\n" +
                  "              bag:bijbehorendeWoonplaats ?woonplaats;\n" +
                  "              bag:naamOpenbareRuimte ?openbareruimte.\n" +
                  "    ?woonplaats bag:naamWoonplaats ?naamwoonplaats;\n" +
                  "                bag:identificatiecode ?identificatiecode.\n" +
                  "    #This part build the address and the point\n" +
                  "    FILTER regex(?huisletter,\"[a-z]\") #When the street number is followed by a letter (14a) it will always be in lower case.\n" +
                  "    BIND (replace(replace(?geometry, \",.*\", \"\"),\"POLYGON\\\\(\\\\(\", \"\") AS ?point) #Take the first point from the polygon\n" +
                  "    BIND (CONCAT(?openbareruimte,\" \",?huisnummer,?huisletter) AS ?address)\n" +
                  "}").execConstructTriples();
    //  FileOutputStream stream = null;
      //  try {
      //    stream = new FileOutputStream(new File(theDir.getPath(), fileName+".nt"));
      //  } catch (FileNotFoundException e) {
      //    e.printStackTrace();
        //  }
        RDFDataMgr.writeTriples(System.out, rs);
    }

    public static void main2(String[] args) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql")
                .config()
                .withCache(new CacheBackendFile(new File("/tmp/cache/foo"), 60 * 60 * 1000 /* ms */)) // Das ist der letzte Wrapper in der Chain
                .withPagination((int) 1)
//                .withClientSideConstruct()
                .withParser(SparqlQueryParserImpl.create()) // Der wrapper kommt zuerst
                .end()
                .create();

        QueryExecution qe = qef.createQueryExecution("CONSTRUCT { ?s ?p ?o . ?s ?p ?o . ?s a ?s}  WHERE { ?s ?p ?o } LIMIT 10");
//        Model m = qe.execConstruct();
//        RDFDataMgr.write(System.out, m, Lang.TURTLE);
//        System.out.println("model size: " + m.size());
        Iterator<Triple> it = qe.execConstructTriples();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
