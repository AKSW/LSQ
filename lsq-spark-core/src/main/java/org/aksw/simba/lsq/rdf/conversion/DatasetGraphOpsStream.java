package org.aksw.simba.lsq.rdf.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.aksw.commons.util.stream.StreamOperatorCollapseRuns;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class DatasetGraphOpsStream {
    public static StreamTransformer<Quad, Entry<Node, List<Quad>>> groupToList()
    {
        return StreamOperatorCollapseRuns.<Quad, Node, List<Quad>>create(CollapseRunsSpec.create(
                Quad::getGraph,
                graph -> new ArrayList<>(),
                (list, item) -> list.add(item)
           ))::transform;
    }

    public static StreamTransformer<Quad, Entry<Node, DatasetGraph>> groupConsecutiveQuadsRaw(
            Function<Quad, Node> grouper,
            Supplier<? extends DatasetGraph> graphSupplier) {

        return StreamOperatorCollapseRuns.<Quad, Node, DatasetGraph>create(CollapseRunsSpec.create(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                DatasetGraph::add))::transform;
    }


    public static StreamTransformer<Quad, DatasetGraph> graphsFromConsecutiveQuads(
            Function<Quad, Node> grouper,
            Supplier<DatasetGraph> graphSupplier) {
        return upstream ->
                groupConsecutiveQuadsRaw(grouper, graphSupplier).apply(upstream)
                .map(Entry::getValue);
    }

    public static StreamTransformer<Quad, Dataset> datasetsFromConsecutiveQuads(
            Function<Quad, Node> grouper,
            Supplier<DatasetGraph> graphSupplier) {
        return upstream ->
                groupConsecutiveQuadsRaw(grouper, graphSupplier).apply(upstream)
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }

    public static StreamTransformer<Quad, Dataset> datasetsFromConsecutiveQuads(
            Supplier<? extends DatasetGraph> graphSupplier) {
        return upstream ->
                groupConsecutiveQuadsRaw(Quad::getGraph, graphSupplier).apply(upstream)
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }
}
