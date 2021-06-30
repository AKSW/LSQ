package org.aksw.simba.lsq.rdf.conversion;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

public class GraphOpsStream {
    public static StreamTransformer<Triple, Entry<Node, Graph>> groupConsecutiveTriplesRaw(
            Function<Triple, Node> grouper,
            Supplier<Graph> graphSupplier) {

        return StreamOperatorSequentialGroupBy.<Triple, Node, Graph>create(SequentialGroupBySpec.create(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                Graph::add))::transform;
    }

    public static StreamTransformer<Triple, Entry<Node, Graph>> graphsFromConsecutiveSubjectsRaw() {
        return graphsFromConsecutiveSubjectsRaw(GraphFactory::createDefaultGraph);
    }

    public static StreamTransformer<Triple, Entry<Node,Graph>> graphsFromConsecutiveSubjectsRaw(Supplier<Graph> graphSupplier) {
        return groupConsecutiveTriplesRaw(Triple::getSubject, graphSupplier);
    }

    public static StreamTransformer<Triple, Graph> graphsFromConsecutiveSubjects() {
        return graphsFromConsecutiveSubjects(GraphFactory::createDefaultGraph);
    }

    public static StreamTransformer<Triple, Graph> graphsFromConsecutiveSubjects(Supplier<Graph> graphSupplier) {
        return graphFromConsecutiveTriples(Triple::getSubject, graphSupplier);
    }

    public static StreamTransformer<Triple, Graph> graphFromConsecutiveTriples(
            Function<Triple, Node> grouper,
            Supplier<Graph> graphSupplier) {
        return upstream ->
                groupConsecutiveTriplesRaw(grouper, graphSupplier).apply(upstream)
                .map(Entry::getValue);
    }
}

