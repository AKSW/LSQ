package org.aksw.simba.lsq.core;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpExtKeyAndTableSupplier
    extends OpExt
{
    protected Object key;
    protected Supplier<Table> queryIterSupplier;

    public OpExtKeyAndTableSupplier(Object key, Supplier<Table> queryIterSupplier) {
        super(OpExtKeyAndTableSupplier.class.getSimpleName());

        this.key = key;
        this.queryIterSupplier = queryIterSupplier;
    }

    public Object getKey() {
        return key;
    }

    public Supplier<Table> getQueryIterSupplier() {
        return queryIterSupplier;
    }

    @Override
    public OpTable effectiveOp() {
        // return null instead?
        Supplier<Table> queryIterSupplier = getQueryIterSupplier();
        Table table = queryIterSupplier.get();
        return OpTable.create(table);

//        Supplier<QueryIterator> queryIterSupplier = getQueryIterSupplier();
//        QueryIterator queryIter = queryIterSupplier.get();
//        try {
//            Table table = TableFactory.create(queryIter);
//            OpTable result = OpTable.create(table);
//            return result;
//        } finally {
//            queryIter.close();
//        }
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        Table table = queryIterSupplier.get();
        QueryIterator result = new QueryIteratorResultSet(table.toResultSet());
        // QueryIterator result = queryIterSupplier.get();
        return result;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.print(key);
    }

    /**
     *
     * The queryIterSupplier does not take part in equivalence testing
     *
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(tag, key);
        return result;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        boolean result = false;
        if(other instanceof OpExtKeyAndTableSupplier) {
            OpExtKeyAndTableSupplier o = (OpExtKeyAndTableSupplier)other;
            result = Objects.equals(key, o.key); // && Objects.equals(queryIterSupplier, o.queryIterSupplier);
        }

        return result;
    }
}
