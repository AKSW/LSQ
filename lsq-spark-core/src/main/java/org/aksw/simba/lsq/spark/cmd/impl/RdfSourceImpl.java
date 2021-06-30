package org.aksw.simba.lsq.spark.cmd.impl;

import org.apache.hadoop.fs.Path;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;

public class RdfSourceImpl
    implements RdfSource
{
    // protected FileSystem fileSystem;
    protected SparkSession sparkSession;
    protected Path path;
    protected Lang lang;

    // protected RddRdfLoaderRegistry registry;

    public RdfSourceImpl(SparkSession sparkSession, Path path, Lang lang) {
        super();
        // this.fileSystem = fileSystem;
        this.sparkSession = sparkSession;
        this.path = path;
        this.lang = lang;
    }

    @Override
    public RDD<Dataset> asDatasets() {
        RddRdfLoader<Dataset> loader;

        loader = RddRdfLoaderRegistryImpl.get().find(lang, Dataset.class);


        RDD<Dataset> result = loader.load(sparkSession.sparkContext(), path.toString());
        return result;
    }




}
