package org.aksw.simba.lsq;

import java.util.Random;

import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;


public class TestTripleRemoval {
    @Test
    public void test() {
        Graph graph = new GraphMem(); // GraphFactory.createDefaultGraph(); // new GraphMem();
        RDFDataMgr.read(graph, "broken-triple-removal-01.ttl");
        RDFDataMgr.write(System.out, graph, RDFFormat.NTRIPLES);
        ExtendedIterator<Triple> it = graph.find();
        try {
            while (it.hasNext()) {
                Triple t = it.next();
                it.remove();
            }
        } finally {
            it.close();
        }
    }

    // @Test
    public void test2() {
        Random r = new Random(0);
        for (int j = 0; j < 100000; ++j) {
            System.out.println(j);
            Graph graph = new GraphMem(); // new NamedGraphWrapper(null, new GraphMem()); //GraphFactory.createGraphMem();
            for (int i = 0; i < 1000; ++i) {
                String str = "urn:foobar" + (i / 3);
                Node n = NodeFactory.createURI(str.repeat(100));
                Triple t = Triple.create(n, n, n);
                // System.out.println(t);
                graph.add(t);
                ExtendedIterator<Triple> it = graph.find();
                while (it.hasNext()) {
                    it.next();
                    if (r.nextFloat() > 0.95) {
                        it.remove();
                    }
                }
                it.close();
            }

            Graph after = NodeTransformLib2.applyNodeTransform(x -> r.nextFloat() > 1 ? x : RDF.Nodes.type, graph);
            // RDFDataMgr.write(System.out, DatasetFactory.wrap(DatasetGraphFactory.wrap(after)), RDFFormat.TRIG_PRETTY);
        }
//        ExtendedIterator<Triple> it = graph.find();
//        try {
//            while (it.hasNext()) {
//                it.next();
//                it.remove();
//            }
//        } finally {
//            it.close();
//        }
    }
}
