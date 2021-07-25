# Benchmarking 

Setting up a benchmark involves three steps:
* Creating a benchmark configuration
* Creating a benchmark execution from a configuration
* Actually running the execution

# Creating a benchmark configuration
This step requires a SPARQL endpoint with a benchmark dataset to be accessible.
The essential arguments are the **URL to the endpoint** and a **label for the dataset** hosted in the endpoint.

A certain URL of a SPARQL endpoint may host many RDF graphs / [datasets](What-Is-A-Dataset) (= specific sets of triples or quads) as time passes by. Usually, these datasets do not have agreed upon labels or identifiers; but having a rather generic label - such as 'dbpedia' (whic specific subset of the hundreds of DBpedia files does this involve?) - is better than having no indication of the dataset at all.
This is particularly true if benchmarking is performed locally using `localhost` URLs.

Hence, an endpoint URL and a dataset label have to be provided to the `benchmark create` command:

```bash
lsq benchmark create --endpoint http://localhost:8890/sparql --dataset mydataset -o

# Short version:
# lsq benchmark create -e http://localhost:8890/sparql -d mydataset -o

```
The result of the invocation is a turtle document with several paramaters which can now be adjusted:

> :warning: **You should NEVER change a configuration once you executed a run from it. Conversely: All runs created from a specific configuration should use the same parameters in order to facilitate comparability.**

```turtle
@prefix dct:   <http://purl.org/dc/terms/> .
@prefix lsqo:  <http://lsq.aksw.org/vocab#> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix lsqr:  <http://lsq.aksw.org/> .
@prefix sp:    <http://spinrdf.org/sp#> .

lsqr:xc-mydataset_2020-08-12
        lsqo:baseIri                  lsqr: ;
        lsqo:connectionTimeoutForCounting
                "60"^^xsd:decimal ;
        lsqo:connectionTimeoutForRetrieval
                "60"^^xsd:decimal ;
        lsqo:datasetLabel             "mydataset" ;
        lsqo:datasetSize              "2005615"^^xsd:long ;
        lsqo:endpoint                 [ a       <http://w3id.org/rpif/vocab#DataRefSparqlEndpoint> ;
                                        <http://w3id.org/rpif/vocab#serviceUrl>
                                                <http://localhost:8890/sparql>
                                      ] ;
        lsqo:executionTimeoutForCounting
                "300"^^xsd:decimal ;
        lsqo:executionTimeoutForRetrieval
                "300"^^xsd:decimal ;
        lsqo:maxByteSizeForCounting   "-1"^^xsd:long ;
        lsqo:maxByteSizeForSerialization
                "1000000"^^xsd:long ;
        lsqo:maxCount                 "1000000000"^^xsd:long ;
        lsqo:maxItemCountForCounting  "1000000"^^xsd:long ;
        lsqo:maxItemCountForSerialization
                "-1"^^xsd:long ;
        lsqo:userAgent                "Linked SPARQL Queries (LSQ) Client" ;
        dct:identifier                "xc-mydataset_2020-08-12" .

```

The next step is to prepare a benchmark run - this essentially just takes your config and associates it with a timestamp:

```
lsq benchmark prepare -c conf.ttl
# Output is the path to the generated run file e.g. /home/foo/conf.run.ttl
```


The final step is to actually start the benchmark
```
lsq benchmark run -c conf.run.ttl log.trig foo/*.trig
```

### Benchmark output

```trig
@prefix dct:   <http://purl.org/dc/terms/> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix lsqr:  <http://lsq.aksw.org/> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix lsq:   <http://lsq.aksw.org/vocab#> .
@prefix sp:    <http://spinrdf.org/sp#> .
@prefix prov:  <http://www.w3.org/ns/prov#> .

lsqr:lsqQuery-EgNvOe-ZxwO8m3IKDBcJ_Zl0LjxTvg8Qc3S4Tzx-pHU {
    lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:itemCount          "0"^^xsd:long ;
            lsq:retrievalDuration  0.007644999 ;
            lsq:evalDuration       0.026379681 ;
            prov:atTime            "2020-08-11T05:18:46.622Z"^^xsd:dateTime ;
            lsq:serializedResult   "{ \"head\": {\n    \"vars\": [ \"obj\" , \"prop\" , \"target\" ]\n  } ,\n  \"results\": {\n    \"bindings\": [\n      \n    ]\n  }\n}\n" .
    lsqr:tpInBgpExec-pbEYvVIs4pTVr-7o6GmGGHrJYFYBPCi261VfMI6WjG4
            lsq:hasTpExec     lsqr:tpExec-Q-TbuUU5ztTM5hBi6lDgqmf-fNO1gise7cr_LPd_mN8-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpToBgpRatio  "1"^^xsd:decimal .
    lsqr:tpInBgp-EOY2pwC-i7ETHnbe7TeYxOOwSmDk_LYiiZkzhPGluxE
            lsq:hasBgp  lsqr:spinBgp-N6V9HpwcxpeSJ5LEZylOhOJbM2KDn6JZlOpxGH1rtWo ;
            lsq:hasTp   lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q .
    lsqr:tpExec-Scgux0RDi5eq1Vc6DErilLKeREa8Qx-Ujw6dh4ztpQ0-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasElementExec      lsqr:queryExec-j2L7CF8oD8UGfMAlrQWPIela8SL8V-6X0bp2RHmVtZo-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:bgpRestrictedTpSel  "0"^^xsd:decimal ;
            lsq:tpToGraphRatio      "0"^^xsd:decimal .
    lsqr:spin-v_iFDK8Tia4M-vRyPWDZTQLeLOqM_bE6qkCkIMdo0Ps
            rdf:rest   rdf:nil ;
            rdf:first  lsqr:spin-qmK0hbqqKi3zTv9boYsdWHIrg__NPAddXdRKZ5ZY4bc .
    lsqr:bgpNode-tksKM3RKuoR1xOYOeAnLhW9UberHtNcRgXs9y3kecj0
            lsq:joinVertexType    lsq:star ;
            lsq:joinVertexDegree  "2"^^xsd:long ;
            lsq:proxyFor          lsqr:spin-rvuFfBhsMaKroY2qSrsH48qTFb8cy6PXPi0fhhK9Dgc ;
            rdf:subject           lsqr:bgpNode-tksKM3RKuoR1xOYOeAnLhW9UberHtNcRgXs9y3kecj0 ;
            rdfs:label            "?obj" ;
            sp:varName            "obj" ;
            rdf:type              lsq:Vertex ;
            lsq:hasSubBgp         lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc ;
            lsq:hasExec           lsqr:bgpNodeExec-dmX7_9uqE4vWshXZksasymqJaKs2IwFFLT62daS3wqQ-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:out               lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI .
    lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs
            lsq:in         lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI ;
            rdf:object     lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs ;
            rdf:predicate  lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs ;
            rdf:type       lsq:Vertex ;
            lsq:proxyFor   lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs ;
            rdfs:label     "<http://data.semanticweb.org/ns/swc/ontology#SessionEvent>" ;
            rdfs:label     "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>" ;
            lsq:hasSubBgp  lsqr:spinBgp-N6V9HpwcxpeSJ5LEZylOhOJbM2KDn6JZlOpxGH1rtWo .
    lsqr:localExec-rzA7kv8heeyeL739mFYuJTf07nnIWSpdPXqh7WzpIUM-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:benchmarkRun  lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasQueryExec  lsqr:queryExec-oFGuXvUblLiqdcUA_89opktjeaPouGBA9ygxSB_dIwU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:tpInBgpExec-Nuz_w7DAGN1yDc8E7ZeB2v03fQ7cUJPkWCuUDl1NHCQ
            lsq:hasTpExec           lsqr:tpExec-Scgux0RDi5eq1Vc6DErilLKeREa8Qx-Ujw6dh4ztpQ0-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpToBgpRatio        "0"^^xsd:decimal ;
            lsq:bgpRestrictedTpSel  "0"^^xsd:decimal .
    lsqr:spin-SuzIussubharnVxa5XgJQUvEO2od1Aj2cY4-eNri_G4
            sp:varName  "target" .
    lsqr:tpExec-Q-TbuUU5ztTM5hBi6lDgqmf-fNO1gise7cr_LPd_mN8-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasElementExec  lsqr:queryExec-oFGuXvUblLiqdcUA_89opktjeaPouGBA9ygxSB_dIwU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpToGraphRatio  0.0044913905 .
    lsqr:lsqQuery-rx8oX-HB90hd43p937hIUOIFkK3YEQv016G_U5XbTss
            lsq:text          "PREFIX  swc:  <http://data.semanticweb.org/ns/swc/ontology#>\n\nSELECT  *\nWHERE\n  { ?obj  a  swc:SessionEvent}\n" ;
            lsq:hash          "rx8oX-HB90hd43p937hIUOIFkK3YEQv016G_U5XbTss" ;
            lsq:hasLocalExec  lsqr:localExec--RHvYthYsTEAn118ozizrrKrhIyF1WA6m_lMIt6qasQ-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:tpInBgp-t-J5bU3XRs89PSKtYTgKEDp5HlfXQQ68cCBCZas7QPI
            lsq:hasBgp  lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc ;
            lsq:hasTp   lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10 .
    lsqr:queryExec-3BKy_58tj3jFqYfOWOZ8xkJQANahrRUSDB8vINGPCXw-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:itemCount          "0"^^xsd:long ;
            lsq:retrievalDuration  0.005128239 ;
            lsq:evalDuration       0.006120587 ;
            prov:atTime            "2020-08-11T05:18:46.652Z"^^xsd:dateTime ;
            lsq:serializedResult   "{ \"head\": {\n    \"vars\": [ \"prop\" ]\n  } ,\n  \"results\": {\n    \"bindings\": [\n      \n    ]\n  }\n}\n" .
    lsqr:spin-rvuFfBhsMaKroY2qSrsH48qTFb8cy6PXPi0fhhK9Dgc
            sp:varName  "obj" .
    lsqr:spin-AUUQflivQlGaqjRxTk6O_R2M_Lk3MvDyt8G34IZq4wA
            rdf:type       sp:Filter ;
            sp:expression  lsqr:spin-4yGM6XZ3kgf-IR94iuHKxUSDTFm1hcKu5M92d26AMAc .
    lsqr:queryExec-oFGuXvUblLiqdcUA_89opktjeaPouGBA9ygxSB_dIwU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            prov:atTime            "2020-08-11T05:18:09.704Z"^^xsd:dateTime ;
            lsq:retrievalDuration  36.893436913 ;
            lsq:evalDuration       36.894111175 ;
            lsq:itemCount          "9008"^^xsd:long ;
            lsq:exceededMaxByteSizeForSerialization  true .
    lsqr:lsqQuery-QeabIjWe1lvJy-o_1V6zzR2Wa1MMgJLBYLJVepv4XX0
            lsq:text          "PREFIX  swc:  <http://data.semanticweb.org/ns/swc/ontology#>\n\nSELECT  *\nWHERE\n  { ?obj  a      swc:SessionEvent ;\n          ?prop  ?target}\n" ;
            lsq:hash          "QeabIjWe1lvJy-o_1V6zzR2Wa1MMgJLBYLJVepv4XX0" ;
            lsq:hasLocalExec  lsqr:localExec-nQvlc9nBzTGjv6CxnV7Rqe8tCC0W0Hi9IPyiCQTEnPU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc_1NUtYys8ZuT8V2xJdgm95np8vbximoaJ74glxEhiXmY
            rdf:first  lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q ;
            rdf:rest   lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc_kPbO4_nrBZitBU0AbldnePJYL2dM6RslzJC1g-yiH5I .
    lsqr:bgpExec-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasElementExec  lsqr:queryExec-oFGuXvUblLiqdcUA_89opktjeaPouGBA9ygxSB_dIwU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasTpInBgpExec  lsqr:tpInBgpExec-pbEYvVIs4pTVr-7o6GmGGHrJYFYBPCi261VfMI6WjG4 .
    lsqr:queryExec-j2L7CF8oD8UGfMAlrQWPIela8SL8V-6X0bp2RHmVtZo-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:itemCount          "0"^^xsd:long ;
            lsq:retrievalDuration  0.003592758 ;
            lsq:evalDuration       0.004300718 ;
            prov:atTime            "2020-08-11T05:18:46.661Z"^^xsd:dateTime ;
            lsq:serializedResult   "{ \"head\": {\n    \"vars\": [ \"obj\" ]\n  } ,\n  \"results\": {\n    \"bindings\": [\n      \n    ]\n  }\n}\n" .
    lsqr:lsqQuery-EgNvOe-ZxwO8m3IKDBcJ_Zl0LjxTvg8Qc3S4Tzx-pHU
            lsq:hasLocalExec           lsqr:localExec-i_AmZGSAgHR7Cl1j2NgC6wI0_mHTz931Owpibhv1_4Q-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasSpin                lsqr:spin-p-Ez-BREln4biCCIpe0aw3f5gbvlFcpGBMhX17L52j8 ;
            lsq:text                   "\nPREFIX swc:<http://data.semanticweb.org/ns/swc/ontology#>\nSELECT DISTINCT ?prop WHERE { ?obj a swc:SessionEvent ; ?prop ?target . FILTER isLiteral(?target) } LIMIT 150\n" ;
            lsq:hash                   "000016c7f62114c1288b3b24b4e09074e442d6c51428ebe52097bdf864f9e2b7" ;
            lsq:hasRemoteExec          lsqr:re-query.wikidata.org-sparql_2017-06-12T03:34:34Z ;
            lsq:hasRemoteExec          lsqr:re-query.wikidata.org-sparql_2017-06-12T02:34:24Z ;
            lsq:hasRemoteExec          lsqr:re-query.wikidata.org-sparql_2017-06-12T04:35:15Z ;
            lsq:hasStructuralFeatures  lsqr:lsqQuery-EgNvOe-ZxwO8m3IKDBcJ_Zl0LjxTvg8Qc3S4Tzx-pHU-sf .
    lsqr:spin-qmK0hbqqKi3zTv9boYsdWHIrg__NPAddXdRKZ5ZY4bc
            sp:varName  "prop" .
    lsqr:tpInBgpExec-UJFCQo9yJxM5341gKV2ZXBsnW_trV5WSLMapwbiHhac
            lsq:hasTpExec           lsqr:tpExec-Q-TbuUU5ztTM5hBi6lDgqmf-fNO1gise7cr_LPd_mN8-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpToBgpRatio        "0"^^xsd:decimal ;
            lsq:bgpRestrictedTpSel  "0"^^xsd:decimal .
    lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc_kPbO4_nrBZitBU0AbldnePJYL2dM6RslzJC1g-yiH5I
            rdf:first  lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10 ;
            rdf:rest   rdf:nil .
    lsqr:spinBgp-N6V9HpwcxpeSJ5LEZylOhOJbM2KDn6JZlOpxGH1rtWo_WSlW1fCqWrXdeR065S84TO2DU487RqFiEPSyXJHdkyM
            rdf:first  lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q ;
            rdf:rest   rdf:nil .
    lsqr:localExec--RHvYthYsTEAn118ozizrrKrhIyF1WA6m_lMIt6qasQ-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:benchmarkRun  lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasQueryExec  lsqr:queryExec-j2L7CF8oD8UGfMAlrQWPIela8SL8V-6X0bp2RHmVtZo-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:spin-tGkG9tQtjDUrqFXtcVqdDtEJDxfUQnJKChxM1-kxDSg
            rdf:rest   lsqr:spin-pRUN94eakTtNR_3a-C1Eou0SWeOP-QIKNB9O0BYAI0A ;
            rdf:first  lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q .
    lsqr:bgpNode-J1Z7QnZT999tdrrhQzM9ggXhZ8kAamCGlG1QIaV_N8o
            sp:varName     "prop" ;
            lsq:hasExec    lsqr:bgpNodeExec-uVqh0HSCYsiuh9G5SSPfMxC7TO-m7l9d-UI4yfBPmv4-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            rdf:predicate  lsqr:bgpNode-J1Z7QnZT999tdrrhQzM9ggXhZ8kAamCGlG1QIaV_N8o ;
            rdf:type       lsq:Vertex ;
            lsq:in         lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI ;
            rdfs:label     "?prop" ;
            lsq:proxyFor   lsqr:spin-qmK0hbqqKi3zTv9boYsdWHIrg__NPAddXdRKZ5ZY4bc ;
            lsq:hasSubBgp  lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU .
    lsqr:re-query.wikidata.org-sparql_2017-06-12T02:34:24Z
            lsq:endpoint  <https://query.wikidata.org/sparql> ;
            prov:atTime   "2017-06-12T02:34:24Z"^^xsd:dateTime .
    lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10
            lsq:extensionQuery  lsqr:lsqQuery-UyRbau8sjxlyd2Er23mZEkwpIxfG_E5-8wsQ1T6a6rA ;
            lsq:hasExec         lsqr:tpExec-Q-TbuUU5ztTM5hBi6lDgqmf-fNO1gise7cr_LPd_mN8-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            sp:object           lsqr:spin-SuzIussubharnVxa5XgJQUvEO2od1Aj2cY4-eNri_G4 ;
            sp:subject          lsqr:spin-rvuFfBhsMaKroY2qSrsH48qTFb8cy6PXPi0fhhK9Dgc ;
            sp:predicate        lsqr:spin-qmK0hbqqKi3zTv9boYsdWHIrg__NPAddXdRKZ5ZY4bc ;
            rdfs:label          "?obj ?prop ?target" .
    lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc
            lsq:hasBgpNode      lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs ;
            lsq:hasBgpNode      lsqr:bgpNode-J1Z7QnZT999tdrrhQzM9ggXhZ8kAamCGlG1QIaV_N8o ;
            rdfs:label          "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" ;
            lsq:hasEdge         lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI ;
            lsq:hasTp           lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc_1NUtYys8ZuT8V2xJdgm95np8vbximoaJ74glxEhiXmY ;
            lsq:hasBgpNode      lsqr:bgpNode-tksKM3RKuoR1xOYOeAnLhW9UberHtNcRgXs9y3kecj0 ;
            lsq:extensionQuery  lsqr:lsqQuery-QeabIjWe1lvJy-o_1V6zzR2Wa1MMgJLBYLJVepv4XX0 ;
            lsq:hasExec         lsqr:bgpExec-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasTpInBgp      lsqr:tpInBgp-t-J5bU3XRs89PSKtYTgKEDp5HlfXQQ68cCBCZas7QPI ;
            lsq:hasBgpNode      lsqr:bgpNode-c8HriOFeLbr5Q8KR_omt9sFh9jeo-7nO6jKElE--2z4 ;
            lsq:hasTpInBgp      lsqr:tpInBgp-pa_PLW5lANHOMotGgbd72rWR7059yCn2XS4AxTPrrLc .
    lsqr:spin-pRUN94eakTtNR_3a-C1Eou0SWeOP-QIKNB9O0BYAI0A
            rdf:rest   lsqr:spin-T0z6KY6MseTHK2gfjx6L24PhNkB2_bAn7GFnffJGuvE ;
            rdf:first  lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10 .
    lsqr:re-query.wikidata.org-sparql_2017-06-12T04:35:15Z
            lsq:endpoint  <https://query.wikidata.org/sparql> ;
            prov:atTime   "2017-06-12T04:35:15Z"^^xsd:dateTime .
    lsqr:bgpExec-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasElementExec  lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasJoinVarExec  lsqr:bgpNodeExec-dmX7_9uqE4vWshXZksasymqJaKs2IwFFLT62daS3wqQ-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasJoinVarExec  lsqr:bgpNodeExec-uVqh0HSCYsiuh9G5SSPfMxC7TO-m7l9d-UI4yfBPmv4-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasTpInBgpExec  lsqr:tpInBgpExec-Nuz_w7DAGN1yDc8E7ZeB2v03fQ7cUJPkWCuUDl1NHCQ ;
            lsq:hasJoinVarExec  lsqr:bgpNodeExec-2iTi9huHQ4q5ANXYtYDKJWAnlSKEVoIhXuY30G4bXXE-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasTpInBgpExec  lsqr:tpInBgpExec-UJFCQo9yJxM5341gKV2ZXBsnW_trV5WSLMapwbiHhac .
    lsqr:bgpNodeExec-uVqh0HSCYsiuh9G5SSPfMxC7TO-m7l9d-UI4yfBPmv4-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasElementExec          lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpSelJoinVarRestricted  "0"^^xsd:decimal ;
            lsq:hasSubBgpExec           lsqr:bgpExec-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:re-query.wikidata.org-sparql_2017-06-12T03:34:34Z
            lsq:endpoint  <https://query.wikidata.org/sparql> ;
            prov:atTime   "2017-06-12T03:34:34Z"^^xsd:dateTime .
    lsqr:spin-p-Ez-BREln4biCCIpe0aw3f5gbvlFcpGBMhX17L52j8
            rdf:type            sp:Select ;
            sp:distinct         true ;
            sp:limit            "150"^^xsd:long ;
            sp:resultVariables  lsqr:spin-v_iFDK8Tia4M-vRyPWDZTQLeLOqM_bE6qkCkIMdo0Ps ;
            sp:where            lsqr:spin-tGkG9tQtjDUrqFXtcVqdDtEJDxfUQnJKChxM1-kxDSg .
    lsqr:spinBgp-N6V9HpwcxpeSJ5LEZylOhOJbM2KDn6JZlOpxGH1rtWo
            lsq:hasTp       lsqr:spinBgp-N6V9HpwcxpeSJ5LEZylOhOJbM2KDn6JZlOpxGH1rtWo_WSlW1fCqWrXdeR065S84TO2DU487RqFiEPSyXJHdkyM ;
            lsq:hasTpInBgp  lsqr:tpInBgp-EOY2pwC-i7ETHnbe7TeYxOOwSmDk_LYiiZkzhPGluxE .
    lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU
            lsq:extensionQuery  lsqr:lsqQuery-UyRbau8sjxlyd2Er23mZEkwpIxfG_E5-8wsQ1T6a6rA ;
            lsq:hasTpInBgp      lsqr:tpInBgp-Y9ATdCQDvVhXJNO0xcsQOVci0iIoxIOtR5r9va8dy2s ;
            lsq:hasTp           lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU_c3N751lG6MdZcumRitZVJVhAYzZJa6DaKIqkqnjXR50 ;
            rdfs:label          "?obj  ?prop  ?target" ;
            lsq:hasExec         lsqr:bgpExec-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q
            lsq:extensionQuery  lsqr:lsqQuery-rx8oX-HB90hd43p937hIUOIFkK3YEQv016G_U5XbTss ;
            lsq:hasExec         lsqr:tpExec-Scgux0RDi5eq1Vc6DErilLKeREa8Qx-Ujw6dh4ztpQ0-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            sp:subject          lsqr:spin-rvuFfBhsMaKroY2qSrsH48qTFb8cy6PXPi0fhhK9Dgc ;
            rdfs:label          "?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.semanticweb.org/ns/swc/ontology#SessionEvent>" ;
            sp:predicate        lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs ;
            sp:object           lsqr:bgpNode-HmAMGylYPCdNAdDlX0nlY0SzoHM641Sb0viRYJogAfs .
    lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU_c3N751lG6MdZcumRitZVJVhAYzZJa6DaKIqkqnjXR50
            rdf:first  lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10 ;
            rdf:rest   rdf:nil .
    lsqr:lsqQuery-UyRbau8sjxlyd2Er23mZEkwpIxfG_E5-8wsQ1T6a6rA
            lsq:text          "SELECT  *\nWHERE\n  { ?obj  ?prop  ?target}\n" ;
            lsq:hash          "UyRbau8sjxlyd2Er23mZEkwpIxfG_E5-8wsQ1T6a6rA" ;
            lsq:hasLocalExec  lsqr:localExec-rzA7kv8heeyeL739mFYuJTf07nnIWSpdPXqh7WzpIUM-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:localExec-i_AmZGSAgHR7Cl1j2NgC6wI0_mHTz931Owpibhv1_4Q-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:benchmarkRun  lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasQueryExec  lsqr:queryExec-3BKy_58tj3jFqYfOWOZ8xkJQANahrRUSDB8vINGPCXw-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasBgpExec    lsqr:bgpExec-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:tpInBgp-pa_PLW5lANHOMotGgbd72rWR7059yCn2XS4AxTPrrLc
            lsq:hasBgp  lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc ;
            lsq:hasTp   lsqr:lsqTriplePattern-BRdmE1hzz53fPXJHRj_NAK0TmgFqBa7XLOfnxCg2i_Q .
    lsqr:localExec-nQvlc9nBzTGjv6CxnV7Rqe8tCC0W0Hi9IPyiCQTEnPU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:benchmarkRun  lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:hasQueryExec  lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:dataRefSparqlEndpoint-zFGdmLcrQYdbNV-39nKlA-qiv-lGQ1ndyHhNatbeV7s
            <http://w3id.org/rpif/vocab#namedGraph>  rdf:nil ;
            <http://w3id.org/rpif/vocab#defaultGraph>  rdf:nil .
    lsqr:bgpNodeExec-2iTi9huHQ4q5ANXYtYDKJWAnlSKEVoIhXuY30G4bXXE-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasSubBgpExec           lsqr:bgpExec-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpSelJoinVarRestricted  "0"^^xsd:decimal ;
            lsq:hasElementExec          lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:tpInBgp-Y9ATdCQDvVhXJNO0xcsQOVci0iIoxIOtR5r9va8dy2s
            lsq:hasBgp  lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU ;
            lsq:hasTp   lsqr:lsqTriplePattern-qXBx4XaN1x3As61TCZ0rtxV4Q4HGP5Jpf4fjEmlMy10 .
    lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI
            rdf:type  lsq:Edge .
    lsqr:bgpNode-c8HriOFeLbr5Q8KR_omt9sFh9jeo-7nO6jKElE--2z4
            lsq:hasSubBgp  lsqr:spinBgp-SdgnXnuODJUBMikrR6rdumdDYmvdGIcfp9yzuPW7SaU ;
            sp:varName     "target" ;
            rdf:object     lsqr:bgpNode-c8HriOFeLbr5Q8KR_omt9sFh9jeo-7nO6jKElE--2z4 ;
            rdf:type       lsq:Vertex ;
            lsq:in         lsqr:directedHyperEdge-vn7CMAhSXraArjJaiHqukNTuFLikeJIY0ApIgChwIjI ;
            rdfs:label     "?target" ;
            lsq:proxyFor   lsqr:spin-SuzIussubharnVxa5XgJQUvEO2od1Aj2cY4-eNri_G4 ;
            lsq:hasExec    lsqr:bgpNodeExec-2iTi9huHQ4q5ANXYtYDKJWAnlSKEVoIhXuY30G4bXXE-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:lsqQuery-EgNvOe-ZxwO8m3IKDBcJ_Zl0LjxTvg8Qc3S4Tzx-pHU-sf
            lsq:joinVertexDegreeMedian  "2"^^xsd:decimal ;
            lsq:tpInBgpCountMean        "2"^^xsd:decimal ;
            lsq:bgpCount                "1"^^xsd:int ;
            lsq:tpInBgpCountMax         "2"^^xsd:int ;
            lsq:hasBgp                  lsqr:spinBgp-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc ;
            lsq:joinVertexCountTotal    "1"^^xsd:int ;
            lsq:tpInBgpCountMedian      "2"^^xsd:decimal ;
            lsq:usesFeature             lsq:fn-isLiteral ;
            lsq:projectVarCount         "1"^^xsd:int ;
            lsq:usesFeature             lsq:Group ;
            lsq:usesFeature             lsq:Select ;
            lsq:tpInBgpCountMin         "2"^^xsd:int ;
            lsq:joinVertexDegreeMean    "2"^^xsd:decimal ;
            lsq:usesFeature             lsq:Limit ;
            lsq:usesFeature             lsq:Functions ;
            lsq:usesFeature             lsq:TriplePattern ;
            lsq:usesFeature             lsq:Distinct ;
            lsq:usesFeature             lsq:Filter .
    lsqr:spin-T0z6KY6MseTHK2gfjx6L24PhNkB2_bAn7GFnffJGuvE
            rdf:rest   rdf:nil ;
            rdf:first  lsqr:spin-AUUQflivQlGaqjRxTk6O_R2M_Lk3MvDyt8G34IZq4wA .
    lsqr:bgpNodeExec-dmX7_9uqE4vWshXZksasymqJaKs2IwFFLT62daS3wqQ-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05
            lsq:hasSubBgpExec           lsqr:bgpExec-ugE2CPeCkkYbkTN_pEebrs1kPUEy4-3Cww7mwOTMYPc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 ;
            lsq:tpSelJoinVarRestricted  "0"^^xsd:decimal ;
            lsq:hasElementExec          lsqr:queryExec--XFLs9iShsDqE9xa0mGAGSpK6dU0uxwXLCqL3WRebUc-xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 .
    lsqr:spin-4yGM6XZ3kgf-IR94iuHKxUSDTFm1hcKu5M92d26AMAc
            rdf:type  sp:isLiteral ;
            sp:arg1  lsqr:spin-SuzIussubharnVxa5XgJQUvEO2od1Aj2cY4-eNri_G4 .
}
```