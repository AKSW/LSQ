lsq \
  -f lsq-example-swdf.sparql \
  -m sparql \
  -e http://localhost:1900/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://data.semanticweb.org/sparql \
  -h 10 \
  -t 60000 \
  -r qel | rapper -i turtle -o ntriples - http://foo | sort -u > lsq-example-swdf.nt
