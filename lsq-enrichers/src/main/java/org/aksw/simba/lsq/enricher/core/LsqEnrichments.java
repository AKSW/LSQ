package org.aksw.simba.lsq.enricher.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.core.util.Skolemize;
import org.aksw.simba.lsq.core.util.SpinUtils;
import org.aksw.simba.lsq.enricher.benchmark.core.LsqExec;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.model.util.SpinCoreUtils;
import org.aksw.simba.lsq.spinx.model.Bgp;
import org.aksw.simba.lsq.spinx.model.BgpInfo;
import org.aksw.simba.lsq.spinx.model.BgpNode;
import org.aksw.simba.lsq.spinx.model.DirectedHyperEdge;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.BaseEncoding;

public class LsqEnrichments {

    private static final Logger logger = LoggerFactory.getLogger(LsqEnrichments.class);

    public static void enrichSpinBgpWithTpInBgp(Bgp bgp) {
        Map<LsqTriplePattern, TpInBgp> tpToTpInBgp = bgp.indexTps();

        Collection<TpInBgp> tpInBgps = bgp.getTpInBgp();
        for(LsqTriplePattern tp : bgp.getTriplePatterns()) {
            TpInBgp tpInBgp = tpToTpInBgp.get(tp);
            if(tpInBgp == null) {
                tpInBgp = bgp.getModel().createResource().as(TpInBgp.class)
                    .setBgp(bgp)
                    .setTriplePattern(tp);

                tpInBgps.add(tpInBgp);
            }

        }
    }

    public static void enrichSpinBgpNodesWithSubBgpsAndQueries(BgpInfo bgpInfo, PrefixMapping prefixMapping) {

            boolean createQueryResources = true;
            for(Bgp bgp : bgpInfo.getBgps()) {

                LsqEnrichments.enrichSpinBgpWithTpInBgp(bgp);

                if(createQueryResources) {
                    LsqEnrichments.enrichSpinBgpWithQuery(bgp, prefixMapping);
                }

                // Enrich the bpg's triple patterns with extension queries
                if(createQueryResources) {
                    for(TriplePattern tp : bgp.getTriplePatterns()) {

                        LsqTriplePattern ltp = tp.as(LsqTriplePattern.class);

                        Triple jenaTriple = ltp.toJenaTriple();
                        ltp.setLabel(NodeFmtLib.str(jenaTriple));
//                        if(jenaTriple.isConcrete()) {
//                            System.out.println("Concrete triple: " + jenaTriple);
//                        }

                        LsqQuery extensionQuery = ltp.getExtensionQuery();
                        if(extensionQuery == null) {
                            extensionQuery = ltp.getModel().createResource().as(LsqQuery.class);

                            Query query = QueryUtils.elementToQuery(ElementUtils.createElementTriple(ltp.toJenaTriple()));
                            if(prefixMapping != null) {
                                query.setPrefixMapping(prefixMapping);
                                QueryUtils.optimizePrefixes(query);
                            }
                            extensionQuery.setQueryAndHash(query);
                            ltp.setExtensionQuery(extensionQuery);

                            // TODO The validation should not be necessary
//                            LsqQuery test = ltp.getExtensionQuery();
//                            Objects.requireNonNull(test);
//                            System.out.println("Set: " + test);
                        }
                    }
                }

                Map<Node, BgpNode> bgpNodeMap = bgp.indexBgpNodes();

                for(BgpNode bgpNode : bgpNodeMap.values()) {
                    Node jenaNode = bgpNode.toJenaNode();

                    // TODO Allow blank nodes?
//                    if (jenaNode.isVariable()) {

                        boolean createJoinExtension = false;
                        if(createJoinExtension && createQueryResources) {
                            LsqQuery extensionQuery = bgpNode.getJoinExtensionQuery();
                            if(extensionQuery == null) {
                                extensionQuery = bgp.getModel().createResource().as(LsqQuery.class);

                                Query query = QueryUtils.elementToQuery(new ElementTriplesBlock(bgp.toBasicPattern()));
                                query.setQueryResultStar(false);
                                query.setDistinct(true);
                                query.getProject().clear();
                                query.getProject().add((Var)jenaNode);

                                if(prefixMapping != null) {
                                    query.setPrefixMapping(prefixMapping);
                                    QueryUtils.optimizePrefixes(query);
                                }


                                extensionQuery.setQueryAndHash(query);

                                bgpNode.setJoinExtensionQuery(extensionQuery);
                            }
                        }


                        List<LsqTriplePattern> subBgpTps = bgp.getTriplePatterns().stream()
                                .filter(tp -> TripleUtils.streamNodes(SpinCoreUtils.toJenaTriple(tp)).collect(Collectors.toSet()).contains(jenaNode))
                                .collect(Collectors.toList());
                        // Do not generate empty subBgps
    //                    if(!subBgpTps.isEmpty()) {

                            Bgp subBgp = bgpNode.getSubBgp();

                            if(subBgp == null) {
                                subBgp = bgpNode.getModel().createResource().as(Bgp.class);
                                bgpNode.setSubBgp(subBgp);
                            }

                            Collection<LsqTriplePattern> dest = subBgp.getTriplePatterns();
                            for(LsqTriplePattern tp : subBgpTps) {
                                dest.add(tp);
                            }

                            LsqEnrichments.enrichSpinBgpWithTpInBgp(subBgp);

                            if(createQueryResources && jenaNode.isVariable()) {
                                LsqEnrichments.enrichSpinBgpWithQuery(subBgp, prefixMapping);
                            }
                        }
//                }
            }
        }

    public static void enrichSpinBgpsWithNodes(BgpInfo bgpInfo) {
        Model spinModel = bgpInfo.getModel();

        for(Bgp bgp : bgpInfo.getBgps()) {
            Set<BgpNode> bgpNodes = bgp.getBgpNodes();
//            logger.info("next bgp\n" + bgp.toBasicPattern());
            Map<Node, BgpNode> bgpNodeMap = bgp.indexBgpNodes();

            for(TriplePattern tp : bgp.getTriplePatterns()) {
                Set<RDFNode> rdfNodes = SpinAccessUtils.listRDFNodes(tp);
//                logger.info("triple pattern: " + tp);
                for(RDFNode rdfNode : rdfNodes) {
                    Node node = org.aksw.simba.lsq.model.util.SpinCoreUtils.readNode(rdfNode);
//                    logger.info("Read bgp node " + rdfNode + " -> " + node);

                    // Compute bgpNodes for all RDF terms - not just variables!
                    // This is mandated the hypergraph model
//                    if(node.isVariable()) {

                    BgpNode bgpNode = bgpNodeMap.computeIfAbsent(node,
                            n -> createBgpNode(spinModel, n));

                    bgpNode.setLabel(NodeFmtLib.str(node));
                    bgpNode.getProxyFor().add(rdfNode);


                    // Redundant inserts into a set
                    bgpNodes.add(bgpNode);
                }
            }
        }
    }

    /**
     * Creates a fresh blank BgpNode. If the jenaNode argument is a variable
     * then the sp:varName attribute is copied to the blank node.
     *
     *
     * @param model
     * @param jenaNode
     * @return
     */
    public static BgpNode createBgpNode(Model model, Node jenaNode) {
        BgpNode result = (jenaNode.isVariable()
                ? SpinAccessUtils.writeNode(model, jenaNode)
                : model.createResource())
            .as(BgpNode.class);

        return result;
    }

    /**
         * Given a spin model, create resources for BGPs (as spin does not natively support BGPS).
         * The current implementation treats any resource having a rdf:list of triples (ElementList)
         * as a BGP.
         *
         * TODO Join vertices - where to create them?
         *
         *
         * @param spinModel
         */
        public static void enrichSpinModelWithBgps(BgpInfo bgpInfo) {
            Model spinModel = bgpInfo.getModel();

            // Extend the spin model with BGPs
            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinAccessUtils.indexBasicPatterns2(spinModel); //queryRes);

            for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {


                // Map each resource to the corresponding jena element
    //            Map<org.topbraid.spin.model.Triple, Element> resToEl = e.getValue().stream()
    //                    .collect(Collectors.toMap(
    //                            r -> r,
    //                            r -> ElementUtils.createElement(SpinUtils.toJenaTriple(r))));

    //            Set<Var> bgpVars = resToEl.values().stream()
    //                    .flatMap(el -> PatternVars.vars(el).stream())
    //                    .collect(Collectors.toSet());

                // Take the skolem ID of the spin element and declare it as a bgp
                // TODO Better introduce new resources based on the skolemIds of the triple patterns
    //            String bgpId = Optional.ofNullable(e.getKey().getProperty(Skolemize.skolemId))
    //                    .map(Statement::getString).orElse(null);

    //            Resource bgpRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-bgp-" + bgpId);
                //Resource bgpCtxRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-bgp-" + bgpId);

                Bgp bgpCtxRes = spinModel.createResource().as(Bgp.class);
    //            if(bgpId != null) {
    //                bgpCtxRes.addProperty(Skolemize.skolemId, "-bgp-" + bgpId);
    //            }

                List<LsqTriplePattern> bgpTps = bgpCtxRes.getTriplePatterns();
                for(org.topbraid.spin.model.Triple tp : e.getValue()) {
                    // System.err.println("TP:" + tp);

                    bgpTps.add(tp.as(LsqTriplePattern.class));
                }


    //            Map<Var, Resource> varToBgpVar = bgpVars.stream()
    //                    .collect(Collectors.toMap(
    //                            v -> v,
    //                            v -> NestedResource.from(bgpCtxRes).nest("-var-").nest(v.getName()).get()));


                bgpInfo.getBgps().add(bgpCtxRes);
            }
    //      // Add the BGP var statistics
    //      //varToCount.forEach((v, c) -> {
    //      for(Var v : bgpVars) {
    //          Resource queryVarRes = varToQueryVarRes.get(v);
    //          //System.out.println("queryVar: " + queryVar);
    //
    //          Resource bgpVar = varToBgpVar.get(v);
    //
    //          bgpVar.addLiteral(LSQ.resultSize, c);
    //          bgpVar.addProperty(LSQ.proxyFor, queryVarRes);
    //      }


    //    Collection<org.topbraid.spin.model.Triple> tps = bgpToTps.values();
        // Note: We assume that each var only originates from a single resource - which is the case for lsq
        // In general, we would have to use a multimap
    //    Map<Var, Resource> varToQueryVarRes = tps.stream()
    //            .flatMap(tp -> SpinUtils.indexTripleNodes2(tp).entrySet().stream())
    //            .filter(e -> e.getValue().isVariable())
    //            .collect(Collectors.toMap(
    //                    e -> (Var)e.getValue(),
    //                    e -> e.getKey().asResource(),
    //                    (old, now) -> now));

        }

    public static void enrichSpinBgpWithQuery(Bgp bgp, PrefixMapping prefixMapping) {
        LsqQuery extensionQuery = bgp.getExtensionQuery();
        if(extensionQuery == null) {
            extensionQuery = bgp.getModel().createResource().as(LsqQuery.class);

            Element elt = new ElementTriplesBlock(bgp.toBasicPattern());

            // TODO Use a prefixed form?
            bgp.setLabel(elt.toString());
            Query query = QueryUtils.elementToQuery(elt);
            if(prefixMapping != null) {
                query.setPrefixMapping(prefixMapping);
                QueryUtils.optimizePrefixes(query);
            }

            extensionQuery.setQueryAndHash(query);
            bgp.setExtensionQuery(extensionQuery);
        }
    }

    /**
     * Enrich an LSQ Query resource with a spin model.
     * Returns an empty maybe if an error occurs, such as parse error
     *
     * @param lsqQuery
     * @return
//     */
//    public static Maybe<LsqQuery> enrichWithFullSpinModel(LsqQuery lsqQuery) {
//        Maybe<LsqQuery> result;
//        try {
//            LsqQuery q = LsqEnrichments.enrichWithFullSpinModelCore(lsqQuery);
//            result = Maybe.just(q);
//        } catch(Exception e) {
//            LsqBenchmarkProcessor.logger.error("Error processing query", e);
//            result = Maybe.empty();
//        }
//        return result;
//    }

    public static LsqQuery enrichWithFullSpinModelCore(LsqQuery lsqQuery) {
    //        Maybe<LsqQuery> result;

            // Query query = QueryFactory.create("SELECT * {  { ?s a ?x ; ?p ?o } UNION { ?s ?j ?k } }");
            String queryStr = lsqQuery.getText();
            Objects.requireNonNull(queryStr, "Query string must not be null");

            Query query;
            try {
                query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
            } catch (Exception e) {
                logger.error("Dumping LsqQuery with non-parseable sparql query string:");
                // TODO Write to logger
                RDFDataMgr.write(System.err, lsqQuery.getModel(), RDFFormat.TURTLE_PRETTY);

                throw new IllegalArgumentException("An LsqQuery with a non-parsable sparql query string was provided.", e);
            }

            PrefixMapping prefixMapping = query.getPrefixMapping();

    //        SpinQueryEx spinRes = lsqQuery.getSpinQuery().as(SpinQueryEx.class);

            Resource spinQuery = SpinUtils.createSpinModel(query, lsqQuery.getModel());

            // Immediately skolemize the spin model - before attachment of
            // additional properties changes the hashes
    //        String part = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());
            spinQuery = skolemizeSpin(spinQuery);
            SpinQueryEx spinRes = spinQuery.as(SpinQueryEx.class);

            lsqQuery.setSpinQuery(spinQuery);

            enrichSpinModelWithBgps(spinRes);
            enrichSpinBgpsWithNodes(spinRes);
            enrichSpinBgpNodesWithSubBgpsAndQueries(spinRes, prefixMapping);


            // Add tpInBgp resources
            // for bgp restricted triple pattern selectivity
    //        for(SpinBgp bgp : spinRes) {
    //            for(TriplePattern tp : bgp.getTriplePatterns()) {
    //
    //            }
    //        }

            // Skolemize the remaining model
            if(false) {
            Skolemize.skolemizeTree(spinRes, true,
                    (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                    (n, d) -> !(n.isResource() && n.asResource().hasProperty(LSQ.text)));
            }

            // Now that the bgps and tps are skolemized, create the tpInBgp nodes
//            for(SpinBgp bgp : spinRes.getBgps()) {
//    //            System.err.println("BGP: " + bgp);
//                Set<TpInBgp> tpInBgps = bgp.getTpInBgp();
//                for(TriplePattern tp : bgp.getTriplePatterns()) {
//    //            	System.err.println("BGP: " + bgp);
//    //                System.err.println("  TP: " + tp);
//                    TpInBgp tpInBgp = spinRes.getModel().createResource().as(TpInBgp.class);
//                    tpInBgp.setBgp(bgp);
//                    tpInBgp.setTriplePattern(tp);
//                    tpInBgps.add(tpInBgp);
//                }
//
//                if(false) {
//                Skolemize.skolemizeTree(bgp, true,
//                        (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
//                        (n, d) -> true);
//                }
//
//                // Clear the bgp attribute that was only required for computing the hash id
//                for(TpInBgp tpInBgp : tpInBgps) {
//                    tpInBgp.setBgp(null);
//                }
//            }

            String lsqBaseIri = "http://lsq.aksw.org/spin-";
            HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(spinRes);//.getHash(bgp);
            Map<RDFNode, String> renames = hashIdCxt.getStringIdMapping();
            ResourceUtils.renameResources(lsqBaseIri, renames);


    //        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_FLAT);
    //        System.exit(1);


    //        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_BLOCKS);
    //
    //        System.exit(0);
            return lsqQuery;
    }


    public static Resource skolemizeSpin(Resource spinQuery) {
        return skolemizeSpin(spinQuery, "http://lsq.aksw.org/spin-");
    }

    public static Resource skolemizeSpin(Resource spinQuery, String prefix) {
        Resource result = Skolemize.skolemizeTree(spinQuery, false,
                (r, hashCode) -> prefix + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                (r, d) -> true).asResource();

        return result;
    }

    /**
     * Analyze the query for a set of structural features (e.g. use of optional,
     * union, exists, etc...) and attach them to the given resource
     *
     * @param resource
     *            The resource on which to attach the features
     * @param query
     *            The query object from which to extract the features
     */
    public static void enrichResourceWithQueryFeatures(Resource resource, Query query) {
        Map<Resource, Integer> featureMap = ElementVisitorFeatureExtractor.getFeatures(query);
        Set<Resource> features = featureMap.keySet();

        for (Resource feature : features) {
            resource.addProperty(LSQ.usesFeature, feature);
        }

        if (features.isEmpty()) {
            resource.addProperty(LSQ.usesFeature, LSQ.None);
        }
    }


    public static <T, R> Optional<R> median(
            List<T> sortedItems,
            Function<? super T, ? extends R> caster,
            BiFunction<? super T, ? super T, ? extends R> averager) {
        R result;

        int size = sortedItems.size();
        if(size == 0) {
            result = null;
        } else {
            boolean isEvenSized = size % 2 == 0;
            if(isEvenSized) {
                int idx = size >> 1;
                T lo = sortedItems.get(idx - 1);
                T hi = sortedItems.get(idx);
                result = averager.apply(lo, hi);
            } else {
                int idx = (size - 1) >> 1;
                T item = sortedItems.get(idx);
                result = caster.apply(item);
            }
        }

        return Optional.ofNullable(result);
    }

//    public static Optional<Number> min(Iterable<? extends Number> numbers) {
//        Number result = null;
//        for(Number number : numbers) {
//            result = result == null
//                    ? number
//                    : number.doubleValue() < result.doubleValue()
//                        ? number
//                        : result;
//        }
//
//        return Optional.ofNullable(result);
//    }
//
//    public static Optional<Number> max(Iterable<? extends Number> numbers) {
//        Number result = null;
//        for(Number number : numbers) {
//            result = result == null
//                    ? number
//                    : number.doubleValue() > result.doubleValue()
//                        ? number
//                        : result;
//        }
//
//        return Optional.ofNullable(result);
//    }


    public static int intSum(Iterable<? extends Number> numbers) {
        int result = 0;
        for(Number number : numbers) {
            result += (number == null ? 0 : number.intValue());
        }

        return result;
    }


//    public static BigDecimal mean(Iterable<? extends Number> numbers) {
//        BigDecimal result = null;
//        int n = 0;
//        for(Number number : numbers) {
//        	++n;
//        	result = result == null
//        			? number
//        			: result.add
//        }
//
//        return result;
//    }



    // Requires the spin model to exist on the argument
    public static LsqQuery enrichWithStaticAnalysis(LsqQuery queryRes) {
        String queryStr = queryRes.getText();
        // TODO Avoid repeated parse
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ);


        LsqStructuralFeatures featureRes = queryRes.getStructuralFeatures();
        if(featureRes == null) {
            featureRes = queryRes.getModel().createResource().as(LsqStructuralFeatures.class);
            queryRes.setStructuralFeatures(featureRes);
        }


        // Add used features
        enrichResourceWithQueryFeatures(featureRes, query);

        if(query.isSelectType()) {
            featureRes.setProjectVarCount(query.getProjectVars().size());
        }

        // Copy the bgp information from the spin model to the structural features model
        // Thereby remove the link from the spinQuery to the bgs
        SpinQueryEx spinEx = queryRes.getSpinQuery().as(SpinQueryEx.class);
        Set<Bgp> bgpsInSpin = spinEx.getBgps();

        BgpInfo bgpInfo = featureRes;
        Set<Bgp> bgps = bgpInfo.getBgps();

        bgps.addAll(bgpsInSpin);
        bgpsInSpin.clear();


        int bgpCount = bgps.size();

        List<Integer> tpInBgpTotalCounts = new ArrayList<>(bgpCount);

        featureRes.setBgpCount(bgpCount);

        for(Bgp bgp : bgpInfo.getBgps()) {
            Set<TpInBgp> tpInBgps = bgp.getTpInBgp();
            int tpCount = tpInBgps.size();

            tpInBgpTotalCounts.add(tpCount);
        }

        List<Integer> sortedTpCounts = tpInBgpTotalCounts.stream().sorted().collect(Collectors.toList());
        int tpCount = intSum(tpInBgpTotalCounts);
        int tpInBgpCountMin = Iterables.getFirst(sortedTpCounts, 0);
        int tpInBgpCountMax = Iterables.getLast(sortedTpCounts, 0);
        BigDecimal tpInBgpCountMean = LsqExec.safeDivide(tpCount, bgpCount);
        BigDecimal tpInBgpCountMedian = median(sortedTpCounts, x -> new BigDecimal(x), LsqExec::avg).orElse(new BigDecimal(0));

        featureRes
            .setTpCount(tpCount)
            .setTpInBgpCountMin(tpInBgpCountMin)
            .setTpInBgpCountMax(tpInBgpCountMax)
            .setTpInBgpCountMean(tpInBgpCountMean)
            .setTpInBgpCountMedian(tpInBgpCountMedian);


        /*
         * join vertex computation
         */

        List<Integer> sortedJoinVertexDegrees = bgpInfo.getBgps().stream()
                .flatMap(bgp -> setUpJoinVertices(bgp).stream()).sorted()
                .collect(Collectors.toList());

        int n = sortedJoinVertexDegrees.size();


        int joinVertexDegreeSum = intSum(sortedJoinVertexDegrees);
        BigDecimal joinVertexDegreeMean = LsqExec.safeDivide(joinVertexDegreeSum, n);
        BigDecimal joinVertexDegreeMedian = median(sortedJoinVertexDegrees, x -> new BigDecimal(x), LsqExec::avg).orElse(new BigDecimal(0));

        // This is on the query level
        featureRes
                .setJoinVertexCount(sortedJoinVertexDegrees.size())
                .setJoinVertexDegreeMean(joinVertexDegreeMean)
                .setJoinVertexDegreeMedian(joinVertexDegreeMedian)
                ;

        QueryStatistics2.enrichWithPropertyPaths(featureRes, query);

        Model spinModel = queryRes.getModel();

        // TODO Move to a util function
        Set<Resource> serviceUris = spinModel.listStatements(null, SP.serviceURI, (RDFNode)null)
                .mapWith(stmt -> stmt.getObject().asResource()).toSet();

        for(Resource serviceUri : serviceUris) {
            featureRes.addProperty(LSQ.usesService, serviceUri);
        }




        // QueryStatistics2.enrichWithMentions(featureRes, query); //the mentions subjects, predicates and objects can be obtained from Spin


//   } catch (Exception ex) {
//       String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
//       queryRes.addLiteral(LSQ.processingError, msg);
//       logger.warn("Failed to process query " + query, ex);
//   }
//
        return queryRes;
    }


    public static List<Integer> setUpJoinVertices(Bgp bgp) {

//        BasicPattern bgp = bgpRes.toBasicPattern();
        // int bgpHash = (new HashSet<>(bgp.getList())).hashCode();

        // Create the hypergraph model over all bgps
        // (Could be changed if individual stats are desired)
        //Model hyperGraph = ModelFactory.createDefaultModel();
//        Model hyperGraph = bgp.getModel();

        // for(BasicPattern bgp : bgps) {
        enrichModelWithHyperGraphData(bgp);

        // System.out.println("HYPER");
        // hyperGraph.write(System.out, "TURTLE");

        Set<Resource> rawJoinVertices = bgp.getBgpNodes().stream()
            .filter(x -> x.hasProperty(RDF.type, LSQ.Vertex))
            .collect(Collectors.toSet());

//        Set<Resource> rawJoinVertices = hyperGraph.listResourcesWithProperty(RDF.type, LSQ.Vertex).toSet();

        Map<Resource, Integer> joinVertexToDegree = rawJoinVertices.stream()
                .collect(Collectors.toMap(r -> r, r -> QueryStatistics2.propertyDegree(r, LSQ.out, LSQ.in)));

        // .filter(x -> x != 1) // Remove vertices that do not join
        joinVertexToDegree = joinVertexToDegree.entrySet().stream()
                .filter(e -> e.getValue() != 1)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<Resource> joinVertices = joinVertexToDegree.keySet();

        List<Integer> degrees = joinVertexToDegree.values().stream().sorted().collect(Collectors.toList());

        // list.add(value);
        // stats = stats + " lsqv:triplePatterns "+totalTriplePatterns +" ; ";
        // stats = stats + " lsqv:joinVertices "+joinVertices.size() +" ; ";
        // stats = stats + " lsqv:meanJoinVerticesDegree 0 . ";

        // stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsObject ";
        // stats = stats + "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsSubject ";
        // stats = stats+ "\nlsqr:sf-q"+(LogRDFizer.queryHash)+"
        // lsqv:mentionsPredicate ";
        // stats = stats + getMentionsTuple(predicates); // subjects and objects
        // ModelUtils.
        // ResourceUtils.
        NestedResource joinVertexNres = new NestedResource(bgp);

        for (Resource v : joinVertices) {
            // TODO Allocate a resource for the join vertex
            // Resource queryRes = null;
            Statement t = v.getProperty(RDFS.label);
            RDFNode o = t.getObject();
            String name = "" + QueryStatistics2.toPrettyString(o);

            // System.out.println(name);
//            Resource joinVertexRes = joinVertexNres.nest("-jv-" + name).get();// lsqr:sf-q"+(LogRDFizer.queryHash)+"-"+joinVertex
            Resource joinVertexRes = v;

//            bgpRes.addProperty(LSQ.joinVertex, joinVertexRes);

            Resource joinVertexType = QueryStatistics2.getJoinVertexType(v);
            int degree = joinVertexToDegree.get(v);

            //rdfNodeToNode = v.getProperty(LSQ.proxyFor).getObject();

//            Node proxyNode = v.getProperty(LSQ.proxyFor).getObject().asNode();
//            RDFNode proxyRdfNode = nodeToModel.get(proxyNode);

//            if(proxyRdfNode == null) {
//                throw new NullPointerException("Should not happen");
//            }

            joinVertexRes
                .addLiteral(LSQ.joinVertexDegree, degree)
                .addProperty(LSQ.joinVertexType, joinVertexType)
//                .addProperty(LSQ.proxyFor, proxyRdfNode)
            // .addProperty(LSQ.proxyFor,
            // v)//v.getPropertyResourceValue(LSQ.proxyFor))
            ;

        }

        return degrees;
    }



    /**
     * Creates a hypergraph model.
     *
     *
     *
     * @param result
     * @param nodeToResource
     *            Mapping from nodes to resources. Can be used to control
     *            whether e.g. nodes of different graph patters should map to
     *            the same or to different resources. This is an in/out
     *            argument.
     * @param triples
     */
    public static void enrichModelWithHyperGraphData(Bgp spinBgp) {
        //Iterable<org.topbraid.spin.model.Triple> triples) { //, Map<Resource, Node> hyperGraphResourceToNode) {
        // result = result == null ? ModelFactory.createDefaultModel() : result;

        //Map<Node, Resource> nodeToResource,

        Model result = spinBgp.getModel();
        Map<Node, BgpNode> bgpNodes = spinBgp.indexBgpNodes();

//        RDFDataMgr.write(System.err, spinBgp.getModel(), RDFFormat.TURTLE_BLOCKS);

        Iterable<? extends org.topbraid.spin.model.Triple> spinTriples = spinBgp.getTriplePatterns();

        for (org.topbraid.spin.model.Triple st : spinTriples) {
            Triple t = SpinCoreUtils.toJenaTriple(st);
            // Get the triple's nodes
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();

            // Create anonymous resources as proxies for the original
            // triple and nodes - needed because RDF literals cannot appear in
            // subject position

            // Note: Here we treat each triple different from the other even if they
            // happen to be equal - apply prior normalization of the query if this undesired.

            DirectedHyperEdge tx = result.createResource().as(DirectedHyperEdge.class);



            // Resource sx = nodeToResource.merge(s, result.createResource(),
            // (x, y) -> x);
            // Resource px = nodeToResource.merge(p, result.createResource(),
            // (x, y) -> x);
            // Resource ox = nodeToResource.merge(o, result.createResource(),
            // (x, y) -> x);
//            Resource sx = nodeToResource.computeIfAbsent(s, (x) -> result.createResource());
//            Resource px = nodeToResource.computeIfAbsent(p, (x) -> result.createResource());
//            Resource ox = nodeToResource.computeIfAbsent(o, (x) -> result.createResource());

              BgpNode sx = bgpNodes.get(s);
              BgpNode px = bgpNodes.get(p);
              BgpNode ox = bgpNodes.get(o);

//            // Add the orginal nodes as annotations
//            Resource ss = result.wrapAsResource(t.getSubject());
//            Resource pp = result.wrapAsResource(t.getPredicate());
//            RDFNode oo = result.asRDFNode(t.getObject());

            //hyperGraphResourceToNode.put(sx, t.getSubject());


            sx
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.subject, sx)
                //.addLiteral(RDFS.label, getLabel(s));
                ;

            px
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.predicate, px)
//                .addLiteral(RDFS.label, getLabel(p));
                ;

            ox
                .addProperty(RDF.type, LSQ.Vertex)
                .addProperty(RDF.object, ox)
//                .addLiteral(RDFS.label, getLabel(o))
                ;

            String tripleStr = FmtUtils.stringForTriple(t) + " .";
            tx
                .addProperty(RDF.type, LSQ.Edge)
//                .addLiteral(RDFS.label, "" + tripleStr);
                ;

            sx.getOutEdges().add(tx);
            px.getInEdges().add(tx);
            ox.getInEdges().add(tx);

            spinBgp.getEdges().add(tx);
        }
        // return result;
    }


    // Util method used by enrichModelWithHyperGraphData
//    public static String getLabel(Node node) {
//        String result;
//        if (node.isURI()) {
//            result = StringUtils.urlEncode(node.getURI()).replaceAll("\\%..", "-").replaceAll("\\-+", "-");
//        } else if (node.isVariable()) {
//            result = ((Var) node).getName();
//        } else if(node.isBlank()) {
//            // result = NodeFmtLib.displayStr(node);
//            // FmtUtils is older, but is decodes bnode labels correctly
//            // Avoid another colon in the URL - may get encoded into an ugly "%3A"
//            result = "__" + node.getBlankNodeLabel(); // FmtUtils.stringForNode(node);
//        } else {
//            result = "" + node;
//        }
//        return result;
//    }
    // Probably not needed
    public static BigDecimal fromNumber(Number n) {
        BigDecimal result;
        if(n == null) {
            result = null;
        } else if(n instanceof Byte) {
            byte val = n.byteValue();
            result = new BigDecimal(val);
        } else if(n instanceof Short) {
            short val = n.shortValue();
            result = new BigDecimal(val);
        } else if(n instanceof Integer) {
            int val = n.intValue();
            result = new BigDecimal(val);
        } else if(n instanceof Long) {
            long val = n.longValue();
            result = new BigDecimal(val);
        } else if(n instanceof Float) {
            float val = n.floatValue();
            result = new BigDecimal(val);
        } else if(n instanceof Double) {
            double val = n.doubleValue();
            result = new BigDecimal(val);
        } else if(n instanceof BigDecimal) {
            result = (BigDecimal)n;
        }
        else {
            throw new IllegalArgumentException("Unknow number type: " + n.getClass());
        }

        return result;
    }

}
