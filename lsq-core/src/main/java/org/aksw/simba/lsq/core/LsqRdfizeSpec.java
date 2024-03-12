package org.aksw.simba.lsq.core;

import java.util.List;

public interface LsqRdfizeSpec {

    String getBaseIri();

    String getInputLogFormat();

    List<String> getPrefixSources();

    boolean isNoMerge();

    boolean isNoHostHash();

    String getHostSalt();

    // Consolidate "Slim mode" and "query only" into a class that captures what features to rdfize

    boolean isSlimMode();
    boolean isQueryOnly();

    String getEndpointUrl();

    List<String> getNonOptionArgs();

    String getBufferSize();

    String getTemporaryDirectory();

    int getParallel();

}
