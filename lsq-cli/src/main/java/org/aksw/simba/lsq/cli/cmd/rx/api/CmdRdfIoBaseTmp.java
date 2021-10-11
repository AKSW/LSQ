package org.aksw.simba.lsq.cli.cmd.rx.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.spark.cmd.impl.CmdRdfIo;
import org.apache.jena.rdf.model.Resource;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@ResourceView
public interface CmdRdfIoBaseTmp
    extends CmdRdfIo, Resource
{
    @IriNs("eg")
    @Parameters(arity="1..*", description="file-list to probe")
    @Override
    List<String> getNonOptionArgs();

    @IriNs("eg")
    @Override
    @Parameters(arity="1..*", description="file-list to probe")
    String getOutFile();


    @IriNs("eg")
    @Option(names= {"-m", "--out-format"}, required=true, description="Output RDF format")
    @Override
    String getOutFormat();


    @IriNs("eg")
    @Option(names={"-n", "--namespaces"}, description="Namespace prefix sources")
    @Override
    List<String> getPrefixSources();


    @IriNs("eg")
    @Override
    @Option(names={"-d", "--defer"}, description="Namespace prefix sources")
    long getDeferOutputForUsedPrefixes();

}