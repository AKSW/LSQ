package org.aksw.simba.lsq.spark.cmd.impl;

import org.apache.jena.ext.com.google.common.collect.HashBasedTable;
import org.apache.jena.ext.com.google.common.collect.Table;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;

import net.sansa_stack.hadoop.jena.rdf.trig.FileInputFormatTrigDataset;

public class RddRdfLoaderRegistryImpl
    implements RddRdfLoaderRegistry
{
    private static RddRdfLoaderRegistry INSTANCE = null;

    public static RddRdfLoaderRegistry get() {
        if (INSTANCE == null) {
            synchronized (RddRdfLoaderRegistryImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RddRdfLoaderRegistryImpl();

                    loadDefaults(INSTANCE);
                }
            }
        }

        return INSTANCE;
    }

    public static void loadDefaults(RddRdfLoaderRegistry registry) {
        registry.register(
                Lang.TRIG,
                Dataset.class,
                (context, path) -> RddRdfLoader.createRdd(context, path, Dataset.class, FileInputFormatTrigDataset.class));
    }


    protected Table<Lang, Class<?>, RddRdfLoader<?>> registry = HashBasedTable.create();

    @Override
    public <T> void register(Lang lang, Class<T> targetType, RddRdfLoader<T> loader) {
        registry.put(lang, targetType, loader);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RddRdfLoader<T> find(Lang lang, Class<T> rdfType) {
        RddRdfLoader<?> tmp = registry.get(lang, rdfType);
        return (RddRdfLoader<T>)tmp;
    }

    public RddRdfLoaderRegistryImpl() {
        super();
    }


}
