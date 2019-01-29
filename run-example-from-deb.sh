#!/bin/bash
file=`find lsq-cli -name '*-jar-with-dependencies.jar'`

command_exists () { command -v "$1" >/dev/null 2>&1; }

if ! command_exists "lsq"; then
  ( mvn clean install && ./reinstall-deb.sh )
fi

#  -f lsq-core/src/test/resources/logs/combined.swdf.log \
lsq \
  lsq-core/src/test/resources/logs/combined.swdf.log \
  -e http://localhost:8890/sparql \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://localhost/service/org.semanticweb.swdf_swdf-full_latest_public_sparql \
  -h 100 \
  -r qel \
  -t 10000 | rapper -i turtle -o turtle - http://foo

