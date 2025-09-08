package org.aksw.simba.lsq.enricher.benchmark.core;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lsq.model.LsqQuery;

/**
 * A (query) pack comprises a primary lsq query and a list of secondary lsq queries.
 * The secondary ones are derived from elements (such as BGPs, TPs) from the primary one.
 */
public record QueryPack(LsqQuery primaryQuery, List<LsqQuery> secondaryQueries) {
    public List<LsqQuery> list() {
        List<LsqQuery> result = new ArrayList<>(1 + secondaryQueries.size());
        result.add(primaryQuery);
        result.addAll(secondaryQueries);
        return result;
    }
}

