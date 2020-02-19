## Work in Progress

The new version of LSQ now allows for decoupling of the RDFization of log files from the analysis.
Instead of having one monolithic processing command, the aim is to have easier-to-use smaller steps.

This makes the LSQ process more modular which in turn simplifies the use of LSQ, as instead of having to provide all arguments to a single
invocation, only those relevant to the processing step are needed - the RDFized log can be considered a re-useable asset.


### Probing for best matching format

The `lsq-probe` command attempts to parse a sample of `n` lines from a given file with all registered parser ordered by weight.
The weight corresponds to the average number of predicates obtained by parsing the sample using a format.
In detail, the weight is `w := parseRatio * avgNumberOfImmediatePredicates` with
`parseRatio = number of successfully parsed lines / n` and avgNumberOfImmediatePredicates is the average number of predicates among the successfully parsed lines.


```bash
$> lsq-probe wikidata-sample.log

wikidata-sample.log [wikidata, sparql2]
```




