package org.aksw.simba.lsq.spark.cmd.impl;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public interface RdfSourceFactory {

    default RdfSource get(String sourceStr) {
        try {
            return create(sourceStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    RdfSource create(String sourceStr) throws Exception;
    RdfSource create(String sourceStr, FileSystem fileSystem) throws Exception;
    RdfSource create(Path path, FileSystem fileSystem) throws Exception;
}
