package org.aksw.simba.lsq.core;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.algebra.table.TableBase;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.collect.Multiset;

public class TableMultiset extends TableBase {
    protected Multiset<Binding> rows;
    protected List<Var>     vars;

//    public TableN() {}
//
//    public TableN(List<Var> vars) {
//        if ( vars != null )
//            this.vars = vars ;
//    }

    /**
     * Build table from an iterator.
     * This operation reads the QueryIterator to
     * completion when creating the table.
     */
//    public TableN(QueryIterator qIter) {
//        materialize(qIter) ;
//    }

    public Multiset<Binding> getRows() {
        return rows;
    }

    protected TableMultiset(List<Var> variables, Multiset<Binding> rows) {
        this.vars = variables ;
        this.rows = rows ;
    }

    private void materialize(QueryIterator qIter) {
        while (qIter.hasNext()) {
            Binding binding = qIter.nextBinding() ;
            addBinding(binding) ;
        }
        qIter.close() ;
    }

    @Override
    public void addBinding(Binding binding) {
        for (Iterator<Var> names = binding.vars(); names.hasNext();) {
            Var v = names.next() ;
            if ( !vars.contains(v) )
                vars.add(v) ;
        }
        rows.add(binding) ;
    }

    @Override
    public int size() {
        return rows.size() ;
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty() ;
    }

    @Override
    public Iterator<Binding> rows() {
        return rows.iterator() ;
    }

    @Override
    public QueryIterator iterator(ExecutionContext execCxt) {
        return new QueryIterPlainWrapper(rows.iterator(), execCxt) ;
    }

    @Override
    public void closeTable() {
        rows = null ;
        // Don't clear the vars in case code later asks for the variables.
    }

    @Override
    public List<String> getVarNames() {
        return Var.varNames(vars) ;
    }

    @Override
    public List<Var> getVars() {
        return vars ;
    }
}