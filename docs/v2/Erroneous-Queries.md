This is a collection of queries with various errors. It is useful for further improvement of the test suite and LSQ2 as a whole.

:warning: LSQ2 confusingly yields 'internal error; continuing anyway' for the following type of error:
```sparql
SELECT DISTINCT ?var1  ?var1Label  ?var2 (  SAMPLE ( ?var3  ) AS  ?var3  ) ?var4 
WHERE {
  ?var1  <http://www.wikidata.org/prop/direct/P2003>  <http://www.wikidata.org/entity/Q44437> .
 OPTIONAL {
  ?var1  <http://www.wikidata.org/prop/direct/P18>  ?var3 .
 }
 OPTIONAL {
  ?var4  <http://schema.org/about>  ?var1 .
  ?var4  <http://schema.org/isPartOf>  <https://en.wikipedia.org/> .
 }
 SERVICE  <http://wikiba.se/ontology#label>   {
    <http://www.bigdata.com/rdf#serviceParam>  <http://wikiba.se/ontology#language>  "[AUTO_LANGUAGE],en,fr,es,de,ru,it,nl,ja,zh,pl,cs".
  }
}
GROUP BY  ?var1  ?var1Label  ?var2  ?var4 
LIMIT 50
```

```
org.apache.jena.query.QueryParseException: Variable used when already in-scope: ?var3 in ((AGG ?.0 SAMPLE(?var3)) AS ?var3)
```
