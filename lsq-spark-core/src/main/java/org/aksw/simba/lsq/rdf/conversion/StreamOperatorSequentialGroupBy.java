package org.aksw.simba.lsq.rdf.conversion;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;


/**
 * Base class with core attributes for partial reduction of consecutive items
 * sharing some attribute (referred to as group key).
 *
 * This class is shared between specification and operation implementations.
 *
 * The most similar implementation I am aware of is
 * https://github.com/amaembo/streamex/blob/master/src/main/java/one/util/streamex/CollapseSpliterator.java
 *
 * There are minor differences in the models though:
 * - Sequential group by only creates a single accumulator per group and feeds all consecutive items to it.
 * - StreamEx seems to create multiple accumulators together with a combine function
 *
 * When accumulating consecutive triples into a graph it does not make much sense having to combine
 * individual graphs as this is quite an expensive operation due to the unnecessary indexing overhead involved.
 * (i.e. indexing of triples only to combine them afterwards anyway)
 *
 * @author raven
 *
 * @param <T>
 * @param <K>
 * @param <V>
 */
class SequentialGroupByBase<T, K, V> {
    /* Function to derive a group key from an item in the flow */
    protected Function<? super T, ? extends K> getGroupKey;

    /* Comparison whether two group keys are equal */
    protected BiPredicate<? super K, ? super K> groupKeyCompare;

    /* Constructor function for accumulators. Receives item index and group key */
    protected BiFunction<? super Long, ? super K, ? extends V> accCtor;

    /* Reduce an item with the accumulator to obtain a new accumulator */
    protected BiFunction<? super V, ? super T, ? extends V> accAdd;

    public SequentialGroupByBase(SequentialGroupByBase<T, K, V> other) {
        super();
        this.getGroupKey = other.getGroupKey;
        this.groupKeyCompare = other.groupKeyCompare;
        this.accCtor = other.accCtor;
        this.accAdd = other.accAdd;
    }


    public SequentialGroupByBase(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        super();
        this.getGroupKey = getGroupKey;
        this.groupKeyCompare = groupKeyCompare;
        this.accCtor = accCtor;
        this.accAdd = accAdd;
    }
}


class SequentialGroupByOperationBase<T, K, V>
    extends SequentialGroupByBase<T, K, V>
{

    public SequentialGroupByOperationBase(SequentialGroupBySpec<T, K, V> other) {
        super(other);
    }

    public class AccumulatorBase
    {
        protected K priorKey;
        protected K currentKey;

        // Number of created accumulators; incremented after accCtor invocation
        protected long accNum = 0;
        protected V currentAcc = null;

        protected boolean lastItemSent = false;
    }
}

/**
 * Specification implementation.
 * Provides several create methods for constructing a spec.
 *
 * @author raven
 *
 * @param <T> Item type
 * @param <K> Group key type
 * @param <V> Accumulator type
 */
class SequentialGroupBySpec<T, K, V>
    extends SequentialGroupByBase<T, K, V>
{
    public SequentialGroupBySpec(SequentialGroupByBase<T, K, V> other) {
        super(other);
    }

    public SequentialGroupBySpec(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        super(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor is a mere supplier (and thus neither depends on the accumulator count nor the group Key)</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> SequentialGroupBySpec<T, K, V> createAcc(
            Function<? super T, ? extends K> getGroupKey,
            Supplier<? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return create(getGroupKey, Objects::equals, groupKey -> accCtor.get(), accAdd);
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> SequentialGroupBySpec<T, K, V> createAcc(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> SequentialGroupBySpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the number of so-far created accumulators (starting with 0) and the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> SequentialGroupBySpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }

    public static <T, K, V> SequentialGroupBySpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return new SequentialGroupBySpec<>(getGroupKey, groupKeyCompare, (accNum, key) -> accCtor.apply(key), accAdd);
    }

    public static <T, K, V> SequentialGroupBySpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return new SequentialGroupBySpec<>(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }
}



public class StreamOperatorSequentialGroupBy<T, K, V>
    extends SequentialGroupByOperationBase<T, K, V>
{
    public static <T, K, V> StreamOperatorSequentialGroupBy<T, K, V> create(SequentialGroupBySpec<T, K, V> spec) {
        return new StreamOperatorSequentialGroupBy<>(spec);
    }

    public StreamOperatorSequentialGroupBy(SequentialGroupBySpec<T, K, V> other) {
        super(other);
    }

    /** Low-level iterator-based transformation */
    public Iterator<Entry<K, V>> transform(Iterator<T> input) {
        return new OperatorImpl(input).getOutput();
    }

    /** Stream-based transformation - relies on the iterator-based one */
    public Stream<Entry<K, V>> transform(Stream<T> input) {
        return Streams.stream(transform(input.sequential().iterator()));
    }

//    interface IteratorDelegate<T>
//        extends Iterator<T>
//    {
//        Iterator<T> getDelegate();
//
//        @Override default boolean hasNext() { return getDelegate().hasNext(); }
//        @Override default T next() { return getDelegate().next(); }
//        @Override default void remove() { getDelegate().remove(); }
//    }

    /**
     *
     * TODO This class could be turned into a spliterator as follows:
     * Upon splitting, consume one list of items with the same group key from the rhs.
     * Append this list to the spliterator of the lhs.
     *
     * @author raven
     *
     */
    public class OperatorImpl
        extends AccumulatorBase
    {
        protected Iterator<T> input;
        protected Iterator<Entry<K, V>> output;

        public OperatorImpl(Iterator<T> input) {
            super();
            this.input = input;
            this.output = new InternalIterator();
        }

        public Iterator<Entry<K, V>> getOutput() {
            return output;
        }

        class InternalIterator extends AbstractIterator<Entry<K, V>> {

            @Override
            protected Entry<K, V> computeNext() {

                Entry<K, V> result = null;

                while (input.hasNext() && result == null) {
                    T item = input.next();

                    currentKey = getGroupKey.apply(item);

                    if (accNum == 0) {
                        // First time init
                        priorKey = currentKey;
                        currentAcc = accCtor.apply(accNum, currentKey);

                        // Objects.requireNonNull(currentAcc, "Got null for an accumulator");
                        ++accNum;
                    } else if (!groupKeyCompare.test(priorKey, currentKey)) {

                        result = new SimpleEntry<>(priorKey, currentAcc);

                        currentAcc = accCtor.apply(accNum, currentKey);
                        // Objects.requireNonNull(currentAcc, "Got null for an accumulator");
                        ++accNum;
                    }

                    if (currentAcc != null) {
                        currentAcc = accAdd.apply(currentAcc, item);
                    }

                    priorKey = currentKey;
                }

                // We only come here if either we have
                // a (non-null) result or input.hasNext() is false
                if (result == null) {
                    if (accNum != 0 && !lastItemSent) {
                        result = new SimpleEntry<>(currentKey, currentAcc);
                        lastItemSent = true;
                    } else {
                        result = endOfData();
                    }
                }

                return result;
            }
        }
    }


    public static void main(String[] args) {
        Stream<Integer> s = Stream.of(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 1, 2, 3, 4);

        StreamOperatorSequentialGroupBy<Integer, Integer, Integer> op =
                StreamOperatorSequentialGroupBy.create(SequentialGroupBySpec.createAcc(
                        i -> i,
                        () -> 0,
                        (acc, item) -> acc + 1));


        List<Entry<Integer, Integer>> actual = op.transform(s).collect(Collectors.toList());
        List<Entry<Integer, Integer>> expected = Arrays.asList(
            new SimpleEntry<>(1, 1),
            new SimpleEntry<>(2, 2),
            new SimpleEntry<>(3, 3),
            new SimpleEntry<>(4, 4),
            new SimpleEntry<>(1, 1),
            new SimpleEntry<>(2, 1),
            new SimpleEntry<>(3, 1),
            new SimpleEntry<>(4, 1)
        );

        System.out.println(expected);
        System.out.println(actual);
    }

}
