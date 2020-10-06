package org.aksw.simba.lsq.core;

import java.math.BigDecimal;

/**
 * Interface for benchmark parameters
 *
 * @author raven
 *
 */
public interface BenchmarkParams {

    BigDecimal getConnectionTimeoutForRetrieval();
    BenchmarkParams setConnectionTimeoutForRetrieval(BigDecimal duration);

    BigDecimal getExecutionTimeoutForRetrieval();
    BenchmarkParams setExecutionTimeoutForRetrieval(BigDecimal duration);

    BigDecimal getConnectionTimeoutForCounting();
    BenchmarkParams setConnectionTimeoutForCounting(BigDecimal duration);

    BigDecimal getExecutionTimeoutForCounting();
    BenchmarkParams setExecutionTimeoutForCounting(BigDecimal duration);

    Long getMaxResultCountForCounting();
    BenchmarkParams setMaxResultCountForCounting(Long maxItemCountForCounting);

    Long getMaxByteSizeForCounting();
    BenchmarkParams setMaxByteSizeForCounting(Long maxByteSizeForCounting);

    Long getMaxResultCountForSerialization();
    BenchmarkParams setMaxResultCountForSerialization(Long maxItemCountForSerialization);

    Long getMaxByteSizeForSerialization();
    BenchmarkParams setMaxByteSizeForSerialization(Long maxByteSizeForSerialization);

    Long getMaxCount();
    BenchmarkParams setMaxCount(Long maxItemCountForCounting);

    Boolean getMaxCountAffectsTp();
    BenchmarkParams setMaxCountAffectsTp(Boolean offOrOn);
}
