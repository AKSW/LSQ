package org.aksw.simba.lsq.model;

import java.math.BigDecimal;

import org.aksw.commons.beans.model.PropertyUtils;

/**
 * Interface for benchmark parameters
 *
 * @author raven
 *
 */
public interface LsqBenchmarkParamsMutable
    extends LsqBenchmarkParams
{
    LsqBenchmarkParamsMutable setConnectionTimeoutForRetrieval(BigDecimal duration);
    LsqBenchmarkParamsMutable setExecutionTimeoutForRetrieval(BigDecimal duration);
    LsqBenchmarkParamsMutable setConnectionTimeoutForCounting(BigDecimal duration);
    LsqBenchmarkParamsMutable setExecutionTimeoutForCounting(BigDecimal duration);
    LsqBenchmarkParamsMutable setMaxResultCountForCounting(Long maxItemCountForCounting);
    LsqBenchmarkParamsMutable setMaxByteSizeForCounting(Long maxByteSizeForCounting);
    LsqBenchmarkParamsMutable setMaxResultCountForSerialization(Long maxItemCountForSerialization);
    LsqBenchmarkParamsMutable setMaxByteSizeForSerialization(Long maxByteSizeForSerialization);
    LsqBenchmarkParamsMutable setMaxCount(Long maxItemCountForCounting);
    LsqBenchmarkParamsMutable setMaxCountAffectsTp(Boolean offOrOn);

    public static LsqBenchmarkParamsMutable copy(LsqBenchmarkParamsMutable dest, LsqBenchmarkParamsMutable src, boolean copyNulls) {
        // XXX BeanCopyUtils?
        PropertyUtils.apply(copyNulls, dest::setExecutionTimeoutForRetrieval, src::getExecutionTimeoutForRetrieval);
        PropertyUtils.apply(copyNulls, dest::setConnectionTimeoutForRetrieval, src::getConnectionTimeoutForRetrieval);
        PropertyUtils.apply(copyNulls, dest::setMaxResultCountForCounting, src::getMaxResultCountForCounting);
        PropertyUtils.apply(copyNulls, dest::setMaxByteSizeForCounting, src::getMaxByteSizeForCounting);
        PropertyUtils.apply(copyNulls, dest::setMaxResultCountForSerialization, src::getMaxResultCountForSerialization);
        PropertyUtils.apply(copyNulls, dest::setMaxByteSizeForSerialization, src::getMaxByteSizeForSerialization);
        PropertyUtils.apply(copyNulls, dest::setExecutionTimeoutForCounting, src::getExecutionTimeoutForCounting);
        PropertyUtils.apply(copyNulls, dest::setConnectionTimeoutForCounting, src::getConnectionTimeoutForCounting);
        PropertyUtils.apply(copyNulls, dest::setMaxCount, src::getMaxCount);
        PropertyUtils.apply(copyNulls, dest::setMaxCountAffectsTp, src::getMaxCountAffectsTp);
        return dest;
    }

    public static void setDefaults(LsqBenchmarkParamsMutable params) {
        params
            .setExecutionTimeoutForRetrieval(new BigDecimal(300))
            .setConnectionTimeoutForRetrieval(new BigDecimal(60))
            .setMaxResultCountForCounting(1000000l) // 1M
            .setMaxByteSizeForCounting(-1l) // limit only by count
            .setMaxResultCountForSerialization(-1l) // limit by byte size
            .setMaxByteSizeForSerialization(1000000l) // 1MB
            .setExecutionTimeoutForCounting(new BigDecimal(300))
            .setConnectionTimeoutForCounting(new BigDecimal(60))
            .setMaxCount(1000000000l)
            .setMaxCountAffectsTp(false);
    }
}
