package org.aksw.simba.lsq.cli.cmd.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.rx.io.resultset.NamedGraphStreamCliUtils;
import org.apache.jena.riot.Lang;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Create a query-centric view from RDFized logs. Implicitly deduplicates queries.")
public class CmdLsqInvert {
    @Parameter(names={"-h", "--help"}, help=true)
    public boolean help = false;

//	@Parameter(names={"-m", "--format"}, description="Input log format")
//	public String inputLogFormat;
//
//	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
//	public List<String> prefixSources = new ArrayList<>();

    @Parameter(description="file-list to probe")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Deprecated // The process was changed that inversion of the
    // log-record-to-query relation is no longer needed as it is now how the process works
    public static void invert(CmdLsqInvert cmdInvert) throws Exception {
        //CmdNgsMap cmd = new CmdNgsMap();
//        SparqlScriptProcessor sparqlProcessor = SparqlScriptProcessor.createWithEnvSubstitution(null);
//        sparqlProcessor.process("lsq-invert-rdfized-log.sparql");
        // cmd.mapSpec = new MapSpec();
        // cmd.mapSpec.stmts.add("lsq-invert-rdfized-log.sparql");
        // sparqlProcessor.process(cmdInvert.nonOptionArgs) ;
        // cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);

        // JenaSystem.init();

        NamedGraphStreamCliUtils.execMap(null,
                cmdInvert.nonOptionArgs,
                Arrays.asList(Lang.TRIG, Lang.NQUADS),
                Arrays.asList("lsq-invert-rdfized-log.sparql"),
                null,
                null,
                20);


//        Flowable<Dataset> flow = NamedGraphStreamCliUtils.createNamedGraphStreamFromArgs(cmdInvert.nonOptionArgs, null, null, Arrays.asList(Lang.TRIG, Lang.NQUADS));
//
//        try (OutputStream out = StdIo.openStdOutWithCloseShield()) {
//	        // StreamRdf streamRdf = StreamRDFWriter.getWriterStream(out, RDFFormat.TRIG_PRETTY, null);
//
//	        // NamedGraphStreamOps.map(DefasultPrefixes.prefixes, sparqlProcessor.getSparqlStmts(), StdIo.openStdout());
//	        try (SPARQLResultExProcessor resultProcessor = SPARQLResultExProcessorBuilder.createForQuadOutput().build()) {
//	        	SparqlStmtUtils.execAny(null, null)
//
//	        }
//        }


        // SparqlStmtUtils
        // Function<RDFConnection, SPARQLResultEx> mapper = SparqlMappers.createMapperFromDataset(outputMode, stmts, resultProcessor);

    }
}