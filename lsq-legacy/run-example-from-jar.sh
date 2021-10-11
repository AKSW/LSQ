#!/bin/bash
file=`find lsq-cli -name '*-jar-with-dependencies.jar'`

if [ ! -f "$file" ]; then
  ( mvn clean install && cd lsq-cli && mvn assembly:assembly )
fi

file=`find lsq-cli -name '*-jar-with-dependencies.jar'`

#echo "$file"

java -cp "$file" org.aksw.simba.lsq.cli.main.MainLSQ \
  -e http://localhost:8890/sparql \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://localhost/service/org.semanticweb.swdf_swdf-full_latest_public_sparql \
  -h 100 \
  -r qel \
  -t 10000 \
  lsq-core/src/test/resources/logs/combined.swdf.log | rapper -i turtle -o turtle - http://foo

