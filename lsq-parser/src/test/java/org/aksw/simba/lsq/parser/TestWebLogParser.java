package org.aksw.simba.lsq.parser;

import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

public class TestWebLogParser {

    @Test
    public void testWebLogParserRoundtripApache() {
        Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));

        Mapper mapper = logFmtRegistry.get("combined");

        Resource x = ModelFactory.createDefaultModel().createResource();
        String expected = "140.203.154.206 - - [16/May/2014:00:29:09 +0100] \"GET http://localhost:8890/sparql?default-graph-uri=&query=SELECT+%3Fparty+%3Fpage++WHERE+%0D%0A%7B+SERVICE+%3Chttp%3A%2F%2Fdbpedia-subset.system.ip.address%3A8891%2Fsparql%3E+%7B+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FBarack_Obama%3E+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2Fparty%3E+%3Fparty+.%7D+%0D%0A++SERVICE+%3Chttp%3A%2F%2Fnewyork-times.system.ip.address%3A8897%2Fsparql%3E+%0D%0A++%7B+%0D%0A++++%3Fx+%3Chttp%3A%2F%2Fdata.nytimes.com%2Felements%2FtopicPage%3E+%3Fpage+.+%0D%0A+%3Fx+%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23sameAs%3E+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FBarack_Obama%3E+.%0D%0A+%7D%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on HTTP/1.0\" 200 32039 \"-\" \"-\"";
        mapper.parse(x, expected);

        String actual = mapper.unparse(x);

        System.out.println(expected);
        System.out.println(actual);

        Assert.assertEquals(expected, actual);
    }
}
