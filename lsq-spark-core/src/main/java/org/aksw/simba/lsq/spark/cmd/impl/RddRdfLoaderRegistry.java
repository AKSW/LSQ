package org.aksw.simba.lsq.spark.cmd.impl;

import org.apache.jena.riot.Lang;

public interface RddRdfLoaderRegistry {
    <T> void register(Lang lang, Class<T> targetType, RddRdfLoader<T> loader);

    <T> RddRdfLoader<T> find(Lang lang, Class<T> rdfType);
}
