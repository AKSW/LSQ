package org.aksw.simba.lsq.parser;

import java.util.Arrays;
import java.util.function.BiFunction;

import com.google.common.base.Converter;

public class ConverterChain<A, B>
    extends Converter<A, B>
{
    protected Iterable<Converter<A, B>> converters;

    public ConverterChain(Iterable<Converter<A, B>> converters) {
        super();
        this.converters = converters;
    }

    public static <A, X, B> B coalesce(A a, Iterable<X> opSymbols, BiFunction<X, A, B> opExecutor) {
        B result = null;

        for(X opSymbol : opSymbols) {
            try {
                result = opExecutor.apply(opSymbol, a);
                break;
            } catch(Exception e) {
                continue;
            }
        }

        if(result == null) {
            throw new IllegalArgumentException("No converter could handle argument " + a);
        }

        return result;
    }

    @Override
    protected B doForward(A a) {
        B result = coalesce(a, converters, (c, x) -> c.convert(x));
        return result;
    }

    @Override
    protected A doBackward(B b) {
        A result = coalesce(b, converters, (c, x) -> c.reverse().convert(x));
        return result;
    }

    @SafeVarargs
    public static <A, B> ConverterChain<A, B> create(Converter<A, B> ... converters) {
        return new ConverterChain<>(Arrays.asList(converters));
    }
}
