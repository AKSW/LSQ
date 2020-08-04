package org.aksw.simba.lsq.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;

public class TableMgr {

    /**
     * Parse a table from a string.
     *
     * @param str
     * @param lang A Lang for a result set such as ResultSetLang.SPARQLResultSetJSON
     * @return
     */
    public static Table parseTableFromString(String str, Lang lang) {
        Objects.requireNonNull(str);
        Table result = null;
        try(InputStream in = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            ResultSet rs = ResultSetMgr.read(in, lang);
            result = TableFactory.create(new QueryIteratorResultSet(rs));
        } catch (IOException e) {
            // ByteArrayInputStream should never raise an IOException
            throw new RuntimeException(e);
        }

        return result;
    }

}
