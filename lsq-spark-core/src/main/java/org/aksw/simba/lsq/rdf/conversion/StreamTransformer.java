package org.aksw.simba.lsq.rdf.conversion;

import java.util.stream.Stream;

public interface StreamTransformer<I, O> {
    Stream<O> apply(Stream<I> input);
}