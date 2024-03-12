package org.aksw.simba.lsq.enricher.core;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.simba.lsq.model.LsqQuery;

public interface LsqEnricher
    extends SerializableFunction<LsqQuery, LsqQuery> //LsqQuery, LsqQuery?
{
}
