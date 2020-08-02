package org.aksw.simba.lsq.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.QueryExec;
import org.aksw.simba.lsq.spinx.model.JoinVertexExec;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinBgp;
import org.aksw.simba.lsq.spinx.model.SpinBgpExec;
import org.aksw.simba.lsq.spinx.model.SpinBgpNode;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.spinx.model.TpExec;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.spinx.model.TpInBgpExec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

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

    public static void createAllExecs(LsqQuery masterQuery, ExperimentRun expRun) {
        Model model = masterQuery.getModel();
        SpinQueryEx spinRoot = masterQuery.getSpinQuery().as(SpinQueryEx.class);

        Map<Resource, LocalExecution> rleMap = masterQuery.getLocalExecutionMap();
        LocalExecution expRoot = rleMap.get(expRun);

        Long datasetSize = expRun.getConfig().getDatasetSize();

        /*
         * BgpExecs, TpInBgpExecs and TpExec
         *
         *
         */
        // Iterate the query's. bgps. For each bgp:
        // 1.) get-or-create the execution
        // 2.) then descend into the tp-in-bgp and the tps and update their stats
        for(SpinBgp bgp : spinRoot.getBgps()) {
            // Get the bgp's execution in this experiment
            LsqExec.createBgpExec(expRun, model, expRoot, bgp);
        }

        // Now that all BgpExecs are set up descend into the tpExecs
        for(SpinBgpExec bgpExec : expRoot.getBgpExecs()) {

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



            for(SpinBgpNode bgpNode : bgpExec.getBgp().getBgpNodes()) {
                JoinVertexExec bgpNodeExec = LsqExec.getOrCreateBgpNodeExec(bgpExec, bgpNode);

                Long bgpNodeSize = bgpNodeExec.getQueryExec().getResultSetSize();
                Long bgpSize = bgpExec.getQueryExec().getResultSetSize();
                BigDecimal value = safeDivide(bgpNodeSize, bgpSize);

                // TODO Maybe don't override if already set
                bgpNodeExec.setBgpRestrictedSelectivitiy(value);

                /*
                 * Sub-bgp executions (partitioned by bgp-node)
                 */

                SpinBgp subBgp = bgpNode.getSubBgp();
                SpinBgpExec subBgpExec = getOrCreateSubBgpExec(expRun, subBgp);

                QueryExec subBgpQueryExec = subBgpExec.getQueryExec();
                Long subBgpSize = subBgpQueryExec.getResultSetSize();

                // For each tpInSubBgp compute its
                for(TpInBgp tpInSubBgp : subBgp.getTpInBgp()) {
                    TpInBgpExec tpInSubBgpExec = getOrCreateTpInBgpExec(subBgpExec, tpInSubBgp);
                    TpExec tpExec = tpInSubBgpExec.getTpExec();

                    // LsqTriplePattern tp = tpExec.getTp();
                    QueryExec tpQueryExec = tpExec.getQueryExec();
                    Long tpSize = tpQueryExec.getResultSetSize();

                    BigDecimal subBgpToTpRatio = safeDivide(subBgpSize, tpSize);

                    tpInSubBgpExec.setTpToBgpRatio(subBgpToTpRatio);

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
    public static SpinBgpExec createBgpExec(RDFNode expRun, Model model, LocalExecution expRoot, SpinBgp bgp) {
        SpinBgpExec bgpExec = expRoot.findBgpExec(bgp);

        if(bgpExec == null) {
            bgpExec = model.createResource().as(SpinBgpExec.class);
            LsqQuery extensionQuery = bgp.getExtensionQuery();
            Map<Resource, LocalExecution> leMap = extensionQuery.getLocalExecutionMap();
            LocalExecution le = leMap.get(expRun);
            QueryExec qe = le.getQueryExec();

            // Link the bgp with the corresponding query execution
            bgpExec
                .setBgp(bgp) /* inverse link */
//                                .setBenchmarkRun(expRun)
                .setQueryExec(qe)
                ;

            expRoot.getBgpExecs().add(bgpExec);

//                            expRoot.getBgpExecs().add(bgpExec);
        }

        return bgpExec;
    }

    // TODO Consolidate common parts with createBgpExec
    public static SpinBgpExec getOrCreateSubBgpExec(RDFNode expRun, SpinBgp bgp) {
        LsqQuery bgpEq = bgp.getExtensionQuery();
        Map<Resource, LocalExecution> bgpEqLeMap = bgpEq.getLocalExecutionMap();
        SpinBgpExec bgpExec = bgp.findBgpExec(expRun);

        if(bgpExec == null) {
            Model model = bgp.getModel();
            bgpExec = model.createResource().as(SpinBgpExec.class);
            LocalExecution le = bgpEqLeMap.get(expRun);
            QueryExec qe = le.getQueryExec();

            // Link the bgp with the corresponding query execution
            bgpExec
                .setBgp(bgp) /* inverse link */
//                                .setBenchmarkRun(expRun)
                .setQueryExec(qe)
                ;

//                            expRoot.getBgpExecs().add(bgpExec);
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
    public static TpInBgpExec getOrCreateTpInBgpExec(SpinBgpExec bgpExec, TpInBgp tpInBgp) {
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
    public static JoinVertexExec getOrCreateBgpNodeExec(SpinBgpExec bgpExec, SpinBgpNode bgpNode) {
        QueryExec queryExec = bgpExec.getQueryExec();
        RDFNode expRun = bgpExec.getQueryExec().getLocalExecution().getBenchmarkRun();

        LsqQuery extensionQuery = bgpNode.getJoinExtensionQuery();
//        LocalExecution bgpNodeLes = extensionQuery.getLocalExecutionMap().get(expRun);

//        SpinBgp bgp = bgpNode.getBgp();
//        SpinBgpExec bgpExec = bgpNodeLes.findBgpExec(bgp);
//
        JoinVertexExec bgpNodeExec = bgpExec.findBgpNodeExec(bgpNode);
        if(bgpNodeExec == null) {
            Model model = bgpNode.getModel();
            bgpNodeExec = model.createResource().as(JoinVertexExec.class);

            bgpNodeExec
            .setBgpNode(bgpNode)
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

}
