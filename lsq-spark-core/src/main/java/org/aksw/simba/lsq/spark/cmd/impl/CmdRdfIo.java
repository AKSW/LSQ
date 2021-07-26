package org.aksw.simba.lsq.spark.cmd.impl;

import java.util.List;


/**
 * Interface for basic RDF-to-RDF batch processing tasks
 *
 */
public interface CmdRdfIo {
     List<String> getNonOptionArgs();

     String getOutFile();
     CmdRdfIo setOutFile(String outFile);

     String getOutFolder();
     CmdRdfIo setOutFolder(String outFolder);

     String getOutFormat();
     CmdRdfIo setOutFormat(String outFormat);

     List<String> getPrefixSources();

     long getDeferOutputForUsedPrefixes();
     CmdRdfIo setDeferOutputForUsedPrefixes(long n);

}
