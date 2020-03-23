package org.aksw.simba.lsq.cli.trash;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmarkRun;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.beust.jcommander.JCommander;

import io.reactivex.Flowable;
import joptsimple.internal.Strings;

public class MainCliLsqLogRdfizer {
	private static final Logger logger = LoggerFactory.getLogger(MainCliLsqLogRdfizer.class);
	
	public static void main(String[] args) throws Exception {
		CmdLsqBenchmarkRun cm = new CmdLsqBenchmarkRun();

		JCommander jc = new JCommander.Builder()
	    	  .addObject(cm)
	    	  .build();

		jc.parse(args);

	}
}
