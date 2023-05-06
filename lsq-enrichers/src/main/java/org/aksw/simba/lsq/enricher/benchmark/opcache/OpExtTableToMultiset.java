package org.aksw.simba.lsq.enricher.benchmark.opcache;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpExtTableToMultiset
    extends OpExt
{
    protected Op subOp;


    public OpExtTableToMultiset(Op subOp) {
        super(OpExtTableToMultiset.class.getSimpleName());
        this.subOp = subOp;
    }

    public Op getSubOp() {
        return subOp;
    }

    @Override
    public Op effectiveOp() {
        return null;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        return null;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
    }

    @Override
    public int hashCode() {
        return 12345;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        return other instanceof OpExtTableToMultiset;
    }

}
