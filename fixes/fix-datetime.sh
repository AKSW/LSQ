#!/bin/bash
sed -r 's|"(.*)\^\^(.*)"\^\^<java:org.apache.jena.rdf.model.impl.LiteralImpl>|"\1"^^<\2>|g'
#"2015-10-24T01:00:00Z^^http://www.w3.org/2001/XMLSchema#dateTime"^^<java:org.apache.jena.rdf.model.impl.LiteralImpl> .
