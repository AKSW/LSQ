package org.aksw.simba.lsq.core;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.riot.Lang;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainLsqConsumeExample {
    public static void main(String[] args) {
        if(false) {
            Iterable<?> items = RDFDataMgrRx.createFlowableDatasets("../tmp/2020-06-27-wikidata-one-day.trig", Lang.TRIG, null)
                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.computation())
                //.blockingSubscribe(System.out::println)
                .blockingIterable()
                ;
                //.blockingIterable(1);

            for(Object item : items) {
                System.out.println(item);
    //            System.out.println("  " + item.getStructuralFeatures().getNumProjectVars());
            }
        }

        if(true) {
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
