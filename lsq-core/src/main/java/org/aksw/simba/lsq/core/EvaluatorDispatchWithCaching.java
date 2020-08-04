package org.aksw.simba.lsq.core;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.ref.Evaluator;
import org.apache.jena.sparql.engine.ref.EvaluatorDispatch;
import org.apache.jena.sparql.engine.ref.EvaluatorFactory;
import org.apache.jena.sparql.engine.ref.EvaluatorSimple;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * A simple cache wrapper for an executor
 *
 * @author raven
 *
 */
public class EvaluatorDispatchWithCaching
    extends EvaluatorDispatch
{
    protected Cache<Op, Table> cache;
    protected Predicate<Op> cacheWriteAllowed;

    public EvaluatorDispatchWithCaching(Evaluator evaluator, Predicate<Op> allowCacheWrite, Cache<Op, Table> cache) {
        super(evaluator);
        this.cacheWriteAllowed = allowCacheWrite;
        this.cache = cache;
    }

    // Hack because getResult is not visible in the base class
    public Table getMyResult() {
        Table table = pop() ;
        return table ;
    }

    @Override
    protected Table eval(Op op) {
        try {
            boolean doCacheWrite = cacheWriteAllowed.test(op);

            // If cache writing is false, still check whether the operation's result is present
            // in the cache - but don't write it
            Table table = doCacheWrite
                    ? cache.get(op, () -> super.eval(op))
                    : Optional.ofNullable(cache.getIfPresent(op)).orElse(super.eval(op));
            return table;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


//    public static EvaluatorDispatch createForKeyedResult() {
//        return createForKeyedResult(null);
//    }

    public static EvaluatorDispatch createForKeyedResult(ExecutionContext execCxt, Cache<Op, Table> cache) {
        Predicate<Op> cacheWriteAllowed = op -> !(op instanceof OpExtKeyAndTableSupplier);
        Evaluator delegate = EvaluatorFactory.create(execCxt);
//        Evaluator delegate = new EvaluatorSimple(execCxt) {
//
//        };

        return new EvaluatorDispatchWithCaching(delegate, cacheWriteAllowed, cache) {
            @Override
            public void visit(OpExt opExt) {
                if(opExt instanceof OpExtKeyAndTableSupplier) {
                    OpExtKeyAndTableSupplier o = (OpExtKeyAndTableSupplier)opExt;
                    Object key = o.getKey();
//                    OpExtKey opKey = new OpExtKey(key);

                    Table table = cache.getIfPresent(key);
                    if(table == null) {
                        Supplier<Table> queryIterSupplier = o.getQueryIterSupplier();
                        table = queryIterSupplier.get();
                        //QueryIterator queryIter = queryIterSupplier.get();
                        //table = TableFactory.create(queryIter);
                        cache.put(opExt, table);
                    }
                    push(table);
                } else if (opExt instanceof OpExtTableToMultiset) {
                    OpExtTableToMultiset o = (OpExtTableToMultiset)opExt;
                    Table table = eval(o.getSubOp()) ;
                    List<Var> vars = table.getVars();
                    QueryIterator it = table.iterator(execCxt);
                    try {
                        Multiset<Binding> rows = bindingIterToMultiset(it);
                        Table result = new TableMultiset(vars, rows);
                        push(result);
                    } finally {
                        it.close();
                    }
                } else {
                    super.visit(opExt);
                }
            }
        };
    }



    public static Multiset<Binding> bindingIterToMultiset(Iterator<Binding> it) {
        Multiset<Binding> result = HashMultiset.create();
        while(it.hasNext()) {
            Binding binding = it.next();
            result.add(binding);
        }
        return result;
    }

    protected static final Cache<Op, Table> DEFAULT_CACHE = CacheBuilder.newBuilder().maximumSize(1000).build();

    public static Table eval(Op op, ExecutionContext execCxt) {
        Evaluator evaluator = new EvaluatorSimple(execCxt);
        EvaluatorDispatchWithCaching ev = new EvaluatorDispatchWithCaching(evaluator, x -> true, DEFAULT_CACHE);
        op.visit(ev);
        Table table = ev.getMyResult();
        return table;
    }

    public static Multiset<Binding> evalToMultiset(Op op, ExecutionContext execCxt) {
        Multiset<Binding> result;
        Table table = eval(op, execCxt);
        if(table instanceof TableMultiset) {
            TableMultiset tmp = (TableMultiset)table;
            result = tmp.getRows();
        } else {
            throw new RuntimeException("Not a multiset table");
        }

        return result;
    }

}

