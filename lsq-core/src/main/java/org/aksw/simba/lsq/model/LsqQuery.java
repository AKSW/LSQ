package org.aksw.simba.lsq.model;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Resource;


/**
 * This class is main entry point for accessing information about a query in LSQ.
 *
 * An LsqQuery is a *record* about a sparql query, encompassing the query string,
 * a spin representation, structural features, mentions in query logs
 * (unfortunately poorly called 'remote execution') and benchmarking information related to the query
 * itself (referred to as local execution) and its constituents.
 *
 *
 * TODO This model keeps the SPIN representation of a query separate from the LSQ record about it,
 * yet I am not totally sure whether actually these should be just two views of a resource which
 * represents a SPARQL query.
 *
 *
 * @author Claus Stadler, Jan 7, 2019
 *
 */
@ResourceView
public interface LsqQuery
    extends Resource
{
    @Iri(LSQ.Strs.text)
    String getText();
    LsqQuery setText(String text);

    @Iri(LSQ.Strs.parseError)
    String getParseError();
    LsqQuery setParseError(String text);

    // Note: org.topbraid.spin.model.Query has no registered implementation
    @Iri(LSQ.Strs.hasSpin)
    Resource getSpinQuery();
    LsqQuery setSpinQuery(Resource resource);

    @Iri(LSQ.Strs.hash)
    String getHash();
    LsqQuery setHash(String hash);

    // TODO We should investigate whether an extension of the model to shacl makes sense
    // The main question is which (sub-)set of all possible
    // sparql queries can be represented as shacl

    @Iri(LSQ.Strs.hasStructuralFeatures)
    LsqStructuralFeatures getStructuralFeatures();
    LsqQuery setStructuralFeatures(LsqStructuralFeatures r);

    @Iri(LSQ.Strs.hasLocalExec)
    <T extends Resource> Set<T> getLocalExecutions(Class<T> itemClazz);

    // Set<LocalExecution> getLocalExecutions();

    //Set<>getLocalExecutions();

    @Iri(LSQ.Strs.hasRemoteExec)
    <T extends Resource> Set<T> getRemoteExecutions(Class<T> itemClazz);


    default LsqQuery setQueryAndHash(String str) {
        String hash = Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();

        setText(str);
        setHash(hash);

        return this;
    }

    default LsqQuery setQueryAndHash(Query query) {
        String str = query.toString();
        setQueryAndHash(str);

        return this;
    }
//    @ToString
//    default String asString() {
//        return toString() + " " + getText();
//    }
    // Set<RemoteExecution> getRemoteExecutions();
}

