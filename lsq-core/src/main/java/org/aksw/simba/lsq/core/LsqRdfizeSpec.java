package org.aksw.simba.lsq.core;

import java.util.List;

public interface LsqRdfizeSpec {

    String getBaseIri();

    String getInputLogFormat();

    List<String> getPrefixSources();

    boolean isNoMerge();

    boolean isNoHostHash();

    String getHostSalt();

    boolean isSlimMode();

    String getEndpointUrl();

    List<String> getNonOptionArgs();

    String getBufferSize();

    String getTemporaryDirectory();

    int getParallel();

}