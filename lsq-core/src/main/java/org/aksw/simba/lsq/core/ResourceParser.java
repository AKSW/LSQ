package org.aksw.simba.lsq.core;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.aksw.jenax.arq.dataset.api.ResourceInDataset;

import io.reactivex.rxjava3.core.Flowable;

public interface ResourceParser {
    Flowable<ResourceInDataset> parse(Callable<InputStream> inSupp);
}
