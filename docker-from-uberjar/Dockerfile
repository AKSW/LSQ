# IMPORTANT: This dockerfile is merely a fallback!
# The recommended way to build a docker image from this java project is using the jib maven plugin
# on the module lsq-pkg-docker-cli:
# mvn clean install
# mvn -pl :lsq-pkg-docker-cli jib:dockerBuild

# Building this Dockerfile requires buildkit:
# Ensure { "features": { "buildkit": true } } exists in /etc/docker/daemon.json
# (or wherever your deamon.json resides)

ARG home="/lsq"

# Final running image
FROM openjdk:11-jre-slim
ARG home
ENV HOME "$home"
# Import the lsq-cli jar from the build step
ADD lsq-pkg-uberjar-cli-*-jar-with-dependencies.jar "$HOME/lsq-cli.jar"

# Using /data as working directory that will be shared with host for input/output files
WORKDIR /data
VOLUME [ "/data" ]

# XXX $HOME Does not get expanded in the entry point
ENTRYPOINT ["java","-jar","/lsq/lsq-cli.jar"]
CMD ["-h"]

# Usage:
# docker run -it -v $(pwd):/data ghcr.io/aksw/lsq rx rdfize --endpoint=http://dbpedia.org/sparql virtuoso.dbpedia.log

