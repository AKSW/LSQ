package org.aksw.simba.lsq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.aksw.simba.lsq.util.Mapper;
import org.aksw.simba.lsq.util.WebLogParser;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class WebServerAccessLogParserTests {

	private static final Logger logger = LoggerFactory.getLogger(WebServerAccessLogParserTests.class);

	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


	@Test
	public void test() throws Exception {
    	Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));

    	org.springframework.core.io.Resource[] resources = resolver.getResources("/logs/*");

    	for(org.springframework.core.io.Resource r : resources) {
    		String rName = r.getFilename();
    		String fmtName = rName.split("\\.", 2)[0];

    		Mapper mapper = logFmtRegistry.get(fmtName);
    		if(mapper == null) {
    			throw new RuntimeException("No mapper for test case: " + rName);
    		}

    		logger.debug("Processing " + rName + " with format " + fmtName + " - " + mapper);

    		try(BufferedReader br = new BufferedReader(new InputStreamReader(r.getInputStream()))) {
    			br.lines().forEach(line -> {
    				Resource x = ModelFactory.createDefaultModel().createResource();
    				mapper.parse(x, line);

    				RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE);
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
}
