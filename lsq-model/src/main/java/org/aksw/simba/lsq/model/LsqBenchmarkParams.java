package org.aksw.simba.lsq.model;

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

    public static void setDefaults(LsqBenchmarkParams params) {
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
            // .benchmarkSecondaryQueries(true)
            // .setDatasetSize(datasetSize)
            // .setDatasetLabel(datasetLabel)
            // .setDatasetIri(datasetIri)
            // .setBaseIri(baseIri)
    }
}
