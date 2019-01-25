package org.aksw.simba.lsq.core;

import java.io.PrintStream;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Sink;

/**
 * Jena Sink implementation that forwards data to a printstream, such as STDOUT
 * 
 * @author raven Mar 21, 2018
 *
 * @param <T>
 */
public class SinkIO<T>
    implements Sink<T>
{
    protected PrintStream out;
    protected boolean doClose;
    protected BiConsumer<PrintStream, T> consumer;

    public static boolean isStdStream(PrintStream stream) {
        boolean result = System.out == stream || System.err == stream;
        return result;
    }

    public SinkIO(BiConsumer<PrintStream, T> consumer) {
        this(System.out, consumer);
    }

    public SinkIO(PrintStream out, BiConsumer<PrintStream, T> consumer) {
        // By default, close streams unless we are dealing with stdout or stderr
    	this(out, !isStdStream(out), consumer);
    }

    public SinkIO(PrintStream out, boolean doClose, BiConsumer<PrintStream, T> consumer) {
        this.out = out;
        this.doClose = doClose;
        this.consumer = consumer;
    }

    @Override
    public void close() {
        if(doClose) {
            out.close();
        }
    }

    @Override
    public void send(T item) {
        consumer.accept(out, item);
    }

    @Override
    public void flush() {
        out.flush();
    }
}
