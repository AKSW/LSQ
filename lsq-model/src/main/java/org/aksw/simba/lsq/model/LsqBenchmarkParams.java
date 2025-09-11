package org.aksw.simba.lsq.model;

import java.math.BigDecimal;

public interface LsqBenchmarkParams {
    BigDecimal getConnectionTimeoutForRetrieval();
    BigDecimal getExecutionTimeoutForRetrieval();
    BigDecimal getConnectionTimeoutForCounting();
    BigDecimal getExecutionTimeoutForCounting();
    Long getMaxResultCountForCounting();
    Long getMaxByteSizeForCounting();
    Long getMaxResultCountForSerialization();
    Long getMaxByteSizeForSerialization();
    Long getMaxCount();
    Boolean getMaxCountAffectsTp();
}
