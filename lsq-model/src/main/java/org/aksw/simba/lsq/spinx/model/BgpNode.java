package org.aksw.simba.lsq.spinx.model;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.util.SpinCoreUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


@ResourceView
public interface BgpNode
    extends Resource, Labeled
{
    @Iri(LSQ.Terms.hasBgpNode)
    @Inverse
    @HashId
    Bgp getBgp();


    /**
     * The set of RDFNodes mentioned in a BGP of a SPIN model that denote the same RDF term.
     *
     * Although all referenced nodes and resources must denote the same RDF term, references
     * my refer to different IRIs or blank nodes in the case of variables.
     *
     *
     * Possible solutions:
     * (1) Mark an arbitrary member of the set as the 'primary' one: Bad because on which basis to assert that?
     * (2) Ensure that for each RDF term there is a canonical resource in the RDF model that can be refered to.
     *     This would add another layer of indirection.
     *
     *
     * @return
     */
//    @HashId
//    @Range({Literal.class, SpinVar.class})
    // @PolymorphicOnly({Literal.class, Var.class})
    @Iri(LSQ.Terms.proxyFor)
    Set<RDFNode> getProxyFor();


    /**
     * Custom hashId handler because the jsa mapper currently does not support a range that
     * may be literals as well as subclasses of resource. Literals are literals (so there is no problem) but
     * we currently cannot tell the mapper that in case of Resource it should try a specific set of classes
     * (a SPIN Variable in this specific case) and use appropriate's class associated id generation mechanism.
     *
     * Once the @Range annotation is working, we can remove this custom handler
     *
     * @param cxt
     * @return
     */
    @HashId
    default HashCode getHashId(HashIdCxt cxt) {

        HashFunction hashFn = cxt.getHashFunction();
        Set<RDFNode> proxies = getProxyFor();

        // The set of proxy nodes either contains:
        // - a single literal
        // - a single IRI
        // - several resources which all have the same value for the sp:varName attribute

        RDFNode literalOrIriOrSpinVar = Iterables.getFirst(proxies, null);

        RDFNode literal;
        if(literalOrIriOrSpinVar == null) {
            literal = null;
        } else if(literalOrIriOrSpinVar.isLiteral()) {
            literal = literalOrIriOrSpinVar;
        } else { // if(pick.isResource()) {
            literal = ResourceUtils.getPropertyValue(literalOrIriOrSpinVar.asResource(), SP.varName);

            if(literal == null) {
                if(literalOrIriOrSpinVar.isURIResource()) {
                    Model model = getModel();
                    // 'iri:' prefix to reduce change of clash with an IRI in a literal such as "http://foo.bar"
                    literal = model.createLiteral("iri:" + literalOrIriOrSpinVar.asResource().getURI());
                } else {
                    throw new IllegalStateException("Proxy reference for a BgpNode must be either a literal or a resource with a SP.varName attribute or an IRI; got: " + literalOrIriOrSpinVar);
                }
            }
        }

        HashCode nodeHash = literal == null
                ? hashFn.hashInt(0)
                : cxt.getGlobalProcessor().apply(literal, cxt);

        if(literalOrIriOrSpinVar.isResource() && cxt.getHashId(literalOrIriOrSpinVar) == null /* TODO maybe use !isProcessed? */) {
            cxt.putHashId(literal, nodeHash);
//            cxt.putString(actual, "var-" + actual.asLiteral().getString());
        }

        Bgp bgp = getBgp();
        HashCode bgpHash = cxt.getHashId(bgp);
        HashCode result = Hashing.combineUnordered(Arrays.asList(bgpHash, nodeHash));

        return result;
    }

//    @StringId
//    String getStringId(HashIdCxt cxt) {
//    	return "
//    }

    // TODO These two methods should by mapped by DirectedHyperEdge
    @Iri(LSQ.Terms.in)
    Set<DirectedHyperEdge> getInEdges();

    @Iri(LSQ.Terms.out)
    Set<DirectedHyperEdge> getOutEdges();


//    @Iri(LSQ.Strs.hasJoinVarExec)
//    Set<JoinVertexExec> getJoinRestrictedSelectivities();


    @Iri(LSQ.Terms.hasExec)
    Set<BgpNodeExec> getBgpNodeExecs();


    /**
     * A resource for the subset of bgp's triple patterns in which the the BGPNode occurs.
     * The identity should be allocated based on the set of the involved triple patterns' identities.
     *
     * @return
     */
    @Iri(LSQ.Terms.hasSubBgp)
    Bgp getSubBgp();
    BgpNode setSubBgp(Bgp subBgp);

    /**
     * The resource that corresponds to the query
     * SELECT COUNT(DISTINCT joinVar) WHERE subBGP
     *
     * @return
     */
    @Iri(LSQ.Terms.joinExtensionQuery)
    LsqQuery getJoinExtensionQuery();
    BgpNode setJoinExtensionQuery(Resource joinExtensionQuery);

    public default Node toJenaNode() {
        Set<RDFNode> set = getProxyFor();
        if(set.isEmpty()) {
            throw new RuntimeException("toJenaNode() requires non-empty set of referenced RDF terms");
        }

        RDFNode node = set.iterator().next();
        Node result = SpinCoreUtils.readNode(node);
        return result;
    }
}
