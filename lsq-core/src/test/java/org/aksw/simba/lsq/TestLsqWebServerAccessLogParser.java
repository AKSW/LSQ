package org.aksw.simba.lsq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.StringMapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLsqWebServerAccessLogParser {

	private static final Logger logger = LoggerFactory.getLogger(TestLsqWebServerAccessLogParser.class);

	// private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


	@Test
	public void test() throws Exception {
    	Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));

    	// org.springframework.core.io.Resource[] resources = resolver.getResources("/logs/*");

    	Collection<ResourceInfo> resources = ClassPath.from(getClass().getClassLoader()).getResources().stream()
    			.filter(r -> r.getResourceName().toLowerCase().matches("^logs/.*log$"))
    			.collect(Collectors.toList());
    			
    	Assert.assertNotEquals(0, resources.size());
    	
    	for (ResourceInfo r : resources) {
    		//String qualifiedNam = r.getResourceName();
    		
    		String rName = Paths.get(r.url().toURI()).getFileName().toString();
    		String fmtName = rName.split("\\.", 2)[0];
    		
    		Mapper mapper = logFmtRegistry.get(fmtName);
    		if(mapper == null) {
    			throw new RuntimeException("No mapper for test case: " + rName);
    		}

//    		logger.debug("Processing " + rName + " with format " + fmtName + " - " + mapper);

    		try(BufferedReader br = new BufferedReader(new InputStreamReader(r.asByteSource().openStream()))) {
    			br.lines().forEach(line -> {
    				logger.debug("Parse attempt [" + fmtName + ", " + rName + "]: "  + line);

//    				if("virtuoso.dbpedia351.log".equals(rName)) {
//    					System.out.println("problematic entry found");
//    				}

    				Resource x = ModelFactory.createDefaultModel().createResource();
    				mapper.parse(x, line);

//    				RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE);
//
//    				WebLogParser.extractQuery(x);
//
////    				if(!x.hasProperty(LSQ.query)) {
////    					throw new RuntimeException("Could not obtain query");
////    				}
//
//    				RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE);
    			});
    		}

    	}



//		String spyPathStr = smlBasePath + r.getFilename() + "/";
//		String r2rPathStr = r2rmlBasePath + r.getFilename() + "/";
//
//		Resource spyPathRes = resolver.getResource(spyPathStr);
//		Resource r2rPathRes = resolver.getResource(r2rPathStr);
//		if(!spyPathRes.exists()) {
//			logger.warn("Resource does not exist " + spyPathStr);
//			return null;
//		}
//
//		if(!r2rPathRes.exists()) {
//			logger.warn("Resource does not exist " + r2rPathStr);
//			return null;
//		}


	}


	//@Test
	public void test2() {
        Map<String, BiConsumer<StringMapper, String>> map = WebLogParser.createWebServerLogStringMapperConfig();

        String logLine = "127.0.0.1 - - [06/Nov/2016:05:12:49 +0100] \"GET /icons/ubuntu-logo.png HTTP/1.1\" 200 3623 \"http://localhost/\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0\"";

        StringMapper mapper = StringMapper.create("%h %l %u %{dd/MMM/yyyy:HH:mm:ss Z}t \"%r\" %>s %b", map::get);

        System.out.println(logLine);
        System.out.println(mapper);

        Resource x = ModelFactory.createDefaultModel().createResource();
        mapper.parse(x, logLine);
        RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE);

        System.out.println(mapper);

        //Resource r = ModelFactory.createDefaultModel().createResource();
        x
            .removeAll(PROV.atTime)
            .removeAll(LSQ.verb)
            .removeAll(LSQ.host)
            .addLiteral(PROV.atTime, Calendar.getInstance())
            .addLiteral(LSQ.verb, "GET")
            .addLiteral(LSQ.host, "0.0.0.0");

        System.out.println(mapper.unparse(x));

	}
}
