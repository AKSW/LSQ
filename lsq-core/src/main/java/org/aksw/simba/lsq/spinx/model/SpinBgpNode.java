package org.aksw.simba.lsq.spinx.model;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


@ResourceView
public interface SpinBgpNode
    extends Resource, Labeled
{
    @Iri(LSQ.Strs.hasBgpNode)
    @Inverse
    @HashId
    SpinBgp getBgp();


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
    @Iri(LSQ.Strs.proxyFor)
    Set<RDFNode> getProxyFor();

    // Once the @Range annotation is working, we can remove the custom handler
    //
    @HashId
    default HashCode getHashId(HashIdCxt cxt) {

        HashFunction hashFn = cxt.getHashFunction();
        Set<RDFNode> proxies = getProxyFor();
        RDFNode literalOrSpinVar = Iterables.getFirst(proxies, null);

        RDFNode literal;
        if(literalOrSpinVar == null) {
            literal = null;
        } else if(literalOrSpinVar.isLiteral()) {
            literal = literalOrSpinVar;
        } else { // if(pick.isResource()) {
            literal = ResourceUtils.getPropertyValue(literalOrSpinVar.asResource(), SP.varName);
        }

        HashCode nodeHash = literal == null
                ? hashFn.hashInt(0)
                : cxt.getGlobalProcessor().apply(literal, cxt);

        if(literalOrSpinVar.isResource() /* TODO and not processed */) {
            cxt.putHash(literal, nodeHash);
//            cxt.putString(actual, "var-" + actual.asLiteral().getString());
        }

        HashCode bgpHash = cxt.getHash(getBgp());
        HashCode result = Hashing.combineUnordered(Arrays.asList(bgpHash, nodeHash));

        return result;
    }

//    @StringId
//    String getStringId(HashIdCxt cxt) {
//    	return "
//    }

    // TODO These two methods should by mapped by DirectedHyperEdge
    @Iri(LSQ.Strs.in)
    Set<DirectedHyperEdge> getInEdges();

    @Iri(LSQ.Strs.out)
    Set<DirectedHyperEdge> getOutEdges();


//    @Iri(LSQ.Strs.hasJoinVarExec)
//    Set<JoinVertexExec> getJoinRestrictedSelectivities();


    @Iri(LSQ.Strs.hasExec)
    Set<JoinVertexExec> getBgpNodeExecs();


    /**
     * A resource for the subset of bgp's triple patterns in which the the BGPNode occurs.
     * The identity should be allocated based on the set of the involved triple patterns' identities.
     *
     * @return
     */
    @Iri(LSQ.Strs.hasSubBgp)
    SpinBgp getSubBgp();
    SpinBgpNode setSubBgp(SpinBgp subBgp);

    /**
     * The resource that corresponds to the query
     * SELECT COUNT(DISTINCT joinVar) WHERE subBGP
     *
     * @return
     */
    @Iri(LSQ.Strs.joinExtensionQuery)
    LsqQuery getJoinExtensionQuery();
    SpinBgpNode setJoinExtensionQuery(Resource joinExtensionQuery);

    public default Node toJenaNode() {
        Set<RDFNode> set = getProxyFor();
        if(set.isEmpty()) {
            throw new RuntimeException("toJenaNode() requires non-empty set of referenced RDF terms");
        }

        RDFNode node = set.iterator().next();
        return SpinUtils.readNode(node);
    }
}
