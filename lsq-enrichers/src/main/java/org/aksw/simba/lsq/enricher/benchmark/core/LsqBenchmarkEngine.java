package org.aksw.simba.lsq.enricher.benchmark.core;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.aksw.jenax.arq.util.binding.ResultSetUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecutionUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.dataaccess.sparql.connection.reconnect.ConnectionLostException;
import org.aksw.simba.lsq.model.LsqBenchmarkParams;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class LsqBenchmarkEngine {
    private static final Logger logger = LoggerFactory.getLogger(LsqBenchmarker.class);

    private long connectionTimeoutForRetrieval;
    private long executionTimeoutForRetrieval;

    private long maxResultCountForCounting;
    private long maxByteSizeForCounting;

    private long maxResultCountForSerialization;
    private long maxByteSizeForSerialization;

    private long connectionTimeoutForCounting;
    private long executionTimeoutForCounting;

    private long maxCount;

    private boolean maxCountAffectsTp;

    private LsqBenchmarkEngine(long connectionTimeoutForRetrieval, long executionTimeoutForRetrieval,
            long maxResultCountForCounting, long maxByteSizeForCounting, long maxResultCountForSerialization,
            long maxByteSizeForSerialization, long connectionTimeoutForCounting, long executionTimeoutForCounting,
            long maxCount, boolean maxCountAffectsTp) {
        super();
        this.connectionTimeoutForRetrieval = connectionTimeoutForRetrieval;
        this.executionTimeoutForRetrieval = executionTimeoutForRetrieval;
        this.maxResultCountForCounting = maxResultCountForCounting;
        this.maxByteSizeForCounting = maxByteSizeForCounting;
        this.maxResultCountForSerialization = maxResultCountForSerialization;
        this.maxByteSizeForSerialization = maxByteSizeForSerialization;
        this.connectionTimeoutForCounting = connectionTimeoutForCounting;
        this.executionTimeoutForCounting = executionTimeoutForCounting;
        this.maxCount = maxCount;
        this.maxCountAffectsTp = maxCountAffectsTp;
    }

    /*
    * Benchmark the combined execution and retrieval time of a given query
    *
    * @param query
    * @param queryExecRes
    * @param qef
    */
   public QueryExec benchmark(SparqlQueryConnection conn, QueryExec result, String queryStr) {


       boolean exceededMaxResultCountForSerialization = false;
       boolean exceededMaxByteSizeForSerialization = false;

       boolean exceededMaxResultCountForCounting = false;
       boolean exceededMaxByteSizeForCounting = false;

       Instant now = Instant.now();
       ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
       Calendar cal = GregorianCalendar.from(zdt);
       XSDDateTime xsdDateTime = new XSDDateTime(cal);

       result.setTimestamp(xsdDateTime);

       Query query;

       try {
           query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
       } catch (Exception e) {
           logger.warn("Skipping benchmark because query failed to parse", e);
           return result;
       }

       // TODO For COUNT queries add the LSQ.countValue to the output model

       Stopwatch evalSw = Stopwatch.createStarted(); // Total time spent evaluating

       List<String> varNames = new ArrayList<>();

       boolean isResultCountComplete = false;
       long itemCount = 0; // We could use rs.getRowNumber() but let's not rely on it
       List<Binding> cache = new ArrayList<>();

       if (maxResultCountForCounting != 0 && maxByteSizeForCounting != 0) {
           logger.info("Benchmarking " + queryStr);
           Stopwatch retrievalSw = Stopwatch.createStarted();

           try(QueryExecution qe = conn.newQuery()
                   .timeout(executionTimeoutForRetrieval, TimeUnit.MILLISECONDS)
                   .query(query)
                   .build()) {
               // https://github.com/apache/jena/issues/1384
               // qe.setTimeout(connectionTimeoutForRetrieval, executionTimeoutForRetrieval);
               // qe.setTimeout(executionTimeoutForRetrieval);

               ResultSet rs = qe.execSelect();
               varNames.addAll(rs.getResultVars());

               long estimatedByteSize = 0;

               while(rs.hasNext()) {
                   ++itemCount;

                   Binding binding = rs.nextBinding();

                   if(cache != null) {
                       // Estimate the size of the binding (e.g. I once had polygons in literals of size 50MB)
                       long bindingSizeContrib = binding.toString().length();
                       estimatedByteSize += bindingSizeContrib;

                       exceededMaxResultCountForSerialization = maxResultCountForSerialization >= 0
                               && itemCount > maxResultCountForSerialization;

                       if(exceededMaxResultCountForSerialization) {
                           // Disable serialization but keep on counting
                           cache = null;
                       }

                       exceededMaxByteSizeForSerialization = maxByteSizeForSerialization >= 0
                               && estimatedByteSize > maxByteSizeForSerialization;
                       if(exceededMaxByteSizeForSerialization) {
                           // Disable serialization but keep on counting
                           cache = null;
                       }


                       if(cache != null) {
                           cache.add(binding);
                       }
                   }

                   exceededMaxResultCountForCounting = maxResultCountForCounting >= 0
                           && itemCount > maxResultCountForCounting;
                   if(exceededMaxByteSizeForSerialization) {
                       break;
                   }

                   exceededMaxByteSizeForCounting = maxByteSizeForCounting >= 0
                           && estimatedByteSize > maxByteSizeForCounting;
                   if(exceededMaxByteSizeForSerialization) {
                       break;
                   }
               }

               if(exceededMaxResultCountForSerialization) {
                   result.setExceededMaxResultCountForSerialization(exceededMaxResultCountForSerialization);
               }

               if(exceededMaxByteSizeForSerialization) {
                   result.setExceededMaxByteSizeForSerialization(exceededMaxByteSizeForSerialization);
               }

               if(exceededMaxResultCountForCounting) {
                   result.setExceededMaxResultCountForCounting(exceededMaxResultCountForCounting);
               }

               if(exceededMaxByteSizeForCounting) {
                   result.setExceededMaxByteSizeForCounting(exceededMaxByteSizeForCounting);
               }

               // Try obtaining a count with a separate query
               isResultCountComplete = !exceededMaxResultCountForCounting && !exceededMaxByteSizeForCounting;
//               } catch (QueryExecException ce) {
//                   // FIXME
           } catch (ConnectionLostException e) {
               throw new ConnectionLostException(e);
           } catch (Exception e) {

               // Set the cache to null so we don't serialize result sets of failed queries
               cache = null;

               logger.warn("Retrieval error: ", e);
//                   String errorMsg = Optional.ofNullable(ExceptionUtils.getRootCause(e)).orElse(e).getMessage();
               String errorMsg = ExceptionUtils.getStackTrace(e);
               result.setRetrievalError(errorMsg);
           }

           BigDecimal retrievalDuration = new BigDecimal(retrievalSw.stop().elapsed(TimeUnit.NANOSECONDS))
                   .divide(new BigDecimal(1000000000));

           result.setRetrievalDuration(retrievalDuration);
       }


       if (!isResultCountComplete) {
           // Try to count using a query and discard the current elapsed time

           Long countItemLimit = maxCount >= 0 ? maxCount : null;
           // SparqlRx.fetchCountQuery(conn, query, countItemLimit, null)
           Stopwatch countingSw = null;
           try {

               // Check whether to disable thee count limit for single pattern queries
               if (!maxCountAffectsTp && countItemLimit != null) {
                   Map<Resource, Integer> features = ElementVisitorFeatureExtractor.getFeatures(query);

                   // Triple patterns and triple paths are counted separately so we need to sum them up
                   int tpCount = features.getOrDefault(LSQ.TriplePattern, 0)
                           + features.getOrDefault(LSQ.TriplePath, 0);

                   if (tpCount == 1) {
                       countItemLimit = null;
                   }
               }


               Entry<Var, Query> queryAndVar = QueryGenerationUtils.createQueryCount(query, countItemLimit, null);

               Var countVar = queryAndVar.getKey();
               Query countQuery = queryAndVar.getValue();

               if (logger.isInfoEnabled()) {
                   logger.info("Counting " + countQuery);
               }

               countingSw = Stopwatch.createStarted();

               try(QueryExecution qe = conn.newQuery()
                       .query(countQuery)
                       .timeout(executionTimeoutForCounting, TimeUnit.MILLISECONDS)
                       .build()) {
                   // qe.setTimeout(connectionTimeoutForCounting, executionTimeoutForCounting);
                   // https://github.com/apache/jena/issues/1384
                   // qe.setTimeout(executionTimeoutForCounting);
                   Number count = QueryExecutionUtils.fetchNumber(qe, countVar);
                   if(count != null) {
                       itemCount = count.longValue();

                       isResultCountComplete = countItemLimit == null || itemCount < countItemLimit;
                   }
               }
           } catch (ConnectionLostException e) {
               throw new ConnectionLostException(e);
           } catch(Exception e) {
               if (logger.isWarnEnabled()) {
                   logger.warn("Counting error: ", e);
               }
//                   String errorMsg = Optional.ofNullable(ExceptionUtils.getRootCause(e)).orElse(e).getMessage();
               String errorMsg = ExceptionUtils.getStackTrace(e);
               result.setCountingError(errorMsg);
           }

           if (countingSw != null) {
               BigDecimal countingDuration = new BigDecimal(countingSw.stop().elapsed(TimeUnit.NANOSECONDS))
                       .divide(new BigDecimal(1000000000));

               result.setCountDuration(countingDuration);
           }
       }

       if(isResultCountComplete) {
           result.setResultSetSize(itemCount);
       }

       if(cache != null) {
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ResultSet replay = ResultSetUtils.create(varNames, cache.iterator());
           ResultSetFormatter.outputAsJSON(baos, replay);
           result.setSerializedResult(baos.toString());
       }


       BigDecimal evalDuration = new BigDecimal(evalSw.stop().elapsed(TimeUnit.NANOSECONDS))
               .divide(new BigDecimal(1000000000));


       result.setEvalDuration(evalDuration);

       if(logger.isInfoEnabled()) {
           String errMsg = result.getRetrievalError();
           logger.info("Benchmark result after " + evalDuration + " seconds: " + result.getResultSetSize() + " results"
                   + (errMsg == null ? " (success)" : " and error message: " + errMsg));
       }

       return result;
       //Calendar end = Calendar.getInstance();
       //Duration duration = Duration.between(start.toInstant(), end.toInstant());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal rawConnectionTimeoutForRetrieval;
        private BigDecimal rawExecutionTimeoutForRetrieval;
        private Long rawMaxResultCountForCounting;
        private Long rawMaxByteSizeForCounting;
        private Long rawMaxResultCountForSerialization;
        private Long rawMaxByteSizeForSerialization;
        private BigDecimal rawConnectionTimeoutForCounting;
        private BigDecimal rawExecutionTimeoutForCounting;
        private Long rawMaxCount;
        private Boolean rawMaxCountAffectsTp;

        private Builder() { super(); }

        public Builder setConnectionTimeoutForRetrieval(BigDecimal rawConnectionTimeoutForRetrieval) {
            this.rawConnectionTimeoutForRetrieval = rawConnectionTimeoutForRetrieval;
            return this;
        }

        public Builder setExecutionTimeoutForRetrieval(BigDecimal rawExecutionTimeoutForRetrieval) {
            this.rawExecutionTimeoutForRetrieval = rawExecutionTimeoutForRetrieval;
            return this;
        }

        public Builder setMaxResultCountForCounting(Long rawMaxResultCountForCounting) {
            this.rawMaxResultCountForCounting = rawMaxResultCountForCounting;
            return this;
        }

        public Builder setMaxByteSizeForCounting(Long rawMaxByteSizeForCounting) {
            this.rawMaxByteSizeForCounting = rawMaxByteSizeForCounting;
            return this;
        }

        public Builder setMaxResultCountForSerialization(Long rawMaxResultCountForSerialization) {
            this.rawMaxResultCountForSerialization = rawMaxResultCountForSerialization;
            return this;
        }

        public Builder setMaxByteSizeForSerialization(Long rawMaxByteSizeForSerialization) {
            this.rawMaxByteSizeForSerialization = rawMaxByteSizeForSerialization;
            return this;
        }

        public Builder setConnectionTimeoutForCounting(BigDecimal rawConnectionTimeoutForCounting) {
            this.rawConnectionTimeoutForCounting = rawConnectionTimeoutForCounting;
            return this;
        }

        public Builder setExecutionTimeoutForCounting(BigDecimal rawExecutionTimeoutForCounting) {
            this.rawExecutionTimeoutForCounting = rawExecutionTimeoutForCounting;
            return this;
        }

        public Builder setMaxCount(Long rawMaxCount) {
            this.rawMaxCount = rawMaxCount;
            return this;
        }

        public Builder setMaxCountAffectsTp(Boolean rawMaxCountAffectsTp) {
            this.rawMaxCountAffectsTp = rawMaxCountAffectsTp;
            return this;
        }

        /** Override the state of the builder with the given params. */
        public Builder setParams(LsqBenchmarkParams params) {
            setExecutionTimeoutForRetrieval(params.getExecutionTimeoutForRetrieval());
            setConnectionTimeoutForRetrieval(params.getConnectionTimeoutForRetrieval());
            setMaxResultCountForCounting(params.getMaxResultCountForCounting());
            setMaxByteSizeForCounting(params.getMaxByteSizeForCounting());
            setMaxResultCountForSerialization(params.getMaxResultCountForSerialization());
            setMaxByteSizeForSerialization(params.getMaxByteSizeForSerialization());
            setExecutionTimeoutForCounting(params.getExecutionTimeoutForCounting());
            setConnectionTimeoutForCounting(params.getConnectionTimeoutForCounting());
            setMaxCount(params.getMaxCount());
            setMaxCountAffectsTp(params.getMaxCountAffectsTp());
            return this;
        }

        public LsqBenchmarkEngine build() {
            long connectionTimeoutForRetrieval = toMillis(rawConnectionTimeoutForRetrieval);
            long executionTimeoutForRetrieval = toMillis(rawExecutionTimeoutForRetrieval);

            long maxResultCountForCounting = Optional.ofNullable(rawMaxResultCountForCounting).orElse(-1l);
            long maxByteSizeForCounting = Optional.ofNullable(rawMaxByteSizeForCounting).orElse(-1l);

            long maxResultCountForSerialization = Optional.ofNullable(rawMaxResultCountForSerialization).orElse(-1l);
            long maxByteSizeForSerialization = Optional.ofNullable(rawMaxByteSizeForSerialization).orElse(-1l);

            long connectionTimeoutForCounting = toMillis(rawConnectionTimeoutForCounting);
            long executionTimeoutForCounting = toMillis(rawExecutionTimeoutForCounting);

            long maxCount = Optional.ofNullable(rawMaxCount).orElse(-1l);

            boolean maxCountAffectsTp = Optional.ofNullable(rawMaxCountAffectsTp).orElse(false);

            return new LsqBenchmarkEngine(connectionTimeoutForRetrieval, executionTimeoutForRetrieval, maxResultCountForCounting,
                maxByteSizeForCounting, maxResultCountForSerialization, maxByteSizeForSerialization,
                connectionTimeoutForCounting, executionTimeoutForCounting, maxCount, maxCountAffectsTp);
        }

        private static long toMillis(BigDecimal d) {
            return Optional.ofNullable(d).map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);
        }
    }
}
