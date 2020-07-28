package org.aksw.simba.lsq.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.spinx.model.LsqTriplePattern;
import org.aksw.simba.lsq.spinx.model.SpinBgp;
import org.aksw.simba.lsq.spinx.model.SpinBgpNode;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.spinx.model.TpInBgp;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.Multimap;
import com.google.common.io.BaseEncoding;

import io.reactivex.rxjava3.core.Maybe;

public class LsqEnrichments {

    public static void enrichSpinBgpNodesWithSubBgpsAndQueries(SpinQueryEx spinNode) {

            boolean createQueryResources = true;
            for(SpinBgp bgp : spinNode.getBgps()) {
                if(createQueryResources) {
                    LsqEnrichments.enrichSpinBgpWithQuery(bgp);
                }

                // Enrich the bpg's triple patterns with extension queries
                if(createQueryResources) {
                    for(TriplePattern tp : bgp.getTriplePatterns()) {

                        LsqTriplePattern ltp = tp.as(LsqTriplePattern.class);

//                        Triple jenaTriple = ltp.toJenaTriple();
//                        if(jenaTriple.isConcrete()) {
//                            System.out.println("Concrete triple: " + jenaTriple);
//                        }

                        LsqQuery extensionQuery = ltp.getExtensionQuery();
                        if(extensionQuery == null) {
                            extensionQuery = ltp.getModel().createResource().as(LsqQuery.class);

                            Query query = QueryUtils.elementToQuery(ElementUtils.createElementTriple(ltp.toJenaTriple()));
                            extensionQuery.setQueryAndHash(query);
                            ltp.setExtensionQuery(extensionQuery);

                            // TODO The validation should not be necessary
//                            LsqQuery test = ltp.getExtensionQuery();
//                            Objects.requireNonNull(test);
//                            System.out.println("Set: " + test);
                        }
                    }
                }

                Map<Node, SpinBgpNode> bgpNodeMap = bgp.indexBgpNodes();

                for(SpinBgpNode bgpNode : bgpNodeMap.values()) {
                    Node jenaNode = bgpNode.toJenaNode();

                    if(createQueryResources && jenaNode.isVariable()) {
                        LsqQuery extensionQuery = bgpNode.getJoinExtensionQuery();
                        if(extensionQuery == null) {
                            extensionQuery = bgp.getModel().createResource().as(LsqQuery.class);

                            Query query = QueryUtils.elementToQuery(new ElementTriplesBlock(bgp.toBasicPattern()));
                            query.setQueryResultStar(false);
                            query.setDistinct(true);
                            query.getProject().clear();
                            query.getProject().add((Var)jenaNode);
                            extensionQuery.setQueryAndHash(query);

                            bgpNode.setJoinExtensionQuery(extensionQuery);
                        }
                    }


                    SpinBgp subBgp = bgpNode.getSubBgp();

                    if(subBgp == null) {
                        subBgp = bgpNode.getModel().createResource().as(SpinBgp.class);
                        bgpNode.setSubBgp(subBgp);
                    }

                    List<LsqTriplePattern> subBgpTps = bgp.getTriplePatterns().stream()
                            .filter(tp -> TripleUtils.streamNodes(SpinUtils.toJenaTriple(tp)).collect(Collectors.toSet()).contains(jenaNode))
                            .collect(Collectors.toList());

                    Collection<LsqTriplePattern> dest = subBgp.getTriplePatterns();
                    for(LsqTriplePattern tp : subBgpTps) {
                        dest.add(tp);
                    }

                    if(createQueryResources && jenaNode.isVariable()) {
                        if(createQueryResources) {
                            LsqEnrichments.enrichSpinBgpWithQuery(subBgp);
                        }
                    }

                }
            }
        }

    public static void enrichSpinBgpsWithNodes(SpinQueryEx spinNode) {
        Model spinModel = spinNode.getModel();

        for(SpinBgp bgp : spinNode.getBgps()) {
            Map<Node, SpinBgpNode> bgpNodeMap = bgp.indexBgpNodes();

            for(TriplePattern tp : bgp.getTriplePatterns()) {
                Set<RDFNode> rdfNodes = SpinUtils.listRDFNodes(tp);
                for(RDFNode rdfNode : rdfNodes) {
                    Node node = SpinUtils.readNode(rdfNode);

                    if(node.isVariable()) {

                        SpinBgpNode bgpNode = bgpNodeMap.computeIfAbsent(node,
                                n -> SpinUtils.writeNode(spinModel, n).as(SpinBgpNode.class));

                        // Redundant inserts into a set
                        bgp.getBgpNodes().add(bgpNode);

                        bgpNode.getProxyFor().add(rdfNode);
                    }
                }
            }
        }
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
        public static void enrichSpinModelWithBgps(SpinQueryEx spinNode) {
            Model spinModel = spinNode.getModel();

            // Extend the spin model with BGPs
            Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinUtils.indexBasicPatterns2(spinModel); //queryRes);

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

                SpinBgp bgpCtxRes = spinModel.createResource().as(SpinBgp.class);
    //            if(bgpId != null) {
    //                bgpCtxRes.addProperty(Skolemize.skolemId, "-bgp-" + bgpId);
    //            }

                List<LsqTriplePattern> bgpTps = bgpCtxRes.getTriplePatterns();
                for(org.topbraid.spin.model.Triple tp : e.getValue()) {
                    bgpTps.add(tp.as(LsqTriplePattern.class));
                }


    //            Map<Var, Resource> varToBgpVar = bgpVars.stream()
    //                    .collect(Collectors.toMap(
    //                            v -> v,
    //                            v -> NestedResource.from(bgpCtxRes).nest("-var-").nest(v.getName()).get()));


                spinNode.getBgps().add(bgpCtxRes);
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

    public static void enrichSpinBgpWithQuery(SpinBgp bgp) {
        LsqQuery extensionQuery = bgp.getExtensionQuery();
        if(extensionQuery == null) {
            extensionQuery = bgp.getModel().createResource().as(LsqQuery.class);

            Query query = QueryUtils.elementToQuery(new ElementTriplesBlock(bgp.toBasicPattern()));
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
     */
    public static Maybe<LsqQuery> enrichWithFullSpinModel(LsqQuery lsqQuery) {
        Maybe<LsqQuery> result;
        try {
            LsqQuery q = LsqEnrichments.enrichWithFullSpinModelCore(lsqQuery);
            result = Maybe.just(q);
        } catch(Exception e) {
            LsqBenchmarkProcessor.logger.error("Error processing query", e);
            result = Maybe.empty();
        }
        return result;
    }

    public static LsqQuery enrichWithFullSpinModelCore(LsqQuery lsqQuery) {
    //        Maybe<LsqQuery> result;

            // Query query = QueryFactory.create("SELECT * {  { ?s a ?x ; ?p ?o } UNION { ?s ?j ?k } }");
            String queryStr = lsqQuery.getText();
            Objects.requireNonNull(queryStr, "Query string must not be null");
            Query query = QueryFactory.create(queryStr);


    //        SpinQueryEx spinRes = lsqQuery.getSpinQuery().as(SpinQueryEx.class);

            Resource spinQuery = LsqProcessor.createSpinModel(query, lsqQuery.getModel());

            // Immediately skolemize the spin model - before attachment of
            // additional properties changes the hashes
    //        String part = BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());
            spinQuery = Skolemize.skolemizeTree(spinQuery, false,
                    (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                    (r, d) -> true).asResource();

            SpinQueryEx spinRes = spinQuery.as(SpinQueryEx.class);

            lsqQuery.setSpinQuery(spinRes);

            enrichSpinModelWithBgps(spinRes);
            enrichSpinBgpsWithNodes(spinRes);
            enrichSpinBgpNodesWithSubBgpsAndQueries(spinRes);


            // Add tpInBgp resources
            // for bgp restricted triple pattern selectivity
    //        for(SpinBgp bgp : spinRes) {
    //            for(TriplePattern tp : bgp.getTriplePatterns()) {
    //
    //            }
    //        }

            // Skolemize the remaining model
            Skolemize.skolemizeTree(spinRes, true,
                    (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                    (n, d) -> !(n.isResource() && n.asResource().hasProperty(LSQ.text)));


            // Now that the bgps and tps are skolemized, create the tpInBgp nodes
            for(SpinBgp bgp : spinRes.getBgps()) {
    //            System.err.println("BGP: " + bgp);
                Set<TpInBgp> tpInBgps = bgp.getTpInBgp();
                for(TriplePattern tp : bgp.getTriplePatterns()) {
    //            	System.err.println("BGP: " + bgp);
    //                System.err.println("  TP: " + tp);
                    TpInBgp tpInBgp = spinRes.getModel().createResource().as(TpInBgp.class);
                    tpInBgp.setBgp(bgp);
                    tpInBgp.setTriplePattern(tp);
                    tpInBgps.add(tpInBgp);
                }

                Skolemize.skolemizeTree(bgp, true,
                        (r, hashCode) -> "http://lsq.aksw.org/spin-" + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes()),
                        (n, d) -> true);

                // Clear the bgp attribute that was only required for computing the hash id
                for(TpInBgp tpInBgp : tpInBgps) {
                    tpInBgp.setBgp(null);
                }
            }

    //        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_FLAT);
    //        System.exit(1);


    //        RDFDataMgr.write(System.out, lsqQuery.getModel(), RDFFormat.TURTLE_BLOCKS);
    //
    //        System.exit(0);
            return lsqQuery;
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
        Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
        for (Resource feature : features) {
            resource.addProperty(LSQ.usesFeature, feature);
        }

        if (features.isEmpty()) {
            resource.addProperty(LSQ.usesFeature, LSQ.None);
        }
    }

    public static LsqQuery enrichWithStaticAnalysis(LsqQuery queryRes) {
           String queryStr = queryRes.getText();
           // TODO Avoid repeated parse
           Query query = QueryFactory.create(queryStr);
    //       LsqProcessor.rdfizeQueryStructuralFeatures(lsqQuery, x -> NestedResource.from(lsqQuery).nest(x), query);

           Function<String, NestedResource> queryAspectFn = x -> NestedResource.from(queryRes).nest(x);
           //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
           LsqStructuralFeatures featureRes = queryAspectFn.apply("-sf").get().as(LsqStructuralFeatures.class); // model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash

           queryRes.setStructuralFeatures(featureRes);
           //queryRes.addProperty(LSQ.hasStructuralFeatures, featureRes);


           // Add used features
           enrichResourceWithQueryFeatures(featureRes, query);

           if(query.isSelectType()) {
               featureRes.addLiteral(LSQ.projectVars, query.getProjectVars().size());
           }

    //       Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
    //       features.forEach(f -> featureRes.addProperty(LSQ.usesFeature, f));

           // TODO These methods have to be ported
           //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
           //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

    //       SpinUtils.enrichWithHasTriplePattern(featureRes, spinRes);
    //       SpinUtils.enrichWithTriplePatternText(spinRes);
           //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);

           //
    //       QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, featureRes);

           QueryStatistics2.enrichWithPropertyPaths(featureRes, query);

           Model spinModel = queryRes.getModel();

           // TODO Move to a util function
           Set<Resource> serviceUris = spinModel.listStatements(null, SP.serviceURI, (RDFNode)null)
                   .mapWith(stmt -> stmt.getObject().asResource()).toSet();

           for(Resource serviceUri : serviceUris) {
               featureRes.addProperty(LSQ.usesService, serviceUri);
           }




           //QueryStatistics2.enrichWithMentions(featureRes, query); //the mentions subjects, predicates and objects can be obtained from Spin


    //   } catch (Exception ex) {
    //       String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
    //       queryRes.addLiteral(LSQ.processingError, msg);
    //       logger.warn("Failed to process query " + query, ex);
    //   }
    //
            return queryRes;
        }

}
