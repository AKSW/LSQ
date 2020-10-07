package org.aksw.simba.lsq.core;

import java.math.BigDecimal;

/**
 * Interface for benchmark parameters
 *
 * @author raven
 *
 */
public interface LsqBenchmarkParams {

    BigDecimal getConnectionTimeoutForRetrieval();
    LsqBenchmarkParams setConnectionTimeoutForRetrieval(BigDecimal duration);

    BigDecimal getExecutionTimeoutForRetrieval();
    LsqBenchmarkParams setExecutionTimeoutForRetrieval(BigDecimal duration);

    BigDecimal getConnectionTimeoutForCounting();
    LsqBenchmarkParams setConnectionTimeoutForCounting(BigDecimal duration);

    BigDecimal getExecutionTimeoutForCounting();
    LsqBenchmarkParams setExecutionTimeoutForCounting(BigDecimal duration);

    Long getMaxResultCountForCounting();
    LsqBenchmarkParams setMaxResultCountForCounting(Long maxItemCountForCounting);

    Long getMaxByteSizeForCounting();
    LsqBenchmarkParams setMaxByteSizeForCounting(Long maxByteSizeForCounting);

    Long getMaxResultCountForSerialization();
    LsqBenchmarkParams setMaxResultCountForSerialization(Long maxItemCountForSerialization);

    Long getMaxByteSizeForSerialization();
    LsqBenchmarkParams setMaxByteSizeForSerialization(Long maxByteSizeForSerialization);

    Long getMaxCount();
    LsqBenchmarkParams setMaxCount(Long maxItemCountForCounting);

    Boolean getMaxCountAffectsTp();
    LsqBenchmarkParams setMaxCountAffectsTp(Boolean offOrOn);
}
