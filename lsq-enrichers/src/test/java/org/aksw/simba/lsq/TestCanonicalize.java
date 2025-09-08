package org.aksw.simba.lsq;

import java.util.List;

import org.aksw.jenax.arq.util.syntax.CanonicalRdf;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class TestCanonicalize {
    @Test
    public void testCanonicalTp01() {
        Triple input = Triple.create(Var.alloc("x"), RDF.type.asNode(), Var.alloc("x"));
        Triple expected = Triple.create(Var.alloc("v_1"), RDF.type.asNode(), Var.alloc("v_1"));
        Triple actual = CanonicalRdf.canonicalize(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCanonicalTp02() {
        Triple input = Triple.create(Var.alloc("x"), RDF.type.asNode(), Var.alloc("z"));
        Triple expected = Triple.create(Var.alloc("v_1"), RDF.type.asNode(), Var.alloc("v_2"));
        Triple actual = CanonicalRdf.canonicalize(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCanonicalBgp() {
        List<Triple> input = List.of(
            Triple.create(Var.alloc("x"), RDF.type.asNode(), Var.alloc("z")),
            Triple.create(Var.alloc("y"), RDF.type.asNode(), Var.alloc("z"))
        );
        List<Triple> expected = List.of(
                Triple.create(Var.alloc("v_1"), RDF.type.asNode(), Var.alloc("v_2")),
                Triple.create(Var.alloc("v_3"), RDF.type.asNode(), Var.alloc("v_2"))
            );
        List<Triple> actual = CanonicalRdf.canonicalizeTriples(input);
        Assert.assertEquals(expected, actual);
    }
}
