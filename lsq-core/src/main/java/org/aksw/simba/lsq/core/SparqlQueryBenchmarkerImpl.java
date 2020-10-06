package org.aksw.simba.lsq.core;

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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.connection.ConnectionLostException;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlQueryBenchmarkerImpl
    implements SparqlQueryBenchmarker
{
    private static final Logger logger = LoggerFactory.getLogger(SparqlQueryBenchmarkerImpl.class);

    protected BenchmarkParams config;

    public SparqlQueryBenchmarkerImpl(BenchmarkParams config) {
        super();
        this.config = Objects.requireNonNull(config, "Config must not be null");
    }

//    @Override
//    public void benchmark(String queryStr, QueryExec result) {
//        Query query;
//        try {
//            query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
//        } catch (Exception e) {
//            logger.warn("Skipping benchmark because query failed to parse", e);
//            //return result;
//            return;
//        }
//
//        benchmark(query, result);
//    }

    @Override
    public void benchmark(
            SparqlQueryConnection conn,
            Query query,
            QueryExec result) {

        long connectionTimeoutForRetrieval = Optional.ofNullable(config.getConnectionTimeoutForRetrieval())
                .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);
        long executionTimeoutForRetrieval = Optional.ofNullable(config.getExecutionTimeoutForRetrieval())
                .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);

        long maxResultCountForCounting = Optional.ofNullable(config.getMaxResultCountForCounting()).orElse(-1l);
        long maxByteSizeForCounting = Optional.ofNullable(config.getMaxByteSizeForCounting()).orElse(-1l);

        long maxResultCountForSerialization = Optional.ofNullable(config.getMaxResultCountForSerialization()).orElse(-1l);
        long maxByteSizeForSerialization = Optional.ofNullable(config.getMaxByteSizeForSerialization()).orElse(-1l);

        long connectionTimeoutForCounting = Optional.ofNullable(config.getConnectionTimeoutForCounting())
                .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);
        long executionTimeoutForCounting = Optional.ofNullable(config.getExecutionTimeoutForCounting())
                .map(x -> x.multiply(new BigDecimal(1000)).longValue()).orElse(-1l);

        long maxCount = Optional.ofNullable(config.getMaxCount()).orElse(-1l);

        boolean maxCountAffectsTp = Optional.ofNullable(config.getMaxCountAffectsTp()).orElse(false);



        boolean exceededMaxResultCountForSerialization = false;
        boolean exceededMaxByteSizeForSerialization = false;

        boolean exceededMaxResultCountForCounting = false;
        boolean exceededMaxByteSizeForCounting = false;


        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
        Calendar cal = GregorianCalendar.from(zdt);
        XSDDateTime xsdDateTime = new XSDDateTime(cal);

        result.setTimestamp(xsdDateTime);

        Stopwatch evalSw = Stopwatch.createStarted(); // Total time spent evaluating

        List<String> varNames = new ArrayList<>();

        boolean isResultCountComplete = false;
        long itemCount = 0; // We could use rs.getRowNumber() but let's not rely on it
        List<Binding> cache = new ArrayList<>();

        if (maxResultCountForCounting != 0 && maxByteSizeForCounting != 0) {
            logger.info("Benchmarking " + query);
            Stopwatch retrievalSw = Stopwatch.createStarted();

            try(QueryExecution qe = conn.query(query)) {
                qe.setTimeout(connectionTimeoutForRetrieval, executionTimeoutForRetrieval);

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
//	                } catch (QueryExecException ce) {
//	                   // FIXME
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

                logger.info("Counting " + countQuery);

                countingSw = Stopwatch.createStarted();

                try(QueryExecution qe = conn.query(countQuery)) {
                    qe.setTimeout(connectionTimeoutForCounting, executionTimeoutForCounting);

                    Number count = ServiceUtils.fetchNumber(qe, countVar);
                    if(count != null) {
                        itemCount = count.longValue();

                        isResultCountComplete = countItemLimit == null || itemCount < countItemLimit;
                    }
                }
            } catch (ConnectionLostException e) {
                throw new ConnectionLostException(e);
            } catch(Exception e) {
                logger.warn("Counting error: ", e);
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

        logger.info("Benchmark result after " + evalDuration + " seconds: " + result.getResultSetSize() + " results and error message " + result.getRetrievalError());

//        return result;
     }

}
