package org.aksw.simba.lsq.model;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.arq.util.syntax.QueryHash;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;


/**
 * This class is main entry point for accessing information about a query in LSQ.
 *
 * A practical difference between an lsq:Query and a spin:Query is that the ID (a hashed value) of lsq:Query
 * is computed from the query string, whereas for spin:Query is is obtained via skolemization of the tree structure
 * of the SPIN representation.
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
    @HashId(excludeRdfProperty = true)
    @Iri(LSQ.Terms.text)
    String getText();
    LsqQuery setText(String text);

    @Iri(LSQ.Terms.parseError)
    String getParseError();
    LsqQuery setParseError(String text);

    // Note: org.topbraid.spin.model.Query has no registered implementation
    @Iri(LSQ.Terms.hasSpin)
    Resource getSpinQuery();
    LsqQuery setSpinQuery(Resource resource);

    @Iri(LSQ.Terms.hash)
    String getHash();
    LsqQuery setHash(String hash);

    // TODO We should investigate whether an extension of the model to shacl makes sense
    // The main question is which (sub-)set of all possible
    // sparql queries can be represented as shacl

    @Iri(LSQ.Terms.hasStructuralFeatures)
    LsqStructuralFeatures getStructuralFeatures();
    LsqQuery setStructuralFeatures(Resource r);

//    @Iri(LSQ.Strs.hasLocalExec)
//    <T extends Resource> Set<T> getLocalExecutions(Class<T> itemClazz);
    @Iri(LSQ.Terms.hasLocalExec)
    Set<LocalExecution> getLocalExecutions();

    // Set<LocalExecution> getLocalExecutions();

    //Set<>getLocalExecutions();

    // FIXME Dynamically typed collections don't properly work with @HashId yet
//    @Iri(LSQ.Strs.hasRemoteExec)
//    <T extends Resource> Set<T> getRemoteExecutions(Class<T> itemClazz);

    @Iri(LSQ.Terms.hasRemoteExec)
    Set<RemoteExecution> getRemoteExecutions();

    @StringId
    default String getStringId(HashIdCxt cxt) {
        String prefix = StringUtils.toLowerCamelCase(LsqQuery.class.getSimpleName());
        String hash = getHash();
        if (hash == null) {
            hash = cxt.getHashAsString(this);
        }
        // Note: Dot '.' is not part of base64 encoding
        // This allows to use a hash as a path segment in an URL without introducing ambiguity
        return prefix + "-" + hash.replace('/', '.');
    }

    /**
     * Index of remote executions by the experiment config resource
     * @return
     */
    default Map<Resource, LocalExecution> getLocalExecutionMap() {
        Set<LocalExecution> res = getLocalExecutions(); //LocalExecution.class);
        Map<Resource, LocalExecution> result = res.stream()
                .collect(Collectors.toMap(r -> r.getBenchmarkRun(), r -> r));
        return result;
    }

//  default Map<Resource, LocalExecution> indexLocalExecs() {
//  Set<LocalExecution> les = getLocalExecutions(LocalExecution.class);
//
//  Map<Resource, LocalExecution> result = les.stream()
//          .collect(Collectors.toMap(le -> le.getBenchmarkRun(), le -> le));
//
//  return result;
//}


    public static String createHashOld(String str) {
//        System.out.println("Hashing " + str.replace('\n', ' '));

        // FIXME The hash computation of the jsa-mapper includes the properties and use the RDF term serialization - so the hashes do not align
        // The solution would have to exclude the property from id generation and use java string representations of the RDF terms
        // such as @HashId(excludeProperty=true, toJavaString=true)

        // Node tmp = NodeFmtLib.str(NodeFactory.createLiteral(str));
        String tmp = str;
        HashCode hashCode = str == null ? null : Hashing.sha256().hashString(tmp, StandardCharsets.UTF_8);
        String result = hashCode == null ? null : BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        return result;
    }

    public static String createHash(String str) {
        String result;
        try {
            Query query = QueryFactory.create(str, Syntax.syntaxARQ);
            result = QueryHash.createHash(query).toString();
        } catch (Exception e) {
            result = createHashOld(str);
        }
        return result;
      }

    default LsqQuery setQueryAndHash(String str) {
        String hash = createHash(str);

        setText(str);
        setHash(hash);

        return this;
    }

    default LsqQuery updateHash() {
        String str = getText();
        String hash = createHash(str);
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

