#!/bin/bash

lsq \
  ../lsq-core/src/test/resources/logs/wikidata.wikidata.20170612-10.log \
  -m wikidata \
  -e https://query.wikidata.org/sparql \
  -l wikidata \
  -b http://lsq.aksw.org/res/ \
  -p https://query.wikidata.org/sparql \
  -a 'Linked Sparql Queries (lsq.aksw.org) <saleem.muhammd@gmail.com>' \
  -y 1000 \
  -r qel \
  -t 10000

