# Skolemization

LSQ relies on the external framework [mapper-proxy](https://github.com/SmartDataAnalytics/jena-sparql-api/tree/master/jena-sparql-api-mapper-proxy) to generate structural hash IDs, local string IDs and eventually global IRIs. 

LSQ first creates a model full of blank nodes. However, this initial model may contain many redundant structures, for example when the same triple pattern from two different basic graph patterns is converted to RDF.
LSQ uses a three phase process in order to consistently remap structures to IRIs based on a structural hashing in order to eleminate redundancy.
The process is deterministic - the same information with the same identitifying attributes yields the same local IDs - but in order to craft IRIs an authority is needed - adding your own can be easily accomplished.
These phases are:

* Assignment of RDF nodes to HashIds. This is based on an annotated Java domain model where methods are annotated with references to RDF properties and `@HashId`. HashIds are conceptually byte sequences which may include non-printable characters and are implemented using Guava [HashCode](https://guava.dev/releases/19.0/api/docs/com/google/common/hash/HashCode.html).
* Derivation of StringIds. Whereas HashIds are generic structural IDs, StringIds are custom strings that may or may not include HashIds. By default, the StringId is composed of the components
  * class name in lower camel case
  * `-`
  * base64url encoding of the hash id
* Mapping StringIds to IRIs. Typically one just adds a IRI-prefix to the StringIds, leading to IRIs such as `http://www.example.org/someClass-abcdefg`.

## Examples of Use in LSQ

* The identity of a [Bgp](https://github.com/AKSW/LSQ/blob/develop/lsq-core/src/main/java/org/aksw/simba/lsq/spinx/model/Bgp.java) is the set of its triples. Hence the method `getTriplePatterns` is declared as
```java
interface Bgp
   extends Resource
{
    @HashId
    @Iri(LSQ.Strs.hasTp)
    List<LsqTriplePattern> getTriplePatterns();
}
```

Note, that we used Lists instead of Sets to model triple patterns because triple pattern ordering may impact query execution performance on SPARQL engines. The [mapper-proxy](https://github.com/SmartDataAnalytics/jena-sparql-api/tree/master/jena-sparql-api-mapper-proxy) framework automatically yields a mutable `java.util.List` implementation that reads/writes to an appropriate [RDFList](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/RDFList.html).

> :wrench: You thought rdf:List was a pain to use? It's not. The framework will take care of skolemizing the "invisible" entry nodes of the rdf:List based on the identity of the elements and that of the owning instance.

* The identity of a "triple pattern in a bgp" - named [TpInBgp](https://github.com/AKSW/LSQ/blob/develop/lsq-core/src/main/java/org/aksw/simba/lsq/spinx/model/TpInBgp.java) - depends on a given Bgp and a Tp. The declaration thus looks like:

```java
interface TpInBgp
  extends Resource {
    @HashId
    @Iri(LSQ.Strs.hasBgp)
    SpinBgp getBgp();
    TpInBgp setBgp(Resource bgp);

    @HashId
    @Iri(LSQ.Strs.hasTp)
    LsqTriplePattern getTriplePattern();
    TpInBgp setTriplePattern(Resource tp);
}
```

* An LSQ query has a single corresponding `StructuralFeatures` resource. Its HashId is that of the query plus the class name.
However, as for the StringId we just want to append a `-sf` suffix to the @StringId of the query:

```java
@ResourceView
@HashId // <-- Inclcude the full class name in the ID
public interface LsqStructuralFeatures
    extends Resource, BgpInfo
{
    @Iri(LSQ.Strs.hasStructuralFeatures)
    @Inverse
    @HashId(excludeRdfProperty = true) // <-- Do not make LSQ.Strs.hasStructuralFeatures part of the ID
    LsqQuery getQuery();


    // Custom @StringId handler that sets the ID of structural features to that of the query with a `-sf` suffix.
    @StringId
    default String getStringId(HashIdCxt cxt) {
        LsqQuery query = getQuery();
        String queryId = cxt.getString(query) + "-sf";
        return queryId;
    }
}
```

### Computing the IDs and Renaming Blank Nodes
```java
// Given a start resource of an appropriately annotated class
Bgp start = ModelFactory.createDefaultModel().createResource().as(Bgp.class);

// Fill out the model (ID computation of course requires all relevant information to be present in the model)
process(start)

// The following line carries out computation for the *whole*
// reachable RDF graph framework via the annotated Java interface / class:

// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
HashIdCxt hashIdCxt = MapperProxyUtils.getHashId(start);
// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

// The mapping of RDFNodes to string IDs (not IRIs at this point) is obtained via
Map<RDFNode, String> renames = hashIdCxt.getStringMapping();

// Get a mapping from the original resources to the renamed ones.
Map<Resource, Resource> renameResources("http://www.example.org/", Map<RDFNode, String> renames);
```

### Output
See [Benchmarking](Benchmarking) for the complete output generated using this approach!
