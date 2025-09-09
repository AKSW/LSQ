package org.aksw.simba.lsq.core.rx.io.input;

import java.util.function.Function;

import org.apache.jena.rdf.model.Resource;

/**
 * A function that takes an RDF resource for a log entry and
 * produces an output RDF resource for the query mode from it.
 */
public interface LsqLogRecordRdfizer
    extends Function<Resource, Resource>
{
}
