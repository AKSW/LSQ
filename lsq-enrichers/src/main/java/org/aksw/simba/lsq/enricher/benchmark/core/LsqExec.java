package org.aksw.simba.lsq.enricher.benchmark.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.simba.lsq.enricher.benchmark.opcache.EvaluatorDispatchWithCaching;
import org.aksw.simba.lsq.enricher.benchmark.opcache.OpExtKeyAndTableSupplier;
import org.aksw.simba.lsq.enricher.benchmark.opcache.TableMgr;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.spinx.model.BgpInfo;
import org.aksw.simba.lsq.spinx.model.BgpNodeExec;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.Bgp;
import org.aksw.simba.lsq.spinx.model.BgpExec;
import org.aksw.simba.lsq.spinx.model.BgpNode;
import org.aksw.simba.lsq.spinx.model.TpExec;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.spinx.model.TpInBgpExec;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.VarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Utilility methods for creating execution resources w.r.t. a given execution ID for the following SPARQL elements:
 * <ul>
 * <li>basic graph pattern (bpg)</li>
 * <li>triple pattern in bgp</li>
 * <li>bgp node</li>
 * <li>triple pattern (tp)</li>
 * </ul>
 *
 * These methods take care of establishing appropriate relations among each other:
 * For example, bpgExecs allow for accessing all related tpInBgpExecs
 *
 * @author raven
 *
 */
public class LsqExec {

    private static final Logger logger = LoggerFactory.getLogger(LsqExec.class);

    public static void createAllExecs(LsqQuery masterQuery, ExperimentRun expRun) {
        Model model = masterQuery.getModel();
        //SpinQueryEx spinRoot = masterQuery.getSpinQuery().as(SpinQueryEx.class);
        BgpInfo spinRoot = masterQuery.getStructuralFeatures();

        Map<Resource, LocalExecution> rleMap = masterQuery.getLocalExecutionMap();
        LocalExecution expRoot = rleMap.get(expRun);

        Long datasetSize = expRun.getExec().getConfig().getDatasetSize();

        /*
         * BgpExecs, TpInBgpExecs and TpExec
         *
         *
         */
        // Iterate the query's. bgps. For each bgp:
        // 1.) get-or-create the execution
        // 2.) then descend into the tp-in-bgp and the tps and update their stats
        for(Bgp bgp : spinRoot.getBgps()) {
            // Get the bgp's execution in this experiment
            LsqExec.createBgpExec(expRun, model, expRoot, bgp);
        }

        // Now that all BgpExecs are set up descend into the tpExecs
        for(BgpExec bgpExec : expRoot.getBgpExecs()) {

            for(TpInBgp tpInBgp : bgpExec.getBgp().getTpInBgp()) {
                TpInBgpExec tpInBgpExec = getOrCreateTpInBgpExec(bgpExec, tpInBgp);
                TpExec tpExec = tpInBgpExec.getTpExec();

                Long tpResultSetSize = tpExec.getQueryExec().getResultSetSize();
                BigDecimal tpSel = safeDivide(tpResultSetSize, datasetSize);
                tpExec
                    .setSelectivity(tpSel);

                Long bgpSize = bgpExec.getQueryExec().getResultSetSize();
                Long tpSize = tpExec.getQueryExec().getResultSetSize();
                BigDecimal value = safeDivide(bgpSize, tpSize);
                tpInBgpExec.setSelectivity(value);
            }

            // Process join vertices ...
            for(BgpNode bgpNode : bgpExec.getBgp().getBgpNodes()) {
                Node jenaNode = bgpNode.toJenaNode();

                // ... they need to be variables
                if(!jenaNode.isVariable()) {
                    continue;
                }

                BgpNodeExec bgpNodeExec = LsqExec.getOrCreateBgpNodeExec(bgpExec, bgpNode);
                logger.info("Allocated bgpNodeExec for " + jenaNode + ": " + ResourceUtils.asBasicRdfNode(bgpNodeExec) + " - " + bgpNodeExec);

                Long bgpNodeSize = bgpNodeExec.getQueryExec().getResultSetSize();
                Long bgpSize = bgpExec.getQueryExec().getResultSetSize();
                BigDecimal value = safeDivide(bgpNodeSize, bgpSize);

                // TODO Maybe don't override if already set
                bgpNodeExec.setBgpRestrictedSelectivitiy(value);

                /*
                 * Sub-bgp executions (partitioned by bgp-node)
                 */

                Bgp subBgp = bgpNode.getSubBgp();
                BgpExec subBgpExec = getOrCreateSubBgpExec(expRun, subBgp);

                // Connect the subBgp to the bgpNodeExec
                bgpNodeExec.setSubBgpExec(subBgpExec);


                QueryExec subBgpQueryExec = subBgpExec.getQueryExec();
                Long subBgpSize = subBgpQueryExec.getResultSetSize();

                Op opSubBgpRs = parseOpTableFromQueryExec(subBgpQueryExec);

                // For each tpInSubBgp compute its
                for(TpInBgp tpInSubBgp : subBgp.getTpInBgp()) {
                    TpInBgpExec tpInSubBgpExec = getOrCreateTpInBgpExec(subBgpExec, tpInSubBgp);
                    TpExec tpExec = tpInSubBgpExec.getTpExec();

                    // LsqTriplePattern tp = tpExec.getTp();
                    QueryExec tpQueryExec = tpExec.getQueryExec();
                    Long tpSize = tpQueryExec.getResultSetSize();

                    BigDecimal subBgpToTpRatio = safeDivide(subBgpSize, tpSize);

                    tpInSubBgpExec.setTpToBgpRatio(subBgpToTpRatio);

                    Op opTpRs = parseOpTableFromQueryExec(tpQueryExec);

                    LsqTriplePattern tp = tpExec.getTp();
                    Triple triple = tp.toJenaTriple();
                    List<Var> tpVars = VarUtils.getVars(triple).stream()
                            .distinct()
                            .sorted((a, b) -> a.getName().compareTo(b.getName()))
                            .collect(Collectors.toList());

                    if(opTpRs != null && opSubBgpRs != null) {
                        ExecutionContext execCxt = null;
                        Multiset<Binding> tpBindings = EvaluatorDispatchWithCaching.evalToMultiset(opTpRs, execCxt);
                        Multiset<Binding> bgpBindings = EvaluatorDispatchWithCaching.evalToMultiset(new OpProject(opSubBgpRs, tpVars), execCxt);
                        //Multiset<Binding> bgpBindings = indexResultSet(subBgpRs, tpVars);new OpProject(tpVars);

                        int compatibleBindingCount = Multisets.intersection(bgpBindings, tpBindings).size();
                        int tpBindingCount = tpBindings.size();

                        BigDecimal bgpRestrictedTpSel = safeDivide(compatibleBindingCount, tpBindingCount);
                        tpExec.setBgpRestrictedTpSel(bgpRestrictedTpSel);
                    }


                    // Compute the ratios and selectivities between each tpInSubBgp and the subBgp


//                    LsqQuery extensionQuery = tp.getExtensionQuery();
////                                RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
//                    Objects.requireNonNull(extensionQuery, "query for a sparql query element (graph pattern) must not be null");
//                    Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
//                    LocalExecution le = leMap.get(expRun);
//                    QueryExec qe = le.getQueryExec();
//
//                    tpExec
//                        .setTpInBgpExec(tpInSubBgpExec) /* inverse link */
//                        .setTp(tp)
//                        .setQueryExec(qe)
//                        ;
                }
            }

        }

    }

//    public static Multiset<Binding> indexResultSet(ResultSet rs, Iterable<Var> vars) {
//        Multiset<Binding> result = HashMultiset.create();
//        while(rs.hasNext()) {
//            Binding o = rs.nextBinding();
//            Binding n = BindingUtils.project(o, vars);
//
//            result.add(n);
//        }
//        return result;
//    }



    /**
     * Utility method with behavior tailored to LSQ.
     *
     * Parse a serialized result from a queryExec object.
     * Returns null if the queryExec or the serializedResult are null or an exception is raised.
     * The exception is passed to the logger.
     *
     * @param queryExec
     * @return
     */
    public static OpExtKeyAndTableSupplier parseOpTableFromQueryExec(QueryExec queryExec) {
        OpExtKeyAndTableSupplier result = null;
        if(queryExec != null) {
            String str = queryExec.getSerializedResult();
            if(str != null) {
                try {
                    String key = queryExec.toString();
                    result = new OpExtKeyAndTableSupplier(key, () ->
                        TableMgr.parseTableFromString(str, ResultSetLang.RS_JSON));
                } catch(Exception e) {
                    logger.warn("Failed to parse a result set", e);
                }
            }
        }
        return result;
    }

    /**
     * Creates a BpgExec resource that is linked to the master query's local execution
     * Note that createSubBgpExec does not create a link to the local execution
     *
     * @param expRun
     * @param model
     * @param expRoot
     * @param bgp
     * @return
     */
    public static BgpExec createBgpExec(RDFNode expRun, Model model, LocalExecution expRoot, Bgp bgp) {
        BgpExec bgpExec = expRoot.findBgpExec(bgp);

        if(bgpExec == null) {
            LsqQuery extensionQuery = bgp.getExtensionQuery();
            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
            LocalExecution le = leMap.get(expRun);
            if (le != null) {
                QueryExec qe = le.getQueryExec();

                bgpExec = model.createResource().as(BgpExec.class);

                // Link the bgp with the corresponding query execution
                bgpExec
                    .setBgp(bgp) /* inverse link */
    //                                .setBenchmarkRun(expRun)
                    .setQueryExec(qe)
                    ;

                expRoot.getBgpExecs().add(bgpExec);
            }

//                            expRoot.getBgpExecs().add(bgpExec);
        }

        return bgpExec;
    }

    // TODO Consolidate common parts with createBgpExec
    public static BgpExec getOrCreateSubBgpExec(RDFNode expRun, Bgp bgp) {
        LsqQuery bgpEq = bgp.getExtensionQuery();
        Objects.requireNonNull(bgpEq, "Missing extension query on bgp OR an execution for a sub-bgp of a non-variable bgp-node was requested " + bgp);
        Map<Resource, LocalExecution> bgpEqLeMap = bgpEq.getLocalExecutionMap();
        BgpExec bgpExec = bgp.findBgpExec(expRun);

        if(bgpExec == null) {
            Model model = bgp.getModel();
            LocalExecution le = bgpEqLeMap.get(expRun);

            if (le != null) {
                QueryExec qe = le.getQueryExec();
                bgpExec = model.createResource().as(BgpExec.class);

                // Link the bgp with the corresponding query execution
                bgpExec
                    .setBgp(bgp) /* inverse link */
    //                                .setBenchmarkRun(expRun)
                    .setQueryExec(qe)
                    ;

//                            expRoot.getBgpExecs().add(bgpExec);
            }
        }

        return bgpExec;
    }


    /**
     * bgpExec -> tpInBgpExec
     *
     * @param expRun
     * @param tp
     * @return
     */
//    public static TpExec getOrCreateTpExec(BgpExec bgpExec, LsqTriplePattern tp) {
//        tp.getExtensionQuery().getLocalExecutionMap().get(expRun);
//
//
//        TpExec tpExec = tpInBgpExec.getTpExec();
//        if(tpExec == null) {
//            tpExec = model.createResource().as(TpExec.class);
//            LsqQuery extensionQuery = tp.getExtensionQuery();
////                    RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
//            Objects.requireNonNull(extensionQuery, "query for a sparql query element (graph pattern) must not be null");
//            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
//            LocalExecution le = leMap.get(expRun);
//            QueryExec qe = le.getQueryExec();
//
//            tpExec
//                .setTpInBgpExec(tpInBgpExec) /* inverse link */
//                .setTp(tp)
//                .setQueryExec(qe)
//                ;
//        }
//
//    }

    /**
     * Get or create an execution for a triple pattern in a bgp.
     * The benchmark run id is taken from the bgpExec.
     * The tpInBgp must be a member of the bgpExec's bgp.
     *
     * Issue: The owner of a bgpExec used to be a local execution (of a query) - but with the introduction of subBgps it may also be
     * a bgpNode. How to link to the execution now?
     *
     * localExec -> bgpExec -> tpInBgpExec -> tpExec
     *           -> bgpNodeExec (optional) -> subBgpExec
     *
     * @param expRun
     * @param bpgExec
     * @param tp
     * @return
     */
    public static TpInBgpExec getOrCreateTpInBgpExec(BgpExec bgpExec, TpInBgp tpInBgp) {
        RDFNode expRun = bgpExec.getQueryExec().getLocalExecution().getBenchmarkRun();

        TpInBgpExec tpInBgpExec = bgpExec.findTpInBgpExec(tpInBgp);
        Model model = bgpExec.getModel();

        if(tpInBgpExec == null) {
            tpInBgpExec = model.createResource().as(TpInBgpExec.class);
            // LsqQuery extensionQuery = tp.getExtensionQuery();

            // Link the bgp with the corresponding query execution
            tpInBgpExec
                .setBgpExec(bgpExec);
        }

        LsqTriplePattern tp = tpInBgp.getTriplePattern();
        TpExec tpExec = tpInBgpExec.getTpExec();
        if(tpExec == null) {
            tpExec = model.createResource().as(TpExec.class);
            LsqQuery extensionQuery = tp.getExtensionQuery();
//                    RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            Objects.requireNonNull(extensionQuery, "query for a sparql query element (graph pattern) must not be null");
            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
            LocalExecution le = leMap.get(expRun);
            QueryExec qe = le.getQueryExec();

            tpExec
                .setTpInBgpExec(tpInBgpExec) /* inverse link */
                .setTp(tp)
                .setQueryExec(qe)
                ;
        }

        return tpInBgpExec;
    }


    /**
     *
     * Assumes bgpNode.getBpg() to have an SpinBgpExec w.r.t. expRun.
     *
     * @param expRun
     * @param bgpNode
     * @return
     */
    public static BgpNodeExec getOrCreateBgpNodeExec(BgpExec bgpExec, BgpNode bgpNode) {
        QueryExec queryExec = bgpExec.getQueryExec();
        RDFNode expRun = bgpExec.getQueryExec().getLocalExecution().getBenchmarkRun();

        LsqQuery extensionQuery = bgpNode.getJoinExtensionQuery();
//        LocalExecution bgpNodeLes = extensionQuery.getLocalExecutionMap().get(expRun);

//        SpinBgp bgp = bgpNode.getBgp();
//        SpinBgpExec bgpExec = bgpNodeLes.findBgpExec(bgp);
//
        BgpNodeExec bgpNodeExec = bgpExec.findBgpNodeExec(bgpNode);
        if(bgpNodeExec == null) {
            Model model = bgpNode.getModel();
            bgpNodeExec = model.createResource().as(BgpNodeExec.class);

            bgpNodeExec
            .setBgpNode(bgpNode) /* inverse link */
            .setBgpExec(bgpExec) /* inverse link */
            .setQueryExec(queryExec)
            ;
        }

        return bgpNodeExec;
    }

    public static BigDecimal safeDivide(Long counter, Long denominator) {
        BigDecimal result = counter == null || denominator == null ? null :
            denominator.longValue() == 0
                ? new BigDecimal(0)
                : new BigDecimal(counter).divide(new BigDecimal(denominator), 10, RoundingMode.CEILING);
        return result;
    }

    public static BigDecimal avg(Integer a, Integer b) {
        return LsqExec.safeDivide(a + b, 2);
    }


    public static BigDecimal safeDivide(Integer counter, Integer denominator) {
        BigDecimal result = counter == null || denominator == null ? null :
            denominator.longValue() == 0
                ? new BigDecimal(0)
                : new BigDecimal(counter).divide(new BigDecimal(denominator), 10, RoundingMode.CEILING);
        return result;
    }

}
