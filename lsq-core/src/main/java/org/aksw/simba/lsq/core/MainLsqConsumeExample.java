package org.aksw.simba.lsq.core;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.op.OperatorOrderedGroupBy;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.riot.Lang;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainLsqConsumeExample {
    public static void main(String[] args) {
//        nativeGroupByExample();
//        orderedGroupByExample();
        consumeLsqExample(args);
    }

    public static void orderedGroupByExample() {
        Iterable<Long> vals = () -> LongStream.range(0, 10000000).iterator();

        Flowable<Entry<Long, Long>> flow = Flowable
                .fromIterable(vals)
                .lift(OperatorOrderedGroupBy.create(
                        v -> (v / 10) * 10,
                        x -> new AtomicLong(), (acc, x) -> acc.incrementAndGet()))
                .map(g -> Maps.immutableEntry(g.getKey(), g.getValue().get()))
                ;
        Iterable<?> items = flow.blockingIterable();
//        Iterable<?> items = flow.toList().blockingGet();

        for(Object x : items) {
            System.out.println(x);
        }
    }

    public static void nativeGroupByExample() {
        Iterable<Long> vals = () -> LongStream.range(0, 10000000).iterator();

        Flowable<Entry<Long, Long>> flow = Flowable
                .fromIterable(vals)
                .groupBy(v -> (v / 10) * 10)
                .flatMapSingle(g -> g.count().map(v -> Maps.immutableEntry(g.getKey(), v)))
                ;

//        flow.subscribe(x -> System.out.println(x));
        Iterable<?> items = flow.blockingIterable();

        for(Object x : items) {
            System.out.println(x);
        }
    }


    public static void consumeLsqExample(String[] args) {
        boolean useIterable = true;
        if(useIterable) {
            Iterable<Object> items = RDFDataMgrRx.createFlowableDatasets("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
                .map(x -> (Object)x)
                .blockingIterable()
                ;

            for(Object item : items) {
                System.out.println(item);
            }

        } else {
            RDFDataMgrRx.createFlowableResources("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
                .subscribeOn(Schedulers.io())
                .map(r -> r.as(LsqQuery.class))
                .blockingSubscribe(item -> {

                    System.out.println(item + " ---------------------------------------------");
                    System.out.println(item.getText());
                    // System.out.println("  " + item.getStructuralFeatures().getNumProjectVars());
                });
        }
    }
}
