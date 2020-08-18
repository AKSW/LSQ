package org.aksw.simba.lsq.opcache;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * An operator that references a result set by key, such as a key of a cache entry.
 * The evaluation depends on the execution context or the executor
 *
 * @author raven
 *
 */
public class OpExtKey
    extends OpExt
{
    protected Object key;

    public OpExtKey(Object key) {
        super(OpExtKey.class.getSimpleName());
        this.key = key;
    }

    @Override
    public Op effectiveOp() {
        return null;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // ExprEvalException?
        throw new RuntimeException("This class requires an xecutor that can handle " + getClass().getName());
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.print(key);
    }

    @Override
    public int hashCode() {
        //int result = tag.hashCode() * 13 + Objects.hashCode(key) * 11;
        int result = Objects.hash(tag, key);
        return result;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        return other instanceof OpExtKey && Objects.equals(key, ((OpExtKey)other).key);
    }

}
